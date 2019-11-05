// Auto generated code, do not modify
package nxt.http.callers;

public class SetAliasCall extends CreateTransactionCallBuilder<SetAliasCall> {
    private SetAliasCall() {
        super(ApiSpec.setAlias);
    }

    public static SetAliasCall create(int chain) {
        return new SetAliasCall().param("chain", chain);
    }

    public SetAliasCall aliasURI(String aliasURI) {
        return param("aliasURI", aliasURI);
    }

    public SetAliasCall aliasName(String aliasName) {
        return param("aliasName", aliasName);
    }
}
