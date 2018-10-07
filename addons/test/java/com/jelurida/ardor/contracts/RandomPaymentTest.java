package com.jelurida.ardor.contracts;

import nxt.Nxt;
import nxt.addons.JA;
import nxt.addons.JO;
import nxt.blockchain.Block;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.FxtChain;
import nxt.blockchain.FxtTransaction;
import nxt.http.APICall;
import nxt.http.callers.GetBlockCall;
import nxt.http.callers.GetBlockchainStatusCall;
import nxt.http.callers.GetExecutedTransactionsCall;
import nxt.http.callers.GetFxtTransactionCall;
import nxt.http.callers.ScanCall;
import nxt.http.callers.TriggerContractByTransactionCall;
import nxt.http.responses.BlockResponse;
import nxt.http.responses.TransactionResponse;
import nxt.util.Convert;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static nxt.blockchain.ChildChain.IGNIS;

public class RandomPaymentTest extends AbstractContractTest {

    @Test
    public void randomPayment() {
        String contractName = RandomPayment.class.getSimpleName();
        ContractTestHelper.deployContract(contractName);

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        messageJson.put("seed", ContractTestHelper.getRandomSeed(System.identityHashCode(messageJson)));
        String message = messageJson.toJSONString();
        String triggerFullHash = ContractTestHelper.bobPaysContract(message, IGNIS);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract made random pay back
        Block lastBlock = Nxt.getBlockchain().getLastBlock();
        ChainTransactionId contractResultTransactionId = null;
        for (FxtTransaction transaction : lastBlock.getFxtTransactions()) {
            for (ChildTransaction childTransaction : transaction.getSortedChildTransactions()) {
                Assert.assertEquals(2, childTransaction.getChain().getId());
                Assert.assertEquals(0, childTransaction.getType().getType());
                Assert.assertEquals(0, childTransaction.getType().getSubtype());
                Assert.assertTrue(childTransaction.getAmount() >= 0 && childTransaction.getAmount() < 200 * IGNIS.ONE_COIN);
                Assert.assertEquals(4000000L, childTransaction.getFee());
                Assert.assertEquals(ALICE.getAccount().getId(), childTransaction.getSenderId());
                Assert.assertEquals(BOB.getAccount().getId(), childTransaction.getRecipientId());
                ChainTransactionId triggerTransactionId = new ChainTransactionId(IGNIS.getId(), Convert.parseHexString(triggerFullHash));
                Assert.assertEquals(triggerTransactionId, childTransaction.getReferencedTransactionId());
                contractResultTransactionId = new ChainTransactionId(childTransaction.getChain().getId(), childTransaction.getFullHash());
            }
        }
        Assert.assertNotNull(contractResultTransactionId);

        // Now let's rerun the operation of the contract
        APICall apiCall = new APICall.Builder("triggerContractByTransaction").
                param("chain", IGNIS.getId()).
                param("triggerFullHash", triggerFullHash).
                build();
        JO response = new JO(apiCall.invoke());
        JA transactions = new JA(response.get("transactions"));
        JO transaction = transactions.get(0);
        JO transactionJson = transaction.getJo("transactionJSON");
        long amountNQT = transactionJson.getLong("amountNQT");
        Assert.assertTrue(amountNQT >= 0 && amountNQT <= 200 * IGNIS.ONE_COIN);
        Assert.assertEquals(4000000, transactionJson.getLong("feeNQT"));

        // Now let's validate the operation of the contract
        apiCall = new APICall.Builder("triggerContractByTransaction").
                param("chain", contractResultTransactionId.getChainId()).
                param("triggerFullHash", Convert.toHexString(contractResultTransactionId.getFullHash())).
                param("apply", "true").
                param("validate", "true").
                build();
        response = new JO(apiCall.invoke());
        Assert.assertTrue(((String)response.get("errorDescription")).startsWith("Invalid phased transaction")); // This is fine since the contract is not under account control

        // Pay some Ardor to the contract
        ContractTestHelper.bobPaysContract(message, FxtChain.FXT);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract made random pay back
        lastBlock = Nxt.getBlockchain().getLastBlock();
        boolean isFound = false;
        for (FxtTransaction parentTransaction : lastBlock.getFxtTransactions()) {
            isFound = true;
            Assert.assertEquals(1, parentTransaction.getChain().getId());
            Assert.assertEquals(-2, parentTransaction.getType().getType());
            Assert.assertEquals(0, parentTransaction.getType().getSubtype());
            Assert.assertTrue(parentTransaction.getAmount() >= 0 && parentTransaction.getAmount() < 200 * IGNIS.ONE_COIN);
            Assert.assertEquals(FxtChain.FXT.ONE_COIN, parentTransaction.getFee());
            Assert.assertEquals(ALICE.getAccount().getId(), parentTransaction.getSenderId());
            Assert.assertEquals(BOB.getAccount().getId(), parentTransaction.getRecipientId());
        }
        Assert.assertTrue(isFound);
    }

