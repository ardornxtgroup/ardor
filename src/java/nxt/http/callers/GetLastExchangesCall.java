// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetLastExchangesCall extends APICall.Builder<GetLastExchangesCall> {
    private GetLastExchangesCall() {
        super("getLastExchanges");
    }

    public static GetLastExchangesCall create(int chain) {
        GetLastExchangesCall instance = new GetLastExchangesCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetLastExchangesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetLastExchangesCall chain(String chain) {
        return param("chain", chain);
    }

    public GetLastExchangesCall chain(int chain) {
        return param("chain", chain);
    }

    public GetLastExchangesCall includeCurrencyInfo(boolean includeCurrencyInfo) {
        return param("includeCurrencyInfo", includeCurrencyInfo);
    }

    public GetLastExchangesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetLastExchangesCall currencies(String... currencies) {
        return param("currencies", currencies);
    }
}
