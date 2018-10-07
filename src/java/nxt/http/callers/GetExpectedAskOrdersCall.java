// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetExpectedAskOrdersCall extends APICall.Builder<GetExpectedAskOrdersCall> {
    private GetExpectedAskOrdersCall() {
        super("getExpectedAskOrders");
    }

    public static GetExpectedAskOrdersCall create(int chain) {
        GetExpectedAskOrdersCall instance = new GetExpectedAskOrdersCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetExpectedAskOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedAskOrdersCall chain(String chain) {
        return param("chain", chain);
    }

    public GetExpectedAskOrdersCall chain(int chain) {
        return param("chain", chain);
    }

    public GetExpectedAskOrdersCall sortByPrice(String sortByPrice) {
        return param("sortByPrice", sortByPrice);
    }

    public GetExpectedAskOrdersCall asset(String asset) {
        return param("asset", asset);
    }

    public GetExpectedAskOrdersCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetExpectedAskOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
