package nxt.http.client;

import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import org.json.simple.JSONObject;

public class IssueAssetBuilder {

    public static final int ASSET_QNT = 10000000;
    public static final int ASSET_DECIMALS = 4;

    private final String secretPhrase;
    private final String name;
    private String description = "asset testing";
    private int quantityQNT = ASSET_QNT;
    private int decimals = ASSET_DECIMALS;
    private long feeNQT = 1000 * ChildChain.IGNIS.ONE_COIN;
    private int deadline = 1440;

    public IssueAssetBuilder(Tester creator, String name) {
        secretPhrase = creator.getSecretPhrase();
        this.name = name;
    }

    public IssueAssetResult issueAsset() {
        return new IssueAssetResult(invokeNoErr());
    }

    private JSONObject invokeNoErr() {
        return build().invokeNoError();
    }

    private APICall build() {
        return new APICall.Builder("issueAsset")
                .param("secretPhrase", secretPhrase)
                .param("name", name)
                .param("description", description)
                .param("quantityQNT", quantityQNT)
                .param("decimals", decimals)
                .param("feeNQT", feeNQT)
                .param("deadline", deadline)
                .build();
    }

    public IssueAssetBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public IssueAssetBuilder setQuantityQNT(int quantityQNT) {
        this.quantityQNT = quantityQNT;
        return this;
    }

    public IssueAssetBuilder setDecimals(int decimals) {
        this.decimals = decimals;
        return this;
    }

    public IssueAssetBuilder setFeeNQT(long feeNQT) {
        this.feeNQT = feeNQT;
        return this;
    }

    public IssueAssetBuilder setDeadline(int deadline) {
        this.deadline = deadline;
        return this;
    }

    public static class IssueAssetResult {
        private final JSONObject jsonObject;

        IssueAssetResult(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public long getAssetId() {
            return Long.parseUnsignedLong(getAssetIdString());
        }

        public String getAssetIdString() {
            return Tester.responseToStringId(jsonObject);
        }
    }
}