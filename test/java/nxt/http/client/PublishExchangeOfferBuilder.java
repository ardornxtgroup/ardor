package nxt.http.client;

import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.ms.Currency;
import org.json.simple.JSONObject;

public class PublishExchangeOfferBuilder {
    private final Tester account;
    private final Currency currency;
    private long feeNQT = ChildChain.IGNIS.ONE_COIN;
    private long buyRateNQT = 1;
    private long sellRateNQT = 1;
    private long totalBuyLimit = 0;
    private long totalSellLimit = 0;
    private long initialBuySupply = 0;
    private long initialSellSupply = 0;
    private int expirationHeight = 0;

    public PublishExchangeOfferBuilder(Tester account, Currency currency) {
        this.account = account;
        this.currency = currency;
    }

    private APICall build() {
        return new APICall.Builder("publishExchangeOffer")
                .secretPhrase(account.getSecretPhrase())
                .feeNQT(feeNQT)
                .param("currency", Long.toUnsignedString(currency.getId()))
                .param("buyRateNQTPerUnit", buyRateNQT)
                .param("sellRateNQTPerUnit", sellRateNQT)
                .param("totalBuyLimitQNT", totalBuyLimit)
                .param("totalSellLimitQNT", totalSellLimit)
                .param("initialBuySupplyQNT", initialBuySupply)
                .param("initialSellSupplyQNT", initialSellSupply)
                .param("expirationHeight", expirationHeight)
                .build();
    }

    public PublishExchangeOfferBuilder setFeeNQT(long feeNQT) {
        this.feeNQT = feeNQT;
        return this;
    }

    public PublishExchangeOfferBuilder setBuyRateNQT(long buyRateNQT) {
        this.buyRateNQT = buyRateNQT;
        return this;
    }

    public PublishExchangeOfferBuilder setSellRateNQT(long sellRateNQT) {
        this.sellRateNQT = sellRateNQT;
        return this;
    }

    public PublishExchangeOfferBuilder setTotalBuyLimit(long totalBuyLimit) {
        this.totalBuyLimit = totalBuyLimit;
        return this;
    }

    public PublishExchangeOfferBuilder setTotalSellLimit(long totalSellLimit) {
        this.totalSellLimit = totalSellLimit;
        return this;
    }

    public PublishExchangeOfferBuilder setInitialBuySupply(long initialBuySupply) {
        this.initialBuySupply = initialBuySupply;
        return this;
    }

    public PublishExchangeOfferBuilder setInitialSellSupply(long initialSellSupply) {
        this.initialSellSupply = initialSellSupply;
        return this;
    }

    public PublishExchangeOfferBuilder setExpirationHeight(int expirationHeight) {
        this.expirationHeight = expirationHeight;
        return this;
    }

    public JSONObject invokeNoError() {
        return build().invokeNoError();
    }
}
