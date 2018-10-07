// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetTransactionBytesCall extends APICall.Builder<GetTransactionBytesCall> {
    private GetTransactionBytesCall() {
        super("getTransactionBytes");
    }

    public static GetTransactionBytesCall create(int chain) {
        GetTransactionBytesCall instance = new GetTransactionBytesCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetTransactionBytesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetTransactionBytesCall chain(String chain) {
        return param("chain", chain);
    }

    public GetTransactionBytesCall chain(int chain) {
        return param("chain", chain);
    }

    public GetTransactionBytesCall fullHash(String fullHash) {
        return param("fullHash", fullHash);
    }

    public GetTransactionBytesCall fullHash(byte[] fullHash) {
        return param("fullHash", fullHash);
    }

    public GetTransactionBytesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
