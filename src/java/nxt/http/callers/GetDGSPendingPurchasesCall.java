// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDGSPendingPurchasesCall extends APICall.Builder<GetDGSPendingPurchasesCall> {
    private GetDGSPendingPurchasesCall() {
        super("getDGSPendingPurchases");
    }

    public static GetDGSPendingPurchasesCall create(int chain) {
        return new GetDGSPendingPurchasesCall().param("chain", chain);
    }

    public GetDGSPendingPurchasesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSPendingPurchasesCall seller(String seller) {
        return param("seller", seller);
    }

    public GetDGSPendingPurchasesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDGSPendingPurchasesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDGSPendingPurchasesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetDGSPendingPurchasesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
