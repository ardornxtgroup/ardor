package nxt.http.client;

import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import org.json.simple.JSONObject;

public class SetAssetPropertyBuilder {
    private final String secretPhrase;
    private final long assetId;
    private final String property;
    private final String value;
    private long feeNQT = 3 * ChildChain.IGNIS.ONE_COIN;

    public SetAssetPropertyBuilder(Tester setter, long assetId, String name, String value) {
        this.secretPhrase = setter.getSecretPhrase();
        this.assetId = assetId;
        this.property = name;
        this.value = value;
    }

    public SetAssetPropertyBuilder setFeeNQT(long feeNQT) {
        this.feeNQT = feeNQT;
        return this;
    }

    public JSONObject invokeNoError() {
        return build().invokeNoError();
    }

    private APICall build() {
        return new APICall.Builder("setAssetProperty")
                .param("secretPhrase", secretPhrase)
                .param("asset", Long.toUnsignedString(assetId))
                .param("feeNQT", feeNQT)
                .param("property", property)
                .param("value", value)
                .build();
    }
}
