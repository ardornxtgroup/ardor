// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetDGSGoodsCall extends APICall.Builder<GetDGSGoodsCall> {
    private GetDGSGoodsCall() {
        super("getDGSGoods");
    }

    public static GetDGSGoodsCall create(int chain) {
        GetDGSGoodsCall instance = new GetDGSGoodsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetDGSGoodsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSGoodsCall seller(String seller) {
        return param("seller", seller);
    }

    public GetDGSGoodsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetDGSGoodsCall chain(int chain) {
        return param("chain", chain);
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
