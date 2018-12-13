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

import nxt.blockchain.Block;
import nxt.freeze.FreezeMonitor;
import nxt.util.Listener;

public class SaveHoldingSnapshotListener implements Listener<Block> {
    private final HoldingSnapshot holdingSnapshot;

    public SaveHoldingSnapshotListener(HoldingSnapshot holdingSnapshot) {
        this.holdingSnapshot = holdingSnapshot;
    }

    @Override
    public void notify(Block block) {
        int height = block.getHeight();
        for (long holdingId : FreezeMonitor.getFreezeIdsAt(holdingSnapshot.getHoldingType(), height)) {
            holdingSnapshot.writeSnapshot(holdingId, height);
        }
    }
}
