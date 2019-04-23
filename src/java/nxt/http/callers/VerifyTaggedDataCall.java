// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class VerifyTaggedDataCall extends APICall.Builder<VerifyTaggedDataCall> {
    private VerifyTaggedDataCall() {
        super("verifyTaggedData");
    }

    public static VerifyTaggedDataCall create(int chain) {
        return new VerifyTaggedDataCall().param("chain", chain);
    }

    public VerifyTaggedDataCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public VerifyTaggedDataCall filename(String filename) {
        return param("filename", filename);
    }

    public VerifyTaggedDataCall data(String data) {
        return param("data", data);
    }

    public VerifyTaggedDataCall channel(String channel) {
        return param("channel", channel);
    }

    public VerifyTaggedDataCall name(String name) {
        return param("name", name);
    }

    public VerifyTaggedDataCall description(String description) {
        return param("description", description);
    }

    public VerifyTaggedDataCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public VerifyTaggedDataCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public VerifyTaggedDataCall type(int type) {
        return param("type", type);
    }

    public VerifyTaggedDataCall isText(boolean isText) {
        return param("isText", isText);
    }

    public VerifyTaggedDataCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public VerifyTaggedDataCall tags(String tags) {
        return param("tags", tags);
    }

    public VerifyTaggedDataCall file(byte[] b) {
        return parts("file", b);
    }
}
