// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.Override;
import java.lang.String;
import nxt.http.APICall;

public class EventWaitCall extends APICall.Builder<EventWaitCall> {
    private EventWaitCall() {
        super("eventWait");
    }

    public static EventWaitCall create() {
        return new EventWaitCall();
    }

    public EventWaitCall timeout(String timeout) {
        return param("timeout", timeout);
    }

    public EventWaitCall token(String token) {
        return param("token", token);
    }

    @Override
    public boolean isRemoteOnly() {
        return true;
    }
}
