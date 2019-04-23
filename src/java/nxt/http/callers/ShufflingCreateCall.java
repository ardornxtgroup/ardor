// Auto generated code, do not modify
package nxt.http.callers;

public class ShufflingCreateCall extends CreateTransactionCallBuilder<ShufflingCreateCall> {
    private ShufflingCreateCall() {
        super("shufflingCreate");
    }

    public static ShufflingCreateCall create(int chain) {
        return new ShufflingCreateCall().param("chain", chain);
    }

    public ShufflingCreateCall holding(String holding) {
        return param("holding", holding);
    }

    public ShufflingCreateCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public ShufflingCreateCall amount(String amount) {
        return param("amount", amount);
    }

    public ShufflingCreateCall registrationPeriod(String registrationPeriod) {
        return param("registrationPeriod", registrationPeriod);
    }

    public ShufflingCreateCall participantCount(String participantCount) {
        return param("participantCount", participantCount);
    }

    public ShufflingCreateCall holdingType(String holdingType) {
        return param("holdingType", holdingType);
    }
}