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

package nxt.http.bundling;

import nxt.Nxt;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.blockchain.FxtChain;
import nxt.http.APICall;
import nxt.http.monetarysystem.TestCurrencyIssuance;
import nxt.ms.CurrencyType;
import nxt.util.JSONAssert;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class CurrencyBundlerTest extends BundlerTest {

    @Test
    public void testTransfer() {
        JSONAssert result = new JSONAssert(new TestCurrencyIssuance.Builder().
                type(CurrencyType.EXCHANGEABLE.getCode()).
                initialSupply((long)100000).
                build().invoke());
        String fullHash = result.str("fullHash");
        String currencyId = bundleIssueCurrency(fullHash);

        startCurrencyBundler(currencyId);

        result = new JSONAssert(new APICall.Builder("transferCurrency").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("currency", currencyId).
                param("recipient", BOB.getStrId()).
                param("unitsQNT", "2000").
                feeNQT(0).
                build().invoke());
        fullHash = result.str("fullHash");
        generateBlock();
        Assert.assertTrue(isBundled(fullHash));
    }

    @Test
    public void testExchange() {
        JSONAssert result = new JSONAssert(new TestCurrencyIssuance.Builder().
                type(CurrencyType.EXCHANGEABLE.getCode()).
                initialSupply((long)100000).
                build().invoke());
        String fullHash = result.str("fullHash");
        String currencyId = bundleIssueCurrency(fullHash);

        startCurrencyBundler(currencyId);

        result = new JSONAssert(new APICall.Builder("publishExchangeOffer").
                secretPhrase(ALICE.getSecretPhrase()).
                feeNQT(0).
                param("deadline", "1440").
                param("currency", currencyId).
                param("buyRateNQTPerUnit", "" + 95). // buy currency for NXT
                param("sellRateNQTPerUnit", "" + 105). // sell currency for NXT
                param("totalBuyLimitQNT", "10000").
                param("totalSellLimitQNT", "5000").
                param("initialBuySupplyQNT", "1000").
                param("initialSellSupplyQNT", "500").
                param("expirationHeight", "" + Integer.MAX_VALUE).
                build().invoke());
        fullHash = result.str("fullHash");
        generateBlock();
        Assert.assertTrue(isBundled(fullHash));

        result = new JSONAssert(new APICall.Builder("currencyBuy").
                secretPhrase(BOB.getSecretPhrase()).feeNQT(0).
                param("currency", currencyId).
                param("rateNQTPerUnit", "" + 106).
                param("unitsQNT", "200").
                build().invoke());
        fullHash = result.str("fullHash");
        generateBlock();
        Assert.assertTrue(isBundled(fullHash));

        result = new JSONAssert(new APICall.Builder("currencySell").
                secretPhrase(BOB.getSecretPhrase()).feeNQT(0).
                param("currency", currencyId).
                param("rateNQTPerUnit", "" + 94).
                param("unitsQNT", "200").
                build().invoke());
        fullHash = result.str("fullHash");
        generateBlock();
        Assert.assertTrue(isBundled(fullHash));
    }

    @Test
    public void testReservable() {
        JSONAssert result = new JSONAssert(new TestCurrencyIssuance.Builder().
                type(CurrencyType.RESERVABLE.getCode() | CurrencyType.CLAIMABLE.getCode()).
                issuanceHeight(Nxt.getBlockchain().getHeight() + 5).
                minReservePerUnitNQT((long) 1).
                initialSupply((long)0).
                reserveSupply((long)100000).
                build().invoke());
        String fullHash = result.str("fullHash");
        String currencyId = bundleIssueCurrency(fullHash);

        startCurrencyBundler(currencyId);

        result = new JSONAssert(new APICall.Builder("currencyReserveIncrease").
                secretPhrase(CHUCK.getSecretPhrase()).
                feeNQT(0).
                param("currency", currencyId).
                param("amountPerUnitNQT", "" + 2).
                build().invoke());
        fullHash = result.str("fullHash");
        generateBlock();
        Assert.assertTrue(isBundled(fullHash));

        generateBlocks(5);

        result = new JSONAssert(new APICall.Builder("currencyReserveClaim").
                secretPhrase(CHUCK.getSecretPhrase()).
                feeNQT(0).
                param("currency", currencyId).
                param("unitsQNT", "2000").
                build().invoke());
        fullHash = result.str("fullHash");
        generateBlock();
        Assert.assertTrue(isBundled(fullHash));
    }

    private String bundleIssueCurrency(String fullHash) {
        String currencyId = Tester.hexFullHashToStringId(fullHash);

        bundleTransactions(Collections.singletonList(fullHash));

        generateBlock();
        return currencyId;
    }

    private void startCurrencyBundler(String currencyId) {
        JSONAssert result = new JSONAssert(new APICall.Builder("startBundler").
                secretPhrase(BOB.getSecretPhrase()).
                param("chain", ChildChain.IGNIS.getId()).
                param("filter", "CurrencyBundler:" + currencyId).
                param("minRateNQTPerFXT", 0).
                param("feeCalculatorName", "MIN_FEE").
                build().invoke());
        result.str("totalFeesLimitFQT");
    }
}
