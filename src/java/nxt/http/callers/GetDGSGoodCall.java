// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetDGSGoodCall extends APICall.Builder<GetDGSGoodCall> {
    private GetDGSGoodCall() {
        super("getDGSGood");
    }

    public static GetDGSGoodCall create(int chain) {
        GetDGSGoodCall instance = new GetDGSGoodCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetDGSGoodCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSGoodCall chain(String chain) {
        return param("chain", chain);
    }

    public GetDGSGoodCall chain(int chain) {
        return param("chain", chain);
    }

    public GetDGSGoodCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetDGSGoodCall goods(String goods) {
        return param("goods", goods);
    }

    public GetDGSGoodCall goods(long goods) {
        return unsignedLongParam("goods", goods);
    }

    public GetDGSGoodCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
