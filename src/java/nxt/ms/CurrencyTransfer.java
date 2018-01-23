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

public final class CurrencyTransfer {

    public enum Event {
        TRANSFER
    }

    private static final Listeners<CurrencyTransfer,Event> listeners = new Listeners<>();

    private static final DbKey.HashKeyFactory<CurrencyTransfer> currencyTransferDbKeyFactory = new DbKey.HashKeyFactory<CurrencyTransfer>("full_hash", "id") {

        @Override
        public DbKey newKey(CurrencyTransfer transfer) {
            return transfer.dbKey;
        }

    };

    private static final EntityDbTable<CurrencyTransfer> currencyTransferTable = new EntityDbTable<CurrencyTransfer>("public.currency_transfer", currencyTransferDbKeyFactory) {

        @Override
        protected CurrencyTransfer load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new CurrencyTransfer(rs, dbKey);
        }

        @Override
        protected void save(Connection con, CurrencyTransfer transfer) throws SQLException {
            transfer.save(con);
        }

    };

    public static DbIterator<CurrencyTransfer> getAllTransfers(int from, int to) {
        return currencyTransferTable.getAll(from, to);
    }

    public static int getCount() {
        return currencyTransferTable.getCount();
    }

    public static boolean addListener(Listener<CurrencyTransfer> listener, Event eventType) {
        return listeners.addListener(listener, eventType);
    }

    public static boolean removeListener(Listener<CurrencyTransfer> listener, Event eventType) {
        return listeners.removeListener(listener, eventType);
    }

    public static DbIterator<CurrencyTransfer> getCurrencyTransfers(long currencyId, int from, int to) {
        return currencyTransferTable.getManyBy(new DbClause.LongClause("currency_id", currencyId), from, to);
    }

    public static DbIterator<CurrencyTransfer> getAccountCurrencyTransfers(long accountId, int from, int to) {
        Connection con = null;
        try {
            con = currencyTransferTable.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM currency_transfer WHERE sender_id = ?"
                    + " UNION ALL SELECT * FROM currency_transfer WHERE recipient_id = ? AND sender_id <> ? ORDER BY height DESC, db_id DESC"
                    + DbUtils.limitsClause(from, to));
            int i = 0;
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, accountId);
            DbUtils.setLimits(++i, pstmt, from, to);
            return currencyTransferTable.getManyBy(con, pstmt, false);
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static DbIterator<CurrencyTransfer> getAccountCurrencyTransfers(long accountId, long currencyId, int from, int to) {
        Connection con = null;
        try {
            con = currencyTransferTable.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM currency_transfer WHERE sender_id = ? AND currency_id = ?"
                    + " UNION ALL SELECT * FROM currency_transfer WHERE recipient_id = ? AND sender_id <> ? AND currency_id = ? ORDER BY height DESC, db_id DESC"
                    + DbUtils.limitsClause(from, to));
            int i = 0;
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, currencyId);
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, currencyId);
            DbUtils.setLimits(++i, pstmt, from, to);
            return currencyTransferTable.getManyBy(con, pstmt, false);
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static int getTransferCount(long currencyId) {
        return currencyTransferTable.getCount(new DbClause.LongClause("currency_id", currencyId));
    }

    static CurrencyTransfer addTransfer(Transaction transaction, CurrencyTransferAttachment attachment) {
        CurrencyTransfer transfer = new CurrencyTransfer(transaction, attachment);
        currencyTransferTable.insert(transfer);
        listeners.notify(transfer, Event.TRANSFER);
        return transfer;
    }

    public static void init() {}


    private final long id;
    private final byte[] hash;
    private final DbKey dbKey;
    private final int chainId;
    private final long currencyId;
    private final int height;
    private final long senderId;
    private final long recipientId;
    private final long unitsQNT;
    private final int timestamp;

    private CurrencyTransfer(Transaction transaction, CurrencyTransferAttachment attachment) {
        this.id = transaction.getId();
        this.hash = transaction.getFullHash();
        this.dbKey = currencyTransferDbKeyFactory.newKey(this.hash, this.id);
        this.chainId = transaction.getChain().getId();
        this.height = Nxt.getBlockchain().getHeight();
        this.currencyId = attachment.getCurrencyId();
        this.senderId = transaction.getSenderId();
        this.recipientId = transaction.getRecipientId();
        this.unitsQNT = attachment.getUnitsQNT();
        this.timestamp = Nxt.getBlockchain().getLastBlockTimestamp();
    }

    private CurrencyTransfer(ResultSet rs, DbKey dbKey) throws SQLException {
        this.id = rs.getLong("id");
        this.hash = rs.getBytes("full_hash");
        this.dbKey = dbKey;
        this.chainId = rs.getInt("chain_id");
        this.currencyId = rs.getLong("currency_id");
        this.senderId = rs.getLong("sender_id");
        this.recipientId = rs.getLong("recipient_id");
        this.unitsQNT = rs.getLong("units");
        this.timestamp = rs.getInt("timestamp");
        this.height = rs.getInt("height");
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO currency_transfer (id, full_hash, chain_id, currency_id, "
                + "sender_id, recipient_id, units, timestamp, height) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            int i = 0;
            pstmt.setLong(++i, this.id);
            pstmt.setBytes(++i, this.hash);
            pstmt.setInt(++i, this.chainId);
            pstmt.setLong(++i, this.currencyId);
            pstmt.setLong(++i, this.senderId);
            pstmt.setLong(++i, this.recipientId);
            pstmt.setLong(++i, this.unitsQNT);
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

    public int getChainId() {
        return chainId;
    }

    public long getCurrencyId() { return currencyId; }

    public long getSenderId() {
        return senderId;
    }

    public long getRecipientId() {
        return recipientId;
    }

    public long getUnitsQNT() {
        return unitsQNT;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getHeight() {
        return height;
    }

}
