// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExpectedBidOrdersCall extends APICall.Builder<GetExpectedBidOrdersCall> {
    private GetExpectedBidOrdersCall() {
        super("getExpectedBidOrders");
    }

    public static GetExpectedBidOrdersCall create(int chain) {
        return new GetExpectedBidOrdersCall().param("chain", chain);
    }

    public GetExpectedBidOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
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
