// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class StopFundingMonitorCall extends APICall.Builder<StopFundingMonitorCall> {
    private StopFundingMonitorCall() {
        super("stopFundingMonitor");
    }

    public static StopFundingMonitorCall create(int chain) {
        return new StopFundingMonitorCall().param("chain", chain);
    }

    public StopFundingMonitorCall holding(String holding) {
        return param("holding", holding);
    }

    public StopFundingMonitorCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public StopFundingMonitorCall holdingType(String holdingType) {
        return param("holdingType", holdingType);
    }

    public StopFundingMonitorCall property(String property) {
        return param("property", property);
    }

    public StopFundingMonitorCall account(String account) {
        return param("account", account);
    }

    public StopFundingMonitorCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public StopFundingMonitorCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
