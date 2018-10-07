/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.addons;

import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.blockchain.Block;
import nxt.blockchain.BlockchainProcessor;
import nxt.blockchain.Chain;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildBlockFxtTransactionType;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.FxtChain;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionProcessor;
import nxt.db.DbIterator;
import nxt.http.API;
import nxt.http.APICall;
import nxt.http.APIServlet;
import nxt.http.APITag;
import nxt.http.JSONData;
import nxt.http.JSONResponses;
import nxt.http.ParameterParser;
import nxt.http.responses.TransactionResponse;
import nxt.lightcontracts.ContractReference;
import nxt.messaging.PrunableEncryptedMessageAppendix;
import nxt.messaging.PrunablePlainMessageAppendix;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.ResourceLookup;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nxt.addons.AbstractContractContext.EventSource;

public final class ContractRunner implements AddOn, ContractProvider {

    private static final String CONFIG_FILE_PROPERTY = "addon.contractRunner.configFile";

    private ContractRunnerConfig config;
    private Map<String, Contract> supportedContracts = new HashMap<>();
    private Map<String, ContractReference> supportedContractTransactions = new HashMap<>();
    private Map<String, APIServlet.APIRequestHandler> apiRequests;
    private Map<String, ContractReference> addedContractReferences = new HashMap<>();
    private Map<String, ContractReference> deletedContractReferences = new HashMap<>();

    @Override
    public void init() {
        // Initialize contract runner APIs
        apiRequests = new HashMap<>();
        apiRequests.put("getSupportedContracts", new GetSupportedContractsAPI(new APITag[] {APITag.ADDONS}, "adminPassword"));
        apiRequests.put("triggerContractByTransaction", new TriggerContractByTransactionAPI(new APITag[] {APITag.ADDONS}, "triggerFullHash", "apply", "validate", "adminPassword"));
        apiRequests.put("triggerContractByHeight", new TriggerContractByHeightAPI(new APITag[] {APITag.ADDONS}, "contractName", "height", "apply", "adminPassword"));
        apiRequests.put("triggerContractByRequest", new TriggerContractByRequestAPI(new APITag[] {APITag.ADDONS}, "contractName", "setupParams", "adminPassword"));
        apiRequests.put("triggerContractByVoucher", new TriggerContractByVoucherAPI("voucher", new APITag[] {APITag.ADDONS}, "contractName", "adminPassword"));

        if (!Nxt.getServerStatus().isDatabaseReady()) {
            // For some utilities it is enough that we register the API even if Nxt itself is not initialized
            return;
        }

        // Read contract runner configuration
        String configFileName = Nxt.getStringProperty(CONFIG_FILE_PROPERTY);
        if (configFileName == null) {
            Logger.logErrorMessage("Contract configuration file " + CONFIG_FILE_PROPERTY + " not specified");
            return;
        }
        JO configJson = ResourceLookup.loadJsonResource(configFileName);
        if (configJson == null) {
            return;
        }
        config = new ContractRunnerConfig(configJson, this);

        try (DbIterator<ContractReference> iterator = ContractReference.getContractReferences(config.getAccountId(), null, 0, Integer.MAX_VALUE)) {
            while (iterator.hasNext()) {
                ContractLoader.loadContract(iterator.next(), supportedContracts, supportedContractTransactions);
            }
        }
        // Register listeners for contract activation
        Nxt.getBlockchainProcessor().addListener(this::processBlock, BlockchainProcessor.Event.AFTER_BLOCK_APPLY);
        Nxt.getTransactionProcessor().addListener(this::processConfirmed, TransactionProcessor.Event.ADDED_CONFIRMED_TRANSACTIONS);
        Nxt.getTransactionProcessor().addListener(this::processReleasedPhased, TransactionProcessor.Event.RELEASE_PHASED_TRANSACTION);
        ContractReference.addListener(this::contractAdded, ContractReference.Event.SET_CONTRACT_REFERENCE);
        ContractReference.addListener(this::contractDeleted, ContractReference.Event.DELETE_CONTRACT_REFERENCE);
        Logger.logInfoMessage("ContractRunner Started");
    }

