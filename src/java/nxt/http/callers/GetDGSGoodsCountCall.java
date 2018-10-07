// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetDGSGoodsCountCall extends APICall.Builder<GetDGSGoodsCountCall> {
    private GetDGSGoodsCountCall() {
        super("getDGSGoodsCount");
    }

    public static GetDGSGoodsCountCall create(int chain) {
        GetDGSGoodsCountCall instance = new GetDGSGoodsCountCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetDGSGoodsCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSGoodsCountCall seller(String seller) {
        return param("seller", seller);
    }

    public GetDGSGoodsCountCall chain(String chain) {
        return param("chain", chain);
    }

    public GetDGSGoodsCountCall chain(int chain) {
        return param("chain", chain);
    }

    public GetDGSGoodsCountCall inStockOnly(String inStockOnly) {
        return param("inStockOnly", inStockOnly);
    }

    public GetDGSGoodsCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
