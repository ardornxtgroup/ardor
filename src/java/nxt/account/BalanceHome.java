/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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

import nxt.Constants;
import nxt.Nxt;
import nxt.blockchain.Chain;
import nxt.blockchain.FxtChain;
import nxt.db.DbKey;
import nxt.db.VersionedEntityDbTable;
import nxt.util.Listener;
import nxt.util.Listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class BalanceHome {

    public enum Event {
        BALANCE, UNCONFIRMED_BALANCE
    }

    public static BalanceHome forChain(Chain chain) {
        if (chain.getBalanceHome() != null) {
            throw new IllegalStateException("already set");
        }
        return new BalanceHome(chain);
    }

    private static final Listeners<Balance, Event> listeners = new Listeners<>();

    public static boolean addListener(Listener<Balance> listener, Event eventType) {
        return listeners.addListener(listener, eventType);
    }

    public static boolean removeListener(Listener<Balance> listener, Event eventType) {
        return listeners.removeListener(listener, eventType);
    }

    private final DbKey.LongKeyFactory<Balance> balanceDbKeyFactory;
    private final VersionedEntityDbTable<Balance> balanceTable;
    private final Chain chain;

    private BalanceHome(Chain chain) {
        this.chain = chain;
        this.balanceDbKeyFactory = new DbKey.LongKeyFactory<Balance>("account_id") {
            @Override
            public DbKey newKey(Balance balance) {
                return balance.dbKey == null ? newKey(balance.accountId) : balance.dbKey;
            }
            @Override
            public Balance newEntity(DbKey dbKey) {
                return new Balance(((DbKey.LongKey)dbKey).getId());
            }
        };
        if (chain instanceof FxtChain) {
            this.balanceTable = new VersionedEntityDbTable<Balance>(chain.getSchemaTable("balance_fxt"), balanceDbKeyFactory) {
                @Override
                protected Balance load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                    return new Balance(rs, dbKey);
                }
                @Override
                protected void save(Connection con, Balance balance) throws SQLException {
                    balance.save(con);
                }
                @Override
                public void trim(int height) {
                    if (height <= Constants.GUARANTEED_BALANCE_CONFIRMATIONS) {
                        return;
                    }
                    super.trim(height);
                }
                @Override
                public void checkAvailable(int height) {
                    if (height > Constants.GUARANTEED_BALANCE_CONFIRMATIONS) {
                        super.checkAvailable(height);
                        return;
                    }
                    if (height > Nxt.getBlockchain().getHeight()) {
                        throw new IllegalArgumentException("Height " + height + " exceeds blockchain height " + Nxt.getBlockchain().getHeight());
                    }
                }
            };
        } else {
            this.balanceTable = new VersionedEntityDbTable<Balance>(chain.getSchemaTable("balance"), balanceDbKeyFactory) {
                @Override
                protected Balance load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                    return new Balance(rs, dbKey);
                }
                @Override
                protected void save(Connection con, Balance balance) throws SQLException {
                    balance.save(con);
                }
            };
        }
    }

    public Balance getBalance(long accountId) {
        DbKey dbKey = balanceDbKeyFactory.newKey(accountId);
        Balance balance = balanceTable.get(dbKey);
        if (balance == null) {
            balance = balanceTable.newEntity(dbKey);
        }
        return balance;
    }

    public Balance getBalance(long accountId, int height) {
        DbKey dbKey = balanceDbKeyFactory.newKey(accountId);
        Balance balance = balanceTable.get(dbKey, height);
        if (balance == null) {
            balance = new Balance(accountId);
        }
        return balance;
    }

    public final class Balance {

        private final long accountId;
        private final DbKey dbKey;
        private long balance;
        private long unconfirmedBalance;

        Balance(long accountId) {
            this.accountId = accountId;
            this.dbKey = balanceDbKeyFactory.newKey(accountId);
            this.balance = 0L;
            this.unconfirmedBalance = 0L;
        }

        private Balance(ResultSet rs, DbKey dbKey) throws SQLException {
            this.accountId = rs.getLong("account_id");
            this.dbKey = dbKey;
            this.balance = rs.getLong("balance");
            this.unconfirmedBalance = rs.getLong("unconfirmed_balance");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO " + balanceTable.getSchemaTable() + " (account_id, "
                    + "balance, unconfirmed_balance, height, latest) "
                    + "KEY (account_id, height) VALUES (?, ?, ?, ?, TRUE)")) {
                int i = 0;
                pstmt.setLong(++i, this.accountId);
                pstmt.setLong(++i, this.balance);
                pstmt.setLong(++i, this.unconfirmedBalance);
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        private void save() {
            if (balance == 0 && unconfirmedBalance == 0) {
                balanceTable.delete(this, true);
            } else {
                balanceTable.insert(this);
            }
        }

        public Chain getChain() {
            return BalanceHome.this.chain;
        }

        public long getAccountId() {
            return accountId;
        }

        public long getBalance() {
            return balance;
        }

        public long getUnconfirmedBalance() {
            return unconfirmedBalance;
        }

        public void addToBalance(AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long amount) {
            addToBalance(event, eventId, amount, 0);
        }

        public void addToBalance(AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long amount, long fee) {
            if (amount == 0 && fee == 0) {
                return;
            }
            long totalAmount = Math.addExact(amount, fee);
            this.balance = Math.addExact(this.balance, totalAmount);
            if (chain == FxtChain.FXT) {
                Account.addToGuaranteedBalanceFQT(this.accountId, totalAmount);
            }
            Account.checkBalance(this.accountId, this.balance, this.unconfirmedBalance);
            save();
            listeners.notify(this, Event.BALANCE);
            if (AccountLedger.mustLogEntry(event, this.accountId, false)) {
                if (fee != 0) {
                    AccountLedger.logEntry(new AccountLedger.LedgerEntry(AccountLedger.LedgerEvent.TRANSACTION_FEE, eventId, this.accountId,
                            AccountLedger.LedgerHolding.COIN_BALANCE, chain.getId(), fee, this.balance - amount));
                }
                if (amount != 0) {
                    AccountLedger.logEntry(new AccountLedger.LedgerEntry(event, eventId, this.accountId,
                            AccountLedger.LedgerHolding.COIN_BALANCE, chain.getId(), amount, this.balance));
                }
            }
        }

        public void addToUnconfirmedBalance(AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long amount) {
            addToUnconfirmedBalance(event, eventId, amount, 0);
        }

        void addToUnconfirmedBalance(AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long amount, long fee) {
            if (amount == 0 && fee == 0) {
                return;
            }
            long totalAmount = Math.addExact(amount, fee);
            this.unconfirmedBalance = Math.addExact(this.unconfirmedBalance, totalAmount);
            Account.checkBalance(this.accountId, this.balance, this.unconfirmedBalance);
            save();
            listeners.notify(this, Event.UNCONFIRMED_BALANCE);
            if (AccountLedger.mustLogEntry(event, this.accountId, true)) {
                if (fee != 0) {
                    AccountLedger.logEntry(new AccountLedger.LedgerEntry(AccountLedger.LedgerEvent.TRANSACTION_FEE, eventId, this.accountId,
                            AccountLedger.LedgerHolding.UNCONFIRMED_COIN_BALANCE, chain.getId(), fee, this.unconfirmedBalance - amount));
                }
                if (amount != 0) {
                    AccountLedger.logEntry(new AccountLedger.LedgerEntry(event, eventId, this.accountId,
                            AccountLedger.LedgerHolding.UNCONFIRMED_COIN_BALANCE, chain.getId(), amount, this.unconfirmedBalance));
                }
            }
        }

        public void addToBalanceAndUnconfirmedBalance(AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long amount) {
            addToBalanceAndUnconfirmedBalance(event, eventId, amount, 0);
        }

        void addToBalanceAndUnconfirmedBalance(AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId, long amount, long fee) {
            if (amount == 0 && fee == 0) {
                return;
            }
            long totalAmount = Math.addExact(amount, fee);
            this.balance = Math.addExact(this.balance, totalAmount);
            this.unconfirmedBalance = Math.addExact(this.unconfirmedBalance, totalAmount);
            if (chain == FxtChain.FXT) {
                Account.addToGuaranteedBalanceFQT(this.accountId, totalAmount);
            }
            Account.checkBalance(this.accountId, this.balance, this.unconfirmedBalance);
            save();
            listeners.notify(this, Event.BALANCE);
            listeners.notify(this, Event.UNCONFIRMED_BALANCE);
            if (AccountLedger.mustLogEntry(event, this.accountId, true)) {
                if (fee != 0) {
                    AccountLedger.logEntry(new AccountLedger.LedgerEntry(AccountLedger.LedgerEvent.TRANSACTION_FEE, eventId, this.accountId,
                            AccountLedger.LedgerHolding.UNCONFIRMED_COIN_BALANCE, chain.getId(), fee, this.unconfirmedBalance - amount));
                }
                if (amount != 0) {
                    AccountLedger.logEntry(new AccountLedger.LedgerEntry(event, eventId, this.accountId,
                            AccountLedger.LedgerHolding.UNCONFIRMED_COIN_BALANCE, chain.getId(), amount, this.unconfirmedBalance));
                }
            }
            if (AccountLedger.mustLogEntry(event, this.accountId, false)) {
                if (fee != 0) {
                    AccountLedger.logEntry(new AccountLedger.LedgerEntry(AccountLedger.LedgerEvent.TRANSACTION_FEE, eventId, this.accountId,
                            AccountLedger.LedgerHolding.COIN_BALANCE, chain.getId(), fee, this.balance - amount));
                }
                if (amount != 0) {
                    AccountLedger.logEntry(new AccountLedger.LedgerEntry(event, eventId, this.accountId,
                            AccountLedger.LedgerHolding.COIN_BALANCE, chain.getId(), amount, this.balance));
                }
            }
        }

    }
}
