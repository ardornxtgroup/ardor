// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAvailableToSellCall extends APICall.Builder<GetAvailableToSellCall> {
    private GetAvailableToSellCall() {
        super("getAvailableToSell");
    }

    public static GetAvailableToSellCall create(int chain) {
        GetAvailableToSellCall instance = new GetAvailableToSellCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAvailableToSellCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAvailableToSellCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAvailableToSellCall chain(int chain) {
        return param("chain", chain);
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
