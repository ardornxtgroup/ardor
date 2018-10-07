// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetDataTagCountCall extends APICall.Builder<GetDataTagCountCall> {
    private GetDataTagCountCall() {
        super("getDataTagCount");
    }

    public static GetDataTagCountCall create(int chain) {
        GetDataTagCountCall instance = new GetDataTagCountCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetDataTagCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDataTagCountCall chain(String chain) {
        return param("chain", chain);
    }

    public GetDataTagCountCall chain(int chain) {
        return param("chain", chain);
    }

    public GetDataTagCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
