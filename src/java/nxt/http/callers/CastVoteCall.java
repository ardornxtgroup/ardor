// Auto generated code, do not modify
package nxt.http.callers;

public class CastVoteCall extends CreateTransactionCallBuilder<CastVoteCall> {
    private CastVoteCall() {
        super("castVote");
    }

    public static CastVoteCall create(int chain) {
        return new CastVoteCall().param("chain", chain);
    }

    public CastVoteCall vote02(String vote02) {
        return param("vote02", vote02);
    }

    public CastVoteCall vote00(String vote00) {
        return param("vote00", vote00);
    }

    public CastVoteCall vote01(String vote01) {
        return param("vote01", vote01);
    }

    public CastVoteCall poll(String poll) {
        return param("poll", poll);
    }

    public CastVoteCall poll(long poll) {
        return unsignedLongParam("poll", poll);
    }
}
