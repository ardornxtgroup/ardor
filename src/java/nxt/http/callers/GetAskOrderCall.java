// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAskOrderCall extends APICall.Builder<GetAskOrderCall> {
    private GetAskOrderCall() {
        super("getAskOrder");
    }

    public static GetAskOrderCall create(int chain) {
        return new GetAskOrderCall().param("chain", chain);
    }

    public GetAskOrderCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
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
