/*
 * Copyright © 2013-2016 The Nxt Core Developers.
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

package nxt.http.votingsystem;

import nxt.BlockchainTest;
import nxt.Nxt;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.voting.VoteWeighting;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestCreatePoll extends BlockchainTest {

    static String issueCreatePoll(APICall apiCall, boolean shouldFail) {
        JSONObject createPollResponse = apiCall.invoke();
        Logger.logMessage("createPollResponse: " + createPollResponse.toJSONString());

        if(!shouldFail) {
            Assert.assertNull(createPollResponse.get("errorCode"));
        }

        generateBlock();

        try {
            byte[] fullHash = Convert.parseHexString((String) createPollResponse.get("fullHash"));

            if(!shouldFail && fullHash == null) Assert.fail();
            String pollId = Long.toUnsignedString(Convert.fullHashToId(fullHash));
            apiCall = new APICall.Builder("getPoll").param("poll", pollId).build();

            JSONObject getPollResponse = apiCall.invoke();
            Logger.logMessage("getPollResponse:" + getPollResponse.toJSONString());
            Assert.assertEquals(pollId, getPollResponse.get("poll"));
            return pollId;
        }catch(Throwable t){
            if(!shouldFail) Assert.fail(t.getMessage());
            return null;
        }
    }

    @Test
    public void createValidPoll() {
        APICall apiCall = new CreatePollBuilder().build();
        issueCreatePoll(apiCall, false);
        generateBlock();

        apiCall = new CreatePollBuilder().votingModel(VoteWeighting.VotingModel.COIN.getCode()).build();
        issueCreatePoll(apiCall, false);
        generateBlock();
    }

    @Test
    public void createInvalidPoll() {
        APICall apiCall = new CreatePollBuilder().minBalance(-ChildChain.IGNIS.ONE_COIN).build();
        issueCreatePoll(apiCall, true);
        generateBlock();

        apiCall = new CreatePollBuilder().minBalance(0).build();
        issueCreatePoll(apiCall, true);
        generateBlock();
    }

    public static class CreatePollBuilder extends APICall.Builder {

        public CreatePollBuilder() {
            super("createPoll");
            secretPhrase(ALICE.getSecretPhrase());
            feeNQT(10 * ChildChain.IGNIS.ONE_COIN);
            param("name", "Test1");
            param("description", "The most cool Beatles guy?");
            param("finishHeight", Nxt.getBlockchain().getHeight() + 100);
            param("votingModel", VoteWeighting.VotingModel.ACCOUNT.getCode());
            param("minNumberOfOptions", 1);
            param("maxNumberOfOptions", 2);
            param("minRangeValue", 0);
            param("maxRangeValue", 1);
            param("minBalance", 10 * ChildChain.IGNIS.ONE_COIN);
            param("minBalanceModel", VoteWeighting.MinBalanceModel.COIN.getCode());
            param("option00", "Ringo");
            param("option01", "Paul");
            param("option02", "John");
        }

        public CreatePollBuilder votingModel(byte votingModel) {
            param("votingModel", votingModel);
            return this;
        }

        public CreatePollBuilder minBalance(long minBalance) {
            param("minBalance", minBalance);
            return this;
        }

        public CreatePollBuilder minBalance(long minBalance, byte minBalanceModel) {
            param("minBalance", minBalance);
            param("minBalanceModel", minBalanceModel);
            return this;
        }

        public CreatePollBuilder minBalance(long minBalance, byte minBalanceModel, long holdingId) {
            param("minBalance", minBalance);
            param("minBalanceModel", minBalanceModel);
            param("holdingId", holdingId);
            return this;
        }
    }
}
