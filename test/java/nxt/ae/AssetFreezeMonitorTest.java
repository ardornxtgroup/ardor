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

package nxt.ae;

import nxt.BlockchainTest;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.db.TransactionalDb;
import nxt.http.APICall;
import nxt.http.assetexchange.AssetExchangeTest;
import nxt.http.client.GetAccountCurrentOrderIdsBuilder;
import nxt.http.client.PlaceAssetOrderBuilder;
import nxt.http.client.SetAssetPropertyBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static nxt.ae.Asset.ASSET_FREEZE_HEIGHT_PROPERTY;
import static org.junit.Assert.assertEquals;

public class AssetFreezeMonitorTest extends BlockchainTest {

    @Test
    public void testOnBlockFreezeAsset() {
        Asset expectedToFreeze = createAssetWithOrders();

        setAssetFreezeHeight(expectedToFreeze, getHeight() + 1);

        generateBlock();

        assertFrozen(expectedToFreeze);
    }

    @Test
    public void testOnBlockFreezeAssetOnPropertySet() {
        Asset expectedToFreeze = createAssetWithOrders();

        setAssetFreezeHeight(expectedToFreeze, 0);
        setAssetFreezeProperty(expectedToFreeze, getHeight() + 2);

        generateBlock();
        generateBlock();

        assertFrozen(expectedToFreeze);
    }

    private void setAssetFreezeProperty(Asset asset, int height) {
        new SetAssetPropertyBuilder(ALICE, asset.getId(), ASSET_FREEZE_HEIGHT_PROPERTY, String.valueOf(height))
                .invokeNoError();
    }

    @Test
    public void testOnBlockTooEarlyToFreezeAsset() {
        Asset tooEarlyToFreeze = createAssetWithOrders();

        setAssetFreezeHeight(tooEarlyToFreeze, getHeight() + 2);

        generateBlock();

        assertLiquid(tooEarlyToFreeze);
    }

    @Test
    public void testOnBlockNoReasonToFreezeAsset() {
        Asset noReasonToFreeze = createAssetWithOrders();

        generateBlock();

        assertLiquid(noReasonToFreeze);
    }

    @Test
    public void testOnBlockTooLateToFreezeAsset() {
        Asset tooLateToFreeze = createAssetWithOrders();

        setAssetFreezeHeight(tooLateToFreeze, getHeight());

        generateBlock();

        assertHasOrders(tooLateToFreeze);
    }

    @Test
    public void testFrozenAssetBidOrderRejected() {
        Asset asset = createFrozenAsset();
        assetTransactionsImpossible(asset);
    }

    private void assetTransactionsImpossible(Asset asset) {
        long assetId = asset.getId();

        APICall.InvocationError error = new PlaceAssetOrderBuilder(ALICE, assetId, 10, ChildChain.IGNIS.ONE_COIN * 2)
                .setFeeNQT(ChildChain.IGNIS.ONE_COIN)
                .placeAskOrderWithError();
        assertEquals("Asset " + Long.toUnsignedString(assetId) + " is frozen, no transaction is possible.", error.getErrorDescription());
    }

    private void assetTransactionsPossible(Asset asset) {
        long assetId = asset.getId();

        new PlaceAssetOrderBuilder(ALICE, assetId, 10, ChildChain.IGNIS.ONE_COIN * 2)
                .setFeeNQT(ChildChain.IGNIS.ONE_COIN)
                .placeAskOrder();
    }

    private Asset createFrozenAsset() {
        Asset asset = Asset.getAsset(AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetId());
        setAssetFreezeHeight(asset, getHeight() - 1);
        return asset;
    }

    private Asset createAssetWithOrders() {
        long assetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetId();
        new PlaceAssetOrderBuilder(ALICE, assetId, 10, ChildChain.IGNIS.ONE_COIN * 2)
                .setFeeNQT(ChildChain.IGNIS.ONE_COIN)
                .placeAskOrder();
        new PlaceAssetOrderBuilder(CHUCK, assetId, 5, ChildChain.IGNIS.ONE_COIN)
                .setFeeNQT(ChildChain.IGNIS.ONE_COIN)
                .placeBidOrder();
        generateBlock();
        return Asset.getAsset(assetId);
    }

    public static void setAssetFreezeHeight(Asset asset, int height) {
        TransactionalDb.runInDbTransaction(() -> AssetFreezeMonitor.enableFreeze(asset.getId(), 1, height));
    }

    private void assertLiquid(Asset asset) {
        assertHasOrders(asset);
        assetTransactionsPossible(asset);
    }

    private void assertHasOrders(Asset asset) {
        List<String> actualOrders = getOrders(asset);
        assertEquals("Actual order ids: " + actualOrders, 2, actualOrders.size());
    }

    private void assertFrozen(Asset asset) {
        assertHasNoOrders(asset);
        assetTransactionsImpossible(asset);
    }

    private void assertHasNoOrders(Asset asset) {
        assertEquals(emptyList(), getOrders(asset));
    }

    private List<String> getOrders(Asset asset) {
        ArrayList<String> result = new ArrayList<>();

        for (Tester tester : Arrays.asList(ALICE, CHUCK)) {
            GetAccountCurrentOrderIdsBuilder requestBuilder = new GetAccountCurrentOrderIdsBuilder(tester.getId()).setAssetId(asset.getId());
            result.addAll(requestBuilder.getAskOrders());
            result.addAll(requestBuilder.getBidOrders());
        }
        return result;
    }
}