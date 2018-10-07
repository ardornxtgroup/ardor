// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetDGSPurchaseCall extends APICall.Builder<GetDGSPurchaseCall> {
    private GetDGSPurchaseCall() {
        super("getDGSPurchase");
    }

    public static GetDGSPurchaseCall create(int chain) {
        GetDGSPurchaseCall instance = new GetDGSPurchaseCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetDGSPurchaseCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSPurchaseCall sharedKey(String sharedKey) {
        return param("sharedKey", sharedKey);
    }

    public GetDGSPurchaseCall chain(String chain) {
        return param("chain", chain);
    }

    public GetDGSPurchaseCall chain(int chain) {
        return param("chain", chain);
    }

    public GetDGSPurchaseCall purchase(String purchase) {
        return param("purchase", purchase);
    }

    public GetDGSPurchaseCall purchase(long purchase) {
        return unsignedLongParam("purchase", purchase);
    }

    public GetDGSPurchaseCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public GetDGSPurchaseCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
