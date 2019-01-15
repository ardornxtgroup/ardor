package nxt.addons;

import nxt.Nxt;
import nxt.blockchain.Chain;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.crypto.Crypto;
import nxt.lightcontracts.ContractReference;
import nxt.taggeddata.TaggedDataAttachment;
import nxt.taggeddata.TaggedDataHome;
import nxt.taggeddata.TaggedDataTransactionType;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.security.BlockchainCertPathParameters;
import nxt.util.security.BlockchainCertificate;
import nxt.util.security.BlockchainPublicKey;
import nxt.util.security.TransactionPrincipal;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.security.Security;
import java.security.SignatureException;
import java.security.Timestamp;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathParameters;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

public class ContractLoader {
    private static URL codeSourceUrl;

    static {
        try {
            codeSourceUrl = new URL("file://untrustedContractCode");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final NullContract NULL_CONTRACT = new NullContract();
    public static final String CLASS_FILE_MIME_TYPE = "application/java-vm";
    public static final String JAR_FILE_MIME_TYPE = "application/java-archive";

    static void loadContract(ContractReference contractReference, Map<String, ContractAndSetupParameters> supportedContracts, Map<String, ContractReference> supportedContractReferences) {
        ContractAndSetupParameters contractAndSetupParameters = loadContractAndSetupParameters(contractReference);
        if (contractAndSetupParameters.getContract() != NULL_CONTRACT) {
            supportedContracts.put(contractReference.getContractName(), contractAndSetupParameters);
            supportedContractReferences.put(contractReference.getContractName(), contractReference);
        }
    }

    public static ContractAndSetupParameters loadContractAndSetupParameters(ContractReference contractReference) {
        ChainTransactionId contractId = contractReference.getContractId();
        JO contractSetupParams = getContractSetupParams(contractReference);
        Contract contract = loadContract(contractId);
        return new ContractAndSetupParameters(contract, contractSetupParams);
    }

    private static Contract loadContract(ChainTransactionId transactionId) {
        Chain chain = transactionId.getChain();
        if (!(chain instanceof ChildChain)) {
            throw new IllegalArgumentException(String.format("Cannot load contract, chain %s is not a child chain", chain));
        }
        ChildChain childChain = (ChildChain) chain;
        TaggedDataHome taggedDataHome = childChain.getTaggedDataHome();
        byte[] fullHash = transactionId.getFullHash();
        TaggedDataHome.TaggedData taggedData = taggedDataHome.getData(fullHash);
        Transaction transaction = Nxt.getBlockchain().getTransaction(chain, fullHash);
        if (taggedData == null) {
            if (transaction != null && transaction.getType() != TaggedDataTransactionType.TAGGED_DATA_UPLOAD) {
                Logger.logInfoMessage(String.format("Cannot load contract, referenced transaction of type %s is not a tagged data transaction", transaction.getType()));
                return NULL_CONTRACT;
            }
            // Perhaps the tagged data transaction containing the contract code was already pruned, try to restore it
            try {
                transaction = Nxt.getBlockchainProcessor().restorePrunedTransaction(childChain, fullHash);
                if (transaction == null) {
                    Logger.logInfoMessage(String.format("Cannot load contract, contract %d:%s was pruned and cannot be restored", chain.getId(), Convert.toHexString(fullHash)));
                    return NULL_CONTRACT;
                }
                taggedData = taggedDataHome.getData(fullHash);
                if (taggedData == null) {
                    Logger.logInfoMessage(String.format("Cannot load contract, tagged data %d:%s restored but is still unavailable (should never happen)", chain.getId(), Convert.toHexString(fullHash)));
                    return NULL_CONTRACT;
                }
            } catch (Exception e) {
                Logger.logInfoMessage(String.format("Cannot load contract, contract %d:%s was never deployed or has been pruned, %s", chain.getId(), Convert.toHexString(fullHash), e.getMessage()));
                return NULL_CONTRACT;
            }
        }
        String name = taggedData.getName();
        if (name == null) {
            throw new IllegalArgumentException(String.format("Tagged data transaction does not specify contract class name, chain %s full hash %s", chain.getName(), Convert.toHexString(fullHash)));
        }
        byte[] data = taggedData.getData();
        if (data == null) {
            throw new IllegalArgumentException(String.format("Tagged data transaction does not store contract class data, chain %s full hash %s", chain.getName(), Convert.toHexString(fullHash)));
        }
        CodeSigner[] codeSigners = null;
        if (System.getSecurityManager() != null) {
            codeSigners = getCodeSigners(transaction);
        }

        // The principal listed in the policy file can be one of the following:
        // Transaction full hash, tagged data hash, hash of the data (which can be calculated using sha256sum command line utility)
        Principal[] principals = new Principal[3];
        principals[0] = new TransactionPrincipal(Convert.toHexString(transaction.getFullHash()));
        principals[1] = new TransactionPrincipal(Convert.toHexString(((TaggedDataAttachment) transaction.getAttachment()).getHash()));
        principals[2] = new TransactionPrincipal(Convert.toHexString(Crypto.sha256().digest(data)));
        switch (taggedData.getType()) {
            case CLASS_FILE_MIME_TYPE:
                return loadContract(name, data, codeSigners, principals);
            case JAR_FILE_MIME_TYPE:
                return loadContractFromJar(name, data, codeSigners, principals);
            default:
                throw new IllegalArgumentException(String.format("Tagged data mime type %s does not represent an executable contract, chain %s full hash %s", taggedData.getType(), chain.getName(), Convert.toHexString(fullHash)));
        }
    }

    private static CodeSigner[] getCodeSigners(Transaction transaction) {
        CodeSigner[] codeSigners = new CodeSigner[1];
        try {
            CertificateFactory blockchainCertificateFactory = CertificateFactory.getInstance("Blockchain", Security.getProvider("Jelurida"));
            Certificate certificate = blockchainCertificateFactory.generateCertificate(new ByteArrayInputStream(transaction.getBytes()));
            try {
                certificate.verify(new BlockchainPublicKey(transaction.getSenderPublicKey()));
            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
                throw new IllegalStateException(e); // Should never happen
            }
            CertPathBuilder certPathBuilder = CertPathBuilder.getInstance("Blockchain", "Jelurida");
            CertPathParameters certPathParameters = new BlockchainCertPathParameters((BlockchainCertificate) certificate);
            CertPathBuilderResult result = certPathBuilder.build(certPathParameters);
            Timestamp timestamp = new Timestamp(new Date(Convert.fromEpochTime(transaction.getTimestamp())), result.getCertPath());
            codeSigners[0] = new CodeSigner(result.getCertPath(), timestamp);
        } catch (CertificateException | NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | CertPathBuilderException e) {
            throw new IllegalArgumentException("Cannot create code signer", e);
        }
        return codeSigners;
    }

    private static Contract loadContract(String name, byte[] data, CodeSigner[] codeSigners, Principal[] principals) {
        return AccessController.doPrivileged((PrivilegedAction<Contract>) () -> {
            ClassLoader classLoader = new CloudDataClassLoader();
            return loadContract(classLoader, name, data, codeSigners, principals);
        });
    }

    private static Contract loadContractFromJar(String name, byte[] buffer, CodeSigner[] codeSigners, Principal[] principals) {
        return AccessController.doPrivileged((PrivilegedAction<Contract>) () -> {
            ClassLoader classLoader = new CloudDataClassLoader();
            return loadContractFromJar(classLoader, name, buffer, codeSigners, principals);
        });
    }

    public static Contract loadContract(ClassLoader classLoader, String name, byte[] data, CodeSigner[] codeSigners, Principal[] principals) {
        ProtectionDomain protectionDomain = new ProtectionDomain(new CodeSource(codeSourceUrl, codeSigners), null, classLoader, principals);
        Object instance;
        Class<?> contractClass;
        try {
            CloudDataClassLoader cloudDataClassLoader = (CloudDataClassLoader) classLoader;
            cloudDataClassLoader.setProtectionDomain(protectionDomain);
            cloudDataClassLoader.setClassBytes(data);
            contractClass = cloudDataClassLoader.findClass(name);
            Constructor<?> constructor = contractClass.getConstructor();
            instance = constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | NoClassDefFoundError e) {
            Logger.logErrorMessage(String.format("Error loading contract %s - %s", name, e.getMessage()), e);
            return NULL_CONTRACT;
        }
        if (!(instance instanceof Contract)) {
            throw new IllegalArgumentException("Class " + contractClass.getCanonicalName() + " not of type " + Contract.class.getCanonicalName());
        }
        String minVersion = ((Contract) instance).minProductVersion();
        if (compareVersions(minVersion, Nxt.VERSION) > 0) {
            throw new IllegalArgumentException(String.format("Class " + contractClass.getCanonicalName() + " minimum version %s higher than existing product version %s",
                    minVersion, Nxt.VERSION));
        }
        return (Contract) instance;
    }

    public static Contract loadContractFromJar(ClassLoader classLoader, String name, byte[] buffer, CodeSigner[] codeSigners, Principal[] principals) {
        return loadContractFromJar(classLoader, name, buffer, codeSigners, principals, null);
    }

    public static Contract loadContractFromJar(ClassLoader classLoader, String name, byte[] buffer, CodeSigner[] codeSigners, Principal[] principals, Map<String, byte[]> classFileData) {
        if (classFileData == null) {
            classFileData = new HashMap<>();
        }
        try {
            try (JarInputStream is = new JarInputStream(new ByteArrayInputStream(buffer))) {
                while (true) {
                    JarEntry nextEntry = is.getNextJarEntry();
                    if (nextEntry == null) {
                        break;
                    }
                    final int estimate = (int) nextEntry.getSize();
                    byte[] data = new byte[estimate > 0 ? estimate : 1024];
                    int real = 0;
                    for (int r = is.read(data); r > 0; r = is.read(data, real, data.length - real)) {
                        if (data.length == (real += r)) {
                            data = Arrays.copyOf(data, data.length * 2);
                        }
                    }
                    if (real != data.length) {
                        data = Arrays.copyOf(data, real);
                    }
                    // Translate file name to fully qualified class name (we change folder names to lower case package name since the Jar sometimes makes it upper case for no reason)
                    String[] tokens = nextEntry.getName().split("/");
                    String classFileName = tokens[tokens.length - 1];
                    if (!classFileName.endsWith(".class")) {
                        continue;
                    }
                    String packageName = Arrays.stream(tokens).filter(t -> !t.endsWith(".class")).map(String::toLowerCase).collect(Collectors.joining("."));
                    String className = packageName + "." + classFileName.split("[.]")[0];
                    classFileData.put(className, data);
                }
            }
            ProtectionDomain protectionDomain = new ProtectionDomain(new CodeSource(codeSourceUrl, codeSigners), null, classLoader, principals);
            Contract contract = null;
            for (String className : classFileData.keySet()) {
                CloudDataClassLoader cloudDataClassLoader = (CloudDataClassLoader) classLoader;
                cloudDataClassLoader.setProtectionDomain(protectionDomain);
                cloudDataClassLoader.setClassBytes(classFileData.get(className));
                Class contractClass = cloudDataClassLoader.findClass(className);
                if (!contractClass.getName().equals(name)) {
                    continue;
                }
                Constructor<?> constructor = contractClass.getConstructor();
                Object instance = constructor.newInstance();
                if (!(instance instanceof Contract)) {
                    continue;
                }
                contract = (Contract) instance;
                // Keep looping since we need to define all the classes in the Jar file
            }
            if (contract == null) {
                throw new IllegalStateException("Contract " + name + " not found in contract Jar file");
            }
            return contract;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static JO getContractSetupParams(ContractReference contractReference) {
        String contractParamsStr = contractReference.getContractParams();
        if (contractParamsStr != null && contractParamsStr.length() > 0) {
            return JO.parse(contractParamsStr);
        } else {
            return new JO();
        }
    }

    public static Class<?> getParametersProvider(Contract contract) {
        Class<?>[] classes = contract.getClass().getDeclaredClasses();
        return Arrays.stream(classes).filter(c -> c.getAnnotation(ContractParametersProvider.class) != null).findFirst().orElse(null);
    }

    public static class CloudDataClassLoader extends SecureClassLoader {

        byte[] classBytes;
        ProtectionDomain protectionDomain;

        public void setClassBytes(byte[] classBytes) {
            this.classBytes = classBytes;
        }

        public void setProtectionDomain(ProtectionDomain protectionDomain) {
            this.protectionDomain = protectionDomain;
        }

        @Override
        protected Class<?> findClass(String name) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                int i = name.lastIndexOf('.');
                if (i >= 0)
                    sm.checkPackageDefinition(name.substring(0, i));
            }
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass != null) {
                return loadedClass;
            } else {
                return defineClass(name, classBytes, 0, classBytes.length, protectionDomain);
            }
        }

        /**
         * Assign the permissions defined in the policy file to the protection domain based on the code source
         * @param codesource the code source for the contract class
         * @return the permissions granted for the contract
         */
        @Override
        protected PermissionCollection getPermissions(CodeSource codesource) {
            return Policy.getPolicy().getPermissions(codesource);
        }
    }

    private static class NullContract extends AbstractContract {
        @Override
        public JO processBlock(BlockContext context) {
            return null;
        }

        @Override
        public JO processTransaction(TransactionContext context) {
            return null;
        }

        @Override
        public JO processRequest(RequestContext context) {
            return null;
        }
    }

    public static int compareVersions(String v1, String v2) {
        Scanner s1 = new Scanner(v1);
        s1.useDelimiter("\\.");
        Scanner s2 = new Scanner(v2);
        s2.useDelimiter("\\.");

        while (s1.hasNext() && s2.hasNext()) {
            String t1 = s1.next();
            if (t1.endsWith("e")) {
                t1 = t1.substring(0, t1.length() - 1);
            }
            int n1 = Integer.parseInt(t1);

            String t2 = s2.next();
            if (t2.endsWith("e")) {
                t2 = t2.substring(0, t2.length() - 1);
            }
            int n2 = Integer.parseInt(t2);

            int compare = Integer.compare(n1, n2);
            if (compare != 0) {
                return compare;
            }
        }
        if (s1.hasNext()) {
            return 1;
        } else if (s2.hasNext()) {
            return -1;
        } else {
            return 0;
        }
    }
}