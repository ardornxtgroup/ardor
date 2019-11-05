// Auto generated code, do not modify
package nxt.http.callers;

public class PlaceAskOrderCall extends CreateTransactionCallBuilder<PlaceAskOrderCall> {
    private PlaceAskOrderCall() {
        super(ApiSpec.placeAskOrder);
    }

    public static PlaceAskOrderCall create(int chain) {
        return new PlaceAskOrderCall().param("chain", chain);
    }

    public PlaceAskOrderCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public PlaceAskOrderCall priceNQTPerShare(long priceNQTPerShare) {
        return param("priceNQTPerShare", priceNQTPerShare);
    }

    public PlaceAskOrderCall asset(String asset) {
        return param("asset", asset);
    }

    public PlaceAskOrderCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }
}