    private void processConfirmed(List<? extends Transaction> transactions) {
        if (isSuspendContractExecution()) {
            return;
        }
        for (Transaction transaction : transactions) {
            if (transaction.isPhased() || transaction.getType() == ChildBlockFxtTransactionType.INSTANCE) {
                // We process phased transactions only when released and child chain block transactions cannot trigger a contract
                continue;
            }
            try {
                JSONObject contractResponse = processTransaction(transaction, true, config.isValidator());
                if (contractResponse != null) {
                    Logger.logInfoMessage("ContractRunner response: " + contractResponse.toJSONString());
                }
            } catch (Exception e) {
                ChainTransactionId txid = new ChainTransactionId(transaction.getChain().getId(), transaction.getFullHash());
                Logger.logErrorMessage("ContractRunner error for transaction " + txid + ": " + e.getMessage(), e);
            }
        }
    }

    private void processReleasedPhased(List<? extends Transaction> transactions) {
        if (isSuspendContractExecution()) {
            return;
        }
        for (Transaction transaction : transactions) {
            try {
                JSONObject contractResponse = processTransaction(transaction, true, config.isValidator());
                if (contractResponse != null) {
                    Logger.logInfoMessage("ContractRunner response: " + contractResponse.toJSONString());
                }
            } catch (Exception e) {
                ChainTransactionId txid = new ChainTransactionId(transaction.getChain().getId(), transaction.getFullHash());
                Logger.logErrorMessage("ContractRunner error for phased transaction " + txid + ": " + e.getMessage(), e);
            }
        }
    }

    private void contractAdded(ContractReference contractReference) {
        addedContractReferences.put(contractReference.getContractName(), contractReference);
    }

    private void contractDeleted(ContractReference contractReference) {
        deletedContractReferences.put(contractReference.getContractName(), contractReference);
    }

    private void processBlock(Block block) {
        addedContractReferences.forEach((contractName, contractReference) ->
                ContractLoader.loadContract(contractReference, supportedContracts, supportedContractTransactions));
        deletedContractReferences.forEach((contractName, contractReference) -> {
            supportedContracts.remove(contractName);
            supportedContractTransactions.remove(contractName);
        });
        addedContractReferences.clear();
        deletedContractReferences.clear();

        if (isSuspendContractExecution()) {
            return;
        }
        try {
            supportedContracts.keySet().forEach(contractName -> {
                try {
                    processBlockContract(block, contractName, true, false, null);
                } catch (Throwable t) {
                    throw new IllegalStateException("Contract " + contractName, t);
                }
            });
        } catch (Throwable t) {
            Logger.logErrorMessage("ContractRunner error: " + t.getMessage(), t);
        }
    }

    private JSONObject processBlockContract(Block block, String contractName, boolean isApply, boolean isValidator, Transaction transactionToApprove) {
        Contract contract = supportedContracts.get(contractName);
        if (contract == null) {
            return generateErrorResponse(1003, "Contract is not supported %s", contractName);
        }
        BlockContext context = new BlockContext(block, config, contractName);
        contract.processBlock(context);
        JO jo = context.getResponse();
        if (jo == null) {
            return null;
        }
        JSONObject contractResponse = jo.toJSONObject();
        Logger.logInfoMessage("ContractRunner processBlock at height " + block.getHeight() + " response: " + contractResponse.toJSONString());
        if (!isApply) {
            String message = "Contract simulator does not submit transactions";
            Logger.logInfoMessage(message);
            contractResponse.put("status", message);
            return contractResponse;
        }
        if (contractResponse.get("transactions") == null) {
            return null;
        }
        JSONArray transactions = ((JA)contractResponse.get("transactions")).toJSONArray();
        if (!isValidator) {
            submitContractTransactions(transactions);
        } else {
            return approveTransaction(transactionToApprove, transactions);
        }
        return null;
    }

