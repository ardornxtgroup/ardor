// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetEffectiveBalanceCall extends APICall.Builder<GetEffectiveBalanceCall> {
    private GetEffectiveBalanceCall() {
        super("getEffectiveBalance");
    }

    public static GetEffectiveBalanceCall create() {
        return new GetEffectiveBalanceCall();
    }

    public GetEffectiveBalanceCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetEffectiveBalanceCall account(String account) {
        return param("account", account);
    }

    public GetEffectiveBalanceCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetEffectiveBalanceCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetEffectiveBalanceCall height(int height) {
        return param("height", height);
    }
}