    @Test
    public void sameRandomPaymentUsingCallObjects() {
        String contractName = RandomPayment.class.getSimpleName();
        ContractTestHelper.deployContract(contractName);

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        String message = messageJson.toJSONString();
        String triggerFullHash = ContractTestHelper.bobPaysContract(message, IGNIS);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract made random pay back
        JO getBlockchainStatusCall = GetBlockchainStatusCall.create().call();
        BlockResponse lastBlock = GetBlockCall.create().height(getBlockchainStatusCall.getInt("numberOfBlocks") - 1).getBlock();
        ChainTransactionId contractResultTransactionId = null;
        for (byte[] fullHash : lastBlock.getParentTransactionFullHashes()) {
            List<TransactionResponse> childTransactions = GetFxtTransactionCall.create().fullHash(fullHash).includeChildTransactions(true).getTransactions("childTransactions");
            for (TransactionResponse childTransaction : childTransactions) {
                Assert.assertEquals(2, childTransaction.getChainId());
                Assert.assertEquals(0, childTransaction.getTransactionType().getType());
                Assert.assertEquals(0, childTransaction.getTransactionType().getSubtype());
                Assert.assertTrue(childTransaction.getAmount() >= 0 && childTransaction.getAmount() < 200 * IGNIS.ONE_COIN);
                Assert.assertEquals(4000000L, childTransaction.getFee());
                Assert.assertEquals(ALICE.getAccount().getId(), childTransaction.getSenderId());
                Assert.assertEquals(BOB.getAccount().getId(), childTransaction.getRecipientId());
                ChainTransactionId triggerTransactionId = new ChainTransactionId(IGNIS.getId(), Convert.parseHexString(triggerFullHash));
                Assert.assertEquals(triggerTransactionId, childTransaction.getReferencedTransaction());
                contractResultTransactionId = new ChainTransactionId(childTransaction.getChainId(), childTransaction.getFullHash());
            }
        }
        Assert.assertNotNull(contractResultTransactionId);

        // Now let's do it again using the GetExecutedTransactions API
        contractResultTransactionId = null;
        List<TransactionResponse> childTransactions = GetExecutedTransactionsCall.create(IGNIS.getId()).type(0).subtype(0).height(getBlockchainStatusCall.getInt("numberOfBlocks") - 1).getTransactions();
        for (TransactionResponse childTransaction : childTransactions) {
            Assert.assertEquals(2, childTransaction.getChainId());
            Assert.assertEquals(0, childTransaction.getTransactionType().getType());
            Assert.assertEquals(0, childTransaction.getTransactionType().getSubtype());
            Assert.assertTrue(childTransaction.getAmount() >= 0 && childTransaction.getAmount() < 200 * IGNIS.ONE_COIN);
            Assert.assertEquals(4000000L, childTransaction.getFee());
            Assert.assertEquals(ALICE.getAccount().getId(), childTransaction.getSenderId());
            Assert.assertEquals(BOB.getAccount().getId(), childTransaction.getRecipientId());
            ChainTransactionId triggerTransactionId = new ChainTransactionId(IGNIS.getId(), Convert.parseHexString(triggerFullHash));
            Assert.assertEquals(triggerTransactionId, childTransaction.getReferencedTransaction());
            contractResultTransactionId = new ChainTransactionId(childTransaction.getChainId(), childTransaction.getFullHash());
        }
        Assert.assertNotNull(contractResultTransactionId);

        // Now let's rerun the operation of the contract
        List<TransactionResponse> transactions = TriggerContractByTransactionCall.create(IGNIS.getId()).triggerFullHash(triggerFullHash).getCreatedTransactions();
        TransactionResponse transaction = transactions.get(0);
        Assert.assertTrue(transaction.getAmount() >= 0 && transaction.getAmount() < 200 * IGNIS.ONE_COIN);
        Assert.assertEquals(4000000L, transaction.getFee());

        // Now let's validate the operation of the contract
        boolean isExceptionThrown = true;
        try {
            TriggerContractByTransactionCall.create(contractResultTransactionId.getChainId()).
                    triggerFullHash(Convert.toHexString(contractResultTransactionId.getFullHash())).apply("true").validate("true").getCreatedTransactions();
            isExceptionThrown = false;
        } catch (IllegalStateException e) {
            JO response = JO.parse(e.getMessage());
            Assert.assertTrue((response.getString("errorDescription")).startsWith("Invalid phased transaction")); // This is fine since the contract is not under account control
        }
        Assert.assertTrue(isExceptionThrown);

        // Pay some Ardor to the contract
        ContractTestHelper.bobPaysContract(message, FxtChain.FXT);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract made random pay back
        getBlockchainStatusCall = GetBlockchainStatusCall.create().call();
        lastBlock = GetBlockCall.create().height(getBlockchainStatusCall.getInt("numberOfBlocks") - 1).includeTransactions(true).getBlock();
        boolean isFound = false;
        for (TransactionResponse parentTransaction : lastBlock.getParentTransactions()) {
            isFound = true;
            Assert.assertEquals(1, parentTransaction.getChainId());
            Assert.assertEquals(-2, parentTransaction.getTransactionType().getType());
            Assert.assertEquals(0, parentTransaction.getTransactionType().getSubtype());
            Assert.assertTrue(parentTransaction.getAmount() >= 0 && parentTransaction.getAmount() < 200 * IGNIS.ONE_COIN);
            Assert.assertEquals(FxtChain.FXT.ONE_COIN, parentTransaction.getFee());
            Assert.assertEquals(ALICE.getAccount().getId(), parentTransaction.getSenderId());
            Assert.assertEquals(BOB.getAccount().getId(), parentTransaction.getRecipientId());
        }
        Assert.assertTrue(isFound);
    }

