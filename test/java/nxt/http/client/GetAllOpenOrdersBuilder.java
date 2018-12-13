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
