// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class ScanCall extends APICall.Builder<ScanCall> {
    private ScanCall() {
        super("scan");
    }

    public static ScanCall create() {
        return new ScanCall();
    }

    public ScanCall numBlocks(String numBlocks) {
        return param("numBlocks", numBlocks);
    }

    public ScanCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public ScanCall validate(String validate) {
        return param("validate", validate);
    }

    public ScanCall height(int height) {
        return param("height", height);
    }
}
