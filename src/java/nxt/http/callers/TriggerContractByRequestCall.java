// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class TriggerContractByRequestCall extends APICall.Builder<TriggerContractByRequestCall> {
    private TriggerContractByRequestCall() {
        super(ApiSpec.triggerContractByRequest);
    }

    public static TriggerContractByRequestCall create() {
        return new TriggerContractByRequestCall();
    }

    public TriggerContractByRequestCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public TriggerContractByRequestCall setupParams(String setupParams) {
        return param("setupParams", setupParams);
    }

    public TriggerContractByRequestCall contractName(String contractName) {
        return param("contractName", contractName);
    }

    public TriggerContractByRequestCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public TriggerContractByRequestCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
