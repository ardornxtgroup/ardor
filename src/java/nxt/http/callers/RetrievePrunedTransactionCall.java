// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class RetrievePrunedTransactionCall extends APICall.Builder<RetrievePrunedTransactionCall> {
    private RetrievePrunedTransactionCall() {
        super(ApiSpec.retrievePrunedTransaction);
    }

    public static RetrievePrunedTransactionCall create(int chain) {
        return new RetrievePrunedTransactionCall().param("chain", chain);
    }

    public RetrievePrunedTransactionCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public RetrievePrunedTransactionCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }
}
