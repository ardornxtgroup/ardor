// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.Override;
import java.lang.String;
import nxt.http.APICall;

public class EventRegisterCall extends APICall.Builder<EventRegisterCall> {
    private EventRegisterCall() {
        super("eventRegister");
    }

    public static EventRegisterCall create() {
        return new EventRegisterCall();
    }

    public EventRegisterCall add(String add) {
        return param("add", add);
    }

    public EventRegisterCall event(String... event) {
        return param("event", event);
    }

    public EventRegisterCall event(long... event) {
        return unsignedLongParam("event", event);
    }

    public EventRegisterCall remove(String remove) {
        return param("remove", remove);
    }

    public EventRegisterCall token(String token) {
        return param("token", token);
    }

    @Override
    public boolean isRemoteOnly() {
        return true;
    }
}
