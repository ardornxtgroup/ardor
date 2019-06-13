// Auto generated code, do not modify
package nxt.http.callers;

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

    public CalculateFeeCall minBundlerFeeLimitFQT(long minBundlerFeeLimitFQT) {
        return param("minBundlerFeeLimitFQT", minBundlerFeeLimitFQT);
    }

    public CalculateFeeCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
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
