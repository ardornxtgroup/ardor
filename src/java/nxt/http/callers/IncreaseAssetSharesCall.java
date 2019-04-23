// Auto generated code, do not modify
package nxt.http.callers;

public class IncreaseAssetSharesCall extends CreateTransactionCallBuilder<IncreaseAssetSharesCall> {
    private IncreaseAssetSharesCall() {
        super("increaseAssetShares");
    }

    public static IncreaseAssetSharesCall create(int chain) {
        return new IncreaseAssetSharesCall().param("chain", chain);
    }

    public IncreaseAssetSharesCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public IncreaseAssetSharesCall asset(String asset) {
        return param("asset", asset);
    }

    public IncreaseAssetSharesCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }
}