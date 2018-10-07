// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetDGSPurchasesCall extends APICall.Builder<GetDGSPurchasesCall> {
    private GetDGSPurchasesCall() {
        super("getDGSPurchases");
    }

    public static GetDGSPurchasesCall create(int chain) {
        GetDGSPurchasesCall instance = new GetDGSPurchasesCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetDGSPurchasesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSPurchasesCall seller(String seller) {
        return param("seller", seller);
    }

    public GetDGSPurchasesCall chain(String chain) {
        return param("chain", chain);
    }

    public GetDGSPurchasesCall chain(int chain) {
        return param("chain", chain);
    }

    public GetDGSPurchasesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDGSPurchasesCall completed(String completed) {
        return param("completed", completed);
    }

    public GetDGSPurchasesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDGSPurchasesCall withPublicFeedbacksOnly(String withPublicFeedbacksOnly) {
        return param("withPublicFeedbacksOnly", withPublicFeedbacksOnly);
    }

    public GetDGSPurchasesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetDGSPurchasesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public GetDGSPurchasesCall buyer(String buyer) {
        return param("buyer", buyer);
    }

    public GetDGSPurchasesCall buyer(long buyer) {
        return unsignedLongParam("buyer", buyer);
    }
}
