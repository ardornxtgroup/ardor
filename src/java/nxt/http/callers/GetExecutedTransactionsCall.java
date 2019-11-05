// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExecutedTransactionsCall extends APICall.Builder<GetExecutedTransactionsCall> {
    private GetExecutedTransactionsCall() {
        super(ApiSpec.getExecutedTransactions);
    }

    public static GetExecutedTransactionsCall create(int chain) {
        return new GetExecutedTransactionsCall().param("chain", chain);
    }

    public GetExecutedTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExecutedTransactionsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetExecutedTransactionsCall sender(String sender) {
        return param("sender", sender);
    }

    public GetExecutedTransactionsCall sender(long sender) {
        return unsignedLongParam("sender", sender);
    }

    public GetExecutedTransactionsCall subtype(int subtype) {
        return param("subtype", subtype);
    }

    public GetExecutedTransactionsCall numberOfConfirmations(String numberOfConfirmations) {
        return param("numberOfConfirmations", numberOfConfirmations);
    }

    public GetExecutedTransactionsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetExecutedTransactionsCall type(int type) {
        return param("type", type);
    }

    public GetExecutedTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetExecutedTransactionsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public GetExecutedTransactionsCall height(int height) {
        return param("height", height);
    }
}
