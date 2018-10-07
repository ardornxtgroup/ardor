// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class RetrievePrunedTransactionCall extends APICall.Builder<RetrievePrunedTransactionCall> {
    private RetrievePrunedTransactionCall() {
        super("retrievePrunedTransaction");
    }

    public static RetrievePrunedTransactionCall create(int chain) {
        RetrievePrunedTransactionCall instance = new RetrievePrunedTransactionCall();
        instance.param("chain", chain);
        return instance;
    }

    public RetrievePrunedTransactionCall chain(String chain) {
        return param("chain", chain);
    }

    public RetrievePrunedTransactionCall chain(int chain) {
        return param("chain", chain);
    }

    public RetrievePrunedTransactionCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public RetrievePrunedTransactionCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }
}
