// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class StartFundingMonitorCall extends APICall.Builder<StartFundingMonitorCall> {
    private StartFundingMonitorCall() {
        super("startFundingMonitor");
    }

    public static StartFundingMonitorCall create(int chain) {
        return new StartFundingMonitorCall().param("chain", chain);
    }

    public StartFundingMonitorCall holding(String holding) {
        return param("holding", holding);
    }

    public StartFundingMonitorCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public StartFundingMonitorCall amount(String amount) {
        return param("amount", amount);
    }

    public StartFundingMonitorCall holdingType(String holdingType) {
        return param("holdingType", holdingType);
    }

    public StartFundingMonitorCall property(String property) {
        return param("property", property);
    }

    public StartFundingMonitorCall interval(String interval) {
        return param("interval", interval);
    }

    public StartFundingMonitorCall threshold(String threshold) {
        return param("threshold", threshold);
    }
}
