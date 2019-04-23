// Auto generated code, do not modify
package nxt.http.callers;

public class SellAliasCall extends CreateTransactionCallBuilder<SellAliasCall> {
    private SellAliasCall() {
        super("sellAlias");
    }

    public static SellAliasCall create(int chain) {
        return new SellAliasCall().param("chain", chain);
    }

    public SellAliasCall priceNQT(long priceNQT) {
        return param("priceNQT", priceNQT);
    }

    public SellAliasCall aliasName(String aliasName) {
        return param("aliasName", aliasName);
    }

    public SellAliasCall alias(String alias) {
        return param("alias", alias);
    }
}
