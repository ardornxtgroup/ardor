package com.jelurida.ardor.contracts;

import nxt.addons.JA;
import nxt.addons.JO;
import nxt.blockchain.Block;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.FxtChain;
import nxt.blockchain.FxtTransaction;
import nxt.http.APICall;
import nxt.http.callers.ApproveTransactionCall;
import nxt.http.callers.GetBlockchainStatusCall;
import nxt.http.callers.GetExecutedTransactionsCall;
import nxt.http.callers.ScanCall;
import nxt.http.callers.SendMessageCall;
import nxt.http.callers.TriggerContractByTransactionCall;
import nxt.http.responses.TransactionResponse;
import nxt.util.Convert;
import nxt.util.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.function.LongPredicate;

import static nxt.blockchain.ChildChain.AEUR;
import static nxt.blockchain.ChildChain.IGNIS;

public class RandomPaymentTest extends AbstractContractTest {

    public static final String TOP_SECRET = "TopSecret";

    @Test
    public void randomPayment() {
        String contractName = ContractTestHelper.deployContract(RandomPayment.class);

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        messageJson.put("seed", ContractTestHelper.getRandomSeed(System.identityHashCode(messageJson)));
        String message = messageJson.toJSONString();
        String triggerFullHash = ContractTestHelper.bobPaysContract(message, IGNIS);
        // Contract should submit transaction now
        generateBlock();
        LongPredicate predicate = amount -> (amount >= 0 && amount < 200 * IGNIS.ONE_COIN);
        ChildTransaction childTransaction = testAndGetLastChildTransaction(2, 0, 0, predicate, 4000000L, ALICE, BOB, triggerFullHash);

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
                param("chain", childTransaction.getChain().getId()).
                param("triggerFullHash", Convert.toHexString(childTransaction.getFullHash())).
                param("apply", "true").
                param("validate", "true").
                build();
        response = new JO(apiCall.invoke());
        Assert.assertTrue(((String) response.get("errorDescription")).startsWith("Cannot approve transaction, validatorSecretPhrase not specified"));

        // Pay some Ardor to the contract
        ContractTestHelper.bobPaysContract(message, FxtChain.FXT);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract made random pay back
        testAndGetLastParentTransaction(1, -2, 0, a -> a >=0 && a < 200 * IGNIS.ONE_COIN, FxtChain.FXT.ONE_COIN, ALICE, BOB);
    }

    @Test
    public void sameRandomPaymentUsingCallObjects() {
        String contractName = ContractTestHelper.deployContract(RandomPayment.class);

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        String message = messageJson.toJSONString();
        String triggerFullHash = ContractTestHelper.bobPaysContract(message, IGNIS);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract made random pay back
        testAndGetLastChildTransaction(2, 0, 0, a -> a >= 0 && a < 200 * IGNIS.ONE_COIN, 4000000L, ALICE, BOB, triggerFullHash);

        // Now let's do it again using the GetExecutedTransactions API for the sake of example
        ChainTransactionId contractResultTransactionId = null;
        JO getBlockchainStatusCall = GetBlockchainStatusCall.create().call();
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
            Assert.assertTrue((response.getString("errorDescription")).startsWith("Cannot approve transaction, validatorSecretPhrase not specified"));
        }
        Assert.assertTrue(isExceptionThrown);

        // Pay some Ardor to the contract
        ContractTestHelper.bobPaysContract(message, FxtChain.FXT);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract made random pay back
        FxtTransaction fxtTransaction = testAndGetLastParentTransaction(1, -2, 0,
                a -> a >= 0 && a < 200 * IGNIS.ONE_COIN, FxtChain.FXT.ONE_COIN,
                ALICE, BOB);

