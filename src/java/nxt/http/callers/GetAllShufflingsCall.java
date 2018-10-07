// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetAllShufflingsCall extends APICall.Builder<GetAllShufflingsCall> {
    private GetAllShufflingsCall() {
        super("getAllShufflings");
    }

    public static GetAllShufflingsCall create(int chain) {
        GetAllShufflingsCall instance = new GetAllShufflingsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetAllShufflingsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllShufflingsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetAllShufflingsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetAllShufflingsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllShufflingsCall includeHoldingInfo(boolean includeHoldingInfo) {
        return param("includeHoldingInfo", includeHoldingInfo);
    }

    public GetAllShufflingsCall finishedOnly(String finishedOnly) {
        return param("finishedOnly", finishedOnly);
    }

    public GetAllShufflingsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllShufflingsCall includeFinished(boolean includeFinished) {
        return param("includeFinished", includeFinished);
    }

    public GetAllShufflingsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAllShufflingsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
