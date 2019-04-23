// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class SetLoggingCall extends APICall.Builder<SetLoggingCall> {
    private SetLoggingCall() {
        super("setLogging");
    }

    public static SetLoggingCall create() {
        return new SetLoggingCall();
    }

    public SetLoggingCall communicationLogging(String communicationLogging) {
        return param("communicationLogging", communicationLogging);
    }

    public SetLoggingCall logLevel(String logLevel) {
        return param("logLevel", logLevel);
    }

    public SetLoggingCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
