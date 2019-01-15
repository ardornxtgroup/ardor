// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DumpPeersCall extends APICall.Builder<DumpPeersCall> {
    private DumpPeersCall() {
        super("dumpPeers");
    }

    public static DumpPeersCall create() {
        return new DumpPeersCall();
    }

    public DumpPeersCall includeNewer(boolean includeNewer) {
        return param("includeNewer", includeNewer);
    }

    public DumpPeersCall service(String... service) {
        return param("service", service);
    }

    public DumpPeersCall version(String version) {
        return param("version", version);
    }

    public DumpPeersCall connect(String connect) {
        return param("connect", connect);
    }

    public DumpPeersCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
