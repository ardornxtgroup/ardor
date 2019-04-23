// Auto generated code, do not modify
package nxt.http.callers;

public class ExchangeCoinsCall extends CreateTransactionCallBuilder<ExchangeCoinsCall> {
    private ExchangeCoinsCall() {
        super("exchangeCoins");
    }

    public static ExchangeCoinsCall create(int chain) {
        return new ExchangeCoinsCall().param("chain", chain);
    }

    public ExchangeCoinsCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public ExchangeCoinsCall exchange(String exchange) {
        return param("exchange", exchange);
    }

    public ExchangeCoinsCall exchange(int exchange) {
        return param("exchange", exchange);
    }

    public ExchangeCoinsCall priceNQTPerCoin(long priceNQTPerCoin) {
        return param("priceNQTPerCoin", priceNQTPerCoin);
    }
}
