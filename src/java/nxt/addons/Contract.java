package nxt.addons;

import nxt.NxtException;

public interface Contract<InvocationData, ReturnedData> {

    void setContractParams(JO params);
    JO getContractParams();

    void processBlock(BlockContext context);
    void processTransaction(TransactionContext context);
    void processRequest(RequestContext context) throws NxtException;
    void processVoucher(VoucherContext context);
    ReturnedData processInvocation(DelegatedContext context, InvocationData data);

    default String minProductVersion() {
        return "0.0.0";
    }
}
