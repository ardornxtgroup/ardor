// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class DownloadPrunableMessageCall extends APICall.Builder<DownloadPrunableMessageCall> {
    private DownloadPrunableMessageCall() {
        super("downloadPrunableMessage");
    }

    public static DownloadPrunableMessageCall create(int chain) {
        return new DownloadPrunableMessageCall().param("chain", chain);
    }

    public DownloadPrunableMessageCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public DownloadPrunableMessageCall sharedKey(String sharedKey) {
        return param("sharedKey", sharedKey);
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

    public DownloadPrunableMessageCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