        // Now let's validate the operation of the contract
        isExceptionThrown = true;
        try {
            TriggerContractByTransactionCall.create(fxtTransaction.getChain().getId()).
                    triggerFullHash(Convert.toHexString(fxtTransaction.getFullHash())).apply("true").validate("true").getCreatedTransactions();
            isExceptionThrown = false;
        } catch (IllegalStateException e) {
            JO response = JO.parse(e.getMessage());
            Assert.assertEquals("Found a match but cannot submit approval to a parent chain transaction", response.getString("errorDescription")); // This is fine since phasing is not supported on the parent chain
        }
        Assert.assertTrue(isExceptionThrown);
    }

    @Test
    public void randomPaymentWithScan() {
        String contractName = ContractTestHelper.deployContract(RandomPayment.class);

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        String message = messageJson.toJSONString();
        String triggerFullHash = ContractTestHelper.bobPaysContract(message, IGNIS);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract made random pay back
        testAndGetLastChildTransaction(2, 0, 0, a -> a >= 0 && a < 200 * IGNIS.ONE_COIN, 4000000L, ALICE, BOB, triggerFullHash);

        // The blockchain rescan will cause the contract to resubmit the transaction but it will become duplicate of the existing transaction
        // and will not be broadcast to the blockchain again.
        ScanCall.create().height(1).call();

        // After the scan we should see the same transaction as before the scan
        testAndGetLastChildTransaction(2, 0, 0, a -> a >= 0 && a < 200 * IGNIS.ONE_COIN, 4000000L, ALICE, BOB, triggerFullHash);
    }

    /**
     * Bob tries to trick Alice into sending him payment she did not receive by sending the funds to himself with a
     * contract trigger message.
     */
    @Test
    public void sendWrongRecipient() {
        String contractName = ContractTestHelper.deployContract(RandomPayment.class);

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        messageJson.put("seed", ContractTestHelper.getRandomSeed(System.identityHashCode(messageJson)));
        String message = messageJson.toJSONString();
        ContractTestHelper.payContract(message, IGNIS, false, BOB.getSecretPhrase(), BOB.getRsAccount());
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract did not make a payment, we simply check that no transactions were included in the last block
        Block lastBlock = getLastBlock();
        Assert.assertEquals(0, lastBlock.getFxtTransactions().size());
    }

    /**
     * Bob tries to trick Alice into sending him payment she received on another chain by sending the funds to another chain.
     */
    @Test
    public void sendWrongChain() {
        String contractName = ContractTestHelper.deployContract(RandomPayment.class);

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        messageJson.put("seed", ContractTestHelper.getRandomSeed(System.identityHashCode(messageJson)));
        String message = messageJson.toJSONString();
        ContractTestHelper.bobPaysContract(message, AEUR, false);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract did not make a payment, we simply check that no transactions were included in the last block
        Block lastBlock = getLastBlock();
        Assert.assertEquals(0, lastBlock.getFxtTransactions().size());
    }

    /**
     * Bob tries to trick Alice into sending him payment by sending her a non-payment transaction.
     */
    @Test
    public void sendWrongTransactionType() {
        String contractName = ContractTestHelper.deployContract(RandomPayment.class);

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        String message = messageJson.toJSONString();
        JO response = SendMessageCall.create(2).messageIsPrunable(true).message(message).recipient(ALICE.getId()).secretPhrase(BOB.getSecretPhrase()).feeNQT(IGNIS.ONE_COIN).call();
        Logger.logInfoMessage(response.toJSONString());
        generateBlock();

        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract did not make a payment, we simply check that no transactions were included in the last block
        Block lastBlock = getLastBlock();
        Assert.assertEquals(0, lastBlock.getFxtTransactions().size());
    }

    @Test
    public void revealHashedSecret() {
        String contractName = ContractTestHelper.deployContract(RandomPayment.class);

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        messageJson.put("seed", ContractTestHelper.getRandomSeed(System.identityHashCode(messageJson)));
        String message = messageJson.toJSONString();
        String triggerFullHash = ContractTestHelper.payContract(message, IGNIS, true, BOB.getSecretPhrase(), ALICE.getRsAccount(), true);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract made random pay back
        LongPredicate predicate = amount -> (amount == 100 * IGNIS.ONE_COIN - 8000000L);
        ChildTransaction childTransaction = testAndGetLastChildTransaction(2, 0, 0, predicate, 8000000L, ALICE, BOB, triggerFullHash);
        Assert.assertNotNull(childTransaction);
        long amountNQT = childTransaction.getAmount();
        long aliceBalanceDiff = ALICE.getChainBalanceDiff(2);
        long bobBalanceDiff = BOB.getChainBalanceDiff(2);

        // Bob after validating the contract payment made by Alice now reveals the hashed secret and approves both his payment
        // and Alice response payment
        int approvalFeeNQT = 200000;
        JO response = ApproveTransactionCall.create(2).
                phasedTransaction("2:" + triggerFullHash, "2:" + Convert.toHexString(childTransaction.getFullHash())).
                revealedSecret(TOP_SECRET).revealedSecretIsText(true).
                feeNQT(approvalFeeNQT).
                secretPhrase(CHUCK.getSecretPhrase()).call();
        Logger.logInfoMessage(response.toJSONString());
        generateBlock();
        Assert.assertEquals(-amountNQT + 100 * IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(2) - aliceBalanceDiff);
        Assert.assertEquals(amountNQT - 100 * IGNIS.ONE_COIN, BOB.getChainBalanceDiff(2) - bobBalanceDiff);
    }

    @Test
    public void revealHashedSecretOnlyToContract() {
        String contractName = ContractTestHelper.deployContract(RandomPayment.class);

        // Pay the contract and attach a message to trigger the contract execution
        JO messageJson = new JO();
        messageJson.put("contract", contractName);
        messageJson.put("seed", ContractTestHelper.getRandomSeed(System.identityHashCode(messageJson)));
        String message = messageJson.toJSONString();
        String triggerFullHash = ContractTestHelper.payContract(message, IGNIS, true, BOB.getSecretPhrase(), ALICE.getRsAccount(), true);
        // Contract should submit transaction now
        generateBlock();

        // Verify that the contract made random pay back
        ChildTransaction childTransaction = testAndGetLastChildTransaction(2, 0, 0,
                amount -> (amount == 100 * IGNIS.ONE_COIN - 8000000L), 8000000L,
                ALICE, BOB, triggerFullHash);
        Assert.assertTrue(childTransaction.isPhased());
        long aliceBalanceDiff = ALICE.getChainBalanceDiff(2);
        long bobBalanceDiff = BOB.getChainBalanceDiff(2);

        // Bob after validating the contract payment made by Alice now reveals the hashed secret only to the contract payment
        // he assumes that his payment won't be approved and he can steal the funds.
        // But the approval implicitly also approves his transaction
        int approvalFeeNQT = 200000;
        JO response = ApproveTransactionCall.create(2).
                phasedTransaction("2:" + Convert.toHexString(childTransaction.getFullHash())).
                revealedSecret(TOP_SECRET).revealedSecretIsText(true).
                feeNQT(approvalFeeNQT).
                secretPhrase(CHUCK.getSecretPhrase()).call();
        Logger.logInfoMessage(response.toJSONString());
        generateBlock();

        long amountNQT = childTransaction.getAmount();
        Assert.assertEquals(-amountNQT + 100 * IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(2) - aliceBalanceDiff);
        Assert.assertEquals(amountNQT - 100 * IGNIS.ONE_COIN, BOB.getChainBalanceDiff(2) - bobBalanceDiff);
    }
}