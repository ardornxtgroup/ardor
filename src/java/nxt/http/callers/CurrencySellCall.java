// Auto generated code, do not modify
package nxt.http.callers;

public class CurrencySellCall extends CreateTransactionCallBuilder<CurrencySellCall> {
    private CurrencySellCall() {
        super("currencySell");
    }

    public static CurrencySellCall create(int chain) {
        return new CurrencySellCall().param("chain", chain);
    }

    public CurrencySellCall unitsQNT(long unitsQNT) {
        return param("unitsQNT", unitsQNT);
    }

    public CurrencySellCall rateNQTPerUnit(long rateNQTPerUnit) {
        return param("rateNQTPerUnit", rateNQTPerUnit);
    }

    public CurrencySellCall currency(String currency) {
        return param("currency", currency);
    }

    public CurrencySellCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }
}
