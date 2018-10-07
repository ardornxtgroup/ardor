// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class StartBundlerCall extends APICall.Builder<StartBundlerCall> {
    private StartBundlerCall() {
        super("startBundler");
    }

    public static StartBundlerCall create(int chain) {
        StartBundlerCall instance = new StartBundlerCall();
        instance.param("chain", chain);
        return instance;
    }

    public StartBundlerCall filter(String... filter) {
        return param("filter", filter);
    }

    public StartBundlerCall overpayFQTPerFXT(long overpayFQTPerFXT) {
        return param("overpayFQTPerFXT", overpayFQTPerFXT);
    }

    public StartBundlerCall totalFeesLimitFQT(long totalFeesLimitFQT) {
        return param("totalFeesLimitFQT", totalFeesLimitFQT);
    }

    public StartBundlerCall minRateNQTPerFXT(long minRateNQTPerFXT) {
        return param("minRateNQTPerFXT", minRateNQTPerFXT);
    }

    public StartBundlerCall chain(String chain) {
        return param("chain", chain);
    }

    public StartBundlerCall chain(int chain) {
        return param("chain", chain);
    }

    public StartBundlerCall bundlingRulesJSON(String bundlingRulesJSON) {
        return param("bundlingRulesJSON", bundlingRulesJSON);
    }

    public StartBundlerCall feeCalculatorName(String feeCalculatorName) {
        return param("feeCalculatorName", feeCalculatorName);
    }

    public StartBundlerCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }
}
