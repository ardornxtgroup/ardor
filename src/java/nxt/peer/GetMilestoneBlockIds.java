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

package nxt.peer;

import nxt.Nxt;
import nxt.blockchain.Block;

import java.util.ArrayList;
import java.util.List;

final class GetMilestoneBlockIds {

    private GetMilestoneBlockIds() {}

    /**
     * Process the GetMilestoneBlockIds message and return the MilestoneBlockIds message
     *
     * @param   peer                    Peer
     * @param   request                 Request message
     * @return                          Response message
     */
    static NetworkMessage processRequest(PeerImpl peer, NetworkMessage.GetMilestoneBlockIdsMessage request) {
        long lastBlockId = request.getLastBlockId();
        long lastMilestoneBlockId = request.getLastMilestoneBlockIdentifier();
        List<Long> milestoneBlockIds = new ArrayList<>();
        if (lastBlockId != 0) {
            long myLastBlockId = Nxt.getBlockchain().getLastBlock().getId();
            if (myLastBlockId == lastBlockId || Nxt.getBlockchain().hasBlock(lastBlockId)) {
                milestoneBlockIds.add(lastBlockId);
                return new NetworkMessage.MilestoneBlockIdsMessage(request.getMessageId(),
                        myLastBlockId == lastBlockId, milestoneBlockIds);
            }
        }
        long blockId;
        int height;
        int jump;
        int limit = 10;
        int blockchainHeight = Nxt.getBlockchain().getHeight();
        if (lastMilestoneBlockId != 0) {
            Block lastMilestoneBlock = Nxt.getBlockchain().getBlock(lastMilestoneBlockId);
            if (lastMilestoneBlock == null) {
                throw new IllegalStateException("Don't have block " + Long.toUnsignedString(lastMilestoneBlockId));
            }
            height = lastMilestoneBlock.getHeight();
            jump = Math.min(1440, Math.max(blockchainHeight - height, 1));
            height = Math.max(height - jump, 0);
        } else if (lastBlockId != 0) {
            height = blockchainHeight;
            jump = 10;
        } else {
            peer.blacklist("Old getMilestoneBlockIds request");
            throw new IllegalArgumentException("Old getMilestoneBlockIds protocol not supported");
        }
        blockId = Nxt.getBlockchain().getBlockIdAtHeight(height);
        while (height > 0 && limit-- > 0) {
            milestoneBlockIds.add(blockId);
            blockId = Nxt.getBlockchain().getBlockIdAtHeight(height);
            height -= jump;
        }
        return new NetworkMessage.MilestoneBlockIdsMessage(request.getMessageId(), false, milestoneBlockIds);
    }
}
