// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetPollVoteCall extends APICall.Builder<GetPollVoteCall> {
    private GetPollVoteCall() {
        super("getPollVote");
    }

    public static GetPollVoteCall create(int chain) {
        GetPollVoteCall instance = new GetPollVoteCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetPollVoteCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPollVoteCall chain(String chain) {
        return param("chain", chain);
    }

    public GetPollVoteCall chain(int chain) {
        return param("chain", chain);
    }

    public GetPollVoteCall includeWeights(boolean includeWeights) {
        return param("includeWeights", includeWeights);
    }

    public GetPollVoteCall poll(String poll) {
        return param("poll", poll);
    }

    public GetPollVoteCall poll(long poll) {
        return unsignedLongParam("poll", poll);
    }

    public GetPollVoteCall account(String account) {
        return param("account", account);
    }

    public GetPollVoteCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetPollVoteCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
