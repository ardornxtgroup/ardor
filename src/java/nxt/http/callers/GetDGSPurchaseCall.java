// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDGSPurchaseCall extends APICall.Builder<GetDGSPurchaseCall> {
    private GetDGSPurchaseCall() {
        super("getDGSPurchase");
    }

    public static GetDGSPurchaseCall create(int chain) {
        return new GetDGSPurchaseCall().param("chain", chain);
    }

    public GetDGSPurchaseCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSPurchaseCall sharedKey(String sharedKey) {
        return param("sharedKey", sharedKey);
    }

    public GetDGSPurchaseCall purchase(String purchase) {
        return param("purchase", purchase);
    }

    public GetDGSPurchaseCall purchase(long purchase) {
        return unsignedLongParam("purchase", purchase);
    }

    public GetDGSPurchaseCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
