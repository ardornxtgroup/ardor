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

import nxt.Constants;
import nxt.Nxt;
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
import nxt.blockchain.TransactionType;
import nxt.db.DbIterator;
import nxt.http.APICall;
import nxt.http.APIServlet;
import nxt.http.APITag;
import nxt.http.JSONData;
import nxt.http.callers.GetAllWaitingTransactionsCall;
import nxt.http.callers.GetUnconfirmedTransactionsCall;
import nxt.http.responses.TransactionResponse;
import nxt.lightcontracts.ContractReference;
import nxt.messaging.PrunableEncryptedMessageAppendix;
import nxt.messaging.PrunablePlainMessageAppendix;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.ResourceLookup;
import nxt.voting.PhasingAppendix;
import nxt.voting.PhasingPollHome;
import nxt.voting.VoteWeighting;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static nxt.addons.AbstractContractContext.EventSource;
import static nxt.addons.ContractRunner.INVOCATION_TYPE.BLOCK;
import static nxt.addons.ContractRunner.INVOCATION_TYPE.REQUEST;
import static nxt.addons.ContractRunner.INVOCATION_TYPE.TRANSACTION;

public final class ContractRunner implements AddOn, ContractProvider {

    enum INVOCATION_TYPE {
        TRANSACTION("processTransaction", TransactionContext.class),
        BLOCK("processBlock", BlockContext.class),
        REQUEST("processRequest", RequestContext.class),
        VOUCHER("processVoucher", VoucherContext.class);

        private final String methodName;
        private final Class contextClass;

        INVOCATION_TYPE(String methodName, Class contextClass) {
            this.methodName = methodName;
            this.contextClass = contextClass;
        }

        public String getMethodName() {
            return methodName;
        }

        public Class getContextClass() {
            return contextClass;
        }
    }

    static final String CONFIG_PROPERTY_PREFIX = "addon.contractRunner.";
    private static final String CONFIG_FILE_PROPERTY = CONFIG_PROPERTY_PREFIX + "configFile";

    private ContractRunnerConfig config;
    private Map<String, ContractAndSetupParameters> supportedContracts = new HashMap<>();
    private Map<String, ContractReference> supportedContractReferences = new HashMap<>();
    private Map<String, APIServlet.APIRequestHandler> apiRequests;
    private Map<String, ContractReference> addedContractReferences = new HashMap<>();
    private Map<String, ContractReference> deletedContractReferences = new HashMap<>();

