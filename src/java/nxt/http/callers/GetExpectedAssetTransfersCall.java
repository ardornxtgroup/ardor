// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetExpectedAssetTransfersCall extends APICall.Builder<GetExpectedAssetTransfersCall> {
    private GetExpectedAssetTransfersCall() {
        super("getExpectedAssetTransfers");
    }

    public static GetExpectedAssetTransfersCall create(int chain) {
        GetExpectedAssetTransfersCall instance = new GetExpectedAssetTransfersCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetExpectedAssetTransfersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedAssetTransfersCall chain(String chain) {
        return param("chain", chain);
    }

    public GetExpectedAssetTransfersCall chain(int chain) {
        return param("chain", chain);
    }

    public GetExpectedAssetTransfersCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetExpectedAssetTransfersCall asset(String asset) {
        return param("asset", asset);
    }

    public GetExpectedAssetTransfersCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetExpectedAssetTransfersCall account(String account) {
        return param("account", account);
    }

    public GetExpectedAssetTransfersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetExpectedAssetTransfersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
