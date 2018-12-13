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

package nxt.blockchain;

import nxt.Nxt;
import nxt.account.Account;
import nxt.migration.HoldingMigrateBlockEventHandler;
import nxt.migration.MigrationMonitor;
import nxt.util.Convert;
import nxt.util.Listener;
import nxt.util.Logger;

import static nxt.account.HoldingType.COIN;
import static nxt.blockchain.BlockchainProcessor.Event.AFTER_BLOCK_ACCEPT;

public class ChildChainLoader {

    public static final String CHILD_CHAIN_LOAD_HEIGHT_PROPERTY_PREFIX = "ChildChainLoad";
    public static final long CHILD_CHAIN_LOAD_SETTER_ACCOUNT = Convert.parseAccountId("ARDOR-XNBG-5TB4-SC4R-B73ZW");

    static void init() {
        MigrationMonitor.init();
        Nxt.getBlockchainProcessor().addListener(new HoldingMigrateBlockEventHandler(new ChildChainSnapshot()), AFTER_BLOCK_ACCEPT);
        Account.addPropertyListener(
                new AccountPropertyEventHandler(),
                Account.Event.SET_PROPERTY);
    }

    public static void enableChildChainLoading(ChildChain childChain, int minHeight, int actualHeight) {
        MigrationMonitor.enableMigration(0L, COIN, childChain, minHeight, actualHeight);
    }

    private static class AccountPropertyEventHandler implements Listener<Account.AccountProperty> {
        @Override
        public void notify(Account.AccountProperty property) {
            if (property.getSetterId() != CHILD_CHAIN_LOAD_SETTER_ACCOUNT) {
                return;
            }
            if (property.getSetterId() != property.getRecipientId()) {
                return;
            }
            if (!property.getProperty().startsWith(CHILD_CHAIN_LOAD_HEIGHT_PROPERTY_PREFIX)) {
                return;
            }
            int height;
            int childChainId;
            try {
                height = Integer.parseInt(property.getValue());
                childChainId = Integer.parseInt(property.getProperty().substring(CHILD_CHAIN_LOAD_HEIGHT_PROPERTY_PREFIX.length()));
            } catch (NumberFormatException e) {
                Logger.logDebugMessage("Invalid height or child chain id", e);
                return;
            }
            ChildChain childChain = ChildChain.getChildChain(childChainId);
            if (childChain == null) {
                Logger.logDebugMessage(String.format("Invalid child chain id %d", childChainId));
                return;
            }
            MigrationMonitor.scheduleMigration(childChain, height);
        }
    }

}
