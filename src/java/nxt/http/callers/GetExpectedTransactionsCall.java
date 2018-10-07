// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetExpectedTransactionsCall extends APICall.Builder<GetExpectedTransactionsCall> {
    private GetExpectedTransactionsCall() {
        super("getExpectedTransactions");
    }

    public static GetExpectedTransactionsCall create(int chain) {
        GetExpectedTransactionsCall instance = new GetExpectedTransactionsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetExpectedTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedTransactionsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetExpectedTransactionsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetExpectedTransactionsCall account(String... account) {
        return param("account", account);
    }

    public GetExpectedTransactionsCall account(long... account) {
        return unsignedLongParam("account", account);
    }

    public GetExpectedTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
