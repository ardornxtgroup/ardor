// Auto generated code, do not modify
package nxt.http.callers;

public class LeaseBalanceCall extends CreateTransactionCallBuilder<LeaseBalanceCall> {
    private LeaseBalanceCall() {
        super("leaseBalance");
    }

    public static LeaseBalanceCall create(int chain) {
        return new LeaseBalanceCall().param("chain", chain);
    }

    public LeaseBalanceCall period(String period) {
        return param("period", period);
    }
}
