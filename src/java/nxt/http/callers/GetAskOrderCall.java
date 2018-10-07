// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAskOrderCall extends APICall.Builder<GetAskOrderCall> {
    private GetAskOrderCall() {
        super("getAskOrder");
    }

    public static GetAskOrderCall create(int chain) {
        GetAskOrderCall instance = new GetAskOrderCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAskOrderCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAskOrderCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAskOrderCall chain(int chain) {
        return param("chain", chain);
    }

    public GetAskOrderCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAskOrderCall order(String order) {
        return param("order", order);
    }

    public GetAskOrderCall order(long order) {
        return unsignedLongParam("order", order);
    }
}
