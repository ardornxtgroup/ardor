// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetExpectedCoinExchangeOrderCancellationsCall extends APICall.Builder<GetExpectedCoinExchangeOrderCancellationsCall> {
    private GetExpectedCoinExchangeOrderCancellationsCall() {
        super("getExpectedCoinExchangeOrderCancellations");
    }

    public static GetExpectedCoinExchangeOrderCancellationsCall create(int chain) {
        GetExpectedCoinExchangeOrderCancellationsCall instance = new GetExpectedCoinExchangeOrderCancellationsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetExpectedCoinExchangeOrderCancellationsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedCoinExchangeOrderCancellationsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetExpectedCoinExchangeOrderCancellationsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetExpectedCoinExchangeOrderCancellationsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
