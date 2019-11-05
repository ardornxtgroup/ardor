// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class StartBundlerCall extends APICall.Builder<StartBundlerCall> {
    private StartBundlerCall() {
        super(ApiSpec.startBundler);
    }

    public static StartBundlerCall create(int chain) {
        return new StartBundlerCall().param("chain", chain);
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

    public StartBundlerCall bundlingRulesJSON(String bundlingRulesJSON) {
        return param("bundlingRulesJSON", bundlingRulesJSON);
    }

    public StartBundlerCall feeCalculatorName(String feeCalculatorName) {
        return param("feeCalculatorName", feeCalculatorName);
    }
}
