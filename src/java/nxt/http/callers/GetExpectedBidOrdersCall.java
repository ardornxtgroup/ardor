// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetExpectedBidOrdersCall extends APICall.Builder<GetExpectedBidOrdersCall> {
    private GetExpectedBidOrdersCall() {
        super("getExpectedBidOrders");
    }

    public static GetExpectedBidOrdersCall create(int chain) {
        GetExpectedBidOrdersCall instance = new GetExpectedBidOrdersCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetExpectedBidOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedBidOrdersCall chain(String chain) {
        return param("chain", chain);
    }

    public GetExpectedBidOrdersCall chain(int chain) {
        return param("chain", chain);
    }

    public GetExpectedBidOrdersCall sortByPrice(String sortByPrice) {
        return param("sortByPrice", sortByPrice);
    }

    public GetExpectedBidOrdersCall asset(String asset) {
        return param("asset", asset);
    }

    public GetExpectedBidOrdersCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetExpectedBidOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
