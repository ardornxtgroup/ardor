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

import java.util.List;

final class GetNextBlocks {

    private GetNextBlocks() {}

    /**
     * Process the GetNextBlocks message and return the Blocks message
     *
     * @param   peer                    Peer
     * @param   request                 Request message
     * @return                          Response message
     */
    static NetworkMessage processRequest(PeerImpl peer, NetworkMessage.GetNextBlocksMessage request) {
        long blockId = request.getBlockId();
        List<Long> blockIds = request.getBlockIds();
        int limit = (request.getLimit() != 0 ? request.getLimit() : 36);
        List<? extends Block> blocks;
        if (!blockIds.isEmpty()) {
            if (blockIds.size() > 36) {
                throw new IllegalArgumentException(Errors.TOO_MANY_BLOCKS_REQUESTED);
            }
            blocks = Nxt.getBlockchain().getBlocksAfter(blockId, blockIds);
        } else {
            if (limit > 36) {
                throw new IllegalArgumentException(Errors.TOO_MANY_BLOCKS_REQUESTED);
            }
            blocks = Nxt.getBlockchain().getBlocksAfter(blockId, limit);
        }
        return new NetworkMessage.BlocksMessage(request.getMessageId(), blocks);
    }
}
