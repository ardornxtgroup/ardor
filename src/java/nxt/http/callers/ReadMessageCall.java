// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class ReadMessageCall extends APICall.Builder<ReadMessageCall> {
    private ReadMessageCall() {
        super(ApiSpec.readMessage);
    }

    public static ReadMessageCall create(int chain) {
        return new ReadMessageCall().param("chain", chain);
    }

    public ReadMessageCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public ReadMessageCall sharedKey(String sharedKey) {
        return param("sharedKey", sharedKey);
    }

    public ReadMessageCall retrieve(boolean retrieve) {
        return param("retrieve", retrieve);
    }

    public ReadMessageCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public ReadMessageCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public ReadMessageCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
