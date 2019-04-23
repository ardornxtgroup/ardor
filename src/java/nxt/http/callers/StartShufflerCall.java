// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class StartShufflerCall extends APICall.Builder<StartShufflerCall> {
    private StartShufflerCall() {
        super("startShuffler");
    }

    public static StartShufflerCall create(int chain) {
        return new StartShufflerCall().param("chain", chain);
    }

    public StartShufflerCall recipientSecretPhrase(String recipientSecretPhrase) {
        return param("recipientSecretPhrase", recipientSecretPhrase);
    }

    public StartShufflerCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public StartShufflerCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public StartShufflerCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public StartShufflerCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }
}
