// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetPollVotesCall extends APICall.Builder<GetPollVotesCall> {
    private GetPollVotesCall() {
        super("getPollVotes");
    }

    public static GetPollVotesCall create(int chain) {
        GetPollVotesCall instance = new GetPollVotesCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetPollVotesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPollVotesCall chain(String chain) {
        return param("chain", chain);
    }

    public GetPollVotesCall chain(int chain) {
        return param("chain", chain);
    }

    public GetPollVotesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetPollVotesCall includeWeights(boolean includeWeights) {
        return param("includeWeights", includeWeights);
    }

    public GetPollVotesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetPollVotesCall poll(String poll) {
        return param("poll", poll);
    }

    public GetPollVotesCall poll(long poll) {
        return unsignedLongParam("poll", poll);
    }

    public GetPollVotesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetPollVotesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
