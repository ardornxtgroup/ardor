// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetShufflingParticipantsCall extends APICall.Builder<GetShufflingParticipantsCall> {
    private GetShufflingParticipantsCall() {
        super(ApiSpec.getShufflingParticipants);
    }

    public static GetShufflingParticipantsCall create(int chain) {
        return new GetShufflingParticipantsCall().param("chain", chain);
    }

    public GetShufflingParticipantsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
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
