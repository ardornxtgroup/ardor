// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetExecutedTransactionsCall extends APICall.Builder<GetExecutedTransactionsCall> {
    private GetExecutedTransactionsCall() {
        super("getExecutedTransactions");
    }

    public static GetExecutedTransactionsCall create(int chain) {
        GetExecutedTransactionsCall instance = new GetExecutedTransactionsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetExecutedTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExecutedTransactionsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetExecutedTransactionsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetExecutedTransactionsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetExecutedTransactionsCall sender(String sender) {
        return param("sender", sender);
    }

    public GetExecutedTransactionsCall sender(long sender) {
        return unsignedLongParam("sender", sender);
    }

    public GetExecutedTransactionsCall subtype(int subtype) {
        return param("subtype", subtype);
    }

    public GetExecutedTransactionsCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public GetExecutedTransactionsCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }

    public GetExecutedTransactionsCall numberOfConfirmations(String numberOfConfirmations) {
        return param("numberOfConfirmations", numberOfConfirmations);
    }

    public GetExecutedTransactionsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetExecutedTransactionsCall type(int type) {
        return param("type", type);
    }

    public GetExecutedTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetExecutedTransactionsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public GetExecutedTransactionsCall height(int height) {
        return param("height", height);
    }
}
