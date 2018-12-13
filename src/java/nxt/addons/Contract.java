package nxt.addons;

import nxt.NxtException;
import nxt.http.responses.TransactionResponse;

import java.util.List;

public interface Contract<InvocationData, ReturnedData> {
    JO processBlock(BlockContext context);
    JO processTransaction(TransactionContext context);
    JO processRequest(RequestContext context) throws NxtException;
    JO processVoucher(VoucherContext context);
    ReturnedData processInvocation(DelegatedContext context, InvocationData data);

    <T extends TransactionResponse> boolean isDuplicate(T myTransaction, List<T> existingUnconfirmedTransactions);

    default String minProductVersion() {
        return "0.0.0";
    }
}
