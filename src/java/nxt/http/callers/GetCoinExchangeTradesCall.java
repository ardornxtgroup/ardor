// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetCoinExchangeTradesCall extends APICall.Builder<GetCoinExchangeTradesCall> {
    private GetCoinExchangeTradesCall() {
        super("getCoinExchangeTrades");
    }

    public static GetCoinExchangeTradesCall create(int chain) {
        return new GetCoinExchangeTradesCall().param("chain", chain);
    }

    public GetCoinExchangeTradesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCoinExchangeTradesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetCoinExchangeTradesCall orderFullHash(String orderFullHash) {
        return param("orderFullHash", orderFullHash);
    }

    public GetCoinExchangeTradesCall orderFullHash(byte[] orderFullHash) {
        return param("orderFullHash", orderFullHash);
    }

    public GetCoinExchangeTradesCall exchange(String exchange) {
        return param("exchange", exchange);
    }

    public GetCoinExchangeTradesCall exchange(int exchange) {
        return param("exchange", exchange);
    }

    public GetCoinExchangeTradesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetCoinExchangeTradesCall account(String account) {
        return param("account", account);
    }

    public GetCoinExchangeTradesCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetCoinExchangeTradesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetCoinExchangeTradesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
