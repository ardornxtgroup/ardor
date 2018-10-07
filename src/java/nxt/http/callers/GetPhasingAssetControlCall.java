// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetPhasingAssetControlCall extends APICall.Builder<GetPhasingAssetControlCall> {
    private GetPhasingAssetControlCall() {
        super("getPhasingAssetControl");
    }

    public static GetPhasingAssetControlCall create() {
        return new GetPhasingAssetControlCall();
    }

    public GetPhasingAssetControlCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPhasingAssetControlCall asset(String asset) {
        return param("asset", asset);
    }

    public GetPhasingAssetControlCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetPhasingAssetControlCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
