// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExpectedCoinExchangeOrderCancellationsCall extends APICall.Builder<GetExpectedCoinExchangeOrderCancellationsCall> {
    private GetExpectedCoinExchangeOrderCancellationsCall() {
        super(ApiSpec.getExpectedCoinExchangeOrderCancellations);
    }

    public static GetExpectedCoinExchangeOrderCancellationsCall create(int chain) {
        return new GetExpectedCoinExchangeOrderCancellationsCall().param("chain", chain);
    }

    public GetExpectedCoinExchangeOrderCancellationsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedCoinExchangeOrderCancellationsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
