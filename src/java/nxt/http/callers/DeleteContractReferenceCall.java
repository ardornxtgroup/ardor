// Auto generated code, do not modify
package nxt.http.callers;

public class DeleteContractReferenceCall extends CreateTransactionCallBuilder<DeleteContractReferenceCall> {
    private DeleteContractReferenceCall() {
        super("deleteContractReference");
    }

    public static DeleteContractReferenceCall create(int chain) {
        return new DeleteContractReferenceCall().param("chain", chain);
    }

    public DeleteContractReferenceCall contractName(String contractName) {
        return param("contractName", contractName);
    }
}
