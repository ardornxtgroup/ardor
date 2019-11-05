// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAllExchangesCall extends APICall.Builder<GetAllExchangesCall> {
    private GetAllExchangesCall() {
        super(ApiSpec.getAllExchanges);
    }

    public static GetAllExchangesCall create(int chain) {
        return new GetAllExchangesCall().param("chain", chain);
    }

    public GetAllExchangesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllExchangesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllExchangesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllExchangesCall includeCurrencyInfo(boolean includeCurrencyInfo) {
        return param("includeCurrencyInfo", includeCurrencyInfo);
    }

    public GetAllExchangesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAllExchangesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public GetAllExchangesCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