    @Override
    public void init() {
        // Initialize contract runner APIs
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new ContractRunnerPermission("init"));
        }
        apiRequests = new HashMap<>();
        apiRequests.put("getSupportedContracts", new ContractRunnerAPIs.GetSupportedContractsAPI(this, new APITag[]{APITag.ADDONS}));
        apiRequests.put("triggerContractByTransaction", new ContractRunnerAPIs.TriggerContractByTransactionAPI(this, new APITag[]{APITag.ADDONS}, "triggerFullHash", "apply", "validate", "adminPassword"));
        apiRequests.put("triggerContractByHeight", new ContractRunnerAPIs.TriggerContractByHeightAPI(this, new APITag[]{APITag.ADDONS}, "contractName", "height", "apply", "adminPassword"));
        apiRequests.put("triggerContractByRequest", new ContractRunnerAPIs.TriggerContractByRequestAPI(this, new APITag[]{APITag.ADDONS}, "contractName", "setupParams", "adminPassword"));
        apiRequests.put("triggerContractByVoucher", new ContractRunnerAPIs.TriggerContractByVoucherAPI(this, "voucher", new APITag[]{APITag.ADDONS}, "contractName", "adminPassword"));
        apiRequests.put("uploadContractRunnerConfiguration", new ContractRunnerAPIs.UploadContractRunnerConfigurationAPI(this, "config", new APITag[]{APITag.ADDONS}, "adminPassword"));

        if (!Nxt.getServerStatus().isDatabaseReady()) {
            // For some utilities it is enough that we register the API even if Nxt itself is not initialized
            return;
        }

        // Read contract runner configuration
        loadConfig(Nxt.getStringProperty(CONFIG_FILE_PROPERTY));
        try {
            try (DbIterator<ContractReference> iterator = ContractReference.getContractReferences(config.getAccountId(), null, 0, Integer.MAX_VALUE)) {
                while (iterator.hasNext()) {
                    ContractLoader.loadContract(iterator.next(), supportedContracts, supportedContractReferences);
                }
            }
        } catch (Throwable t) {
            String message = t.toString();
            config = new NullContractRunnerConfig(message);
            Logger.logErrorMessage(message);
            return;
        }
        // Register listeners for contract activation
        Nxt.getBlockchainProcessor().addListener(this::processBlock, BlockchainProcessor.Event.AFTER_BLOCK_ACCEPT);
        Nxt.getTransactionProcessor().addListener(this::processConfirmed, TransactionProcessor.Event.ADDED_CONFIRMED_TRANSACTIONS);
        Nxt.getTransactionProcessor().addListener(this::processReleasedPhased, TransactionProcessor.Event.RELEASE_PHASED_TRANSACTION);
        ContractReference.addListener(this::contractAdded, ContractReference.Event.SET_CONTRACT_REFERENCE);
        ContractReference.addListener(this::contractDeleted, ContractReference.Event.DELETE_CONTRACT_REFERENCE);
        Block lastBlock = Nxt.getBlockchain().getLastBlock();
        if (lastBlock.getFxtTransactions().size() > 0 && lastBlock.getHeight() > 1) {
            Logger.logInfoMessage("ContractRunner popOff last block");
            Nxt.getBlockchainProcessor().popOffTo(lastBlock.getHeight() - 1);
            Nxt.getTransactionProcessor().processLater(lastBlock.getFxtTransactions());
        }
        Logger.logInfoMessage("ContractRunner Started");
    }

    private void loadConfig(String configFileName) {
        JO configJson;
        if (configFileName != null) {
            configJson = AccessController.doPrivileged((PrivilegedAction<JO>) () -> ResourceLookup.loadJsonResource(configFileName));
            if (configJson == null) {
                Logger.logInfoMessage("Cannot load contract runner config from " + configFileName);
                configJson = new JO();
            }
        } else {
            configJson = new JO();
        }
        loadConfig(configJson);
    }

    void loadConfig(JO configJson) {
        if (config == null) {
            config = new ActiveContractRunnerConfig(this);
        }
        config.init(configJson);
    }

    <T extends AbstractContractContext> JO process(ContractAndSetupParameters contract, T context, INVOCATION_TYPE invocationType) {
        if (Constants.isAutomatedTest) {
            return AccessController.doPrivileged((PrivilegedAction<JO>) () -> processImpl(contract, context, invocationType));
        } else {
            return processImpl(contract, context, invocationType);
        }
    }

    private <T extends AbstractContractContext> JO processImpl(ContractAndSetupParameters contractAndParameters, T context, INVOCATION_TYPE invocationType) {
        Contract contract = contractAndParameters.getContract();
        try {
            Method contractMethod;
            try {
                contractMethod = contract.getClass().getDeclaredMethod(invocationType.getMethodName(), invocationType.getContextClass());
            } catch (NoSuchMethodException e) {
                return null;
            }
            context.setContractSetupParameters(contractAndParameters.getParams());
            if (invocationType == BLOCK || invocationType == REQUEST) {
                return (JO) contractMethod.invoke(contract, context);
            }
            AbstractOperationContext operationContext = context.getContext();
            Annotation[] methodAnnotations = contractMethod.getDeclaredAnnotations();
            for (Annotation annotation : methodAnnotations) {
                if (annotation.annotationType().equals(ValidateContractRunnerIsRecipient.class)) {
                    if (operationContext.notSameRecipient()) {
                        return context.generateErrorResponse(11001, "The trigger %s %s recipient %s differs from contract runner account %s",
                                invocationType, Convert.toHexString(operationContext.getTransaction().getFullHash()), operationContext.getTransaction().getRecipientRs(), config.getAccountRs());
                    }
                } else if (annotation.annotationType().equals(ValidateContractRunnerIsSender.class)) {
                    if (operationContext.notSameSender()) {
                        return context.generateErrorResponse(11004, "The trigger %s %s sender %s differs from contract runner account %s",
                                invocationType, Convert.toHexString(operationContext.getTransaction().getFullHash()), operationContext.getTransaction().getSenderRs(), config.getAccountRs());
                    }
                } else if (annotation.annotationType().equals(ValidateChain.class)) {
                    ValidateChain validateChain = (ValidateChain) annotation;
                    int chain = operationContext.getTransaction().getChainId();
                    boolean isAccepted = validateChain.accept().length == 0 || IntStream.of(validateChain.accept()).anyMatch(c -> c == chain);
                    boolean isRejected = validateChain.reject().length != 0 && IntStream.of(validateChain.reject()).anyMatch(c -> c == chain);
                    if (!isAccepted || isRejected) {
                        return context.generateErrorResponse(11002, "The trigger %s %s chain %s is not accepted by contract type %s",
                                invocationType, Convert.toHexString(operationContext.getTransaction().getFullHash()), chain, contract.getClass().getName());
                    }
                } else if (annotation.annotationType().equals(ValidateTransactionType.class)) {
                    ValidateTransactionType validateTransactionType = (ValidateTransactionType) annotation;
                    TransactionType transactionType = operationContext.getTransaction().getTransactionType();
                    boolean isAccepted = validateTransactionType.accept().length == 0 || Arrays.stream(validateTransactionType.accept()).anyMatch(tt -> tt.getTransactionType() == transactionType);
                    boolean isRejected = validateTransactionType.reject().length != 0 && Arrays.stream(validateTransactionType.reject()).anyMatch(tt -> tt.getTransactionType() == transactionType);
                    if (!isAccepted || isRejected) {
                        return context.generateErrorResponse(11003, "The trigger %s %s is not an accepted transaction type of contract %s",
                                invocationType, operationContext.getTransaction().getFullHash(), contract.getClass().getName());
                    }
                }
            }
            Logger.logInfoMessage("Invoking %s on contract %s", contractMethod.getName(), contract.getClass().getCanonicalName());
            return (JO) contractMethod.invoke(contract, context);
        } catch (ReflectiveOperationException e) {
            Logger.logInfoMessage("Error running contract " + contract.getClass().getName(), e);
            return context.generateErrorResponse(11002, e.toString());
        }
    }

    private void processConfirmed(List<? extends Transaction> transactions) {
        if (isSuspendContractRunnerExecution()) {
            return;
        }
        for (Transaction transaction : transactions) {
            if (transaction.getType() == ChildBlockFxtTransactionType.INSTANCE) {
                // child chain block transactions cannot trigger a contract
                continue;
            }
            if (transaction.isPhased()) {
                // contract validation can be triggered by a phased transaction submitted by another contract account under its control
                if (!config.isValidator()) {
                    PhasingAppendix phasing = ((ChildTransaction) transaction).getPhasing();
                    if (phasing.getParams().getVoteWeighting().getVotingModel() != VoteWeighting.VotingModel.HASH) {
                        // ignore phased transactions which does not hold a secret hash
                        continue;
                    }
                    if (phasing.getFinishHeight() < transaction.getHeight() + 200) {
                        Logger.logInfoMessage("ContractRunner won't process phased transaction %s, phasing finish height less than 200 blocks ahead", Convert.toHexString(transaction.getFullHash()));
                        continue;
                    }
                }
            }
            try {
                JO contractResponse = processTransaction(transaction, true, config.isValidator());
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
        if (isSuspendContractRunnerExecution()) {
            return;
        }
        for (Transaction transaction : transactions) {
            PhasingAppendix phasing = ((ChildTransaction) transaction).getPhasing();
            if (phasing.getParams().getVoteWeighting().getVotingModel() == VoteWeighting.VotingModel.HASH) {
                // ignore phased transactions holding a secret hash since these were processed when submitted
                continue;
            }
            try {
                JO contractResponse = processTransaction(transaction, true, config.isValidator());
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
        if (contractReference.getAccountId() == config.getAccountId()) {
            addedContractReferences.put(contractReference.getContractName(), contractReference);
        }
    }

    private void contractDeleted(ContractReference contractReference) {
        if (contractReference.getAccountId() == config.getAccountId()) {
            deletedContractReferences.put(contractReference.getContractName(), contractReference);
        }
    }

    private void processBlock(Block block) {
        try {
            addedContractReferences.forEach((contractName, contractReference) ->
                    ContractLoader.loadContract(contractReference, supportedContracts, supportedContractReferences));
            deletedContractReferences.forEach((contractName, contractReference) -> {
                supportedContracts.remove(contractName);
                supportedContractReferences.remove(contractName);
            });
            addedContractReferences.clear();
            deletedContractReferences.clear();

            if (isSuspendContractRunnerExecution()) {
                return;
            }
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

    JO processBlockContract(Block block, String contractName, boolean isApply, boolean isValidator, Transaction transactionToApprove) {
        ContractAndSetupParameters contract = supportedContracts.get(contractName);
        if (contract == null) {
            return generateErrorResponse(1003, "Contract is not supported %s", contractName);
        }
        BlockContext context = new BlockContext(block, config, contractName);
        JO contractResponse = process(contract, context, BLOCK);
        if (contractResponse == null) {
            return null;
        }
        Logger.logInfoMessage("ContractRunner processBlock at height " + block.getHeight() + " response: " + contractResponse.toJSONString());
        if (!isApply) {
            String message = "Contract simulator does not submit transactions";
            Logger.logInfoMessage(message);
            contractResponse.put("status", message);
            return contractResponse;
        }
        if (!contractResponse.isExist("transactions")) {
            return null;
        }
        List<JO> transactions = contractResponse.getJoList("transactions");
        if (!isValidator) {
            return submitContractTransactions(contract.getContract(), transactions);
        } else {
            return approveTransaction(contract.getContract(), transactionToApprove, transactions);
        }
    }

    JO processTransaction(Transaction contractOrTriggerTransaction, boolean isApply, boolean validator) {
        Logger.logInfoMessage(String.format("ContractRunner Process transaction %d:%s", contractOrTriggerTransaction.getChain().getId(), Convert.toHexString(contractOrTriggerTransaction.getFullHash())));

        // Parse the trigger message attachment for the transaction and extract the contract information
        JO messageJson = parsePrunableMessage(contractOrTriggerTransaction);
        if (messageJson.get("errorDescription") != null) {
            return messageJson;
        }
        String contractName = messageJson.get("contract") != null ? (String) messageJson.get("contract") : null;
        EventSource source = messageJson.get("source") != null ? EventSource.valueOf((String) messageJson.get("source")) : EventSource.NONE;
        String seed = messageJson.get("seed") != null ? (String) messageJson.get("seed") : null;
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
            contractName = messageJson.get("contract") != null ? (String) messageJson.get("contract") : null;
            seed = messageJson.get("seed") != null ? (String) messageJson.get("seed") : null;
            isValidator = true;
        } else if (source.isTransaction() && validator) {
            // To Validate parent chain transactions submitted by a contract we cannot use referenced transaction so we point to the trigger transaction
            // using an attached message
            ChainTransactionId triggerTransactionId = ChainTransactionId.fromStringId((String) messageJson.get("trigger"));
            if (triggerTransactionId == null) {
                return generateErrorResponse(1000, "Cannot parse trigger transaction %s", messageJson.toJSONString());
            }
            triggerTransaction = Nxt.getBlockchain().getTransaction(triggerTransactionId.getChain(), triggerTransactionId.getFullHash());
            messageJson = parsePrunableMessage(triggerTransaction);
            contractName = messageJson.get("contract") != null ? (String) messageJson.get("contract") : null;
            isValidator = true;
        } else if (source.isBlock() && validator) {
            // To validate contract transactions submitted by process block we determine the transaction execution height
            // then re-process the block at this height to validate that the contract generates the same transaction
            int height;
            if (contractOrTriggerTransaction.isPhased()) {
                // TODO: test
                PhasingPollHome.PhasingPollResult phasingPollResult = PhasingPollHome.getResult(contractOrTriggerTransaction);
                if (phasingPollResult != null) {
                    if (!phasingPollResult.isApproved()) {
                        return generateErrorResponse(1000, "Validation failed - phased transaction submitted by contract wss not approved %s", Convert.toHexString(contractOrTriggerTransaction.getFullHash()));
                    }
                    height = phasingPollResult.getHeight();
                } else {
                    return generateErrorResponse(1000, "Validation failed - phased transaction submitted by contract not executed yet %s", Convert.toHexString(contractOrTriggerTransaction.getFullHash()));
                }
            } else {
                height = contractOrTriggerTransaction.getECBlockHeight();
            }

            Block block = Nxt.getBlockchain().getBlockAtHeight(height);
            contractName = messageJson.get("submittedBy") != null ? (String) messageJson.get("submittedBy") : null;
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
        ContractAndSetupParameters contract = supportedContracts.get(contractName);
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
        JO contractResponse = process(contract, context, TRANSACTION);
        if (contractResponse == null) {
            contractResponse = new JO();
        }
        if (!isApply) {
            String message = "Contract simulator does not submit transactions";
            Logger.logInfoMessage(message);
            contractResponse.put("status", message);
            return contractResponse;
        }
        if (!contractResponse.isExist("transactions")) {
            Logger.logInfoMessage("Contract %s did not submitted any transaction", contractName);
            return contractResponse;
        }

        // Transactions generated by the contract but not submitted yet
        List<JO> transactions = contractResponse.getJoList("transactions");
        if (transactions.size() > config.getMaxSubmittedTransactionsPerInvocation()) {
            // Limit on the number of transactions a contract can submit in a single run.
            Logger.logInfoMessage("Contract cannot submit more than %d transactions in a single invocation, it generated %d transactions, consider increasing maxSubmittedTransactionsPerInvocation",
                    config.getMaxSubmittedTransactionsPerInvocation(), transactions.size());
            return contractResponse;
        }
        if (!isValidator) {
            return submitContractTransactions(contract.getContract(), transactions);
        } else {
            contractResponse = new JO(approveTransaction(contract.getContract(), contractOrTriggerTransaction, transactions));
            contractResponse.put("status", "Approval transaction submitted");
            return contractResponse;
        }
    }

    /**
     * Let a runner validate a transaction submitted by another contract runner instance.
     * Given a transaction generated by a contract previously and the transactions generated by the current invocation
     * If the transaction to approve is equal to one of the generated transactions, submit an approval transaction
     *
     * @param contract the contract
     * @param transactionToApprove the transaction we are trying to approve
     * @param contractTransactions transactions generated by the current contract invocation
     * @return the approve transaction JSON or an error message if not approved
     */
    private JO approveTransaction(Contract contract, Transaction transactionToApprove, List<JO> contractTransactions) {
        // First we normalize the data of the transaction objects so that we can compare them
        JO expectedTransactionJSON = new JO(JSONData.unconfirmedTransaction(transactionToApprove));
        TransactionResponse expectedTransaction = TransactionResponse.create(expectedTransactionJSON);
        for (JO transactionObject : contractTransactions) {
            JO actualTransactionJSON = transactionObject.getJo("transactionJSON");
            TransactionResponse actualTransaction = TransactionResponse.create(actualTransactionJSON);
            if (isDuplicate(contract, actualTransaction)) {
                continue;
            }
            if (actualTransaction.similar(expectedTransaction)) {
                if (transactionToApprove.getChain() == FxtChain.FXT) {
                    return generateErrorResponse(1000, "Found a match but cannot submit approval to a parent chain transaction");
                }
                // If we found a match, we submit approval transaction to the original transaction
                String validatorSecretPhrase = config.getValidatorSecretPhrase();
                if (validatorSecretPhrase == null) {
                    return generateErrorResponse(1000, "Cannot approve transaction, validatorSecretPhrase not specified");
                }
                expectedTransactionJSON = new JO(JSONData.unconfirmedTransaction(transactionToApprove));
                APICall.Builder builder = new APICall.Builder("approveTransaction").
                        param("phasedTransaction", expectedTransactionJSON.getInt("chain") + ":" + expectedTransactionJSON.getString("fullHash")).
                        secretPhrase(validatorSecretPhrase);
                int chainId = (int) expectedTransactionJSON.get("chain");
                if (Chain.getChain(chainId) instanceof ChildChain) {
                    builder.param("feeRateNQTPerFXT", config.getFeeRateNQTPerFXT(chainId));
                }
                return new JO(builder.build().invoke());
            } else {
                Logger.logInfoMessage("Transactions differ");
                Logger.logInfoMessage("Expected Transaction " + expectedTransactionJSON.toJSONString());
                Logger.logInfoMessage("Actual   Transaction " + actualTransactionJSON.toJSONString());
            }
        }
        return generateErrorResponse(1000, "Cannot approve contract transaction %s chain %s", expectedTransactionJSON.get("fullHash"), expectedTransactionJSON.get("chain"));
    }

    JO submitContractTransactions(Contract contract, List<JO> transactions) {
        String secretPhrase = config.getSecretPhrase();
        if (secretPhrase == null) {
            return generateErrorResponse(1000, "Cannot submit transactions, contract runner secret phrase not specified");
        }
        int counter = 0;
        int errorsCounter = 0;
        for (JO transaction : transactions) {
            if (!transaction.isExist("transactionJSON")) {
                Logger.logErrorMessage(String.format("Error %s %s in transaction submitted by contract",
                        transaction.get("errorCode"), transaction.get("errorDescription")));
                errorsCounter++;
                continue;
            }
            JO transactionJSON = transaction.getJo("transactionJSON");
            if (isDuplicate(contract, TransactionResponse.create(transactionJSON))) {
                continue;
            }
            APICall.Builder builder;
            APICall apiCall;
            if (!transactionJSON.isExist("signature")) {
                builder = new APICall.Builder("signTransaction").
                        secretPhrase(secretPhrase).
                        param("unsignedTransactionJSON", transactionJSON.toJSONString()).
                        param("validate", "true");
                apiCall = builder.build();
                JO signTransactionResponse = new JO(apiCall.invoke());
                if (signTransactionResponse.isExist("errorCode")) {
                    Logger.logErrorMessage(String.format("Error signing transaction %s chain %d message %s",
                            transactionJSON.getString("fullHash"), transactionJSON.getLong("chain"), signTransactionResponse.getString("errorDescription")));
                    errorsCounter++;
                    continue;
                }
                transactionJSON = new JO(signTransactionResponse.get("transactionJSON"));
            }
            builder = new APICall.Builder("broadcastTransaction").param("transactionJSON", transactionJSON.toJSONString());
            apiCall = builder.build();
            JO broadcastTransactionResponse = new JO(apiCall.invoke());
            if (broadcastTransactionResponse.get("errorCode") != null) {
                Logger.logErrorMessage(String.format("Error broadcasting transaction %s chain %d message %s",
                        transactionJSON.getString("fullHash"), transactionJSON.getLong("chain"), broadcastTransactionResponse.getString("errorDescription")));
                errorsCounter++;
            } else {
                counter++;
            }
        }
        return generateInfoResponse("contract runner submitted %d transactions with %d errors", counter, errorsCounter);
    }

    private boolean isDuplicate(Contract contract, TransactionResponse transaction) {
        JO unconfirmedTransactions = GetUnconfirmedTransactionsCall.create(transaction.getChainId()).account(transaction.getSenderId()).call();
        JO waitingTransactions = GetAllWaitingTransactionsCall.create().call();
        List<JO> transactionsList = new ArrayList<>(unconfirmedTransactions.getJoList("unconfirmedTransactions"));
        transactionsList.addAll(waitingTransactions.getJoList("transactions"));
        return contract.isDuplicate(transaction, transactionsList.stream().map(TransactionResponse::create).collect(Collectors.toList()));
    }

    private boolean isSuspendContractRunnerExecution() {
        if (FxtChain.FXT.getBalanceHome().getBalance(config.getAccountId()).getUnconfirmedBalance() < Constants.UNCONFIRMED_POOL_DEPOSIT_FQT) {
            Logger.logErrorMessage(String.format("contract runner account %s must have enough %s to pay the unconfirmed pool deposit of %d FQT", Convert.rsAccount(config.getAccountId()), FxtChain.FXT_NAME, Constants.UNCONFIRMED_POOL_DEPOSIT_FQT));
            return true;
        }
        return Nxt.getBlockchain().getLastBlockTimestamp() < Nxt.getEpochTime() - config.getCatchUpInterval();
    }

    JO generateErrorResponse(int code, String message, Object... params) {
        message = String.format(message, params);
        Logger.logInfoMessage(message);
        JO response = new JO();
        response.put("errorCode", code);
        response.put("errorDescription", message);
        return response;
    }

    public JO generateInfoResponse(String message, Object... params) {
        message = String.format(message, params);
        Logger.logInfoMessage(message, params);
        JO response = new JO();
        response.put("info", message);
        return response;
    }

    /**
     * Load JSON data from transaction attached prunable message, plain or encrypted
     *
     * @param transaction the transaction
     * @return the attached message in JSON format
     */
    private JO parsePrunableMessage(Transaction transaction) {
        PrunablePlainMessageAppendix plainAppendix = (PrunablePlainMessageAppendix) transaction.getAppendages().stream().filter(a -> a instanceof PrunablePlainMessageAppendix).findFirst().orElse(null);
        PrunableEncryptedMessageAppendix encryptedAppendix = (PrunableEncryptedMessageAppendix) transaction.getAppendages().stream().filter(a -> a instanceof PrunableEncryptedMessageAppendix).findFirst().orElse(null);
        String messageText;
        if (plainAppendix != null) {
            messageText = Convert.toString(plainAppendix.getMessage(), plainAppendix.isText());
        } else if (encryptedAppendix != null) {
            if (transaction.getRecipientId() != config.getAccountId()) {
                return generateErrorResponse(1000, "Cannot decrypt attached message, contract account %s is not the recipient %s of attached message", config.getAccountRs(), Convert.rsAccount(transaction.getRecipientId()));
            } else if (config.getSecretPhrase() == null) {
                return generateErrorResponse(1000, "Cannot decrypt attached message, contract runner secret phrase not specified for account %s", config.getAccountRs());
            } else if (config.isValidator()) {
                // TODO if this becomes an important limitation perhaps we can rely on a shared key in this case
                return generateErrorResponse(1000, "Cannot decrypt attached message, validator cannot decrypt encrypted trigger message");
            }
            messageText = Convert.toString(Account.decryptFrom(transaction.getSenderPublicKey(), encryptedAppendix.getEncryptedData(), config.getSecretPhrase(), encryptedAppendix.isCompressed()), encryptedAppendix.isText());
        } else {
            // TODO this is not really an error condition. Can we handle this gracefully
            return generateErrorResponse(1000, "Transaction %s of chain %s does not trigger a contract", Convert.toHexString(transaction.getFullHash()), transaction.getChain());
        }
        try {
            return JO.parse(messageText);
        } catch (Exception e) {
            return generateErrorResponse(1000, "cannot parse attached message " + messageText + ", probably not a trigger transaction " + e);
        }
    }

    @Override
    public void shutdown() {
        Logger.logInfoMessage("ContractRunner shutdown");
    }

    public void reset() {
        supportedContracts.clear();
        supportedContractReferences.clear();
    }

    @Override
    public Map<String, APIServlet.APIRequestHandler> getAPIRequests() {
        return apiRequests;
    }

    @Override
    public ContractAndSetupParameters getContract(String name) {
        return supportedContracts.get(name);
    }

    ContractRunnerConfig getConfig() {
        return config;
    }

    Set<String> getSupportedContractNames() {
        return Collections.unmodifiableSet(supportedContracts.keySet());
    }

    ContractReference getSupportedContractReference(String name) {
        return supportedContractReferences.get(name);
    }
}
