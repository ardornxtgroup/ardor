// Auto generated code, do not modify
package nxt.http.callers;

public class DgsFeedbackCall extends CreateTransactionCallBuilder<DgsFeedbackCall> {
    private DgsFeedbackCall() {
        super("dgsFeedback");
    }

    public static DgsFeedbackCall create(int chain) {
        return new DgsFeedbackCall().param("chain", chain);
    }

    public DgsFeedbackCall purchase(String purchase) {
        return param("purchase", purchase);
    }

    public DgsFeedbackCall purchase(long purchase) {
        return unsignedLongParam("purchase", purchase);
    }
}
