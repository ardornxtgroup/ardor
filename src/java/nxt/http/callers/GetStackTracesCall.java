// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetStackTracesCall extends APICall.Builder<GetStackTracesCall> {
    private GetStackTracesCall() {
        super("getStackTraces");
    }

    public static GetStackTracesCall create() {
        return new GetStackTracesCall();
    }

    public GetStackTracesCall depth(String depth) {
        return param("depth", depth);
    }

    public GetStackTracesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
