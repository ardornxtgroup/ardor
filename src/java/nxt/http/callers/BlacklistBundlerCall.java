// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class BlacklistBundlerCall extends APICall.Builder<BlacklistBundlerCall> {
    private BlacklistBundlerCall() {
        super(ApiSpec.blacklistBundler);
    }

    public static BlacklistBundlerCall create() {
        return new BlacklistBundlerCall();
    }

    public BlacklistBundlerCall account(String account) {
        return param("account", account);
    }

    public BlacklistBundlerCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public BlacklistBundlerCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
