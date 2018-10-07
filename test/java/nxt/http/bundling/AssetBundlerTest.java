package nxt.http.bundling;

import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.blockchain.FxtChain;
import nxt.http.APICall;
import nxt.http.assetexchange.AssetExchangeTest;
import nxt.util.JSONAssert;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class AssetBundlerTest extends BundlerTest {

    @Test
    public void testAssetTransferBundler() {
        String assetId = issueAsset();
        String assetId1 = issueAsset();

        startAssetBundler(assetId);

        String fullHash = new JSONAssert(AssetExchangeTest.transfer(assetId, ALICE, CHUCK, 10, 0)).str("fullHash");
        Assert.assertTrue(isBundled(fullHash));

        fullHash = new JSONAssert(AssetExchangeTest.transfer(assetId1, ALICE, CHUCK, 10, 0)).str("fullHash");
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

        String fullHash = new JSONAssert(AssetExchangeTest.transfer(assetId, ALICE, BOB, 100, 0)).fullHash();
        Assert.assertTrue(isBundled(fullHash));

        for (int i = 0; i < quota; i++) {
            fullHash = new JSONAssert(AssetExchangeTest.transfer(assetId, BOB, CHUCK, 10, 0)).fullHash();
            Assert.assertTrue(isBundled(fullHash));
        }
        //Bob's quota is over
        fullHash = new JSONAssert(AssetExchangeTest.transfer(assetId, BOB, CHUCK, 10, 0)).fullHash();
        Assert.assertFalse(isBundled(fullHash));

        //Chuck still has quota
        fullHash = new JSONAssert(AssetExchangeTest.transfer(assetId, CHUCK, BOB, 10, 0)).fullHash();
        Assert.assertTrue(isBundled(fullHash));

        //Transferring to unknown account is not allowed
        fullHash = new JSONAssert(AssetExchangeTest.transfer(assetId, CHUCK, new Tester("Unknown account secret " + System.currentTimeMillis()), 10, 0)).fullHash();
        Assert.assertFalse(isBundled(fullHash));
    }

    private String placeAssetOrder(Tester sender, String assetId, long quantityQNT, long price, boolean isBid) {
        String result = new JSONAssert(new APICall.Builder(isBid ? "placeBidOrder" : "placeAskOrder")
                .param("secretPhrase", sender.getSecretPhrase())
                .param("asset", assetId)
                .param("quantityQNT", quantityQNT)
                .param("priceNQTPerShare", price)
                .param("feeNQT", 0)
                .build().invoke()).str("fullHash");
        generateBlock();
        return result;
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
