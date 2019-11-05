// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class TriggerContractByHeightCall extends APICall.Builder<TriggerContractByHeightCall> {
    private TriggerContractByHeightCall() {
        super(ApiSpec.triggerContractByHeight);
    }

    public static TriggerContractByHeightCall create() {
        return new TriggerContractByHeightCall();
    }

    public TriggerContractByHeightCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public TriggerContractByHeightCall apply(String apply) {
        return param("apply", apply);
    }

    public TriggerContractByHeightCall contractName(String contractName) {
        return param("contractName", contractName);
    }

    public TriggerContractByHeightCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public TriggerContractByHeightCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public TriggerContractByHeightCall height(int height) {
        return param("height", height);
    }
}
