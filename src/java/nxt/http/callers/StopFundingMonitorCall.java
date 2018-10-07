// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class StopFundingMonitorCall extends APICall.Builder<StopFundingMonitorCall> {
    private StopFundingMonitorCall() {
        super("stopFundingMonitor");
    }

    public static StopFundingMonitorCall create(int chain) {
        StopFundingMonitorCall instance = new StopFundingMonitorCall();
        instance.param("chain", chain);
        return instance;
    }

    public StopFundingMonitorCall holding(String holding) {
        return param("holding", holding);
    }

    public StopFundingMonitorCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public StopFundingMonitorCall chain(String chain) {
        return param("chain", chain);
    }

    public StopFundingMonitorCall chain(int chain) {
        return param("chain", chain);
    }

    public StopFundingMonitorCall holdingType(String holdingType) {
        return param("holdingType", holdingType);
    }

    public StopFundingMonitorCall property(String property) {
        return param("property", property);
    }

    public StopFundingMonitorCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
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
