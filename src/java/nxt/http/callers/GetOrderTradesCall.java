// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetOrderTradesCall extends APICall.Builder<GetOrderTradesCall> {
    private GetOrderTradesCall() {
        super("getOrderTrades");
    }

    public static GetOrderTradesCall create(int chain) {
        GetOrderTradesCall instance = new GetOrderTradesCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetOrderTradesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetOrderTradesCall chain(String chain) {
        return param("chain", chain);
    }

    public GetOrderTradesCall chain(int chain) {
        return param("chain", chain);
    }

    public GetOrderTradesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetOrderTradesCall askOrderFullHash(String askOrderFullHash) {
        return param("askOrderFullHash", askOrderFullHash);
    }

    public GetOrderTradesCall askOrderFullHash(byte[] askOrderFullHash) {
        return param("askOrderFullHash", askOrderFullHash);
    }

    public GetOrderTradesCall bidOrderFullHash(String bidOrderFullHash) {
        return param("bidOrderFullHash", bidOrderFullHash);
    }

    public GetOrderTradesCall bidOrderFullHash(byte[] bidOrderFullHash) {
        return param("bidOrderFullHash", bidOrderFullHash);
    }

    public GetOrderTradesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetOrderTradesCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetOrderTradesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetOrderTradesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
