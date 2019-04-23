// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAliasCountCall extends APICall.Builder<GetAliasCountCall> {
    private GetAliasCountCall() {
        super("getAliasCount");
    }

    public static GetAliasCountCall create(int chain) {
        return new GetAliasCountCall().param("chain", chain);
    }

    public GetAliasCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAliasCountCall account(String account) {
        return param("account", account);
    }

    public GetAliasCountCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAliasCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