    private JSONObject processTransaction(Transaction contractOrTriggerTransaction, boolean isApply, boolean validator) {
        Logger.logInfoMessage(String.format("ContractRunner Process transaction %d:%s", contractOrTriggerTransaction.getChain().getId(), Convert.toHexString(contractOrTriggerTransaction.getFullHash())));

        // Parse the trigger message attachment for the transaction and extract the contract information
        JSONObject messageJson = parsePrunableMessage(contractOrTriggerTransaction);
        if (messageJson.get("errorDescription") != null) {
            return messageJson;
        }
        String contractName = messageJson.get("contract") != null ? (String)messageJson.get("contract") : null;
        EventSource source = messageJson.get("source") != null ? EventSource.valueOf((String)messageJson.get("source")) : EventSource.NONE;
        String seed = messageJson.get("seed") != null ? (String)messageJson.get("seed") : null;
        ChainTransactionId referencedTransactionId = null;
        if (contractOrTriggerTransaction instanceof ChildTransaction) {
            referencedTransactionId = ((ChildTransaction) contractOrTriggerTransaction).getReferencedTransactionId();
        }
        Transaction triggerTransaction;
        boolean isValidator;
        if (referencedTransactionId != null && source.isTransaction() && validator) {
            // Validate a transaction generated by a contract by loading the trigger transaction and the original contract
            // and run it again to receive the same results
            triggerTransaction = Nxt.getBlockchain().getTransaction(referencedTransactionId.getChain(), referencedTransactionId.getFullHash());
            messageJson = parsePrunableMessage(triggerTransaction); // This points to the contract which the trigger transaction has triggered
            contractName = messageJson.get("contract") != null ? (String)messageJson.get("contract") : null;
            seed = messageJson.get("seed") != null ? (String)messageJson.get("seed") : null;
            isValidator = true;
        } else if (source.isTransaction() && validator) {
            // To Validate Ardor transactions submitted by a contract we cannot use referenced transaction so we point to the trigger transaction
            // using an attached message
            // TODO test
            ChainTransactionId triggerTransactionId = ChainTransactionId.fromStringId((String)messageJson.get("trigger"));
            if (triggerTransactionId == null) {
                return generateErrorResponse(1000, "Cannot parse trigger transaction %s", messageJson.toJSONString());
            }
            triggerTransaction = Nxt.getBlockchain().getTransaction(triggerTransactionId.getChain(), triggerTransactionId.getFullHash());
            messageJson = parsePrunableMessage(triggerTransaction);
            contractName = messageJson.get("contract") != null ? (String)messageJson.get("contract") : null;
            isValidator = true;
        } else if (source.isBlock() && validator) {
            // To validate contract transactions submitted by process block we use the ec block height to find the original
            // block in which the transaction executed
            // TODO: what if the execution height is different? if transaction was phased, or included with a delay?
            Block block = Nxt.getBlockchain().getBlockAtHeight(contractOrTriggerTransaction.getECBlockHeight());
            contractName = messageJson.get("submittedBy") != null ? (String)messageJson.get("submittedBy") : null;
            if (contractName == null) {
                return generateErrorResponse(1000, "Cannot trigger contract, contract id not specified %s", messageJson.toJSONString());
            }
            return processBlockContract(block, contractName, true, true, contractOrTriggerTransaction);
        } else {
            // This is main use case when we receive a new trigger transaction
            triggerTransaction = contractOrTriggerTransaction;
            isValidator = false;
        }
        if (contractName == null) {
            return generateErrorResponse(1000, "ContractRunner trigger %s did not specify contract name", Convert.toHexString(triggerTransaction.getFullHash()));
        }
        Contract contract = supportedContracts.get(contractName);
        if (contract == null) {
            return generateErrorResponse(1000, "Contract %s is not supported", contractName);
        }
        JO contractInvocationParamsJson;
        if (messageJson.get("params") != null) {
            contractInvocationParamsJson = new JO(messageJson.get("params"));
        } else {
            contractInvocationParamsJson = new JO();
        }
        Logger.logInfoMessage(String.format("Executing contract name %s class %s", contractName, contract.getClass().getCanonicalName()));
        TransactionContext context = new TransactionContext(triggerTransaction, config, contractInvocationParamsJson, contractName, seed);
        contract.processTransaction(context);
        JO jo = context.getResponse();
        JSONObject contractResponse = jo.toJSONObject();
        if (!isApply) {
            String message = "Contract simulator does not submit transactions";
            Logger.logInfoMessage(message);
            contractResponse.put("status", message);
            return contractResponse;
        }
        JSONArray transactions;
        if (contractResponse.get("transactions") != null) {
            // Transactions generated by the contract but not submitted yet
            transactions = ((JA)contractResponse.get("transactions")).toJSONArray();
        } else {
            transactions = new JSONArray();
        }
        if (!isValidator) {
            submitContractTransactions(transactions);
            contractResponse.put("status", "Transaction submitted");
            return contractResponse;
        } else {
            contractResponse = approveTransaction(contractOrTriggerTransaction, transactions);
            contractResponse.put("status", "Approval transaction submitted");
            return contractResponse;
        }
    }

