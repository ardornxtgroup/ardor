// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAllTradesCall extends APICall.Builder<GetAllTradesCall> {
    private GetAllTradesCall() {
        super("getAllTrades");
    }

    public static GetAllTradesCall create(int chain) {
        GetAllTradesCall instance = new GetAllTradesCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAllTradesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllTradesCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAllTradesCall chain(int chain) {
        return param("chain", chain);
    }

    public GetAllTradesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllTradesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllTradesCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetAllTradesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAllTradesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public GetAllTradesCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
