/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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

package nxt.http.twophased;

import nxt.BlockchainTest;
import nxt.Nxt;
import nxt.ae.Asset;
import nxt.blockchain.ChildChain;
import nxt.crypto.HashFunction;
import nxt.http.APICall;
import nxt.http.accountControl.ACTestUtils;
import nxt.http.twophased.TestCreateTwoPhased.TwoPhasedMoneyTransferBuilder;
import nxt.util.JSONAssert;
import nxt.util.Logger;
import nxt.voting.VoteWeighting;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class TestApproveTransaction extends BlockchainTest {

    @Test
    public void validVoteCasting() {
        int duration = 10;

        APICall apiCall = new TwoPhasedMoneyTransferBuilder()
                .finishHeight(Nxt.getBlockchain().getHeight() + duration)
                .build();

        JSONObject transactionJSON = TestCreateTwoPhased.issueCreateTwoPhased(apiCall, false);
        generateBlock();

        apiCall = new APICall.Builder("approveTransaction")
                .param("secretPhrase", CHUCK.getSecretPhrase())
                .param("phasedTransaction", ChildChain.IGNIS.getId() + ":" + transactionJSON.get("fullHash"))
                .param("feeNQT", ChildChain.IGNIS.ONE_COIN)
                .build();

        JSONObject response = apiCall.invoke();
        Logger.logMessage("approvePhasedTransactionResponse:" + response.toJSONString());
        Assert.assertNotNull(response.get("fullHash"));

        generateBlocks(duration);
        Assert.assertEquals(-50 * ChildChain.IGNIS.ONE_COIN - 2 * ChildChain.IGNIS.ONE_COIN,
                ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(50 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-ChildChain.IGNIS.ONE_COIN, CHUCK.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void invalidVoteCasting() {
        int duration = 10;

        APICall apiCall = new TwoPhasedMoneyTransferBuilder()
                .finishHeight(Nxt.getBlockchain().getHeight() + duration)
                .build();

        JSONObject transactionJSON = TestCreateTwoPhased.issueCreateTwoPhased(apiCall, false);
        generateBlock();
        apiCall = new APICall.Builder("approveTransaction")
                .param("secretPhrase", DAVE.getSecretPhrase())
                .param("phasedTransaction", ChildChain.IGNIS.getId() + ":" + transactionJSON.get("fullHash"))
                .param("feeNQT", ChildChain.IGNIS.ONE_COIN)
                .build();
        JSONObject response = apiCall.invoke();
        Assert.assertNotNull(response.get("error"));
        generateBlock();

        Assert.assertEquals("ALICE balance: ", -2 * ChildChain.IGNIS.ONE_COIN,
                ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals("BOB balance: ", 0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals("CHUCK balance: ", 0, CHUCK.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals("DAVE balance: ", 0, DAVE.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlocks(duration);

        Assert.assertEquals("ALICE balance: ", -2 * ChildChain.IGNIS.ONE_COIN,
                ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals("BOB balance: ", 0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals("CHUCK balance: ", 0, CHUCK.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals("DAVE balance: ", 0, DAVE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void sendMoneyPhasedNoVoting() {
        long fee = 2* ChildChain.IGNIS.ONE_COIN;
        JSONObject response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * ChildChain.IGNIS.ONE_COIN).
                param("feeNQT", fee).
                param("phased", "true").
                param("phasingFinishHeight", baseHeight + 2).
                param("phasingVotingModel", -1).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);

        generateBlock();
        // Transaction is not applied yet, fee is paid
        // Forger
        Assert.assertEquals(fee, FORGY.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(fee, FORGY.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
        // Sender
        Assert.assertEquals(-fee, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, ALICE.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
        // Recipient
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(0, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlock();
        // Transaction is applied
        // Sender
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, ALICE.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
        // Recipient
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void sendMoneyPhasedByTransactionHash() {
        JSONObject response = getSignedBytes();
        Logger.logDebugMessage("signedSendMessage: " + response);
        String fullHash = (String)response.get("fullHash");
        Assert.assertEquals(64, fullHash.length());
        String approvalTransactionBytes = (String)response.get("transactionBytes");

        long fee = 3 * ChildChain.IGNIS.ONE_COIN;
        response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * ChildChain.IGNIS.ONE_COIN).
                param("feeNQT", fee).
                param("phased", "true").
                param("phasingFinishHeight", baseHeight + 3).
                param("phasingVotingModel", 4).
                param("phasingLinkedTransaction", ChildChain.IGNIS.getId() + ":" + fullHash).
                param("phasingQuorum", 1).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);

        generateBlock();
        // Transaction is not applied yet
        // Sender
        Assert.assertEquals(-fee, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, ALICE.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
        // Recipient
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(0, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));

        response = new APICall.Builder("broadcastTransaction").
                param("transactionBytes", approvalTransactionBytes).
                build().invoke();
        Logger.logDebugMessage("broadcastTransaction: " + response);
        generateBlock();

        // Transaction is applied before finish height
        // Sender
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, ALICE.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
        // Recipient
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void sendMoneyPhasedByTransactionHash2of3() {
        JSONObject response = getSignedBytes();
        Logger.logDebugMessage("signedSendMessage: " + response);
        String fullHash1 = (String)response.get("fullHash");
        Assert.assertEquals(64, fullHash1.length());
        String approvalTransactionBytes1 = (String)response.get("transactionBytes");
        response = getSignedBytes();
        Logger.logDebugMessage("signedSendMessage: " + response);
        String fullHash2 = (String)response.get("fullHash");
        Assert.assertEquals(64, fullHash2.length());
        response = getSignedBytes();
        Logger.logDebugMessage("signedSendMessage: " + response);
        String fullHash3 = (String)response.get("fullHash");
        Assert.assertEquals(64, fullHash3.length());
        String approvalTransactionBytes3 = (String)response.get("transactionBytes");

        String chainPrefix = ChildChain.IGNIS.getId() + ":";
        long fee = 5 * ChildChain.IGNIS.ONE_COIN;
        response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * ChildChain.IGNIS.ONE_COIN).
                param("feeNQT", fee).
                param("phased", "true").
                param("phasingFinishHeight", baseHeight + 2).
                param("phasingVotingModel", 4).
                param("phasingLinkedTransaction", new String[] { chainPrefix + fullHash1, chainPrefix + fullHash2,
                        chainPrefix + fullHash3 }).
                param("phasingQuorum", 2).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);

        generateBlock();
        // Transaction is not applied yet
        // Sender
        Assert.assertEquals(-fee, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, ALICE.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
        // Recipient
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(0, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));

        response = new APICall.Builder("broadcastTransaction").
                param("transactionBytes", approvalTransactionBytes1).
                build().invoke();
        Logger.logDebugMessage("broadcastTransaction: " + response);
        response = new APICall.Builder("broadcastTransaction").
                param("transactionBytes", approvalTransactionBytes3).
                build().invoke();
        Logger.logDebugMessage("broadcastTransaction: " + response);
        generateBlock();

        // Transaction is applied since 2 out 3 hashes were provided
        // Sender
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, ALICE.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
        // Recipient
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void sendMoneyPhasedByTransactionHashNotApplied() {
        long fee = 3 * ChildChain.IGNIS.ONE_COIN;
        JSONObject response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * ChildChain.IGNIS.ONE_COIN).
                param("feeNQT", fee).
                param("phased", "true").
                param("phasingFinishHeight", baseHeight + 2).
                param("phasingVotingModel", 4).
                param("phasingLinkedTransaction", ChildChain.IGNIS.getId() + ":a13bbe67211fea8d59b2621f1e0118bb242dc5000d428a23a8bd47491a05d681"). // this hash does not match any transaction
                param("phasingQuorum", 1).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);

        generateBlock();
        // Transaction is not applied yet
        // Sender
        Assert.assertEquals(-fee, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, ALICE.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
        // Recipient
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(0, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlock();
        // Transaction is rejected since full hash does not match
        // Sender
        Assert.assertEquals(-fee, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-fee, ALICE.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
        // Recipient
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(0, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void setAliasPhasedByTransactionHashInvalid() {
        JSONObject response = getSignedBytes();
        Logger.logDebugMessage("signedSendMessage: " + response);
        String fullHash = (String)response.get("fullHash");
        Assert.assertEquals(64, fullHash.length());
        String approvalTransactionBytes = (String)response.get("transactionBytes");

        long fee = 2 * ChildChain.IGNIS.ONE_COIN;
        String alias = "alias" + System.currentTimeMillis();
        response = new APICall.Builder("setAlias").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("aliasName", alias).
                param("feeNQT", fee).
                param("phased", "true").
                param("phasingFinishHeight", baseHeight + 4).
                param("phasingVotingModel", 4).
                param("phasingLinkedFullHash", fullHash).
                param("phasingQuorum", 1).
                build().invoke();
        Logger.logDebugMessage("setAlias: " + response);

        generateBlock();
        response = new APICall.Builder("getAlias").
                param("aliasName", alias).
                build().invoke();
        Logger.logDebugMessage("getAlias: " + response);
        Assert.assertEquals((long)5, response.get("errorCode"));

        response = new APICall.Builder("broadcastTransaction").
                param("transactionBytes", approvalTransactionBytes).
                build().invoke();
        Logger.logDebugMessage("broadcastTransaction: " + response);
        generateBlock();

        // allocate the same alias immediately
        response = new APICall.Builder("setAlias").
                param("secretPhrase", BOB.getSecretPhrase()).
                param("aliasName", alias).
                param("feeNQT", fee).
                build().invoke();
        Logger.logDebugMessage("setSameAlias: " + response);
        generateBlock();
        // phased setAlias transaction is applied but invalid
        response = new APICall.Builder("getAlias").
                param("aliasName", alias).
                build().invoke();
        Logger.logDebugMessage("getAlias: " + response);
        Assert.assertEquals(BOB.getStrId(), response.get("account"));
        generateBlock();
        // phased setAlias transaction is applied but invalid
        response = new APICall.Builder("getAlias").
                param("aliasName", alias).
                build().invoke();
        Logger.logDebugMessage("getAlias: " + response);
        Assert.assertEquals(BOB.getStrId(), response.get("account"));
    }

    @Test
    public void testInvalidHash() {
        long amount = 100 * ChildChain.IGNIS.ONE_COIN;
        String secret = "abc";
        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder("sendMoney", ALICE);
        builder.param("recipient", BOB.getStrId()).param("amountNQT", amount);
        builder.votingModel(VoteWeighting.VotingModel.HASH).hashedSecret(secret, HashFunction.SHA256).quorum(1);

        String fullHash = new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        APICall.Builder approveBuilder = ACTestUtils.approveBuilder(fullHash, BOB, "wrong secret");

        JSONAssert jsonAssert = new JSONAssert(approveBuilder.build().invoke());
        Assert.assertTrue(jsonAssert.str("errorDescription").matches("Hashed secret\\(s\\) in phased transaction [0-9]+ do not match any of the revealed secret"));
    }

    @Test
    public void testApproveTwoTransactionsWithOneSecret() {
        long amount = 100 * ChildChain.IGNIS.ONE_COIN;
        String secret = "abc";
        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder("sendMoney", ALICE);
        builder.param("recipient", BOB.getStrId()).param("amountNQT", amount);
        builder.votingModel(VoteWeighting.VotingModel.HASH).hashedSecret(secret, HashFunction.SHA256).quorum(1);

        String fullHash1 = new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        String fullHash2 = new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        APICall.Builder approveBuilder = ACTestUtils.approveBuilder(fullHash1, BOB, secret);
        approveBuilder.param("phasedTransaction", new String[] { ChildChain.IGNIS.getId() + ":" + fullHash1,  ChildChain.IGNIS.getId() + ":" + fullHash2});

        new JSONAssert(approveBuilder.build().invoke()).str("fullHash");

        generateBlock();

        Assert.assertEquals(2 * amount - ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testApproveTwoTransactionsWithTwoSecrets() {
        long amount = 100 * ChildChain.IGNIS.ONE_COIN;
        String secret1 = "abc111";
        String secret2 = "abc222";
        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder("sendMoney", ALICE);
        builder.param("recipient", BOB.getStrId()).param("amountNQT", amount);
        builder.votingModel(VoteWeighting.VotingModel.HASH).hashedSecret(secret1, HashFunction.SHA256).quorum(1);

        String fullHash1 = new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        builder.hashedSecret(secret2, HashFunction.SHA256);

        String fullHash2 = new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        APICall.Builder approveBuilder = ACTestUtils.approveBuilder(fullHash1, BOB, "");
        approveBuilder.param("phasedTransaction", new String[] { ChildChain.IGNIS.getId() + ":" + fullHash1,  ChildChain.IGNIS.getId() + ":" + fullHash2});
        approveBuilder.param("revealedSecretText", new String[] {secret1, secret2});

        new JSONAssert(approveBuilder.build().invoke()).str("fullHash");

        generateBlock();

        Assert.assertEquals(2 * amount - ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testApproveUnusedSecret() {
        long amount = 100 * ChildChain.IGNIS.ONE_COIN;
        String secret1 = "abc111";
        String secretUnused = "abc222";
        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder("sendMoney", ALICE);
        builder.param("recipient", BOB.getStrId()).param("amountNQT", amount);
        builder.votingModel(VoteWeighting.VotingModel.HASH).hashedSecret(secret1, HashFunction.SHA256).quorum(1);

        String fullHash1 = new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        APICall.Builder approveBuilder = ACTestUtils.approveBuilder(fullHash1, BOB, "");
        approveBuilder.param("revealedSecretText", new String[] {secret1, secretUnused});

        Assert.assertEquals("Revealed secret with index 1 is not used",
                new JSONAssert(approveBuilder.build().invoke()).str("errorDescription"));
    }

    @Test
    public void testInvalidHashInOneOfTheTransactions() {
        long amount = 100 * ChildChain.IGNIS.ONE_COIN;
        String secret = "abc";
        String wrongSecret = "wrong secret";
        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder("sendMoney", ALICE);
        builder.param("recipient", BOB.getStrId()).param("amountNQT", amount);
        builder.votingModel(VoteWeighting.VotingModel.HASH).hashedSecret(secret, HashFunction.SHA256).quorum(1);

        String fullHash1 = new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        builder.hashedSecret(wrongSecret, HashFunction.SHA256);

        String fullHash2 = new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        APICall.Builder approveBuilder = ACTestUtils.approveBuilder(fullHash1, BOB, secret);
        approveBuilder.param("phasedTransaction", new String[] { ChildChain.IGNIS.getId() + ":" + fullHash1,  ChildChain.IGNIS.getId() + ":" + fullHash2});

        JSONAssert jsonAssert = new JSONAssert(approveBuilder.build().invoke());
        Assert.assertTrue(jsonAssert.str("errorDescription").matches("Hashed secret\\(s\\) in phased transaction [0-9]+ do not match any of the revealed secret"));
    }

    static JSONObject getSignedBytes() {
        JSONObject response = new APICall.Builder("sendMessage").
                param("publicKey", CHUCK.getPublicKeyStr()).
                param("recipient", ALICE.getStrId()).
                param("message", "approval notice").
                param("feeNQT", ChildChain.IGNIS.ONE_COIN).
                build().invoke();
        Logger.logDebugMessage("sendMessage not broadcasted: " + response);
        response = new APICall.Builder("signTransaction").
                param("secretPhrase", CHUCK.getSecretPhrase()).
                param("unsignedTransactionBytes", (String)response.get("unsignedTransactionBytes")).
                build().invoke();
        return response;
    }
}