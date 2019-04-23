// Auto generated code, do not modify
package nxt.http.callers;

public class TransferCurrencyCall extends CreateTransactionCallBuilder<TransferCurrencyCall> {
    private TransferCurrencyCall() {
        super("transferCurrency");
    }

    public static TransferCurrencyCall create(int chain) {
        return new TransferCurrencyCall().param("chain", chain);
    }

    public TransferCurrencyCall unitsQNT(long unitsQNT) {
        return param("unitsQNT", unitsQNT);
    }

    public TransferCurrencyCall currency(String currency) {
        return param("currency", currency);
    }

    public TransferCurrencyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }
}
