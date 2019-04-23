// Auto generated code, do not modify
package nxt.http.callers;

public class CancelBidOrderCall extends CreateTransactionCallBuilder<CancelBidOrderCall> {
    private CancelBidOrderCall() {
        super("cancelBidOrder");
    }

    public static CancelBidOrderCall create(int chain) {
        return new CancelBidOrderCall().param("chain", chain);
    }

    public CancelBidOrderCall order(String order) {
        return param("order", order);
    }

    public CancelBidOrderCall order(long order) {
        return unsignedLongParam("order", order);
    }
}
