// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExpectedOrderCancellationsCall extends APICall.Builder<GetExpectedOrderCancellationsCall> {
    private GetExpectedOrderCancellationsCall() {
        super("getExpectedOrderCancellations");
    }

    public static GetExpectedOrderCancellationsCall create(int chain) {
        return new GetExpectedOrderCancellationsCall().param("chain", chain);
    }

    public GetExpectedOrderCancellationsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedOrderCancellationsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
