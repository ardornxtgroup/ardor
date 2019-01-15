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

package nxt.blockchain;

import nxt.NxtException;
import nxt.db.Table;
import nxt.dbschema.Db;
import nxt.util.Convert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TransactionHome {

    public static TransactionHome forChain(Chain chain) {
        if (chain.getTransactionHome() != null) {
            throw new IllegalStateException("already set");
        }
        return new TransactionHome(chain);
    }

    private final Chain chain;
    private final Table transactionTable;

    private TransactionHome(Chain chain) {
        this.chain = chain;
        transactionTable = new Table(chain.getSchemaTable(chain instanceof FxtChain ? "transaction_fxt" : "transaction"));
    }

    static FxtTransactionImpl findFxtTransaction(long transactionId) {
        try (Connection con = Db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM transaction_fxt WHERE id = ? ORDER BY height DESC")) {
            pstmt.setLong(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return (FxtTransactionImpl)TransactionImpl.loadTransaction(FxtChain.FXT, rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        } catch (NxtException.ValidationException e) {
            throw new RuntimeException("Transaction already in database, id = " + transactionId + ", does not pass validation!", e);
        }
    }

    public TransactionImpl findTransaction(byte[] fullHash) {
        return findTransaction(fullHash, Integer.MAX_VALUE);
    }

    public TransactionImpl findTransaction(byte[] fullHash, int height) {
        long transactionId = Convert.fullHashToId(fullHash);
        try (Connection con = transactionTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM " + transactionTable.getSchemaTable() + " WHERE id = ?")) {
            pstmt.setLong(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    if (Arrays.equals(rs.getBytes("full_hash"), fullHash) && rs.getInt("height") <= height) {
                        return TransactionImpl.loadTransaction(chain, rs);
                    }
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        } catch (NxtException.ValidationException e) {
            throw new RuntimeException("Transaction already in database, full_hash = " + Convert.toHexString(fullHash)
                    + ", does not pass validation!", e);
        }
    }

    static boolean hasFxtTransaction(long transactionId) {
        return hasFxtTransaction(transactionId, Integer.MAX_VALUE);
    }

    static boolean hasFxtTransaction(long transactionId, int height) {
        try (Connection con = Db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT height FROM transaction_fxt WHERE id = ? ORDER BY height DESC")) {
            pstmt.setLong(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    if (rs.getInt("height") <= height) {
                        return true;
                    }
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    boolean hasTransaction(Transaction transaction) {
        return hasTransaction(transaction.getFullHash(), transaction.getId(), Integer.MAX_VALUE);
    }

    boolean hasTransaction(Transaction transaction, int height) {
        return hasTransaction(transaction.getFullHash(), transaction.getId(), height);
    }

    boolean hasTransaction(byte[] fullHash) {
        return hasTransaction(fullHash, Convert.fullHashToId(fullHash), Integer.MAX_VALUE);
    }

    public boolean hasTransaction(byte[] fullHash, int height) {
        return hasTransaction(fullHash, Convert.fullHashToId(fullHash), height);
    }

    public boolean hasTransaction(byte[] fullHash, long transactionId, int height) {
        try (Connection con = transactionTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT full_hash, height FROM " + transactionTable.getSchemaTable() + " WHERE id = ?")) {
            pstmt.setLong(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    if (Arrays.equals(rs.getBytes("full_hash"), fullHash) && rs.getInt("height") <= height) {
                        return true;
                    }
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    List<byte[]> findChildTransactionFullHashes(long fxtTransactionId) {
        if (chain == FxtChain.FXT) {
            throw new RuntimeException("Invalid chain");
        }
        List<byte[]> list = new ArrayList<>();
        try (Connection con = transactionTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT full_hash FROM " + transactionTable.getSchemaTable()
                     + " WHERE fxt_transaction_id = ? ORDER BY fxt_transaction_id, transaction_index")) {
            pstmt.setLong(1, fxtTransactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getBytes("full_hash"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
        return list;
    }

    List<ChildTransactionImpl> findChildTransactions(long fxtTransactionId) {
        if (chain == FxtChain.FXT) {
            throw new RuntimeException("Invalid chain");
        }
        List<ChildTransactionImpl> list = new ArrayList<>();
        try (Connection con = transactionTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM " + transactionTable.getSchemaTable()
                     + " WHERE fxt_transaction_id = ? ORDER BY fxt_transaction_id, transaction_index")) {
            pstmt.setLong(1, fxtTransactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add((ChildTransactionImpl) TransactionImpl.loadTransaction(chain, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        } catch (NxtException.ValidationException e) {
            throw new RuntimeException("Transaction already in database for fxtTransactionId = " + Long.toUnsignedString(fxtTransactionId)
                    + " does not pass validation!", e);
        }
        return list;
    }

    static List<FxtTransactionImpl> findBlockTransactions(long blockId) {
        try (Connection con = Db.getConnection()) {
            return findBlockTransactions(con, blockId);
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static List<FxtTransactionImpl> findBlockTransactions(Connection con, long blockId) {
        List<FxtTransactionImpl> list = new ArrayList<>();
        try (PreparedStatement pstmt = con.prepareStatement("SELECT * FROM transaction_fxt"
                + " WHERE block_id = ? ORDER BY transaction_index")) {
            pstmt.setLong(1, blockId);
            pstmt.setFetchSize(50);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add((FxtTransactionImpl)TransactionImpl.loadTransaction(FxtChain.FXT, rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        } catch (NxtException.ValidationException e) {
            throw new RuntimeException("Transaction already in database for block_id = " + Long.toUnsignedString(blockId)
                    + " does not pass validation!", e);
        }
        return list;
    }

    List<PrunableTransaction> findPrunableTransactions(Connection con, int minTimestamp, int maxTimestamp) {
        List<PrunableTransaction> result = new ArrayList<>();
        try (PreparedStatement pstmt = con.prepareStatement("SELECT full_hash, type, subtype, "
                + "has_prunable_attachment AS prunable_attachment, "
                + "has_prunable_message AS prunable_plain_message, "
                + "has_prunable_encrypted_message AS prunable_encrypted_message "
                + "FROM " + transactionTable.getSchemaTable() + " WHERE (timestamp BETWEEN ? AND ?) AND "
                + "(has_prunable_attachment = TRUE OR has_prunable_message = TRUE OR has_prunable_encrypted_message = TRUE)")) {
            pstmt.setInt(1, minTimestamp);
            pstmt.setInt(2, maxTimestamp);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    byte[] fullHash = rs.getBytes("full_hash");
                    byte type = rs.getByte("type");
                    byte subtype = rs.getByte("subtype");
                    TransactionType transactionType = TransactionType.findTransactionType(type, subtype);
                    result.add(new PrunableTransaction(fullHash, transactionType,
                            rs.getBoolean("prunable_attachment"),
                            rs.getBoolean("prunable_plain_message"),
                            rs.getBoolean("prunable_encrypted_message")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
        return result;
    }

    static void saveTransactions(Connection con, List<FxtTransactionImpl> transactions) {
        try {
            for (FxtTransactionImpl transaction : transactions) {
                transaction.save(con, "transaction_fxt");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static class PrunableTransaction {
        private final byte[] fullHash;
        private final TransactionType transactionType;
        private final boolean prunableAttachment;
        private final boolean prunablePlainMessage;
        private final boolean prunableEncryptedMessage;

        private PrunableTransaction(byte[] fullHash, TransactionType transactionType, boolean prunableAttachment,
                                    boolean prunablePlainMessage, boolean prunableEncryptedMessage) {
            this.fullHash = fullHash;
            this.transactionType = transactionType;
            this.prunableAttachment = prunableAttachment;
            this.prunablePlainMessage = prunablePlainMessage;
            this.prunableEncryptedMessage = prunableEncryptedMessage;
        }

        public byte[] getFullHash() {
            return fullHash;
        }

        public TransactionType getTransactionType() {
            return transactionType;
        }

        public boolean hasPrunableAttachment() {
            return prunableAttachment;
        }

        public boolean hasPrunablePlainMessage() {
            return prunablePlainMessage;
        }

        public boolean hasPrunableEncryptedMessage() {
            return prunableEncryptedMessage;
        }
    }

    int getTransactionCount() {
        return transactionTable.getCount();
    }

}
