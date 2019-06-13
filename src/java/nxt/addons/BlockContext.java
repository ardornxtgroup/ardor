/*
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

import nxt.blockchain.Block;
import nxt.http.callers.GetBlockCall;
import nxt.http.responses.BlockResponse;

public class BlockContext extends AbstractContractContext {

    private final int height;
    private BlockResponse blockResponse;

    public BlockContext(Block block, ContractRunnerConfig config, String contractName) {
        super(config, contractName);
        this.source = EventSource.BLOCK;
        this.height = block.getHeight();
    }

    /**
     * Returns the block in which this transaction is stored
     * @return the block response
     */
    @Override
    public BlockResponse getBlock() {
        if (blockResponse != null) {
            return blockResponse;
        }
        blockResponse = GetBlockCall.create().height(height).getBlock();
        return blockResponse;
    }

    /**
     * Returns the height of the block which triggered the contract
     * @return the block height
     */
    public int getHeight() {
        return height;
    }

    @Override
    protected String getReferencedTransaction() {
        return null;
    }
}
