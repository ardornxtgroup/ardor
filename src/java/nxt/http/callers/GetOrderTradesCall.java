// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetOrderTradesCall extends APICall.Builder<GetOrderTradesCall> {
    private GetOrderTradesCall() {
        super("getOrderTrades");
    }

    public static GetOrderTradesCall create(int chain) {
        return new GetOrderTradesCall().param("chain", chain);
    }

    public GetOrderTradesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
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
