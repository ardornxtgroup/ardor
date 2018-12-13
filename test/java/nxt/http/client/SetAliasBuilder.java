package nxt.http.client;

import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import org.json.simple.JSONObject;

public class SetAliasBuilder {
    private final APICall.Builder builder = new APICall.Builder("setAlias");
    private long fee;

    public SetAliasBuilder(Tester tester, ChildChain childChain, String aliasName, String aliasUri) {
        builder.param("secretPhrase", tester.getSecretPhrase())
                .param("chain", childChain.getName())
                .param("aliasName", aliasName)
                .param("aliasURI", aliasUri);
    }

    public SetAliasBuilder setFee(long fee) {
        this.fee = fee;
        return this;
    }

    public JSONObject invokeNoError() {
        return builder
                .param("feeNQT", fee)
                .build().invokeNoError();
    }
}
