// Auto generated code, do not modify
package nxt.http.callers;

public class DividendPaymentCall extends CreateTransactionCallBuilder<DividendPaymentCall> {
    private DividendPaymentCall() {
        super(ApiSpec.dividendPayment);
    }

    public static DividendPaymentCall create(int chain) {
        return new DividendPaymentCall().param("chain", chain);
    }

    public DividendPaymentCall holding(String holding) {
        return param("holding", holding);
    }

    public DividendPaymentCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public DividendPaymentCall holdingType(byte holdingType) {
        return param("holdingType", holdingType);
    }

    public DividendPaymentCall amountNQTPerShare(long amountNQTPerShare) {
        return param("amountNQTPerShare", amountNQTPerShare);
    }

    public DividendPaymentCall asset(String asset) {
        return param("asset", asset);
    }

    public DividendPaymentCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public DividendPaymentCall height(int height) {
        return param("height", height);
    }
}
