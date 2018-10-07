// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAskOrderIdsCall extends APICall.Builder<GetAskOrderIdsCall> {
    private GetAskOrderIdsCall() {
        super("getAskOrderIds");
    }

    public static GetAskOrderIdsCall create(int chain) {
        GetAskOrderIdsCall instance = new GetAskOrderIdsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAskOrderIdsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAskOrderIdsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAskOrderIdsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetAskOrderIdsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAskOrderIdsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAskOrderIdsCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAskOrderIdsCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAskOrderIdsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAskOrderIdsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
