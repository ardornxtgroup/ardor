// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class SearchPollsCall extends APICall.Builder<SearchPollsCall> {
    private SearchPollsCall() {
        super("searchPolls");
    }

    public static SearchPollsCall create(int chain) {
        return new SearchPollsCall().param("chain", chain);
    }

    public SearchPollsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
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
