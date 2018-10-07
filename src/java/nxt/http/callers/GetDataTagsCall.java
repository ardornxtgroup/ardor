// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetDataTagsCall extends APICall.Builder<GetDataTagsCall> {
    private GetDataTagsCall() {
        super("getDataTags");
    }

    public static GetDataTagsCall create(int chain) {
        GetDataTagsCall instance = new GetDataTagsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetDataTagsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDataTagsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetDataTagsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetDataTagsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDataTagsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDataTagsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetDataTagsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
