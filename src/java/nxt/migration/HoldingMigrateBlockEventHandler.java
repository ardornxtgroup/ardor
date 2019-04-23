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

import nxt.account.HoldingType;
import nxt.blockchain.Block;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Genesis;
import nxt.db.DbIterator;
import nxt.db.TransactionalDb;
import nxt.freeze.FreezeMonitor;
import nxt.util.Listener;
import nxt.util.Logger;

import java.util.Map;

public class HoldingMigrateBlockEventHandler implements Listener<Block> {
    private final HoldingSnapshot holdingSnapshot;

    public HoldingMigrateBlockEventHandler(HoldingSnapshot holdingSnapshot) {
        this.holdingSnapshot = holdingSnapshot;
    }

    @Override
    public void notify(Block block) {
        try (DbIterator<HoldingMigration> targets = HoldingMigration.getMigrations(block.getHeight())) {
            for (HoldingMigration target : targets) {
                TransactionalDb.runInDbTransaction(() -> handle(target));
            }
        }
    }

    private void handle(HoldingMigration target) {
        HoldingType holdingType = target.getHoldingType();
        long holdingId = target.getHoldingId();
        if (holdingType != holdingSnapshot.getHoldingType()) {
            return;
        }
        if (holdingType != HoldingType.COIN) {
            if (!FreezeMonitor.isFrozen(holdingType, holdingId)) {
                Logger.logErrorMessage(String.format("%s %s is not frozen, will not migrate to a child chain!!!",
                        holdingType.name(), Long.toUnsignedString(holdingId)));
                return;
            }
        }
        Map<String, Long> snapshot = holdingSnapshot.getSnapshot(target);
        ChildChain childChain = target.getChildChain();
        Genesis.loadBalances(childChain, snapshot);
    }

}
