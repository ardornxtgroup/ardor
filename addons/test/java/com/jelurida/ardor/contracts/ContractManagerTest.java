package com.jelurida.ardor.contracts;

import nxt.Nxt;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContractManagerTest extends AbstractContractTest {

    @BeforeClass
    public static void init() {
        Map<String, String> properties = new HashMap<>();
        properties.put("nxt.addOns", "nxt.addons.ContractRunner");
        properties.put("addon.contractRunner.configFile", "./test/resources/contracts.json");
        properties.put("nxt.testnetLeasingDelay", "2");

        properties.put("contract.manager.secretPhrase", aliceSecretPhrase);
        properties.put("contract.manager.feeNQT", "100000000");
        properties.put("contract.manager.serverAddress", "");
        initNxt(properties);
        initBlockchainTest();
    }

    @Test
    public void uploadClassFile() {
        ContractManager contractManager = new ContractManager();
        contractManager.init("RandomPayment");
        byte[] fullHash = contractManager.upload("RandomPayment");
        contractManager.reference("RandomPayment", fullHash);
        generateBlock(); // Contract class is confirmed
        List<TransactionResponse> transactionList = GetExecutedTransactionsCall.create(2).height(Nxt.getBlockchain().getHeight()).sender(ALICE.getId()).type(6).getTransactions();
        Assert.assertEquals(1, transactionList.size());
        Assert.assertArrayEquals(fullHash, transactionList.get(0).getFullHash());
        transactionList = GetExecutedTransactionsCall.create(2).height(Nxt.getBlockchain().getHeight()).sender(ALICE.getId()).type(12).getTransactions();
        Assert.assertEquals(1, transactionList.size());
        Assert.assertEquals(12, transactionList.get(0).getTransactionType().getType());
        Assert.assertEquals(0, transactionList.get(0).getTransactionType().getSubtype());
        Assert.assertEquals("RandomPayment", transactionList.get(0).getAttachmentJson().getString("contractName"));
        byte[] propertyFullHash = transactionList.get(0).getFullHash();

        // Update reference to the existing contract class - possibly changing parameters
        contractManager.reference("RandomPayment", fullHash);
        generateBlock(); // Account property pointing to the class is confirmed
        transactionList = GetExecutedTransactionsCall.create(2).height(Nxt.getBlockchain().getHeight()).sender(ALICE.getId()).getTransactions();
        Assert.assertEquals(1, transactionList.size());
        Assert.assertEquals(12, transactionList.get(0).getTransactionType().getType());
        Assert.assertEquals(0, transactionList.get(0).getTransactionType().getSubtype());
        Assert.assertEquals("RandomPayment", transactionList.get(0).getAttachmentJson().getString("contractName"));
        Assert.assertFalse(Arrays.equals(propertyFullHash, transactionList.get(0).getFullHash())); // This shows that the property was updated

        // Now add a separate reference to the existing contract class
        contractManager.reference("RandomPayment2", fullHash);
        generateBlock(); // Account property pointing to the class is confirmed
        transactionList = GetExecutedTransactionsCall.create(2).height(Nxt.getBlockchain().getHeight()).sender(ALICE.getId()).getTransactions();
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
    }

    @Test
    public void uploadJarFile() {
        ContractManager contractManager = new ContractManager();
        String contractName = "IgnisArdorRates";
        contractManager.init(contractName);
        byte[] fullHash = contractManager.upload(contractName);
        contractManager.reference(contractName, fullHash);
        generateBlock(); // Contract upload and reference are confirmed
        List<TransactionResponse> transactionList = GetExecutedTransactionsCall.create(2).height(Nxt.getBlockchain().getHeight()).sender(ALICE.getId()).type(6).getTransactions();
        Assert.assertArrayEquals(fullHash, transactionList.get(0).getFullHash());
        transactionList = GetExecutedTransactionsCall.create(2).height(Nxt.getBlockchain().getHeight()).sender(ALICE.getId()).type(12).getTransactions();
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
    }

    /**
     * Run this test using a Java JDK from the same version used by the IntelliJ compiler.
     * The test won't pass when using a Java JRE or when using a different version of JDK.
     * To run the test as part of the suite, comment out the @Ignore annotation
     */
    @Test
    @Ignore
    public void verifyClassFile() {
        ContractManager contractManager = new ContractManager();
        contractManager.init("ForgingReward");
        byte[] fullHash = contractManager.upload("ForgingReward");
        contractManager.reference("ForgingReward", fullHash);
        generateBlock(); // Contract class is confirmed
        boolean result = contractManager.verify(Convert.toHexString(fullHash), "addons/src/java/com/jelurida/ardor/contracts/ForgingReward.java");
        Assert.assertTrue(result);
    }
}
