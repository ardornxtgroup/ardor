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

package nxt.http.monetarysystem;

import nxt.BlockchainTest;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.ms.CurrencyType;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestCurrencyIssuance extends BlockchainTest {

    @Test
    public void issueCurrency() {
        issueCurrencyImpl();
    }

    public String issueCurrencyImpl() {
        APICall apiCall = new Builder().build();
        return issueCurrencyApi(apiCall);
    }

    @Test
    public void issueMultipleCurrencies() {
        APICall apiCall = new Builder().naming("axcc", "AXCC", "Currency A").build();
        issueCurrencyApi(apiCall);
        apiCall = new Builder().naming("bXbx", "BXBX", "Currency B").feeNQT(1000 * ChildChain.IGNIS.ONE_COIN).build();
        issueCurrencyApi(apiCall);
        apiCall = new Builder().naming("ccXcc", "CCCXC", "Currency C").feeNQT(40 * ChildChain.IGNIS.ONE_COIN).build();
        issueCurrencyApi(apiCall);
        apiCall = new APICall.Builder("getCurrency").param("code", "BXBX").build();
        JSONObject response = apiCall.invoke();
        Assert.assertEquals("bXbx", response.get("name"));
    }

    static String issueCurrencyApi(APICall apiCall) {
        JSONObject issueCurrencyResponse = apiCall.invoke();
        String currencyId = Tester.responseToStringId(issueCurrencyResponse);
        generateBlock();

        apiCall = new APICall.Builder("getCurrency").param("currency", currencyId).build();
        JSONObject getCurrencyResponse = apiCall.invoke();
        Assert.assertEquals(currencyId, getCurrencyResponse.get("currency"));
        return currencyId;
    }

    public static class Builder extends APICall.Builder {

        private static int[] FEE_STEPS = new int[] { 0, 0, 0, 25000, 1000, 40};

        public Builder() {
            super("issueCurrency");
            secretPhrase(ALICE.getSecretPhrase());
            chain(ChildChain.IGNIS.getId());
            param("name", "Test1");
            param("code", "TSXXX");
            param("description", "Test Currency 1");
            param("type", CurrencyType.EXCHANGEABLE.getCode());
            param("maxSupplyQNT", 100000);
            param("initialSupplyQNT", 100000);
            param("issuanceHeight", 0);
            param("algorithm", (byte)0);
            feeNQT(40 * ChildChain.IGNIS.ONE_COIN);
        }

        public Builder naming(String name, String code, String description) {
            param("name", name);
            param("code", code).
            param("description", description);
            feeNQT(FEE_STEPS[code.length()] * ChildChain.IGNIS.ONE_COIN);
            return this;
        }

        public Builder type(int type) {
            param("type", type);
            return this;
        }

        public Builder maxSupply(long maxSupply) {
            param("maxSupplyQNT", maxSupply);
            return this;
        }

        public Builder reserveSupply(long reserveSupply) {
            param("reserveSupplyQNT", reserveSupply);
            return this;
        }

        public Builder initialSupply(long initialSupply) {
            param("initialSupplyQNT", initialSupply);
            return this;
        }

        public Builder issuanceHeight(int issuanceHeight) {
            param("issuanceHeight", issuanceHeight);
            return this;
        }

        public Builder minReservePerUnitNQT(long minReservePerUnitNQT) {
            param("minReservePerUnitNQT", minReservePerUnitNQT);
            return this;
        }

        public Builder minting(byte minDifficulty, byte maxDifficulty, byte algorithm) {
            param("minDifficulty", minDifficulty);
            param("maxDifficulty", maxDifficulty);
            param("algorithm", algorithm);
            return this;
        }

    }
}
