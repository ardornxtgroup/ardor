// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountPropertiesCall extends APICall.Builder<GetAccountPropertiesCall> {
    private GetAccountPropertiesCall() {
        super("getAccountProperties");
    }

    public static GetAccountPropertiesCall create() {
        return new GetAccountPropertiesCall();
    }

    public GetAccountPropertiesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountPropertiesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountPropertiesCall property(String property) {
        return param("property", property);
    }

    public GetAccountPropertiesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountPropertiesCall setter(String setter) {
        return param("setter", setter);
    }

    public GetAccountPropertiesCall setter(long setter) {
        return unsignedLongParam("setter", setter);
    }

    public GetAccountPropertiesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAccountPropertiesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
