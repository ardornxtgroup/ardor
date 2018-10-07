// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetPollCall extends APICall.Builder<GetPollCall> {
    private GetPollCall() {
        super("getPoll");
    }

    public static GetPollCall create(int chain) {
        GetPollCall instance = new GetPollCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetPollCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPollCall chain(String chain) {
        return param("chain", chain);
    }

    public GetPollCall chain(int chain) {
        return param("chain", chain);
    }

    public GetPollCall poll(String poll) {
        return param("poll", poll);
    }

    public GetPollCall poll(long poll) {
        return unsignedLongParam("poll", poll);
    }

    public GetPollCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
