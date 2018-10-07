// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetVoterPhasedTransactionsCall extends APICall.Builder<GetVoterPhasedTransactionsCall> {
    private GetVoterPhasedTransactionsCall() {
        super("getVoterPhasedTransactions");
    }

    public static GetVoterPhasedTransactionsCall create(int chain) {
        GetVoterPhasedTransactionsCall instance = new GetVoterPhasedTransactionsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetVoterPhasedTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetVoterPhasedTransactionsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetVoterPhasedTransactionsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetVoterPhasedTransactionsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetVoterPhasedTransactionsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetVoterPhasedTransactionsCall account(String account) {
        return param("account", account);
    }

    public GetVoterPhasedTransactionsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetVoterPhasedTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetVoterPhasedTransactionsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
