// Auto generated code, do not modify
package nxt.http.callers;

public class DgsQuantityChangeCall extends CreateTransactionCallBuilder<DgsQuantityChangeCall> {
    private DgsQuantityChangeCall() {
        super(ApiSpec.dgsQuantityChange);
    }

    public static DgsQuantityChangeCall create(int chain) {
        return new DgsQuantityChangeCall().param("chain", chain);
    }

    public DgsQuantityChangeCall goods(String goods) {
        return param("goods", goods);
    }

    public DgsQuantityChangeCall goods(long goods) {
        return unsignedLongParam("goods", goods);
    }

    public DgsQuantityChangeCall deltaQuantity(String deltaQuantity) {
        return param("deltaQuantity", deltaQuantity);
    }
}
