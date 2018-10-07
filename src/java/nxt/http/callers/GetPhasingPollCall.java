// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetPhasingPollCall extends APICall.Builder<GetPhasingPollCall> {
    private GetPhasingPollCall() {
        super("getPhasingPoll");
    }

    public static GetPhasingPollCall create(int chain) {
        GetPhasingPollCall instance = new GetPhasingPollCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetPhasingPollCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPhasingPollCall chain(String chain) {
        return param("chain", chain);
    }

    public GetPhasingPollCall chain(int chain) {
        return param("chain", chain);
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
