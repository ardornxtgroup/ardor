// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class SetAPIProxyPeerCall extends APICall.Builder<SetAPIProxyPeerCall> {
    private SetAPIProxyPeerCall() {
        super("setAPIProxyPeer");
    }

    public static SetAPIProxyPeerCall create() {
        return new SetAPIProxyPeerCall();
    }

    public SetAPIProxyPeerCall peer(String peer) {
        return param("peer", peer);
    }

    public SetAPIProxyPeerCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
