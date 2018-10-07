// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetCurrencyPhasedTransactionsCall extends APICall.Builder<GetCurrencyPhasedTransactionsCall> {
    private GetCurrencyPhasedTransactionsCall() {
        super("getCurrencyPhasedTransactions");
    }

    public static GetCurrencyPhasedTransactionsCall create(int chain) {
        GetCurrencyPhasedTransactionsCall instance = new GetCurrencyPhasedTransactionsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetCurrencyPhasedTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCurrencyPhasedTransactionsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetCurrencyPhasedTransactionsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetCurrencyPhasedTransactionsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetCurrencyPhasedTransactionsCall withoutWhitelist(String withoutWhitelist) {
        return param("withoutWhitelist", withoutWhitelist);
    }

    public GetCurrencyPhasedTransactionsCall currency(String currency) {
        return param("currency", currency);
    }

    public GetCurrencyPhasedTransactionsCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetCurrencyPhasedTransactionsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetCurrencyPhasedTransactionsCall account(String account) {
        return param("account", account);
    }

    public GetCurrencyPhasedTransactionsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetCurrencyPhasedTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetCurrencyPhasedTransactionsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
