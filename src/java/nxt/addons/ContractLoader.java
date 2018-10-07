package nxt.addons;

import nxt.Nxt;
import nxt.blockchain.Chain;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.lightcontracts.ContractReference;
import nxt.taggeddata.TaggedDataHome;
import nxt.taggeddata.TaggedDataTransactionType;
import nxt.util.Convert;
import nxt.util.Logger;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

public class ContractLoader {

    private static final NullContract NULL_CONTRACT = new NullContract();
    public static final String CLASS_FILE_MIME_TYPE = "application/java-vm";
    public static final String JAR_FILE_MIME_TYPE = "application/java-archive";

    static void loadContract(ContractReference contractReference, Map<String, Contract> supportedContracts, Map<String, ContractReference> supportedContractTransactions) {
        ChainTransactionId contractId = contractReference.getContractId();
        JO contractSetupParams = getContractSetupParams(contractReference);
        String contractName = contractReference.getContractName();
        Contract contract = loadContract(contractId, contractSetupParams);
        if (contract != NULL_CONTRACT) {
            supportedContracts.put(contractName, contract);
            supportedContractTransactions.put(contractName, contractReference);
        }
    }

    static Contract loadContract(ChainTransactionId transactionId, JO setupParams) {
        Chain chain = transactionId.getChain();
        if (!(chain instanceof ChildChain)) {
            throw new IllegalArgumentException(String.format("Cannot load contract, chain %s is not a child chain", chain));
        }
        ChildChain childChain = (ChildChain) chain;
        TaggedDataHome taggedDataHome = childChain.getTaggedDataHome();
        byte[] fullHash = transactionId.getFullHash();
        TaggedDataHome.TaggedData taggedData = taggedDataHome.getData(fullHash);
        if (taggedData == null) {
            Transaction transaction = Nxt.getBlockchain().getTransaction(chain, fullHash);
            if (transaction != null && transaction.getType() != TaggedDataTransactionType.TAGGED_DATA_UPLOAD) {
                Logger.logInfoMessage(String.format("Cannot load contract, referenced transaction of type %s is not a tagged data transaction", transaction.getType()));
                return NULL_CONTRACT;
            }
            // Perhaps the tagged data transaction containing the contract code was already pruned, try to restore it
            try {
                if (Nxt.getBlockchainProcessor().restorePrunedTransaction(childChain, fullHash) == null) {
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
        switch (taggedData.getType()) {
            case CLASS_FILE_MIME_TYPE:
                return loadContract(name, data, setupParams);
            case JAR_FILE_MIME_TYPE:
                return loadContractFromJar(name, data, setupParams);
            default:
                throw new IllegalArgumentException(String.format("Tagged data mime type %s does not represent an executable contract, chain %s full hash %s", taggedData.getType(), chain.getName(), Convert.toHexString(fullHash)));
        }
    }

    public static Contract loadContract(String name, byte[] data, JO setupParams) {
        Contract contract = loadContract(name, data);
        contract.setContractParams(setupParams);
        return contract;
    }

    public static Contract loadContract(String name, byte[] data) {
        CloudDataClassLoader classLoader = new CloudDataClassLoader(Thread.currentThread().getContextClassLoader());
        Object instance;
        Class<?> contractClass;
        try {
            contractClass = classLoader.defineClass(name, data);
            Constructor<?> constructor = contractClass.getConstructor();
            instance = constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | NoClassDefFoundError e) {
            Logger.logErrorMessage(String.format("Error loading contract %s - %s", name, e.getMessage()), e);
            return NULL_CONTRACT;
        }
        if (!(instance instanceof Contract)) {
            throw new IllegalArgumentException("Class " + contractClass.getCanonicalName() + " not of type " + Contract.class.getCanonicalName());
        }
        String minVersion = ((Contract)instance).minProductVersion();
        if (compareVersions(minVersion, Nxt.VERSION) > 0) {
            throw new IllegalArgumentException(String.format("Class " + contractClass.getCanonicalName() + " minimum version %s higher than existing product version %s",
                    minVersion, Nxt.VERSION));
        }
        return (Contract)instance;
    }

    private static Contract loadContractFromJar(String name, byte[] buffer, JO setupParams) {
        Contract contract = loadContractFromJar(name, buffer);
        contract.setContractParams(setupParams);
        return contract;
    }

    public static Contract loadContractFromJar(String name, byte[] buffer) {
        final Map<String, byte[]> classFileData = new HashMap<>();
        try {
            try (JarInputStream is = new JarInputStream(new ByteArrayInputStream(buffer))) {
                for (;;) {
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
            CloudDataClassLoader classLoader = new CloudDataClassLoader(Thread.currentThread().getContextClassLoader());
            Contract contract = null;
            for (String className : classFileData.keySet()) {
                Class contractClass = classLoader.defineClass(className, classFileData.get(className));
                if (!contractClass.getName().equals(name)) {
                    continue;
                }
                Constructor<?> constructor = contractClass.getConstructor();
                Object instance = constructor.newInstance();
                if (!(instance instanceof Contract)) {
                    continue;
                }
                contract = (Contract)instance;
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
        if (contractParamsStr.length() > 0) {
            return JO.parse(contractParamsStr);
        } else {
            return new JO();
        }
    }

    private static class CloudDataClassLoader extends ClassLoader {

        private CloudDataClassLoader(ClassLoader deferTo) {
            super(deferTo);
        }

        private Class<?> defineClass(String fullClassName, byte[] bytes) {
            return defineClass(fullClassName, bytes, 0, bytes.length);
        }
    }

    private static class NullContract extends AbstractContract {
        @Override
        public void processBlock(BlockContext context) {}

        @Override
        public void processTransaction(TransactionContext context) {}

        @Override
        public void processRequest(RequestContext context) {}
    }

    public static int compareVersions(String v1, String v2) {
        Scanner s1 = new Scanner(v1);
        s1.useDelimiter("\\.");
        Scanner s2 = new Scanner(v2);
        s2.useDelimiter("\\.");

        while(s1.hasNext() && s2.hasNext()) {
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