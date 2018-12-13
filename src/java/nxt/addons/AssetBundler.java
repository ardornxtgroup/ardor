package nxt.addons;

import nxt.ae.Asset;
import nxt.ae.AssetExchangeTransactionType;
import nxt.blockchain.Bundler;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;

import java.util.Arrays;
import java.util.Collection;

import static java.util.Collections.unmodifiableList;

/**
 * Bundles only transactions for asset ID provided as parameter
 */
public class AssetBundler implements Bundler.Filter {
    private static final Collection<TransactionType> OK_TYPES = unmodifiableList(Arrays.asList(
            AssetExchangeTransactionType.ASSET_TRANSFER,
            AssetExchangeTransactionType.ASK_ORDER_PLACEMENT,
            AssetExchangeTransactionType.BID_ORDER_PLACEMENT,
            AssetExchangeTransactionType.ASK_ORDER_CANCELLATION,
            AssetExchangeTransactionType.BID_ORDER_CANCELLATION
    ));

    private long assetId;

    @Override
    public boolean ok(Bundler bundler, ChildTransaction childTransaction) {
        TransactionType type = childTransaction.getType();
        if (!(type instanceof AssetExchangeTransactionType)) {
            return false;
        }
        if (!OK_TYPES.contains(type)) {
            return false;
        }
        return assetId == ((AssetExchangeTransactionType) type).getAssetId(childTransaction);
    }

    @Override
    public void setParameter(String parameter) {
        long assetId = Convert.parseUnsignedLong(parameter);
        if (Asset.getAsset(assetId) == null) {
            throw new IllegalArgumentException("Unknown asset " + parameter);
        }
        this.assetId = assetId;
    }

    @Override
    public String getParameter() {
        return Long.toUnsignedString(this.assetId);
    }

    @Override
    public String getName() {
        return "AssetBundler";
    }

    @Override
    public String getDescription() {
        return "Bundles only transactions for asset with ID provided as parameter";
    }
}
