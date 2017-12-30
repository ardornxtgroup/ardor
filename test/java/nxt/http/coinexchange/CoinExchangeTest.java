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

package nxt.http.coinexchange;

import nxt.BlockchainTest;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static nxt.blockchain.ChildChain.AEUR;
import static nxt.blockchain.ChildChain.IGNIS;

public class CoinExchangeTest extends BlockchainTest {

    @Test
    public void simpleExchange() {
        // Want to buy 25 AEUR with a maximum price of 4 IGNIS per AEUR
        // Convert the amount to IGNIS
        long displayAEURAmount = 25;
        long quantityQNT = displayAEURAmount * AEUR.ONE_COIN;
        long displayIgnisPerAEURPrice = 4;
        long priceNQT = displayIgnisPerAEURPrice * IGNIS.ONE_COIN;

        // Submit request to buy 25 AEUR with a maximum price of 4 IGNIS per AEUR
        // Quantity is denominated in AEUR and price is denominated in IGNIS per whole AEUR
        APICall apiCall = new APICall.Builder("exchangeCoins").
                secretPhrase(ALICE.getSecretPhrase()).
                param("feeRateNQTPerFXT", IGNIS.ONE_COIN).
                param("chain", IGNIS.getId()).
                param("exchange", AEUR.getId()).
                param("quantityQNT", quantityQNT).
                param("priceNQTPerCoin", priceNQT).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logDebugMessage("exchangeCoins: " + response);
        generateBlock();

        JSONObject transactionJSON = (JSONObject)response.get("transactionJSON");
        String orderId = Tester.responseToStringId(transactionJSON);
        apiCall = new APICall.Builder("getCoinExchangeOrder").
                param("order", orderId).
                build();
        response = apiCall.invoke();
        Assert.assertEquals(Long.toString(25 * AEUR.ONE_COIN), response.get("quantityQNT"));
        Assert.assertEquals(Long.toString(4 * IGNIS.ONE_COIN), response.get("bidNQTPerCoin"));
        Assert.assertEquals(Long.toString((long)(1.0 / 4 * AEUR.ONE_COIN)), response.get("askNQTPerCoin"));

        // Want to buy 110 IGNIS with a maximum price of 1/4 AEUR per IGNIS
        // Quantity is denominated in IGNIS price is denominated in AEUR per whole IGNIS
        apiCall = new APICall.Builder("exchangeCoins").
                secretPhrase(BOB.getSecretPhrase()).
                param("feeRateNQTPerFXT", AEUR.ONE_COIN).
                param("chain", AEUR.getId()).
                param("exchange", IGNIS.getId()).
                param("quantityQNT", 100 * IGNIS.ONE_COIN + 10 * IGNIS.ONE_COIN).
                param("priceNQTPerCoin", AEUR.ONE_COIN / 4).
                build();
        response = apiCall.invoke();
        Logger.logDebugMessage("exchangeCoins: " + response);
        generateBlock();

        transactionJSON = (JSONObject)response.get("transactionJSON");
        orderId = Tester.responseToStringId(transactionJSON);
        apiCall = new APICall.Builder("getCoinExchangeOrder").
                param("order", orderId).
                build();
        response = apiCall.invoke();
        Assert.assertEquals(Long.toString(10 * IGNIS.ONE_COIN), response.get("quantityQNT")); // leftover after the exchange of 100
        Assert.assertEquals(Long.toString((long) (0.25 * AEUR.ONE_COIN)), response.get("bidNQTPerCoin"));
        Assert.assertEquals(Long.toString(4 * IGNIS.ONE_COIN), response.get("askNQTPerCoin"));

        // Now look at the resulting trades
        apiCall = new APICall.Builder("getCoinExchangeTrades").
                param("chain", ChildChain.AEUR.getId()).
                param("account", BOB.getRsAccount()).
                build();
        response = apiCall.invoke();
        Logger.logDebugMessage("GetCoinExchangeTrades: " + response);

        // Bob received 100 IGNIS and paid 0.25 AEUR per IGNIS
        JSONArray trades = (JSONArray) response.get("trades");
        JSONObject trade = (JSONObject) trades.get(0);
        Assert.assertEquals(AEUR.getId(), (int)(long)trade.get("chain"));
        Assert.assertEquals(IGNIS.getId(), (int)(long)trade.get("exchange"));
        Assert.assertEquals("" + (100 * IGNIS.ONE_COIN), trade.get("quantityQNT")); // IGNIS bought
        Assert.assertEquals("" + (long)(0.25 * AEUR.ONE_COIN), trade.get("priceNQTPerCoin")); // AEUR per IGNIS price

        apiCall = new APICall.Builder("getCoinExchangeTrades").
                param("chain", IGNIS.getId()).
                param("account", ALICE.getRsAccount()).
                build();
        response = apiCall.invoke();
        Logger.logDebugMessage("GetCoinExchangeTrades: " + response);

        // Alice received 25 AEUR and paid 4 IGNIS per AEUR
        trades = (JSONArray) response.get("trades");
        trade = (JSONObject) trades.get(0);
        Assert.assertEquals(IGNIS.getId(), (int)(long)trade.get("chain"));
        Assert.assertEquals(AEUR.getId(), (int)(long)trade.get("exchange"));
        Assert.assertEquals("" + (25 * AEUR.ONE_COIN), trade.get("quantityQNT")); // AEUR bought
        Assert.assertEquals("" + (4 * IGNIS.ONE_COIN), trade.get("priceNQTPerCoin")); // IGNIS per AEUR price

        Assert.assertEquals(-100 * IGNIS.ONE_COIN - IGNIS.ONE_COIN / 10, ALICE.getChainBalanceDiff(IGNIS.getId()));
        Assert.assertEquals(25 * AEUR.ONE_COIN, ALICE.getChainBalanceDiff(AEUR.getId()));
        Assert.assertEquals(100 * IGNIS.ONE_COIN, BOB.getChainBalanceDiff(IGNIS.getId()));
        Assert.assertEquals(-25 * AEUR.ONE_COIN - AEUR.ONE_COIN / 10, BOB.getChainBalanceDiff(AEUR.getId()));
    }

