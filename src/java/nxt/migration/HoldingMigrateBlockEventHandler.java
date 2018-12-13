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

package nxt.migration;

import nxt.account.Account;
import nxt.account.HoldingType;
import nxt.blockchain.Block;
import nxt.blockchain.ChildChain;
import nxt.db.DbIterator;
import nxt.db.TransactionalDb;
import nxt.dbschema.Db;
import nxt.freeze.FreezeMonitor;
import nxt.util.Convert;
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
        int count = 0;
        long total = 0;
        Logger.logDebugMessage("Loading balances for child chain %s", childChain.getName());
        for (Map.Entry<String, Long> entry : snapshot.entrySet()) {
            long quantity = entry.getValue();
            Account account;
            String key = entry.getKey();
            if (key.length() == 64) {
                byte[] publicKey = Convert.parseHexString(key);
                account = Account.addOrGetAccount(Account.getId(publicKey));
                try {
                    account.apply(publicKey);
                } catch (IllegalStateException e) {
                    Logger.logErrorMessage(String.format("Public key mismatch for account %s", Long.toUnsignedString(account.getId())), e);
                }
            } else {
                account = Account.addOrGetAccount(Long.parseUnsignedLong(key));
            }
            account.addToBalanceAndUnconfirmedBalance(childChain, null, null, quantity);
            total += quantity;
            if (++count % 1000 == 0) {
                Db.db.commitTransaction();
            }
        }
        Logger.logDebugMessage("Total balance %f %s", (double)total / childChain.ONE_COIN, childChain.getName());
    }

}
