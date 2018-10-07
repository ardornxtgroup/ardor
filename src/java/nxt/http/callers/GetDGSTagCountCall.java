// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetDGSTagCountCall extends APICall.Builder<GetDGSTagCountCall> {
    private GetDGSTagCountCall() {
        super("getDGSTagCount");
    }

    public static GetDGSTagCountCall create(int chain) {
        GetDGSTagCountCall instance = new GetDGSTagCountCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetDGSTagCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSTagCountCall chain(String chain) {
        return param("chain", chain);
    }

    public GetDGSTagCountCall chain(int chain) {
        return param("chain", chain);
    }

    public GetDGSTagCountCall inStockOnly(String inStockOnly) {
        return param("inStockOnly", inStockOnly);
    }

    public GetDGSTagCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
