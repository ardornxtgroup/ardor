// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetBidOrderCall extends APICall.Builder<GetBidOrderCall> {
    private GetBidOrderCall() {
        super("getBidOrder");
    }

    public static GetBidOrderCall create(int chain) {
        GetBidOrderCall instance = new GetBidOrderCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetBidOrderCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBidOrderCall chain(String chain) {
        return param("chain", chain);
    }

    public GetBidOrderCall chain(int chain) {
        return param("chain", chain);
    }

    public GetBidOrderCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetBidOrderCall order(String order) {
        return param("order", order);
    }

    public GetBidOrderCall order(long order) {
        return unsignedLongParam("order", order);
    }
}
