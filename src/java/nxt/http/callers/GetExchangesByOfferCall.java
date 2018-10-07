// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetExchangesByOfferCall extends APICall.Builder<GetExchangesByOfferCall> {
    private GetExchangesByOfferCall() {
        super("getExchangesByOffer");
    }

    public static GetExchangesByOfferCall create(int chain) {
        GetExchangesByOfferCall instance = new GetExchangesByOfferCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetExchangesByOfferCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExchangesByOfferCall offer(String offer) {
        return param("offer", offer);
    }

    public GetExchangesByOfferCall offer(long offer) {
        return unsignedLongParam("offer", offer);
    }

    public GetExchangesByOfferCall chain(String chain) {
        return param("chain", chain);
    }

    public GetExchangesByOfferCall chain(int chain) {
        return param("chain", chain);
    }

    public GetExchangesByOfferCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetExchangesByOfferCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetExchangesByOfferCall includeCurrencyInfo(boolean includeCurrencyInfo) {
        return param("includeCurrencyInfo", includeCurrencyInfo);
    }

    public GetExchangesByOfferCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetExchangesByOfferCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
