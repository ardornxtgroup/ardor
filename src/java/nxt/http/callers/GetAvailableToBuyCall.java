// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAvailableToBuyCall extends APICall.Builder<GetAvailableToBuyCall> {
    private GetAvailableToBuyCall() {
        super(ApiSpec.getAvailableToBuy);
    }

    public static GetAvailableToBuyCall create(int chain) {
        return new GetAvailableToBuyCall().param("chain", chain);
    }

    public GetAvailableToBuyCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAvailableToBuyCall unitsQNT(long unitsQNT) {
        return param("unitsQNT", unitsQNT);
    }

    public GetAvailableToBuyCall currency(String currency) {
        return param("currency", currency);
    }

    public GetAvailableToBuyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetAvailableToBuyCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
