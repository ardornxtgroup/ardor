// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetBalanceCall extends APICall.Builder<GetBalanceCall> {
    private GetBalanceCall() {
        super("getBalance");
    }

    public static GetBalanceCall create(int chain) {
        GetBalanceCall instance = new GetBalanceCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetBalanceCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBalanceCall chain(String chain) {
        return param("chain", chain);
    }

    public GetBalanceCall chain(int chain) {
        return param("chain", chain);
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
