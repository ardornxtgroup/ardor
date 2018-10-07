// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class HashCall extends APICall.Builder<HashCall> {
    private HashCall() {
        super("hash");
    }

    public static HashCall create() {
        return new HashCall();
    }

    public HashCall secretIsText(boolean secretIsText) {
        return param("secretIsText", secretIsText);
    }

    public HashCall secret(String secret) {
        return param("secret", secret);
    }

    public HashCall hashAlgorithm(String hashAlgorithm) {
        return param("hashAlgorithm", hashAlgorithm);
    }
}
