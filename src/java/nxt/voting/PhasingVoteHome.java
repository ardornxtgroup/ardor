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

package nxt.voting;

import nxt.Nxt;
import nxt.account.Account;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.EntityDbTable;
import nxt.db.ValuesDbTable;
import nxt.util.Convert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static nxt.voting.VoteWeighting.VotingModel.COMPOSITE;
import static nxt.voting.VoteWeighting.VotingModel.HASH;

public final class PhasingVoteHome {

    public static PhasingVoteHome forChain(ChildChain childChain) {
        if (childChain.getPhasingVoteHome() != null) {
            throw new IllegalStateException("already set");
        }
        return new PhasingVoteHome(childChain);
    }

    private final ChildChain childChain;
    private final DbKey.HashLongKeyFactory<PhasingVote> phasingVoteDbKeyFactory;
    private final EntityDbTable<PhasingVote> phasingVoteTable;
    private final DbKey.HashLongKeyFactory<PhasingVote> phasingVoteSubPollDbKeyFactory;
    private final ValuesDbTable<PhasingVote, PhasingVoteSubPoll> phasingVoteSubPollsTable;

    private PhasingVoteHome(ChildChain childChain) {
        this.childChain = childChain;
        this.phasingVoteDbKeyFactory = new DbKey.HashLongKeyFactory<PhasingVote>("transaction_full_hash",
                "transaction_id", "voter_id") {
            @Override
            public DbKey newKey(PhasingVote vote) {
                return vote.dbKey;
            }
        };
        this.phasingVoteTable = new EntityDbTable<PhasingVote>(childChain.getSchemaTable("phasing_vote"), phasingVoteDbKeyFactory) {
            @Override
            protected PhasingVote load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new PhasingVote(rs, dbKey);
            }
            @Override
            protected void save(Connection con, PhasingVote vote) throws SQLException {
                vote.save(con);
            }
        };

        this.phasingVoteSubPollDbKeyFactory = new DbKey.HashLongKeyFactory<PhasingVote>("transaction_full_hash",
                "transaction_id", "voter_id") {
            @Override
            public DbKey newKey(PhasingVote vote) {
                return vote.dbKey;
            }
        };

