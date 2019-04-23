// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPeerCall extends APICall.Builder<GetPeerCall> {
    private GetPeerCall() {
        super("getPeer");
    }

    public static GetPeerCall create() {
        return new GetPeerCall();
    }

    public GetPeerCall peer(String peer) {
        return param("peer", peer);
    }
}
