// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class TriggerContractByTransactionCall extends APICall.Builder<TriggerContractByTransactionCall> {
    private TriggerContractByTransactionCall() {
        super(ApiSpec.triggerContractByTransaction);
    }

    public static TriggerContractByTransactionCall create(int chain) {
        return new TriggerContractByTransactionCall().param("chain", chain);
    }

    public TriggerContractByTransactionCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public TriggerContractByTransactionCall apply(String apply) {
        return param("apply", apply);
    }

    public TriggerContractByTransactionCall triggerFullHash(String triggerFullHash) {
        return param("triggerFullHash", triggerFullHash);
    }

    public TriggerContractByTransactionCall triggerFullHash(byte[] triggerFullHash) {
        return param("triggerFullHash", triggerFullHash);
    }

    public TriggerContractByTransactionCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public TriggerContractByTransactionCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public TriggerContractByTransactionCall validate(boolean validate) {
        return param("validate", validate);
    }
}
