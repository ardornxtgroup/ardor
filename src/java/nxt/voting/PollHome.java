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

import nxt.Constants;
import nxt.Nxt;
import nxt.blockchain.BlockchainProcessor;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.EntityDbTable;
import nxt.db.ValuesDbTable;
import nxt.util.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

public final class PollHome {

    private static final boolean isPollsProcessing = Nxt.getBooleanProperty("nxt.processPolls");

    public static final class OptionResult {

        private long result;
        private long weight;

        private OptionResult(long result, long weight) {
            this.result = result;
            this.weight = weight;
        }

        public long getResult() {
            return result;
        }

        public long getWeight() {
            return weight;
        }

        private void add(long vote, long weight) {
            this.result += vote;
            this.weight += weight;
        }

    }

    public static PollHome forChain(ChildChain childChain) {
        if (childChain.getPollHome() != null) {
            throw new IllegalStateException("already set");
        }
        return new PollHome(childChain);
    }

    private final ChildChain childChain;
    private final DbKey.LongKeyFactory<Poll> pollDbKeyFactory;
    private final EntityDbTable<Poll> pollTable;
    private final DbKey.LongKeyFactory<Poll> pollResultsDbKeyFactory;
    private final ValuesDbTable<Poll, OptionResult> pollResultsTable;

