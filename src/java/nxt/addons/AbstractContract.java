package nxt.addons;

import nxt.http.responses.TransactionResponse;

import java.util.List;

public abstract class AbstractContract<InvocationData, ReturnedData> implements Contract<InvocationData, ReturnedData> {
    @Override
    public JO processBlock(BlockContext context) { throw new UnsupportedOperationException(); }

    @Override
    public JO processTransaction(TransactionContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JO processRequest(RequestContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JO processVoucher(VoucherContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReturnedData processInvocation(DelegatedContext context, InvocationData data) {
        JO jo = new JO();
        jo.put("errorDescription", String.format("Contract %s does not support processInvocation operation", getClass().getCanonicalName()));
        context.generateResponse(jo);
        return null;
    }

    @Override
    public <T extends TransactionResponse> boolean isDuplicate(T myTransaction, List<T> existingUnconfirmedTransactions) {
        for (TransactionResponse transactionResponse : existingUnconfirmedTransactions) {
            // Check for byte to byte equality
            if (transactionResponse.equals(myTransaction)) {
                return true;
            }
        }
        return false;
    }
}
