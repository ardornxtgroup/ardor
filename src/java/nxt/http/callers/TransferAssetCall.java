// Auto generated code, do not modify
package nxt.http.callers;

public class TransferAssetCall extends CreateTransactionCallBuilder<TransferAssetCall> {
    private TransferAssetCall() {
        super(ApiSpec.transferAsset);
    }

    public static TransferAssetCall create(int chain) {
        return new TransferAssetCall().param("chain", chain);
    }

    public TransferAssetCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public TransferAssetCall asset(String asset) {
        return param("asset", asset);
    }

    public TransferAssetCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }
}
