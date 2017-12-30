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
 
 package nxt.ae;

import nxt.Nxt;
import nxt.NxtException;
import nxt.NxtException.AccountControlException;
import nxt.account.HoldingType;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.TransactionType;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.VersionedEntityDbTable;
import nxt.db.VersionedValuesDbTable;
import nxt.shuffling.ShufflingCreationAttachment;
import nxt.shuffling.ShufflingTransactionType;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.voting.PhasingAppendix;
import nxt.voting.PhasingControl;
import nxt.voting.PhasingParams;
import nxt.voting.VoteWeighting.VotingModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class AssetControl {
    private final static Set<TransactionType> ASSET_CONTROL_TRANSACTION_TYPES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            AssetExchangeTransactionType.ASSET_TRANSFER,
            AssetExchangeTransactionType.ASSET_DELETE,
            AssetExchangeTransactionType.ASK_ORDER_PLACEMENT,
            AssetExchangeTransactionType.BID_ORDER_PLACEMENT,
            AssetExchangeTransactionType.DIVIDEND_PAYMENT,
            ShufflingTransactionType.SHUFFLING_CREATION)));

    public static final class PhasingOnly extends PhasingControl {

        public static final String DEFAULT_ASSET_CONTROL_VARIABLE = "ASC";

        public static PhasingOnly get(long assetId) {
            return phasingControlTable.getBy(new DbClause.LongClause("asset_id", assetId).
                    and(new DbClause.ByteClause("voting_model", DbClause.Op.NE, VotingModel.NONE.getCode())));
        }

        public static int getCount() {
            return phasingControlTable.getCount();
        }

        public static DbIterator<PhasingOnly> getAll(int from, int to) {
            return phasingControlTable.getAll(from, to);
        }

        static void set(SetPhasingAssetControlAttachment attachment) {
            PhasingParams phasingParams = attachment.getPhasingParams();
            long assetId = attachment.getAssetId();
            Asset asset = Asset.getAsset(assetId);

            if (phasingParams.getVoteWeighting().getVotingModel() == VotingModel.NONE) {
                //no voting - remove the control
                asset.setHasPhasingControl(false);
                PhasingOnly phasingOnly = get(assetId);
                phasingOnly.params = phasingParams;
                phasingControlTable.delete(phasingOnly);
                if (!phasingOnly.params.getSubPolls().isEmpty()) {
                    phasingControlSubPollTable.delete(phasingOnly);
                }
            } else {
                asset.setHasPhasingControl(true);
                PhasingOnly phasingOnly = get(assetId);
                if (phasingOnly == null) {
                    phasingOnly = new PhasingOnly(assetId, phasingParams);
                } else {
                    phasingOnly.params = phasingParams;
                }
                phasingControlTable.insert(phasingOnly);

                SortedMap<String, PhasingParams> subPolls = phasingOnly.params.getSubPolls();
                if (!subPolls.isEmpty()) {
                    phasingControlSubPollTable.insert(phasingOnly, subPolls.entrySet().stream().map(
                            entry -> new PhasingOnlySubPoll(assetId, entry.getKey(), entry.getValue())).collect(Collectors.toList()));
                }
            }
        }

        private final DbKey dbKey;
        private final long assetId;

        private PhasingOnly(long assetId, PhasingParams params) {
            super(params);
            this.assetId = assetId;
            dbKey = phasingControlDbKeyFactory.newKey(this.assetId);
        }

        private PhasingOnly(ResultSet rs, DbKey dbKey) throws SQLException {
            this.assetId = rs.getLong("asset_id");
            this.dbKey = dbKey;

            init(rs, () -> phasingControlSubPollTable.get(phasingControlSubPollDbKeyFactory.newKey(this)));
        }

        public long getAssetId() {
            return assetId;
        }

        @Override
        public final String getControlType() {
            return "asset";
        }

        @Override
        public final String getDefaultControlVariable() {
            return DEFAULT_ASSET_CONTROL_VARIABLE;
        }

        @Override
        protected void checkTransaction(ChildTransaction transaction) throws AccountControlException {

            try {
                params.checkApprovable();
            } catch (NxtException.NotCurrentlyValidException e) {
                Logger.logDebugMessage("Asset control no longer valid: " + e.getMessage());
                return;
            }
            PhasingAppendix phasingAppendix = transaction.getPhasing();
            checkPhasing(phasingAppendix);
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO asset_control_phasing "
                    + "(asset_id, whitelist, expression, " + PhasingParams.COMMON_COLUMN_NAMES
                    + ", height, latest) KEY (asset_id, height) VALUES (?, ?, ?, " + PhasingParams.COMMON_COLUMN_PARAMETER_MARKERS + ", ?, TRUE)")) {
                int i = 0;
                pstmt.setLong(++i, this.assetId);
                DbUtils.setArrayEmptyToNull(pstmt, ++i, Convert.toArray(params.getWhitelist()));

                pstmt.setString(++i, params.getExpressionStr());

                i = PhasingParams.setCommonColumnValues(params, pstmt, i);

                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }
    }

    static final class PhasingOnlySubPoll extends PhasingControl.SubPoll {
        private final long assetId;


        private PhasingOnlySubPoll(long assetId, String variableName, PhasingParams params) {
            super(variableName, params);
            this.assetId = assetId;
        }

        private PhasingOnlySubPoll(ResultSet rs) throws SQLException {
            super(rs);
            this.assetId = rs.getLong("asset_id");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO asset_control_phasing_sub_poll (asset_id, " +
                    "name, whitelist, " + PhasingParams.COMMON_COLUMN_NAMES + ", height, latest) " +
                    "VALUES (?, ?, ?, " + PhasingParams.COMMON_COLUMN_PARAMETER_MARKERS + ", ?, TRUE)")) {
                int i = 0;

                pstmt.setLong(++i, assetId);
                pstmt.setString(++i, variableName);
                DbUtils.setArray(pstmt, ++i, Convert.toArray(params.getWhitelist()));

                i = PhasingParams.setCommonColumnValues(params, pstmt, i);

                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }
    }

    private static final DbKey.LongKeyFactory<PhasingOnly> phasingControlDbKeyFactory = new DbKey.LongKeyFactory<PhasingOnly>("asset_id") {
        @Override
        public DbKey newKey(PhasingOnly rule) {
            return rule.dbKey;
        }
    };

    private static final VersionedEntityDbTable<PhasingOnly> phasingControlTable = new VersionedEntityDbTable<PhasingOnly>(
            "public.asset_control_phasing", phasingControlDbKeyFactory) {

        @Override
        protected PhasingOnly load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new PhasingOnly(rs, dbKey);
        }

        @Override
        protected void save(Connection con, PhasingOnly phasingOnly) throws SQLException {
            phasingOnly.save(con);
        }
    };

    private static final DbKey.LongKeyFactory<PhasingOnly> phasingControlSubPollDbKeyFactory = new DbKey.LongKeyFactory<PhasingOnly>("asset_id") {
        @Override
        public DbKey newKey(PhasingOnly rule) {
            return rule.dbKey == null ? newKey(rule.assetId) : rule.dbKey;
        }
    };

    private static final VersionedValuesDbTable<PhasingOnly, PhasingOnlySubPoll> phasingControlSubPollTable = new VersionedValuesDbTable<PhasingOnly, PhasingOnlySubPoll>(
            "public.asset_control_phasing_sub_poll", phasingControlSubPollDbKeyFactory) {
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
        TransactionType transactionType = transaction.getType();
        if (!ASSET_CONTROL_TRANSACTION_TYPES.contains(transactionType)) {
            return;
        }
        long assetId = 0;

        if (transactionType instanceof AssetExchangeTransactionType) {
            AssetExchangeTransactionType aeType = (AssetExchangeTransactionType) transactionType;
            assetId = aeType.getAssetId(transaction);
        } else if (ShufflingTransactionType.SHUFFLING_CREATION.equals(transactionType)) {
            ShufflingCreationAttachment attachment = (ShufflingCreationAttachment) transaction.getAttachment();
            if (attachment.getHoldingType() == HoldingType.ASSET) {
                assetId = attachment.getHoldingId();
            }
        }

        if (assetId == 0) {
            return;
        }
        Asset asset = Asset.getAsset(assetId);
        if (asset == null) {
            throw new NxtException.NotCurrentlyValidException("Asset " + Long.toUnsignedString(assetId) + " does not exist yet");
        }
        if (asset.hasPhasingControl()) {
            if (ShufflingTransactionType.SHUFFLING_CREATION.equals(transactionType)) {
                throw new NxtException.NotCurrentlyValidException("Shuffling of asset under asset control is not supported");
            }

            PhasingOnly phasingOnly = PhasingOnly.get(assetId);
            phasingOnly.checkTransaction(transaction);

            PhasingParams phasingParams = phasingOnly.getPhasingParams();
            if (AssetExchangeTransactionType.DIVIDEND_PAYMENT.equals(transaction.getType())
                    && phasingParams.getVoteWeighting().getVotingModel() == VotingModel.PROPERTY
                    && phasingParams.getRecipientPropertyVoting().getSetterId() != 0) {
                throw new NxtException.NotCurrentlyValidException("Dividend payment with asset under by-recipient property control is not supported");
            }
        }
    }
}
