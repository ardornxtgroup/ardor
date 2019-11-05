// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetTaggedDataCall extends APICall.Builder<GetTaggedDataCall> {
    private GetTaggedDataCall() {
        super(ApiSpec.getTaggedData);
    }

    public static GetTaggedDataCall create(int chain) {
        return new GetTaggedDataCall().param("chain", chain);
    }

    public GetTaggedDataCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetTaggedDataCall retrieve(boolean retrieve) {
        return param("retrieve", retrieve);
    }

    public GetTaggedDataCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetTaggedDataCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetTaggedDataCall includeData(boolean includeData) {
        return param("includeData", includeData);
    }

    public GetTaggedDataCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
