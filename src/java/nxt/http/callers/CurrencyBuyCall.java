// Auto generated code, do not modify
package nxt.http.callers;

public class CurrencyBuyCall extends CreateTransactionCallBuilder<CurrencyBuyCall> {
    private CurrencyBuyCall() {
        super(ApiSpec.currencyBuy);
    }

    public static CurrencyBuyCall create(int chain) {
        return new CurrencyBuyCall().param("chain", chain);
    }

    public CurrencyBuyCall unitsQNT(long unitsQNT) {
        return param("unitsQNT", unitsQNT);
    }

    public CurrencyBuyCall rateNQTPerUnit(long rateNQTPerUnit) {
        return param("rateNQTPerUnit", rateNQTPerUnit);
    }

    public CurrencyBuyCall currency(String currency) {
        return param("currency", currency);
    }

    public CurrencyBuyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }
}
