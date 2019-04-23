// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetBidOrderIdsCall extends APICall.Builder<GetBidOrderIdsCall> {
    private GetBidOrderIdsCall() {
        super("getBidOrderIds");
    }

    public static GetBidOrderIdsCall create(int chain) {
        return new GetBidOrderIdsCall().param("chain", chain);
    }

    public GetBidOrderIdsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBidOrderIdsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetBidOrderIdsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetBidOrderIdsCall asset(String asset) {
        return param("asset", asset);
    }

    public GetBidOrderIdsCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetBidOrderIdsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetBidOrderIdsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
