// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetDGSExpiredPurchasesCall extends APICall.Builder<GetDGSExpiredPurchasesCall> {
    private GetDGSExpiredPurchasesCall() {
        super("getDGSExpiredPurchases");
    }

    public static GetDGSExpiredPurchasesCall create(int chain) {
        GetDGSExpiredPurchasesCall instance = new GetDGSExpiredPurchasesCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetDGSExpiredPurchasesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSExpiredPurchasesCall seller(String seller) {
        return param("seller", seller);
    }

    public GetDGSExpiredPurchasesCall chain(String chain) {
        return param("chain", chain);
    }

    public GetDGSExpiredPurchasesCall chain(int chain) {
        return param("chain", chain);
    }

    public GetDGSExpiredPurchasesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDGSExpiredPurchasesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDGSExpiredPurchasesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetDGSExpiredPurchasesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
