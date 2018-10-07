// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class RetrievePrunedDataCall extends APICall.Builder<RetrievePrunedDataCall> {
    private RetrievePrunedDataCall() {
        super("retrievePrunedData");
    }

    public static RetrievePrunedDataCall create(int chain) {
        RetrievePrunedDataCall instance = new RetrievePrunedDataCall();
        instance.param("chain", chain);
        return instance;
    }

    public RetrievePrunedDataCall chain(String chain) {
        return param("chain", chain);
    }

    public RetrievePrunedDataCall chain(int chain) {
        return param("chain", chain);
    }

    public RetrievePrunedDataCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
