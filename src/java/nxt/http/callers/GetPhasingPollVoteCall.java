// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPhasingPollVoteCall extends APICall.Builder<GetPhasingPollVoteCall> {
    private GetPhasingPollVoteCall() {
        super("getPhasingPollVote");
    }

    public static GetPhasingPollVoteCall create(int chain) {
        return new GetPhasingPollVoteCall().param("chain", chain);
    }

    public GetPhasingPollVoteCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
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
