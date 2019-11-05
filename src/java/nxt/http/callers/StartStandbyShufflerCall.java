// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class StartStandbyShufflerCall extends APICall.Builder<StartStandbyShufflerCall> {
    private StartStandbyShufflerCall() {
        super(ApiSpec.startStandbyShuffler);
    }

    public static StartStandbyShufflerCall create(int chain) {
        return new StartStandbyShufflerCall().param("chain", chain);
    }

    public StartStandbyShufflerCall minAmount(String minAmount) {
        return param("minAmount", minAmount);
    }

    public StartStandbyShufflerCall holding(String holding) {
        return param("holding", holding);
    }

    public StartStandbyShufflerCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public StartStandbyShufflerCall recipientPublicKeys(String recipientPublicKeys) {
        return param("recipientPublicKeys", recipientPublicKeys);
    }

    public StartStandbyShufflerCall recipientPublicKeys(byte[] recipientPublicKeys) {
        return param("recipientPublicKeys", recipientPublicKeys);
    }

    public StartStandbyShufflerCall holdingType(byte holdingType) {
        return param("holdingType", holdingType);
    }

    public StartStandbyShufflerCall maxAmount(String maxAmount) {
        return param("maxAmount", maxAmount);
    }

    public StartStandbyShufflerCall minParticipants(int minParticipants) {
        return param("minParticipants", minParticipants);
    }
}
