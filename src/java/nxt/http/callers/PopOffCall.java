// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class PopOffCall extends APICall.Builder<PopOffCall> {
    private PopOffCall() {
        super("popOff");
    }

    public static PopOffCall create() {
        return new PopOffCall();
    }

    public PopOffCall numBlocks(String numBlocks) {
        return param("numBlocks", numBlocks);
    }

    public PopOffCall keepTransactions(String keepTransactions) {
        return param("keepTransactions", keepTransactions);
    }

    public PopOffCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public PopOffCall height(int height) {
        return param("height", height);
    }
}
