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

import nxt.Nxt;
import nxt.account.HoldingType;
import nxt.blockchain.Block;
import nxt.blockchain.ChildChain;
import nxt.migration.HoldingMigrateBlockEventHandler;
import nxt.migration.MigrationMonitor;
import nxt.migration.SaveHoldingSnapshotListener;
import nxt.util.Listener;
import nxt.util.Logger;

import static nxt.blockchain.BlockchainProcessor.Event.AFTER_BLOCK_ACCEPT;

public class AssetMigrateMonitor {
    public static void init() {
        MigrationMonitor.init();
        Nxt.getBlockchainProcessor().addListener(new HoldingMigrateBlockEventHandler(new AssetSnapshot()), AFTER_BLOCK_ACCEPT);
        Asset.addListener(
                new AssetPropertyEventHandler(),
                Asset.Event.SET_PROPERTY);
    }

    public static Listener<Block> getSaveAssetSnapshotListener() {
        return new SaveHoldingSnapshotListener(new AssetSnapshot());
    }

    public static void enableMigration(long assetId, ChildChain targetChain, int minHeight, int actualHeight) {
        MigrationMonitor.enableMigration(assetId, HoldingType.ASSET, targetChain, minHeight, actualHeight);
    }

    private static class AssetPropertyEventHandler implements Listener<Asset.AssetProperty> {
        @Override
        public void notify(Asset.AssetProperty property) {
            if (!Asset.ASSET_MIGRATE_HEIGHT_PROPERTY.equals(property.getProperty())) {
                return;
            }
            int height;
            try {
                height = Integer.parseInt(property.getValue());
            } catch (NumberFormatException e) {
                Logger.logDebugMessage("Invalid height value", e);
                return;
            }
            Asset asset = Asset.getAsset(property.getAssetId());
            if (asset.getAccountId() != property.getSetterId()) {
                return;
            }
            MigrationMonitor.scheduleMigration(HoldingType.ASSET, property.getAssetId(), height);
        }
    }

}