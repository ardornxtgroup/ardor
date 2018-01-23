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
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.blockchain.ChildChain;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.VersionedEntityDbTable;
import nxt.util.Convert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Each CurrencyFounder instance represents a single founder contribution for a non issued currency
 * Once the currency is issued all founder contributions are removed
 * In case the currency is not issued because of insufficient funding, all funds are returned to the founders
 */
public final class CurrencyFounderHome {

    public static CurrencyFounderHome forChain(ChildChain childChain) {
        if (childChain.getCurrencyFounderHome() != null) {
            throw new IllegalStateException("already set");
        }
        return new CurrencyFounderHome(childChain);
    }

    private final ChildChain childChain;
    private final DbKey.LongLongKeyFactory<CurrencyFounder> currencyFounderDbKeyFactory;
    private final VersionedEntityDbTable<CurrencyFounder> currencyFounderTable;

    private CurrencyFounderHome(ChildChain childChain) {
        this.childChain = childChain;
        this.currencyFounderDbKeyFactory = new DbKey.LongLongKeyFactory<CurrencyFounder>("currency_id", "account_id") {
            @Override
            public DbKey newKey(CurrencyFounder currencyFounder) {
                return currencyFounder.dbKey;
            }
        };
        this.currencyFounderTable = new VersionedEntityDbTable<CurrencyFounder>(childChain.getSchemaTable("currency_founder"), currencyFounderDbKeyFactory) {
            @Override
            protected CurrencyFounder load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new CurrencyFounder(rs, dbKey);
            }
            @Override
            protected void save(Connection con, CurrencyFounder currencyFounder) throws SQLException {
                currencyFounder.save(con);
            }
            @Override
            public String defaultSort() {
                return " ORDER BY height DESC ";
            }
        };
    }

    private void addOrUpdateFounder(long currencyId, long accountId, long amountPerUnit, long amount) {
        CurrencyFounder founder = getFounder(currencyId, accountId);
        if (founder == null) {
            founder = new CurrencyFounder(currencyId, accountId, amountPerUnit, amount);
        } else {
            founder.amountPerUnitNQT += amountPerUnit;
            founder.amountNQT += amount;
        }
        currencyFounderTable.insert(founder);
    }

    public CurrencyFounder getFounder(long currencyId, long accountId) {
        return currencyFounderTable.get(currencyFounderDbKeyFactory.newKey(currencyId, accountId));
    }

    public DbIterator<CurrencyFounder> getCurrencyFounders(long currencyId, int from, int to) {
        return currencyFounderTable.getManyBy(new DbClause.LongClause("currency_id", currencyId), from, to);
    }

    public DbIterator<CurrencyFounder> getFounderCurrencies(long accountId, int from, int to) {
        return currencyFounderTable.getManyBy(new DbClause.LongClause("account_id", accountId), from, to);
    }

    void remove(long currencyId) {
        List<CurrencyFounder> founders = new ArrayList<>();
        try (DbIterator<CurrencyFounder> currencyFounders = getCurrencyFounders(currencyId, 0, Integer.MAX_VALUE)) {
            for (CurrencyFounder founder : currencyFounders) {
                founders.add(founder);
            }
        }
        founders.forEach(currencyFounderTable::delete);
    }

    void increaseReserve(AccountLedger.LedgerEvent event, AccountLedger.LedgerEventId eventId,
                         Account account, long currencyId, long amountPerUnitNQT) {
        Currency currency = Currency.getCurrency(currencyId);
        long amountNQT = Convert.unitRateToAmount(currency.getReserveSupplyQNT(), currency.getDecimals(),
                                    amountPerUnitNQT, childChain.getDecimals());
        account.addToBalance(childChain, event, eventId, -amountNQT);
        currency.increaseReserve(amountPerUnitNQT);
        addOrUpdateFounder(currencyId, account.getId(), amountPerUnitNQT, amountNQT);
    }


    public final class CurrencyFounder {

        private final DbKey dbKey;
        private final long currencyId;
        private final long accountId;
        private long amountPerUnitNQT;
        private long amountNQT;

        private CurrencyFounder(long currencyId, long accountId, long amountPerUnitNQT, long amountNQT) {
            this.currencyId = currencyId;
            this.dbKey = currencyFounderDbKeyFactory.newKey(currencyId, accountId);
            this.accountId = accountId;
            this.amountPerUnitNQT = amountPerUnitNQT;
            this.amountNQT = amountNQT;
        }

        private CurrencyFounder(ResultSet rs, DbKey dbKey) throws SQLException {
            this.currencyId = rs.getLong("currency_id");
            this.accountId = rs.getLong("account_id");
            this.dbKey = dbKey;
            this.amountPerUnitNQT = rs.getLong("amount_per_unit");
            this.amountNQT = rs.getLong("amount");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO currency_founder "
                    + "(currency_id, account_id, amount_per_unit, amount, height, latest) "
                    + "KEY (currency_id, account_id, height) VALUES (?, ?, ?, ?, ?, TRUE)")) {
                int i = 0;
                pstmt.setLong(++i, this.getCurrencyId());
                pstmt.setLong(++i, this.getAccountId());
                pstmt.setLong(++i, this.getAmountPerUnitNQT());
                pstmt.setLong(++i, this.getAmountNQT());
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        public long getCurrencyId() {
            return currencyId;
        }

        public long getAccountId() {
            return accountId;
        }

        public long getAmountPerUnitNQT() {
            return amountPerUnitNQT;
        }

        public long getAmountNQT() {
            return amountNQT;
        }

        public ChildChain getChildChain() {
            return childChain;
        }

    }

}
