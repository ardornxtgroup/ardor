// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class TrimDerivedTablesCall extends APICall.Builder<TrimDerivedTablesCall> {
    private TrimDerivedTablesCall() {
        super("trimDerivedTables");
    }

    public static TrimDerivedTablesCall create() {
        return new TrimDerivedTablesCall();
    }

    public TrimDerivedTablesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
