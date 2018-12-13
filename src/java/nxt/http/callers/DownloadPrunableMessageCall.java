// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DownloadPrunableMessageCall extends APICall.Builder<DownloadPrunableMessageCall> {
    private DownloadPrunableMessageCall() {
        super("downloadPrunableMessage");
    }

    public static DownloadPrunableMessageCall create(int chain) {
        DownloadPrunableMessageCall instance = new DownloadPrunableMessageCall();
        instance.param("chain", chain);
        return instance;
    }

    public DownloadPrunableMessageCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public DownloadPrunableMessageCall sharedKey(String sharedKey) {
        return param("sharedKey", sharedKey);
    }

    public DownloadPrunableMessageCall chain(String chain) {
        return param("chain", chain);
    }

    public DownloadPrunableMessageCall chain(int chain) {
        return param("chain", chain);
    }

    public DownloadPrunableMessageCall save(String save) {
        return param("save", save);
    }

    public DownloadPrunableMessageCall retrieve(boolean retrieve) {
        return param("retrieve", retrieve);
    }

    public DownloadPrunableMessageCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public DownloadPrunableMessageCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public DownloadPrunableMessageCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DownloadPrunableMessageCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
