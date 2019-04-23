// Auto generated code, do not modify
package nxt.http.callers;

public class CancelCoinExchangeCall extends CreateTransactionCallBuilder<CancelCoinExchangeCall> {
    private CancelCoinExchangeCall() {
        super("cancelCoinExchange");
    }

    public static CancelCoinExchangeCall create(int chain) {
        return new CancelCoinExchangeCall().param("chain", chain);
    }

    public CancelCoinExchangeCall order(String order) {
        return param("order", order);
    }

    public CancelCoinExchangeCall order(long order) {
        return unsignedLongParam("order", order);
    }
}
