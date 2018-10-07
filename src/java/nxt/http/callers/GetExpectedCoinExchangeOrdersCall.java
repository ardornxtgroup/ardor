// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetExpectedCoinExchangeOrdersCall extends APICall.Builder<GetExpectedCoinExchangeOrdersCall> {
    private GetExpectedCoinExchangeOrdersCall() {
        super("getExpectedCoinExchangeOrders");
    }

    public static GetExpectedCoinExchangeOrdersCall create(int chain) {
        GetExpectedCoinExchangeOrdersCall instance = new GetExpectedCoinExchangeOrdersCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetExpectedCoinExchangeOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedCoinExchangeOrdersCall chain(String chain) {
        return param("chain", chain);
    }

    public GetExpectedCoinExchangeOrdersCall chain(int chain) {
        return param("chain", chain);
    }

    public GetExpectedCoinExchangeOrdersCall exchange(String exchange) {
        return param("exchange", exchange);
    }

    public GetExpectedCoinExchangeOrdersCall exchange(int exchange) {
        return param("exchange", exchange);
    }

    public GetExpectedCoinExchangeOrdersCall account(String account) {
        return param("account", account);
    }

    public GetExpectedCoinExchangeOrdersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetExpectedCoinExchangeOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
