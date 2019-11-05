// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetCurrencyCall extends APICall.Builder<GetCurrencyCall> {
    private GetCurrencyCall() {
        super(ApiSpec.getCurrency);
    }

    public static GetCurrencyCall create(int chain) {
        return new GetCurrencyCall().param("chain", chain);
    }

    public GetCurrencyCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCurrencyCall code(String code) {
        return param("code", code);
    }

    public GetCurrencyCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetCurrencyCall includeDeleted(boolean includeDeleted) {
        return param("includeDeleted", includeDeleted);
    }

    public GetCurrencyCall currency(String currency) {
        return param("currency", currency);
    }

    public GetCurrencyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetCurrencyCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
