package nxt.addons;

import nxt.blockchain.Transaction;
import nxt.http.callers.GetBlockCall;
import nxt.http.callers.GetTransactionCall;
import nxt.http.responses.BlockResponse;
import nxt.http.responses.TransactionResponse;
import nxt.util.Convert;

public class TransactionContext extends AbstractOperationContext {

    private JO transactionJson;
    private TransactionResponse transactionResponse;
    private JO blockJson;

    public TransactionContext(Transaction transaction, ContractRunnerConfig config, JO runtimeParams, String contractName, String seed) {
        super(transaction.getFullHash(), transaction.getChain().getId(), transaction.getBlockId(), config, runtimeParams, contractName, seed);
        this.source = EventSource.TRANSACTION;
    }

    @Override
    protected JO getTransactionJson() {
        if (transactionJson != null) {
            return transactionJson;
        }
        transactionJson = GetTransactionCall.create(chain).
                fullHash(Convert.toHexString(fullHash)).
                call();
        return transactionJson;
    }

    @Override
    public TransactionResponse getTransaction() {
        if (transactionResponse != null) {
            return transactionResponse;
        }
        transactionResponse = TransactionResponse.create(getTransactionJson());
        return transactionResponse;
    }

    private JO getBlockJson() {
        if (blockJson != null) {
            return blockJson;
        }
        blockJson = GetBlockCall.create().
                block(blockId).
                call();
        return blockJson;
    }

    /**
     * Returns the block in which this transaction is stored
     * @return the block response
     */
    @Override
    public BlockResponse getBlock() {
        return BlockResponse.create(getBlockJson());
    }

    @Override
    protected JO addTriggerData(JO jo) {
        if (chain == 1) {
            jo.put("trigger", chain + ":" + Convert.toHexString(fullHash));
        }
        return super.addTriggerData(jo);
    }

    @Override
    protected String getReferencedTransaction() {
        if (chain == 1) {
            return null;
        }
        return chain + ":" + Convert.toHexString(fullHash);
    }

}
