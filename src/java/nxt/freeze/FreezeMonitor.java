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

package nxt.freeze;

import nxt.Constants;
import nxt.Nxt;
import nxt.account.HoldingType;
import nxt.db.DbIterator;
import nxt.env.ServerStatus;
import nxt.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class FreezeMonitor {
    static {
        HoldingFreeze.init();
    }

    private final HoldingType holdingType;

    public FreezeMonitor(HoldingType holdingType) {
        this.holdingType = holdingType;
    }

    public boolean isFrozen(long holdingId) {
        return isFrozen(holdingType, holdingId);
    }

    public static boolean isFrozen(HoldingType holdingType, long holdingId) {
        HoldingFreeze holdingFreeze = HoldingFreeze.getById(holdingId, holdingType);
        return holdingFreeze != null && holdingFreeze.isFrozen(Nxt.getBlockchain().getHeight());
    }

    public void scheduleFreeze(long holdingId, int height) {
        if (tryScheduleFreeze(holdingId, height)) {
            Logger.logDebugMessage(String.format("Scheduled freezing of %s at height %d",
                    Long.toUnsignedString(holdingId), height));
        } else {
            Logger.logDebugMessage(String.format("Failed to schedule freezing of %s at height %d, current height %d",
                    Long.toUnsignedString(holdingId), height, Nxt.getBlockchain().getHeight()));
        }
    }

    private boolean tryScheduleFreeze(long holdingId, int height) {
        int blockchainHeight = Nxt.getBlockchain().getHeight();
        if (height <= blockchainHeight) {
            return false;
        }
        HoldingFreeze freeze = HoldingFreeze.getById(holdingId, holdingType);
        if (freeze == null) {
            return false;
        }
        if (freeze.isFrozen(blockchainHeight)) {
            return false;
        }
        if (height < freeze.getMinHeight()) {
            return false;
        }
        freeze.setActualHeight(height);
        HoldingFreeze.insert(freeze);
        return true;
    }

    public void enableFreeze(long holdingId, int minHeight, int actualHeight) {
        if (!Constants.isAutomatedTest && Nxt.getServerStatus() != ServerStatus.BEFORE_DATABASE && !Nxt.getBlockchainProcessor().isScanning()) {
            throw new IllegalStateException("Setting freeze only allowed during tests or in DbVersion");
        }
        HoldingFreeze holdingFreeze = HoldingFreeze.getById(holdingId, holdingType);
        if (holdingFreeze != null) {
            throw new IllegalStateException("Holding freeze already set: " + holdingFreeze.toString());
        }
        if (minHeight <= 0 || actualHeight < 0 || (actualHeight > 0 && actualHeight < minHeight)) {
            throw new IllegalArgumentException(String.format("Invalid minHeight %d or actualHeight %d",
                    minHeight, actualHeight));
        }
        HoldingFreeze.insert(new HoldingFreeze(holdingId, holdingType, minHeight, actualHeight));
    }

    public static boolean hasFreezesAt(int actualHeight) {
        try (DbIterator<HoldingFreeze> dbIterator = HoldingFreeze.getFreezes(actualHeight)) {
            return dbIterator.hasNext();
        }
    }

    public static List<Long> getFreezeIdsAt(HoldingType holdingType, int actualHeight) {
        List<Long> holdingIds = new ArrayList<>();
        try (DbIterator<HoldingFreeze> freezes = HoldingFreeze.getFreezes(holdingType, actualHeight)) {
            while (freezes.hasNext()) {
                holdingIds.add(freezes.next().getHoldingId());
            }
        }
        return holdingIds;
    }

    public static int getMinFreezeHeight(long holdingId, HoldingType holdingType) {
        HoldingFreeze holdingFreeze = HoldingFreeze.getById(holdingId, holdingType);
        if (holdingFreeze == null) {
            return 0;
        }
        return holdingFreeze.getMinHeight();
    }
}
