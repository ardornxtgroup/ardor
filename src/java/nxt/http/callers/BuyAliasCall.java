// Auto generated code, do not modify
package nxt.http.callers;

public class BuyAliasCall extends CreateTransactionCallBuilder<BuyAliasCall> {
    private BuyAliasCall() {
        super("buyAlias");
    }

    public static BuyAliasCall create(int chain) {
        return new BuyAliasCall().param("chain", chain);
    }

    public BuyAliasCall aliasName(String aliasName) {
        return param("aliasName", aliasName);
    }

    public BuyAliasCall amountNQT(long amountNQT) {
        return param("amountNQT", amountNQT);
    }

    public BuyAliasCall alias(String alias) {
        return param("alias", alias);
    }
}
