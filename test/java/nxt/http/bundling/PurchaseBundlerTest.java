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
import nxt.http.APICall;
import nxt.util.JSONAssert;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class PurchaseBundlerTest extends BundlerTest {
    @Test
    public void testPurchaseAndFeedback() {
        startPurchaseBundler(ALICE);

        long price = ChildChain.IGNIS.ONE_COIN;
        JSONAssert result = new JSONAssert(new APICall.Builder("dgsListing")
                .secretPhrase(ALICE.getSecretPhrase())
                .feeNQT(0)
                .param("name", "TestDGS")
                .param("quantity", "10")
                .param("priceNQT", price).build().invoke());
        String goodsId = result.id();

        bundleTransactions(Collections.singletonList(result.fullHash()));

        generateBlock();

        result = new JSONAssert(new APICall.Builder("dgsPurchase")
                .secretPhrase(CHUCK.getSecretPhrase())
                .feeNQT(0)
                .param("goods", goodsId)
                .param("priceNQT", price)
                .param("quantity", 1)
                .param("deliveryDeadlineTimestamp", Nxt.getEpochTime() + 100)
                .build().invoke());
        String purchaseId = result.id();
        generateBlock();
        Assert.assertTrue(isBundled(result.fullHash()));

        result = new JSONAssert(new APICall.Builder("dgsDelivery")
                .secretPhrase(ALICE.getSecretPhrase())
                .feeNQT(0)
                .param("purchase", purchaseId)
                .param("goodsToEncrypt", "a")
                .build().invoke());
        bundleTransactions(Collections.singletonList(result.fullHash()));
        generateBlock();

        result = new JSONAssert(new APICall.Builder("dgsFeedback")
                .secretPhrase(CHUCK.getSecretPhrase())
                .feeNQT(0)
                .param("purchase", purchaseId)
                .param("message", "my feedback 123")
                .build().invoke());
        generateBlock();
        Assert.assertTrue(isBundled(result.fullHash()));
    }

    private void startPurchaseBundler(Tester seller) {
        JSONAssert result = new JSONAssert(new APICall.Builder("startBundler").
                secretPhrase(BOB.getSecretPhrase()).
                param("chain", ChildChain.IGNIS.getId()).
                param("filter", "PurchaseBundler:" + seller.getStrId()).
                param("minRateNQTPerFXT", 0).
                param("feeCalculatorName", "MIN_FEE").
                build().invoke());
        result.str("totalFeesLimitFQT");
    }

}
