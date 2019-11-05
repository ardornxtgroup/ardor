// Auto generated code, do not modify
package nxt.http.callers;

public class DgsPriceChangeCall extends CreateTransactionCallBuilder<DgsPriceChangeCall> {
    private DgsPriceChangeCall() {
        super(ApiSpec.dgsPriceChange);
    }

    public static DgsPriceChangeCall create(int chain) {
        return new DgsPriceChangeCall().param("chain", chain);
    }

    public DgsPriceChangeCall priceNQT(long priceNQT) {
        return param("priceNQT", priceNQT);
    }

    public DgsPriceChangeCall goods(String goods) {
        return param("goods", goods);
    }

    public DgsPriceChangeCall goods(long goods) {
        return unsignedLongParam("goods", goods);
    }
}
