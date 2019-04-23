// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class SimulateCoinExchangeCall extends APICall.Builder<SimulateCoinExchangeCall> {
    private SimulateCoinExchangeCall() {
        super("simulateCoinExchange");
    }

    public static SimulateCoinExchangeCall create(int chain) {
        return new SimulateCoinExchangeCall().param("chain", chain);
    }

    public SimulateCoinExchangeCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public SimulateCoinExchangeCall exchange(String exchange) {
        return param("exchange", exchange);
    }

    public SimulateCoinExchangeCall exchange(int exchange) {
        return param("exchange", exchange);
    }

    public SimulateCoinExchangeCall priceNQTPerCoin(long priceNQTPerCoin) {
        return param("priceNQTPerCoin", priceNQTPerCoin);
    }
}
