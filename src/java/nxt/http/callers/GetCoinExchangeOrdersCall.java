// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetCoinExchangeOrdersCall extends APICall.Builder<GetCoinExchangeOrdersCall> {
    private GetCoinExchangeOrdersCall() {
        super("getCoinExchangeOrders");
    }

    public static GetCoinExchangeOrdersCall create(int chain) {
        return new GetCoinExchangeOrdersCall().param("chain", chain);
    }

    public GetCoinExchangeOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCoinExchangeOrdersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetCoinExchangeOrdersCall showExpectedCancellations(String showExpectedCancellations) {
        return param("showExpectedCancellations", showExpectedCancellations);
    }

    public GetCoinExchangeOrdersCall exchange(String exchange) {
        return param("exchange", exchange);
    }

    public GetCoinExchangeOrdersCall exchange(int exchange) {
        return param("exchange", exchange);
    }

    public GetCoinExchangeOrdersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetCoinExchangeOrdersCall account(String account) {
        return param("account", account);
    }

    public GetCoinExchangeOrdersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetCoinExchangeOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetCoinExchangeOrdersCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
