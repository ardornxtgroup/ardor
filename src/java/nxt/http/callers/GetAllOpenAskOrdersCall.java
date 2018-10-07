// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAllOpenAskOrdersCall extends APICall.Builder<GetAllOpenAskOrdersCall> {
    private GetAllOpenAskOrdersCall() {
        super("getAllOpenAskOrders");
    }

    public static GetAllOpenAskOrdersCall create(int chain) {
        GetAllOpenAskOrdersCall instance = new GetAllOpenAskOrdersCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAllOpenAskOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllOpenAskOrdersCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAllOpenAskOrdersCall chain(int chain) {
        return param("chain", chain);
    }

    public GetAllOpenAskOrdersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllOpenAskOrdersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllOpenAskOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAllOpenAskOrdersCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
