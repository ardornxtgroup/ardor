// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAllBroadcastedTransactionsCall extends APICall.Builder<GetAllBroadcastedTransactionsCall> {
    private GetAllBroadcastedTransactionsCall() {
        super("getAllBroadcastedTransactions");
    }

    public static GetAllBroadcastedTransactionsCall create() {
        return new GetAllBroadcastedTransactionsCall();
    }

    public GetAllBroadcastedTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllBroadcastedTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAllBroadcastedTransactionsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
