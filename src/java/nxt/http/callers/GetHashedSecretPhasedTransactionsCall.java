// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetHashedSecretPhasedTransactionsCall extends APICall.Builder<GetHashedSecretPhasedTransactionsCall> {
    private GetHashedSecretPhasedTransactionsCall() {
        super("getHashedSecretPhasedTransactions");
    }

    public static GetHashedSecretPhasedTransactionsCall create() {
        return new GetHashedSecretPhasedTransactionsCall();
    }

    public GetHashedSecretPhasedTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetHashedSecretPhasedTransactionsCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public GetHashedSecretPhasedTransactionsCall phasingHashedSecretAlgorithm(
            byte phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public GetHashedSecretPhasedTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
