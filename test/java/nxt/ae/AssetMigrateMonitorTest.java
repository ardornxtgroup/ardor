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

package nxt.ae;

import nxt.BlockchainTest;
import nxt.DeleteFileRule;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.db.TransactionalDb;
import nxt.http.assetexchange.AssetExchangeTest;
import nxt.http.client.SetAssetPropertyBuilder;
import nxt.http.client.TransferAssetBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;

//TODO: test fails because ChildChain.AEUR already has a snapshot file
public class AssetMigrateMonitorTest extends BlockchainTest {
    @Rule
    public final DeleteFileRule deleteFileRule = new DeleteFileRule();

    private final ChildChain targetChain = ChildChain.AEUR;
    private final long chuckAssetQnt = 3;
    private final long bobAssetQnt = 2;

    private Tester assetOwner;

    @Before
    public void setUp() {
        assetOwner = ALICE;
    }

    @Test
    public void migratesAssetToChildChainWithoutSnapshot() {
        Asset asset = createAssetWithBalances();

        generateBlock();

        freezeAsset(asset, getHeight());
        setAssetMigrationHeight(asset, getHeight() + 1);
        generateBlock();

        assertTokensDistributed();
    }

    @Test
    public void setMigrationHeightUsingAssetProperty() {
        Asset asset = createAssetWithBalances();

        generateBlock();

        freezeAsset(asset, getHeight());
        setAssetMinMigrationHeight(asset, getHeight());

        setAssetMigrationAssetProperty(asset, getHeight() + 2);

        generateBlock();
        generateBlock();

        assertTokensDistributed();
    }

    private void setAssetMigrationAssetProperty(Asset asset, int height) {
        String property = Asset.ASSET_MIGRATE_HEIGHT_PROPERTY + Long.toUnsignedString(asset.getId());
        String value = Integer.toString(height);
        new SetAssetPropertyBuilder(assetOwner, asset.getId(), property, value).invokeNoError();
    }

    @Test
    public void migratesAssetToChildChainUsingSnapshot() throws URISyntaxException {
        Asset asset = createAsset();
        generateBlock();

        int freezeHeight = getHeight();
        freezeAsset(asset, freezeHeight);
        createFakeSnapshot(asset, freezeHeight);

        setAssetMigrationHeight(asset, getHeight() + 1);
        generateBlock();

        assertTokensDistributed();
    }

    private void createFakeSnapshot(Asset asset, int freezeHeight) throws URISyntaxException {
        HashMap<String, Long> snapshot = new HashMap<>();
        snapshot.put(Long.toUnsignedString(BOB.getId()), bobAssetQnt);
        snapshot.put(Long.toUnsignedString(CHUCK.getId()), chuckAssetQnt);
        File file = new AssetSnapshot().writeSnapshot(snapshot, asset.getId(), freezeHeight);
        deleteFileRule.addFile(file);
        deleteFileRule.moveToTestClasspath(file);
    }

    private void assertTokensDistributed() {
        Assert.assertEquals(bobAssetQnt, BOB.getChainBalanceDiff(targetChain.getId()));
        Assert.assertEquals(chuckAssetQnt, CHUCK.getChainBalanceDiff(targetChain.getId()));
    }

    private void setAssetMigrationHeight(Asset asset, int height) {
        TransactionalDb.runInDbTransaction(() -> {
            AssetMigrateMonitor.enableMigration(asset.getId(), targetChain, 0, height);
        });
    }

    private void setAssetMinMigrationHeight(Asset asset, int minHeight) {
        TransactionalDb.runInDbTransaction(() -> {
            AssetMigrateMonitor.enableMigration(asset.getId(), targetChain, minHeight, 0);
        });
    }

    private Asset createAssetWithBalances() {
        Asset asset = createAsset();

        transfer(asset.getId(), BOB, bobAssetQnt);
        transfer(asset.getId(), CHUCK, chuckAssetQnt);

        return asset;
    }

    private Asset createAsset() {
        return Asset.getAsset(AssetExchangeTest.issueAsset(assetOwner, "AssetC").getAssetId());
    }

    private void transfer(long assetId, Tester tester, long qnt) {
        new TransferAssetBuilder(assetId, assetOwner, tester)
                .setQuantityQNT(qnt)
                .transfer();
    }

    private void freezeAsset(Asset asset, int height) {
        AssetFreezeMonitorTest.setAssetFreezeHeight(asset, height);
    }
}