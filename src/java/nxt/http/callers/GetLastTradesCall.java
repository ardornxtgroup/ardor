// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetLastTradesCall extends APICall.Builder<GetLastTradesCall> {
    private GetLastTradesCall() {
        super("getLastTrades");
    }

    public static GetLastTradesCall create(int chain) {
        GetLastTradesCall instance = new GetLastTradesCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetLastTradesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetLastTradesCall chain(String chain) {
        return param("chain", chain);
    }

    public GetLastTradesCall chain(int chain) {
        return param("chain", chain);
    }

    public GetLastTradesCall assets(String... assets) {
        return param("assets", assets);
    }

    public GetLastTradesCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetLastTradesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
