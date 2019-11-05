// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetContractReferencesCall extends APICall.Builder<GetContractReferencesCall> {
    private GetContractReferencesCall() {
        super(ApiSpec.getContractReferences);
    }

    public static GetContractReferencesCall create() {
        return new GetContractReferencesCall();
    }

    public GetContractReferencesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetContractReferencesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetContractReferencesCall includeContract(boolean includeContract) {
        return param("includeContract", includeContract);
    }

    public GetContractReferencesCall contractName(String contractName) {
        return param("contractName", contractName);
    }

    public GetContractReferencesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetContractReferencesCall account(String account) {
        return param("account", account);
    }

    public GetContractReferencesCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetContractReferencesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetContractReferencesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
