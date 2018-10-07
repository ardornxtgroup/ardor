// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class StartShufflerCall extends APICall.Builder<StartShufflerCall> {
    private StartShufflerCall() {
        super("startShuffler");
    }

    public static StartShufflerCall create(int chain) {
        StartShufflerCall instance = new StartShufflerCall();
        instance.param("chain", chain);
        return instance;
    }

    public StartShufflerCall chain(String chain) {
        return param("chain", chain);
    }

    public StartShufflerCall chain(int chain) {
        return param("chain", chain);
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

    public StartShufflerCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public StartShufflerCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public StartShufflerCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public StartShufflerCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }
}
