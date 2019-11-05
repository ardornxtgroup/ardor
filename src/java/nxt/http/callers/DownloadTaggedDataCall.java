// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class DownloadTaggedDataCall extends APICall.Builder<DownloadTaggedDataCall> {
    private DownloadTaggedDataCall() {
        super(ApiSpec.downloadTaggedData);
    }

    public static DownloadTaggedDataCall create(int chain) {
        return new DownloadTaggedDataCall().param("chain", chain);
    }

    public DownloadTaggedDataCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
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
