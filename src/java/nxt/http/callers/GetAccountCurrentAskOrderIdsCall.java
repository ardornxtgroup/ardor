// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAccountCurrentAskOrderIdsCall extends APICall.Builder<GetAccountCurrentAskOrderIdsCall> {
    private GetAccountCurrentAskOrderIdsCall() {
        super("getAccountCurrentAskOrderIds");
    }

    public static GetAccountCurrentAskOrderIdsCall create(int chain) {
        GetAccountCurrentAskOrderIdsCall instance = new GetAccountCurrentAskOrderIdsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAccountCurrentAskOrderIdsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountCurrentAskOrderIdsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAccountCurrentAskOrderIdsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetAccountCurrentAskOrderIdsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountCurrentAskOrderIdsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountCurrentAskOrderIdsCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAccountCurrentAskOrderIdsCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAccountCurrentAskOrderIdsCall account(String account) {
        return param("account", account);
    }

    public GetAccountCurrentAskOrderIdsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountCurrentAskOrderIdsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAccountCurrentAskOrderIdsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
