// Auto generated code, do not modify
package nxt.http.callers;

public class PublishExchangeOfferCall extends CreateTransactionCallBuilder<PublishExchangeOfferCall> {
    private PublishExchangeOfferCall() {
        super(ApiSpec.publishExchangeOffer);
    }

    public static PublishExchangeOfferCall create(int chain) {
        return new PublishExchangeOfferCall().param("chain", chain);
    }

    public PublishExchangeOfferCall initialSellSupplyQNT(long initialSellSupplyQNT) {
        return param("initialSellSupplyQNT", initialSellSupplyQNT);
    }

    public PublishExchangeOfferCall expirationHeight(String expirationHeight) {
        return param("expirationHeight", expirationHeight);
    }

    public PublishExchangeOfferCall buyRateNQTPerUnit(long buyRateNQTPerUnit) {
        return param("buyRateNQTPerUnit", buyRateNQTPerUnit);
    }

    public PublishExchangeOfferCall sellRateNQTPerUnit(long sellRateNQTPerUnit) {
        return param("sellRateNQTPerUnit", sellRateNQTPerUnit);
    }

    public PublishExchangeOfferCall totalSellLimitQNT(long totalSellLimitQNT) {
        return param("totalSellLimitQNT", totalSellLimitQNT);
    }

    public PublishExchangeOfferCall totalBuyLimitQNT(long totalBuyLimitQNT) {
        return param("totalBuyLimitQNT", totalBuyLimitQNT);
    }

    public PublishExchangeOfferCall currency(String currency) {
        return param("currency", currency);
    }

    public PublishExchangeOfferCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public PublishExchangeOfferCall initialBuySupplyQNT(long initialBuySupplyQNT) {
        return param("initialBuySupplyQNT", initialBuySupplyQNT);
    }
}
