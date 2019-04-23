// Auto generated code, do not modify
package nxt.http.callers;

public class BundleTransactionsCall extends CreateTransactionCallBuilder<BundleTransactionsCall> {
    private BundleTransactionsCall() {
        super("bundleTransactions");
    }

    public static BundleTransactionsCall create(int chain) {
        return new BundleTransactionsCall().param("chain", chain);
    }

    public BundleTransactionsCall transactionFullHash(String... transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public BundleTransactionsCall transactionFullHash(byte[]... transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }
}
