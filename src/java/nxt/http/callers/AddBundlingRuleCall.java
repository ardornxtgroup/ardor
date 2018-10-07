// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class AddBundlingRuleCall extends APICall.Builder<AddBundlingRuleCall> {
    private AddBundlingRuleCall() {
        super("addBundlingRule");
    }

    public static AddBundlingRuleCall create(int chain) {
        AddBundlingRuleCall instance = new AddBundlingRuleCall();
        instance.param("chain", chain);
        return instance;
    }

    public AddBundlingRuleCall filter(String... filter) {
        return param("filter", filter);
    }

    public AddBundlingRuleCall overpayFQTPerFXT(long overpayFQTPerFXT) {
        return param("overpayFQTPerFXT", overpayFQTPerFXT);
    }

    public AddBundlingRuleCall totalFeesLimitFQT(long totalFeesLimitFQT) {
        return param("totalFeesLimitFQT", totalFeesLimitFQT);
    }

    public AddBundlingRuleCall minRateNQTPerFXT(long minRateNQTPerFXT) {
        return param("minRateNQTPerFXT", minRateNQTPerFXT);
    }

    public AddBundlingRuleCall chain(String chain) {
        return param("chain", chain);
    }

    public AddBundlingRuleCall chain(int chain) {
        return param("chain", chain);
    }

    public AddBundlingRuleCall feeCalculatorName(String feeCalculatorName) {
        return param("feeCalculatorName", feeCalculatorName);
    }

    public AddBundlingRuleCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }
}
