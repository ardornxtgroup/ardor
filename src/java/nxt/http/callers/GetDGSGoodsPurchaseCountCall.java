// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetDGSGoodsPurchaseCountCall extends APICall.Builder<GetDGSGoodsPurchaseCountCall> {
    private GetDGSGoodsPurchaseCountCall() {
        super("getDGSGoodsPurchaseCount");
    }

    public static GetDGSGoodsPurchaseCountCall create(int chain) {
        GetDGSGoodsPurchaseCountCall instance = new GetDGSGoodsPurchaseCountCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetDGSGoodsPurchaseCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSGoodsPurchaseCountCall chain(String chain) {
        return param("chain", chain);
    }

    public GetDGSGoodsPurchaseCountCall chain(int chain) {
        return param("chain", chain);
    }

    public GetDGSGoodsPurchaseCountCall goods(String goods) {
        return param("goods", goods);
    }

    public GetDGSGoodsPurchaseCountCall goods(long goods) {
        return unsignedLongParam("goods", goods);
    }

    public GetDGSGoodsPurchaseCountCall completed(String completed) {
        return param("completed", completed);
    }

    public GetDGSGoodsPurchaseCountCall withPublicFeedbacksOnly(String withPublicFeedbacksOnly) {
        return param("withPublicFeedbacksOnly", withPublicFeedbacksOnly);
    }

    public GetDGSGoodsPurchaseCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
