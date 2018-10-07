// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class SearchPollsCall extends APICall.Builder<SearchPollsCall> {
    private SearchPollsCall() {
        super("searchPolls");
    }

    public static SearchPollsCall create(int chain) {
        SearchPollsCall instance = new SearchPollsCall();
        instance.param("chain", chain);
        return instance;
    }

    public SearchPollsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public SearchPollsCall chain(String chain) {
        return param("chain", chain);
    }

    public SearchPollsCall chain(int chain) {
        return param("chain", chain);
    }

    public SearchPollsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public SearchPollsCall query(String query) {
        return param("query", query);
    }

    public SearchPollsCall includeFinished(boolean includeFinished) {
        return param("includeFinished", includeFinished);
    }

    public SearchPollsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public SearchPollsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public SearchPollsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
