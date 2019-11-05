// Auto generated code, do not modify
package nxt.http.callers;

public class ShufflingVerifyCall extends CreateTransactionCallBuilder<ShufflingVerifyCall> {
    private ShufflingVerifyCall() {
        super(ApiSpec.shufflingVerify);
    }

    public static ShufflingVerifyCall create(int chain) {
        return new ShufflingVerifyCall().param("chain", chain);
    }

    public ShufflingVerifyCall shufflingStateHash(String shufflingStateHash) {
        return param("shufflingStateHash", shufflingStateHash);
    }

    public ShufflingVerifyCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public ShufflingVerifyCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }
}
