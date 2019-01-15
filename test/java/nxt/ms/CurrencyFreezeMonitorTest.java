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

package nxt.ms;

import nxt.BlockchainTest;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.db.TransactionalDb;
import nxt.http.APICall;
import nxt.http.APICall.InvocationError;
import nxt.http.client.PublishExchangeOfferBuilder;
import nxt.http.client.SetAccountPropertyBuilder;
import nxt.http.monetarysystem.TestCurrencyIssuance;
import nxt.util.JSONAssert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CurrencyFreezeMonitorTest extends BlockchainTest {
    @Test
    public void testOnBlockFreezeCurrency() {
        Currency expectedToFreeze = createCurrencyWithOffers();

        setCurrencyFreezeHeight(expectedToFreeze, getHeight() + 1);

        generateBlock();

        assertFrozen(expectedToFreeze);
    }

    @Test
    public void testOnBlockFreezeCurrencyOnFreezeAlias() {
        Currency expectedToFreeze = createCurrencyWithOffers();

        setCurrencyFreezeHeight(expectedToFreeze, 0);

        setCurrencyFreezeAccountProperty(expectedToFreeze, getHeight() + 2);
        generateBlock();

        generateBlock();

        assertFrozen(expectedToFreeze);
    }

    private void setCurrencyFreezeAccountProperty(Currency currency, int height) {
        String name = Currency.CURRENCY_FREEZE_HEIGHT_PROPERTY_PREFIX + Long.toUnsignedString(currency.getId());
        String value = Integer.toString(height);
        ChildChain chain = ChildChain.IGNIS;
        new SetAccountPropertyBuilder(ALICE, name, value)
                .setFeeNQT(chain.ONE_COIN)
                .invokeNoError();
    }


    private void assertFrozen(Currency currency) {
        assertNoOffers(currency);
        assertTransactionsFail(currency);
    }

    private void assertTransactionsFail(Currency currency) {
        InvocationError actual = createTransferCurrencyCall(BOB, currency).invokeWithError();
        assertEquals(
                "Currency " + Long.toUnsignedString(currency.getId()) + " is frozen, no transaction is possible.",
                actual.getErrorDescription());
    }

    private static APICall createTransferCurrencyCall(Tester recipient, Currency currency) {
        return new APICall.Builder("transferCurrency")
                .param("secretPhrase", TestCurrencyIssuance.Builder.creator.getSecretPhrase())
                .param("currency", Long.toUnsignedString(currency.getId()))
                .param("recipient", recipient.getStrId())
                .param("unitsQNT", 3)
                .feeNQT(ChildChain.IGNIS.ONE_COIN)
                .build();
    }

    private void assertNoOffers(Currency currency) {
        assertEquals(new JSONArray(), getGetSellOffers(currency).get("offers"));
        assertEquals(new JSONArray(), getGetBuyOffers(currency).get("offers"));
    }

    private JSONObject getGetBuyOffers(Currency currency) {
        return getOffers("getBuyOffers", currency);
    }

    private JSONObject getGetSellOffers(Currency currency) {
        return getOffers("getSellOffers", currency);
    }

    private JSONObject getOffers(String getSellOffers, Currency currency) {
        return new APICall.Builder(getSellOffers)
                .param("currency", Long.toUnsignedString(currency.getId()))
                .build().invokeNoError();
    }

    public static void setCurrencyFreezeHeight(Currency currency, int height) {
        TransactionalDb.runInDbTransaction(() -> CurrencyFreezeMonitor.enableFreeze(currency.getId(), 1, height));
    }

    private Currency createCurrencyWithOffers() {
        Currency currency = createCurrency();
        createOffers(currency);
        return currency;
    }

    private void createOffers(Currency currency) {
        createTransferCurrencyCall(BOB, currency).invokeNoError();
        generateBlock();

        new PublishExchangeOfferBuilder(BOB, currency)
                .setInitialBuySupply(1)
                .setTotalBuyLimit(1)
                .setInitialSellSupply(1)
                .setTotalSellLimit(1)
                .setExpirationHeight(getHeight() + 100)
                .invokeNoError();

        generateBlock();
    }

    public static Currency createCurrency() {
        JSONObject jsonObject = new TestCurrencyIssuance.Builder().build().invokeNoError();
        String currencyId = Tester.hexFullHashToStringId(new JSONAssert(jsonObject).str("fullHash"));
        generateBlock();
        return Currency.getCurrency(Long.parseUnsignedLong(currencyId));
    }
}