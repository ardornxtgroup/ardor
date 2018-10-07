// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class LuceneReindexCall extends APICall.Builder<LuceneReindexCall> {
    private LuceneReindexCall() {
        super("luceneReindex");
    }

    public static LuceneReindexCall create() {
        return new LuceneReindexCall();
    }

    public LuceneReindexCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
