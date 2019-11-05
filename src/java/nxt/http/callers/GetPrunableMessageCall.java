// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPrunableMessageCall extends APICall.Builder<GetPrunableMessageCall> {
    private GetPrunableMessageCall() {
        super(ApiSpec.getPrunableMessage);
    }

    public static GetPrunableMessageCall create(int chain) {
        return new GetPrunableMessageCall().param("chain", chain);
    }

    public GetPrunableMessageCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPrunableMessageCall sharedKey(String sharedKey) {
        return param("sharedKey", sharedKey);
    }

    public GetPrunableMessageCall retrieve(boolean retrieve) {
        return param("retrieve", retrieve);
    }

    public GetPrunableMessageCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetPrunableMessageCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetPrunableMessageCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
