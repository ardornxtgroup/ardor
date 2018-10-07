// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetLogCall extends APICall.Builder<GetLogCall> {
    private GetLogCall() {
        super("getLog");
    }

    public static GetLogCall create() {
        return new GetLogCall();
    }

    public GetLogCall count(String count) {
        return param("count", count);
    }

    public GetLogCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
