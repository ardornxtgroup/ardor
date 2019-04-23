// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class SplitSecretCall extends APICall.Builder<SplitSecretCall> {
    private SplitSecretCall() {
        super("splitSecret");
    }

    public static SplitSecretCall create() {
        return new SplitSecretCall();
    }

    public SplitSecretCall totalPieces(int totalPieces) {
        return param("totalPieces", totalPieces);
    }

    public SplitSecretCall minimumPieces(int minimumPieces) {
        return param("minimumPieces", minimumPieces);
    }

    public SplitSecretCall primeFieldSize(String primeFieldSize) {
        return param("primeFieldSize", primeFieldSize);
    }
}
