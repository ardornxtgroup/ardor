package nxt.tools;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.addons.Contract;
import nxt.addons.ContractLoader;
import nxt.addons.JA;
import nxt.addons.JO;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildChain;
import nxt.configuration.Setup;
import nxt.crypto.Crypto;
import nxt.http.API;
import nxt.http.JSONResponses;
import nxt.http.callers.DownloadTaggedDataCall;
import nxt.http.callers.GetConstantsCall;
import nxt.http.callers.GetContractReferencesCall;
import nxt.http.callers.GetSupportedContractsCall;
import nxt.lightcontracts.ContractReferenceAttachment;
import nxt.lightcontracts.ContractReferenceDeleteAttachment;
import nxt.taggeddata.TaggedDataAttachment;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.ResourceLookup;
import nxt.util.Search;
import nxt.util.TrustAllSSLProvider;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContractManager {

    private static final String CLI_HEADER = "Use Contract Manager to Upload or Reference Lightweight Contracts";
    private static final String CONTRACT_UPLOADER_JSON_FILE = "contract.uploader.json";

    private String secretPhrase;
    private long feeNQT;
    private long feeRateNQTPerFXT;
    private long minBundlerBalanceFXT;
    private ChildChain childChain;
    private JO contractSetup;

    public enum OPTION {
        NAME('n', "name", true, "contract name", false, (OPTION)null),
        HASH('h', "hash", true, "contract full hash", false, (OPTION)null),
        ACCOUNT('a', "account", true, "account id", false, (OPTION)null),
        SOURCE('s', "source", true, "path to source code file to verify", false, (OPTION)null),
        UPLOAD('u', "upload", false, "upload new contract", true, NAME),
        REFERENCE('r', "reference", false, "reference existing contract", true, HASH, NAME),
        DELETE('d', "delete", false, "delete reference", true, ACCOUNT, NAME),
        LIST('l', "list", false, "list contract references for account", true),
        VERIFY('v', "verify", false, "Verify that the source code provided represents the deployed contract", true, HASH, SOURCE);

        private final char opt;
        private final String longOpt;
        private boolean hasArgs;
        private String description;
        private boolean isAction;
        private OPTION[] dependencies;

        OPTION(char opt, String longOpt, boolean hasArgs, String description, boolean isAction, OPTION... dependencies) {
            this.opt = opt;
            this.longOpt = longOpt;
            this.hasArgs = hasArgs;
            this.description = description;
            this.isAction = isAction;
            this.dependencies = dependencies;
        }

        public String getOpt() {
            return Character.toString(opt);
        }

        public String getLongOpt() {
            return longOpt;
        }

        public boolean hasArgs() {
            return hasArgs;
        }

        public String getDescription() {
            return description;
        }

        public boolean isAction() {
            return isAction;
        }

        public OPTION[] getDependencies() {
            return dependencies;
        }
    }

    public static void main(String[] args) {
        Options options = new Options();
        Arrays.stream(OPTION.values()).forEach(o -> options.addOption(new Option(o.getOpt(), o.getLongOpt(), o.hasArgs(), o.getDescription())));

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            Logger.logInfoMessage(e.getMessage(), e);
            formatter.printHelp(ContractManager.class.getName(), options);
            return;
        }
        List<OPTION> specifiedOptions = Arrays.stream(OPTION.values()).filter(o -> cmd.hasOption(o.getOpt())).collect(Collectors.toList());
        List<OPTION> actionOptions = specifiedOptions.stream().filter(OPTION::isAction).collect(Collectors.toList());
        if (actionOptions.size() != 1) {
            String actions = Arrays.stream(OPTION.values()).filter(OPTION::isAction).map(o -> "--" + o.getLongOpt()).collect(Collectors.joining(","));
            formatter.printHelp(ContractManager.class.getName(), CLI_HEADER, options, String.format("Exactly one of the arguments %s has to be specified", actions));
            return;
        }
        OPTION action = actionOptions.get(0);
        OPTION[] dependencies = action.getDependencies();
        String missingDependencies = Arrays.stream(dependencies).filter(d -> !specifiedOptions.contains(d)).map(o -> "--" + o.getLongOpt()).collect(Collectors.joining(","));
        if (missingDependencies.length() > 0) {
            formatter.printHelp(ContractManager.class.getName(), CLI_HEADER, options, String.format("Action --%s is missing the following arguments %s", action.getLongOpt(), missingDependencies));
            return;
        }

        Nxt.init(Setup.CLIENT_APP);
        try {
            try {
                GetConstantsCall.create().remote(getUrl()).call();
            } catch (Exception e) {
                if (e.getCause() != null && e.getCause() instanceof ConnectException) {
                    Logger.logErrorMessage("Cannot connect to " + getUrl() + " make sure the node is running");
                } else {
                    Logger.logErrorMessage("Error connecting to remote node ", e);
                }
                return;
            }
            ContractManager contractManager = new ContractManager();
            if (action == OPTION.LIST) {
                contractManager.list(cmd.getOptionValue(OPTION.ACCOUNT.getOpt()), cmd.getOptionValue(OPTION.NAME.getOpt()));
                return;
            }
            String contractName = cmd.getOptionValue(OPTION.NAME.getOpt());
            contractManager.init(contractName);
            if (action == OPTION.UPLOAD) {
                byte[] contractFullHash = contractManager.upload(contractName);
                if (contractFullHash == null) {
                    return;
                }
                contractManager.reference(contractName, contractFullHash);
            } else if (action == OPTION.REFERENCE) {
                byte[] contractFullHash = Convert.parseHexString(cmd.getOptionValue(OPTION.HASH.getOpt()));
                contractManager.reference(contractName, contractFullHash);
            } else if (action == OPTION.DELETE) {
                contractManager.delete(contractName);
            } else if (action == OPTION.VERIFY) {
                contractManager.verify(cmd.getOptionValue(OPTION.HASH.getOpt()), cmd.getOptionValue(OPTION.SOURCE.getOpt()));
            } else {
                formatter.printHelp(ContractManager.class.getName(), CLI_HEADER, options, "Should never happen");
            }
        } finally {
            Nxt.shutdown();
        }
    }

    public void init(String contractName) {
        secretPhrase = Convert.emptyToNull(Nxt.getStringProperty("contract.manager.secretPhrase", null, true));
        if (secretPhrase == null) {
            throw new IllegalArgumentException("contract.manager.secretPhrase not specified in nxt.properties");
        }
        feeNQT = Nxt.getIntProperty("contract.manager.feeNQT", -1);
        feeRateNQTPerFXT = Nxt.getIntProperty("contract.manager.feeRateNQTPerFXT", -1);
        minBundlerBalanceFXT = Nxt.getIntProperty("contract.manager.minBundlerBalanceFXT", 0);
        childChain = ChildChain.IGNIS;
        Path contractUploadParamsPath = Paths.get(Nxt.getUserHomeDir(), "conf", CONTRACT_UPLOADER_JSON_FILE);
        Logger.logInfoMessage("Loading contract upload configuration from: %s", contractUploadParamsPath.toAbsolutePath());
        JO contractUploaderConfig = ResourceLookup.loadJsonResource(contractUploadParamsPath);
        if (contractUploaderConfig == null) {
            // The first time we start the contract manager it will copy its configuration file from the samples template to the conf folder
            Path from = Paths.get("./addons/resources/" + CONTRACT_UPLOADER_JSON_FILE);
            try {
                Files.copy(from, contractUploadParamsPath);
                Logger.logInfoMessage(String.format("Sample contract uploader params file copied from %s to %s",  from, contractUploadParamsPath));
                contractUploaderConfig = ResourceLookup.loadJsonResource(contractUploadParamsPath);
                if (contractUploaderConfig == null) {
                    throw new IOException(String.format("Failed to create %s", contractUploadParamsPath));
                }
            } catch (IOException e2) {
                Logger.logErrorMessage(String.format("Cannot write or load sample contract uploader params file %s", contractUploadParamsPath), e2);
                return;
            }
        }
        JA contracts = contractUploaderConfig.getArray("contracts");
        List<JO> contractList = contracts.objects();
        Logger.logInfoMessage("Contract configuration loaded for contracts: %s", contractList.stream().map(c -> c.getString("className")).collect(Collectors.joining(", ")));
        if (contractName == null) {
            Logger.logInfoMessage("Contract name not provided");
            return;
        }
        contractSetup = contractList.stream().filter(c -> contractName.equals(c.getString("className"))).findFirst().orElse(null);
        if (contractSetup == null) {
            throw new IllegalArgumentException(String.format("Contract definition for contract '%s' not found in %s", contractName, contractUploadParamsPath.toAbsolutePath()));
        }
    }

    public void list(String account, String name) {
        if (account == null) {
            // See if the node has a contract runner enabled, if so, list the contract runner account
            try {
                JO supportedContractsResponse = GetSupportedContractsCall.create().adminPassword(API.adminPassword).remote(getUrl()).call();
                if (!supportedContractsResponse.isExist("contractAccount")) {
                    Logger.logInfoMessage("Account not specified and cannot determine contract runner account - %s", supportedContractsResponse.toJSONString());
                    return;
                }
                account = supportedContractsResponse.getString("contractAccount");
            } catch (Exception e) {
                Logger.logInfoMessage("Contract runner not enabled %s", e.getMessage());
                return;
            }
        }
        JO response = listImpl(account, name);
        Logger.logInfoMessage("Listing contracts for account %s", name);
        if (response.isExist("contractReferences")) {
            List<JO> references = response.getJoList("contractReferences");
            for (JO reference : references) {
                Logger.logInfoMessage(reference.toJSONString());
            }
        } else {
            Logger.logInfoMessage("No contract references found");
        }
        Logger.logInfoMessage("End of list");
    }

    public JO listImpl(String account, String name) {
        GetContractReferencesCall getContractReferences = GetContractReferencesCall.create().account(account).remote(getUrl());
        if (name != null) {
            getContractReferences.contractName(name);
        }
        return getContractReferences.call();
    }

    public byte[] upload(String contractName) {
        JO response = uploadImpl(contractName);
        if (!response.isExist("fullHash")) {
            Logger.logErrorMessage("Upload error: " + response.toJSONString());
            return null;
        }
        return response.parseHexString("fullHash");
    }

    public JO uploadImpl(String contractName) {
        String packageName = contractSetup.getString("packageName");
        String filePath = contractSetup.getString("filePath");
        if (filePath == null) {
            filePath = (packageName + "." + contractName).replace('.', '/') + ".class";
        }
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        Logger.logInfoMessage("Loading resource from " + filePath);
        byte[] resourceBytes = ResourceLookup.getResourceBytes(filePath);
        if (resourceBytes == null) {
            throw new IllegalStateException("Cannot load " + filePath);
        }
        String type;
        String detectedMimeType = Search.detectMimeType(resourceBytes, fileName);
        if (detectedMimeType != null) {
            type = detectedMimeType.substring(0, Math.min(detectedMimeType.length(), Constants.MAX_TAGGED_DATA_TYPE_LENGTH));
        } else {
            throw new IllegalStateException("Cannot detect file type " + filePath);
        }
        Contract contract;
        switch (type) {
            case ContractLoader.CLASS_FILE_MIME_TYPE: {
                contract = ContractLoader.loadContract(packageName + "." + contractName, resourceBytes);
                if (contract == null) {
                    throw new IllegalStateException("Cannot load contract " + contractName);
                }
                break;
            }
            case ContractLoader.JAR_FILE_MIME_TYPE: {
                contract = ContractLoader.loadContractFromJar(packageName + "." + contractName, resourceBytes);
                if (contract == null) {
                    throw new IllegalStateException("Cannot load contract " + contractName + " from Jar file");
                }
                break;
            }
            default:
                throw new IllegalArgumentException(String.format("Resource mime type for %s does not represent a known executable contract format", filePath));
        }
        TaggedDataAttachment attachment;
        try {
            JO contractDescription = new JO();
            if (contractSetup.isExist("version")) {
                contractDescription.put("version", contractSetup.getString("version"));
            }
            if (contractSetup.isExist("url")) {
                contractDescription.put("url", contractSetup.getString("url"));
            }
            if (contractSetup.isExist("comment")) {
                contractDescription.put("comment", contractSetup.getString("comment"));
            }
            attachment = new TaggedDataAttachment(packageName + "." + contractName,
                    contractDescription.toJSONString(), contractSetup.getString("tags", ""), type, "contracts", false, fileName, resourceBytes);
        } catch (NxtException.NotValidException e) {
            throw new IllegalArgumentException(e);
        }
        JO uploadContractTransaction = LocalSigner.signAndBroadcast(childChain, 0, attachment, secretPhrase, feeNQT, feeRateNQTPerFXT, minBundlerBalanceFXT, null, getUrl());
        if (!uploadContractTransaction.isExist("fullHash")) {
            Logger.logErrorMessage("Contract not uploaded %s", contractName);
            return uploadContractTransaction;
        }
        Logger.logInfoMessage("Contract class %s uploaded %s", attachment.getName(), uploadContractTransaction.getString("fullHash"));
        return uploadContractTransaction;
    }

    public void reference(String contractName, byte[] contractFullHash) {
        reference(contractName, contractFullHash, null);
    }

    public JO reference(String contractName, byte[] contractFullHash, JO contractParams) {
        ChainTransactionId contractId = new ChainTransactionId(childChain.getId(), contractFullHash);
        if (contractParams == null) {
            contractParams = contractSetup.getJo("params");
        }
        ContractReferenceAttachment attachment = new ContractReferenceAttachment(contractName, contractParams == null ? null : contractParams.toJSONString(), contractId);
        URL url = getUrl();
        JO contractReferenceTransaction = LocalSigner.signAndBroadcast(ChildChain.IGNIS, 0, attachment, secretPhrase, feeNQT, feeRateNQTPerFXT, minBundlerBalanceFXT, null, url);
        if (!contractReferenceTransaction.isExist("fullHash")) {
            Logger.logErrorMessage("Contract reference not registered %s", contractName);
            return contractReferenceTransaction;
        }
        Logger.logInfoMessage("Contract reference registered %s=%s for contract %s", attachment.getContractName(), attachment.getContractParams(), attachment.getContractId().toString());
        return contractReferenceTransaction;
    }

    public void delete(String contractName) {
        String account = Convert.rsAccount(Account.getId(Crypto.getPublicKey(secretPhrase)));
        JO response = listImpl(account, contractName);
        List<JO> contractReferences = response.getJoList("contractReferences");
        if (contractReferences.size() == 0) {
            Logger.logErrorMessage("Cannot find contract reference for account %s contract name %s", account, contractName);
            return;
        }
        long referenceId = contractReferences.get(0).getEntityId("id");
        ContractReferenceDeleteAttachment attachment = new ContractReferenceDeleteAttachment(referenceId);
        URL url = getUrl();
        JO contractReferenceDeleteTransaction = LocalSigner.signAndBroadcast(ChildChain.IGNIS, 0, attachment, secretPhrase, feeNQT, feeRateNQTPerFXT, minBundlerBalanceFXT, null, url);
        if (!contractReferenceDeleteTransaction.isExist("fullHash")) {
            Logger.logErrorMessage("Contract reference delete failed %s", contractReferenceDeleteTransaction.toJSONString());
            return;
        }
        Logger.logInfoMessage("Contract reference %s deleted for account %s contract name %s", Long.toUnsignedString(attachment.getContractReferenceId()), account, contractName);
    }

    /**
     * We assume that the source file produces a single class file and verify that this class file is stored in the
     * blockchain cloud data which corresponds to the provided transaction hash on IGNIS.
     * @param hash hash of the cloud data transaction which stores the contract file
     * @param sourceFile path to the source file to compile
     * @return true if the compiled source code is identical to the code deployed to the blockchain, false otherwise
     */
    public boolean verify(String hash, String sourceFile) {
        byte[] cloudDataClassBytes = DownloadTaggedDataCall.create(ChildChain.IGNIS.getId()).remote(getUrl()).transactionFullHash(hash).retrieve("true").download();
        if (cloudDataClassBytes == null) {
            Logger.logErrorMessage("Cannot load cloud data with hash %s", hash);
            return false;
        }
        if (Convert.toString(cloudDataClassBytes).equals(JSONResponses.PRUNED_TRANSACTION.toString())) {
            Logger.logErrorMessage("Cannot load cloud data with hash %s transaction is pruned and cannot be retrieved by the current node", hash);
            return false;
        }
        String javacOptions = JDKToolsWrapper.javap(cloudDataClassBytes);
        if (javacOptions == null) {
            return false;
        }
        Logger.logInfoMessage("Use the following javac option: " + javacOptions);
        Map<String, byte[]> classFiles = JDKToolsWrapper.compile(sourceFile, javacOptions);
        if (classFiles == null || classFiles.size() == 0) {
            Logger.logErrorMessage("No class files were generated for %s", sourceFile);
            return false;
        }
        if (classFiles.size() > 1) {
            Logger.logErrorMessage("More than one class file was compiled from %s we can only verify a single class file at the moment", sourceFile);
            return false;
        }
        byte[] compiledClassBytes = classFiles.values().stream().findFirst().orElse(null);
        if (compiledClassBytes == null) {
            Logger.logErrorMessage("Cannot load class bytes for compiled class %s", classFiles.keySet().stream().findFirst().orElse(null));
            return false;
        }
        if (Arrays.equals(compiledClassBytes, cloudDataClassBytes)) {
            Logger.logInfoMessage("Verification succeeded - class files are identical");
            return true;
        } else {
            Logger.logErrorMessage("Verification failed - class files differ");
            Logger.logErrorMessage("Compiled class bytes length " + compiledClassBytes.length);
            Logger.logErrorMessage("Cloud data class bytes length " + cloudDataClassBytes.length);
            Logger.logErrorMessage("Make sure the javac options used for verification are the same as the javac options used when compiling the blockchain contract");
            Logger.logErrorMessage("Make sure the Java version used for verification is the same as the Java version used when compiling the blockchain contract");
            return false;
        }
    }

    private static URL getUrl() {
        String host = Convert.emptyToNull(Nxt.getStringProperty("contract.manager.serverAddress"));
        if (host == null) {
            return null;
        }
        String protocol = "http";
        boolean useHttps = Nxt.getBooleanProperty("contract.manager.useHttps");
        if (useHttps) {
            protocol = "https";
            HttpsURLConnection.setDefaultSSLSocketFactory(TrustAllSSLProvider.getSslSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(TrustAllSSLProvider.getHostNameVerifier());
        }
        int port = Constants.isTestnet ? API.TESTNET_API_PORT : Nxt.getIntProperty("nxt.apiServerPort");
        URL url;
        try {
            url = new URL(protocol, host, port, "/nxt");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        Logger.logInfoMessage("Connecting to URL " + url);
        return url;
    }

}
