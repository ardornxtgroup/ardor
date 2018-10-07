// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class ClearUnconfirmedTransactionsCall extends APICall.Builder<ClearUnconfirmedTransactionsCall> {
    private ClearUnconfirmedTransactionsCall() {
        super("clearUnconfirmedTransactions");
    }

    public static ClearUnconfirmedTransactionsCall create() {
        return new ClearUnconfirmedTransactionsCall();
    }

    public ClearUnconfirmedTransactionsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