    @Test
    public void ronsSample() {
        long AEURToBuy = 5 * AEUR.ONE_COIN;
        long ignisPerWholeAEUR = (long) (0.75 * IGNIS.ONE_COIN);

        APICall apiCall = new APICall.Builder("exchangeCoins").
                secretPhrase(ALICE.getSecretPhrase()).
                param("feeRateNQTPerFXT", IGNIS.ONE_COIN).
                param("chain", IGNIS.getId()).
                param("exchange", AEUR.getId()).
                param("quantityQNT", AEURToBuy).
                param("priceNQTPerCoin", ignisPerWholeAEUR).
                build();
        JSONObject response = apiCall.invoke();
        String aliceOrder = Tester.responseToStringId(response);
        generateBlock();

        long ignisToBuy = 5 * IGNIS.ONE_COIN;
        long AEURPerWholeIgnis = (long) (1.35 * AEUR.ONE_COIN);

        apiCall = new APICall.Builder("exchangeCoins").
                secretPhrase(BOB.getSecretPhrase()).
                param("feeRateNQTPerFXT", AEUR.ONE_COIN).
                param("chain", AEUR.getId()).
                param("exchange", IGNIS.getId()).
                param("quantityQNT", ignisToBuy).
                param("priceNQTPerCoin", AEURPerWholeIgnis).
                build();
        response = apiCall.invoke();
        String bobOrder = Tester.responseToStringId(response);
        generateBlock();

        Assert.assertEquals((long)(-3.75 * IGNIS.ONE_COIN) - IGNIS.ONE_COIN / 10, ALICE.getChainBalanceDiff(IGNIS.getId()));
        Assert.assertEquals(5 * AEUR.ONE_COIN, ALICE.getChainBalanceDiff(AEUR.getId()));
        Assert.assertEquals((long)(3.75 * IGNIS.ONE_COIN), BOB.getChainBalanceDiff(IGNIS.getId()));
        Assert.assertEquals(-5 * AEUR.ONE_COIN - AEUR.ONE_COIN / 10, BOB.getChainBalanceDiff(AEUR.getId()));

        apiCall = new APICall.Builder("getCoinExchangeOrder").
                param("order", aliceOrder).
                build();
        response = apiCall.invoke();
        Assert.assertEquals(5L, response.get("errorCode"));

        apiCall = new APICall.Builder("getCoinExchangeOrder").
                param("order", bobOrder).
                build();
        response = apiCall.invoke();
        Assert.assertEquals((long)(1.25 * IGNIS.ONE_COIN), Long.parseLong((String) response.get("quantityQNT")));
        Assert.assertEquals((long)(1.35 * AEUR.ONE_COIN), Long.parseLong((String) response.get("bidNQTPerCoin")));
        Assert.assertEquals((long)(0.74074074 * IGNIS.ONE_COIN), Long.parseLong((String) response.get("askNQTPerCoin")));
    }

}