    private PollHome(ChildChain childChain) {
        this.childChain = childChain;
        this.pollDbKeyFactory = new DbKey.LongKeyFactory<Poll>("id") {
            @Override
            public DbKey newKey(Poll poll) {
                return poll.dbKey == null ? newKey(poll.id) : poll.dbKey;
            }
        };
        this.pollTable = new EntityDbTable<Poll>(childChain.getSchemaTable("poll"), pollDbKeyFactory, "name,description") {
            @Override
            protected Poll load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new Poll(rs, dbKey);
            }
            @Override
            protected void save(Connection con, Poll poll) throws SQLException {
                poll.save(con);
            }
        };
        this.pollResultsDbKeyFactory = new DbKey.LongKeyFactory<Poll>("poll_id") {
            @Override
            public DbKey newKey(Poll poll) {
                return poll.dbKey;
            }
        };
        this.pollResultsTable = new ValuesDbTable<Poll, OptionResult>(childChain.getSchemaTable("poll_result"), pollResultsDbKeyFactory) {
            @Override
            protected OptionResult load(Connection con, ResultSet rs) throws SQLException {
                long weight = rs.getLong("weight");
                return weight == 0 ? null : new OptionResult(rs.getLong("result"), weight);
            }
            @Override
            protected void save(Connection con, Poll poll, OptionResult optionResult) throws SQLException {
                try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO poll_result (poll_id, "
                        + "result, weight, height) VALUES (?, ?, ?, ?)")) {
                    int i = 0;
                    pstmt.setLong(++i, poll.getId());
                    if (optionResult != null) {
                        pstmt.setLong(++i, optionResult.result);
                        pstmt.setLong(++i, optionResult.weight);
                    } else {
                        pstmt.setNull(++i, Types.BIGINT);
                        pstmt.setLong(++i, 0);
                    }
                    pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                    pstmt.executeUpdate();
                }
            }
        };
        if (PollHome.isPollsProcessing) {
            Nxt.getBlockchainProcessor().addListener(block -> {
                int height = block.getHeight();
                checkPolls(height);
            }, BlockchainProcessor.Event.AFTER_BLOCK_APPLY);
        }
    }

    public Poll getPoll(long id) {
        return pollTable.get(pollDbKeyFactory.newKey(id));
    }

    public DbIterator<Poll> getPollsFinishingAtOrBefore(int height, int from, int to) {
        return pollTable.getManyBy(new DbClause.IntClause("finish_height", DbClause.Op.LTE, height), from, to);
    }

    public DbIterator<Poll> getAllPolls(int from, int to) {
        return pollTable.getAll(from, to);
    }

    public DbIterator<Poll> getActivePolls(int from, int to) {
        return pollTable.getManyBy(new DbClause.IntClause("finish_height", DbClause.Op.GT, Nxt.getBlockchain().getHeight()), from, to);
    }

    public DbIterator<Poll> getPollsByAccount(long accountId, boolean includeFinished, boolean finishedOnly, int from, int to) {
        DbClause dbClause = new DbClause.LongClause("account_id", accountId);
        if (finishedOnly) {
            dbClause = dbClause.and(new DbClause.IntClause("finish_height", DbClause.Op.LTE, Nxt.getBlockchain().getHeight()));
        } else if (!includeFinished) {
            dbClause = dbClause.and(new DbClause.IntClause("finish_height", DbClause.Op.GT, Nxt.getBlockchain().getHeight()));
        }
        return pollTable.getManyBy(dbClause, from, to);
    }

    public DbIterator<Poll> getPollsFinishingAt(int height) {
        return pollTable.getManyBy(new DbClause.IntClause("finish_height", height), 0, Integer.MAX_VALUE);
    }

    public DbIterator<Poll> searchPolls(String query, boolean includeFinished, int from, int to) {
        DbClause dbClause = includeFinished ? DbClause.EMPTY_CLAUSE : new DbClause.IntClause("finish_height", DbClause.Op.GT, Nxt.getBlockchain().getHeight());
        return pollTable.search(query, dbClause, from, to, " ORDER BY ft.score DESC, poll.height DESC, poll.db_id DESC ");
    }

    public int getCount() {
        return pollTable.getCount();
    }

    void addPoll(Transaction transaction, PollCreationAttachment attachment) {
        Poll poll = new Poll(transaction, attachment);
        pollTable.insert(poll);
    }

    private void checkPolls(int currentHeight) {
        try (DbIterator<Poll> polls = getPollsFinishingAt(currentHeight)) {
            for (Poll poll : polls) {
                try {
                    List<OptionResult> results = poll.countResults(poll.getVoteWeighting(), currentHeight);
                    pollResultsTable.insert(poll, results);
                    Logger.logDebugMessage("Poll " + Long.toUnsignedString(poll.getId()) + " has been finished");
                } catch (RuntimeException e) {
                    Logger.logErrorMessage("Couldn't count votes for poll " + Long.toUnsignedString(poll.getId()));
                }
            }
        }
    }


    public final class Poll extends AbstractPoll {

        private final DbKey dbKey;
        private final String name;
        private final String description;
        private final String[] options;
        private final byte minNumberOfOptions;
        private final byte maxNumberOfOptions;
        private final byte minRangeValue;
        private final byte maxRangeValue;
        private final int timestamp;
        private final VoteWeighting voteWeighting;
        private Poll(Transaction transaction, PollCreationAttachment attachment) {
            super(transaction.getId(), transaction.getSenderId(), attachment.getFinishHeight());
            this.voteWeighting = attachment.getVoteWeighting();
            this.dbKey = pollDbKeyFactory.newKey(this.id);
            this.name = attachment.getPollName();
            this.description = attachment.getPollDescription();
            this.options = attachment.getPollOptions();
            this.minNumberOfOptions = attachment.getMinNumberOfOptions();
            this.maxNumberOfOptions = attachment.getMaxNumberOfOptions();
            this.minRangeValue = attachment.getMinRangeValue();
            this.maxRangeValue = attachment.getMaxRangeValue();
            this.timestamp = Nxt.getBlockchain().getLastBlockTimestamp();
        }

        private Poll(ResultSet rs, DbKey dbKey) throws SQLException {
            super(rs);
            this.voteWeighting = readVoteWeighting(rs);
            this.dbKey = dbKey;
            this.name = rs.getString("name");
            this.description = rs.getString("description");
            this.options = DbUtils.getArray(rs, "options", String[].class);
            this.minNumberOfOptions = rs.getByte("min_num_options");
            this.maxNumberOfOptions = rs.getByte("max_num_options");
            this.minRangeValue = rs.getByte("min_range_value");
            this.maxRangeValue = rs.getByte("max_range_value");
            this.timestamp = rs.getInt("timestamp");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO poll (id, account_id, "
                    + "name, description, options, finish_height, voting_model, min_balance, min_balance_model, "
                    + "holding_id, min_num_options, max_num_options, min_range_value, max_range_value, timestamp, height) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, id);
                pstmt.setLong(++i, accountId);
                pstmt.setString(++i, name);
                pstmt.setString(++i, description);
                DbUtils.setArray(pstmt, ++i, options);
                pstmt.setInt(++i, finishHeight);
                pstmt.setByte(++i, voteWeighting.getVotingModel().getCode());
                DbUtils.setLongZeroToNull(pstmt, ++i, voteWeighting.getMinBalance());
                pstmt.setByte(++i, voteWeighting.getMinBalanceModel().getCode());
                DbUtils.setLongZeroToNull(pstmt, ++i, voteWeighting.getHoldingId());
                pstmt.setByte(++i, minNumberOfOptions);
                pstmt.setByte(++i, maxNumberOfOptions);
                pstmt.setByte(++i, minRangeValue);
                pstmt.setByte(++i, maxRangeValue);
                pstmt.setInt(++i, timestamp);
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        public List<OptionResult> getResults(VoteWeighting voteWeighting) {
            if (this.voteWeighting.equals(voteWeighting)) {
                return getResults();
            } else {
                return countResults(voteWeighting);
            }

        }

        public VoteWeighting getVoteWeighting() {
            return voteWeighting;
        }

        public List<OptionResult> getResults() {
            if (PollHome.isPollsProcessing && isFinished()) {
                return pollResultsTable.get(pollDbKeyFactory.newKey(this));
            } else {
                return countResults(voteWeighting);
            }
        }

        public DbIterator<VoteHome.Vote> getVotes() {
            return childChain.getVoteHome().getVotes(this.getId(), 0, -1);
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String[] getOptions() {
            return options;
        }


        public byte getMinNumberOfOptions() {
            return minNumberOfOptions;
        }

        public byte getMaxNumberOfOptions() {
            return maxNumberOfOptions;
        }

        public byte getMinRangeValue() {
            return minRangeValue;
        }

        public byte getMaxRangeValue() {
            return maxRangeValue;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public boolean isFinished() {
            return finishHeight <= Nxt.getBlockchain().getHeight();
        }

        public ChildChain getChildChain() {
            return PollHome.this.childChain;
        }

        private List<OptionResult> countResults(VoteWeighting voteWeighting) {
            int countHeight = Math.min(finishHeight, Nxt.getBlockchain().getHeight());
            if (countHeight < Nxt.getBlockchainProcessor().getMinRollbackHeight()) {
                return null;
            }
            return countResults(voteWeighting, countHeight);
        }

        private List<OptionResult> countResults(VoteWeighting voteWeighting, int height) {
            final OptionResult[] result = new OptionResult[options.length];
            VoteWeighting.VotingModel votingModel = voteWeighting.getVotingModel();
            try (DbIterator<VoteHome.Vote> votes = childChain.getVoteHome().getVotes(this.getId(), 0, -1)) {
                for (VoteHome.Vote vote : votes) {
                    long weight = votingModel.calcWeight(voteWeighting, vote.getVoterId(), height);
                    if (weight <= 0) {
                        continue;
                    }
                    long[] partialResult = countVote(vote, weight);
                    for (int i = 0; i < partialResult.length; i++) {
                        if (partialResult[i] != Long.MIN_VALUE) {
                            if (result[i] == null) {
                                result[i] = new OptionResult(partialResult[i], weight);
                            } else {
                                result[i].add(partialResult[i], weight);
                            }
                        }
                    }
                }
            }
            return Arrays.asList(result);
        }

        private long[] countVote(VoteHome.Vote vote, long weight) {
            final long[] partialResult = new long[options.length];
            final byte[] optionValues = vote.getVoteBytes();
            for (int i = 0; i < optionValues.length; i++) {
                if (optionValues[i] != Constants.NO_VOTE_VALUE) {
                    partialResult[i] = (long) optionValues[i] * weight;
                } else {
                    partialResult[i] = Long.MIN_VALUE;
                }
            }
            return partialResult;
        }
    }

}