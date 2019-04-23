// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetForgingCall extends APICall.Builder<GetForgingCall> {
    private GetForgingCall() {
        super("getForging");
    }

    public static GetForgingCall create() {
        return new GetForgingCall();
    }

    public GetForgingCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
