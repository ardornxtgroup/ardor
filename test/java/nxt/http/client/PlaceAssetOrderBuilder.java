/*
 * Copyright © 2016-2019 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.http.client;

import nxt.Tester;
import nxt.http.APICall;
import nxt.util.JSONAssert;
import org.json.simple.JSONObject;

public class PlaceAssetOrderBuilder {
    private final Tester sender;
    private final long assetId;
    private final long quantityQNT;
    private final long price;
    private long feeNQT;

    public PlaceAssetOrderBuilder(Tester sender, String assetId, long quantityQNT, long price) {
        this(sender, Long.parseUnsignedLong(assetId), quantityQNT, price);
    }

    public PlaceAssetOrderBuilder(Tester sender, long assetId, long quantityQNT, long price) {
        this.sender = sender;
        this.assetId = assetId;
        this.quantityQNT = quantityQNT;
        this.price = price;
    }

    public PlaceAssetOrderBuilder setFeeNQT(long feeNQT) {
        this.feeNQT = feeNQT;
        return this;
    }

    private APICall build(String requestType) {
        return new APICall.Builder(requestType)
                .param("secretPhrase", sender.getSecretPhrase())
                .param("asset", Long.toUnsignedString(assetId))
                .param("quantityQNT", quantityQNT)
                .param("priceNQTPerShare", price)
                .param("feeNQT", feeNQT)
                .build();
    }

    private APICall buildBid() {
        return build("placeBidOrder");
    }

    private APICall buildAsk() {
        return build("placeAskOrder");
    }

    public PlaceOrderResult placeBidOrder() {
        return placeSuccess(buildBid());
    }

    public PlaceOrderResult placeAskOrder() {
        return placeSuccess(buildAsk());
    }

    private PlaceOrderResult placeSuccess(APICall apiCall) {
        return new PlaceOrderResult(apiCall.invokeNoError());
    }

    public APICall.InvocationError placeBidOrderWithError() {
        return buildBid().invokeWithError();
    }

    public APICall.InvocationError placeAskOrderWithError() {
        return buildAsk().invokeWithError();
    }

    public static class PlaceOrderResult {
        private final JSONObject jsonObject;

        PlaceOrderResult(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public String getFullHash() {
            return new JSONAssert(jsonObject).str("fullHash");
        }
    }
}
