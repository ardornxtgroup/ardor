// Auto generated code, do not modify
package nxt.http.callers;

public class DgsRefundCall extends CreateTransactionCallBuilder<DgsRefundCall> {
    private DgsRefundCall() {
        super("dgsRefund");
    }

    public static DgsRefundCall create(int chain) {
        return new DgsRefundCall().param("chain", chain);
    }

    public DgsRefundCall refundNQT(long refundNQT) {
        return param("refundNQT", refundNQT);
    }

    public DgsRefundCall purchase(String purchase) {
        return param("purchase", purchase);
    }

    public DgsRefundCall purchase(long purchase) {
        return unsignedLongParam("purchase", purchase);
    }
}
