// Auto generated code, do not modify
package nxt.http.callers;

public class PlaceBidOrderCall extends CreateTransactionCallBuilder<PlaceBidOrderCall> {
    private PlaceBidOrderCall() {
        super(ApiSpec.placeBidOrder);
    }

    public static PlaceBidOrderCall create(int chain) {
        return new PlaceBidOrderCall().param("chain", chain);
    }

    public PlaceBidOrderCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public PlaceBidOrderCall priceNQTPerShare(long priceNQTPerShare) {
        return param("priceNQTPerShare", priceNQTPerShare);
    }

    public PlaceBidOrderCall asset(String asset) {
        return param("asset", asset);
    }

    public PlaceBidOrderCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }
}
