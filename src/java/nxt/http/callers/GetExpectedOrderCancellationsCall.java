// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetExpectedOrderCancellationsCall extends APICall.Builder<GetExpectedOrderCancellationsCall> {
    private GetExpectedOrderCancellationsCall() {
        super("getExpectedOrderCancellations");
    }

    public static GetExpectedOrderCancellationsCall create(int chain) {
        GetExpectedOrderCancellationsCall instance = new GetExpectedOrderCancellationsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetExpectedOrderCancellationsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedOrderCancellationsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetExpectedOrderCancellationsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetExpectedOrderCancellationsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
