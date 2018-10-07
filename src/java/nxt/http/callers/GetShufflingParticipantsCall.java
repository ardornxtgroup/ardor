// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetShufflingParticipantsCall extends APICall.Builder<GetShufflingParticipantsCall> {
    private GetShufflingParticipantsCall() {
        super("getShufflingParticipants");
    }

    public static GetShufflingParticipantsCall create(int chain) {
        GetShufflingParticipantsCall instance = new GetShufflingParticipantsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetShufflingParticipantsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetShufflingParticipantsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetShufflingParticipantsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetShufflingParticipantsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetShufflingParticipantsCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public GetShufflingParticipantsCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }
}
