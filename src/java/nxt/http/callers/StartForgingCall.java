// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class StartForgingCall extends APICall.Builder<StartForgingCall> {
    private StartForgingCall() {
        super("startForging");
    }

    public static StartForgingCall create() {
        return new StartForgingCall();
    }

    public StartForgingCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }
}
