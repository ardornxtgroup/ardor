// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DownloadTaggedDataCall extends APICall.Builder<DownloadTaggedDataCall> {
    private DownloadTaggedDataCall() {
        super("downloadTaggedData");
    }

    public static DownloadTaggedDataCall create(int chain) {
        DownloadTaggedDataCall instance = new DownloadTaggedDataCall();
        instance.param("chain", chain);
        return instance;
    }

    public DownloadTaggedDataCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public DownloadTaggedDataCall chain(String chain) {
        return param("chain", chain);
    }

    public DownloadTaggedDataCall chain(int chain) {
        return param("chain", chain);
    }

    public DownloadTaggedDataCall retrieve(boolean retrieve) {
        return param("retrieve", retrieve);
    }

    public DownloadTaggedDataCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public DownloadTaggedDataCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public DownloadTaggedDataCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
