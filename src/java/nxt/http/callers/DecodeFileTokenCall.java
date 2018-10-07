// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DecodeFileTokenCall extends APICall.Builder<DecodeFileTokenCall> {
    private DecodeFileTokenCall() {
        super("decodeFileToken");
    }

    public static DecodeFileTokenCall create() {
        return new DecodeFileTokenCall();
    }

    public DecodeFileTokenCall token(String token) {
        return param("token", token);
    }

    public APICall.Builder file(byte[] b) {
        return parts("file", b);
    }
}