        this.phasingVoteSubPollsTable = new ValuesDbTable<PhasingVote, PhasingVoteSubPoll>(
                childChain.getSchemaTable("phasing_vote_sub_poll"), phasingVoteSubPollDbKeyFactory) {
            @Override
            protected PhasingVoteSubPoll load(Connection con, ResultSet rs) throws SQLException {
                return new PhasingVoteSubPoll(rs);
            }

            @Override
            protected void save(Connection con, PhasingVote phasingVote, PhasingVoteSubPoll phasingVoteSubPoll) throws SQLException {
                phasingVoteSubPoll.save(con);
            }
        };
    }

    public DbIterator<PhasingVote> getVotes(byte[] phasedTransactionHash, int from, int to) {
        return phasingVoteTable.getManyBy(new DbClause.HashClause("transaction_full_hash", "transaction_id", phasedTransactionHash), from, to);
    }

    public DbIterator<PhasingVoteSubPoll> getSubPollVotes(byte[] phasedTransactionHash, String subPollName) {
        Connection con = null;
        try {
            con = phasingVoteSubPollsTable.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM phasing_vote_sub_poll "
                    + " WHERE transaction_id = ? AND transaction_full_hash = ?  AND sub_poll_name = ?");

            int i = 0;
            pstmt.setLong(++i, Convert.fullHashToId(phasedTransactionHash));
            pstmt.setBytes(++i, phasedTransactionHash);
            pstmt.setString(++i, subPollName);

            return new DbIterator<>(con, pstmt, (connection, rs) -> new PhasingVoteSubPoll(rs));
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public PhasingVote getVote(byte[] phasedTransactionHash, long voterId) {
        return phasingVoteTable.get(phasingVoteDbKeyFactory.newKey(phasedTransactionHash, voterId));
    }

    public long getVoteCount(byte[] phasedTransactionHash) {
        DbClause clause = new DbClause.HashClause("transaction_full_hash", "transaction_id", phasedTransactionHash);
        return phasingVoteTable.getCount(clause);
    }

    public long getSubPollVoteCount(byte[] phasedTransactionHash, String subPollName) {
        DbClause clause = new DbClause.HashClause("transaction_full_hash", "transaction_id", phasedTransactionHash)
                .and(new DbClause.StringClause("sub_poll_name", subPollName));
        return phasingVoteSubPollsTable.getCount(clause);
    }

    void addVote(Transaction transaction, Account voter, byte[] phasedTransactionHash) {
        PhasingVote phasingVote = phasingVoteTable.get(phasingVoteDbKeyFactory.newKey(phasedTransactionHash, voter.getId()));
        if (phasingVote == null) {
            PhasingPollHome.PhasingPoll poll = childChain.getPhasingPollHome().getPoll(phasedTransactionHash);
            List<PhasingVoteSubPoll> subPollVotes = new ArrayList<>();
            if (poll.getVoteWeighting().getVotingModel() == COMPOSITE) {
                List<byte[]> revealedSecrets = ((PhasingVoteCastingAttachment) transaction.getAttachment()).getRevealedSecrets();
                poll.getParams().getSubPolls().forEach((name, subPoll) -> {
                    if (subPoll.acceptsVotes() && subPoll.isAccountWhitelisted(voter.getId())
                            && (subPoll.getVoteWeighting().getVotingModel() != HASH
                                || revealedSecrets.stream().anyMatch(secret -> PhasingPollHome.checkSecretMatch(secret, subPoll)))) {
                        subPollVotes.add(new PhasingVoteSubPoll(phasedTransactionHash, voter, name));
                    }
                });
            }

            phasingVote = new PhasingVote(transaction, voter, phasedTransactionHash);
            phasingVoteTable.insert(phasingVote);

            if (!subPollVotes.isEmpty()) {
                phasingVoteSubPollsTable.insert(phasingVote, subPollVotes);
            }
        }
    }

    public final class PhasingVote {

        private final long phasedTransactionId;
        private final byte[] phasedTransactionHash;
        private final long voterId;
        private final DbKey dbKey;
        private final long voteId;
        private final byte[] voteHash;

        private PhasingVote(Transaction transaction, Account voter, byte[] phasedTransactionHash) {
            this.phasedTransactionHash = phasedTransactionHash;
            this.phasedTransactionId = Convert.fullHashToId(phasedTransactionHash);
            this.voterId = voter.getId();
            this.dbKey = phasingVoteDbKeyFactory.newKey(this.phasedTransactionHash, this.phasedTransactionId, this.voterId);
            this.voteId = transaction.getId();
            this.voteHash = transaction.getFullHash();
        }

        private PhasingVote(ResultSet rs, DbKey dbKey) throws SQLException {
            this.phasedTransactionId = rs.getLong("transaction_id");
            this.phasedTransactionHash = rs.getBytes("transaction_full_hash");
            this.voterId = rs.getLong("voter_id");
            this.dbKey = dbKey;
            this.voteId = rs.getLong("vote_id");
            this.voteHash = rs.getBytes("vote_full_hash");
        }

        public ChildChain getChildChain() {
            return childChain;
        }

        public long getPhasedTransactionId() {
            return phasedTransactionId;
        }

        public byte[] getPhasedTransactionFullHash() {
            return phasedTransactionHash;
        }

        public long getVoterId() {
            return voterId;
        }

        public long getVoteId() {
            return voteId;
        }

        public byte[] getVoteFullHash() {
            return voteHash;
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO phasing_vote (vote_id, vote_full_hash, transaction_id, "
                    + "transaction_full_hash, voter_id, height) VALUES (?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, this.voteId);
                pstmt.setBytes(++i, this.voteHash);
                pstmt.setLong(++i, this.phasedTransactionId);
                pstmt.setBytes(++i, this.phasedTransactionHash);
                pstmt.setLong(++i, this.voterId);
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }
    }

    public final class PhasingVoteSubPoll {
        private final long phasedTransactionId;
        private final byte[] phasedTransactionHash;
        private final long voterId;
        private final String subPollName;

        private PhasingVoteSubPoll(byte[] phasedTransactionHash, Account voter, String subPollName) {
            this.phasedTransactionHash = phasedTransactionHash;
            this.phasedTransactionId = Convert.fullHashToId(phasedTransactionHash);
            this.voterId = voter.getId();
            this.subPollName = subPollName;
        }

        private PhasingVoteSubPoll(ResultSet rs) throws SQLException {
            this.phasedTransactionId = rs.getLong("transaction_id");
            this.phasedTransactionHash = rs.getBytes("transaction_full_hash");
            this.voterId = rs.getLong("voter_id");
            this.subPollName = rs.getString("sub_poll_name");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO phasing_vote_sub_poll (transaction_id, "
                    + "transaction_full_hash, voter_id, sub_poll_name, height) VALUES (?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, this.phasedTransactionId);
                pstmt.setBytes(++i, this.phasedTransactionHash);
                pstmt.setLong(++i, this.voterId);
                pstmt.setString(++i, this.subPollName);
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        public long getVoterId() {
            return voterId;
        }
    }

}