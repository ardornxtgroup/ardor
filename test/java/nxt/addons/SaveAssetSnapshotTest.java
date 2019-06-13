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

package nxt.addons;

import nxt.BlockchainTest;
import nxt.DeleteFileRule;
import nxt.FileUtils;
import nxt.Tester;
import nxt.ae.Asset;
import nxt.ae.AssetFreezeMonitorTest;
import nxt.blockchain.TransactionProcessorImpl;
import nxt.http.assetexchange.AssetExchangeTest;
import nxt.http.client.IssueAssetBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SaveAssetSnapshotTest extends BlockchainTest {
    @Rule
    public final DeleteFileRule deleteFileRule = new DeleteFileRule();
    private Tester assetOwner;

    @Before
    public void setUp() {
        assetOwner = ALICE;
        new SaveAssetSnapshot().init();
    }

    @After
    public void destroy() {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            TransactionProcessorImpl.getInstance().clearUnconfirmedTransactions();
            blockchainProcessor.popOffTo(-2);
            return null;
        });
    }

    @Test
    public void writesSnapshot() throws IOException {
        Asset asset = createAsset();
        generateBlock();

        int freezeHeight = getHeight() + 1;
        freezeAsset(asset, freezeHeight);
        generateBlock();

        assertSnapshotSaved(asset, freezeHeight);
    }

    private void assertSnapshotSaved(Asset asset, int height) throws IOException {
        String assetId = Long.toUnsignedString(asset.getId());
        String fileName = String.format("snapshot-asset_%s-height_%s-testnet.json", assetId, height);
        File expectedFile = new File(fileName);
        deleteFileRule.addFile(expectedFile);
        assertTrue(expectedFile.exists());

        String expectedJson = String.format("{\"%s\":%s}", assetOwner.getStrId(), IssueAssetBuilder.ASSET_QNT);
        assertEquals(expectedJson, FileUtils.readFile(expectedFile));
    }

    private Asset createAsset() {
        return Asset.getAsset(AssetExchangeTest.issueAsset(assetOwner, "AssetC").getAssetId());
    }

    private void freezeAsset(Asset asset, int height) {
        AssetFreezeMonitorTest.setAssetFreezeHeight(asset, height);
    }
}