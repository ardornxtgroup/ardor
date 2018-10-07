// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAskOrdersCall extends APICall.Builder<GetAskOrdersCall> {
    private GetAskOrdersCall() {
        super("getAskOrders");
    }

    public static GetAskOrdersCall create(int chain) {
        GetAskOrdersCall instance = new GetAskOrdersCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAskOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAskOrdersCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAskOrdersCall chain(int chain) {
        return param("chain", chain);
    }

    public GetAskOrdersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAskOrdersCall showExpectedCancellations(String showExpectedCancellations) {
        return param("showExpectedCancellations", showExpectedCancellations);
    }

    public GetAskOrdersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAskOrdersCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAskOrdersCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAskOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAskOrdersCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
