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

package nxt.freeze;

import nxt.account.HoldingType;
import nxt.blockchain.Block;
import nxt.db.DbIterator;
import nxt.db.TransactionalDb;
import nxt.util.Listener;

public abstract class AbstractFreezeBlockHandler implements Listener<Block> {
    private final HoldingType holdingType;

    protected AbstractFreezeBlockHandler(HoldingType holdingType) {
        this.holdingType = holdingType;
    }

    @Override
    public final void notify(Block block) {
        int height = block.getHeight();
        try (DbIterator<HoldingFreeze> freezes = HoldingFreeze.getFreezes(holdingType, height)) {
            for (HoldingFreeze freeze : freezes) {
                TransactionalDb.runInDbTransaction(() -> handleFreeze(freeze.getHoldingId()));
            }
        }
    }

    protected abstract void handleFreeze(long holdingId);
}
