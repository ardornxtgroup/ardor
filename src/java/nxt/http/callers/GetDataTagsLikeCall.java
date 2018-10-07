// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetDataTagsLikeCall extends APICall.Builder<GetDataTagsLikeCall> {
    private GetDataTagsLikeCall() {
        super("getDataTagsLike");
    }

    public static GetDataTagsLikeCall create(int chain) {
        GetDataTagsLikeCall instance = new GetDataTagsLikeCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetDataTagsLikeCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDataTagsLikeCall chain(String chain) {
        return param("chain", chain);
    }

    public GetDataTagsLikeCall chain(int chain) {
        return param("chain", chain);
    }

    public GetDataTagsLikeCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDataTagsLikeCall tagPrefix(String tagPrefix) {
        return param("tagPrefix", tagPrefix);
    }

    public GetDataTagsLikeCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDataTagsLikeCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetDataTagsLikeCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
