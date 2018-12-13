/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.ae;

import nxt.Nxt;
import nxt.NxtException;
import nxt.account.HoldingType;
import nxt.blockchain.BlockchainProcessor;
import nxt.blockchain.ChildChain;
import nxt.db.DbIterator;
import nxt.dbschema.Db;
import nxt.freeze.AbstractFreezeBlockHandler;
import nxt.freeze.FreezeMonitor;
import nxt.util.Listener;
import nxt.util.Logger;

import java.util.Iterator;

public class AssetFreezeMonitor {
    private static final FreezeMonitor freezeMonitor = new FreezeMonitor(HoldingType.ASSET);
    public static void init() {
        Nxt.getBlockchainProcessor().addListener(new CancelOrdersBlockHandler(), BlockchainProcessor.Event.AFTER_BLOCK_ACCEPT);
        Asset.addListener(
                new AssetPropertyEventHandler(),
                Asset.Event.SET_PROPERTY);
    }

    static void checkLiquid(long assetId) throws NxtException.NotCurrentlyValidException {
        if (freezeMonitor.isFrozen(assetId)) {
            throw new NxtException.NotCurrentlyValidException("Asset " + Long.toUnsignedString(assetId) + " is frozen, no transaction is possible.");
        }
    }

    public static void enableFreeze(long assetId, int minHeight, int actualHeight) {
        freezeMonitor.enableFreeze(assetId, minHeight, actualHeight);
    }

    private static class CancelOrdersBlockHandler extends AbstractFreezeBlockHandler {
        private CancelOrdersBlockHandler() {
            super(HoldingType.ASSET);
        }

        @Override
        protected void handleFreeze(long assetId) {
            for (ChildChain childChain : ChildChain.getAll()) {
                cancelOrders(childChain, assetId);
            }
        }

        private static void cancelOrders(ChildChain chain, long assetId) {
            cancelOrders(chain.getOrderHome(), assetId);
        }

        private static void cancelOrders(OrderHome orderHome, long assetId) {
            try (DbIterator<OrderHome.Ask> orders = orderHome.getAskOrdersByAsset(assetId, 0, -1)) {
                int count = cancelOrders(orders);
                Logger.logDebugMessage("Cancelled " + count + " ask orders for asset " + Long.toUnsignedString(assetId));
            }
            try (DbIterator<OrderHome.Bid> orders = orderHome.getBidOrdersByAsset(assetId, 0, -1)) {
                int count = cancelOrders(orders);
                Logger.logDebugMessage("Cancelled " + count + " bid orders for asset " + Long.toUnsignedString(assetId));
            }
        }

        private static int cancelOrders(Iterator<? extends OrderHome.Order> orders) {
            int count = 0;
            while (orders.hasNext()) {
                orders.next().cancelOrder(null);
                if (++count % 1000 == 0) {
                    Db.db.commitTransaction();
                }
            }
            return count;
        }
    }

    private static class AssetPropertyEventHandler implements Listener<Asset.AssetProperty> {
        @Override
        public void notify(Asset.AssetProperty property) {
            if (!Asset.ASSET_FREEZE_HEIGHT_PROPERTY.equals(property.getProperty())) {
                return;
            }
            int height;
            try {
                height = Integer.parseInt(property.getValue());
            } catch (NumberFormatException e) {
                Logger.logDebugMessage("Invalid height value", e);
                return;
            }
            long assetId = property.getAssetId();
            Asset asset = Asset.getAsset(assetId);
            if (asset.getAccountId() != property.getSetterId()) {
                return;
            }
            freezeMonitor.scheduleFreeze(property.getAssetId(), height);
        }
    }

}
