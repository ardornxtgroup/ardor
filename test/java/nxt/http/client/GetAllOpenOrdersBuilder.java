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
import org.json.simple.JSONObject;

public class GetAllOpenOrdersBuilder {
    private int firstIndex = 0;
    private int lastIndex = Integer.MAX_VALUE;

    private JSONObject getOrders(String requestType) {
        return new APICall.Builder(requestType)
                .param("firstIndex", firstIndex)
                .param("lastIndex", lastIndex)
                .build().invokeNoError();
    }

    public JSONObject getBidOrders() {
        return getOrders("getAllOpenBidOrders");
    }

    public JSONObject getAskOrders() {
        return getOrders("getAllOpenAskOrders");
    }

    public GetAllOpenOrdersBuilder setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
        return this;
    }

    public GetAllOpenOrdersBuilder setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
        return this;
    }
}