    /**
     * Let a runner validate a transaction submitted by another contract runner instance.
     * Given a transaction generated by a contract previously and the transactions generated by the current invocation
     * If the transaction to approve is equal to one of the generated transactions, submit an approval transaction
     * @param transactionToApprove the transaction we are trying to approve
     * @param contractTransactions transactions generated by the current contract invocation
     * @return the approve transaction JSON or an error message if not approved
     */
    private JSONObject approveTransaction(Transaction transactionToApprove, JSONArray contractTransactions) {
        // First we normalize the data of the transaction objects so that we can compare them
        JSONObject expectedTransactionJSON = JSONData.unconfirmedTransaction(transactionToApprove);
        TransactionResponse expectedTransaction = TransactionResponse.create(expectedTransactionJSON);
        for (Object transactionObject : contractTransactions) {
            JSONObject actualTransactionResponse = (JSONObject)transactionObject;
            JSONObject actualTransactionJSON = (JSONObject)actualTransactionResponse.get("transactionJSON");
            TransactionResponse actualTransaction = TransactionResponse.create(actualTransactionJSON);
            if (actualTransaction.equals(expectedTransaction)) {
                if (transactionToApprove.getChain() == FxtChain.FXT) {
                    return generateErrorResponse(1000, "Found a match but cannot submit approval to a parent chain transaction");
                }
                // If we found a match, we submit approval transaction to the original transaction
                expectedTransactionJSON = JSONData.unconfirmedTransaction(transactionToApprove);
                APICall.Builder builder = new APICall.Builder("approveTransaction").
                        secretPhrase(config.getSecretPhrase()).
                        param("phasedTransaction", expectedTransactionJSON.get("chain") + ":" + expectedTransactionJSON.get("fullHash"));
                int chainId = (int) expectedTransactionJSON.get("chain");
                if (Chain.getChain(chainId) instanceof ChildChain) {
                    builder.param("feeRateNQTPerFXT", config.getFeeRateNQTPerFXT(chainId));
                }
                return builder.build().invoke();
            } else {
                Logger.logInfoMessage("Transactions differ");
                Logger.logInfoMessage("Expected Transaction " + expectedTransactionJSON.toJSONString());
                Logger.logInfoMessage("Actual   Transaction " + actualTransactionJSON.toJSONString());
            }
        }
        return generateErrorResponse(1000, "Cannot approve contract transaction %s chain %s", expectedTransactionJSON.get("fullHash"), expectedTransactionJSON.get("chain"));
    }

    private void submitContractTransactions(JSONArray transactions) {
        for (Object transactionObject : transactions) {
            JSONObject transaction = (JSONObject) transactionObject;
            JSONObject transactionJSON = (JSONObject) transaction.get("transactionJSON");
            if (transactionJSON == null) {
                Logger.logErrorMessage(String.format("Error %s %s in transaction submitted by contract",
                        transaction.get("errorCode"), transaction.get("errorDescription")));
                continue;
            }
            APICall.Builder builder;
            APICall apiCall;
            if (transactionJSON.get("signature") == null) {
                builder = new APICall.Builder("signTransaction").
                        secretPhrase(config.getSecretPhrase()).
                        param("unsignedTransactionJSON", transactionJSON.toJSONString()).
                        param("validate", "true");
                apiCall = builder.build();
                JSONObject signTransactionResponse = apiCall.invoke();
                if (signTransactionResponse.get("errorCode") != null) {
                    Logger.logErrorMessage(String.format("Error signing transaction %s chain %s message %s",
                            transactionJSON.get("fullHash"), transactionJSON.get("chain"), signTransactionResponse.get("errorDescription")));
                    continue;
                }
                transactionJSON = (JSONObject) signTransactionResponse.get("transactionJSON");
            }
            builder = new APICall.Builder("broadcastTransaction").
                    param("transactionJSON", transactionJSON.toJSONString());
            apiCall = builder.build();
            JSONObject broadcastTransactionResponse = apiCall.invoke();
            if (broadcastTransactionResponse.get("errorCode") != null) {
                Logger.logErrorMessage(String.format("Error broadcasting transaction %s chain %s message %s",
                        transactionJSON.get("fullHash"), transactionJSON.get("chain"), broadcastTransactionResponse.get("errorDescription")));
            }
        }
    }

