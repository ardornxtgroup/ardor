// Auto generated code, do not modify
package nxt.http.callers;

public class CancelAskOrderCall extends CreateTransactionCallBuilder<CancelAskOrderCall> {
    private CancelAskOrderCall() {
        super("cancelAskOrder");
    }

    public static CancelAskOrderCall create(int chain) {
        return new CancelAskOrderCall().param("chain", chain);
    }

    public CancelAskOrderCall order(String order) {
        return param("order", order);
    }

    public CancelAskOrderCall order(long order) {
        return unsignedLongParam("order", order);
    }
}
