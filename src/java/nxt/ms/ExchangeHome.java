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
import nxt.db.DbUtils;
import nxt.db.EntityDbTable;
import nxt.util.Listener;
import nxt.util.Listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class ExchangeHome {

    public enum Event {
        EXCHANGE
    }

    public static ExchangeHome forChain(ChildChain childChain) {
        if (childChain.getExchangeHome() != null) {
            throw new IllegalStateException("already set");
        }
        return new ExchangeHome(childChain);
    }

    private static final Listeners<Exchange, Event> listeners = new Listeners<>();

    public static boolean addListener(Listener<Exchange> listener, Event eventType) {
        return listeners.addListener(listener, eventType);
    }

    public static boolean removeListener(Listener<Exchange> listener, Event eventType) {
        return listeners.removeListener(listener, eventType);
    }

    private final ChildChain childChain;
    private final DbKey.HashHashKeyFactory<Exchange> exchangeDbKeyFactory;
    private final EntityDbTable<Exchange> exchangeTable;

    private ExchangeHome(ChildChain childChain) {
        this.childChain = childChain;
        this.exchangeDbKeyFactory = new DbKey.HashHashKeyFactory<Exchange>("transaction_full_hash", "transaction_id",
                "offer_full_hash", "offer_id") {
            @Override
            public DbKey newKey(Exchange exchange) {
                return exchange.dbKey;
            }
        };
        this.exchangeTable = new EntityDbTable<Exchange>(childChain.getSchemaTable("exchange"), exchangeDbKeyFactory) {
            @Override
            protected Exchange load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new Exchange(rs, dbKey);
            }
            @Override
            protected void save(Connection con, Exchange exchange) throws SQLException {
                exchange.save(con);
            }
        };
    }

    public DbIterator<Exchange> getAllExchanges(int from, int to) {
        return exchangeTable.getAll(from, to);
    }

    public int getCount() {
        return exchangeTable.getCount();
    }

    public DbIterator<Exchange> getCurrencyExchanges(long currencyId, int from, int to) {
        return exchangeTable.getManyBy(new DbClause.LongClause("currency_id", currencyId), from, to);
    }

    public List<Exchange> getLastExchanges(long[] currencyIds) {
        try (Connection con = exchangeTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM exchange WHERE currency_id = ? ORDER BY height DESC, db_id DESC LIMIT 1")) {
            List<Exchange> result = new ArrayList<>();
            for (long currencyId : currencyIds) {
                pstmt.setLong(1, currencyId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        result.add(new Exchange(rs, null));
                    }
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public DbIterator<Exchange> getAccountExchanges(long accountId, int from, int to) {
        Connection con = null;
        try {
            con = exchangeTable.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM exchange WHERE seller_id = ?"
                    + " UNION ALL SELECT * FROM exchange WHERE buyer_id = ? AND seller_id <> ? ORDER BY height DESC, db_id DESC"
                    + DbUtils.limitsClause(from, to));
            int i = 0;
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, accountId);
            DbUtils.setLimits(++i, pstmt, from, to);
            return exchangeTable.getManyBy(con, pstmt, false);
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public DbIterator<Exchange> getAccountCurrencyExchanges(long accountId, long currencyId, int from, int to) {
        Connection con = null;
        try {
            con = exchangeTable.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM exchange WHERE seller_id = ? AND currency_id = ?"
                    + " UNION ALL SELECT * FROM exchange WHERE buyer_id = ? AND seller_id <> ? AND currency_id = ? ORDER BY height DESC, db_id DESC"
                    + DbUtils.limitsClause(from, to));
            int i = 0;
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, currencyId);
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, currencyId);
            DbUtils.setLimits(++i, pstmt, from, to);
            return exchangeTable.getManyBy(con, pstmt, false);
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public DbIterator<Exchange> getExchanges(long transactionId) {
        return exchangeTable.getManyBy(new DbClause.LongClause("transaction_id", transactionId), 0, -1);
    }

    public DbIterator<Exchange> getOfferExchanges(long offerId, int from, int to) {
        return exchangeTable.getManyBy(new DbClause.LongClause("offer_id", offerId), from, to);
    }

    public int getExchangeCount(long currencyId) {
        return exchangeTable.getCount(new DbClause.LongClause("currency_id", currencyId));
    }

    Exchange addExchange(Transaction transaction, long currencyId, ExchangeOfferHome.ExchangeOffer offer,
                         long sellerId, long buyerId, long units) {
        Exchange exchange = new Exchange(transaction.getId(), transaction.getFullHash(), currencyId, offer,
                sellerId, buyerId, units);
        exchangeTable.insert(exchange);
        listeners.notify(exchange, Event.EXCHANGE);
        return exchange;
    }


    public final class Exchange {

        private final long transactionId;
        private final byte[] transactionHash;
        private final int timestamp;
        private final long currencyId;
        private final long blockId;
        private final int height;
        private final long offerId;
        private final byte[] offerHash;
        private final long sellerId;
        private final long buyerId;
        private final DbKey dbKey;
        private final long unitsQNT;
        private final long rateNQT;

        private Exchange(long transactionId, byte[] transactionHash, long currencyId, ExchangeOfferHome.ExchangeOffer offer,
                         long sellerId, long buyerId, long unitsQNT) {
            Block block = Nxt.getBlockchain().getLastBlock();
            this.transactionId = transactionId;
            this.transactionHash = transactionHash;
            this.blockId = block.getId();
            this.height = block.getHeight();
            this.currencyId = currencyId;
            this.timestamp = block.getTimestamp();
            this.offerId = offer.getId();
            this.offerHash = offer.getFullHash();
            this.sellerId = sellerId;
            this.buyerId = buyerId;
            this.dbKey = exchangeDbKeyFactory.newKey(this.transactionHash, this.transactionId, this.offerHash, this.offerId);
            this.unitsQNT = unitsQNT;
            this.rateNQT = offer.getRateNQT();
        }

        private Exchange(ResultSet rs, DbKey dbKey) throws SQLException {
            this.transactionId = rs.getLong("transaction_id");
            this.transactionHash = rs.getBytes("transaction_full_hash");
            this.currencyId = rs.getLong("currency_id");
            this.blockId = rs.getLong("block_id");
            this.offerId = rs.getLong("offer_id");
            this.offerHash = rs.getBytes("offer_full_hash");
            this.sellerId = rs.getLong("seller_id");
            this.buyerId = rs.getLong("buyer_id");
            this.dbKey = dbKey;
            this.unitsQNT = rs.getLong("units");
            this.rateNQT = rs.getLong("rate");
            this.timestamp = rs.getInt("timestamp");
            this.height = rs.getInt("height");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO exchange (transaction_id, transaction_full_hash, currency_id, block_id, "
                    + "offer_id, offer_full_hash, seller_id, buyer_id, units, rate, timestamp, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, this.transactionId);
                pstmt.setBytes(++i, this.transactionHash);
                pstmt.setLong(++i, this.currencyId);
                pstmt.setLong(++i, this.blockId);
                pstmt.setLong(++i, this.offerId);
                pstmt.setBytes(++i, this.offerHash);
                pstmt.setLong(++i, this.sellerId);
                pstmt.setLong(++i, this.buyerId);
                pstmt.setLong(++i, this.unitsQNT);
                pstmt.setLong(++i, this.rateNQT);
                pstmt.setInt(++i, this.timestamp);
                pstmt.setInt(++i, this.height);
                pstmt.executeUpdate();
            }
        }

        public long getTransactionId() {
            return transactionId;
        }

        public byte[] getTransactionFullHash() {
            return transactionHash;
        }

        public long getBlockId() {
            return blockId;
        }

        public long getOfferId() {
            return offerId;
        }

        public byte[] getOfferFullHash() {
            return offerHash;
        }

        public long getSellerId() {
            return sellerId;
        }

        public long getBuyerId() {
            return buyerId;
        }

        public long getUnitsQNT() {
            return unitsQNT;
        }

        public long getRateNQT() {
            return rateNQT;
        }

        public long getCurrencyId() {
            return currencyId;
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

        @Override
        public String toString() {
            return "Exchange currency: " + Long.toUnsignedString(currencyId) + " offer: " + Long.toUnsignedString(offerId)
                    + " rate: " + rateNQT + " units: " + unitsQNT + " height: " + height + " transaction: " + Long.toUnsignedString(transactionId);
        }
    }

}
