// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetTransactionCall extends APICall.Builder<GetTransactionCall> {
    private GetTransactionCall() {
        super("getTransaction");
    }

    public static GetTransactionCall create(int chain) {
        GetTransactionCall instance = new GetTransactionCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetTransactionCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetTransactionCall chain(String chain) {
        return param("chain", chain);
    }

    public GetTransactionCall chain(int chain) {
        return param("chain", chain);
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
