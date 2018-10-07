// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetBundlersCall extends APICall.Builder<GetBundlersCall> {
    private GetBundlersCall() {
        super("getBundlers");
    }

    public static GetBundlersCall create(int chain) {
        GetBundlersCall instance = new GetBundlersCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetBundlersCall chain(String chain) {
        return param("chain", chain);
    }

    public GetBundlersCall chain(int chain) {
        return param("chain", chain);
    }

    public GetBundlersCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
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
