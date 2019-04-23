// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetFxtTransactionCall extends APICall.Builder<GetFxtTransactionCall> {
    private GetFxtTransactionCall() {
        super("getFxtTransaction");
    }

    public static GetFxtTransactionCall create() {
        return new GetFxtTransactionCall();
    }

    public GetFxtTransactionCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetFxtTransactionCall includeChildTransactions(boolean includeChildTransactions) {
        return param("includeChildTransactions", includeChildTransactions);
    }

    public GetFxtTransactionCall fullHash(String fullHash) {
        return param("fullHash", fullHash);
    }

    public GetFxtTransactionCall fullHash(byte[] fullHash) {
        return param("fullHash", fullHash);
    }

    public GetFxtTransactionCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public GetFxtTransactionCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public GetFxtTransactionCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
