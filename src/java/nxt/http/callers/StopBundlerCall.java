// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class StopBundlerCall extends APICall.Builder<StopBundlerCall> {
    private StopBundlerCall() {
        super("stopBundler");
    }

    public static StopBundlerCall create(int chain) {
        return new StopBundlerCall().param("chain", chain);
    }

    public StopBundlerCall account(String account) {
        return param("account", account);
    }

    public StopBundlerCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public StopBundlerCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
