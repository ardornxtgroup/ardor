// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetExchangesByExchangeRequestCall extends APICall.Builder<GetExchangesByExchangeRequestCall> {
    private GetExchangesByExchangeRequestCall() {
        super("getExchangesByExchangeRequest");
    }

    public static GetExchangesByExchangeRequestCall create(int chain) {
        GetExchangesByExchangeRequestCall instance = new GetExchangesByExchangeRequestCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetExchangesByExchangeRequestCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExchangesByExchangeRequestCall chain(String chain) {
        return param("chain", chain);
    }

    public GetExchangesByExchangeRequestCall chain(int chain) {
        return param("chain", chain);
    }

    public GetExchangesByExchangeRequestCall includeCurrencyInfo(boolean includeCurrencyInfo) {
        return param("includeCurrencyInfo", includeCurrencyInfo);
    }

    public GetExchangesByExchangeRequestCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public GetExchangesByExchangeRequestCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public GetExchangesByExchangeRequestCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
