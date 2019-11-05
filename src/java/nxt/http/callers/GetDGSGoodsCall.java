// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDGSGoodsCall extends APICall.Builder<GetDGSGoodsCall> {
    private GetDGSGoodsCall() {
        super(ApiSpec.getDGSGoods);
    }

    public static GetDGSGoodsCall create(int chain) {
        return new GetDGSGoodsCall().param("chain", chain);
    }

    public GetDGSGoodsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSGoodsCall seller(String seller) {
        return param("seller", seller);
    }

    public GetDGSGoodsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDGSGoodsCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetDGSGoodsCall hideDelisted(String hideDelisted) {
        return param("hideDelisted", hideDelisted);
    }

    public GetDGSGoodsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDGSGoodsCall inStockOnly(String inStockOnly) {
        return param("inStockOnly", inStockOnly);
    }

    public GetDGSGoodsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetDGSGoodsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
