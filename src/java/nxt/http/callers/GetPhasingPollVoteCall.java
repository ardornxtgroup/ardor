// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetPhasingPollVoteCall extends APICall.Builder<GetPhasingPollVoteCall> {
    private GetPhasingPollVoteCall() {
        super("getPhasingPollVote");
    }

    public static GetPhasingPollVoteCall create(int chain) {
        GetPhasingPollVoteCall instance = new GetPhasingPollVoteCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetPhasingPollVoteCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPhasingPollVoteCall chain(String chain) {
        return param("chain", chain);
    }

    public GetPhasingPollVoteCall chain(int chain) {
        return param("chain", chain);
    }

    public GetPhasingPollVoteCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetPhasingPollVoteCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetPhasingPollVoteCall account(String account) {
        return param("account", account);
    }

    public GetPhasingPollVoteCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetPhasingPollVoteCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
