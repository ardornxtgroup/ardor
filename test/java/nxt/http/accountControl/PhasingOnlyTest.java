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

package nxt.http.accountControl;

import nxt.BlockchainTest;
import nxt.Nxt;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.http.APICall.Builder;
import nxt.http.twophased.TestPropertyVoting;
import nxt.util.Convert;
import nxt.util.JSON;
import nxt.util.JSONAssert;
import nxt.util.Logger;
import nxt.voting.PhasingParams;
import nxt.voting.VoteWeighting;
import nxt.voting.VoteWeighting.MinBalanceModel;
import nxt.voting.VoteWeighting.VotingModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class PhasingOnlyTest extends BlockchainTest {
    @Test
    public void testSetAndGet() throws Exception {
        
        ACTestUtils.assertNoPhasingOnlyControl();

        setPhasingOnlyControl(VotingModel.ACCOUNT, null, 1L, null, null, new long[] {BOB.getId()},
                10 * ChildChain.IGNIS.ONE_COIN, 5, 1440);

        assertPhasingOnly(new PhasingParams(new VoteWeighting(VotingModel.ACCOUNT.getCode(), 0L, 0L, (byte)0),
                        1L,  new long[] {BOB.getId()}, Collections.emptyList(), null, null, null, null),
                buildMaxFeesJSON(ChildChain.IGNIS.getId(), 10 * ChildChain.IGNIS.ONE_COIN), 5, 1440);
    }
    
    @Test
    public void testAccountVoting() throws Exception {
        //all transactions must be approved either by BOB or CHUCK
        setPhasingOnlyControl(VotingModel.ACCOUNT, null, 1L, null, null, new long[] {BOB.getId(), CHUCK.getId()}, 0, 0, 0);

        Builder builder = new ACTestUtils.Builder("sendMoney", ALICE.getSecretPhrase())
            .recipient(BOB.getId())
            .param("amountNQT", ChildChain.IGNIS.ONE_COIN);
        
        //no phasing - block
        ACTestUtils.assertTransactionBlocked(builder);
        
      //correct phasing
        setTransactionPhasingParams(builder, 20, VotingModel.ACCOUNT, null, 1L, null, null, new long[] {BOB.getId(), CHUCK.getId()});
        ACTestUtils.assertTransactionSuccess(builder);
        
      //subset of the voters should also be blocked
        setTransactionPhasingParams(builder, 20, VotingModel.ACCOUNT, null, 1L, null, null, new long[] {BOB.getId()});
        ACTestUtils.assertTransactionBlocked(builder);
        
      //incorrect quorum - even if more restrictive, should also be blocked
        setTransactionPhasingParams(builder, 20, VotingModel.ACCOUNT, null, 2L, null, null, new long[] {BOB.getId(), CHUCK.getId()});
        ACTestUtils.assertTransactionBlocked(builder);
        
        //remove the phasing control
        builder = new ACTestUtils.Builder("setPhasingOnlyControl", ALICE.getSecretPhrase());
        
        ACTestUtils.setControlPhasingParams(builder, VotingModel.NONE, null, null, null, null, null, 0, 0, 0);
        
        setTransactionPhasingParams(builder, 3, VotingModel.ACCOUNT, null, 1L, null, null, new long[] {BOB.getId(), CHUCK.getId()});
        
        JSONObject removePhasingOnlyJSON = ACTestUtils.assertTransactionSuccess(builder);
        generateBlock();
        
        assertPhasingOnly(new PhasingParams(new VoteWeighting(VotingModel.ACCOUNT.getCode(), 0L, 0L, (byte)0),
                1L, new long[] {BOB.getId(), CHUCK.getId()}, Collections.emptyList(),
                null, null, null, null), new JSONObject(), 0, 0);

        //approve the remove
        builder = new ACTestUtils.Builder("approveTransaction", BOB.getSecretPhrase())
                .param("phasedTransaction", getPhasedTransaction(removePhasingOnlyJSON));
        ACTestUtils.assertTransactionSuccess(builder);
        
        generateBlock();

        ACTestUtils.assertNoPhasingOnlyControl();
    }

    @Test
    public void testExtraRestrictions() throws Exception {
        //all transactions must be approved either by BOB or CHUCK, total fees 5 NXT, min duration 4, max duration 100
        setPhasingOnlyControl(VotingModel.ACCOUNT, null, 1L, null, null, new long[] {BOB.getId(), CHUCK.getId()},
                5 * ChildChain.IGNIS.ONE_COIN, 4, 100);

        Builder builder = new ACTestUtils.Builder("sendMoney", ALICE.getSecretPhrase())
                .recipient(BOB.getId())
                .param("amountNQT", ChildChain.IGNIS.ONE_COIN)
                .feeNQT(7 * ChildChain.IGNIS.ONE_COIN);
        // fee too high
        setTransactionPhasingParams(builder, 20, VotingModel.ACCOUNT, null, 1L, null, null, new long[] {BOB.getId(), CHUCK.getId()});
        ACTestUtils.assertTransactionBlocked(builder);

        // fee at the limit
        builder.feeNQT(5 * ChildChain.IGNIS.ONE_COIN);
        JSONObject response = ACTestUtils.assertTransactionSuccess(builder);

        generateBlock();

        // not yet approved, another transaction at max fee should fail
        ACTestUtils.assertTransactionBlocked(builder);

        //approve
        Builder approveBuilder = new ACTestUtils.Builder("approveTransaction", BOB.getSecretPhrase())
                .param("phasedTransaction", getPhasedTransaction(response));
        ACTestUtils.assertTransactionSuccess(approveBuilder);
        generateBlock();

        //now can submit next transaction
        response = ACTestUtils.assertTransactionSuccess(builder);
        String fullHash = getPhasedTransaction(response);
        generateBlock();

        //approve
        approveBuilder.param("phasedTransaction", fullHash);
        ACTestUtils.assertTransactionSuccess(approveBuilder);
        generateBlock();

        //too long or too short periods should fail
        builder.param("phasingFinishHeight", Nxt.getBlockchain().getHeight() + 200);
        ACTestUtils.assertTransactionBlocked(builder);
        builder.param("phasingFinishHeight", Nxt.getBlockchain().getHeight() + 3);
        ACTestUtils.assertTransactionBlocked(builder);
        builder.param("phasingFinishHeight", Nxt.getBlockchain().getHeight() + 4);
        ACTestUtils.assertTransactionSuccess(builder);

    }

    @Test
    public void testRejectingPendingTransaction() throws Exception {

        long amount = ChildChain.IGNIS.ONE_COIN;
        Builder builder = new ACTestUtils.Builder("sendMoney", ALICE.getSecretPhrase())
            .recipient(BOB.getId())
            .param("amountNQT", amount);
    
        setTransactionPhasingParams(builder, 4, VotingModel.ACCOUNT, null, 1L, null, null, new long[] {BOB.getId(), CHUCK.getId()});
        JSONObject sendMoneyJSON = ACTestUtils.assertTransactionSuccess(builder);
        generateBlock();
        
        builder = new ACTestUtils.Builder("setPhasingOnlyControl", ALICE.getSecretPhrase());
        
        ACTestUtils.setControlPhasingParams(builder, VotingModel.ACCOUNT, null, 1L, null, null, new long[] {DAVE.getId()}, 0, 0, 0);
        
        ACTestUtils.assertTransactionSuccess(builder);
        
        generateBlock();
        
        long balanceBeforeTransactionApproval = ACTestUtils.getAccountBalance(BOB.getId(), "unconfirmedBalanceNQT");

        //approve the pending transaction
        builder = new ACTestUtils.Builder("approveTransaction", CHUCK.getSecretPhrase())
                .param("phasedTransaction", getPhasedTransaction(sendMoneyJSON));
        ACTestUtils.assertTransactionSuccess(builder);
        
        generateBlock();

        //the sendMoney finish height
        generateBlock();

        //Transaction is approved - since commit 8b44767 account control is not checked at finish height
        Assert.assertEquals(balanceBeforeTransactionApproval + amount,
                ACTestUtils.getAccountBalance(BOB.getId(), "unconfirmedBalanceNQT"));
    }

    @Test
    public void testBalanceVoting() {
        setPhasingOnlyControl(VotingModel.COIN, "2", 100 * ChildChain.IGNIS.ONE_COIN, null, null, null, 0, 0, 0);

        Builder builder = new ACTestUtils.Builder("sendMoney", ALICE.getSecretPhrase())
            .recipient(BOB.getId())
            .param("amountNQT", ChildChain.IGNIS.ONE_COIN);

        //no phasing - block
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.COIN, "2", 100 * ChildChain.IGNIS.ONE_COIN, null, null, new long[] {DAVE.getId()});
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.ACCOUNT, null, 1L, null, null, new long[] {BOB.getId(), CHUCK.getId()});
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.COIN, "2", 100 * ChildChain.IGNIS.ONE_COIN + 1, null, null, null);
        ACTestUtils.assertTransactionBlocked(builder);

        builder = new ACTestUtils.Builder("sendMoney", ALICE.getSecretPhrase())
            .recipient(BOB.getId())
            .param("amountNQT", ChildChain.IGNIS.ONE_COIN);

        setTransactionPhasingParams(builder, 20, VotingModel.COIN, "2", 100 * ChildChain.IGNIS.ONE_COIN, null, null, null);
        ACTestUtils.assertTransactionSuccess(builder);
    }

    @Test
    public void testAssetVoting() {
        Builder builder = new ACTestUtils.AssetBuilder(ALICE.getSecretPhrase(), "TestAsset");
        String assetId = Tester.responseToStringId(ACTestUtils.assertTransactionSuccess(builder));
        generateBlock();

        builder = new ACTestUtils.AssetBuilder(ALICE.getSecretPhrase(), "TestAsset2");
        String asset2Id = Tester.responseToStringId(ACTestUtils.assertTransactionSuccess(builder));
        generateBlock();

        setPhasingOnlyControl(VotingModel.ASSET, assetId, 100L, null, null, null, 0, 0, 0);

        builder = new ACTestUtils.Builder("sendMoney", ALICE.getSecretPhrase())
            .recipient(BOB.getId())
            .param("amountNQT", ChildChain.IGNIS.ONE_COIN);
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.ASSET, asset2Id, 100L, null, null, null);
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.ASSET, assetId, 100L, null, null, null);
        ACTestUtils.assertTransactionSuccess(builder);
    }

    @Test
    public void testCurrencyVoting() {
        Builder builder = new ACTestUtils.CurrencyBuilder().naming("fgsha", "FGSHA", "Test AC");
        String currencyId = Tester.responseToStringId(ACTestUtils.assertTransactionSuccess(builder));
        generateBlock();

        builder = new ACTestUtils.CurrencyBuilder().naming("fgshb", "FGSHB", "Test AC");
        String currency2Id = Tester.responseToStringId(ACTestUtils.assertTransactionSuccess(builder));
        generateBlock();

        setPhasingOnlyControl(VotingModel.CURRENCY, currencyId, 100L, null, null, null, 0, 0, 0);

        builder = new ACTestUtils.Builder("sendMoney", ALICE.getSecretPhrase())
            .recipient(BOB.getId())
            .param("amountNQT", ChildChain.IGNIS.ONE_COIN);
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.CURRENCY, currency2Id, 100L, null, null, null);
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.CURRENCY, currencyId, 100L, null, null, null);
        ACTestUtils.assertTransactionSuccess(builder);
    }

    @Test
    public void testPropertyVoting() {
        Builder builder = new ACTestUtils.Builder("setPhasingOnlyControl", ALICE.getSecretPhrase());
        ACTestUtils.setControlPhasingParams(builder, VotingModel.PROPERTY, null, 1L,
                0L, MinBalanceModel.NONE, null, 0, 0, 0);

        String propertyName = "propac1";
        String propertyValue = "prop_val";

        builder.param("controlSenderPropertySetter", CHUCK.getStrId());
        builder.param("controlSenderPropertyName", propertyName);
        builder.param("controlSenderPropertyValue", propertyValue);

        new JSONAssert(builder.build().invoke()).str("fullHash");
        generateBlock();

        builder = TestPropertyVoting.createGenericBuilder();
        builder.param("phasingSenderPropertySetter", CHUCK.getStrId());
        builder.param("phasingSenderPropertyName", propertyName);
        builder.param("phasingSenderPropertyValue", propertyValue + "a");

        new JSONAssert(builder.build().invoke()).str("errorDescription");

        builder.param("phasingSenderPropertyValue", propertyValue);
        builder.param("phasingSenderPropertySetter", BOB.getStrId());
        new JSONAssert(builder.build().invoke()).str("errorDescription");

        builder.param("phasingSenderPropertySetter", CHUCK.getStrId());
        new JSONAssert(builder.build().invoke()).str("fullHash");

        generateBlock();
    }

    private void assertPhasingOnly(PhasingParams expected, JSONObject maxFees, int minDuration, int maxDuration) {
        Builder builder = new APICall.Builder("getPhasingOnlyControl")
            .param("account", Long.toUnsignedString(ALICE.getId()));

        JSONAssert response = new JSONAssert(builder.build().invoke());
        JSONAssert params = response.subObj("controlParams");
        Assert.assertEquals(expected.getVoteWeighting().getVotingModel().getCode(), ((Long) params.integer("phasingVotingModel")).byteValue());
        Assert.assertEquals(expected.getQuorum(), Convert.parseLong(params.str("phasingQuorum")));
        Assert.assertEquals(expected.getWhitelist().length, params.array("phasingWhitelist", String.class).size());
        Assert.assertEquals(expected.getVoteWeighting().getHoldingId(), Convert.parseUnsignedLong(params.str("phasingHolding")));
        Assert.assertEquals(expected.getVoteWeighting().getMinBalance(), Convert.parseLong(params.str("phasingMinBalance")));
        Assert.assertEquals(expected.getVoteWeighting().getMinBalanceModel().getCode(), ((Long) params.integer("phasingVotingModel")).byteValue());
        Assert.assertEquals(maxFees, response.subObj("maxFees").getJson());
        Assert.assertEquals(minDuration, response.integer("minDuration"));
        Assert.assertEquals(maxDuration, response.integer("maxDuration"));
    }

    public static void setPhasingOnlyControl(VotingModel votingModel, String holdingId, Long quorum,
                                       Long minBalance, MinBalanceModel minBalanceModel, long[] whitelist,
                                       long maxFees, int minDuration, int maxDuration) {

        Builder builder = new ACTestUtils.Builder("setPhasingOnlyControl", ALICE.getSecretPhrase());

        ACTestUtils.setControlPhasingParams(builder, votingModel, holdingId, quorum,
                minBalance, minBalanceModel, whitelist, maxFees, minDuration, maxDuration);

        APICall apiCall = builder.build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("setPhasingOnlyControl response: " + response.toJSONString());

        String result = (String) response.get("fullHash");
        Assert.assertNotNull(result);

        generateBlock();
    }

    private void setTransactionPhasingParams(Builder builder, int finishAfter, VotingModel votingModel, String holdingId, Long quorum,
            Long minBalance, MinBalanceModel minBalanceModel, long[] whitelist) {

        builder.param("phased", "true");

        builder.param("phasingVotingModel", votingModel.getCode());

        builder.param("phasingFinishHeight", Nxt.getBlockchain().getHeight() + finishAfter);

        builder.param("phasingHolding", holdingId);

        if (quorum != null) {
            builder.param("phasingQuorum", quorum);
        }

        if (minBalance != null) {
            builder.param("phasingMinBalance", minBalance);
        }

        if (minBalanceModel != null) {
            builder.param("phasingMinBalanceModel", minBalanceModel.getCode());
        }

        if (whitelist != null) {
            builder.param("phasingWhitelisted", Arrays.stream(whitelist).mapToObj(Long::toUnsignedString).toArray(String[]::new));
        }
    }

    private String getPhasedTransaction(JSONObject sendMoneyJSON) {
        return ((JSONObject)sendMoneyJSON.get("transactionJSON")).get("chain") + ":" + sendMoneyJSON.get("fullHash");
    }

    private JSONObject buildMaxFeesJSON(int chainId, long fees) {
        JSONObject result = new JSONObject();
        result.put("" + chainId, Long.toUnsignedString(fees));
        return result;
    }
}
