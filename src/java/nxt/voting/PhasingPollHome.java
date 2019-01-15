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

package nxt.voting;

import nxt.Constants;
import nxt.Nxt;
import nxt.account.Account;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.Transaction;
import nxt.crypto.HashFunction;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.DerivedDbTable;
import nxt.db.EntityDbTable;
import nxt.db.ValuesDbTable;
import nxt.util.BooleanExpression;
import nxt.util.Convert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static nxt.voting.VoteWeighting.VotingModel.COMPOSITE;
import static nxt.voting.VoteWeighting.VotingModel.HASH;
import static nxt.voting.VoteWeighting.VotingModel.NONE;
import static nxt.voting.VoteWeighting.VotingModel.PROPERTY;
import static nxt.voting.VoteWeighting.VotingModel.TRANSACTION;

public final class PhasingPollHome {

    public static final Set<HashFunction> acceptedHashFunctions =
            Collections.unmodifiableSet(EnumSet.of(HashFunction.SHA256, HashFunction.RIPEMD160, HashFunction.RIPEMD160_SHA256));

    public static HashFunction getHashFunction(byte code) {
        try {
            HashFunction hashFunction = HashFunction.getHashFunction(code);
            if (acceptedHashFunctions.contains(hashFunction)) {
                return hashFunction;
            }
        } catch (IllegalArgumentException ignore) {
        }
        return null;
    }

    public static PhasingPollHome forChain(ChildChain childChain) {
        if (childChain.getPhasingPollHome() != null) {
            throw new IllegalStateException("already set");
        }
        return new PhasingPollHome(childChain);
    }

    private static final DerivedDbTable phasingPollFinishTable = new DerivedDbTable("public.phasing_poll_finish") {};

    public static final Comparator<ChildTransaction> finishingTransactionsComparator =
            Comparator.comparingInt(ChildTransaction::getHeight)
                    .thenComparingInt(ChildTransaction::getIndex)
                    .thenComparingLong(ChildTransaction::getId);

