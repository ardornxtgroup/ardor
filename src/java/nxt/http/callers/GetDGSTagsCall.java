// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDGSTagsCall extends APICall.Builder<GetDGSTagsCall> {
    private GetDGSTagsCall() {
        super(ApiSpec.getDGSTags);
    }

    public static GetDGSTagsCall create(int chain) {
        return new GetDGSTagsCall().param("chain", chain);
    }

    public GetDGSTagsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSTagsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDGSTagsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDGSTagsCall inStockOnly(String inStockOnly) {
        return param("inStockOnly", inStockOnly);
    }

    public GetDGSTagsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetDGSTagsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
