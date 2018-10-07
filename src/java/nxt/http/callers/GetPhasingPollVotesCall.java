// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetPhasingPollVotesCall extends APICall.Builder<GetPhasingPollVotesCall> {
    private GetPhasingPollVotesCall() {
        super("getPhasingPollVotes");
    }

    public static GetPhasingPollVotesCall create(int chain) {
        GetPhasingPollVotesCall instance = new GetPhasingPollVotesCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetPhasingPollVotesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPhasingPollVotesCall chain(String chain) {
        return param("chain", chain);
    }

    public GetPhasingPollVotesCall chain(int chain) {
        return param("chain", chain);
    }

    public GetPhasingPollVotesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetPhasingPollVotesCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetPhasingPollVotesCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetPhasingPollVotesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetPhasingPollVotesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetPhasingPollVotesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
