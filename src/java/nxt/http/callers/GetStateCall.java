// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetStateCall extends APICall.Builder<GetStateCall> {
    private GetStateCall() {
        super("getState");
    }

    public static GetStateCall create(int chain) {
        return new GetStateCall().param("chain", chain);
    }

    public GetStateCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetStateCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