    public static List<? extends ChildTransaction> getFinishingTransactions(int height) {
        List<ChildTransaction> childTransactions = new ArrayList<>();
        try (Connection con = phasingPollFinishTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT full_hash, chain_id FROM phasing_poll_finish "
             + "WHERE finish_height = ?")) {
            pstmt.setInt(1, height);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ChildChain childChain = ChildChain.getChildChain(rs.getInt("chain_id"));
                    Transaction childTransaction = childChain.getTransactionHome().findTransaction(rs.getBytes("full_hash"));
                    childTransactions.add((ChildTransaction)childTransaction);
                }
                childTransactions.sort(finishingTransactionsComparator);
                return childTransactions;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    //The transaction may still have finished early, this only checks for finish_height not yet reached
    public static boolean hasUnfinishedPhasedTransaction(long transactionId) {
        try (Connection con = phasingPollFinishTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT transaction_id FROM phasing_poll_finish "
                     + "WHERE transaction_id = ? AND finish_height > ?")) {
            pstmt.setLong(1, transactionId);
            pstmt.setInt(2, Nxt.getBlockchain().getHeight());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static boolean checkSecretMatch(byte[] revealedSecret, PhasingParams phasingParams) {
        HashFunction hashFunction = getHashFunction(phasingParams.getAlgorithm());
        return hashFunction != null && Arrays.equals(phasingParams.getHashedSecret(), hashFunction.hash(revealedSecret));
    }

    private static final DbKey.HashKeyFactory<PhasingPoll> linkedTransactionDbKeyFactory = new DbKey.HashKeyFactory<PhasingPoll>("transaction_full_hash", "transaction_id") {
        @Override
        public DbKey newKey(PhasingPoll poll) {
            return poll.dbKey == null ? newKey(poll.hash, poll.id) : poll.dbKey;
        }
    };

    private static final ValuesDbTable<PhasingPoll, LinkedTransaction> linkedTransactionTable = new ValuesDbTable<PhasingPoll, LinkedTransaction>
            ("public.phasing_poll_linked_transaction", linkedTransactionDbKeyFactory) {
        @Override
        protected LinkedTransaction load(Connection con, ResultSet rs) throws SQLException {
            return new LinkedTransaction(rs);
        }
        @Override
        protected void save(Connection con, PhasingPoll poll, LinkedTransaction linkedTransaction) throws SQLException {
            linkedTransaction.save(con, poll);
        }
    };

    private static final class LinkedTransaction {
        private final ChainTransactionId transactionId;
        private final String subPollName;

        private LinkedTransaction(ChainTransactionId transactionId, String subPollName) {
            this.transactionId = transactionId;
            this.subPollName = subPollName;
        }

        private LinkedTransaction(ResultSet rs) throws SQLException {
            this.transactionId = new ChainTransactionId(rs.getInt("linked_chain_id"), rs.getBytes("linked_full_hash"));
            this.subPollName = rs.getString("sub_poll_name");
        }

        void save(Connection con, PhasingPoll poll) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO phasing_poll_linked_transaction " +
                    "(chain_id, transaction_id, transaction_full_hash, sub_poll_name, "
                    + "linked_chain_id, linked_full_hash, linked_transaction_id, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setInt(++i, poll.getChildChain().getId());
                pstmt.setLong(++i, poll.getId());
                pstmt.setBytes(++i, poll.getFullHash());
                pstmt.setString(++i, subPollName);
                pstmt.setInt(++i, transactionId.getChainId());
                pstmt.setBytes(++i, transactionId.getFullHash());
                pstmt.setLong(++i, transactionId.getTransactionId());
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        public ChainTransactionId getTransactionId() {
            return transactionId;
        }
    }


    public static List<? extends ChildTransaction> getLinkedPhasedTransactions(Transaction transaction) {
        return getLinkedPhasedTransactions(transaction.getFullHash(), transaction.getId());
    }

    public static List<? extends ChildTransaction> getLinkedPhasedTransactions(byte[] linkedTransactionFullHash) {
        return getLinkedPhasedTransactions(linkedTransactionFullHash, Convert.fullHashToId(linkedTransactionFullHash));
    }

    private static List<? extends ChildTransaction> getLinkedPhasedTransactions(byte[] linkedTransactionFullHash, long linkedTransactionId) {
        try (Connection con = linkedTransactionTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT chain_id, transaction_full_hash FROM phasing_poll_linked_transaction " +
                     "WHERE linked_transaction_id = ? AND linked_full_hash = ? GROUP BY transaction_id, transaction_full_hash")) {
            int i = 0;
            pstmt.setLong(++i, linkedTransactionId);
            pstmt.setBytes(++i, linkedTransactionFullHash);
            List<ChildTransaction> transactions = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ChildChain childChain = ChildChain.getChildChain(rs.getInt("chain_id"));
                    transactions.add((ChildTransaction)childChain.getTransactionHome().findTransaction(rs.getBytes("transaction_full_hash")));
                }
            }
            return transactions;
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }


    private static final DbKey.HashHashLongKeyFactory<HashedSecretPhasedTransaction> hashedSecretPhasedTransactionDbKeyFactory = new DbKey.HashHashLongKeyFactory<HashedSecretPhasedTransaction>(
            "hashed_secret", "hashed_secret_id", "transaction_full_hash", "transaction_id", "algorithm") {
        @Override
        public DbKey newKey(HashedSecretPhasedTransaction hashedSecretPhasedTransaction) {
            return hashedSecretPhasedTransaction.dbKey;
        }
    };

    private static final EntityDbTable<HashedSecretPhasedTransaction> hashedSecretPhasedTransactionTable = new EntityDbTable<HashedSecretPhasedTransaction>(
            "public.phasing_poll_hashed_secret", hashedSecretPhasedTransactionDbKeyFactory) {
        @Override
        protected HashedSecretPhasedTransaction load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new HashedSecretPhasedTransaction(rs, dbKey);
        }

        @Override
        protected void save(Connection con, HashedSecretPhasedTransaction hashedSecretPhasedTransaction) throws SQLException {
            hashedSecretPhasedTransaction.save(con);
        }
    };

    private static final class HashedSecretPhasedTransaction {

        private final PhasingParams.HashVoting hashVoting;
        private final ChainTransactionId phasedTransactionId;
        private final DbKey dbKey;
        private final int finishHeight;

        private HashedSecretPhasedTransaction(PhasingParams.HashVoting hashVoting, ChainTransactionId phasedTransactionId, int finishHeight) {
            this.hashVoting = hashVoting;
            this.phasedTransactionId = phasedTransactionId;
            this.dbKey = hashedSecretPhasedTransactionDbKeyFactory.newKey(hashVoting.getHashedSecret(), phasedTransactionId.getFullHash(), hashVoting.getAlgorithm());
            this.finishHeight = finishHeight;
        }

        private HashedSecretPhasedTransaction(ResultSet rs, DbKey dbKey) throws SQLException {
            this.hashVoting = new PhasingParams.HashVoting(rs.getBytes("hashed_secret"), rs.getByte("algorithm"));
            this.phasedTransactionId = new ChainTransactionId(rs.getInt("chain_id"), rs.getBytes("transaction_full_hash"));
            this.dbKey = dbKey;
            this.finishHeight = rs.getInt("finish_height");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO phasing_poll_hashed_secret ("
                    + "hashed_secret, hashed_secret_id, algorithm, transaction_full_hash, transaction_id, chain_id, finish_height, height) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setBytes(++i, hashVoting.getHashedSecret());
                pstmt.setLong(++i, Convert.fullHashToId(hashVoting.getHashedSecret()));
                pstmt.setByte(++i, hashVoting.getAlgorithm());
                pstmt.setBytes(++i, phasedTransactionId.getFullHash());
                pstmt.setLong(++i, phasedTransactionId.getTransactionId());
                pstmt.setInt(++i, phasedTransactionId.getChainId());
                pstmt.setInt(++i, finishHeight);
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        public ChainTransactionId getPhasedTransactionId() {
            return phasedTransactionId;
        }

    }

    public static List<ChainTransactionId> getHashedSecretPhasedTransactionIds(PhasingParams.HashVoting hashVoting, int blockchainHeight) {
        List<ChainTransactionId> hashedSecretPhasedTransactionIds = new ArrayList<>();
        try (DbIterator<HashedSecretPhasedTransaction> iterator = hashedSecretPhasedTransactionTable.getManyBy(
                new DbClause.HashClause("hashed_secret", "hashed_secret_id", hashVoting.getHashedSecret())
                        .and(new DbClause.ByteClause("algorithm", hashVoting.getAlgorithm()))
                .and(new DbClause.IntClause("finish_height", DbClause.Op.GTE, blockchainHeight)), 0, -1)) {
            while (iterator.hasNext()) {
                hashedSecretPhasedTransactionIds.add(iterator.next().getPhasedTransactionId());
            }
        }
        return hashedSecretPhasedTransactionIds;
    }

    public static Set<ChainTransactionId> getVotedTransactionIds(ChildTransaction transaction) {
        PhasingVoteCastingAttachment attachment = (PhasingVoteCastingAttachment) transaction.getAttachment();
        Set<ChainTransactionId> votedTransactionIds = new HashSet<>(attachment.getPhasedTransactionsIds());
        if (Nxt.getBlockchain().getHeight() >= Constants.LIGHT_CONTRACTS_BLOCK) {
            for (byte[] revealedSecret : attachment.getRevealedSecrets()) {
                for (HashFunction hashFunction : PhasingPollHome.acceptedHashFunctions) {
                    PhasingParams.HashVoting hashVoting = new PhasingParams.HashVoting(hashFunction.hash(revealedSecret), hashFunction.getId());
                    List<ChainTransactionId> hashedSecretPhasedTransactions = PhasingPollHome.getHashedSecretPhasedTransactionIds(
                            hashVoting, attachment.getFinishValidationHeight(transaction) + 1);
                    for (ChainTransactionId hashedSecretPhasedTransactionId : hashedSecretPhasedTransactions) {
                        if (votedTransactionIds.contains(hashedSecretPhasedTransactionId)) {
                            continue;
                        }
                        ChildTransaction hashedSecretPhasedTransaction = hashedSecretPhasedTransactionId.getChildTransaction();
                        PhasingPollHome.PhasingPoll poll = hashedSecretPhasedTransaction.getChain().getPhasingPollHome().getPoll(hashedSecretPhasedTransaction);
                        if (poll == null) {
                            continue;
                        }
                        if (! poll.getParams().isAccountWhitelisted(transaction.getSenderId())) {
                            continue;
                        }
                        if (PhasingPollHome.getResult(hashedSecretPhasedTransaction) != null) {
                            continue;
                        }
                        votedTransactionIds.add(hashedSecretPhasedTransactionId);
                    }
                }
            }
        }
        return votedTransactionIds;
    }


    private static final DbKey.HashKeyFactory<PhasingPollResult> resultDbKeyFactory = new DbKey.HashKeyFactory<PhasingPollResult>("full_hash", "id") {
        @Override
        public DbKey newKey(PhasingPollResult phasingPollResult) {
            return phasingPollResult.dbKey;
        }
    };

    private static final EntityDbTable<PhasingPollResult> resultTable = new EntityDbTable<PhasingPollResult>("public.phasing_poll_result", resultDbKeyFactory) {
        @Override
        protected PhasingPollResult load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new PhasingPollResult(rs, dbKey);
        }
        @Override
        protected void save(Connection con, PhasingPollResult phasingPollResult) throws SQLException {
            phasingPollResult.save(con);
        }
    };

    public static PhasingPollResult getResult(Transaction transaction) {
        return resultTable.get(resultDbKeyFactory.newKey(transaction.getFullHash(), transaction.getId()));
    }

    public static PhasingPollResult getResult(byte[] fullHash) {
        return resultTable.get(resultDbKeyFactory.newKey(fullHash));
    }

    public static DbIterator<PhasingPollResult> getApproved(int height) {
        return resultTable.getManyBy(new DbClause.IntClause("height", height).and(new DbClause.BooleanClause("approved", true)),
                0, -1, " ORDER BY db_id ASC ");
    }

    public static final class PhasingPollResult {

        private final long id;
        private final byte[] hash;
        private final DbKey dbKey;
        private final ChildChain childChain;
        private final long result;
        private final boolean approved;
        private final int height;

        private PhasingPollResult(PhasingPoll poll, long result) {
            this.id = poll.getId();
            this.hash = poll.getFullHash();
            this.dbKey = resultDbKeyFactory.newKey(this.hash, this.id);
            this.childChain = poll.getChildChain();
            this.result = result;
            this.approved = result >= poll.getQuorum();
            this.height = Nxt.getBlockchain().getHeight();
        }

        private PhasingPollResult(ResultSet rs, DbKey dbKey) throws SQLException {
            this.id = rs.getLong("id");
            this.hash = rs.getBytes("full_hash");
            this.dbKey = dbKey;
            this.childChain = ChildChain.getChildChain(rs.getInt("chain_id"));
            this.result = rs.getLong("result");
            this.approved = rs.getBoolean("approved");
            this.height = rs.getInt("height");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO phasing_poll_result (id, full_hash, "
                    + "chain_id, result, approved, height) VALUES (?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, id);
                pstmt.setBytes(++i, hash);
                pstmt.setInt(++i, childChain.getId());
                pstmt.setLong(++i, result);
                pstmt.setBoolean(++i, approved);
                pstmt.setInt(++i, height);
                pstmt.executeUpdate();
            }
        }

        public long getId() {
            return id;
        }

        public byte[] getFullHash() {
            return hash;
        }

        public long getResult() {
            return result;
        }

        public boolean isApproved() {
            return approved;
        }

        public int getHeight() {
            return height;
        }

        public ChildChain getChildChain() {
            return childChain;
        }

    }


    private final ChildChain childChain;
    private final PhasingVoteHome phasingVoteHome;
    private final DbKey.HashKeyFactory<PhasingPoll> phasingPollDbKeyFactory;
    private final EntityDbTable<PhasingPoll> phasingPollTable;
    private final DbKey.HashKeyFactory<PhasingPoll> votersDbKeyFactory;
    private final ValuesDbTable<PhasingPoll, PhasingPollVoter> votersTable;
    private final DbKey.HashKeyFactory<PhasingPoll> subPollsDbKeyFactory;
    private final ValuesDbTable<PhasingPoll, PhasingSubPoll> subPollsTable;

    private PhasingPollHome(ChildChain childChain) {
        this.childChain = childChain;
        this.phasingVoteHome = childChain.getPhasingVoteHome();
        this.phasingPollDbKeyFactory = new DbKey.HashKeyFactory<PhasingPoll>("full_hash", "id") {
            @Override
            public DbKey newKey(PhasingPoll poll) {
                return poll.dbKey;
            }
        };
        this.phasingPollTable = new EntityDbTable<PhasingPoll>(childChain.getSchemaTable("phasing_poll"), phasingPollDbKeyFactory) {
            @Override
            protected PhasingPoll load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new PhasingPoll(rs, dbKey);
            }
            @Override
            protected void save(Connection con, PhasingPoll poll) throws SQLException {
                poll.save(con);
            }
            @Override
            public void trim(int height) {
                super.trim(height);
                try (Connection con = getConnection();
                     DbIterator<PhasingPoll> pollsToTrim = phasingPollTable.getManyBy(new DbClause.IntClause("finish_height", DbClause.Op.LT, height), 0, -1);
                     PreparedStatement pstmt1 = con.prepareStatement("DELETE FROM phasing_poll WHERE id = ? AND full_hash = ?");
                     PreparedStatement pstmt2 = con.prepareStatement("DELETE FROM phasing_poll_voter WHERE transaction_id = ? AND transaction_full_hash = ?");
                     PreparedStatement pstmt3 = con.prepareStatement("DELETE FROM phasing_vote_sub_poll WHERE transaction_id = ? AND transaction_full_hash = ?");
                     PreparedStatement pstmt4 = con.prepareStatement("DELETE FROM phasing_vote WHERE transaction_id = ? AND transaction_full_hash = ?");
                     PreparedStatement pstmt5 = con.prepareStatement("DELETE FROM phasing_poll_linked_transaction WHERE transaction_id = ? AND transaction_full_hash = ?");
                     PreparedStatement pstmt6 = con.prepareStatement("DELETE FROM phasing_poll_finish WHERE transaction_id = ? AND full_hash = ?");
                     PreparedStatement pstmt7 = con.prepareStatement("DELETE FROM phasing_sub_poll WHERE transaction_id = ? AND transaction_full_hash = ?");
                     PreparedStatement pstmt8 = con.prepareStatement("DELETE FROM phasing_poll_hashed_secret WHERE transaction_id = ? AND transaction_full_hash = ?")) {
                    while (pollsToTrim.hasNext()) {
                        PhasingPoll poll = pollsToTrim.next();
                        long id = poll.getId();
                        byte[] hash = poll.getFullHash();
                        pstmt1.setLong(1, id);
                        pstmt1.setBytes(2, hash);
                        pstmt1.executeUpdate();
                        pstmt2.setLong(1, id);
                        pstmt2.setBytes(2, hash);
                        pstmt2.executeUpdate();
                        pstmt3.setLong(1, id);
                        pstmt3.setBytes(2, hash);
                        pstmt3.executeUpdate();
                        pstmt4.setLong(1, id);
                        pstmt4.setBytes(2, hash);
                        pstmt4.executeUpdate();
                        pstmt5.setLong(1, id);
                        pstmt5.setBytes(2, hash);
                        pstmt5.executeUpdate();
                        pstmt6.setLong(1, id);
                        pstmt6.setBytes(2, hash);
                        pstmt6.executeUpdate();
                        pstmt7.setLong(1, id);
                        pstmt7.setBytes(2, hash);
                        pstmt7.executeUpdate();
                        pstmt8.setLong(1, id);
                        pstmt8.setBytes(2, hash);
                        pstmt8.executeUpdate();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e.toString(), e);
                }
            }
        };
        this.votersDbKeyFactory = new DbKey.HashKeyFactory<PhasingPoll>("transaction_full_hash", "transaction_id") {
            @Override
            public DbKey newKey(PhasingPoll poll) {
                return poll.dbKey == null ? newKey(poll.hash, poll.id) : poll.dbKey;
            }
        };
        this.votersTable = new ValuesDbTable<PhasingPoll, PhasingPollVoter>(childChain.getSchemaTable("phasing_poll_voter"), votersDbKeyFactory) {
            @Override
            protected PhasingPollVoter load(Connection con, ResultSet rs) throws SQLException {
                return new PhasingPollVoter(rs);
            }
            @Override
            protected void save(Connection con, PhasingPoll poll, PhasingPollVoter voter) throws SQLException {
                voter.save(con, poll);
            }
        };
        this.subPollsDbKeyFactory = new DbKey.HashKeyFactory<PhasingPoll>("transaction_full_hash", "transaction_id") {
            @Override
            public DbKey newKey(PhasingPoll poll) {
                return poll.dbKey == null ? newKey(poll.hash, poll.id) : poll.dbKey;
            }
        };
        this.subPollsTable = new ValuesDbTable<PhasingPoll, PhasingSubPoll>(childChain.getSchemaTable("phasing_sub_poll"), subPollsDbKeyFactory) {

            @Override
            protected PhasingSubPoll load(Connection con, ResultSet rs) throws SQLException {
                return new PhasingSubPoll(rs);
            }

            @Override
            protected void save(Connection con, PhasingPoll phasingPoll, PhasingSubPoll phasingSubPoll) throws SQLException {
                phasingSubPoll.save(con);
            }
        };
    }

    public PhasingPoll getPoll(Transaction transaction) {
        return phasingPollTable.get(phasingPollDbKeyFactory.newKey(transaction.getFullHash(), transaction.getId()));
    }

    public PhasingPoll getPoll(byte[] fullHash) {
        return phasingPollTable.get(phasingPollDbKeyFactory.newKey(fullHash));
    }

    public DbIterator<? extends ChildTransaction> getVoterPhasedTransactions(long voterId, int from, int to) {
        Connection con = null;
        try {
            con = phasingPollTable.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT transaction.* "
                    + "FROM transaction, phasing_poll_voter, phasing_poll "
                    + "LEFT JOIN phasing_poll_result ON phasing_poll.id = phasing_poll_result.id "
                    + "AND phasing_poll.full_hash = phasing_poll_result.full_hash "
                    + "WHERE transaction.id = phasing_poll.id AND "
                    + "transaction.full_hash = phasing_poll.full_hash AND "
                    + "phasing_poll.finish_height > ? AND "
                    + "phasing_poll.id = phasing_poll_voter.transaction_id "
                    + "AND phasing_poll.full_hash = phasing_poll_voter.transaction_full_hash "
                    + "AND phasing_poll_voter.voter_id = ? "
                    + "AND phasing_poll_result.id IS NULL "
                    + "AND phasing_poll_result.full_hash IS NULL "
                    + "GROUP BY transaction.id, transaction.full_hash "
                    + "ORDER BY transaction.height DESC, transaction.transaction_index DESC "
                    + DbUtils.limitsClause(from, to));
            int i = 0;
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.setLong(++i, voterId);
            DbUtils.setLimits(++i, pstmt, from, to);

            return Nxt.getBlockchain().getTransactions(childChain, con, pstmt);
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public DbIterator<? extends ChildTransaction> getHoldingPhasedTransactions(long holdingId, VoteWeighting.VotingModel votingModel,
                                                                    long accountId, boolean withoutWhitelist, int from, int to) {
        Connection con = null;
        try {
            con = phasingPollTable.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT transaction.* " +
                    "FROM transaction, phasing_poll " +
                    "WHERE phasing_poll.holding_id = ? " +
                    "AND phasing_poll.voting_model = ? " +
                    "AND phasing_poll.id = transaction.id " +
                    "AND phasing_poll.full_hash = transaction.full_hash " +
                    "AND phasing_poll.finish_height > ? " +
                    (accountId != 0 ? "AND phasing_poll.account_id = ? " : "") +
                    (withoutWhitelist ? "AND phasing_poll.whitelist_size = 0 " : "") +
                    "ORDER BY transaction.height DESC, transaction.transaction_index DESC " +
                    DbUtils.limitsClause(from, to));
            int i = 0;
            pstmt.setLong(++i, holdingId);
            pstmt.setByte(++i, votingModel.getCode());
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            if (accountId != 0) {
                pstmt.setLong(++i, accountId);
            }
            DbUtils.setLimits(++i, pstmt, from, to);

            return Nxt.getBlockchain().getTransactions(childChain, con, pstmt);
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public DbIterator<? extends ChildTransaction> getAccountPhasedTransactions(long accountId, int from, int to) {
        Connection con = null;
        try {
            con = phasingPollTable.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT transaction.* FROM transaction, phasing_poll " +
                    " LEFT JOIN phasing_poll_result ON phasing_poll.id = phasing_poll_result.id " +
                    " AND phasing_poll.full_hash = phasing_poll_result.full_hash " +
                    " WHERE phasing_poll.id = transaction.id AND (transaction.sender_id = ? OR transaction.recipient_id = ?) " +
                    " AND phasing_poll.full_hash = transaction.full_hash " +
                    " AND phasing_poll_result.id IS NULL AND phasing_poll_result.full_hash IS NULL " +
                    " AND phasing_poll.finish_height > ? ORDER BY transaction.height DESC, transaction.transaction_index DESC " +
                    DbUtils.limitsClause(from, to));
            int i = 0;
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, accountId);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            DbUtils.setLimits(++i, pstmt, from, to);

            return Nxt.getBlockchain().getTransactions(childChain, con, pstmt);
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public int getAccountPhasedTransactionCount(long accountId) {
        try (Connection con = phasingPollTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT COUNT(*) FROM transaction, phasing_poll " +
                     " LEFT JOIN phasing_poll_result ON phasing_poll.id = phasing_poll_result.id " +
                     " AND phasing_poll.full_hash = phasing_poll_result.full_hash " +
                     " WHERE phasing_poll.id = transaction.id AND (transaction.sender_id = ? OR transaction.recipient_id = ?) " +
                     " AND phasing_poll.full_hash = transaction.full_hash " +
                     " AND phasing_poll_result.id IS NULL AND phasing_poll_result.full_hash IS NULL " +
                     " AND phasing_poll.finish_height > ?")) {
            int i = 0;
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, accountId);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public long getSenderPhasedTransactionFees(long accountId) {
        try (Connection con = phasingPollTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT SUM(transaction.fee) AS fees FROM transaction, phasing_poll " +
                     " LEFT JOIN phasing_poll_result ON phasing_poll.id = phasing_poll_result.id AND phasing_poll.full_hash = phasing_poll_result.full_hash " +
                     " WHERE phasing_poll.id = transaction.id AND transaction.sender_id = ? " +
                     " AND phasing_poll.full_hash = transaction.full_hash " +
                     " AND phasing_poll_result.id IS NULL AND phasing_poll_result.full_hash IS NULL " +
                     " AND phasing_poll.finish_height > ?")) {
            int i = 0;
            pstmt.setLong(++i, accountId);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getLong("fees");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }


    void addPoll(Transaction transaction, PhasingAppendix appendix) {
        PhasingPoll poll = new PhasingPoll(transaction, appendix);
        phasingPollTable.insert(poll);

        List<PhasingPollVoter> phasingPollVoters = new ArrayList<>();
        for (long accountId : poll.getWhitelist()) {
            phasingPollVoters.add(new PhasingPollVoter(accountId, null));
        }
        List<LinkedTransaction> linkedTransactions = new ArrayList<>();
        appendix.getLinkedTransactionsIds().forEach(id -> linkedTransactions.add(new LinkedTransaction(id, null)));

        SortedMap<String, PhasingParams> subPolls = appendix.getParams().getSubPolls();
        if (!subPolls.isEmpty()) {
            List<PhasingSubPoll> subPollsList = new ArrayList<>(subPolls.size());
            subPolls.forEach((name, params) -> {
                for (long accountId : params.getWhitelist()) {
                    phasingPollVoters.add(new PhasingPollVoter(accountId, name));
                }
                params.getLinkedTransactionsIds().forEach(id -> linkedTransactions.add(new LinkedTransaction(id, name)));
                subPollsList.add(new PhasingSubPoll(transaction, name, params));
            });
            subPollsTable.insert(poll, subPollsList);
        }
        votersTable.insert(poll, phasingPollVoters);
        linkedTransactionTable.insert(poll, linkedTransactions);
        ChainTransactionId id = ChainTransactionId.getChainTransactionId(transaction);
        poll.getHashedSecretParams().forEach(params -> {
            HashedSecretPhasedTransaction hashedSecretPhasedTransaction = new HashedSecretPhasedTransaction(
                    params.getHashVoting(), id, poll.getFinishHeight());
            if (hashedSecretPhasedTransactionTable.get(hashedSecretPhasedTransaction.dbKey) == null) {
                hashedSecretPhasedTransactionTable.insert(hashedSecretPhasedTransaction);
            }
        });
    }


    public final class PhasingPoll extends AbstractPoll {

        private final long recipientAccountId;
        private final byte[] hash;
        private final DbKey dbKey;
        private final PhasingParams params;

        private PhasingPoll(Transaction transaction, PhasingAppendix appendix) {
            super(transaction.getId(), transaction.getSenderId(), appendix.getFinishHeight());
            this.recipientAccountId = transaction.getRecipientId();
            this.hash = transaction.getFullHash();
            this.dbKey = phasingPollDbKeyFactory.newKey(this.hash, this.id);
            params = appendix.getParams();
        }

        private PhasingPoll(ResultSet rs, DbKey dbKey) throws SQLException {
            super(rs);
            this.recipientAccountId = rs.getLong("recipient_account_id");
            this.hash = rs.getBytes("full_hash");
            this.dbKey = dbKey;

            VoteWeighting voteWeighting = readVoteWeighting(rs);

            Supplier<long[]> whitelistSupplier;
            if (rs.getByte("whitelist_size") == 0 || voteWeighting.getVotingModel() == COMPOSITE) {
                whitelistSupplier = null;
            } else {
                whitelistSupplier = () -> Convert.toArray(votersTable.get(votersDbKeyFactory.newKey(PhasingPoll.this)).stream()
                        .map(PhasingPollVoter::getAccountId).toArray(Long[]::new));
            }

            PhasingParams.HashVoting hashVoting = new PhasingParams.HashVoting(rs.getBytes("hashed_secret"), rs.getByte("algorithm"));

            PhasingParams.CompositeVoting compositeVoting = null;
            if (voteWeighting.getVotingModel() == COMPOSITE) {
                List<PhasingSubPoll> phasingSubPolls = subPollsTable.get(subPollsDbKeyFactory.newKey(this));
                SortedMap<String, PhasingParams> subPolls = new TreeMap<>();
                phasingSubPolls.forEach(phasingSubPoll -> subPolls.put(phasingSubPoll.getVariableName(), phasingSubPoll.getParams()));
                compositeVoting = new PhasingParams.CompositeVoting(rs.getString("expression"), subPolls);
            }

            Supplier<List<ChainTransactionId>> linkedTransactionsSupplier = null;
            if (voteWeighting.getVotingModel() == TRANSACTION) {
                //no need to filter transactions linked to sub-polls - there should be none
                linkedTransactionsSupplier = () -> linkedTransactionTable.get(
                        linkedTransactionDbKeyFactory.newKey(PhasingPoll.this)).stream()
                        .map(LinkedTransaction::getTransactionId).collect(Collectors.toList());
            }

            PhasingParams.PropertyVoting senderPropertyVoting = PhasingParams.readPropertyVoting(rs, "sender_", voteWeighting);
            PhasingParams.PropertyVoting recipientPropertyVoting = PhasingParams.readPropertyVoting(rs, "recipient_", voteWeighting);

            params = new PhasingParams(voteWeighting, rs.getLong("quorum"), whitelistSupplier, linkedTransactionsSupplier,
                    hashVoting, compositeVoting, senderPropertyVoting, recipientPropertyVoting);
        }

        public ChildChain getChildChain() {
            return childChain;
        }

        void finish(long result) {
            PhasingPollResult phasingPollResult = new PhasingPollResult(this, result);
            resultTable.insert(phasingPollResult);
        }

        public PhasingParams getParams() {
            return params;
        }

        public VoteWeighting getVoteWeighting() {
            return params.getVoteWeighting();
        }

        public long[] getWhitelist() {
            return params.getWhitelist();
        }

        public long getQuorum() {
            return params.getQuorum();
        }

        public byte[] getFullHash() {
            return hash;
        }

        public List<ChainTransactionId> getLinkedTransactions() {
            return params.getLinkedTransactionsIds();
        }

        public byte[] getHashedSecret() {
            return params.getHashedSecret();
        }

        public byte getAlgorithm() {
            return params.getAlgorithm();
        }

        /**
         * @return an Iterable containing the PhasingParams of this poll, or its sub-polls, having by-hash voting model
         */
        Iterable<PhasingParams> getHashedSecretParams() {
            if (params.getVoteWeighting().getVotingModel().equals(HASH)) {
                return Collections.singletonList(params);
            } else if (params.getVoteWeighting().getVotingModel().equals(COMPOSITE)) {
                return params.getSubPolls().values().stream().filter(subPoll -> subPoll.getVoteWeighting().getVotingModel() == HASH)::iterator;
            } else {
                return Collections.emptyList();
            }
        }

        public boolean isCompositeVoting() {
            return params.getVoteWeighting().getVotingModel() == COMPOSITE;
        }

        public long countVotes() {
            return countVotes(null, params);
        }

        BooleanExpression.Value getCompositeVotingResult() {
            Map<String, BooleanExpression.Value> values = params.getSubPolls().entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey, e -> {
                        PhasingParams subPoll = e.getValue();
                        final long quorum = subPoll.getQuorum();

                        if (PhasingPoll.this.finishHeight <= Nxt.getBlockchain().getHeight()) {
                            // after finishHeight
                            return BooleanExpression.Value.fromBoolean(countVotes(e.getKey(), subPoll) >= quorum);
                        } else {
                            if (subPoll.allowEarlyFinish()) {
                                if (countVotes(e.getKey(), subPoll) >= quorum) {
                                    return BooleanExpression.Value.TRUE;
                                } else {
                                    return BooleanExpression.Value.UNKNOWN;
                                }
                            } else {
                                return BooleanExpression.Value.UNKNOWN;
                            }
                        }
                    }));
            try {
                return params.getExpression().evaluate(values);
            } catch (BooleanExpression.BooleanExpressionException e) {
                //Should never happen - these phasing parameters are always loaded from the DB and the expression
                //should be validated before entering the DB
                throw new RuntimeException("Invalid boolean expression while counting votes.", e);
            }
        }

        private boolean isPropertyVotingValid(PhasingParams.PropertyVoting propertyVoting, Account.AccountProperty property) {
            return property != null && property.getValue() != null && (propertyVoting.getValue().isEmpty()
                    || propertyVoting.getValue().equals(property.getValue()));
        }

        private long countVotes(String subPollName, PhasingParams phasingParams) {
            VoteWeighting voteWeighting = phasingParams.getVoteWeighting();
            VoteWeighting.VotingModel votingModel = voteWeighting.getVotingModel();
            if (votingModel == NONE) {
                return 0;
            }
            int height = Math.min(this.finishHeight, Nxt.getBlockchain().getHeight());
            if (votingModel == TRANSACTION) {
                int count = 0;
                for (ChainTransactionId linkedTransaction : phasingParams.getLinkedTransactionsIds()) {
                    if (linkedTransaction.getChain().getTransactionHome().hasTransaction(linkedTransaction.getFullHash(), linkedTransaction.getTransactionId(), height)) {
                        count += 1;
                    }
                }
                return count;
            }
            if (votingModel == PROPERTY) {
                Account.AccountProperty property;
                PhasingParams.PropertyVoting propertyVoting;
                if (this.recipientAccountId != 0) {
                    propertyVoting = phasingParams.getRecipientPropertyVoting();
                    if (propertyVoting.getSetterId() != 0) {
                        property = Account.getProperty(this.recipientAccountId, propertyVoting.getName(), propertyVoting.getSetterId());
                        if (!isPropertyVotingValid(propertyVoting, property)) {
                            return 0;
                        }
                    }
                }
                propertyVoting = phasingParams.getSenderPropertyVoting();
                if (propertyVoting.getSetterId() == 0) {
                    return 1;
                } else {
                    property = Account.getProperty(this.accountId, propertyVoting.getName(), propertyVoting.getSetterId());
                }
                return isPropertyVotingValid(propertyVoting, property) ? 1 : 0;
            }
            if (votingModel == COMPOSITE) {
                BooleanExpression.Value result = getCompositeVotingResult();
                switch (result) {
                    case TRUE:
                        return 1;
                    default:
                        return 0;
                }
            }
            if (voteWeighting.isBalanceIndependent()) {
                if (subPollName == null) {
                    return phasingVoteHome.getVoteCount(this.hash);
                } else {
                    return phasingVoteHome.getSubPollVoteCount(this.hash, subPollName);
                }
            }

            long cumulativeWeight = 0;
            if (subPollName == null) {
                try (DbIterator<PhasingVoteHome.PhasingVote> votes = phasingVoteHome.getVotes(this.hash, 0, Integer.MAX_VALUE)) {
                    for (PhasingVoteHome.PhasingVote vote : votes) {
                        cumulativeWeight += votingModel.calcWeight(voteWeighting, vote.getVoterId(), height);
                    }
                }
            } else {
                try (DbIterator<PhasingVoteHome.PhasingVoteSubPoll> votes = phasingVoteHome.getSubPollVotes(this.hash, subPollName)) {
                    for (PhasingVoteHome.PhasingVoteSubPoll vote : votes) {
                        cumulativeWeight += votingModel.calcWeight(voteWeighting, vote.getVoterId(), height);
                    }
                }
            }
            return cumulativeWeight;
        }

        public boolean allowEarlyFinish() {
            return params.allowEarlyFinish();
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO phasing_poll (id, full_hash, account_id, "
                    + "recipient_account_id, finish_height, whitelist_size, expression, hashed_secret, algorithm, "
                    + PhasingParams.COMMON_COLUMN_NAMES + ", height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, "
                    + PhasingParams.COMMON_COLUMN_PARAMETER_MARKERS + ", ?)")) {
                int i = 0;

                pstmt.setLong(++i, id);
                pstmt.setBytes(++i, hash);
                pstmt.setLong(++i, accountId);
                pstmt.setLong(++i, recipientAccountId);
                pstmt.setInt(++i, finishHeight);
                pstmt.setByte(++i, (byte) getWhitelist().length);
                pstmt.setString(++i, params.getExpressionStr());
                DbUtils.setBytes(pstmt, ++i, params.getHashedSecret());
                pstmt.setByte(++i, params.getAlgorithm());

                i = PhasingParams.setCommonColumnValues(params, pstmt, i);

                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO phasing_poll_finish (transaction_id, full_hash, chain_id, finish_height, height) "
                    + "VALUES (?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, id);
                pstmt.setBytes(++i, hash);
                pstmt.setInt(++i, childChain.getId());
                pstmt.setInt(++i, finishHeight);
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

    }

    public final class PhasingSubPoll {
        private final long id;
        private final byte[] hash;
        private final String variableName;
        private final PhasingParams params;

        private PhasingSubPoll(Transaction transaction, String variableName, PhasingParams params) {
            this.id = transaction.getId();
            this.hash = transaction.getFullHash();
            this.variableName = variableName;
            this.params = params;
        }

        private PhasingSubPoll(ResultSet rs) throws SQLException {
            this.id = rs.getLong("transaction_id");
            this.hash = rs.getBytes("transaction_full_hash");
            this.variableName = Convert.nullToEmpty(rs.getString("name"));

            VoteWeighting voteWeighting = AbstractPoll.readVoteWeighting(rs);

            Supplier<long[]> whitelistSupplier = null;
            if (rs.getByte("whitelist_size") > 0) {
                whitelistSupplier = () -> Convert.toArray(votersTable.get(votersDbKeyFactory.newKey(hash, id)).stream()
                        .filter(phasingPollVoter -> variableName.equals(phasingPollVoter.subPollName))
                            .map(PhasingPollVoter::getAccountId).toArray(Long[]::new));
            }

            Supplier<List<ChainTransactionId>> linkedTransactionsSupplier = null;
            if (voteWeighting.getVotingModel() == TRANSACTION) {
                linkedTransactionsSupplier = () -> linkedTransactionTable.get(linkedTransactionDbKeyFactory.newKey(hash, id)).stream()
                    .filter(linkedTransaction -> variableName.equals(linkedTransaction.subPollName))
                        .map(LinkedTransaction::getTransactionId).collect(Collectors.toList());
            }

            PhasingParams.PropertyVoting senderPropertyVoting = PhasingParams.readPropertyVoting(rs, "sender_", voteWeighting);
            PhasingParams.PropertyVoting recipientPropertyVoting = PhasingParams.readPropertyVoting(rs, "recipient_", voteWeighting);

            params = new PhasingParams(voteWeighting, rs.getLong("quorum"),
                    whitelistSupplier, linkedTransactionsSupplier,
                    new PhasingParams.HashVoting(rs.getBytes("hashed_secret"), rs.getByte("algorithm")),
                    null, senderPropertyVoting, recipientPropertyVoting);
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO phasing_sub_poll (transaction_id, transaction_full_hash, "
                    + "name, whitelist_size, hashed_secret, algorithm, "
                    + PhasingParams.COMMON_COLUMN_NAMES + ", height) " + "VALUES (?, ?, ?, ?, ?, ?, "
                    + PhasingParams.COMMON_COLUMN_PARAMETER_MARKERS + ", ?)")) {
                int i = 0;

                pstmt.setLong(++i, id);
                pstmt.setBytes(++i, hash);
                pstmt.setString(++i, variableName);
                pstmt.setByte(++i, (byte) params.getWhitelist().length);
                DbUtils.setBytes(pstmt, ++i, params.getHashedSecret());
                pstmt.setByte(++i, params.getAlgorithm());

                i = PhasingParams.setCommonColumnValues(params, pstmt, i);

                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        public String getVariableName() {
            return variableName;
        }

        public PhasingParams getParams() {
            return params;
        }
    }

    public final class PhasingPollVoter {
        private final long accountId;
        private final String subPollName;

        private PhasingPollVoter(long accountId, String subPollName) {
            this.accountId = accountId;
            this.subPollName = subPollName;
        }

        PhasingPollVoter(ResultSet rs) throws SQLException {
            this.accountId = rs.getLong("voter_id");
            this.subPollName = rs.getString("sub_poll_name");
        }

        void save(Connection con, PhasingPoll poll) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO phasing_poll_voter (transaction_id, transaction_full_hash, "
                    + "voter_id, sub_poll_name, height) VALUES (?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, poll.getId());
                pstmt.setBytes(++i, poll.getFullHash());
                pstmt.setLong(++i, accountId);
                pstmt.setString(++i, subPollName);
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        public long getAccountId() {
            return accountId;
        }
    }

}