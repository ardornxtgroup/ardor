// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAccountShufflingsCall extends APICall.Builder<GetAccountShufflingsCall> {
    private GetAccountShufflingsCall() {
        super("getAccountShufflings");
    }

    public static GetAccountShufflingsCall create(int chain) {
        GetAccountShufflingsCall instance = new GetAccountShufflingsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAccountShufflingsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountShufflingsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAccountShufflingsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetAccountShufflingsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountShufflingsCall includeHoldingInfo(boolean includeHoldingInfo) {
        return param("includeHoldingInfo", includeHoldingInfo);
    }

    public GetAccountShufflingsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountShufflingsCall includeFinished(boolean includeFinished) {
        return param("includeFinished", includeFinished);
    }

    public GetAccountShufflingsCall account(String account) {
        return param("account", account);
    }

    public GetAccountShufflingsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountShufflingsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAccountShufflingsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