    private boolean isSuspendContractExecution() {
        return Nxt.getBlockchain().getLastBlockTimestamp() < Nxt.getEpochTime() - config.getCatchUpInterval();
    }

    private JSONObject generateErrorResponse(int code, String message, Object... params) {
        message = String.format(message, params);
        Logger.logInfoMessage(message);
        JSONObject response = new JSONObject();
        response.put("errorCode", code);
        response.put("errorDescription", message);
        return response;
    }

    /**
     * Load JSON data from transaction attached prunable message, plain or encrypted
     * @param transaction the transaction
     * @return the attached message in JSON format
     */
    private JSONObject parsePrunableMessage(Transaction transaction) {
        PrunablePlainMessageAppendix plainAppendix = (PrunablePlainMessageAppendix)transaction.getAppendages().stream().filter(a -> a instanceof PrunablePlainMessageAppendix).findFirst().orElse(null);
        PrunableEncryptedMessageAppendix encryptedAppendix = (PrunableEncryptedMessageAppendix)transaction.getAppendages().stream().filter(a -> a instanceof PrunableEncryptedMessageAppendix).findFirst().orElse(null);
        String messageText;
        if (plainAppendix != null) {
            messageText = Convert.toString(plainAppendix.getMessage(), plainAppendix.isText());
        } else if (encryptedAppendix != null) {
            if (transaction.getRecipientId() != config.getAccountId()) {
                return generateErrorResponse(1000, "Cannot decrypt attached message, contract account %s is not the recipient %s of attached message", config.getAccountRs(), Convert.rsAccount(transaction.getRecipientId()));
            }
            messageText = Convert.toString(Account.decryptFrom(transaction.getSenderPublicKey(), encryptedAppendix.getEncryptedData(), config.getSecretPhrase(), encryptedAppendix.isCompressed()), encryptedAppendix.isText());
        } else {
            return generateErrorResponse(1000, "Transaction %s of chain %s does not trigger a contract", Convert.toHexString(transaction.getFullHash()), transaction.getChain());
        }
        try {
            Object messageObject = JSONValue.parseWithException(messageText);
            if (!(messageObject instanceof JSONObject)) {
                return generateErrorResponse(1000, "trigger message does not represent a JSON object");
            }
            return (JSONObject)messageObject;
        } catch (Exception e) {
            return generateErrorResponse(1000, "cannot parse attached message, probably not a trigger transaction " + e);
        }
    }

    @Override
    public void shutdown() {
        Logger.logInfoMessage("ContractRunner shutdown");
    }

    public void reset() {
        supportedContracts.clear();
        supportedContractTransactions.clear();
    }

    @Override
    public Map<String, APIServlet.APIRequestHandler> getAPIRequests() {
        return apiRequests;
    }

    @Override
    public Contract getContract(String name) {
        return supportedContracts.get(name);
    }

    public class TriggerContractByTransactionAPI extends APIServlet.APIRequestHandler {
        TriggerContractByTransactionAPI(APITag[] apiTags, String... origParameters) {
            super(apiTags, origParameters);
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
            API.verifyPassword(req);
            byte[] triggerFullHash = ParameterParser.getBytes(req, "triggerFullHash", true);
            Chain chain = ParameterParser.getChain(req);
            boolean isApply = "true".equalsIgnoreCase(req.getParameter("apply"));
            boolean isValidator = "true".equalsIgnoreCase(req.getParameter("validate"));
            Transaction transaction = Nxt.getBlockchain().getTransaction(chain, triggerFullHash);
            if (transaction != null) {
                return processTransaction(transaction, isApply, isValidator);
            }
            return generateErrorResponse(1001, "Unknown transaction %d:%s", chain.getId(), Convert.toHexString(triggerFullHash));
        }
    }

