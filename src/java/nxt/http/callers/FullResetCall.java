// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class FullResetCall extends APICall.Builder<FullResetCall> {
    private FullResetCall() {
        super(ApiSpec.fullReset);
    }

    public static FullResetCall create() {
        return new FullResetCall();
    }

    public FullResetCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
