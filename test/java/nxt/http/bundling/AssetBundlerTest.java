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

import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.http.assetexchange.AssetExchangeTest;
import nxt.http.client.PlaceAssetOrderBuilder;
import nxt.http.client.PlaceAssetOrderBuilder.PlaceOrderResult;
import nxt.util.JSONAssert;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class AssetBundlerTest extends BundlerTest {

    @Test
    public void testAssetTransferBundler() {
        String assetId = issueAsset();
        String assetId1 = issueAsset();

        startAssetBundler(assetId);

        String fullHash = AssetExchangeTest.transfer(assetId, ALICE, CHUCK, 10, 0).getFullHash();
        Assert.assertTrue(isBundled(fullHash));

        fullHash = AssetExchangeTest.transfer(assetId1, ALICE, CHUCK, 10, 0).getFullHash();
        Assert.assertFalse(isBundled(fullHash));
    }

    @Test
    public void testAssetBidBundler() {
        String assetId = issueAsset();
        startAssetBundler(assetId);

        String fullHash = placeAssetOrder(ALICE, assetId, 10, ChildChain.IGNIS.ONE_COIN, false);
        Assert.assertTrue(isBundled(fullHash));

        fullHash = placeAssetOrder(CHUCK, assetId, 10, ChildChain.IGNIS.ONE_COIN, true);
        Assert.assertTrue(isBundled(fullHash));

        fullHash = placeAssetOrder(ALICE, assetId, 10, ChildChain.IGNIS.ONE_COIN * 2, false);
        Assert.assertTrue(isBundled(fullHash));
        String askId = Tester.hexFullHashToStringId(fullHash);

        //Won't match the ask order
        fullHash = placeAssetOrder(CHUCK, assetId, 10, ChildChain.IGNIS.ONE_COIN, true);
        Assert.assertTrue(isBundled(fullHash));
        String bidId = Tester.hexFullHashToStringId(fullHash);

        fullHash = cancelAssetOrder(ALICE, askId, false);
        Assert.assertTrue(isBundled(fullHash));

        fullHash = cancelAssetOrder(CHUCK, bidId, true);
        Assert.assertTrue(isBundled(fullHash));
    }

    @Test
    public void testAssetBundlerWithQuota() {
        String assetId = issueAsset();

        int quota = 4;
        JSONAssert result = new JSONAssert(new APICall.Builder("startBundler").
                secretPhrase(ALICE.getSecretPhrase()).
                param("chain", ChildChain.IGNIS.getId()).
                param("filter", new String[] {"AssetBundler:" + assetId, "QuotaBundler:" + quota}).
                param("minRateNQTPerFXT", 0).
                param("feeCalculatorName", "MIN_FEE").
                build().invoke());
        result.str("totalFeesLimitFQT");

        String fullHash = AssetExchangeTest.transfer(assetId, ALICE, BOB, 100, 0).getFullHash();
        Assert.assertTrue(isBundled(fullHash));

        for (int i = 0; i < quota; i++) {
            fullHash = AssetExchangeTest.transfer(assetId, BOB, CHUCK, 10, 0).getFullHash();
            Assert.assertTrue(isBundled(fullHash));
        }
        //Bob's quota is over
        fullHash = AssetExchangeTest.transfer(assetId, BOB, CHUCK, 10, 0).getFullHash();
        Assert.assertFalse(isBundled(fullHash));

        //Chuck still has quota
        fullHash = AssetExchangeTest.transfer(assetId, CHUCK, BOB, 10, 0).getFullHash();
        Assert.assertTrue(isBundled(fullHash));

        //Transferring to unknown account is not allowed
        fullHash = AssetExchangeTest.transfer(assetId, CHUCK, new Tester("Unknown account secret " + System.currentTimeMillis()), 10, 0).getFullHash();
        Assert.assertFalse(isBundled(fullHash));
    }

    private String placeAssetOrder(Tester sender, String assetId, long quantityQNT, long price, boolean isBid) {
        PlaceAssetOrderBuilder builder = new PlaceAssetOrderBuilder(sender, assetId, quantityQNT, price);
        PlaceOrderResult result = isBid ? builder.placeBidOrder() : builder.placeAskOrder();
        generateBlock();
        return result.getFullHash();
    }

    private String cancelAssetOrder(Tester sender, String orderId, boolean isBid) {
        String result = new JSONAssert(new APICall.Builder(isBid ? "cancelBidOrder" : "cancelAskOrder")
                .param("secretPhrase", sender.getSecretPhrase())
                .param("order", orderId)
                .param("feeNQT", 0)
                .build().invoke()).str("fullHash");
        generateBlock();
        return result;
    }


    private void startAssetBundler(String assetId) {
        JSONAssert result = new JSONAssert(new APICall.Builder("startBundler").
                secretPhrase(BOB.getSecretPhrase()).
                param("chain", ChildChain.IGNIS.getId()).
                param("filter", "AssetBundler:" + assetId).
                param("minRateNQTPerFXT", 0).
                param("feeCalculatorName", "MIN_FEE").
                build().invoke());
        result.str("totalFeesLimitFQT");
    }

    private String issueAsset() {
        JSONAssert result = new JSONAssert(new APICall.Builder("issueAsset")
                .param("secretPhrase", ALICE.getSecretPhrase())
                .param("name", "Bundl")
                .param("description", "asset bundle testing")
                .param("quantityQNT", 10000000)
                .param("decimals", 4)
                .param("feeNQT", 1000 * ChildChain.IGNIS.ONE_COIN)
                .param("deadline", 1440)
                .build().invoke());
        String fullHash = result.str("fullHash");
        String assetId = Tester.hexFullHashToStringId(fullHash);

        bundleTransactions(Collections.singletonList(fullHash));

        generateBlock();
        return assetId;
    }
}
