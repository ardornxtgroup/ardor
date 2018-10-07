// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetBidOrdersCall extends APICall.Builder<GetBidOrdersCall> {
    private GetBidOrdersCall() {
        super("getBidOrders");
    }

    public static GetBidOrdersCall create(int chain) {
        GetBidOrdersCall instance = new GetBidOrdersCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetBidOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBidOrdersCall chain(String chain) {
        return param("chain", chain);
    }

    public GetBidOrdersCall chain(int chain) {
        return param("chain", chain);
    }

    public GetBidOrdersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetBidOrdersCall showExpectedCancellations(String showExpectedCancellations) {
        return param("showExpectedCancellations", showExpectedCancellations);
    }

    public GetBidOrdersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetBidOrdersCall asset(String asset) {
        return param("asset", asset);
    }

    public GetBidOrdersCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetBidOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetBidOrdersCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
