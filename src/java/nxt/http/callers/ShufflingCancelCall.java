// Auto generated code, do not modify
package nxt.http.callers;

public class ShufflingCancelCall extends CreateTransactionCallBuilder<ShufflingCancelCall> {
    private ShufflingCancelCall() {
        super("shufflingCancel");
    }

    public static ShufflingCancelCall create(int chain) {
        return new ShufflingCancelCall().param("chain", chain);
    }

    public ShufflingCancelCall cancellingAccount(String cancellingAccount) {
        return param("cancellingAccount", cancellingAccount);
    }

    public ShufflingCancelCall shufflingStateHash(String shufflingStateHash) {
        return param("shufflingStateHash", shufflingStateHash);
    }

    public ShufflingCancelCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public ShufflingCancelCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }
}
