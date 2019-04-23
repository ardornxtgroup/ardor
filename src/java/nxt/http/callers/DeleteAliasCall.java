// Auto generated code, do not modify
package nxt.http.callers;

public class DeleteAliasCall extends CreateTransactionCallBuilder<DeleteAliasCall> {
    private DeleteAliasCall() {
        super("deleteAlias");
    }

    public static DeleteAliasCall create(int chain) {
        return new DeleteAliasCall().param("chain", chain);
    }

    public DeleteAliasCall aliasName(String aliasName) {
        return param("aliasName", aliasName);
    }

    public DeleteAliasCall alias(String alias) {
        return param("alias", alias);
    }
}
