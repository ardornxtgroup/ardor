// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountPhasedTransactionCountCall extends APICall.Builder<GetAccountPhasedTransactionCountCall> {
    private GetAccountPhasedTransactionCountCall() {
        super("getAccountPhasedTransactionCount");
    }

    public static GetAccountPhasedTransactionCountCall create(int chain) {
        return new GetAccountPhasedTransactionCountCall().param("chain", chain);
    }

    public GetAccountPhasedTransactionCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountPhasedTransactionCountCall account(String account) {
        return param("account", account);
    }

    public GetAccountPhasedTransactionCountCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountPhasedTransactionCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}