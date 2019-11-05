// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAllPrunableMessagesCall extends APICall.Builder<GetAllPrunableMessagesCall> {
    private GetAllPrunableMessagesCall() {
        super(ApiSpec.getAllPrunableMessages);
    }

    public static GetAllPrunableMessagesCall create(int chain) {
        return new GetAllPrunableMessagesCall().param("chain", chain);
    }

    public GetAllPrunableMessagesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllPrunableMessagesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllPrunableMessagesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllPrunableMessagesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAllPrunableMessagesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public GetAllPrunableMessagesCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
