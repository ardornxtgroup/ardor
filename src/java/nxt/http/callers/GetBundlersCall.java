// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetBundlersCall extends APICall.Builder<GetBundlersCall> {
    private GetBundlersCall() {
        super(ApiSpec.getBundlers);
    }

    public static GetBundlersCall create(int chain) {
        return new GetBundlersCall().param("chain", chain);
    }

    public GetBundlersCall account(String account) {
        return param("account", account);
    }

    public GetBundlersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetBundlersCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
