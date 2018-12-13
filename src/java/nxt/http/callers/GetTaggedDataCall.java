// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetTaggedDataCall extends APICall.Builder<GetTaggedDataCall> {
    private GetTaggedDataCall() {
        super("getTaggedData");
    }

    public static GetTaggedDataCall create(int chain) {
        GetTaggedDataCall instance = new GetTaggedDataCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetTaggedDataCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetTaggedDataCall chain(String chain) {
        return param("chain", chain);
    }

    public GetTaggedDataCall chain(int chain) {
        return param("chain", chain);
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
