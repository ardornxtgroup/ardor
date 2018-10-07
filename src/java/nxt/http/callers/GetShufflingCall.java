// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetShufflingCall extends APICall.Builder<GetShufflingCall> {
    private GetShufflingCall() {
        super("getShuffling");
    }

    public static GetShufflingCall create(int chain) {
        GetShufflingCall instance = new GetShufflingCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetShufflingCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetShufflingCall chain(String chain) {
        return param("chain", chain);
    }

    public GetShufflingCall chain(int chain) {
        return param("chain", chain);
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
