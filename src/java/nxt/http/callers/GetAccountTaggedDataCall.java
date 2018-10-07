// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAccountTaggedDataCall extends APICall.Builder<GetAccountTaggedDataCall> {
    private GetAccountTaggedDataCall() {
        super("getAccountTaggedData");
    }

    public static GetAccountTaggedDataCall create(int chain) {
        GetAccountTaggedDataCall instance = new GetAccountTaggedDataCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAccountTaggedDataCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountTaggedDataCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAccountTaggedDataCall chain(int chain) {
        return param("chain", chain);
    }

    public GetAccountTaggedDataCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountTaggedDataCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountTaggedDataCall includeData(boolean includeData) {
        return param("includeData", includeData);
    }

    public GetAccountTaggedDataCall account(String account) {
        return param("account", account);
    }

    public GetAccountTaggedDataCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountTaggedDataCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAccountTaggedDataCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
