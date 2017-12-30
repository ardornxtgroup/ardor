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

package nxt.ms;

import nxt.Nxt;
import nxt.blockchain.Block;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.EntityDbTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class ExchangeRequestHome {

    public static ExchangeRequestHome forChain(ChildChain childChain) {
        if (childChain.getExchangeRequestHome() != null) {
            throw new IllegalStateException("already set");
        }
        return new ExchangeRequestHome(childChain);
    }

    private final ChildChain childChain;
    private final DbKey.HashKeyFactory<ExchangeRequest> exchangeRequestDbKeyFactory;
    private final EntityDbTable<ExchangeRequest> exchangeRequestTable;

    private ExchangeRequestHome(ChildChain childChain) {
        this.childChain = childChain;
        this.exchangeRequestDbKeyFactory = new DbKey.HashKeyFactory<ExchangeRequest>("full_hash", "id") {
            @Override
            public DbKey newKey(ExchangeRequest exchangeRequest) {
                return exchangeRequest.dbKey;
            }
        };
        this.exchangeRequestTable = new EntityDbTable<ExchangeRequest>(childChain.getSchemaTable("exchange_request"), exchangeRequestDbKeyFactory) {
            @Override
            protected ExchangeRequest load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new ExchangeRequest(rs, dbKey);
            }
            @Override
            protected void save(Connection con, ExchangeRequest exchangeRequest) throws SQLException {
                exchangeRequest.save(con);
            }
        };
    }

    public DbIterator<ExchangeRequest> getAllExchangeRequests(int from, int to) {
        return exchangeRequestTable.getAll(from, to);
    }

    public int getCount() {
        return exchangeRequestTable.getCount();
    }

    public DbIterator<ExchangeRequest> getCurrencyExchangeRequests(long currencyId, int from, int to) {
        return exchangeRequestTable.getManyBy(new DbClause.LongClause("currency_id", currencyId), from, to);
    }

    public DbIterator<ExchangeRequest> getAccountExchangeRequests(long accountId, int from, int to) {
        return exchangeRequestTable.getManyBy(new DbClause.LongClause("account_id", accountId), from, to);
    }

    public DbIterator<ExchangeRequest> getAccountCurrencyExchangeRequests(long accountId, long currencyId, int from, int to) {
        return exchangeRequestTable.getManyBy(new DbClause.LongClause("account_id", accountId).and(new DbClause.LongClause("currency_id", currencyId)), from, to);
    }

    void addExchangeRequest(Transaction transaction, ExchangeBuyAttachment attachment) {
        ExchangeRequest exchangeRequest = new ExchangeRequest(transaction, attachment);
        exchangeRequestTable.insert(exchangeRequest);
    }

    void addExchangeRequest(Transaction transaction, ExchangeSellAttachment attachment) {
        ExchangeRequest exchangeRequest = new ExchangeRequest(transaction, attachment);
        exchangeRequestTable.insert(exchangeRequest);
    }

    public final class ExchangeRequest {

        private final long id;
        private final byte[] hash;
        private final long accountId;
        private final long currencyId;
        private final int height;
        private final int timestamp;
        private final DbKey dbKey;
        private final long unitsQNT;
        private final long rateNQT;
        private final boolean isBuy;

        private ExchangeRequest(Transaction transaction, ExchangeBuyAttachment attachment) {
            this(transaction, attachment, true);
        }

        private ExchangeRequest(Transaction transaction, ExchangeSellAttachment attachment) {
            this(transaction, attachment, false);
        }

        private ExchangeRequest(Transaction transaction, ExchangeAttachment attachment, boolean isBuy) {
            this.id = transaction.getId();
            this.hash = transaction.getFullHash();
            this.dbKey = exchangeRequestDbKeyFactory.newKey(this.hash, this.id);
            this.accountId = transaction.getSenderId();
            this.currencyId = attachment.getCurrencyId();
            this.unitsQNT = attachment.getUnitsQNT();
            this.rateNQT = attachment.getRateNQT();
            this.isBuy = isBuy;
            Block block = Nxt.getBlockchain().getLastBlock();
            this.height = block.getHeight();
            this.timestamp = block.getTimestamp();
        }

        private ExchangeRequest(ResultSet rs, DbKey dbKey) throws SQLException {
            this.id = rs.getLong("id");
            this.hash = rs.getBytes("full_hash");
            this.dbKey = dbKey;
            this.accountId = rs.getLong("account_id");
            this.currencyId = rs.getLong("currency_id");
            this.unitsQNT = rs.getLong("units");
            this.rateNQT = rs.getLong("rate");
            this.isBuy = rs.getBoolean("is_buy");
            this.timestamp = rs.getInt("timestamp");
            this.height = rs.getInt("height");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO exchange_request (id, full_hash, account_id, currency_id, "
                    + "units, rate, is_buy, timestamp, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, this.id);
                pstmt.setBytes(++i, this.hash);
                pstmt.setLong(++i, this.accountId);
                pstmt.setLong(++i, this.currencyId);
                pstmt.setLong(++i, this.unitsQNT);
                pstmt.setLong(++i, this.rateNQT);
                pstmt.setBoolean(++i, this.isBuy);
                pstmt.setInt(++i, this.timestamp);
                pstmt.setInt(++i, this.height);
                pstmt.executeUpdate();
            }
        }

        public long getId() {
            return id;
        }

        public byte[] getFullHash() {
            return hash;
        }

        public long getAccountId() {
            return accountId;
        }

        public long getCurrencyId() {
            return currencyId;
        }

        public long getUnitsQNT() {
            return unitsQNT;
        }

        public long getRateNQT() {
            return rateNQT;
        }

        public boolean isBuy() {
            return isBuy;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public int getHeight() {
            return height;
        }

        public ChildChain getChildChain() {
            return childChain;
        }

    }
}
