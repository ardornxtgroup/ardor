// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class ReadMessageCall extends APICall.Builder<ReadMessageCall> {
    private ReadMessageCall() {
        super("readMessage");
    }

    public static ReadMessageCall create(int chain) {
        ReadMessageCall instance = new ReadMessageCall();
        instance.param("chain", chain);
        return instance;
    }

    public ReadMessageCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public ReadMessageCall sharedKey(String sharedKey) {
        return param("sharedKey", sharedKey);
    }

    public ReadMessageCall chain(String chain) {
        return param("chain", chain);
    }

    public ReadMessageCall chain(int chain) {
        return param("chain", chain);
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

    public ReadMessageCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public ReadMessageCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
