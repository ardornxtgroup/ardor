package com.jelurida.ardor.contracts;

import nxt.BlockchainTest;
import nxt.addons.AddOns;
import nxt.addons.ContractRunner;
import nxt.addons.JO;
import nxt.http.callers.GetExecutedTransactionsCall;
import nxt.http.responses.TransactionResponse;
import nxt.tools.ContractManager;
import nxt.util.Convert;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContractManagerTest extends AbstractContractTest {

    @BeforeClass
    public static void init() {
        Map<String, String> properties = new HashMap<>();
        properties.put("nxt.addOns", "nxt.addons.ContractRunner");
        properties.put("addon.contractRunner.secretPhrase", BlockchainTest.aliceSecretPhrase);
        properties.put("addon.contractRunner.feeRateNQTPerFXT.IGNIS", "200000000");
        properties.put("nxt.testnetLeasingDelay", "2");
        properties.put("contract.manager.secretPhrase", aliceSecretPhrase);
        properties.put("contract.manager.feeNQT", "100000000");
        properties.put("contract.manager.serverAddress", "");
        properties.put("nxt.disableSecurityPolicy", "true");
        initNxt(properties);
        initBlockchainTest();
    }

    @Test
    public void uploadClassFile() {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            ContractManager contractManager = new ContractManager();
            contractManager.init("RandomPayment");
            ContractManager.ContractData contractData = contractManager.upload(RandomPayment.class.getSimpleName(), RandomPayment.class.getPackage().getName());
            byte[] fullHash = contractData.getResponse().parseHexString("fullHash");
            contractManager.reference(contractData, fullHash);
            generateBlock(); // Contract class is confirmed
            List<TransactionResponse> transactionList = GetExecutedTransactionsCall.create(2).height(getHeight()).sender(ALICE.getId()).type(6).getTransactions();
            Assert.assertEquals(1, transactionList.size());
            Assert.assertArrayEquals(fullHash, transactionList.get(0).getFullHash());
            transactionList = GetExecutedTransactionsCall.create(2).height(getHeight()).sender(ALICE.getId()).type(12).getTransactions();
            Assert.assertEquals(1, transactionList.size());
            Assert.assertEquals(12, transactionList.get(0).getTransactionType().getType());
            Assert.assertEquals(0, transactionList.get(0).getTransactionType().getSubtype());
            Assert.assertEquals("RandomPayment", transactionList.get(0).getAttachmentJson().getString("contractName"));
            byte[] propertyFullHash = transactionList.get(0).getFullHash();

            // Update reference to the existing contract class - possibly changing parameters
            contractData = new ContractManager.ContractData("RandomPayment");
            contractManager.reference(contractData, fullHash);
            generateBlock(); // Account property pointing to the class is confirmed
            transactionList = GetExecutedTransactionsCall.create(2).height(getHeight()).sender(ALICE.getId()).getTransactions();
            Assert.assertEquals(1, transactionList.size());
            Assert.assertEquals(12, transactionList.get(0).getTransactionType().getType());
            Assert.assertEquals(0, transactionList.get(0).getTransactionType().getSubtype());
            Assert.assertEquals("RandomPayment", transactionList.get(0).getAttachmentJson().getString("contractName"));
            Assert.assertFalse(Arrays.equals(propertyFullHash, transactionList.get(0).getFullHash())); // This shows that the property was updated

            // Now add a separate reference to the existing contract class
            contractData = new ContractManager.ContractData("RandomPayment2");
            contractManager.reference(contractData, fullHash);
            generateBlock(); // Account property pointing to the class is confirmed
            transactionList = GetExecutedTransactionsCall.create(2).height(getHeight()).sender(ALICE.getId()).getTransactions();
            Assert.assertEquals(1, transactionList.size());
            Assert.assertEquals(12, transactionList.get(0).getTransactionType().getType());
            Assert.assertEquals(0, transactionList.get(0).getTransactionType().getSubtype());
            Assert.assertEquals("RandomPayment2", transactionList.get(0).getAttachmentJson().getString("contractName"));

            // List the contracts
            JO response = contractManager.listImpl(ALICE.getRsAccount(), null);
            Assert.assertEquals(2, response.getJoList("contractReferences").size());

            // Delete a reference
            contractManager.delete("RandomPayment");
            generateBlock();

            // List the contracts again
            response = contractManager.listImpl(ALICE.getRsAccount(), null);
            Assert.assertEquals(1, response.getJoList("contractReferences").size());
            return null;
        });
    }

    @Test
    public void uploadJarFile() {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            ContractManager contractManager = new ContractManager();
            String contractName = IgnisArdorRates.class.getSimpleName();
            String packageName = IgnisArdorRates.class.getPackage().getName();
            contractManager.init(contractName);
            ContractManager.ContractData contractData = contractManager.upload(contractName, packageName);
            byte[] fullHash = contractData.getResponse().parseHexString("fullHash");
            contractManager.reference(contractData, fullHash);
            generateBlock(); // Contract upload and reference are confirmed
            List<TransactionResponse> transactionList = GetExecutedTransactionsCall.create(2).height(getHeight()).sender(ALICE.getId()).type(6).getTransactions();
            Assert.assertArrayEquals(fullHash, transactionList.get(0).getFullHash());
            transactionList = GetExecutedTransactionsCall.create(2).height(getHeight()).sender(ALICE.getId()).type(12).getTransactions();
            Assert.assertEquals(12, transactionList.get(0).getTransactionType().getType());
            Assert.assertEquals(0, transactionList.get(0).getTransactionType().getSubtype());
            Assert.assertEquals(contractName, transactionList.get(0).getAttachmentJson().getString("contractName"));
            // Because the contract frequency is 3 need to generate 3 blocks now
            generateBlock();
            generateBlock();
            generateBlock();
            new IgnisArdorRatesTest().executeContract(contractName);
            ContractRunner contractRunner = (ContractRunner) AddOns.getAddOn(ContractRunner.class);
            contractRunner.reset();
            return null;
        });
    }

    /**
     * Run this test using a Java JDK from the same version used by the IntelliJ compiler.
     * The test won't pass when using a Java JRE or when using a different version of JDK.
     * This test won't run from a suite due to permission problems. Always run it using this test class.
     * Comment out the @Ignore annotation to run.
     */
    @Test
    @Ignore
    public void verifyClassFile() {
            ContractManager contractManager = new ContractManager();
            String contractName = ForgingReward.class.getSimpleName();
            contractManager.init(contractName);
            ContractManager.ContractData contractData = contractManager.upload(contractName, ForgingReward.class.getPackage().getName());
            byte[] fullHash = contractData.getResponse().parseHexString("fullHash");
            contractManager.reference(contractData, fullHash);
            generateBlock(); // Contract class is confirmed
            boolean result = contractManager.verify(Convert.toHexString(fullHash), "addons/src/java/com/jelurida/ardor/contracts/ForgingReward.java");
            Assert.assertTrue(result);
    }

    /**
     * Run this test using a Java JDK from the same version used by the IntelliJ compiler.
     * The test won't pass when using a Java JRE or when using a different version of JDK.
     * This test won't run from a suite due to permission problems. Always run it using this test class.
     * Comment out the @Ignore annotation to run
     */
    @Test
    @Ignore
    public void verifySingleSourceJarFile() {
            ContractManager contractManager = new ContractManager();
            String contractName = AllowedActions.class.getSimpleName(); // Need single source contract compiled into multiple class files like this one
            contractManager.init(contractName);
            ContractManager.ContractData contractData = contractManager.upload(contractName, ForgingReward.class.getPackage().getName());
            byte[] fullHash = contractData.getResponse().parseHexString("fullHash");
            contractManager.reference(contractData, fullHash);
            generateBlock(); // Contract class is confirmed
            boolean result = contractManager.verify(Convert.toHexString(fullHash), "addons/src/java/com/jelurida/ardor/contracts/AllowedActions.java");
            Assert.assertTrue(result);
    }

    @Test
    public void loadInterfaceByReflection() {
        Class<?>[] interfaces = ForgingReward.class.getDeclaredClasses();
        Assert.assertEquals(1, interfaces.length);
    }
}
