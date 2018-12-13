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

package nxt.ms;

import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.HoldingType;
import nxt.db.DbIterator;
import nxt.dbschema.Db;
import nxt.freeze.AbstractFreezeBlockHandler;
import nxt.freeze.FreezeMonitor;
import nxt.util.Listener;
import nxt.util.Logger;

import static nxt.blockchain.BlockchainProcessor.Event.AFTER_BLOCK_ACCEPT;

public class CurrencyFreezeMonitor {
    private static final FreezeMonitor freezeMonitor = new FreezeMonitor(HoldingType.CURRENCY);
    public static void init() {
        Nxt.getBlockchainProcessor().addListener(new CancelOffersBlockHandler(), AFTER_BLOCK_ACCEPT);
        Account.addPropertyListener(
                new AccountPropertyEventHandler(),
                Account.Event.SET_PROPERTY);
    }

    public static void enableFreeze(long currencyId, int minHeight, int actualHeight) {
        freezeMonitor.enableFreeze(currencyId, minHeight, actualHeight);
    }

    static void checkLiquid(long currencyId) throws NxtException.NotCurrentlyValidException {
        if (freezeMonitor.isFrozen(currencyId)) {
            throw new NxtException.NotCurrentlyValidException("Currency " + Long.toUnsignedString(currencyId) + " is frozen, no transaction is possible.");
        }
    }

    private static class CancelOffersBlockHandler extends AbstractFreezeBlockHandler {
        private CancelOffersBlockHandler() {
            super(HoldingType.CURRENCY);
        }

        @Override
        protected void handleFreeze(long currencyId) {
            cancelOffers(Currency.getCurrency(currencyId));
        }

        private void cancelOffers(Currency currency) {
            ExchangeOfferHome offerHome = currency.getChildChain().getExchangeOfferHome();
            try (DbIterator<ExchangeOfferHome.BuyOffer> offers = offerHome.getCurrencyBuyOffers(currency.getId(), true, -1, -1)) {
                int count = 0;
                for (ExchangeOfferHome.BuyOffer offer : offers) {
                    offerHome.removeOffer(null, offer);
                    if (++count % 1000 == 0) {
                        Db.db.commitTransaction();
                    }
                }
            }
        }
    }

    private static class AccountPropertyEventHandler implements Listener<Account.AccountProperty> {
        @Override
        public void notify(Account.AccountProperty property) {
            if (!property.getProperty().startsWith(Currency.CURRENCY_FREEZE_HEIGHT_PROPERTY_PREFIX)) {
                return;
            }
            if (property.getSetterId() != property.getRecipientId()) {
                return;
            }
            int height;
            long currencyId;
            try {
                height = Integer.parseInt(property.getValue());
                currencyId = Long.parseUnsignedLong(property.getProperty().substring(Currency.CURRENCY_FREEZE_HEIGHT_PROPERTY_PREFIX.length()));
            } catch (NumberFormatException e) {
                Logger.logDebugMessage("Invalid height or currencyId value", e);
                return;
            }
            Currency currency = Currency.getCurrency(currencyId);
            if (currency == null || currency.getAccountId() != property.getSetterId()) {
                return;
            }
            freezeMonitor.scheduleFreeze(currencyId, height);
        }
    }

}
