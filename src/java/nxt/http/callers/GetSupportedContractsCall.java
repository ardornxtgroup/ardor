// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetSupportedContractsCall extends APICall.Builder<GetSupportedContractsCall> {
    private GetSupportedContractsCall() {
        super("getSupportedContracts");
    }

    public static GetSupportedContractsCall create() {
        return new GetSupportedContractsCall();
    }

    public GetSupportedContractsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetSupportedContractsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
