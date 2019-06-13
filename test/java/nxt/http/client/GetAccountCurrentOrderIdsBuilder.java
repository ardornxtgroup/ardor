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

import nxt.http.APICall;
import nxt.util.JSONAssert;
import org.json.simple.JSONObject;

import java.util.List;

public class GetAccountCurrentOrderIdsBuilder {
    private final long accountId;
    private long assetId = 0;
    private int firstIndex = 0;
    private int lastIndex = Integer.MAX_VALUE;

    public GetAccountCurrentOrderIdsBuilder(long accountId) {
        this.accountId = accountId;
    }

    public GetAccountCurrentOrderIdsBuilder setAssetId(String assetId) {
        return setAssetId(Long.parseUnsignedLong(assetId));
    }

    public GetAccountCurrentOrderIdsBuilder setAssetId(long assetId) {
        this.assetId = assetId;
        return this;
    }

    public GetAccountCurrentOrderIdsBuilder setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
        return this;
    }

    public GetAccountCurrentOrderIdsBuilder setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
        return this;
    }

    public List<String> getAskOrders() {
        return getOrders("getAccountCurrentAskOrderIds", "askOrderIds");
    }

    public List<String> getBidOrders() {
        return getOrders("getAccountCurrentBidOrderIds", "bidOrderIds");
    }

    private List<String> getOrders(String requestType, String resultKey) {
        JSONObject result = new APICall.Builder(requestType)
                .param("account", Long.toUnsignedString(accountId))
                .param("asset", Long.toUnsignedString(assetId))
                .param("firstIndex", firstIndex)
                .param("lastIndex", lastIndex)
                .build().invokeNoError();
        return new JSONAssert(result).array(resultKey, String.class);
    }
}
