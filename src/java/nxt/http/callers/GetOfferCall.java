// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetOfferCall extends APICall.Builder<GetOfferCall> {
    private GetOfferCall() {
        super("getOffer");
    }

    public static GetOfferCall create(int chain) {
        GetOfferCall instance = new GetOfferCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetOfferCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetOfferCall offer(String offer) {
        return param("offer", offer);
    }

    public GetOfferCall offer(long offer) {
        return unsignedLongParam("offer", offer);
    }

    public GetOfferCall chain(String chain) {
        return param("chain", chain);
    }

    public GetOfferCall chain(int chain) {
        return param("chain", chain);
    }

    public GetOfferCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
