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

package nxt.account;

import nxt.ae.Asset;
import nxt.blockchain.Chain;
import nxt.ms.Currency;
import nxt.util.security.BlockchainPermission;

public enum HoldingType {

    COIN((byte)0) {

        @Override
        public int getDecimals(long holdingId) {
            return Chain.getChain(Math.toIntExact(holdingId)).getDecimals();
        }

        @Override
        public long getBalance(Account account, long holdingId) {
            return Chain.getChain(Math.toIntExact(holdingId)).getBalanceHome().getBalance(account.getId()).getBalance();
        }

        @Override
        public long getUnconfirmedBalance(Account account, long holdingId) {
            return Chain.getChain(Math.toIntExact(holdingId)).getBalanceHome().getBalance(account.getId()).getUnconfirmedBalance();
        }

        @Override
        public void addToBalance(Account account, AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long holdingId, long amount) {
            account.addToBalance(Chain.getChain(Math.toIntExact(holdingId)), event, eventId, amount);
        }

        @Override
        public void addToUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long holdingId, long amount) {
            account.addToUnconfirmedBalance(Chain.getChain(Math.toIntExact(holdingId)), event, eventId, amount);
        }

        @Override
        public void addToBalanceAndUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long holdingId, long amount) {
            account.addToBalanceAndUnconfirmedBalance(Chain.getChain(Math.toIntExact(holdingId)), event, eventId, amount);
        }

    },

    ASSET((byte)1) {

        @Override
        public int getDecimals(long holdingId) {
            return Asset.getAsset(holdingId).getDecimals();
        }

        @Override
        public long getBalance(Account account, long holdingId) {
            return account.getAssetBalanceQNT(holdingId);
        }

        @Override
        public long getUnconfirmedBalance(Account account, long holdingId) {
            return account.getUnconfirmedAssetBalanceQNT(holdingId);
        }

        @Override
        public void addToBalance(Account account, AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long holdingId, long amount) {
            account.addToAssetBalanceQNT(event, eventId, holdingId, amount);
        }

        @Override
        public void addToUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long holdingId, long amount) {
            account.addToUnconfirmedAssetBalanceQNT(event, eventId, holdingId, amount);
        }

        @Override
        public void addToBalanceAndUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long holdingId, long amount) {
            account.addToAssetAndUnconfirmedAssetBalanceQNT(event, eventId, holdingId, amount);
        }

    },

    CURRENCY((byte)2) {

        @Override
        public int getDecimals(long holdingId) {
            return Currency.getCurrency(holdingId, true).getDecimals();
        }

        @Override
        public long getBalance(Account account, long holdingId) {
            return account.getCurrencyUnits(holdingId);
        }

        @Override
        public long getUnconfirmedBalance(Account account, long holdingId) {
            return account.getUnconfirmedCurrencyUnits(holdingId);
        }

        @Override
        public void addToBalance(Account account, AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long holdingId, long amount) {
            account.addToCurrencyUnits(event, eventId, holdingId, amount);
        }

        @Override
        public void addToUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long holdingId, long amount) {
            account.addToUnconfirmedCurrencyUnits(event, eventId, holdingId, amount);
        }

        @Override
        public void addToBalanceAndUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long holdingId, long amount) {
            account.addToCurrencyAndUnconfirmedCurrencyUnits(event, eventId, holdingId, amount);
        }

    };

    public static HoldingType get(byte code) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("holding"));
        }
        for (HoldingType holdingType : values()) {
            if (holdingType.getCode() == code) {
                return holdingType;
            }
        }
        throw new IllegalArgumentException("Invalid holdingType code: " + code);
    }

    private final byte code;

    HoldingType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public abstract int getDecimals(long holdingId);

    public abstract long getBalance(Account account, long holdingId);

    public abstract long getUnconfirmedBalance(Account account, long holdingId);

    public abstract void addToBalance(Account account, AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long holdingId, long amount);

    public abstract void addToUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long holdingId, long amount);

    public abstract void addToBalanceAndUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long holdingId, long amount);

}
