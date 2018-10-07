// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class CalculateFeeCall extends APICall.Builder<CalculateFeeCall> {
    private CalculateFeeCall() {
        super("calculateFee");
    }

    public static CalculateFeeCall create() {
        return new CalculateFeeCall();
    }

    public CalculateFeeCall transactionJSON(String transactionJSON) {
        return param("transactionJSON", transactionJSON);
    }

    public CalculateFeeCall transactionBytes(String transactionBytes) {
        return param("transactionBytes", transactionBytes);
    }

    public CalculateFeeCall transactionBytes(byte[] transactionBytes) {
        return param("transactionBytes", transactionBytes);
    }

    public CalculateFeeCall prunableAttachmentJSON(String prunableAttachmentJSON) {
        return param("prunableAttachmentJSON", prunableAttachmentJSON);
    }
}
