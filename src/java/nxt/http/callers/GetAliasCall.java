// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAliasCall extends APICall.Builder<GetAliasCall> {
    private GetAliasCall() {
        super("getAlias");
    }

    public static GetAliasCall create(int chain) {
        return new GetAliasCall().param("chain", chain);
    }

    public GetAliasCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAliasCall aliasName(String aliasName) {
        return param("aliasName", aliasName);
    }

    public GetAliasCall alias(String alias) {
        return param("alias", alias);
    }

    public GetAliasCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
