// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAccountCurrentAskOrdersCall extends APICall.Builder<GetAccountCurrentAskOrdersCall> {
    private GetAccountCurrentAskOrdersCall() {
        super("getAccountCurrentAskOrders");
    }

    public static GetAccountCurrentAskOrdersCall create(int chain) {
        GetAccountCurrentAskOrdersCall instance = new GetAccountCurrentAskOrdersCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAccountCurrentAskOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountCurrentAskOrdersCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAccountCurrentAskOrdersCall chain(int chain) {
        return param("chain", chain);
    }

    public GetAccountCurrentAskOrdersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountCurrentAskOrdersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountCurrentAskOrdersCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAccountCurrentAskOrdersCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAccountCurrentAskOrdersCall account(String account) {
        return param("account", account);
    }

    public GetAccountCurrentAskOrdersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountCurrentAskOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAccountCurrentAskOrdersCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
