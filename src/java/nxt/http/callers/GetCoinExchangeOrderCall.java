// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetCoinExchangeOrderCall extends APICall.Builder<GetCoinExchangeOrderCall> {
    private GetCoinExchangeOrderCall() {
        super("getCoinExchangeOrder");
    }

    public static GetCoinExchangeOrderCall create() {
        return new GetCoinExchangeOrderCall();
    }

    public GetCoinExchangeOrderCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCoinExchangeOrderCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetCoinExchangeOrderCall order(String order) {
        return param("order", order);
    }

    public GetCoinExchangeOrderCall order(long order) {
        return unsignedLongParam("order", order);
    }
}
