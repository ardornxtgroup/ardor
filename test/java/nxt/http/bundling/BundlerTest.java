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

import nxt.BlockchainTest;
import nxt.Constants;
import nxt.Nxt;
import nxt.Tester;
import nxt.account.PaymentTransactionType;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildChain;
import nxt.blockchain.FxtChain;
import nxt.http.APICall;
import nxt.http.assetexchange.AssetExchangeTest;
import nxt.util.Convert;
import nxt.util.JSON;
import nxt.util.JSONAssert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BundlerTest extends BlockchainTest {
    @BeforeClass
    public static void init() {
        initNxt(Collections.emptyMap());
        initBlockchainTest();
        for (Chain chain : ChildChain.getAll()) {
            JSONObject response = new APICall.Builder("stopBundler").
                    secretPhrase(FORGY.getSecretPhrase()).
                    param("chain", chain.getId()).
                    build().invoke();
            Assert.assertEquals(Boolean.TRUE, response.get("stoppedBundler"));
        }
    }

    @After
    public void destroy() {
        super.destroy();
        new APICall.Builder("stopBundler").
                param("chain", ChildChain.IGNIS.getId()).
                build().invoke();
    }


    protected long startTwoRulesBundler(long publicRate, long personalBundlerOverpay) {
        JSONArray rulesArray = new JSONArray();
        JSONObject rule = new JSONObject();

        JSONObject filterJson = new JSONObject();
        filterJson.put("name", "PersonalBundler");
        JSONArray filtersJson = new JSONArray();
        filtersJson.add(filterJson);

        rule.put("filters", filtersJson);
        rule.put("feeCalculatorName", "MIN_FEE");
        rule.put("minRateNQTPerFXT", "0");
        rule.put("overpayFQTPerFXT", personalBundlerOverpay);
        rulesArray.add(rule);

        rule = new JSONObject();
        rule.put("feeCalculatorName", "MIN_FEE");
        rule.put("minRateNQTPerFXT", Long.toUnsignedString(publicRate));
        rulesArray.add(rule);

        JSONAssert result = new JSONAssert(new APICall.Builder("startBundler").
                secretPhrase(BOB.getSecretPhrase()).
                param("chain", ChildChain.IGNIS.getId()).
                param("bundlingRulesJSON", JSON.toString(rulesArray)).
                build().invoke());
        result.str("totalFeesLimitFQT");
        return publicRate;
    }

    protected long getMinFeeNQT(long rate) {
        return BigInteger.valueOf(getMinFeeFQT()).multiply(BigInteger.valueOf(rate)).
                divide(BigInteger.valueOf(FxtChain.FXT.ONE_COIN)).longValueExact();
    }

    protected long getMinFeeFQT() {
        return PaymentTransactionType.ORDINARY.getBaselineFee(null).getFee(null, null);
    }

    protected boolean bundleTransaction(Tester sender, long fee) {
        String fullHash = createTransaction(sender, fee, null);
        generateBlock();
        return isBundled(fullHash);
    }

    protected boolean isBundled(String fullHash) {
        JSONAssert result = new JSONAssert(new APICall.Builder("getTransaction").param("fullHash", fullHash).
                build().invoke());
        Object errorDescription = result.getJson().get("errorDescription");
        if ("Unknown transaction".equals(errorDescription)) {
            return false;
        }
        return Nxt.getBlockchain().getHeight() == result.integer("height");
    }

    protected String createTransaction(Tester sender, long fee, String message) {
        APICall.Builder builder = new APICall.Builder("sendMoney").secretPhrase(sender.getSecretPhrase()).
                recipient(DAVE.getId()).param("amountNQT", 10 * ChildChain.IGNIS.ONE_COIN).
                param("feeNQT", fee);
        if (message != null) {
            builder.param("message", message);
        }
        JSONAssert result = new JSONAssert(builder.build().invoke());
        return result.str("fullHash");
    }
}
