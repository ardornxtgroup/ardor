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
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.http.monetarysystem.TestCurrencyIssuance;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.voting.VoteWeighting;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestGetCurrencyPhasedTransactions extends BlockchainTest {

    static APICall phasedTransactionsApiCall(String currency) {
        return new APICall.Builder("getCurrencyPhasedTransactions")
                .param("currency", currency)
                .param("firstIndex", 0)
                .param("lastIndex", 20)
                .build();
    }

    private APICall byCurrencyApiCall(String currency){
        return new TestCreateTwoPhased.TwoPhasedMoneyTransferBuilder()
                .votingModel(VoteWeighting.VotingModel.CURRENCY.getCode())
                .holding(Convert.parseUnsignedLong(currency))
                .minBalance(1, VoteWeighting.MinBalanceModel.CURRENCY.getCode())
                .fee(21 * ChildChain.IGNIS.ONE_COIN)
                .build();
    }

    @Test
    public void simpleTransactionLookup() {
        String currency = issueTestCurrency();
        JSONObject transactionJSON = TestCreateTwoPhased.issueCreateTwoPhased(byCurrencyApiCall(currency), false);
        JSONObject response = phasedTransactionsApiCall(currency).invoke();
        Logger.logMessage("getCurrencyPhasedTransactionsResponse:" + response.toJSONString());
        JSONArray transactionsJson = (JSONArray) response.get("transactions");
        Assert.assertTrue(TwoPhasedSuite.searchForTransactionId(transactionsJson, (String) transactionJSON.get("fullHash")));
    }

    @Test
    public void sorting() {
        String currency = issueTestCurrency();
        for (int i = 0; i < 15; i++) {
            TestCreateTwoPhased.issueCreateTwoPhased(byCurrencyApiCall(currency), false);
        }

        JSONObject response = phasedTransactionsApiCall(currency).invoke();
        Logger.logMessage("getCurrencyPhasedTransactionsResponse:" + response.toJSONString());
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

    private String issueTestCurrency() {
        String code = "JUTST";
        APICall apiCall = new TestCurrencyIssuance.Builder()
                .naming("JUnitTest", code, "tests currency")
                .build();

        apiCall.invoke();
        BlockchainTest.generateBlock();

        apiCall = new APICall.Builder("getCurrency")
                .param("code", code)
                .build();

        JSONObject result = apiCall.invoke();
        return (String) result.get("currency");
    }

}
