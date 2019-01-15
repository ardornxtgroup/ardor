// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class UploadContractRunnerConfigurationCall extends APICall.Builder<UploadContractRunnerConfigurationCall> {
    private UploadContractRunnerConfigurationCall() {
        super("uploadContractRunnerConfiguration");
    }

    public static UploadContractRunnerConfigurationCall create() {
        return new UploadContractRunnerConfigurationCall();
    }

    public UploadContractRunnerConfigurationCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public UploadContractRunnerConfigurationCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public UploadContractRunnerConfigurationCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public APICall.Builder config(byte[] b) {
        return parts("config", b);
    }
}
