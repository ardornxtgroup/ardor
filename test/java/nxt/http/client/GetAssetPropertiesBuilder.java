package nxt.http.client;

import nxt.Tester;
import nxt.http.APICall;
import org.json.simple.JSONObject;

public class GetAssetPropertiesBuilder {
    private final APICall.Builder builder;

    public GetAssetPropertiesBuilder(long assetId) {
        builder = new APICall.Builder("getAssetProperties")
                .param("asset", Long.toUnsignedString(assetId));
    }

    public GetAssetPropertiesBuilder setter(Tester setter) {
        builder.param("setter", setter.getStrId());
        return this;
    }

    public JSONObject invokeNoError() {
        return builder.build().invokeNoError();
    }
}
