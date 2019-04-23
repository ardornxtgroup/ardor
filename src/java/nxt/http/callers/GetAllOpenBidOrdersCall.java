// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAllOpenBidOrdersCall extends APICall.Builder<GetAllOpenBidOrdersCall> {
    private GetAllOpenBidOrdersCall() {
        super("getAllOpenBidOrders");
    }

    public static GetAllOpenBidOrdersCall create(int chain) {
        return new GetAllOpenBidOrdersCall().param("chain", chain);
    }

    public GetAllOpenBidOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllOpenBidOrdersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllOpenBidOrdersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllOpenBidOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAllOpenBidOrdersCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}