// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetReferencingTransactionsCall extends APICall.Builder<GetReferencingTransactionsCall> {
    private GetReferencingTransactionsCall() {
        super(ApiSpec.getReferencingTransactions);
    }

    public static GetReferencingTransactionsCall create(int chain) {
        return new GetReferencingTransactionsCall().param("chain", chain);
    }

    public GetReferencingTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetReferencingTransactionsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetReferencingTransactionsCall transactionFullHash(String transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetReferencingTransactionsCall transactionFullHash(byte[] transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public GetReferencingTransactionsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetReferencingTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetReferencingTransactionsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
