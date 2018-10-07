// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetChannelTaggedDataCall extends APICall.Builder<GetChannelTaggedDataCall> {
    private GetChannelTaggedDataCall() {
        super("getChannelTaggedData");
    }

    public static GetChannelTaggedDataCall create(int chain) {
        GetChannelTaggedDataCall instance = new GetChannelTaggedDataCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetChannelTaggedDataCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetChannelTaggedDataCall chain(String chain) {
        return param("chain", chain);
    }

    public GetChannelTaggedDataCall chain(int chain) {
        return param("chain", chain);
    }

    public GetChannelTaggedDataCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetChannelTaggedDataCall channel(String channel) {
        return param("channel", channel);
    }

    public GetChannelTaggedDataCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetChannelTaggedDataCall includeData(boolean includeData) {
        return param("includeData", includeData);
    }

    public GetChannelTaggedDataCall account(String account) {
        return param("account", account);
    }

    public GetChannelTaggedDataCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetChannelTaggedDataCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetChannelTaggedDataCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
