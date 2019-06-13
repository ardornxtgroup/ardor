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

package com.jelurida.ardor.contracts;

import nxt.BlockchainTest;
import nxt.addons.JO;
import nxt.blockchain.Chain;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.http.callers.GetBlockCall;
import nxt.http.callers.GetBlockchainStatusCall;
import nxt.http.callers.GetExecutedTransactionsCall;
import nxt.http.responses.BlockResponse;
import nxt.http.responses.TransactionResponse;
import nxt.tools.ContractManager;
import nxt.util.Convert;
import nxt.util.Logger;
import org.junit.Assert;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Random;

import static nxt.blockchain.ChildChain.IGNIS;

class ContractTestHelper {

    static String bobPaysContract(String message, Chain chain) {
        return bobPaysContract(message, chain, true);
    }

    static String bobPaysContract(String message, Chain chain, boolean encryptMessage) {
        return payContract(message, chain, encryptMessage, BlockchainTest.BOB.getSecretPhrase(), BlockchainTest.ALICE.getRsAccount());
    }

    static String payContract(String message, Chain chain, boolean encryptMessage, String secretPhrase, String recipient) {
        return payContract(message, chain, encryptMessage, secretPhrase, recipient, false);
    }

    static String payContract(String message, Chain chain, boolean encryptMessage, String secretPhrase, String recipient, boolean addHashedSecret) {
        APICall.Builder builder = new APICall.Builder("sendMoney").
                secretPhrase(secretPhrase).
                param("chain", chain.getId()).
                param("recipient", recipient).
                param("amountNQT", 100 * chain.ONE_COIN);
        if (message != null) {
            if (encryptMessage) {
                builder.param("encryptedMessageIsPrunable", "true").param("messageToEncrypt", message);
            } else {
                builder.param("messageIsPrunable", "true").param("message", message);
            }
        }
        if (addHashedSecret) {
            builder.param("phased", true);
            builder.param("phasingFinishHeight", BlockResponse.create(GetBlockCall.create().call()).getHeight() + 201);
            builder.param("phasingQuorum", 1);
            builder.param("phasingVotingModel", 5);
            builder.param("phasingHashedSecret", "ad531905859e62ee0b5ef2cc916cef3949b11d9b8817a8e4d7ac04f44c79e704");
            builder.param("phasingHashedSecretAlgorithm", 2);
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

    static String deployContract(Class contractClass) {
        return deployContract(contractClass, null);
    }

    static String deployContract(Class contractClass, JO setupParams) {
        return deployContract(contractClass, setupParams, true);
    }

    static String deployContract(Class contractClass, JO setupParams, boolean isGenerateBlock) {
        String contractName = contractClass.getSimpleName();
        deployContract(contractName, contractClass.getPackage().getName(), setupParams, isGenerateBlock);
        return contractName;
    }

    static void deployContract(String contractName, String packageName, JO setupParams) {
        deployContract(contractName, packageName, setupParams, true);
    }

    static void deployContract(String contractName, String packageName, JO setupParams, boolean isGenerateBlock) {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            ContractManager contractManager = new ContractManager();
            contractManager.init(contractName);
            ContractManager.ContractData contractData = contractManager.uploadImpl(contractName, packageName);
            if (contractData.getResponse().isExist("errorCode")) {
                JO response = contractData.getResponse();
                Assert.fail(String.format("Failed to deploy contract, reason: %s: %s",response.get("errorCode"), response.get("errorDescription")));
            }
            Logger.logInfoMessage("tagged data hash: " + Convert.toHexString(contractData.getTaggedDataHash()));
            JO contractReferenceTransaction = contractManager.reference(contractData, contractData.getResponse().parseHexString("fullHash"), setupParams);
            if (contractReferenceTransaction.isExist("errorCode")) {
                Assert.fail("Failed to set contract property");
            }
            if (isGenerateBlock) {
                BlockchainTest.generateBlock();
            }
            return null;
        });
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
