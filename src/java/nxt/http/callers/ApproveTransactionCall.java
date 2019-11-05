// Auto generated code, do not modify
package nxt.http.callers;

public class ApproveTransactionCall extends CreateTransactionCallBuilder<ApproveTransactionCall> {
    private ApproveTransactionCall() {
        super(ApiSpec.approveTransaction);
    }

    public static ApproveTransactionCall create(int chain) {
        return new ApproveTransactionCall().param("chain", chain);
    }

    public ApproveTransactionCall revealedSecret(String... revealedSecret) {
        return param("revealedSecret", revealedSecret);
    }

    public ApproveTransactionCall revealedSecretText(String revealedSecretText) {
        return param("revealedSecretText", revealedSecretText);
    }

    public ApproveTransactionCall phasedTransaction(String... phasedTransaction) {
        return param("phasedTransaction", phasedTransaction);
    }

    public ApproveTransactionCall revealedSecretIsText(boolean revealedSecretIsText) {
        return param("revealedSecretIsText", revealedSecretIsText);
    }
}
