// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class TriggerContractByTransactionCall extends APICall.Builder<TriggerContractByTransactionCall> {
    private TriggerContractByTransactionCall() {
        super("triggerContractByTransaction");
    }

    public static TriggerContractByTransactionCall create(int chain) {
        TriggerContractByTransactionCall instance = new TriggerContractByTransactionCall();
        instance.param("chain", chain);
        return instance;
    }

    public TriggerContractByTransactionCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public TriggerContractByTransactionCall chain(String chain) {
        return param("chain", chain);
    }

    public TriggerContractByTransactionCall chain(int chain) {
        return param("chain", chain);
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

    public TriggerContractByTransactionCall validate(String validate) {
        return param("validate", validate);
    }
}
