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
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.http.APICall.InvocationError;
import nxt.util.JSONAssert;
import org.json.simple.JSONObject;

public class TransferAssetBuilder {
    private final long assetId;
    private final Tester from;
    private final Tester to;
    private long quantityQNT = 1;
    private long fee = ChildChain.IGNIS.ONE_COIN;

    public TransferAssetBuilder(String assetId, Tester from, Tester to) {
        this(Long.parseUnsignedLong(assetId), from, to);
    }

    public TransferAssetBuilder(long assetId, Tester from, Tester to) {
        this.assetId = assetId;
        this.from = from;
        this.to = to;
    }

    public TransferAssetBuilder setQuantityQNT(long quantityQNT) {
        this.quantityQNT = quantityQNT;
        return this;
    }

    public TransferAssetBuilder setFee(long fee) {
        this.fee = fee;
        return this;
    }

    private APICall build() {
        return new APICall.Builder("transferAsset")
                .param("secretPhrase", from.getSecretPhrase())
                .param("recipient", to.getRsAccount())
                .param("asset", Long.toUnsignedString(assetId))
                .param("quantityQNT", quantityQNT)
                .param("feeNQT", fee)
                .build();
    }

    public TransferResult transfer() {
        return new TransferResult(build().invokeNoError());
    }

    public InvocationError transferWithError() {
        return build().invokeWithError();
    }

    public static class TransferResult {
        private final JSONObject jsonObject;

        TransferResult(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public String getFullHash() {
            return new JSONAssert(jsonObject).str("fullHash");
        }
    }
}
