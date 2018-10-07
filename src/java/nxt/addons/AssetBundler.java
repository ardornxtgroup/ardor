package nxt.addons;

import nxt.ae.Asset;
import nxt.ae.AssetExchangeTransactionType;
import nxt.ae.OrderCancellationAttachment;
import nxt.ae.OrderHome;
import nxt.blockchain.Bundler;
import nxt.blockchain.ChildTransaction;
import nxt.util.Convert;

/**
 * Bundles only transactions for asset ID provided as parameter
 */
public class AssetBundler implements Bundler.Filter {

    private long assetId;

    @Override
    public boolean ok(Bundler bundler, ChildTransaction childTransaction) {
        if (childTransaction.getType() instanceof AssetExchangeTransactionType) {
            AssetExchangeTransactionType transactionType = (AssetExchangeTransactionType) childTransaction.getType();
            long transactionAssetId;
            if (transactionType == AssetExchangeTransactionType.ASSET_TRANSFER
                    || transactionType == AssetExchangeTransactionType.ASK_ORDER_PLACEMENT
                    || transactionType == AssetExchangeTransactionType.BID_ORDER_PLACEMENT) {
                transactionAssetId = transactionType.getAssetId(childTransaction);
            } else if (transactionType == AssetExchangeTransactionType.ASK_ORDER_CANCELLATION
                    || transactionType == AssetExchangeTransactionType.BID_ORDER_CANCELLATION) {
                OrderCancellationAttachment attachment = (OrderCancellationAttachment) childTransaction.getAttachment();
                OrderHome orderHome = childTransaction.getChain().getOrderHome();
                if (transactionType == AssetExchangeTransactionType.ASK_ORDER_CANCELLATION) {
                    transactionAssetId = orderHome.getAskOrder(attachment.getOrderId()).getAssetId();
                } else {
                    transactionAssetId = orderHome.getBidOrder(attachment.getOrderId()).getAssetId();
                }
            } else {
                return false;
            }
            return transactionAssetId == assetId;
        }
        return false;
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
