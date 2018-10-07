// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class ProcessVoucherCall extends APICall.Builder<ProcessVoucherCall> {
    private ProcessVoucherCall() {
        super("processVoucher");
    }

    public static ProcessVoucherCall create() {
        return new ProcessVoucherCall();
    }

    public ProcessVoucherCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public ProcessVoucherCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public ProcessVoucherCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public ProcessVoucherCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public ProcessVoucherCall validate(String validate) {
        return param("validate", validate);
    }

    public APICall.Builder voucher(byte[] b) {
        return parts("voucher", b);
    }
}
