package com.jelurida.ardor.contracts;

import nxt.BlockchainTest;
import nxt.addons.JO;
import nxt.blockchain.Chain;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.http.callers.GetBlockchainStatusCall;
import nxt.http.callers.GetExecutedTransactionsCall;
import nxt.http.responses.TransactionResponse;
import nxt.tools.ContractManager;
import nxt.util.Convert;
import nxt.util.Logger;
import org.junit.Assert;

import java.util.List;
import java.util.Random;

import static nxt.blockchain.ChildChain.IGNIS;

class ContractTestHelper {

    static String bobPaysContract(String message, Chain chain) {
        APICall.Builder builder = new APICall.Builder("sendMoney").
                secretPhrase(BlockchainTest.BOB.getSecretPhrase()).
                param("chain", chain.getId()).
                param("recipient", BlockchainTest.ALICE.getRsAccount()).
                param("amountNQT", 100 * IGNIS.ONE_COIN);
        if (message != null) {
            builder.param("encryptedMessageIsPrunable", "true").param("messageToEncrypt", message);
        }
        builder.feeNQT(IGNIS.ONE_COIN);
        APICall apiCall = builder.build();
        JO response = new JO(apiCall.invoke());
        Logger.logDebugMessage("sendMoney: " + response);
        BlockchainTest.generateBlock();
        return (String)response.get("fullHash");
    }

    static String messageTriggerContract(String message) {
        return messageTriggerContract(message, BlockchainTest.BOB.getSecretPhrase());
    }

    static String messageTriggerContract(String message, String secretPhrase) {
        APICall apiCall = new APICall.Builder("sendMessage").
                secretPhrase(secretPhrase).
                param("chain", ChildChain.IGNIS.getId()).
                param("recipient", BlockchainTest.ALICE.getRsAccount()).
                param("messageIsPrunable", "true").
                param("message", message).
                feeNQT(IGNIS.ONE_COIN).
                build();
        JO response = new JO(apiCall.invoke());
        Logger.logDebugMessage("sendMessage: " + response);
        BlockchainTest.generateBlock();
        return (String)response.get("fullHash");
    }

    static void deployContract(String contractName) {
        deployContract(contractName, null);
    }

    static void deployContract(String contractName, JO setupParams) {
        deployContract(contractName, setupParams, true);
    }

    static void deployContract(String contractName, JO setupParams, boolean isGenerateBlock) {
        ContractManager contractManager = new ContractManager();
        contractManager.init(contractName);
        JO uploadContractTransaction = contractManager.uploadImpl(contractName);
        if (uploadContractTransaction.isExist("errorCode")) {
            Assert.fail("Failed to deploy contract");
        }
        JO contractReferenceTransaction = contractManager.reference(contractName, uploadContractTransaction.parseHexString("fullHash"), setupParams);
        if (contractReferenceTransaction.isExist("errorCode")) {
            Assert.fail("Failed to set contract property");
        }
        if (isGenerateBlock) {
            BlockchainTest.generateBlock();
        }
    }

    @SuppressWarnings("SameParameterValue")
    static void testChildTransaction(int chainId, int type, int subType, long amount, long fee, long sender, List<Long> recipients) {
        JO getBlockchainStatusCall = GetBlockchainStatusCall.create().call();
        ChainTransactionId contractResultTransactionId = null;
        List<TransactionResponse> childTransactions = GetExecutedTransactionsCall.create(IGNIS.getId()).type(0).subtype(0).height(getBlockchainStatusCall.getInt("numberOfBlocks") - 1).getTransactions();
        for (TransactionResponse childTransaction : childTransactions) {
            Assert.assertEquals(chainId, childTransaction.getChainId());
            Assert.assertEquals(type, childTransaction.getTransactionType().getType());
            Assert.assertEquals(subType, childTransaction.getTransactionType().getSubtype());
            Assert.assertEquals(amount, childTransaction.getAmount());
            Assert.assertEquals(fee, childTransaction.getFee());
            Assert.assertEquals(sender, childTransaction.getSenderId());
            Assert.assertTrue(recipients.contains(childTransaction.getRecipientId()));
            contractResultTransactionId = new ChainTransactionId(childTransaction.getChainId(), childTransaction.getFullHash());
        }
        Assert.assertNotNull(contractResultTransactionId);
    }

    static String getRandomSeed(int seed) {
        return Convert.toHexString(Convert.longToBytes(new Random(seed).nextLong()));
    }
}