    @Test
    public void randomPaymentWithScan() {
        String contractName = RandomPayment.class.getSimpleName();
        ContractTestHelper.deployContract(contractName);

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        String message = messageJson.toJSONString();
        String triggerFullHash = ContractTestHelper.bobPaysContract(message, IGNIS);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract made random pay back
        JO getBlockchainStatusCall = GetBlockchainStatusCall.create().call();
        BlockResponse lastBlock = GetBlockCall.create().height(getBlockchainStatusCall.getInt("numberOfBlocks") - 1).getBlock();
        ChainTransactionId contractResultTransactionId = null;
        for (byte[] fullHash : lastBlock.getParentTransactionFullHashes()) {
            List<TransactionResponse> childTransactions = GetFxtTransactionCall.create().fullHash(fullHash).includeChildTransactions(true).getTransactions("childTransactions");
            Assert.assertEquals(1, childTransactions.size());
            TransactionResponse childTransaction = childTransactions.get(0);
            Assert.assertEquals(2, childTransaction.getChainId());
            Assert.assertEquals(0, childTransaction.getTransactionType().getType());
            Assert.assertEquals(0, childTransaction.getTransactionType().getSubtype());
            Assert.assertTrue(childTransaction.getAmount() >= 0 && childTransaction.getAmount() < 200 * IGNIS.ONE_COIN);
            Assert.assertEquals(4000000L, childTransaction.getFee());
            Assert.assertEquals(ALICE.getAccount().getId(), childTransaction.getSenderId());
            Assert.assertEquals(BOB.getAccount().getId(), childTransaction.getRecipientId());
            ChainTransactionId triggerTransactionId = new ChainTransactionId(IGNIS.getId(), Convert.parseHexString(triggerFullHash));
            Assert.assertEquals(triggerTransactionId, childTransaction.getReferencedTransaction());
            contractResultTransactionId = new ChainTransactionId(childTransaction.getChainId(), childTransaction.getFullHash());
        }
        Assert.assertNotNull(contractResultTransactionId);

        // The blockchain rescan will cause the contract to resubmit the transaction but it will become duplicate of the existing transaction
        // and will not be broadcast to the blockchain again.
        ScanCall.create().height(1).call();

        // After the scan we should see the same transaction as before the scan
        ChainTransactionId afterScanContractResultTransactionId = null;
        for (byte[] fullHash : lastBlock.getParentTransactionFullHashes()) {
            List<TransactionResponse> childTransactions = GetFxtTransactionCall.create().fullHash(fullHash).includeChildTransactions(true).getTransactions("childTransactions");
            Assert.assertEquals(1, childTransactions.size());
            TransactionResponse childTransaction = childTransactions.get(0);
            Assert.assertEquals(2, childTransaction.getChainId());
            Assert.assertEquals(0, childTransaction.getTransactionType().getType());
            Assert.assertEquals(0, childTransaction.getTransactionType().getSubtype());
            Assert.assertTrue(childTransaction.getAmount() >= 0 && childTransaction.getAmount() < 200 * IGNIS.ONE_COIN);
            Assert.assertEquals(4000000L, childTransaction.getFee());
            Assert.assertEquals(ALICE.getAccount().getId(), childTransaction.getSenderId());
            Assert.assertEquals(BOB.getAccount().getId(), childTransaction.getRecipientId());
            ChainTransactionId triggerTransactionId = new ChainTransactionId(IGNIS.getId(), Convert.parseHexString(triggerFullHash));
            Assert.assertEquals(triggerTransactionId, childTransaction.getReferencedTransaction());
            afterScanContractResultTransactionId = new ChainTransactionId(childTransaction.getChainId(), childTransaction.getFullHash());
        }
        Assert.assertEquals(contractResultTransactionId, afterScanContractResultTransactionId);
    }
}
