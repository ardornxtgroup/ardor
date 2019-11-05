// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class RetrievePrunedDataCall extends APICall.Builder<RetrievePrunedDataCall> {
    private RetrievePrunedDataCall() {
        super(ApiSpec.retrievePrunedData);
    }

    public static RetrievePrunedDataCall create(int chain) {
        return new RetrievePrunedDataCall().param("chain", chain);
    }

    public RetrievePrunedDataCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
