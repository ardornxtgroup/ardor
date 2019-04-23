// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class AddBundlingRuleCall extends APICall.Builder<AddBundlingRuleCall> {
    private AddBundlingRuleCall() {
        super("addBundlingRule");
    }

    public static AddBundlingRuleCall create(int chain) {
        return new AddBundlingRuleCall().param("chain", chain);
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

    public AddBundlingRuleCall feeCalculatorName(String feeCalculatorName) {
        return param("feeCalculatorName", feeCalculatorName);
    }
}
