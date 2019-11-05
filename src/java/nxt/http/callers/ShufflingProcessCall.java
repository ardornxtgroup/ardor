// Auto generated code, do not modify
package nxt.http.callers;

public class ShufflingProcessCall extends CreateTransactionCallBuilder<ShufflingProcessCall> {
    private ShufflingProcessCall() {
        super(ApiSpec.shufflingProcess);
    }

    public static ShufflingProcessCall create(int chain) {
        return new ShufflingProcessCall().param("chain", chain);
    }

    public ShufflingProcessCall recipientSecretPhrase(String recipientSecretPhrase) {
        return param("recipientSecretPhrase", recipientSecretPhrase);
    }

    public ShufflingProcessCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public ShufflingProcessCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }
}
