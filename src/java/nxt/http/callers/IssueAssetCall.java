// Auto generated code, do not modify
package nxt.http.callers;

public class IssueAssetCall extends CreateTransactionCallBuilder<IssueAssetCall> {
    private IssueAssetCall() {
        super(ApiSpec.issueAsset);
    }

    public static IssueAssetCall create(int chain) {
        return new IssueAssetCall().param("chain", chain);
    }

    public IssueAssetCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public IssueAssetCall decimals(String decimals) {
        return param("decimals", decimals);
    }

    public IssueAssetCall name(String name) {
        return param("name", name);
    }

    public IssueAssetCall description(String description) {
        return param("description", description);
    }
}