    public class TriggerContractByHeightAPI extends APIServlet.APIRequestHandler {
        TriggerContractByHeightAPI(APITag[] apiTags, String... origParameters) {
            super(apiTags, origParameters);
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
            API.verifyPassword(req);
            int height = ParameterParser.getHeight(req);
            String contractName = req.getParameter("contractName");
            boolean isApply = "true".equalsIgnoreCase(req.getParameter("apply"));
            Block block = Nxt.getBlockchain().getBlockAtHeight(height);
            return processBlockContract(block, contractName, isApply, false, null);
        }

        @Override
        protected boolean isChainSpecific() {
            return false;
        }
    }

    public class TriggerContractByRequestAPI extends APIServlet.APIRequestHandler {
        TriggerContractByRequestAPI(APITag[] apiTags, String... origParameters) {
            super(apiTags, origParameters);
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
            API.verifyPassword(req);
            String contractName = req.getParameter("contractName");
            ContractReference contractReference = ContractReference.getContractReference(config.getAccountId(), contractName);
            if (contractReference == null) {
                return generateErrorResponse(1002, "Contract %s not found", contractName);
            }
            JO setupParams = ContractLoader.getContractSetupParams(contractReference);
            Contract contract = ContractLoader.loadContract(contractReference.getContractId(), setupParams);
            RequestContext context = new RequestContext(req, config, contractName);
            contract.processRequest(context);
            JO jo = context.getResponse();
            if (jo == null) {
                return generateErrorResponse(1002, "Contract %s class %s returned no response", contractName, contract.getClass().getCanonicalName());
            }
            return jo.toJSONObject();
        }

        @Override
        protected boolean isChainSpecific() {
            return false;
        }
    }

    public final class TriggerContractByVoucherAPI extends APIServlet.APIRequestHandler {

        private TriggerContractByVoucherAPI(String fileParameter, APITag[] apiTags, String... origParameters) {
            super(fileParameter, apiTags, origParameters);
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
            API.verifyPassword(req);
            ParameterParser.FileData fileData = ParameterParser.getFileData(req, "voucher", true);
            if (fileData == null) {
                return JSONResponses.INCORRECT_FILE;
            }
            byte[] data = fileData.getData();
            String contractName = req.getParameter("contractName");
            JSONObject voucher = ParameterParser.parseVoucher(data);
            Contract contract = supportedContracts.get(contractName);
            VoucherContext context = new VoucherContext(new JO(voucher), config, contractName);
            contract.processVoucher(context);
            JO jo = context.getResponse();
            if (jo == null) {
                return generateErrorResponse(1003, "Contract %s with class %s invoked by account %s returned no response",
                        contractName, contract.getClass().getCanonicalName(), config.getAccountRs());
            }
            if (jo.isExist("transactions")) {
                submitContractTransactions(jo.getArray("transactions").toJSONArray());
            }
            return jo.toJSONObject();
        }

        @Override
        protected boolean isChainSpecific() {
            return false;
        }
    }

    public class GetSupportedContractsAPI extends APIServlet.APIRequestHandler {
        GetSupportedContractsAPI(APITag[] apiTags, String... origParameters) {
            super(apiTags, origParameters);
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
            API.verifyPassword(req);
            JO response = new JO();
            response.put("contractAccount", config.getAccountRs());
            JA array = new JA();
            for (String name : supportedContracts.keySet()) {
                Contract contract = supportedContracts.get(name);
                JO contractJson = new JO();
                contractJson.put("name", name);
                contractJson.put("contractClass", contract.getClass().getCanonicalName());
                contractJson.put("params", contract.getContractParams().toJSONObject());
                ContractReference contractReference = supportedContractTransactions.get(name);
                contractJson.put("contractReference", JSONData.contractReference(contractReference));
                ChainTransactionId contractId = contractReference.getContractId();
                Transaction taggedDataUploadTransaction = contractId.getChildTransaction();
                contractJson.put("uploadTransaction", JSONData.transaction(taggedDataUploadTransaction));
                array.add(contractJson);
            }
            response.put("supportedContracts", array);
            return response.toJSONObject();
        }

        @Override
        protected boolean isChainSpecific() {
            return false;
        }
    }

}
