// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class SimulateCoinExchangeCall extends APICall.Builder<SimulateCoinExchangeCall> {
    private SimulateCoinExchangeCall() {
        super("simulateCoinExchange");
    }

    public static SimulateCoinExchangeCall create(int chain) {
        SimulateCoinExchangeCall instance = new SimulateCoinExchangeCall();
        instance.param("chain", chain);
        return instance;
    }

    public SimulateCoinExchangeCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public SimulateCoinExchangeCall chain(String chain) {
        return param("chain", chain);
    }

    public SimulateCoinExchangeCall chain(int chain) {
        return param("chain", chain);
    }

    public SimulateCoinExchangeCall exchange(String exchange) {
        return param("exchange", exchange);
    }

    public SimulateCoinExchangeCall exchange(int exchange) {
        return param("exchange", exchange);
    }

    public SimulateCoinExchangeCall priceNQTPerCoin(String priceNQTPerCoin) {
        return param("priceNQTPerCoin", priceNQTPerCoin);
    }
}
