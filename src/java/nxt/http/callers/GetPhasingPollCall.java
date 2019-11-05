// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPhasingPollCall extends APICall.Builder<GetPhasingPollCall> {
    private GetPhasingPollCall() {
        super(ApiSpec.getPhasingPoll);
    }

    public static GetPhasingPollCall create(int chain) {
        return new GetPhasingPollCall().param("chain", chain);
    }

    public GetPhasingPollCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPhasingPollCall countVotes(String countVotes) {
        return param("countVotes", countVotes);
    }

    public GetPhasingPollCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetPhasingPollCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetPhasingPollCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
