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

package nxt.http.monetarysystem;

import nxt.BlockchainTest;
import nxt.account.Account;
import nxt.blockchain.ChildChain;
import nxt.crypto.Crypto;
import nxt.http.APICall;
import nxt.ms.CurrencyType;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestCurrencyReserveAndClaim extends BlockchainTest {

    @Test
    public void reserveIncrease() {
        APICall apiCall = new TestCurrencyIssuance.Builder().
                type(CurrencyType.RESERVABLE.getCode() | CurrencyType.EXCHANGEABLE.getCode()).
                issuanceHeight(baseHeight + 5).
                minReservePerUnitNQT((long) 1).
                initialSupply((long)0).
                reserveSupply((long)100000).
                build();
        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall);
        reserveIncreaseImpl(currencyId, ALICE.getSecretPhrase(), BOB.getSecretPhrase());
    }

    @Test
    public void cancelCrowdFunding() {
        APICall apiCall1 = new TestCurrencyIssuance.Builder().
                type(CurrencyType.RESERVABLE.getCode() | CurrencyType.EXCHANGEABLE.getCode()).
                issuanceHeight(baseHeight + 4).
                minReservePerUnitNQT((long) 11).
                initialSupply((long)0).
                reserveSupply((long)100000).
                build();
        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall1);
        long balanceNQT1 = ALICE.getChainBalance(ChildChain.IGNIS.getId());
        long balanceNQT2 = BOB.getChainBalance(ChildChain.IGNIS.getId());
        reserveIncreaseImpl(currencyId, ALICE.getSecretPhrase(), BOB.getSecretPhrase());
        generateBlock(); // cancellation of crowd funding because of insufficient funds
        APICall apiCall = new APICall.Builder("getCurrencyFounders").
                param("currency", currencyId).
                build();
        JSONObject getFoundersResponse = apiCall.invoke();
        Logger.logMessage("getFoundersResponse: " + getFoundersResponse);
        Assert.assertTrue(((JSONArray)getFoundersResponse.get("founders")).size() == 0);
        Assert.assertEquals(balanceNQT1 - ChildChain.IGNIS.ONE_COIN, ALICE.getChainBalance(ChildChain.IGNIS.getId()));
        Assert.assertEquals(balanceNQT2 - 2* ChildChain.IGNIS.ONE_COIN, BOB.getChainBalance(ChildChain.IGNIS.getId()));
    }

    @Test
    public void crowdFundingDistribution() {
        APICall apiCall = new TestCurrencyIssuance.Builder().
                type(CurrencyType.RESERVABLE.getCode() | CurrencyType.EXCHANGEABLE.getCode()).
                initialSupply((long) 0).
                reserveSupply((long) 100000).
                issuanceHeight(baseHeight + 4).
                minReservePerUnitNQT((long) 10).
                build();

        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall);
        long balanceNQT1 = ALICE.getChainBalance(ChildChain.IGNIS.getId());
        long balanceNQT2 = BOB.getChainBalance(ChildChain.IGNIS.getId());
        reserveIncreaseImpl(currencyId, ALICE.getSecretPhrase(), BOB.getSecretPhrase());
        generateBlock(); // distribution of currency to founders
        Assert.assertEquals(20000, ALICE.getCurrencyUnits(Convert.parseAccountId(currencyId)));
        Assert.assertEquals(80000, BOB.getCurrencyUnits(Convert.parseAccountId(currencyId)));
        Assert.assertEquals(balanceNQT1 - ChildChain.IGNIS.ONE_COIN - 200000 + (100000*10), ALICE.getChainBalance(ChildChain.IGNIS.getId()));
        Assert.assertEquals(balanceNQT2 - 2*ChildChain.IGNIS.ONE_COIN - 800000, BOB.getChainBalance(ChildChain.IGNIS.getId()));
    }

    @Test
    public void crowdFundingDistributionRounding() {
        APICall apiCall = new TestCurrencyIssuance.Builder().
                type(CurrencyType.RESERVABLE.getCode() | CurrencyType.EXCHANGEABLE.getCode()).
                initialSupply((long)0).
                reserveSupply((long)24).
                maxSupply((long) 24).
                issuanceHeight(baseHeight + 4).
                minReservePerUnitNQT((long) 10).
                build();

        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall);
        long balanceNQT1 = ALICE.getChainBalance(ChildChain.IGNIS.getId());
        long balanceNQT2 = BOB.getChainBalance(ChildChain.IGNIS.getId());
        long balanceNQT3 = CHUCK.getChainBalance(ChildChain.IGNIS.getId());
        reserveIncreaseImpl(currencyId, BOB.getSecretPhrase(), CHUCK.getSecretPhrase());
        generateBlock(); // distribution of currency to founders

        // account 2 balance round(24 * 0.2) = round(4.8) = 4
        // account 3 balance round(24 * 0.8) = round(19.2) = 19
        // issuer receives the leftover of 1
        Assert.assertEquals(4, BOB.getCurrencyUnits(Convert.parseAccountId(currencyId)));
        Assert.assertEquals(19, CHUCK.getCurrencyUnits(Convert.parseAccountId(currencyId)));
        Assert.assertEquals(1, ALICE.getCurrencyUnits(Convert.parseAccountId(currencyId)));
        Assert.assertEquals(balanceNQT1 + 24 * 10, ALICE.getChainBalance(ChildChain.IGNIS.getId()));
        Assert.assertEquals(balanceNQT2 - ChildChain.IGNIS.ONE_COIN - 24 * 2, BOB.getChainBalance(ChildChain.IGNIS.getId()));
        Assert.assertEquals(balanceNQT3 - 2 * ChildChain.IGNIS.ONE_COIN - 24 * 8, CHUCK.getChainBalance(ChildChain.IGNIS.getId()));

        apiCall = new APICall.Builder("getCurrency").
                param("currency", currencyId).
                build();
        JSONObject response = apiCall.invoke();
        Assert.assertEquals("24", response.get("currentSupplyQNT"));
    }

    private void reserveIncreaseImpl(String currencyId, String secret1, String secret2) {
        APICall apiCall = new APICall.Builder("currencyReserveIncrease").
                secretPhrase(secret1).
                feeNQT(ChildChain.IGNIS.ONE_COIN).
                param("currency", currencyId).
                param("amountPerUnitNQT", "" + 2).
                build();
        JSONObject reserveIncreaseResponse = apiCall.invoke();
        Logger.logMessage("reserveIncreaseResponse: " + reserveIncreaseResponse);
        generateBlock();

        // Two increase reserve transactions in the same block
        apiCall = new APICall.Builder("currencyReserveIncrease").
                secretPhrase(secret2).
                feeNQT(ChildChain.IGNIS.ONE_COIN).
                param("currency", currencyId).
                param("amountPerUnitNQT", "" + 3).
                build();
        reserveIncreaseResponse = apiCall.invoke();
        Logger.logMessage("reserveIncreaseResponse: " + reserveIncreaseResponse);

        apiCall = new APICall.Builder("currencyReserveIncrease").
                secretPhrase(secret2).
                feeNQT(ChildChain.IGNIS.ONE_COIN).
                param("currency", currencyId).
                param("amountPerUnitNQT", "" + 5).
                build();
        reserveIncreaseResponse = apiCall.invoke();
        Logger.logMessage("reserveIncreaseResponse: " + reserveIncreaseResponse);

        generateBlock();

        apiCall = new APICall.Builder("getCurrencyFounders").
                param("currency", currencyId).
                build();
        JSONObject getFoundersResponse = apiCall.invoke();
        Logger.logMessage("getFoundersResponse: " + getFoundersResponse);

        JSONArray founders = (JSONArray)getFoundersResponse.get("founders");
        JSONObject founder1 = (JSONObject)founders.get(0);
        Assert.assertTrue(Long.toUnsignedString(Account.getId(Crypto.getPublicKey(secret1))).equals(founder1.get("account")) ||
                Long.toUnsignedString(Account.getId(Crypto.getPublicKey(secret2))).equals(founder1.get("account")));
        Assert.assertTrue(String.valueOf(3L + 5L).equals(founder1.get("amountPerUnitNQT")) || String.valueOf(2L).equals(founder1.get("amountPerUnitNQT")));

        JSONObject founder2 = (JSONObject)founders.get(1);
        Assert.assertTrue(Long.toUnsignedString(Account.getId(Crypto.getPublicKey(secret1))).equals(founder2.get("account")) ||
                Long.toUnsignedString(Account.getId(Crypto.getPublicKey(secret2))).equals(founder2.get("account")));
        Assert.assertTrue(String.valueOf(3L + 5L).equals(founder2.get("amountPerUnitNQT")) || String.valueOf(2L).equals(founder2.get("amountPerUnitNQT")));
    }

}
