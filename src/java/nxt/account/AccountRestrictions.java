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

package nxt.account;

import nxt.Nxt;
import nxt.NxtException;
import nxt.NxtException.AccountControlException;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.FxtTransaction;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionType;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.VersionedEntityDbTable;
import nxt.db.VersionedValuesDbTable;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.security.BlockchainPermission;
import nxt.voting.AccountControlTransactionType;
import nxt.voting.PhasingAppendix;
import nxt.voting.PhasingControl;
import nxt.voting.PhasingParams;
import nxt.voting.SetPhasingOnlyAttachment;
import nxt.voting.VoteWeighting;
import nxt.voting.VoteWeighting.VotingModel;
import nxt.voting.VotingTransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

public final class AccountRestrictions {

    public static final class PhasingOnly extends PhasingControl {
        public static final String DEFAULT_ACCOUNT_CONTROL_VARIABLE = "ACC";

        public static PhasingOnly get(long accountId) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(new BlockchainPermission("phasing"));
            }
            return phasingControlTable.getBy(new DbClause.LongClause("account_id", accountId).
                    and(new DbClause.ByteClause("voting_model", DbClause.Op.NE, VotingModel.NONE.getCode())));
        }

        public static int getCount() {
            return phasingControlTable.getCount();
        }

        public static DbIterator<PhasingOnly> getAll(int from, int to) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(new BlockchainPermission("phasing"));
            }
            return phasingControlTable.getAll(from, to);
        }

        public static void set(Account senderAccount, SetPhasingOnlyAttachment attachment) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(new BlockchainPermission("phasing"));
            }
            PhasingParams phasingParams = attachment.getPhasingParams();
            long accountId = senderAccount.getId();
            if (phasingParams.getVoteWeighting().getVotingModel() == VotingModel.NONE) {
                //no voting - remove the control
                senderAccount.removeControl(Account.ControlType.PHASING_ONLY);
                PhasingOnly phasingOnly = get(accountId);
                phasingOnly.params = phasingParams;
                phasingControlTable.delete(phasingOnly);
                if (!phasingOnly.params.getSubPolls().isEmpty()) {
                    phasingControlSubPollTable.delete(phasingOnly);
                }
            } else {
                senderAccount.addControl(Account.ControlType.PHASING_ONLY);
                PhasingOnly phasingOnly = get(accountId);
                if (phasingOnly == null) {
                    phasingOnly = new PhasingOnly(accountId, phasingParams, attachment.getMaxFees(),
                            attachment.getMinDuration(), attachment.getMaxDuration());
                } else {
                    phasingOnly.params = phasingParams;
                    phasingOnly.maxFees = attachment.getMaxFees();
                    phasingOnly.minDuration = attachment.getMinDuration();
                    phasingOnly.maxDuration = attachment.getMaxDuration();
                }
                phasingControlTable.insert(phasingOnly);

                SortedMap<String, PhasingParams> subPolls = phasingOnly.params.getSubPolls();
                if (!subPolls.isEmpty()) {
                    phasingControlSubPollTable.insert(phasingOnly, subPolls.entrySet().stream().map(
                            entry -> new PhasingOnlySubPoll(accountId, entry.getKey(), entry.getValue())).collect(Collectors.toList()));
                }
            }
        }

        public static void importPhasingOnly(long accountId, long[] whitelist, int quorum, long maxFees, int minDuration, int maxDuration) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(new BlockchainPermission("phasing"));
            }
            Account.getAccount(accountId).addControl(Account.ControlType.PHASING_ONLY);
            Map<Integer, Long> maxFeesMap = new HashMap<>();
            maxFeesMap.put(ChildChain.IGNIS.getId(), maxFees);
            PhasingOnly phasingOnly = new PhasingOnly(accountId, new PhasingParams(new VoteWeighting((byte)0, 0, 0, (byte)0), quorum, whitelist, null, null, null, null, null), maxFeesMap,
                    (short)minDuration, (short)maxDuration);
            phasingControlTable.insert(phasingOnly);
        }

        private final DbKey dbKey;
        private final long accountId;

        private Map<Integer,Long> maxFees;
        private short minDuration;
        private short maxDuration;

        private PhasingOnly(long accountId, PhasingParams params, Map<Integer, Long> maxFees, short minDuration, short maxDuration) {
            super(params);
            this.accountId = accountId;
            dbKey = phasingControlDbKeyFactory.newKey(this.accountId);
            this.maxFees = maxFees;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
        }

        private PhasingOnly(ResultSet rs, DbKey dbKey) throws SQLException {
            this.accountId = rs.getLong("account_id");
            this.dbKey = dbKey;

            init(rs, () -> phasingControlSubPollTable.get(phasingControlSubPollDbKeyFactory.newKey(this)));

            Integer[] chainIds = DbUtils.getArray(rs, "max_fees_chains", Integer[].class);
            Long[] chainMaxFees = DbUtils.getArray(rs, "max_fees", Long[].class);
            this.maxFees = new HashMap<>(chainIds.length);
            for (int i = 0; i < chainIds.length; i++) {
                this.maxFees.put(chainIds[i], chainMaxFees[i]);
            }
            this.minDuration = rs.getShort("min_duration");
            this.maxDuration = rs.getShort("max_duration");
        }

        @Override
        public final String getControlType() {
            return "account";
        }

        @Override
        public final String getDefaultControlVariable() {
            return DEFAULT_ACCOUNT_CONTROL_VARIABLE;
        }

        public long getAccountId() {
            return accountId;
        }

        public Map<Integer,Long> getMaxFees() {
            return maxFees;
        }

        public short getMinDuration() {
            return minDuration;
        }

        public short getMaxDuration() {
            return maxDuration;
        }

        @Override
        protected void checkTransaction(ChildTransaction transaction) throws AccountControlException {
            ChildChain childChain = transaction.getChain();
            long maxFee = Convert.nullToZero(maxFees.get(childChain.getId()));
            if (maxFee > 0) {
                long totalFee = Math.addExact(transaction.getFee(),
                        childChain.getPhasingPollHome().getSenderPhasedTransactionFees(transaction.getSenderId()));
                if (totalFee > maxFee) {
                    throw new AccountControlException(String.format("Maximum total fees limit of %f %s exceeded, total fees are %f %s",
                            ((double) maxFee) / childChain.ONE_COIN, childChain.getName(), ((double)totalFee) / childChain.ONE_COIN, childChain.getName()));
                }
            }
            if (transaction.getType() == VotingTransactionType.PHASING_VOTE_CASTING) {
                return;
            }
            try {
                params.checkApprovable();
            } catch (NxtException.NotCurrentlyValidException e) {
                Logger.logDebugMessage("Account control no longer valid: " + e.getMessage());
                return;
            }
            PhasingAppendix phasingAppendix = transaction.getPhasing();
            checkPhasing(phasingAppendix);

            int duration = phasingAppendix.getFinishHeight() - Nxt.getBlockchain().getHeight();
            if ((maxDuration > 0 && duration > maxDuration) || (minDuration > 0 && duration < minDuration)) {
                throw new AccountControlException("Invalid phasing duration " + duration);
            }
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO account_control_phasing "
                    + "(account_id, whitelist, max_fees_chains, max_fees, expression, min_duration, max_duration, " +
                    PhasingParams.COMMON_COLUMN_NAMES + ", height, latest) KEY (account_id, height) VALUES (?, ?, ?, ?, ?, ?, ?, " +
                    PhasingParams.COMMON_COLUMN_PARAMETER_MARKERS + ", ?, TRUE)")) {
                int i = 0;
                pstmt.setLong(++i, this.accountId);
                DbUtils.setArrayEmptyToNull(pstmt, ++i, Convert.toArray(params.getWhitelist()));
                Integer[] chainIds = new Integer[maxFees.size()];
                Long[] chainMaxFees = new Long[maxFees.size()];
                int j = 0;
                for (Map.Entry<Integer,Long> entry : maxFees.entrySet()) {
                    chainIds[j] = entry.getKey();
                    chainMaxFees[j] = entry.getValue();
                    j++;
                }
                DbUtils.setArray(pstmt, ++i, chainIds);
                DbUtils.setArray(pstmt, ++i, chainMaxFees);
                pstmt.setString(++i, params.getExpressionStr());
                pstmt.setShort(++i, this.minDuration);
                pstmt.setShort(++i, this.maxDuration);

                i = PhasingParams.setCommonColumnValues(params, pstmt, i);

                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }
    }

    static final class PhasingOnlySubPoll extends PhasingControl.SubPoll {
        private final long accountId;


        private PhasingOnlySubPoll(long accountId, String variableName, PhasingParams params) {
            super(variableName, params);
            this.accountId = accountId;
        }

        private PhasingOnlySubPoll(ResultSet rs) throws SQLException {
            super(rs);
            this.accountId = rs.getLong("account_id");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO account_control_phasing_sub_poll (account_id, " +
                    "name, whitelist, " + PhasingParams.COMMON_COLUMN_NAMES + ", height, latest) " +
                    "VALUES (?, ?, ?, " + PhasingParams.COMMON_COLUMN_PARAMETER_MARKERS + ", ?, TRUE)")) {
                int i = 0;

                pstmt.setLong(++i, accountId);
                pstmt.setString(++i, variableName);
                DbUtils.setArray(pstmt, ++i, Convert.toArray(params.getWhitelist()));

                i = PhasingParams.setCommonColumnValues(params, pstmt, i);

                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }
    }

    private static final DbKey.LongKeyFactory<PhasingOnly> phasingControlDbKeyFactory = new DbKey.LongKeyFactory<PhasingOnly>("account_id") {
        @Override
        public DbKey newKey(PhasingOnly rule) {
            return rule.dbKey;
        }
    };

    private static final VersionedEntityDbTable<PhasingOnly> phasingControlTable = new VersionedEntityDbTable<PhasingOnly>("public.account_control_phasing", phasingControlDbKeyFactory) {

        @Override
        protected PhasingOnly load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new PhasingOnly(rs, dbKey);
        }

        @Override
        protected void save(Connection con, PhasingOnly phasingOnly) throws SQLException {
            phasingOnly.save(con);
        }
    };

    private static final DbKey.LongKeyFactory<PhasingOnly> phasingControlSubPollDbKeyFactory = new DbKey.LongKeyFactory<PhasingOnly>("account_id") {
        @Override
        public DbKey newKey(PhasingOnly rule) {
            return rule.dbKey == null ? newKey(rule.accountId) : rule.dbKey;
        }
    };

    private static final VersionedValuesDbTable<PhasingOnly, PhasingOnlySubPoll> phasingControlSubPollTable =
            new VersionedValuesDbTable<PhasingOnly, PhasingOnlySubPoll>("public.account_control_phasing_sub_poll", phasingControlSubPollDbKeyFactory) {

                @Override
                protected PhasingOnlySubPoll load(Connection con, ResultSet rs) throws SQLException {
                    return new PhasingOnlySubPoll(rs);
                }

                @Override
                protected void save(Connection con, PhasingOnly phasingOnly, PhasingOnlySubPoll phasingOnlySubPoll) throws SQLException {
                    phasingOnlySubPoll.save(con);
                }
            };


    public static void init() {
    }

    public static void checkTransaction(ChildTransaction transaction) throws NxtException.NotCurrentlyValidException {
        Account senderAccount = Account.getAccount(transaction.getSenderId());
        if (senderAccount == null) {
            throw new NxtException.NotCurrentlyValidException("Account " + Convert.rsAccount(transaction.getSenderId()) + " does not exist yet");
        }
        if (senderAccount.getControls().contains(Account.ControlType.PHASING_ONLY)) {
            PhasingOnly phasingOnly = PhasingOnly.get(transaction.getSenderId());
            phasingOnly.checkTransaction(transaction);
        }
    }

    public static void checkTransaction(FxtTransaction transaction) throws NxtException.NotCurrentlyValidException {
        Account senderAccount = Account.getAccount(transaction.getSenderId());
        if (senderAccount == null) {
            throw new NxtException.NotCurrentlyValidException("Account " + Convert.rsAccount(transaction.getSenderId()) + " does not exist yet");
        }
        if (senderAccount.getControls().contains(Account.ControlType.PHASING_ONLY)) {
            throw new AccountControlException(String.format("Account %s is under account control and cannot submit forging chain transaction %d:%s", Convert.rsAccount(transaction.getSenderId()),
                    transaction.getChain().getId(), Convert.toHexString(transaction.getFullHash())));
        }
    }

    public static boolean isBlockDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
        Account senderAccount = Account.getAccount(transaction.getSenderId());
        if (!senderAccount.getControls().contains(Account.ControlType.PHASING_ONLY)) {
            return false;
        }
        if (PhasingOnly.get(transaction.getSenderId()).getMaxFees().get(transaction.getChain().getId()) == null) {
            return false;
        }
        return transaction.getType() != AccountControlTransactionType.SET_PHASING_ONLY &&
                TransactionType.isDuplicate(AccountControlTransactionType.SET_PHASING_ONLY, Long.toUnsignedString(senderAccount.getId()),
                        duplicates, true);
    }

}
