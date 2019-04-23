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

package nxt.migration;

import nxt.Constants;
import nxt.Nxt;
import nxt.account.HoldingType;
import nxt.blockchain.ChildChain;
import nxt.db.DbIterator;
import nxt.env.ServerStatus;
import nxt.freeze.FreezeMonitor;
import nxt.util.Logger;

public class MigrationMonitor {

    public static void init() {
        HoldingMigration.init();
    }

    public static void scheduleMigration(ChildChain childChain, int height) {
        if (tryScheduleMigration(HoldingMigration.getByChildChain(childChain), height)) {
            Logger.logDebugMessage(String.format("Scheduled loading of child chain %s at height %d",
                    childChain.getName(), height));
        } else {
            Logger.logDebugMessage(String.format("Failed to schedule loading of child chain %s at height %d, current height %d",
                    childChain.getName(), height, Nxt.getBlockchain().getHeight()));
        }
    }

    public static void scheduleMigration(HoldingType holdingType, long holdingId, int height) {
        HoldingMigration holdingMigration = HoldingMigration.getByHoldingId(holdingId, holdingType);
        if (tryScheduleMigration(holdingMigration, height)) {
            Logger.logDebugMessage(String.format("Scheduled migration of %s %s to child chain %s at height %d",
                    holdingType.name(), Long.toUnsignedString(holdingId), holdingMigration.getChildChain().getName(), height));
        } else {
            Logger.logDebugMessage(String.format("Failed to schedule migration of %s %s at height %d, current height %d"),
                    holdingType.name(), Long.toUnsignedString(holdingId), height, Nxt.getBlockchain().getHeight());
        }
    }

    private static boolean tryScheduleMigration(HoldingMigration holdingMigration, int height) {
        if (holdingMigration == null) {
            return false;
        }
        final int blockchainHeight = Nxt.getBlockchain().getHeight();
        if (height <= blockchainHeight) {
            return false;
        }
        if (holdingMigration.getActualHeight() != 0 && holdingMigration.getActualHeight() <= blockchainHeight) {
            return false;
        }
        if (height < holdingMigration.getMinHeight()) {
            return false;
        }
        holdingMigration.setActualHeight(height);
        HoldingMigration.insert(holdingMigration);
        return true;
    }

    public static void enableMigration(long holdingId, HoldingType holdingType, ChildChain targetChain, int minHeight, int actualHeight) {
        if (!Constants.isAutomatedTest && Nxt.getServerStatus() != ServerStatus.BEFORE_DATABASE && !Nxt.getBlockchainProcessor().isScanning()) {
            throw new IllegalStateException("Setting migration only allowed during tests, rescan, or in DbVersion");
        }
        HoldingMigration holdingMigration = HoldingMigration.getByChildChain(targetChain);
        if (holdingMigration != null) {
            throw new IllegalStateException("Holding migration already set: " + holdingMigration.toString());
        }
        if (minHeight <= 0 || actualHeight < 0 || (actualHeight > 0 && actualHeight < minHeight)) {
            throw new IllegalArgumentException(String.format("Invalid minHeight %d or actualHeight %d",
                    minHeight, actualHeight));
        }
        if (holdingType != HoldingType.COIN) {
            int minFreezeHeight = FreezeMonitor.getMinFreezeHeight(holdingId, holdingType);
            if (minFreezeHeight == 0) {
                throw new IllegalStateException(String.format("%s %s not enabled for freezing",
                        holdingType.name(), Long.toUnsignedString(holdingId)));
            }
            if (minHeight < minFreezeHeight) {
                throw new IllegalArgumentException(String.format("Migration minHeight %d before freezing minHeight %d", minHeight, minFreezeHeight));
            }
        }
        HoldingMigration.insert(new HoldingMigration(holdingId, holdingType, targetChain, minHeight, actualHeight));
    }

    public static boolean hasMigrationsAt(int height) {
        try (DbIterator<HoldingMigration> dbIterator = HoldingMigration.getMigrations(height)) {
            return dbIterator.hasNext();
        }
    }


}
