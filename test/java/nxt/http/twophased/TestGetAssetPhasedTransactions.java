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

package nxt.http.twophased;

import nxt.BlockchainTest;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.voting.VoteWeighting;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Base64;

public class TestGetAssetPhasedTransactions extends BlockchainTest {

    static APICall phasedTransactionsApiCall(String asset) {
        return new APICall.Builder("getAssetPhasedTransactions")
                .param("asset", asset)
                .param("firstIndex", 0)
                .param("lastIndex", 10)
                .build();
    }

    private APICall byAssetApiCall(String asset) {
        return new TestCreateTwoPhased.TwoPhasedMoneyTransferBuilder()
                .votingModel(VoteWeighting.VotingModel.ASSET.getCode())
                .holding(Convert.parseUnsignedLong(asset))
                .minBalance(1, VoteWeighting.MinBalanceModel.ASSET.getCode())
                .fee(21 * ChildChain.IGNIS.ONE_COIN)
                .build();
    }


    @Test
    public void simpleTransactionLookup() {
        String asset = issueTestAsset();
        JSONObject transactionJSON = TestCreateTwoPhased.issueCreateTwoPhased(byAssetApiCall(asset), false);

        JSONObject response = phasedTransactionsApiCall(asset).invoke();
        Logger.logMessage("getAssetPhasedTransactionsResponse:" + response.toJSONString());
        JSONArray transactionsJson = (JSONArray) response.get("transactions");
        Assert.assertTrue(TwoPhasedSuite.searchForTransactionId(transactionsJson, (String) transactionJSON.get("fullHash")));
    }

    @Test
    public void sorting() {
        String asset = issueTestAsset();

        for (int i = 0; i < 15; i++) {
            TestCreateTwoPhased.issueCreateTwoPhased(byAssetApiCall(asset), false);
        }

        JSONObject response = phasedTransactionsApiCall(asset).invoke();
        Logger.logMessage("getAssetPhasedTransactionsResponse:" + response.toJSONString());
        JSONArray transactionsJson = (JSONArray) response.get("transactions");

        //sorting check
        int prevHeight = Integer.MAX_VALUE;
        for (Object transactionsJsonObj : transactionsJson) {
            JSONObject transactionObject = (JSONObject) transactionsJsonObj;
            int height = ((Long) transactionObject.get("height")).intValue();
            Assert.assertTrue(height <= prevHeight);
            prevHeight = height;
        }
    }

    private String issueTestAsset() {
        String name = "lz1cdqGYD";
        APICall apiCall = new APICall.Builder("issueAsset")
                .param("secretPhrase", RIKER.getSecretPhrase())
                .param("name", name)
                .param("description", "asset testing")
                .param("quantityQNT", 10000000)
                .param("decimals", 4)
                .param("feeNQT", 1000 * ChildChain.IGNIS.ONE_COIN)
                .param("deadline", 1440)
                .build();
        JSONObject response;
        apiCall.invoke();
        BlockchainTest.generateBlock();

        apiCall = new APICall.Builder("searchAssets")
                .param("query", name)
                .build();
        response = apiCall.invoke();
        JSONArray assets = (JSONArray) response.get("assets");
        return (String) ((JSONObject)assets.get(0)).get("asset");
    }
}
