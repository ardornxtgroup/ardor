// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAllTaggedDataCall extends APICall.Builder<GetAllTaggedDataCall> {
    private GetAllTaggedDataCall() {
        super("getAllTaggedData");
    }

    public static GetAllTaggedDataCall create(int chain) {
        return new GetAllTaggedDataCall().param("chain", chain);
    }

    public GetAllTaggedDataCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllTaggedDataCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllTaggedDataCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllTaggedDataCall includeData(boolean includeData) {
        return param("includeData", includeData);
    }

    public GetAllTaggedDataCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAllTaggedDataCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
