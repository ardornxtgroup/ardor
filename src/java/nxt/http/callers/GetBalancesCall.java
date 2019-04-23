// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetBalancesCall extends APICall.Builder<GetBalancesCall> {
    private GetBalancesCall() {
        super("getBalances");
    }

    public static GetBalancesCall create(int chain) {
        return new GetBalancesCall().param("chain", chain);
    }

    public GetBalancesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBalancesCall account(String account) {
        return param("account", account);
    }

    public GetBalancesCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetBalancesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetBalancesCall height(int height) {
        return param("height", height);
    }
}
