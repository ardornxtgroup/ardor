/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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
import nxt.Constants;
import nxt.Nxt;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.crypto.HashFunction;
import nxt.http.APICall;
import nxt.http.accountControl.ACTestUtils;
import nxt.util.Convert;
import nxt.util.JSONAssert;
import nxt.util.Logger;
import nxt.voting.VoteWeighting;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class TestCompositeVoting extends BlockchainTest {
    @Test
    public void testWhitelistAndByHash() {
        String secret = "test secret";
        JSONObject response = createPhasedWhiteListAndByHash(secret, BOB);

        Object fullHash = response.get("fullHash");
        approve(fullHash, CHUCK, null);

        generateBlocks(4);

        //Not approved
        Assert.assertEquals(ACTestUtils.PhasingStatus.REJECTED, ACTestUtils.getPhasingStatus(fullHash));
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        secret = "test secret 1";
        response = createPhasedWhiteListAndByHash(secret, BOB);
        fullHash = response.get("fullHash");
        approve(fullHash, DAVE, secret);

        generateBlocks(4);

        Assert.assertEquals(ACTestUtils.PhasingStatus.REJECTED, ACTestUtils.getPhasingStatus(fullHash));
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        response = createPhasedWhiteListAndByHash(secret, BOB);
        approve(response.get("fullHash"), DAVE, secret);
        approve(response.get("fullHash"), CHUCK, null);

        generateBlock();

        //Approved
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testWhitelistedMoreThanOnce() {
        String secret = "test abcd";
        APICall.Builder builder = createWhitelistAndHashBuilder(BOB, secret);
        JSONObject response = builder.
                param("phasingExpression", "A & B & C & D").
                param("phasingCVotingModel", VoteWeighting.VotingModel.ACCOUNT.getCode()).
                param("phasingCWhitelisted", CHUCK.getStrId()).
                param("phasingCQuorum", 1).

                param("phasingDVotingModel", VoteWeighting.VotingModel.ACCOUNT.getCode()).
                param("phasingDWhitelisted", CHUCK.getStrId()).
                param("phasingDQuorum", 1).build().invoke();

        Assert.assertNull(response.get("error"));

        generateBlock();

        //single vote approves all sub-polls where CHUCK is whitelisted
        approve(response.get("fullHash"), CHUCK, null);
        approve(response.get("fullHash"), DAVE, secret);

        generateBlock();

        //Approved
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testSameSecretInTwoSubPolls() {
        String secret = "test abcd";
        String hashedSecret = Convert.toHexString(HashFunction.SHA256.hash(secret.getBytes()));

        APICall.Builder builder = createWhitelistAndHashBuilder(BOB, secret);
        JSONObject response = builder.
                param("phasingExpression", "A & B & C").

                param("phasingCVotingModel", VoteWeighting.VotingModel.HASH.getCode()).
                param("phasingCQuorum", 1).
                param("phasingCHashedSecret", hashedSecret).
                param("phasingCHashedSecretAlgorithm", HashFunction.SHA256.getId()).build().invoke();

        Assert.assertNull(response.get("error"));

        generateBlock();

        approve(response.get("fullHash"), CHUCK, null);

        //single vote approves all sub-polls where the hashed secret is found
        approve(response.get("fullHash"), DAVE, secret);

        generateBlock();

        //Approved
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testTwoSecretsApproveAtOnce() {
        String secretA = "test A";
        String secretB = "test B";

        ACTestUtils.PhasingBuilder builder = new ACTestUtils.PhasingBuilder("sendMoney", ALICE);
        long amount = 100 * ChildChain.IGNIS.ONE_COIN;
        builder.feeNQT(4 * ChildChain.IGNIS.ONE_COIN).param("recipient", BOB.getStrId()).param("amountNQT", amount);
        builder.votingModel(VoteWeighting.VotingModel.COMPOSITE).quorum(1).phasingParam("Expression", "A & B");
        builder.startSubPoll("A").votingModel(VoteWeighting.VotingModel.HASH).hashedSecret(secretA, HashFunction.SHA256).quorum(1);
        builder.startSubPoll("B").votingModel(VoteWeighting.VotingModel.HASH).hashedSecret(secretB, HashFunction.SHA256).quorum(1);

        String fullHash = new JSONAssert(builder.build().invoke()).str("fullHash");

        generateBlock();

        APICall.Builder approveBuilder = ACTestUtils.approveBuilder(fullHash, CHUCK, secretA);
        approveBuilder.param("revealedSecretText", new String[] {secretA, secretB});
        new JSONAssert(approveBuilder.build().invoke()).str("fullHash");

        generateBlock();

        //Approved
        Assert.assertEquals(amount, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testAssetAndCurrency() {
        generateBlocks(4); //empty for the back fees feature

        String currencyId = distributeCurrency(100);
        String assetId = distributeAsset(100);

        JSONObject response = createPhasedAssetAndCurrency(DAVE, assetId, currencyId);
        Object fullHash = response.get("fullHash");
        approve(fullHash, BOB, null);
        generateBlocks(4);

        Assert.assertEquals(ACTestUtils.PhasingStatus.REJECTED, ACTestUtils.getPhasingStatus(fullHash));
        Assert.assertEquals(0, DAVE.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        response = createPhasedAssetAndCurrency(DAVE, assetId, currencyId);
        fullHash = response.get("fullHash");
        approve(fullHash, CHUCK, null);
        generateBlocks(4);

        Assert.assertEquals(ACTestUtils.PhasingStatus.REJECTED, ACTestUtils.getPhasingStatus(fullHash));
        Assert.assertEquals(0, DAVE.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        response = createPhasedAssetAndCurrency(DAVE, assetId, currencyId);
        fullHash = response.get("fullHash");
        approve(fullHash, BOB, null);
        approve(fullHash, CHUCK, null);

        generateBlocks(1);
        //Not yet approved
        Assert.assertEquals(ACTestUtils.PhasingStatus.PENDING, ACTestUtils.getPhasingStatus(fullHash));
        Assert.assertEquals(0, DAVE.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlocks(3);
        //Approved
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        response = createPhasedAssetAndCurrency(DAVE, assetId, currencyId);
        approve(response.get("fullHash"), ALICE, null);
        generateBlocks(4);
        //Since Alice has enough of both the asset and the currency, she can finish both polls with one transaction
        Assert.assertEquals(200 * ChildChain.IGNIS.ONE_COIN, DAVE.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        //test trimming
        //generateBlocks(Math.max(Constants.MAX_ROLLBACK, Nxt.getIntProperty("nxt.trimFrequency")));
    }

    @Test
    public void testNegatedWhitelist() {
        long fee = 3 * ChildChain.IGNIS.ONE_COIN;
        JSONObject response = createWhitelistNegated(BOB, fee);
        Object fullHash = response.get("fullHash");

        generateBlocks(3);
        //Not yet approved
        Assert.assertEquals(ACTestUtils.PhasingStatus.PENDING, ACTestUtils.getPhasingStatus(fullHash));
        Assert.assertEquals(0, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlock();
        //Approved by simply waiting for the finish height
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        response = createWhitelistNegated(DAVE, fee);

        fullHash = response.get("fullHash");
        approve(fullHash, CHUCK, null);
        generateBlock();

        Assert.assertEquals(ACTestUtils.PhasingStatus.REJECTED, ACTestUtils.getPhasingStatus(fullHash));
        Assert.assertEquals(-fee, DAVE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void xorApprovalNoApproval() {
        long fee = 3 * ChildChain.IGNIS.ONE_COIN;
        createXorWhitelist(BOB, fee);

        generateBlocks(3);
        // Not yet approved
        Assert.assertEquals(0, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlock();
        // Not Approved, Bob only paid the fee
        Assert.assertEquals(0, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-fee, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void xorApprovalBothApprove() {
        long fee = 3 * ChildChain.IGNIS.ONE_COIN;
        JSONObject response = createXorWhitelist(BOB, fee);
        generateBlock();
        // Not yet approved
        Assert.assertEquals(0, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));

        approve(response.get("fullHash"), CHUCK, null);
        approve(response.get("fullHash"), DAVE, null);
        generateBlock();
        // Not apprvoed (no need to wait for finish height)
        Assert.assertEquals(0, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-fee, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void xorApprovalChuckApproves() {
        long fee = 3 * ChildChain.IGNIS.ONE_COIN;
        JSONObject response = createXorWhitelist(BOB, fee);
        approve(response.get("fullHash"), CHUCK, null);
        generateBlocks(3);
        // Not yet approved
        Assert.assertEquals(0, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlock();
        // Approved at finsh height
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void xorApprovalDaveApproves() {
        long fee = 3 * ChildChain.IGNIS.ONE_COIN;
        JSONObject response = createXorWhitelist(BOB, fee);
        approve(response.get("fullHash"), CHUCK, null);
        generateBlocks(3);
        // Not yet approved
        Assert.assertEquals(0, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlock();
        // Approved at finsh height
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, BOB.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testNegatedAssetApproved() {
        long fee = 3 * ChildChain.IGNIS.ONE_COIN;
        createPhasedByAssetNegated(BOB, CHUCK, fee);

        generateBlocks(4);

        //Approved by simply waiting for the finish height
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, CHUCK.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testNegatedAssetRejected() {
        long fee = 3 * ChildChain.IGNIS.ONE_COIN;
        JSONObject response = createPhasedByAssetNegated(DAVE, CHUCK, fee);
        approve(response.get("fullHash"), BOB, null);
        generateBlock();

        // still not rejected
        Assert.assertEquals(0, CHUCK.getChainBalanceDiff(ChildChain.IGNIS.getId()));
        Assert.assertEquals(-100 * ChildChain.IGNIS.ONE_COIN - fee, DAVE.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlocks(3);

        // Rejected at finish height
        Assert.assertEquals(-fee, DAVE.getChainUnconfirmedBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testByPropertyAndNone() {
        String propertyName = "prop3";
        String propertyValue = "prop_val";

        APICall.Builder builder = TestPropertyVoting.createSetPropertyBuilder(CHUCK, ALICE, propertyName, propertyValue);
        builder.build().invoke();
        generateBlock();

        builder = createGenericBuilder(ALICE, BOB, 3 * ChildChain.IGNIS.ONE_COIN);
        builder.param("phasingExpression", "A & N");

        builder.param("phasingAVotingModel", VoteWeighting.VotingModel.PROPERTY.getCode());
        builder.param("phasingAQuorum", 1);
        builder.param("phasingASenderPropertySetter", CHUCK.getStrId());
        builder.param("phasingASenderPropertyName", propertyName);
        builder.param("phasingASenderPropertyValue", propertyValue);

        builder.param("phasingNVotingModel", VoteWeighting.VotingModel.NONE.getCode());
        //builder.param("phasingNQuorum", 0);

        builder.build().invoke();
        generateBlock();

        //Not finished yet
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlocks(4);
        //Finished
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testNoEarlyFinish() {

        APICall.Builder builder = createGenericBuilder(ALICE, BOB, 3 * ChildChain.IGNIS.ONE_COIN);
        builder.param("phasingExpression", "A & N");

        builder.param("phasingAVotingModel", VoteWeighting.VotingModel.ACCOUNT.getCode());
        builder.param("phasingAWhitelisted", CHUCK.getStrId());
        builder.param("phasingAQuorum", 1);

        builder.param("phasingNVotingModel", VoteWeighting.VotingModel.NONE.getCode());
        //builder.param("phasingNQuorum", 0);

        builder.build().invoke();
        generateBlock();

        //Not finished yet
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlocks(4);
        //Rejected
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        //Phase again
        builder.param("phasingFinishHeight", Nxt.getBlockchain().getHeight() + 5);
        JSONObject response = builder.build().invoke();
        generateBlock();

        //approve
        approve(response.get("fullHash"), CHUCK, null);
        generateBlock();

        //still not finished
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlocks(3);

        //finished
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testByTwoProperties() {

        APICall.Builder propBuilder = TestPropertyVoting.createSetPropertyBuilder(CHUCK, ALICE, "a", "b");
        propBuilder.build().invoke();

        propBuilder = TestPropertyVoting.createSetPropertyBuilder(CHUCK, ALICE, "c", "d");
        propBuilder.build().invoke();

        generateBlock();

        ACTestUtils.PhasingBuilder builder = createGenericBuilder(ALICE, BOB, 4 * ChildChain.IGNIS.ONE_COIN);
        builder.votingModel(VoteWeighting.VotingModel.COMPOSITE).quorum(1).phasingParam("Expression", "A & B");
        builder.startSubPoll("A").votingModel(VoteWeighting.VotingModel.PROPERTY).property("Sender", CHUCK, "a", "b").quorum(1);
        builder.startSubPoll("B").votingModel(VoteWeighting.VotingModel.PROPERTY).property("Sender", CHUCK, "c", "d").quorum(1);

        String fullHash = new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        //finished
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        APICall.Builder getPollBuilder = new APICall.Builder("getPhasingPoll").param("transactionFullHash", fullHash).param("countVotes", "true");
        JSONAssert result = new JSONAssert(getPollBuilder.build().invoke());

        Assert.assertEquals("1", result.str("result"));
    }

    @Test
    public void testCompositeVotingNotAcceptingSecret() {
        ACTestUtils.PhasingBuilder builder = createGenericBuilder(ALICE, BOB, 4 * ChildChain.IGNIS.ONE_COIN);
        builder.votingModel(VoteWeighting.VotingModel.COMPOSITE).quorum(1).phasingParam("Expression", "A & B");
        builder.startSubPoll("A").votingModel(VoteWeighting.VotingModel.ACCOUNT).whitelist(CHUCK).quorum(1);
        builder.startSubPoll("B").votingModel(VoteWeighting.VotingModel.ACCOUNT).whitelist(DAVE).quorum(1);

        String fullHash = new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        APICall.Builder approveBuilder = ACTestUtils.approveBuilder(fullHash, CHUCK, "some secret");

        JSONAssert jsonAssert = new JSONAssert(approveBuilder.build().invoke());
        Assert.assertTrue(jsonAssert.str("errorDescription").matches("Phased transaction [0-9]+ does not accept by-hash voting"));
    }

    @Test
    public void testEarlyFinishOfTransactionVoting() {
        JSONAssert response = new JSONAssert(TestApproveTransaction.getSignedBytes());
        String fullHash1 = response.str("fullHash");
        String approvalTransactionBytes1 = response.str("transactionBytes");

        response = new JSONAssert(TestApproveTransaction.getSignedBytes());
        String fullHash2 = response.str("fullHash");
        String approvalTransactionBytes2 = response.str("transactionBytes");

        ACTestUtils.PhasingBuilder builder = createGenericBuilder(ALICE, BOB, 4 * ChildChain.IGNIS.ONE_COIN);
        builder.votingModel(VoteWeighting.VotingModel.COMPOSITE).quorum(1).phasingParam("Expression", "T1 & T2");
        builder.startSubPoll("T1").votingModel(VoteWeighting.VotingModel.TRANSACTION).phasingParam("LinkedTransaction",
                ChildChain.IGNIS.getId() + ":" + fullHash1).quorum(1);
        builder.startSubPoll("T2").votingModel(VoteWeighting.VotingModel.TRANSACTION).phasingParam("LinkedTransaction",
                ChildChain.IGNIS.getId() + ":" + fullHash2).quorum(1);

        new JSONAssert(builder.build().invoke()).str("fullHash");

        generateBlock();

        new APICall.Builder("broadcastTransaction").
                param("transactionBytes", approvalTransactionBytes1).
                build().invoke();

        new APICall.Builder("broadcastTransaction").
                param("transactionBytes", approvalTransactionBytes2).
                build().invoke();

        generateBlock();

        //finished
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testLinkedTransactionsGrouping() {
        JSONAssert response = new JSONAssert(TestApproveTransaction.getSignedBytes());
        String fullHash = response.str("fullHash");
        String approvalTransactionBytes = response.str("transactionBytes");

        //two sub-polls with same linked transaction
        ACTestUtils.PhasingBuilder builder = createGenericBuilder(ALICE, BOB, 4 * ChildChain.IGNIS.ONE_COIN);
        builder.votingModel(VoteWeighting.VotingModel.COMPOSITE).quorum(1).phasingParam("Expression", "T1 & T2");
        builder.startSubPoll("T1").votingModel(VoteWeighting.VotingModel.TRANSACTION).phasingParam("LinkedTransaction",
                ChildChain.IGNIS.getId() + ":" + fullHash).quorum(1);
        builder.startSubPoll("T2").votingModel(VoteWeighting.VotingModel.TRANSACTION).phasingParam("LinkedTransaction",
                ChildChain.IGNIS.getId() + ":" + fullHash).quorum(1);

        String phasedFullHash = new JSONAssert(builder.build().invoke()).str("fullHash");

        generateBlock();

        APICall.Builder queryBuilder = new APICall.Builder("getLinkedPhasedTransactions")
                .param("linkedFullHash", fullHash);
        List<JSONObject> transactions = new JSONAssert(queryBuilder.build().invoke()).array("transactions", JSONObject.class);
        Assert.assertEquals(1, transactions.size());
        Assert.assertEquals(phasedFullHash, new JSONAssert(transactions.get(0)).str("fullHash"));

        //both sub-polls are approved with one voting transaction
        new APICall.Builder("broadcastTransaction").
                param("transactionBytes", approvalTransactionBytes).
                build().invoke();

        generateBlock();

        //finished
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    @Test
    public void testWhitelistedCoinVoting() {
        ACTestUtils.PhasingBuilder builder = createGenericBuilder(ALICE, BOB, 4 * ChildChain.IGNIS.ONE_COIN);
        builder.votingModel(VoteWeighting.VotingModel.COMPOSITE).quorum(1).phasingParam("Expression", "A1 & A2");
        builder.startSubPoll("A1").votingModel(VoteWeighting.VotingModel.ACCOUNT).whitelist(CHUCK, DAVE).quorum(2);
        builder.startSubPoll("A2").votingModel(VoteWeighting.VotingModel.COIN).whitelist(DAVE)
                .quorum(DAVE.getInitialChainBalance(ChildChain.IGNIS.getId()) + 1); //A2 cannot be approved

        String phasedFullHash = new JSONAssert(builder.build().invoke()).str("fullHash");

        generateBlock();

        APICall.Builder queryBuilder = new APICall.Builder("getVoterPhasedTransactions")
                .param("account", DAVE.getStrId());
        List<JSONObject> transactions = new JSONAssert(queryBuilder.build().invoke()).array("transactions", JSONObject.class);
        Assert.assertEquals(1, transactions.size());
        Assert.assertEquals(phasedFullHash, new JSONAssert(transactions.get(0)).str("fullHash"));

        approve(phasedFullHash, CHUCK, null);
        approve(phasedFullHash, DAVE, null);

        generateBlocks(4);

        //rejected - chuck's vote should not be counted for sub-poll A2 (since he is not whitelisted there)
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        //while builder state is still in sub-poll A2 - add CHUCK to whitelist
        builder.whitelist(CHUCK, DAVE);
        builder.param("phasingFinishHeight", Nxt.getBlockchain().getHeight() + 5);
        phasedFullHash = new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        approve(phasedFullHash, CHUCK, null);
        approve(phasedFullHash, DAVE, null);

        generateBlock();

        //cannot finish early due to balance voting
        Assert.assertEquals(0, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));

        generateBlocks(3);

        //approved
        Assert.assertEquals(100 * ChildChain.IGNIS.ONE_COIN, BOB.getChainBalanceDiff(ChildChain.IGNIS.getId()));
    }

    private String distributeCurrency(long chuckCurrencyAmount) {
        APICall.Builder builder = new ACTestUtils.CurrencyBuilder().naming("CompositeV", "TCOMV", "Test Composite Voting");
        String currencyId = Tester.responseToStringId(ACTestUtils.assertTransactionSuccess(builder));
        generateBlock();

        builder = new APICall.Builder("transferCurrency")
                .param("secretPhrase", ALICE.getSecretPhrase())
                .param("recipient", CHUCK.getRsAccount())
                .param("currency", currencyId)
                .param("unitsQNT", chuckCurrencyAmount)
                .param("feeNQT", ChildChain.IGNIS.ONE_COIN)
                .param("deadline", 1440);
        ACTestUtils.assertTransactionSuccess(builder);

        BlockchainTest.generateBlock();

        return currencyId;
    }

    private String distributeAsset(long bobAssetAmount) {
        APICall.Builder builder = new ACTestUtils.AssetBuilder(ALICE.getSecretPhrase(), "CompositeV");
        String assetId = Tester.responseToStringId(ACTestUtils.assertTransactionSuccess(builder));
        generateBlock();

        builder = new APICall.Builder("transferAsset")
                .param("secretPhrase", ALICE.getSecretPhrase())
                .param("recipient", BOB.getRsAccount())
                .param("asset", assetId)
                .param("description", "asset testing")
                .param("quantityQNT", bobAssetAmount)
                .param("feeNQT", ChildChain.IGNIS.ONE_COIN);
        ACTestUtils.assertTransactionSuccess(builder);

        BlockchainTest.generateBlock();

        return assetId;
    }

    private static JSONObject approve(Object fullHash, Tester approver, String secret) {
        return ACTestUtils.approve(fullHash, approver, secret);
    }

    private JSONObject createPhasedWhiteListAndByHash(String secret, Tester recipient) {

        JSONObject response = createWhitelistAndHashBuilder(recipient, secret).build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);

        Assert.assertNull(response.get("error"));

        generateBlock();

        return response;
    }

    private APICall.Builder createWhitelistAndHashBuilder(Tester recipient, String secret) {
        long fee = 3 * ChildChain.IGNIS.ONE_COIN;
        String hashedSecret = Convert.toHexString(HashFunction.SHA256.hash(secret.getBytes()));

        Tester sender = ALICE;
        return createGenericBuilder(sender, recipient, fee).
                param("phasingExpression", "A & B").

                param("phasingAVotingModel", VoteWeighting.VotingModel.HASH.getCode()).
                param("phasingAHashedSecret", hashedSecret).
                param("phasingAHashedSecretAlgorithm", HashFunction.SHA256.getId()).
                param("phasingAQuorum", 1).

                param("phasingBVotingModel", VoteWeighting.VotingModel.ACCOUNT.getCode()).
                param("phasingBWhitelisted", CHUCK.getStrId()).
                param("phasingBQuorum", 1);
    }

    private ACTestUtils.PhasingBuilder createGenericBuilder(Tester sender, Tester recipient, long fee) {
        ACTestUtils.PhasingBuilder result = new ACTestUtils.PhasingBuilder("sendMoney", sender).
                votingModel(VoteWeighting.VotingModel.COMPOSITE).quorum(1);
        result.param("recipient", recipient.getStrId()).
                param("amountNQT", 100 * ChildChain.IGNIS.ONE_COIN).
                param("feeNQT", fee);
        return result;
    }

    private JSONObject createPhasedAssetAndCurrency(Tester recipient, String assetId, String currencyId) {
        long fee = 6 * ChildChain.IGNIS.ONE_COIN;
        JSONObject response = createGenericBuilder(ALICE, recipient, fee).
                param("phasingExpression", "A & C").

                param("phasingAVotingModel", VoteWeighting.VotingModel.ASSET.getCode()).
                param("phasingAHolding", assetId).
                param("phasingAQuorum", 100).

                param("phasingCVotingModel", VoteWeighting.VotingModel.CURRENCY.getCode()).
                param("phasingCHolding", currencyId).
                param("phasingCQuorum", 100).

                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);

        Assert.assertNull(response.get("errorCode"));

        generateBlock();

        return response;
    }

    private JSONObject createWhitelistNegated(Tester sender, long fee) {

        JSONObject response = createGenericBuilder(sender, ALICE, fee).
                param("phasingExpression", "!A").

                param("phasingAVotingModel", VoteWeighting.VotingModel.ACCOUNT.getCode()).
                param("phasingAWhitelisted", CHUCK.getStrId()).
                param("phasingAQuorum", 1).build().invoke();

        Assert.assertNull(response.get("errorCode"));

        generateBlock();

        return response;
    }

    private JSONObject createXorWhitelist(Tester sender, long fee) {
        // XOR CHUCK and DAVE approval
        JSONObject response = createGenericBuilder(sender, ALICE, fee).
                param("phasingExpression", "A & !B | !A & B").
                param("phasingAVotingModel", VoteWeighting.VotingModel.ACCOUNT.getCode()).
                param("phasingAWhitelisted", CHUCK.getStrId()).
                param("phasingAQuorum", 1).
                param("phasingBVotingModel", VoteWeighting.VotingModel.ACCOUNT.getCode()).
                param("phasingBWhitelisted", DAVE.getStrId()).
                param("phasingBQuorum", 1).
                build().invoke();

        Assert.assertNull(response.get("errorCode"));
        generateBlock();
        return response;
    }

    private JSONObject createPhasedByAssetNegated(Tester sender, Tester recipient, long fee) {

        String assetId = distributeAsset(150);

        JSONObject response = createGenericBuilder(sender, recipient, fee).
                param("phasingExpression", "!A").

                param("phasingAVotingModel", VoteWeighting.VotingModel.ASSET.getCode()).
                param("phasingAHolding", assetId).
                param("phasingAQuorum", 150).build().invoke();

        Assert.assertNull(response.get("errorCode"));

        generateBlock();

        return response;
    }
}
