// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetTradesCall extends APICall.Builder<GetTradesCall> {
    private GetTradesCall() {
        super("getTrades");
    }

    public static GetTradesCall create(int chain) {
        GetTradesCall instance = new GetTradesCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetTradesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetTradesCall chain(String chain) {
        return param("chain", chain);
    }

    public GetTradesCall chain(int chain) {
        return param("chain", chain);
    }

    public GetTradesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetTradesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetTradesCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetTradesCall asset(String asset) {
        return param("asset", asset);
    }

    public GetTradesCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetTradesCall account(String account) {
        return param("account", account);
    }

    public GetTradesCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetTradesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetTradesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public GetTradesCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
