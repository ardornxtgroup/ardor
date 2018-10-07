// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetDGSTagsLikeCall extends APICall.Builder<GetDGSTagsLikeCall> {
    private GetDGSTagsLikeCall() {
        super("getDGSTagsLike");
    }

    public static GetDGSTagsLikeCall create(int chain) {
        GetDGSTagsLikeCall instance = new GetDGSTagsLikeCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetDGSTagsLikeCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSTagsLikeCall chain(String chain) {
        return param("chain", chain);
    }

    public GetDGSTagsLikeCall chain(int chain) {
        return param("chain", chain);
    }

    public GetDGSTagsLikeCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDGSTagsLikeCall tagPrefix(String tagPrefix) {
        return param("tagPrefix", tagPrefix);
    }

    public GetDGSTagsLikeCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDGSTagsLikeCall inStockOnly(String inStockOnly) {
        return param("inStockOnly", inStockOnly);
    }

    public GetDGSTagsLikeCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetDGSTagsLikeCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
