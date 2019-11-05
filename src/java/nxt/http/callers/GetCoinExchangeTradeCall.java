// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetCoinExchangeTradeCall extends APICall.Builder<GetCoinExchangeTradeCall> {
    private GetCoinExchangeTradeCall() {
        super(ApiSpec.getCoinExchangeTrade);
    }

    public static GetCoinExchangeTradeCall create() {
        return new GetCoinExchangeTradeCall();
    }

    public GetCoinExchangeTradeCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCoinExchangeTradeCall orderFullHash(String orderFullHash) {
        return param("orderFullHash", orderFullHash);
    }

    public GetCoinExchangeTradeCall orderFullHash(byte[] orderFullHash) {
        return param("orderFullHash", orderFullHash);
    }

    public GetCoinExchangeTradeCall matchFullHash(String matchFullHash) {
        return param("matchFullHash", matchFullHash);
    }

    public GetCoinExchangeTradeCall matchFullHash(byte[] matchFullHash) {
        return param("matchFullHash", matchFullHash);
    }

    public GetCoinExchangeTradeCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
