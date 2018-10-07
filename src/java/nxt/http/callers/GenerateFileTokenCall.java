// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GenerateFileTokenCall extends APICall.Builder<GenerateFileTokenCall> {
    private GenerateFileTokenCall() {
        super("generateFileToken");
    }

    public static GenerateFileTokenCall create() {
        return new GenerateFileTokenCall();
    }

    public GenerateFileTokenCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public APICall.Builder file(byte[] b) {
        return parts("file", b);
    }
}
