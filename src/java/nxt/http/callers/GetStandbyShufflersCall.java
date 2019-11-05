// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetStandbyShufflersCall extends APICall.Builder<GetStandbyShufflersCall> {
    private GetStandbyShufflersCall() {
        super(ApiSpec.getStandbyShufflers);
    }

    public static GetStandbyShufflersCall create(int chain) {
        return new GetStandbyShufflersCall().param("chain", chain);
    }

    public GetStandbyShufflersCall holding(String holding) {
        return param("holding", holding);
    }

    public GetStandbyShufflersCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public GetStandbyShufflersCall includeHoldingInfo(boolean includeHoldingInfo) {
        return param("includeHoldingInfo", includeHoldingInfo);
    }

    public GetStandbyShufflersCall holdingType(byte holdingType) {
        return param("holdingType", holdingType);
    }

    public GetStandbyShufflersCall account(String account) {
        return param("account", account);
    }

    public GetStandbyShufflersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetStandbyShufflersCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
