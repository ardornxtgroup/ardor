// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetPrunableMessageCall extends APICall.Builder<GetPrunableMessageCall> {
    private GetPrunableMessageCall() {
        super("getPrunableMessage");
    }

    public static GetPrunableMessageCall create(int chain) {
        GetPrunableMessageCall instance = new GetPrunableMessageCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetPrunableMessageCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPrunableMessageCall sharedKey(String sharedKey) {
        return param("sharedKey", sharedKey);
    }

    public GetPrunableMessageCall chain(String chain) {
        return param("chain", chain);
    }

    public GetPrunableMessageCall chain(int chain) {
        return param("chain", chain);
    }

    public GetPrunableMessageCall retrieve(String retrieve) {
        return param("retrieve", retrieve);
    }

    public GetPrunableMessageCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetPrunableMessageCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetPrunableMessageCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public GetPrunableMessageCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
