// Auto generated code, do not modify
package nxt.http.callers;

public class DeleteAssetSharesCall extends CreateTransactionCallBuilder<DeleteAssetSharesCall> {
    private DeleteAssetSharesCall() {
        super("deleteAssetShares");
    }

    public static DeleteAssetSharesCall create(int chain) {
        return new DeleteAssetSharesCall().param("chain", chain);
    }

    public DeleteAssetSharesCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public DeleteAssetSharesCall asset(String asset) {
        return param("asset", asset);
    }

    public DeleteAssetSharesCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }
}