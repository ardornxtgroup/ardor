package nxt.addons;

import nxt.http.responses.BlockResponse;
import nxt.http.responses.TransactionResponse;

public class VoucherContext extends AbstractOperationContext {

    private final JO voucher;
    private TransactionResponse transactionResponse;

    public VoucherContext(JO voucher, ContractRunnerConfig config, String contractName) {
        super(null, voucher.getJo("transactionJSON").getInt("chain"), 0, config, null, contractName, null);
        this.source = EventSource.VOUCHER;
        this.voucher = voucher;
    }

    @Override
    protected JO getTransactionJson() {
        return voucher.getJo("transactionJSON");
    }

    @Override
    public TransactionResponse getTransaction() {
        if (transactionResponse != null) {
            return transactionResponse;
        }
        transactionResponse = TransactionResponse.create(voucher.getJo("transactionJSON"));
        return transactionResponse;
    }

    @Override
    public BlockResponse getBlock() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected JO addTriggerData(JO jo) {
        jo.put("triggerVoucherSignature", voucher.getString("signature"));
        return super.addTriggerData(jo);
    }

    /**
     * Returns the transaction voucher which triggered this contract execution
     * @return the transaction voucher json format
     */
    public JO getVoucher() {
        return voucher;
    }

    @Override
    protected String getReferencedTransaction() {
        return null;
    }
}
