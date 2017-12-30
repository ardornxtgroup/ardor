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

package nxt.blockchain;

import nxt.db.DbIterator;
import nxt.util.Filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public interface Blockchain {

    void readLock();

    void readUnlock();

    void updateLock();

    void updateUnlock();

    Block getLastBlock();

    Block getLastBlock(int timestamp);

    int getHeight();

    int getLastBlockTimestamp();

    Block getBlock(long blockId);

    Block getBlock(long blockId, boolean loadTransactions);

    Block getBlockAtHeight(int height);

    boolean hasBlock(long blockId);

    DbIterator<? extends Block> getAllBlocks();

    DbIterator<? extends Block> getBlocks(int from, int to);

    DbIterator<? extends Block> getBlocks(long accountId, int timestamp);

    DbIterator<? extends Block> getBlocks(long accountId, int timestamp, int from, int to);

    int getBlockCount(long accountId);

    DbIterator<? extends Block> getBlocks(Connection con, PreparedStatement pstmt);

    List<Long> getBlockIdsAfter(long blockId, int limit);

    List<? extends Block> getBlocksAfter(long blockId, int limit);

    List<? extends Block> getBlocksAfter(long blockId, List<Long> blockList);

    long getBlockIdAtHeight(int height);

    Block getECBlock(int timestamp);

    Transaction getTransaction(Chain chain, byte[] fullHash);

    boolean hasTransaction(Chain chain, byte[] fullHash);

    FxtTransaction getFxtTransaction(long transactionId);

    boolean hasFxtTransaction(long transactionId);

    int getTransactionCount(Chain chain);

    DbIterator<? extends ChildTransaction> getTransactions(ChildChain chain, long accountId, byte type, byte subtype, int blockTimestamp,
                                                      boolean includeExpiredPrunable);

    DbIterator<? extends ChildTransaction> getTransactions(ChildChain chain, long accountId, int numberOfConfirmations, byte type, byte subtype,
                                                      int blockTimestamp, boolean withMessage, boolean phasedOnly, boolean nonPhasedOnly,
                                                      int from, int to, boolean includeExpiredPrunable, boolean executedOnly);

    DbIterator<? extends FxtTransaction> getTransactions(FxtChain chain, long accountId, int numberOfConfirmations,
            byte type, byte subtype, int blockTimestamp, int from, int to);

    DbIterator<? extends FxtTransaction> getTransactions(FxtChain chain, Connection con, PreparedStatement pstmt);

    DbIterator<? extends ChildTransaction> getTransactions(ChildChain childChain, Connection con, PreparedStatement pstmt);

    List<? extends Transaction> getExpectedTransactions(Filter<Transaction> filter);

    DbIterator<? extends ChildTransaction> getReferencingTransactions(ChildChain chain, byte[] referencedTransactionFullHash, int from, int to);

    DbIterator<? extends Transaction> getExecutedTransactions(Chain chain, long senderId, long recipientId,
                                                              byte type, byte subtype,
                                                              int height, int numberOfConfirmations,
                                                              int from, int to);

}
