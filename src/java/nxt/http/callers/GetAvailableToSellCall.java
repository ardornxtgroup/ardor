// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAvailableToSellCall extends APICall.Builder<GetAvailableToSellCall> {
    private GetAvailableToSellCall() {
        super("getAvailableToSell");
    }

    public static GetAvailableToSellCall create(int chain) {
        return new GetAvailableToSellCall().param("chain", chain);
    }

    public GetAvailableToSellCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAvailableToSellCall unitsQNT(long unitsQNT) {
        return param("unitsQNT", unitsQNT);
    }

    public GetAvailableToSellCall currency(String currency) {
        return param("currency", currency);
    }

    public GetAvailableToSellCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetAvailableToSellCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
