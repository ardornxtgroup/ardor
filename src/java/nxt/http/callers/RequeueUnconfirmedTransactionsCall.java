// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class RequeueUnconfirmedTransactionsCall extends APICall.Builder<RequeueUnconfirmedTransactionsCall> {
    private RequeueUnconfirmedTransactionsCall() {
        super("requeueUnconfirmedTransactions");
    }

    public static RequeueUnconfirmedTransactionsCall create() {
        return new RequeueUnconfirmedTransactionsCall();
    }

    public RequeueUnconfirmedTransactionsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
