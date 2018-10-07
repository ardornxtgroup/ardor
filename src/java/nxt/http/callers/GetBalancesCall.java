// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetBalancesCall extends APICall.Builder<GetBalancesCall> {
    private GetBalancesCall() {
        super("getBalances");
    }

    public static GetBalancesCall create(int chain) {
        GetBalancesCall instance = new GetBalancesCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetBalancesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBalancesCall chain(String... chain) {
        return param("chain", chain);
    }

    public GetBalancesCall chain(int... chain) {
        return param("chain", chain);
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
