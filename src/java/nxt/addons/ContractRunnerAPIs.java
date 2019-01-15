package nxt.addons;

import nxt.Nxt;
import nxt.NxtException;
import nxt.blockchain.Block;
import nxt.blockchain.Chain;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.Transaction;
import nxt.http.API;
import nxt.http.APIServlet;
import nxt.http.APITag;
import nxt.http.JSONData;
import nxt.http.JSONResponses;
import nxt.http.ParameterParser;
import nxt.lightcontracts.ContractReference;
import nxt.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static nxt.addons.ContractRunner.INVOCATION_TYPE.REQUEST;
import static nxt.addons.ContractRunner.INVOCATION_TYPE.VOUCHER;

class ContractRunnerAPIs {

    public static class TriggerContractByTransactionAPI extends APIServlet.APIRequestHandler {
        private final ContractRunner contractRunner;

        TriggerContractByTransactionAPI(ContractRunner contractRunner, APITag[] apiTags, String... origParameters) {
            super(apiTags, origParameters);
            this.contractRunner = contractRunner;
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
                return contractRunner.processTransaction(transaction, isApply, isValidator).toJSONObject();
            }
            return contractRunner.generateErrorResponse(1001, "Unknown transaction %d:%s", chain.getId(), Convert.toHexString(triggerFullHash)).toJSONObject();
        }
    }

    public static class TriggerContractByHeightAPI extends APIServlet.APIRequestHandler {
        private final ContractRunner contractRunner;

        TriggerContractByHeightAPI(ContractRunner contractRunner, APITag[] apiTags, String... origParameters) {
            super(apiTags, origParameters);
            this.contractRunner = contractRunner;
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
            API.verifyPassword(req);
            int height = ParameterParser.getHeight(req);
            String contractName = req.getParameter("contractName");
            boolean isApply = "true".equalsIgnoreCase(req.getParameter("apply"));
            Block block = Nxt.getBlockchain().getBlockAtHeight(height);
            JO jo = contractRunner.processBlockContract(block, contractName, isApply, false, null);
            if (jo == null) {
                return null;
            }
            return jo.toJSONObject();
        }

        @Override
        protected boolean isChainSpecific() {
            return false;
        }
    }

    public static class TriggerContractByRequestAPI extends APIServlet.APIRequestHandler {
        private final ContractRunner contractRunner;

        TriggerContractByRequestAPI(ContractRunner contractRunner, APITag[] apiTags, String... origParameters) {
            super(apiTags, origParameters);
            this.contractRunner = contractRunner;
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
            API.verifyPassword(req);
            ContractRunnerConfig config = contractRunner.getConfig();
            if (config instanceof NullContractRunnerConfig) {
                return runnerNotInitializedResponse(config.getStatus()).toJSONObject();
            }
            String contractName = req.getParameter("contractName");
            ContractReference contractReference = ContractReference.getContractReference(config.getAccountId(), contractName);
            if (contractReference == null) {
                return contractRunner.generateErrorResponse(1002, "Contract %s not found", contractName).toJSONObject();
            }
            ContractAndSetupParameters contract = ContractLoader.loadContractAndSetupParameters(contractReference);
            RequestContext context = new RequestContext(req, config, contractName);
            JO jo = contractRunner.process(contract, context, REQUEST);
            if (jo == null) {
                return contractRunner.generateErrorResponse(1002, "Contract %s class %s returned no response", contractName, contract.getClass().getCanonicalName()).toJSONObject();
            }
            return jo.toJSONObject();
        }

        @Override
        protected boolean isChainSpecific() {
            return false;
        }
    }

    public static final class TriggerContractByVoucherAPI extends APIServlet.APIRequestHandler {

        private final ContractRunner contractRunner;

        TriggerContractByVoucherAPI(ContractRunner contractRunner, String fileParameter, APITag[] apiTags, String... origParameters) {
            super(fileParameter, apiTags, origParameters);
            this.contractRunner = contractRunner;
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
            ContractRunnerConfig config = contractRunner.getConfig();
            if (config instanceof NullContractRunnerConfig) {
                return runnerNotInitializedResponse(config.getStatus()).toJSONObject();
            }
            ParameterParser.FileData fileData = ParameterParser.getFileData(req, "voucher", true);
            if (fileData == null) {
                return JSONResponses.INCORRECT_FILE;
            }
            byte[] data = fileData.getData();
            String contractName = req.getParameter("contractName");
            JSONObject voucher = ParameterParser.parseVoucher(data);
            ContractAndSetupParameters contractAndSetupParameters = contractRunner.getContract(contractName);
            Contract contract = contractAndSetupParameters.getContract();
            VoucherContext context = new VoucherContext(new JO(voucher), config, contractName);
            JO jo = contractRunner.process(contractAndSetupParameters, context, VOUCHER);
            if (jo == null) {
                return contractRunner.generateErrorResponse(1003, "Contract %s with class %s invoked by account %s returned no response",
                        contractName, contract.getClass().getCanonicalName(), config.getAccountRs()).toJSONObject();
            }
            if (jo.isExist("transactions")) {
                jo.put("submitContractTransactionsResponse", contractRunner.submitContractTransactions(contract, jo.getJoList("transactions")));
            }
            return jo.toJSONObject();
        }

        @Override
        protected boolean isChainSpecific() {
            return false;
        }
    }

    public static class GetSupportedContractsAPI extends APIServlet.APIRequestHandler {
        private final ContractRunner contractRunner;

        GetSupportedContractsAPI(ContractRunner contractRunner, APITag[] apiTags) {
            super(apiTags);
            this.contractRunner = contractRunner;
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
            ContractRunnerConfig config = contractRunner.getConfig();
            if (config instanceof NullContractRunnerConfig) {
                return runnerNotInitializedResponse(config.getStatus()).toJSONObject();
            }
            JO response = new JO();
            response.put("status", config.getStatus());
            response.put("contractRunnerAccount", config.getAccount());
            response.put("contractRunnerAccountRS", config.getAccountRs());
            response.put("hasSecretPhrase", config.getSecretPhrase() != null);
            response.put("isValidator", config.isValidator());
            response.put("hasValidatorSecretPhrase", config.getValidatorSecretPhrase() != null);
            response.put("hasRandomSeed", !Arrays.equals(config.getRunnerSeed(), config.getPublicKey()));
            int chainId = 1;
            while (true) {
                Chain chain = Chain.getChain(chainId);
                if (chain == null) {
                    break;
                }
                long feeRateNQTPerFXT = config.getFeeRateNQTPerFXT(chainId);
                if (feeRateNQTPerFXT != -1) {
                    response.put("feeRateNQTPerFXT." + chain.getName(), Long.toUnsignedString(feeRateNQTPerFXT));
                }
                chainId++;
            }
            JA array = new JA();
            for (String name : contractRunner.getSupportedContractNames()) {
                JO contractJson = new JO();
                contractJson.put("name", name);
                ContractAndSetupParameters contractAndSetupParameters = contractRunner.getContract(name);
                contractJson.put("setupParams", contractAndSetupParameters.getParams().toJSONObject());
                ContractReference contractReference = contractRunner.getSupportedContractReference(name);
                contractJson.put("contractReference", JSONData.contractReference(contractReference, false));
                contractJson.put("contract", JSONData.contract(contractAndSetupParameters.getContract()));
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

    public static class UploadContractRunnerConfigurationAPI extends APIServlet.APIRequestHandler {
        private final ContractRunner contractRunner;

        UploadContractRunnerConfigurationAPI(ContractRunner contractRunner, String fileParameter, APITag[] apiTags, String... origParameters) {
            super(fileParameter, apiTags, origParameters);
            this.contractRunner = contractRunner;
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
            API.verifyPassword(req);
            ContractRunnerConfig config = contractRunner.getConfig();
            if (config instanceof NullContractRunnerConfig) {
                return runnerNotInitializedResponse(config.getStatus()).toJSONObject();
            }
            ParameterParser.FileData fileData = ParameterParser.getFileData(req, "config", true);
            if (fileData == null) {
                return JSONResponses.INCORRECT_FILE;
            }
            byte[] data = fileData.getData();
            JO configJson = JO.parse(new StringReader(new String(data, StandardCharsets.UTF_8)));
            contractRunner.loadConfig(configJson);
            JO response = new JO();
            response.put("configLoaded", true);
            return response.toJSONObject();
        }

        @Override
        protected boolean isChainSpecific() {
            return false;
        }
    }

    private static JO runnerNotInitializedResponse(String description) {
        JO response = new JO();
        response.put("errorCode", 1000);
        response.put("errorDescription", description);
        return response;
    }

}
