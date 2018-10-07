// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetDGSTagsCall extends APICall.Builder<GetDGSTagsCall> {
    private GetDGSTagsCall() {
        super("getDGSTags");
    }

    public static GetDGSTagsCall create(int chain) {
        GetDGSTagsCall instance = new GetDGSTagsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetDGSTagsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSTagsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetDGSTagsCall chain(int chain) {
        return param("chain", chain);
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
