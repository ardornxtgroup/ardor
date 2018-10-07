// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetStateCall extends APICall.Builder<GetStateCall> {
    private GetStateCall() {
        super("getState");
    }

    public static GetStateCall create(int chain) {
        GetStateCall instance = new GetStateCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetStateCall chain(String chain) {
        return param("chain", chain);
    }

    public GetStateCall chain(int chain) {
        return param("chain", chain);
    }

    public GetStateCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetStateCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
