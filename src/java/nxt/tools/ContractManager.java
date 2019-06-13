/*
 * Copyright © 2016-2019 Jelurida IP B.V.
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

package nxt.tools;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.addons.Contract;
import nxt.addons.ContractInfo;
import nxt.addons.ContractLoader;
import nxt.addons.ContractSetupParameter;
import nxt.addons.JA;
import nxt.addons.JO;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildChain;
import nxt.configuration.Setup;
import nxt.crypto.Crypto;
import nxt.http.API;
import nxt.http.JSONResponses;
import nxt.http.callers.EventRegisterCall;
import nxt.http.callers.EventWaitCall;
import nxt.http.callers.GetConstantsCall;
import nxt.http.callers.GetContractReferencesCall;
import nxt.http.callers.GetSupportedContractsCall;
import nxt.http.callers.GetTaggedDataCall;
import nxt.http.responses.TaggedDataResponse;
import nxt.http.responses.TaggedDataResponseImpl;
import nxt.lightcontracts.ContractReferenceAttachment;
import nxt.lightcontracts.ContractReferenceDeleteAttachment;
import nxt.taggeddata.TaggedDataAttachment;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.ReproducibleJarOutputStream;
import nxt.util.ResourceLookup;
import nxt.util.Search;
import nxt.util.TrustAllSSLProvider;
import nxt.util.security.BlockchainPermission;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class ContractManager {

    private static final String CLI_HEADER = "Use Contract Manager to Upload or Reference Lightweight Contracts";
    private static final String CONTRACT_UPLOADER_JSON_FILE = "contract.uploader.json";
    private static final String CONTRACT_MANAGER_PROPERTY_FORMAT = "contract.%s.%s";
    private static final String CONTRACT_MANAGER_PROPERTY_PARAM_FORMAT = "contract.%s.param.%s";

    private String secretPhrase;
    private long feeNQT;
    private long feeRateNQTPerFXT;
    private long minBundlerBalanceFXT;
    private ChildChain childChain;
    private JO contractSetup;
    private static URL url;

    public enum OPTION {
        NAME('n', "name", true, "contract name", false, (OPTION)null),
        PACKAGE('p', "package", true, "package name", false, (OPTION)null),
        HASH('h', "hash", true, "contract full hash", false, (OPTION)null),
        ACCOUNT('a', "account", true, "account id", false, (OPTION)null),
        SOURCE('s', "source", true, "path to source code file to verify", false, (OPTION)null),
        UPLOAD('u', "upload", false, "upload new contract", true, NAME, PACKAGE),
        REFERENCE('r', "reference", false, "reference existing contract", true, HASH, NAME),
        DELETE('d', "delete", false, "delete reference", true, NAME),
        LIST('l', "list", false, "list contract references for account", true, ACCOUNT),
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
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("tools"));
        }

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
        System.setProperty("nxt.logging.properties.file.name.prefix", "contract.manager.");
        Nxt.init(Setup.CLIENT_APP);
        try {
            try {
                GetConstantsCall.create().remote(getUrl()).call();
            } catch (Exception e) {
                if (e.getCause() != null && e.getCause() instanceof ConnectException) {
                    Logger.logErrorMessage("Cannot connect to " + getUrl() + " make sure the node is running");
                } else {
                    Logger.logErrorMessage("Error connecting to remote node " + getUrl(), e);
                }
                return;
            }
            Logger.logInfoMessage("========================");
            Logger.logInfoMessage("Contract Manager Started");
            Logger.logInfoMessage("========================");
            ContractManager contractManager = new ContractManager();
            if (action == OPTION.LIST) {
                contractManager.list(cmd.getOptionValue(OPTION.ACCOUNT.getOpt()), cmd.getOptionValue(OPTION.NAME.getOpt()));
                return;
            }
            String contractName = cmd.getOptionValue(OPTION.NAME.getOpt());
            contractManager.init(contractName);
            if (action == OPTION.UPLOAD) {
                ContractData contractData = contractManager.upload(contractName, cmd.getOptionValue(OPTION.PACKAGE.getOpt()));
                byte[] contractFullHash = contractData.getResponse().parseHexString("fullHash");
                if (contractFullHash == null) {
                    return;
                }
                contractManager.reference(contractData, contractFullHash);
                contractManager.waitForNextBlock();
            } else if (action == OPTION.REFERENCE) {
                byte[] contractFullHash = Convert.parseHexString(cmd.getOptionValue(OPTION.HASH.getOpt()));
                ContractData contractData = new ContractData(contractName);
                contractManager.reference(contractData, contractFullHash);
                contractManager.waitForNextBlock();
            } else if (action == OPTION.DELETE) {
                contractManager.delete(contractName);
                contractManager.waitForNextBlock();
            } else if (action == OPTION.VERIFY) {
                contractManager.verify(cmd.getOptionValue(OPTION.HASH.getOpt()), cmd.getOptionValue(OPTION.SOURCE.getOpt()));
            } else {
                formatter.printHelp(ContractManager.class.getName(), CLI_HEADER, options, "Should never happen");
            }
        } finally {
            Logger.logInfoMessage("==============================");
            Logger.logInfoMessage("Contract Manager Shutting Down");
            Logger.logInfoMessage("==============================");
            Nxt.shutdown();
        }
    }

    public void init(String contractName) {
        String secretPhrasePropertyKey = "contract.manager.secretPhrase";
        secretPhrase = Convert.emptyToNull(Nxt.getStringProperty(secretPhrasePropertyKey, null, true));
        if (secretPhrase == null) {
            throw new IllegalArgumentException(String.format("%s not specified in nxt.properties", secretPhrasePropertyKey));
        }
        feeNQT = Nxt.getIntProperty("contract.manager.feeNQT", -1);
        feeRateNQTPerFXT = Nxt.getIntProperty("contract.manager.feeRateNQTPerFXT", -1);
        minBundlerBalanceFXT = Nxt.getIntProperty("contract.manager.minBundlerBalanceFXT", 0);
        childChain = ChildChain.IGNIS;
        Path contractUploadParamsPath = Paths.get(Nxt.getUserHomeDir(), "conf", CONTRACT_UPLOADER_JSON_FILE);
        Logger.logInfoMessage("Loading contract upload configuration from: %s", contractUploadParamsPath.toAbsolutePath());
        JO contractUploaderConfig = ResourceLookup.loadJsonResource(contractUploadParamsPath);
        if (contractUploaderConfig == null) {
            // The first time we start the contract manager it will copy its configuration file from the samples template
            // in the installation folder to the user conf folder
            Path from = Paths.get("./addons/resources/" + CONTRACT_UPLOADER_JSON_FILE);
            try {
                Files.copy(from, contractUploadParamsPath);
                Logger.logInfoMessage(String.format("Sample contract uploader params file copied from %s to %s", from, contractUploadParamsPath));
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
            Logger.logInfoMessage(String.format("Contract definition for contract '%s' not found in %s will use only parameters from properties file", contractName, contractUploadParamsPath.toAbsolutePath()));
            contractSetup = new JO();
        }
    }

    public void list(String account, String name) {
        if (Convert.emptyToNull(account) == null) {
            // See if the node has a contract runner enabled, if so, list the contract runner account
            try {
                JO supportedContractsResponse = GetSupportedContractsCall.create().remote(getUrl()).call();
                if (!supportedContractsResponse.isExist("contractRunnerAccountRS")) {
                    Logger.logInfoMessage("Account not specified and cannot determine contract runner account - %s", supportedContractsResponse.toJSONString());
                    return;
                }
                account = supportedContractsResponse.getString("contractRunnerAccountRS");
            } catch (Exception e) {
                Logger.logInfoMessage("Contract runner not enabled %s", e.getMessage());
                return;
            }
        }
        JO response = listImpl(account, name);
        Logger.logInfoMessage("Listing contracts for account %s", account);
        if (response.isExist("contractReferences")) {
            List<JO> references = response.getJoList("contractReferences");
            for (JO reference : references) {
                Logger.logInfoMessage(reference.toJSONString());
            }
            Logger.logInfoMessage("End of list");
        } else {
            Logger.logInfoMessage("No contract references found");
        }
    }

    public JO listImpl(String account, String name) {
        GetContractReferencesCall getContractReferences = GetContractReferencesCall.create().account(account).remote(getUrl());
        if (name != null) {
            getContractReferences.contractName(name);
        }
        return getContractReferences.call();
    }

    public ContractData upload(String contractName, String packageName) {
        ContractData contractData = uploadImpl(contractName, packageName);
        JO response = contractData.getResponse();
        if (!response.isExist("fullHash")) {
            Logger.logErrorMessage("Upload error: " + response.toJSONString());
        }
        return contractData;
    }

    public ContractData uploadImpl(String contractName, String packageName) {
        if (packageName == null) {
            if (!contractSetup.isExist("packageName")) {
                throw new IllegalStateException("Contract package name not specified neither in the command nor in the contract manager config file");
            }
            packageName = contractSetup.getString("packageName");
        }
        String fullName = packageName + "." + contractName;
        String filePath = Nxt.getStringProperty(String.format(CONTRACT_MANAGER_PROPERTY_FORMAT, contractName, "filePath"));
        if (filePath == null) {
            filePath = contractSetup.getString("filePath");
            if (filePath == null) {
                filePath = fullName.replace('.', '/') + ".class";
            }
        }
        Logger.logInfoMessage("Loading resource from " + filePath);
        byte[] resourceBytes = ResourceLookup.getResourceBytes(filePath);
        if (resourceBytes == null) {
            throw new IllegalStateException("Cannot load " + filePath);
        }
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        String mimeType = Search.detectMimeType(resourceBytes, fileName);
        if (mimeType == null) {
            throw new IllegalStateException("Cannot determine contract mime type i.e. class or jar file");
        }
        ContractData tempContractData = loadContract(fullName, resourceBytes, mimeType, filePath);
        Contract contract = tempContractData.getContract();

        // Log contract annotations
        Annotation[] annotations = contract.getClass().getAnnotations();
        Arrays.stream(annotations).forEach(a -> Logger.logInfoMessage(a.toString()));

        // Parse the contract info annotation. We first try to load the data from the json config, then from nxt.properties then fall back to the
        // annotation default value
        JO contractDescription = new JO();
        ContractInfo contractInfoAnnotation = contract.getClass().getDeclaredAnnotation(ContractInfo.class);
        Method[] methods = ContractInfo.class.getDeclaredMethods();
        Arrays.stream(methods).forEach(m -> {
            if (contractSetup.isExist(m.getName())) {
                contractDescription.put(m.getName(), contractSetup.getString(m.getName()));
            } else if (Nxt.getStringProperty(String.format(CONTRACT_MANAGER_PROPERTY_FORMAT, contractName, m.getName())) != null) {
                contractDescription.put(m.getName(), Nxt.getStringProperty(String.format(CONTRACT_MANAGER_PROPERTY_FORMAT, contractName, m.getName())));
            } else if (contractInfoAnnotation != null) {
                String value;
                try {
                    value = (String)m.invoke(contractInfoAnnotation);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException();
                }
                if (Convert.emptyToNull(value) != null) {
                    contractDescription.put(m.getName(), value);
                }
            }
        });
        Logger.logInfoMessage("Contract Description %s", contractDescription.toJSONString());

        TaggedDataAttachment attachment;
        byte[] taggedDataHash;
        try {
            attachment = new TaggedDataAttachment(fullName,
                    contractDescription.toJSONString(), contractSetup.getString("tags", ""), tempContractData.getMimeType(), "contracts", false, fileName, tempContractData.getBytes());
            taggedDataHash = attachment.getHash();
        } catch (NxtException.NotValidException e) {
            throw new IllegalArgumentException(e);
        }
        JO uploadContractTransaction = LocalSigner.signAndBroadcast(childChain, 0, attachment, secretPhrase, feeNQT, feeRateNQTPerFXT, minBundlerBalanceFXT, null, getUrl());
        if (uploadContractTransaction.isExist("fullHash")) {
            Logger.logInfoMessage("Contract class %s uploaded %s", attachment.getName(), uploadContractTransaction.getString("fullHash"));
        } else {
            Logger.logErrorMessage("Contract %s not uploaded, response %s", contractName, uploadContractTransaction.toJSONString());
        }
        return new ContractData(contractName, tempContractData.getFullName(), uploadContractTransaction, tempContractData.getBytes(), tempContractData.getMimeType(), tempContractData.getContract(), taggedDataHash);
    }

    private ContractData loadContract(String fullName, byte[] resourceBytes, String mimeType, String filePath) {
        Contract contract;
        switch (mimeType) {
            case ContractLoader.CLASS_FILE_MIME_TYPE: {
                ClassLoader classLoader = new ContractLoader.CloudDataClassLoader();
                contract = ContractLoader.loadContract(classLoader, fullName, resourceBytes, null, null);
                if (contract == null) {
                    throw new IllegalStateException("Cannot load contract from file " + fullName);
                }
                Class<?>[] classes = contract.getClass().getDeclaredClasses();
                if (classes.length > 0 && filePath != null) {
                    try {
                        // Contract has inner classes, need to package and deploy it as a Jar file
                        Manifest manifest = new Manifest();
                        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                        ByteArrayOutputStream jarStream = new ByteArrayOutputStream();
                        JarOutputStream target = new ReproducibleJarOutputStream(jarStream, manifest, Constants.EPOCH_BEGINNING);

                        // Add the sub-directories to Jar
                        Path path = Paths.get(filePath);
                        if (path.getNameCount() > 1) {
                            for (int i=1; i<path.getNameCount(); i++) {
                                String dirName = path.subpath(0, i).toString();
                                dirName = dirName.replace("\\", "/");
                                if (!dirName.endsWith("/")) {
                                    dirName += "/";
                                }
                                JarEntry entry = new JarEntry(dirName);
                                target.putNextEntry(entry);
                                target.closeEntry();
                            }
                        }

                        // Add the original contract class to the Jar
                        JarEntry contractEntry = new JarEntry(filePath);
                        target.putNextEntry(contractEntry);
                        target.write(resourceBytes);

                        // Add all inner classes
                        Arrays.stream(classes).forEach(c -> {
                            String innerClassName = c.getCanonicalName();
                            String[] tokens = filePath.split(".class");
                            String innerClassFilePath = tokens[0] + "$" + c.getSimpleName() + ".class";
                            Logger.logInfoMessage("Loading inner class %s resource from %s", innerClassName, innerClassFilePath);
                            byte[] innerClassBytes = ResourceLookup.getResourceBytes(innerClassFilePath);
                            if (innerClassBytes == null) {
                                throw new IllegalStateException("Cannot load inner class from " + innerClassFilePath);
                            }
                            JarEntry entry = new JarEntry(innerClassFilePath);
                            try {
                                target.putNextEntry(entry);
                                target.write(innerClassBytes);
                                target.closeEntry();
                            } catch (IOException e) {
                                throw new IllegalStateException();
                            }
                        });
                        target.close();
                        resourceBytes = jarStream.toByteArray();
                        mimeType = ContractLoader.JAR_FILE_MIME_TYPE;
                        classLoader = new ContractLoader.CloudDataClassLoader();
                        contract = ContractLoader.loadContractFromJar(classLoader, fullName, resourceBytes, null, null);
                        if (contract == null) {
                            throw new IllegalStateException("Cannot load contract " + fullName + " from Jar file");
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
                break;
            }
            case ContractLoader.JAR_FILE_MIME_TYPE: {
                ClassLoader classLoader = new ContractLoader.CloudDataClassLoader();
                contract = ContractLoader.loadContractFromJar(classLoader, fullName, resourceBytes, null, null);
                if (contract == null) {
                    throw new IllegalStateException("Cannot load contract " + fullName + " from Jar file");
                }
                break;
            }
            default:
                throw new IllegalArgumentException(String.format("Resource mime type for %s does not represent a known executable contract format", fullName));
        }
        return new ContractData(null, fullName, null, resourceBytes, mimeType, contract, null);
    }

    public void reference(ContractData contractData, byte[] contractFullHash) {
        JO setupParams = null;
        if (contractSetup != null && contractSetup.isExist("params")) {
            setupParams = contractSetup.getJo("params");
        }
        reference(contractData, contractFullHash, setupParams);
    }

    public JO reference(ContractData contractData, byte[] fullHash, JO uploaderParams) {
        String contractName = contractData.getContractName();
        Contract contract = contractData.getContract();
        if (contract == null) {
            JO response = GetTaggedDataCall.create(ChildChain.IGNIS.getId()).transactionFullHash(fullHash).includeData(true).retrieve(true).remote(getUrl()).call();
            TaggedDataResponse taggedDataResponse = new TaggedDataResponseImpl(response);
            contract = loadContract(taggedDataResponse.getName(), taggedDataResponse.getData(), taggedDataResponse.getType(), null).getContract();
        }
        JO contractParams;
        if (uploaderParams != null) {
            contractParams = uploaderParams;
        } else {
            contractParams = new JO();
        }
        Class<?> parametersClass = ContractLoader.getParametersProvider(contract);
        if (parametersClass != null) {
            Method[] parameterMethods = parametersClass.getDeclaredMethods();
            Arrays.stream(parameterMethods).forEach(method -> {
                if (method.getDeclaredAnnotation(ContractSetupParameter.class) == null) {
                    return;
                }
                String key = method.getName();
                String value = Nxt.getStringProperty(String.format(CONTRACT_MANAGER_PROPERTY_PARAM_FORMAT, contractName, key));
                if (value == null) {
                    return;
                }
                contractParams.put(key, value);
            });
        }
        Field[] declaredFields = contract.getClass().getDeclaredFields();
        Arrays.stream(declaredFields).forEach(field -> {
            if (field.getDeclaredAnnotation(ContractSetupParameter.class) == null) {
                return;
            }
            String key = field.getName();
            String value = Nxt.getStringProperty(String.format(CONTRACT_MANAGER_PROPERTY_PARAM_FORMAT, contractName, key));
            if (value == null) {
                return;
            }
            contractParams.put(key, value);
        });
        String setupParamsStr = null;
        if (contractParams.size() > 0) {
            setupParamsStr = contractParams.toJSONString();
        }
        ContractReferenceAttachment attachment = new ContractReferenceAttachment(contractName, setupParamsStr, new ChainTransactionId(childChain.getId(), fullHash));
        JO contractReferenceTransaction = LocalSigner.signAndBroadcast(ChildChain.IGNIS, 0, attachment, secretPhrase, feeNQT, feeRateNQTPerFXT, minBundlerBalanceFXT, null, getUrl());
        if (!contractReferenceTransaction.isExist("fullHash")) {
            Logger.logErrorMessage("Contract reference not registered %s response %s", contractName, contractReferenceTransaction.toJSONString());
            return contractReferenceTransaction;
        }
        Logger.logInfoMessage("Contract reference %s registered with params '%s' for contract %s", attachment.getContractName(), attachment.getContractParams(), attachment.getContractId().toString());
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
        JO contractReferenceDeleteTransaction = LocalSigner.signAndBroadcast(ChildChain.IGNIS, 0, attachment, secretPhrase, feeNQT, feeRateNQTPerFXT, minBundlerBalanceFXT, null, getUrl());
        if (!contractReferenceDeleteTransaction.isExist("fullHash")) {
            Logger.logErrorMessage("Contract reference delete for %s account %s failed with response %s", contractName, account, contractReferenceDeleteTransaction.toJSONString());
            return;
        }
        Logger.logInfoMessage("Contract reference %s deleted for account %s contract name %s", Long.toUnsignedString(attachment.getContractReferenceId()), account, contractName);
    }

    /**
     * We assume that the contract is composed of a single source file which is deployed to the blockchain either as a single class file or
     * as a Jar file containing all its inner classes. This compiled contract is identified by the provided transaction hash on the Ignis chain.
     * @param hash hash of the cloud data transaction which stores the contract file
     * @param sourceFile path to the source file to compile
     * @return true if the compiled source code is identical to the code deployed to the blockchain, false otherwise
     */
    public boolean verify(String hash, String sourceFile) {
        JO taggedData = GetTaggedDataCall.create(ChildChain.IGNIS.getId()).remote(getUrl()).transactionFullHash(hash).includeData(true).retrieve(true).call();
        if (taggedData.toJSONObject().equals(JSONResponses.PRUNED_TRANSACTION)) {
            Logger.logErrorMessage("Cannot load cloud data with hash %s transaction is pruned and cannot be retrieved by the current node", hash);
            return false;
        }
        TaggedDataResponse taggedDataResponse = new TaggedDataResponseImpl(taggedData);
        if (taggedDataResponse.getData() == null) {
            Logger.logErrorMessage("Cannot load cloud data with hash %s", hash);
            return false;
        }
        byte[] contractBytes;
        Map<String, byte[]> classFileBytes = new HashMap<>();
        String mimeType = taggedDataResponse.getType();
        switch (mimeType) {
            case ContractLoader.CLASS_FILE_MIME_TYPE: {
                // Verify the cloud data class file
                contractBytes = taggedDataResponse.getData();
                classFileBytes.put(taggedDataResponse.getName(), contractBytes);
                break;
            }
            case ContractLoader.JAR_FILE_MIME_TYPE: {
                // Load the class files to verify from the cloud data Jar
                ContractLoader.loadContractFromJar(new ContractLoader.CloudDataClassLoader(), taggedDataResponse.getName(), taggedDataResponse.getData(), null, null, classFileBytes);
                contractBytes = classFileBytes.get(taggedDataResponse.getName());
                break;
            }
            default:
                throw new IllegalArgumentException(String.format("Resource mime type for transaction %s does not represent a known executable contract format", hash));
        }

        // Determine the javac options to use by running javap on the compiled contract class
        String javacOptions = JDKToolsWrapper.javap(contractBytes);
        if (javacOptions == null) {
            return false;
        }
        Logger.logInfoMessage("Use the following javac option: " + javacOptions);

        // Compile the source code to verify
        String tmpDir = System.getProperty("java.io.tmpdir");
        Path outputPath = Paths.get(tmpDir, "src");
        Map<String, byte[]> classFiles = JDKToolsWrapper.compile(sourceFile, javacOptions, outputPath);
        if (classFiles == null || classFiles.size() == 0) {
            Logger.logErrorMessage("No class files were generated for %s", sourceFile);
            return false;
        }

        // Compare the class files loaded from the cloud data with the compiled class files
        for (String classFile : classFiles.keySet()) {
            byte[] compiledClassBytes = classFiles.get(classFile);
            if (compiledClassBytes == null) {
                Logger.logErrorMessage("Cannot load class bytes for compiled class %s", classFiles.keySet().stream().findFirst().orElse(null));
                return false;
            }

            // Convert absolute file name to fully qualified class name
            String relativePath = classFile.replace(outputPath.toString(), "").replace(".class", "");
            String className = relativePath.substring(1).replace("\\", ".").replace("/", ".");
            if (!Arrays.equals(compiledClassBytes, classFileBytes.get(className))) {
                Logger.logErrorMessage("Verification failed - class files differ");
                Logger.logErrorMessage("Compiled class bytes length " + compiledClassBytes.length);
                Logger.logErrorMessage("Cloud data class bytes length " + contractBytes.length);
                Logger.logErrorMessage("Make sure the javac options used for verification are the same as the javac options used when compiling the blockchain contract");
                Logger.logErrorMessage("Make sure the Java version used for verification is the same as the Java version used when compiling the blockchain contract");
                return false;
            }
            Logger.logInfoMessage("Verification succeeded - class %s is identical", classFile);
        }
        return true;
    }

    private static URL getUrl() {
        if (url != null) {
            return url;
        }
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
        try {
            url = new URL(protocol, host, port, "/nxt");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        Logger.logInfoMessage("Connecting to URL " + url);
        return url;
    }

    private void waitForNextBlock() {
        String token = null;
        try {
            // Monitor the blockchain for a new block
            JO response = EventRegisterCall.create().event("Block.BLOCK_PUSHED").remote(getUrl()).call();
            Logger.logInfoMessage("Waiting for new block %s", response.toJSONString());
            if (!response.isExist("token")) {
                // Registration failed
                Logger.logInfoMessage("Something went wrong %s", response.toJSONString());
                return;
            }
            token = response.getString("token");
            JA events;
            // Wait for the next event. The while loop is not necessary but serves as a good practice in order not to
            // keep and Http request open for a long time.
            while (true) {
                // Wait up to 1 second for the event to occur
                response = EventWaitCall.create().timeout(1).token(token).remote(getUrl()).call();
                events = response.getArray("events");
                if (events.size() > 0) {
                    // If the event occurred stop waiting
                    break;
                }
                System.out.print(".");
            }
            // At this point the events array may include more than one event.
            System.out.println();
            events.objects().forEach(event -> Logger.logInfoMessage("" + event));
        } finally {
            if (token != null) {
                // Unregister the event listener
                JO response = EventRegisterCall.create().token(token).remove(true).remote(getUrl()).call();
                Logger.logInfoMessage("EventRegisterCall remove %s", response.toJSONString());
            }
        }
    }

    public static class ContractData {
        private final String contractName;
        private final String fullName;
        private final JO response;
        private final byte[] bytes;
        private final String mimeType;
        private final Contract contract;
        private final byte[] taggedDataHash;

        public ContractData(String contractName) {
            this.contractName = contractName;
            this.fullName = null;
            this.response = null;
            this.bytes = null;
            this.mimeType = null;
            this.contract = null;
            this.taggedDataHash = null;
        }

        public ContractData(String contractName, String fullName, JO response, byte[] bytes, String mimeType, Contract contract, byte[] taggedDataHash) {
            this.contractName = contractName;
            this.fullName = fullName;
            this.response = response;
            this.bytes = bytes;
            this.mimeType = mimeType;
            this.contract = contract;
            this.taggedDataHash = taggedDataHash;
        }

        public String getContractName() {
            return contractName;
        }

        public String getFullName() {
            return fullName;
        }

        public JO getResponse() {
            return response;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public String getMimeType() {
            return mimeType;
        }

        public Contract getContract() {
            return contract;
        }

        public byte[] getTaggedDataHash() {
            return taggedDataHash;
        }
    }
}
