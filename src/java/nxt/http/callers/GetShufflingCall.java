// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetShufflingCall extends APICall.Builder<GetShufflingCall> {
    private GetShufflingCall() {
        super("getShuffling");
    }

    public static GetShufflingCall create(int chain) {
        return new GetShufflingCall().param("chain", chain);
    }

    public GetShufflingCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetShufflingCall includeHoldingInfo(boolean includeHoldingInfo) {
        return param("includeHoldingInfo", includeHoldingInfo);
    }

    public GetShufflingCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetShufflingCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public GetShufflingCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }
}
