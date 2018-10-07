// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAliasesLikeCall extends APICall.Builder<GetAliasesLikeCall> {
    private GetAliasesLikeCall() {
        super("getAliasesLike");
    }

    public static GetAliasesLikeCall create(int chain) {
        GetAliasesLikeCall instance = new GetAliasesLikeCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAliasesLikeCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAliasesLikeCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAliasesLikeCall chain(int chain) {
        return param("chain", chain);
    }

    public GetAliasesLikeCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAliasesLikeCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAliasesLikeCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAliasesLikeCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public GetAliasesLikeCall aliasPrefix(String aliasPrefix) {
        return param("aliasPrefix", aliasPrefix);
    }
}
