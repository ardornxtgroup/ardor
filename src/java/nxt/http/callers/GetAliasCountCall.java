// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAliasCountCall extends APICall.Builder<GetAliasCountCall> {
    private GetAliasCountCall() {
        super("getAliasCount");
    }

    public static GetAliasCountCall create(int chain) {
        GetAliasCountCall instance = new GetAliasCountCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAliasCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAliasCountCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAliasCountCall chain(int chain) {
        return param("chain", chain);
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
