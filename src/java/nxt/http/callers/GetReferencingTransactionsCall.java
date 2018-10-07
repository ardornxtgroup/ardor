// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetReferencingTransactionsCall extends APICall.Builder<GetReferencingTransactionsCall> {
    private GetReferencingTransactionsCall() {
        super("getReferencingTransactions");
    }

    public static GetReferencingTransactionsCall create(int chain) {
        GetReferencingTransactionsCall instance = new GetReferencingTransactionsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetReferencingTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetReferencingTransactionsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetReferencingTransactionsCall chain(int chain) {
        return param("chain", chain);
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
