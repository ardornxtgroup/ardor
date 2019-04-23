// Auto generated code, do not modify
package nxt.http.callers;

public class SetContractReferenceCall extends CreateTransactionCallBuilder<SetContractReferenceCall> {
    private SetContractReferenceCall() {
        super("setContractReference");
    }

    public static SetContractReferenceCall create(int chain) {
        return new SetContractReferenceCall().param("chain", chain);
    }

    public SetContractReferenceCall contract(String contract) {
        return param("contract", contract);
    }

    public SetContractReferenceCall contractName(String contractName) {
        return param("contractName", contractName);
    }

    public SetContractReferenceCall contractParams(String contractParams) {
        return param("contractParams", contractParams);
    }
}
