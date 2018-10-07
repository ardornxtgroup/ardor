// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetPrunableMessagesCall extends APICall.Builder<GetPrunableMessagesCall> {
    private GetPrunableMessagesCall() {
        super("getPrunableMessages");
    }

    public static GetPrunableMessagesCall create(int chain) {
        GetPrunableMessagesCall instance = new GetPrunableMessagesCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetPrunableMessagesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPrunableMessagesCall otherAccount(String otherAccount) {
        return param("otherAccount", otherAccount);
    }

    public GetPrunableMessagesCall chain(String chain) {
        return param("chain", chain);
    }

    public GetPrunableMessagesCall chain(int chain) {
        return param("chain", chain);
    }

    public GetPrunableMessagesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetPrunableMessagesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetPrunableMessagesCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public GetPrunableMessagesCall account(String account) {
        return param("account", account);
    }

    public GetPrunableMessagesCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetPrunableMessagesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetPrunableMessagesCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public GetPrunableMessagesCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
