// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetTransactionCall extends APICall.Builder<GetTransactionCall> {
    private GetTransactionCall() {
        super(ApiSpec.getTransaction);
    }

    public static GetTransactionCall create(int chain) {
        return new GetTransactionCall().param("chain", chain);
    }

    public GetTransactionCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetTransactionCall includePhasingResult(boolean includePhasingResult) {
        return param("includePhasingResult", includePhasingResult);
    }

    public GetTransactionCall fullHash(String fullHash) {
        return param("fullHash", fullHash);
    }

    public GetTransactionCall fullHash(byte[] fullHash) {
        return param("fullHash", fullHash);
    }

    public GetTransactionCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
