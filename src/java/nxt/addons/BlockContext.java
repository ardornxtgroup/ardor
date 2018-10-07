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
