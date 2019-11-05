// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetBalanceCall extends APICall.Builder<GetBalanceCall> {
    private GetBalanceCall() {
        super(ApiSpec.getBalance);
    }

    public static GetBalanceCall create(int chain) {
        return new GetBalanceCall().param("chain", chain);
    }

    public GetBalanceCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBalanceCall account(String account) {
        return param("account", account);
    }

    public GetBalanceCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetBalanceCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetBalanceCall height(int height) {
        return param("height", height);
    }
}
