/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2019 Jelurida IP B.V.
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

package nxt.ms;

import nxt.Nxt;
import nxt.account.Account;
import nxt.account.HoldingType;
import nxt.blockchain.Block;
import nxt.blockchain.ChildChain;
import nxt.migration.HoldingMigrateBlockEventHandler;
import nxt.migration.MigrationMonitor;
import nxt.migration.SaveHoldingSnapshotListener;
import nxt.util.Listener;
import nxt.util.Logger;

import static nxt.account.HoldingType.CURRENCY;
import static nxt.blockchain.BlockchainProcessor.Event.AFTER_BLOCK_ACCEPT;

public class CurrencyMigrateMonitor {
    static void init() {
        MigrationMonitor.init();
        Nxt.getBlockchainProcessor().addListener(new HoldingMigrateBlockEventHandler(new CurrencySnapshot()), AFTER_BLOCK_ACCEPT);
        Account.addPropertyListener(
                new AccountPropertyEventHandler(),
                Account.Event.SET_PROPERTY);
    }

    public static void enableMigration(long currencyId, ChildChain targetChain, int minHeight, int actualHeight) {
        MigrationMonitor.enableMigration(currencyId, CURRENCY, targetChain, minHeight, actualHeight);
    }

    public static Listener<Block> getSaveCurrencySnapshotListener() {
        return new SaveHoldingSnapshotListener(new CurrencySnapshot());
    }

    private static class AccountPropertyEventHandler implements Listener<Account.AccountProperty> {
        @Override
        public void notify(Account.AccountProperty property) {
            if (!property.getProperty().startsWith(Currency.CURRENCY_MIGRATE_HEIGHT_PROPERTY_PREFIX)) {
                return;
            }
            if (property.getSetterId() != property.getRecipientId()) {
                return;
            }
            int height;
            long currencyId;
            try {
                height = Integer.parseInt(property.getValue());
                currencyId = Long.parseUnsignedLong(property.getProperty().substring(Currency.CURRENCY_MIGRATE_HEIGHT_PROPERTY_PREFIX.length()));
            } catch (NumberFormatException e) {
                Logger.logDebugMessage("Invalid height or currencyId value", e);
                return;
            }
            Currency currency = Currency.getCurrency(currencyId);
            if (currency == null || currency.getAccountId() != property.getSetterId()) {
                return;
            }
            MigrationMonitor.scheduleMigration(HoldingType.CURRENCY, currencyId, height);
        }
    }

}
