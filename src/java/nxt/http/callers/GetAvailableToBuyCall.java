// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAvailableToBuyCall extends APICall.Builder<GetAvailableToBuyCall> {
    private GetAvailableToBuyCall() {
        super("getAvailableToBuy");
    }

    public static GetAvailableToBuyCall create(int chain) {
        GetAvailableToBuyCall instance = new GetAvailableToBuyCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAvailableToBuyCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAvailableToBuyCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAvailableToBuyCall chain(int chain) {
        return param("chain", chain);
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
