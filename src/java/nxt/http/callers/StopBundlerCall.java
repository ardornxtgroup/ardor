// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class StopBundlerCall extends APICall.Builder<StopBundlerCall> {
    private StopBundlerCall() {
        super("stopBundler");
    }

    public static StopBundlerCall create(int chain) {
        StopBundlerCall instance = new StopBundlerCall();
        instance.param("chain", chain);
        return instance;
    }

    public StopBundlerCall chain(String chain) {
        return param("chain", chain);
    }

    public StopBundlerCall chain(int chain) {
        return param("chain", chain);
    }

    public StopBundlerCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
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
