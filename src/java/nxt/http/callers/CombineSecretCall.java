// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class CombineSecretCall extends APICall.Builder<CombineSecretCall> {
    private CombineSecretCall() {
        super(ApiSpec.combineSecret);
    }

    public static CombineSecretCall create() {
        return new CombineSecretCall();
    }

    public CombineSecretCall pieces(String... pieces) {
        return param("pieces", pieces);
    }
}
