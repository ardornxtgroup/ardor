// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAccountExchangeRequestsCall extends APICall.Builder<GetAccountExchangeRequestsCall> {
    private GetAccountExchangeRequestsCall() {
        super("getAccountExchangeRequests");
    }

    public static GetAccountExchangeRequestsCall create(int chain) {
        GetAccountExchangeRequestsCall instance = new GetAccountExchangeRequestsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAccountExchangeRequestsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountExchangeRequestsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAccountExchangeRequestsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetAccountExchangeRequestsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountExchangeRequestsCall currency(String currency) {
        return param("currency", currency);
    }

    public GetAccountExchangeRequestsCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetAccountExchangeRequestsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountExchangeRequestsCall includeCurrencyInfo(boolean includeCurrencyInfo) {
        return param("includeCurrencyInfo", includeCurrencyInfo);
    }

    public GetAccountExchangeRequestsCall account(String account) {
        return param("account", account);
    }

    public GetAccountExchangeRequestsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountExchangeRequestsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAccountExchangeRequestsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
