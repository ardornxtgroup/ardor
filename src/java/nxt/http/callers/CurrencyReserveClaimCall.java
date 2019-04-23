// Auto generated code, do not modify
package nxt.http.callers;

public class CurrencyReserveClaimCall extends CreateTransactionCallBuilder<CurrencyReserveClaimCall> {
    private CurrencyReserveClaimCall() {
        super("currencyReserveClaim");
    }

    public static CurrencyReserveClaimCall create(int chain) {
        return new CurrencyReserveClaimCall().param("chain", chain);
    }

    public CurrencyReserveClaimCall unitsQNT(long unitsQNT) {
        return param("unitsQNT", unitsQNT);
    }

    public CurrencyReserveClaimCall currency(String currency) {
        return param("currency", currency);
    }

    public CurrencyReserveClaimCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }
}
