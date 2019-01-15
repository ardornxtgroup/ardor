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
import nxt.NxtException;
import nxt.NxtException.ValidationException;
import nxt.account.Account;
import nxt.ae.Asset;
import nxt.blockchain.Chain;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionImpl;
import nxt.db.DbUtils;
import nxt.ms.Currency;
import nxt.util.BooleanExpression;
import nxt.util.Convert;
import nxt.util.JSON;
import nxt.util.bbh.StringRw;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static nxt.util.bbh.LengthRwPrimitiveType.BYTE;
import static nxt.util.bbh.LengthRwPrimitiveType.SHORT;
import static nxt.voting.VoteWeighting.VotingModel.ACCOUNT;
import static nxt.voting.VoteWeighting.VotingModel.ASSET;
import static nxt.voting.VoteWeighting.VotingModel.COMPOSITE;
import static nxt.voting.VoteWeighting.VotingModel.CURRENCY;
import static nxt.voting.VoteWeighting.VotingModel.HASH;
import static nxt.voting.VoteWeighting.VotingModel.NONE;
import static nxt.voting.VoteWeighting.VotingModel.PROPERTY;
import static nxt.voting.VoteWeighting.VotingModel.TRANSACTION;

/**
 * Phasing poll data. Handles the serialization, deserialization and validation of phasing poll parameters.
 */
public class PhasingParams {
    public static final String COMMON_COLUMN_NAMES = "voting_model, quorum, min_balance, holding_id, min_balance_model, " +
            "sender_property_setter_id, sender_property_name, sender_property_value, " +
            "recipient_property_setter_id, recipient_property_name, recipient_property_value";
    public static final String COMMON_COLUMN_PARAMETER_MARKERS = "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";

    public static class HashVoting {
        private final byte[] hashedSecret;
        private final byte algorithm;

        private HashVoting(ByteBuffer buffer) {
            byte hashedSecretLength = buffer.get();
            if (hashedSecretLength > 0) {
                hashedSecret = new byte[hashedSecretLength];
                buffer.get(hashedSecret);
            } else {
                hashedSecret = Convert.EMPTY_BYTE;
            }
            algorithm = buffer.get();
        }

        private HashVoting(JSONObject attachmentData) {
            String hashedSecret = Convert.emptyToNull((String)attachmentData.get("phasingHashedSecret"));
            if (hashedSecret != null) {
                this.hashedSecret = Convert.parseHexString(hashedSecret);
                this.algorithm = ((Long) attachmentData.get("phasingHashedSecretAlgorithm")).byteValue();
            } else {
                this.hashedSecret = Convert.EMPTY_BYTE;
                this.algorithm = 0;
            }
        }

        public HashVoting(byte[] hashedSecret, byte algorithm) {
            this.hashedSecret = hashedSecret != null ? hashedSecret : Convert.EMPTY_BYTE;
            this.algorithm = algorithm;
        }

        public byte[] getHashedSecret() {
            return hashedSecret;
        }

        public byte getAlgorithm() {
            return algorithm;
        }

        private int getMySize() {
            return 1 + hashedSecret.length + 1;
        }

        private void putMyBytes(ByteBuffer buffer) {
            buffer.put((byte) hashedSecret.length);
            buffer.put(hashedSecret);
            buffer.put(algorithm);
        }

        private void putMyJSON(JSONObject json) {
            if (hashedSecret.length > 0) {
                json.put("phasingHashedSecret", Convert.toHexString(hashedSecret));
                json.put("phasingHashedSecretAlgorithm", algorithm);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof HashVoting)) {
                return false;
            }
            HashVoting other = (HashVoting) obj;
            return Arrays.equals(other.hashedSecret, this.hashedSecret)
                    && other.algorithm == this.algorithm;
        }

        @Override
        public int hashCode() {
            return 31 * Arrays.hashCode(hashedSecret) + algorithm;
        }
    }

    public static class CompositeVoting {

        public static final CompositeVoting EMPTY = new CompositeVoting("", null);
        public static final StringRw EXPRESSION_RW = new StringRw(SHORT, Constants.MAX_PHASING_COMPOSITE_VOTE_EXPRESSION_LENGTH);
        public static final StringRw SUB_POLL_NAME_RW = new StringRw(BYTE, Constants.MAX_PHASING_COMPOSITE_VOTE_SUBPOLL_NAME_LENGTH);

        private final String expressionStr;
        private final SortedMap<String, PhasingParams> subPolls;
        private final BooleanExpression expression;

        private CompositeVoting(ByteBuffer buffer) throws NxtException.NotValidException {
            this.expressionStr = EXPRESSION_RW.readFromBuffer(buffer);
            this.expression = new BooleanExpression(this.expressionStr);
            byte subPollsNum = buffer.get();
            if (subPollsNum > 0) {
                SortedMap<String, PhasingParams> subPolls = new TreeMap<>();
                for (int i = 0; i < subPollsNum; i++) {
                    String subPollName = SUB_POLL_NAME_RW.readFromBuffer(buffer);
                    PhasingParams subPoll = new PhasingParams(buffer);
                    subPolls.put(subPollName, subPoll);
                }
                this.subPolls = Collections.unmodifiableSortedMap(subPolls);
            } else {
                this.subPolls = Collections.emptySortedMap();
            }
        }

        public CompositeVoting(String expression, SortedMap<String, PhasingParams> subPolls) {
            this.expressionStr = Convert.nullToEmpty(expression);
            this.expression = new BooleanExpression(expressionStr);
            this.subPolls = subPolls == null ? Collections.emptySortedMap() : Collections.unmodifiableSortedMap(subPolls);
        }

        private CompositeVoting(JSONObject attachmentData) {
            this.expressionStr = Convert.nullToEmpty((String)attachmentData.get("phasingExpression"));
            this.expression = new BooleanExpression(this.expressionStr);
            Set<String> variables = this.expression.getVariables();
            JSONObject subPollsJson = (JSONObject)attachmentData.get("phasingSubPolls");
            subPolls = new TreeMap<>();
            for (String variable : variables) {
                JSONObject subPollJson = (JSONObject)subPollsJson.get(variable);
                subPolls.put(variable, new PhasingParams(subPollJson));
            }
        }

        private int getMySize() {
            int result = EXPRESSION_RW.getSize(expressionStr);
            result += 1;
            result += subPolls.entrySet().stream().mapToInt(
                    entry -> SUB_POLL_NAME_RW.getSize(entry.getKey()) + entry.getValue().getMySize()
            ).sum();

            return result;
        }

        private void putMyBytes(ByteBuffer buffer) {
            EXPRESSION_RW.writeToBuffer(expressionStr, buffer);

            buffer.put((byte) subPolls.size());
            subPolls.forEach((name, subPoll) -> {
                SUB_POLL_NAME_RW.writeToBuffer(name, buffer);
                subPoll.putMyBytes(buffer);
            });
        }

        private void putMyJSON(JSONObject json) {
            if (!expressionStr.isEmpty()) {
                json.put("phasingExpression", expressionStr);
            }
            if (!subPolls.isEmpty()) {
                JSONObject subPollsJson = new JSONObject();
                subPolls.forEach((name, subPoll) -> {
                    JSONObject subPollJson = new JSONObject();
                    subPoll.putMyJSON(subPollJson);
                    subPollsJson.put(name, subPollJson);
                });

                json.put("phasingSubPolls", subPollsJson);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CompositeVoting)) {
                return false;
            }
            CompositeVoting other = (CompositeVoting)obj;
            return other.expressionStr.equals(this.expressionStr)
                    && other.subPolls.equals(this.subPolls);
        }

        @Override
        public int hashCode() {
            return 31 * expressionStr.hashCode() + subPolls.hashCode();
        }
    }

    public static class PropertyVoting {

        public static final PropertyVoting EMPTY = new PropertyVoting(0, "", "");

        private final long propertySetterId;
        private final String propertyName;
        private final String propertyValue;

        private PropertyVoting(ByteBuffer buffer) throws NxtException.NotValidException {
            this.propertySetterId = buffer.getLong();
            this.propertyName = Account.PROPERTY_NAME_RW.readFromBuffer(buffer);
            this.propertyValue = Account.PROPERTY_VALUE_RW.readFromBuffer(buffer);
        }

        private PropertyVoting(JSONObject attachmentData) {
            this.propertySetterId = Convert.parseUnsignedLong((String)attachmentData.get("setter"));
            this.propertyName = Convert.nullToEmpty((String)attachmentData.get("name"));
            this.propertyValue = Convert.nullToEmpty((String)attachmentData.get("value"));
        }

        public PropertyVoting(long propertySetterId, String propertyName, String propertyValue) {
            this.propertySetterId = propertySetterId;
            this.propertyName = Convert.nullToEmpty(propertyName);
            this.propertyValue = Convert.nullToEmpty(propertyValue);
        }

        private int getMySize() {
            return 8 + Account.PROPERTY_NAME_RW.getSize(propertyName) + Account.PROPERTY_VALUE_RW.getSize(propertyValue);
        }

        private void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(propertySetterId);
            Account.PROPERTY_NAME_RW.writeToBuffer(propertyName, buffer);
            Account.PROPERTY_VALUE_RW.writeToBuffer(propertyValue, buffer);
        }

        private void putMyJSON(JSONObject json) {
            if (propertySetterId != 0) {
                json.put("setter", Long.toUnsignedString(propertySetterId));
            }
            if (!propertyName.isEmpty()) {
                json.put("name", propertyName);
            }
            if (!propertyValue.isEmpty()) {
                json.put("value", propertyValue);
            }
        }

        private int save(PreparedStatement pstmt, int i) throws SQLException {
            pstmt.setLong(++i, propertySetterId);
            pstmt.setString(++i, propertyName);
            pstmt.setString(++i, propertyValue);
            return i;
        }

        private void validate(String prefix) throws NxtException.NotValidException {
            if (propertySetterId != 0) {
                if (propertyName.isEmpty()) {
                    throw new NxtException.NotValidException(prefix + "PropertyName must not be empty");
                }
                if (!Account.PROPERTY_NAME_RW.validate(propertyName)) {
                    throw new NxtException.NotValidException("Invalid " + prefix + "PropertyName " + propertyName);
                }
                if (!Account.PROPERTY_VALUE_RW.validate(propertyValue)) {
                    throw new NxtException.NotValidException("Invalid " + prefix + "PropertyValue " + propertyValue);
                }
            }
        }

        private void validateEmpty(String prefix) throws NxtException.NotValidException {
            if (propertySetterId != 0) {
                throw new NxtException.NotValidException(prefix + "PropertySetterId can only be used with VotingModel.PROPERTY");
            }
            if (!propertyName.isEmpty()) {
                throw new NxtException.NotValidException(prefix + "PropertyName can only be used with VotingModel.PROPERTY");
            }
            if (!propertyValue.isEmpty()) {
                throw new NxtException.NotValidException(prefix + "PropertyValue can only be used with VotingModel.PROPERTY");
            }
        }

        public long getSetterId() {
            return propertySetterId;
        }

        public String getName() {
            return propertyName;
        }

        public String getValue() {
            return propertyValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PropertyVoting)) {
                return false;
            }
            PropertyVoting other = (PropertyVoting)obj;
            return other.propertySetterId == this.propertySetterId
                    && other.propertyName.equals(this.propertyName)
                    && other.propertyValue.equals(this.propertyValue);
        }

        @Override
        public int hashCode() {
            int hashCode = 17;
            hashCode = 31 * hashCode + Long.hashCode(propertySetterId);
            hashCode = 31 * hashCode + propertyName.hashCode();
            hashCode = 31 * hashCode + propertyValue.hashCode();
            return hashCode;
        }
    }

    private final long quorum;
    private final VoteWeighting voteWeighting;
    private long[] whitelist;
    private Supplier<long[]> whitelistSupplier = null;
    private List<ChainTransactionId> linkedTransactionsIds;
    private Supplier<List<ChainTransactionId>> linkedTransactionsSupplier = null;
    private final HashVoting hashVoting;
    private final CompositeVoting compositeVoting;
    private final PropertyVoting senderPropertyVoting;
    private final PropertyVoting recipientPropertyVoting;

    public PhasingParams(ByteBuffer buffer) throws NxtException.NotValidException {
        byte votingModel = buffer.get();
        quorum = buffer.getLong();
        long minBalance = buffer.getLong();
        byte whitelistSize = buffer.get();
        if (whitelistSize > 0) {
            whitelist = new long[whitelistSize];
            for (int i = 0; i < whitelistSize; i++) {
                whitelist[i] = buffer.getLong();
            }
        } else {
            whitelist = Convert.EMPTY_LONG;
        }
        long holdingId = buffer.getLong();
        byte minBalanceModel = buffer.get();
        voteWeighting = new VoteWeighting(votingModel, holdingId, minBalance, minBalanceModel);

        byte size = 0;
        if (voteWeighting.getVotingModel() == TRANSACTION) {
            size = buffer.get();
        }
        if (size > 0) {
            linkedTransactionsIds = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                linkedTransactionsIds.add(ChainTransactionId.parse(buffer));
            }
        } else {
            linkedTransactionsIds = Collections.emptyList();
        }

        if (voteWeighting.getVotingModel() == HASH) {
            hashVoting = new HashVoting(buffer);
        } else {
            hashVoting = new HashVoting(Convert.EMPTY_BYTE, (byte)0);
        }

        if (voteWeighting.getVotingModel() == COMPOSITE) {
            compositeVoting = new CompositeVoting(buffer);
        } else {
            compositeVoting = CompositeVoting.EMPTY;
        }

        if (voteWeighting.getVotingModel() == PROPERTY) {
            senderPropertyVoting = new PropertyVoting(buffer);
            recipientPropertyVoting = new PropertyVoting(buffer);
        } else {
            senderPropertyVoting = PropertyVoting.EMPTY;
            recipientPropertyVoting = PropertyVoting.EMPTY;
        }
    }

    public PhasingParams(JSONObject attachmentData) {
        quorum = Convert.parseLong(attachmentData.get("phasingQuorum"));
        long minBalance = Convert.parseLong(attachmentData.get("phasingMinBalance"));
        byte votingModel = ((Long) attachmentData.get("phasingVotingModel")).byteValue();
        long holdingId = Convert.parseUnsignedLong((String) attachmentData.get("phasingHolding"));
        JSONArray whitelistJson = (JSONArray) (attachmentData.get("phasingWhitelist"));
        if (whitelistJson != null && whitelistJson.size() > 0) {
            whitelist = new long[whitelistJson.size()];
            for (int i = 0; i < whitelist.length; i++) {
                whitelist[i] = Convert.parseUnsignedLong((String) whitelistJson.get(i));
            }
        } else {
            whitelist = Convert.EMPTY_LONG;
        }
        byte minBalanceModel = ((Long) attachmentData.get("phasingMinBalanceModel")).byteValue();
        voteWeighting = new VoteWeighting(votingModel, holdingId, minBalance, minBalanceModel);

        JSONArray linkedTransactionsJson = (JSONArray) attachmentData.get("phasingLinkedTransactions");
        if (linkedTransactionsJson != null && linkedTransactionsJson.size() > 0) {
            linkedTransactionsIds = new ArrayList<>(linkedTransactionsJson.size());
            linkedTransactionsJson.forEach(json -> linkedTransactionsIds.add(ChainTransactionId.parse((JSONObject)json)));
        } else {
            linkedTransactionsIds = Collections.emptyList();
        }
        this.hashVoting = new HashVoting(attachmentData);

        if (voteWeighting.getVotingModel() == COMPOSITE) {
            compositeVoting = new CompositeVoting(attachmentData);
        } else {
            compositeVoting = CompositeVoting.EMPTY;
        }

        boolean isPropertyVoting = voteWeighting.getVotingModel() == PROPERTY;

        Object senderProperty = attachmentData.get("phasingSenderProperty");
        if (isPropertyVoting && senderProperty != null) {
            senderPropertyVoting = new PropertyVoting((JSONObject) senderProperty);
        } else {
            senderPropertyVoting = PropertyVoting.EMPTY;
        }
        Object recipientProperty = attachmentData.get("phasingRecipientProperty");
        if (isPropertyVoting && recipientProperty != null) {
            recipientPropertyVoting = new PropertyVoting((JSONObject) recipientProperty);
        } else {
            recipientPropertyVoting = PropertyVoting.EMPTY;
        }

    }

    public PhasingParams(VoteWeighting voteWeighting, long quorum, long[] whitelist,
                         List<ChainTransactionId> linkedTransactionsIds, HashVoting hashVoting,
                         CompositeVoting compositeVoting, PropertyVoting senderPropertyVoting, PropertyVoting recipientPropertyVoting) {
        this(voteWeighting, quorum, hashVoting, compositeVoting, senderPropertyVoting, recipientPropertyVoting);
        if (whitelist != null && whitelist.length > 0) {
            Arrays.sort(whitelist);
        }
        setWhitelist(whitelist);
        if (linkedTransactionsIds != null && !linkedTransactionsIds.isEmpty()) {
            Collections.sort(linkedTransactionsIds);
        }
        setLinkedTransactions(linkedTransactionsIds);
    }

    public PhasingParams(VoteWeighting voteWeighting, long quorum, Supplier<long[]> whitelistSupplier,
                         Supplier<List<ChainTransactionId>> linkedTransactionsSupplier, HashVoting hashVoting,
                         CompositeVoting compositeVoting, PropertyVoting senderPropertyVoting, PropertyVoting recipientPropertyVoting) {

        this(voteWeighting, quorum, hashVoting, compositeVoting, senderPropertyVoting, recipientPropertyVoting);

        this.whitelistSupplier = whitelistSupplier;
        if (whitelistSupplier == null) {
            this.whitelist = Convert.EMPTY_LONG;
        }
        this.linkedTransactionsSupplier = linkedTransactionsSupplier;
        if (linkedTransactionsSupplier == null) {
            this.linkedTransactionsIds = Collections.emptyList();
        }
    }

    private PhasingParams(VoteWeighting voteWeighting, long quorum, HashVoting hashVoting, CompositeVoting compositeVoting,
                          PropertyVoting senderPropertyVoting, PropertyVoting recipientPropertyVoting) {
        this.quorum = quorum;
        this.voteWeighting = voteWeighting;
        this.hashVoting = hashVoting != null ? hashVoting : new HashVoting(Convert.EMPTY_BYTE, (byte)0);
        this.compositeVoting = compositeVoting != null ? compositeVoting : CompositeVoting.EMPTY;
        this.senderPropertyVoting = senderPropertyVoting != null ? senderPropertyVoting : PropertyVoting.EMPTY;
        this.recipientPropertyVoting = recipientPropertyVoting != null ? recipientPropertyVoting : PropertyVoting.EMPTY;
    }

    public int getMySize() {
        int result = 1 + 8 + 8 + 1 + 8 * getWhitelist().length + 8 + 1;
        if (voteWeighting.getVotingModel() == TRANSACTION) {
            result += 1 + ChainTransactionId.BYTE_SIZE * getLinkedTransactionsIds().size();
        }
        if (voteWeighting.getVotingModel() == HASH) {
            result += hashVoting.getMySize();
        }
        if (voteWeighting.getVotingModel() == COMPOSITE) {
            result += compositeVoting.getMySize();
        }
        if (voteWeighting.getVotingModel() == PROPERTY) {
            result += senderPropertyVoting.getMySize();
            result += recipientPropertyVoting.getMySize();
        }
        return result;
    }

    public void putMyBytes(ByteBuffer buffer) {
        buffer.put(voteWeighting.getVotingModel().getCode());
        buffer.putLong(quorum);
        buffer.putLong(voteWeighting.getMinBalance());
        long[] whitelist = getWhitelist();
        buffer.put((byte) whitelist.length);
        for (long account : whitelist) {
            buffer.putLong(account);
        }
        buffer.putLong(voteWeighting.getHoldingId());
        buffer.put(voteWeighting.getMinBalanceModel().getCode());
        if (voteWeighting.getVotingModel() == TRANSACTION) {
            List<ChainTransactionId> transactionsIds = getLinkedTransactionsIds();
            buffer.put((byte) transactionsIds.size());
            transactionsIds.forEach(linkedTransaction -> linkedTransaction.put(buffer));
        }
        if (voteWeighting.getVotingModel() == HASH) {
            hashVoting.putMyBytes(buffer);
        }
        if (voteWeighting.getVotingModel() == COMPOSITE) {
            compositeVoting.putMyBytes(buffer);
        }
        if (voteWeighting.getVotingModel() == PROPERTY) {
            senderPropertyVoting.putMyBytes(buffer);
            recipientPropertyVoting.putMyBytes(buffer);
        }
    }

    public void putMyJSON(JSONObject json) {
        json.put("phasingQuorum", String.valueOf(quorum));
        json.put("phasingMinBalance", String.valueOf(voteWeighting.getMinBalance()));
        json.put("phasingVotingModel", voteWeighting.getVotingModel().getCode());
        json.put("phasingHolding", Long.toUnsignedString(voteWeighting.getHoldingId()));
        json.put("phasingMinBalanceModel", voteWeighting.getMinBalanceModel().getCode());
        long[] whitelist = getWhitelist();
        if (whitelist.length > 0) {
            JSONArray whitelistJson = new JSONArray();
            for (long accountId : whitelist) {
                whitelistJson.add(Long.toUnsignedString(accountId));
            }
            json.put("phasingWhitelist", whitelistJson);
        }
        List<ChainTransactionId> transactionsIds = getLinkedTransactionsIds();
        if (transactionsIds.size() > 0) {
            JSONArray linkedTransactionsJson = new JSONArray();
            transactionsIds.forEach(linkedTransaction -> linkedTransactionsJson.add(linkedTransaction.getJSON()));
            json.put("phasingLinkedTransactions", linkedTransactionsJson);
        }

        hashVoting.putMyJSON(json);

        compositeVoting.putMyJSON(json);

        if (senderPropertyVoting.getSetterId() != 0) {
            JSONObject senderPropertyJson = new JSONObject();
            senderPropertyVoting.putMyJSON(senderPropertyJson);
            json.put("phasingSenderProperty", senderPropertyJson);
        }
        if (recipientPropertyVoting.getSetterId() != 0) {
            JSONObject recipientPropertyJson = new JSONObject();
            recipientPropertyVoting.putMyJSON(recipientPropertyJson);
            json.put("phasingRecipientProperty", recipientPropertyJson);
        }
    }

    public void validateRestrictableParams() throws ValidationException {
        validateRestrictableParams(false);
    }

    private void validateRestrictableParams(boolean isSubPoll) throws ValidationException {
        long[] whitelist = getWhitelist();
        if (whitelist.length > Constants.MAX_PHASING_WHITELIST_SIZE) {
            throw new NxtException.NotValidException("Whitelist is too big");
        }

        long previousAccountId = 0;
        for (long accountId : whitelist) {
            if (previousAccountId != 0 && accountId < previousAccountId) {
                throw new NxtException.NotValidException("Whitelist not sorted " + Arrays.toString(whitelist));
            }
            if (accountId == previousAccountId) {
                throw new NxtException.NotValidException("Duplicate accountId " + Long.toUnsignedString(accountId) + " in whitelist");
            }
            previousAccountId = accountId;
        }

        if (quorum <= 0 && voteWeighting.getVotingModel() != NONE) {
            throw new NxtException.NotValidException("quorum <= 0");
        }

        if (voteWeighting.getVotingModel() == NONE) {
            if (quorum != 0) {
                throw new NxtException.NotValidException("Quorum must be 0 for no-voting phased transaction");
            }
            if (whitelist.length != 0) {
                throw new NxtException.NotValidException("No whitelist needed for no-voting phased transaction");
            }
        }

        if (voteWeighting.getVotingModel() == ACCOUNT && whitelist.length > 0 && quorum > whitelist.length) {
            throw new NxtException.NotValidException("Quorum of " + quorum + " cannot be achieved in by-account voting with whitelist of length "
                    + whitelist.length);
        }

        voteWeighting.validate();

        if (voteWeighting.getVotingModel() == CURRENCY) {
            Currency currency = Currency.getCurrency(voteWeighting.getHoldingId());
            if (currency == null) {
                throw new NxtException.NotCurrentlyValidException("Currency " + Long.toUnsignedString(voteWeighting.getHoldingId()) + " not found");
            }
            if (quorum > currency.getMaxSupplyQNT()) {
                throw new NxtException.NotCurrentlyValidException("Quorum of " + quorum
                        + " exceeds max currency supply " + currency.getMaxSupplyQNT());
            }
            if (voteWeighting.getMinBalance() > currency.getMaxSupplyQNT()) {
                throw new NxtException.NotCurrentlyValidException("MinBalance of " + voteWeighting.getMinBalance()
                        + " exceeds max currency supply " + currency.getMaxSupplyQNT());
            }
        } else if (voteWeighting.getVotingModel() == ASSET) {
            Asset asset = Asset.getAsset(voteWeighting.getHoldingId());
            if (quorum > asset.getQuantityQNT()) {
                throw new NxtException.NotCurrentlyValidException("Quorum of " + quorum
                        + " exceeds total asset quantity " + asset.getQuantityQNT());
            }
            if (voteWeighting.getMinBalance() > asset.getQuantityQNT()) {
                throw new NxtException.NotCurrentlyValidException("MinBalance of " + voteWeighting.getMinBalance()
                        + " exceeds total asset quantity " + asset.getQuantityQNT());
            }
        } else if (voteWeighting.getMinBalance() > 0) {
            if (voteWeighting.getMinBalanceModel() == VoteWeighting.MinBalanceModel.ASSET) {
                Asset asset = Asset.getAsset(voteWeighting.getHoldingId());
                if (voteWeighting.getMinBalance() > asset.getQuantityQNT()) {
                    throw new NxtException.NotCurrentlyValidException("MinBalance of " + voteWeighting.getMinBalance()
                            + " exceeds total asset quantity " + asset.getQuantityQNT());
                }
            } else if (voteWeighting.getMinBalanceModel() == VoteWeighting.MinBalanceModel.CURRENCY) {
                Currency currency = Currency.getCurrency(voteWeighting.getHoldingId());
                if (currency == null) {
                    throw new NxtException.NotCurrentlyValidException("Currency " + Long.toUnsignedString(voteWeighting.getHoldingId()) + " not found");
                }
                if (voteWeighting.getMinBalance() > currency.getMaxSupplyQNT()) {
                    throw new NxtException.NotCurrentlyValidException("MinBalance of " + voteWeighting.getMinBalance()
                            + " exceeds max currency supply " + currency.getMaxSupplyQNT());
                }
            }
        }

        if (voteWeighting.getVotingModel() == COMPOSITE) {
            if (isSubPoll) {
                throw new NxtException.NotValidException("Nested sub-polls are not allowed");
            }

            if (quorum != 1) {
                throw new NxtException.NotValidException("Composite voting requires quorum 1");
            }

            if (whitelist.length != 0) {
                throw new NxtException.NotValidException("No whitelist needed for composite voting");
            }

            if (getExpressionStr().isEmpty()) {
                throw new NxtException.NotValidException("Composite voting requires boolean expression");
            }
            if (!CompositeVoting.EXPRESSION_RW.validate(getExpressionStr())) {
                throw new NxtException.NotValidException("Invalid boolean expression size " + getExpressionStr());
            }

            if (getSubPolls().size() > Constants.MAX_PHASING_COMPOSITE_VOTE_VARIABLES_COUNT) {
                throw new NxtException.NotValidException("Sub-polls count " + getSubPolls().size() + " exceeds the maximum of "
                        + Constants.MAX_PHASING_COMPOSITE_VOTE_VARIABLES_COUNT + " variables");
            }

            BooleanExpression expression = getExpression();
            BooleanExpression.BadSyntaxException e = expression.getSyntaxException();
            if (e != null) {
                throw new NxtException.NotValidException("Failed to parse boolean expression \"" + e.getMessage() + "\"", e);
            }

            if (expression.getVariables().size() > Constants.MAX_PHASING_COMPOSITE_VOTE_VARIABLES_COUNT) {
                throw new NxtException.NotValidException("Variables count " + getSubPolls().size() + " exceeds the maximum of "
                        + Constants.MAX_PHASING_COMPOSITE_VOTE_VARIABLES_COUNT);
            }

            for (String v : expression.getVariables()) {
                if (!CompositeVoting.SUB_POLL_NAME_RW.validate(v)) {
                    throw new NxtException.NotValidException("Invalid variable name size " + v);
                }
            }

            int literalsCount = expression.getLiteralsCount();
            if (literalsCount > Constants.MAX_PHASING_COMPOSITE_VOTE_LITERALS_COUNT) {
                throw new NxtException.NotValidException("Literals count " + literalsCount + "  exceeds the maximum of "
                        + Constants.MAX_PHASING_COMPOSITE_VOTE_LITERALS_COUNT);
            }

            if (!expression.getVariables().equals(getSubPolls().keySet())) {
                throw new NxtException.NotValidException("The variables found in the boolean expression do not match the sub-polls names");
            }

            for (PhasingParams subPoll : getSubPolls().values()) {
                subPoll.validateRestrictableParams(true);
            }
        } else {
            if (!getExpressionStr().isEmpty()) {
                throw new NxtException.NotValidException("Boolean expression can only be used with VotingModel.COMPOSITE");
            }
            if (!getSubPolls().isEmpty()) {
                throw new NxtException.NotValidException("Sub-polls can only be used with VotingModel.COMPOSITE");
            }
        }

        if (voteWeighting.getVotingModel() == PROPERTY) {
            senderPropertyVoting.validate("Sender");
            recipientPropertyVoting.validate("Recipient");
            if (senderPropertyVoting.getSetterId() == 0 && recipientPropertyVoting.getSetterId() == 0) {
                throw new NxtException.NotValidException("By-property voting requires property parameters for either " +
                        "sender or recipient, or both");
            }
        } else {
            senderPropertyVoting.validateEmpty("Sender");
            recipientPropertyVoting.validateEmpty("Recipient");
        }
    }

    void validate(Transaction transaction) throws ValidationException {
        validateRestrictableParams();
        validateByTransactionAndHashParams(transaction);
    }

    private void validateByTransactionAndHashParams(Transaction transaction) throws NxtException.NotValidException, NxtException.NotCurrentlyValidException {
        List<ChainTransactionId> linkedTransactionsIds = getLinkedTransactionsIds();
        if (getVoteWeighting().getVotingModel() == TRANSACTION) {
            if (linkedTransactionsIds.size() == 0 || linkedTransactionsIds.size() > Constants.MAX_PHASING_LINKED_TRANSACTIONS) {
                throw new NxtException.NotValidException("Invalid number of linkedFullHashes " + linkedTransactionsIds.size());
            }
            ChainTransactionId previous = null;
            for (ChainTransactionId linkedTransactionId : linkedTransactionsIds) {
                byte[] hash = linkedTransactionId.getFullHash();
                if (Convert.emptyToNull(hash) == null || hash.length != 32) {
                    throw new NxtException.NotValidException("Invalid linkedFullHash " + Convert.toHexString(hash));
                }
                if (previous != null && previous.compareTo(linkedTransactionId) >= 0) {
                    throw new NxtException.NotValidException("Linked transaction ids not sorted or contain duplicates");
                }
                previous = linkedTransactionId;
                Chain chain = linkedTransactionId.getChain();
                if (chain == null) {
                    throw new NxtException.NotValidException("Invalid chain id " + linkedTransactionId.getChainId());
                }
                TransactionImpl linkedTransaction = chain.getTransactionHome().findTransaction(hash, Nxt.getBlockchain().getHeight());
                if (linkedTransaction != null) {
                    if (transaction.getTimestamp() - linkedTransaction.getTimestamp() > Constants.MAX_REFERENCED_TRANSACTION_TIMESPAN) {
                        throw new NxtException.NotValidException("Linked transaction cannot be more than 60 days older than the phased transaction");
                    }
                    if (linkedTransaction instanceof ChildTransaction && ((ChildTransaction)linkedTransaction).getPhasing() != null) {
                        throw new NxtException.NotCurrentlyValidException("Cannot link to an already existing phased transaction");
                    }
                }
            }
            if (getQuorum() > linkedTransactionsIds.size()) {
                throw new NxtException.NotValidException("Quorum of " + getQuorum() + " cannot be achieved in by-transaction voting with "
                        + linkedTransactionsIds.size() + " linked full hashes only");
            }
        } else {
            if (linkedTransactionsIds.size() != 0) {
                throw new NxtException.NotValidException("LinkedFullHashes can only be used with VotingModel.TRANSACTION");
            }
        }

        byte[] hashedSecret = getHashedSecret();
        byte algorithm = getAlgorithm();
        if (getVoteWeighting().getVotingModel() == HASH) {
            if (getQuorum() != 1) {
                throw new NxtException.NotValidException("Quorum must be 1 for by-hash voting");
            }
            if (hashedSecret.length == 0 || hashedSecret.length > Byte.MAX_VALUE) {
                throw new NxtException.NotValidException("Invalid hashedSecret " + Convert.toHexString(hashedSecret));
            }
            if (PhasingPollHome.getHashFunction(algorithm) == null) {
                throw new NxtException.NotValidException("Invalid hashedSecretAlgorithm " + algorithm);
            }
        } else {
            if (hashedSecret.length != 0) {
                throw new NxtException.NotValidException("HashedSecret can only be used with VotingModel.HASH");
            }
            if (algorithm != 0) {
                throw new NxtException.NotValidException("HashedSecretAlgorithm can only be used with VotingModel.HASH");
            }
        }

        if (voteWeighting.getVotingModel() == COMPOSITE) {
            for (PhasingParams subPoll : getSubPolls().values()) {
                subPoll.validateByTransactionAndHashParams(transaction);
            }
        }
    }

    public void checkApprovable() throws NxtException.NotCurrentlyValidException {
        if (voteWeighting.getVotingModel() == CURRENCY
                && Currency.getCurrency(voteWeighting.getHoldingId()) == null) {
            throw new NxtException.NotCurrentlyValidException("Currency " + Long.toUnsignedString(voteWeighting.getHoldingId()) + " not found");
        }
        if (voteWeighting.getMinBalance() > 0 && voteWeighting.getMinBalanceModel() == VoteWeighting.MinBalanceModel.CURRENCY
                && Currency.getCurrency(voteWeighting.getHoldingId()) == null) {
            throw new NxtException.NotCurrentlyValidException("Currency " + Long.toUnsignedString(voteWeighting.getHoldingId()) + " not found");
        }

        for (PhasingParams subPoll : getSubPolls().values()) {
            subPoll.checkApprovable();
        }
    }

    public boolean isAccountWhitelisted(long accountId) {
        if (voteWeighting.getVotingModel() == COMPOSITE) {
            return getSubPolls().values().stream().anyMatch(subPoll -> subPoll.isAccountWhitelisted(accountId));
        } else {
            long[] whitelist = getWhitelist();
            return whitelist.length == 0 || Arrays.binarySearch(whitelist, accountId) >= 0;
        }
    }

    public boolean acceptsVotes() {
        if (voteWeighting.getVotingModel() == COMPOSITE) {
            return getSubPolls().values().stream().anyMatch(PhasingParams::acceptsVotes);
        } else {
            return voteWeighting.acceptsVotes();
        }

    }

    public long getQuorum() {
        return quorum;
    }

    public long[] getWhitelist() {
        if (this.whitelist == null) {
            setWhitelist(whitelistSupplier.get());
        }
        return whitelist;
    }

    private void setWhitelist(long[] whitelist) {
        this.whitelist = Convert.nullToEmpty(whitelist);
    }

    public VoteWeighting getVoteWeighting() {
        return voteWeighting;
    }

    public List<ChainTransactionId> getLinkedTransactionsIds() {
        if (linkedTransactionsIds == null) {
            setLinkedTransactions(linkedTransactionsSupplier.get());
        }
        return linkedTransactionsIds;
    }

    private void setLinkedTransactions(List<ChainTransactionId> transactionsIds) {
        if (transactionsIds == null) {
            linkedTransactionsIds = Collections.emptyList();
        } else {
            linkedTransactionsIds = Collections.unmodifiableList(transactionsIds);
        }
    }

    public byte[] getHashedSecret() {
        return hashVoting.hashedSecret;
    }

    public byte getAlgorithm() {
        return hashVoting.algorithm;
    }

    public String getExpressionStr() {
        return compositeVoting.expressionStr;
    }

    public BooleanExpression getExpression() {
        return compositeVoting.expression;
    }

    public SortedMap<String, PhasingParams> getSubPolls() {
        return compositeVoting.subPolls;
    }


    public PropertyVoting getSenderPropertyVoting() {
        return senderPropertyVoting;
    }

    public PropertyVoting getRecipientPropertyVoting() {
        return recipientPropertyVoting;
    }

    public HashVoting getHashVoting() {
        return hashVoting;
    }

    public boolean allowEarlyFinish() {
        return allowEarlyFinish(this, false);
    }

    public boolean allowFinishAtCreation() {
        return allowEarlyFinish(this, true);
    }

    private static boolean allowEarlyFinish(PhasingParams phasingParams, final boolean allowFinishAtCreation) {
        VoteWeighting voteWeighting = phasingParams.getVoteWeighting();
        VoteWeighting.VotingModel votingModel = voteWeighting.getVotingModel();
        if (votingModel == COMPOSITE) {
            //the composite poll is allowed to finish early if the expression produces result having only TRUE values
            //for the early-finishing sub-polls - before finish height the expression is evaluated only with TRUE or UNKNOWN values
            Map<String, BooleanExpression.Value> values = phasingParams.getSubPolls().entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey, e ->
                            allowEarlyFinish(e.getValue(), allowFinishAtCreation) ? BooleanExpression.Value.TRUE : BooleanExpression.Value.UNKNOWN));
            try {
                return phasingParams.getExpression().evaluate(values) != BooleanExpression.Value.UNKNOWN;
            } catch (BooleanExpression.BooleanExpressionException e) {
                //Should never happen - these phasing parameters are always loaded from the DB and the expression
                //should be validated before entering the DB
                throw new RuntimeException("Invalid boolean expression while counting votes.", e);
            }
        } else {
            if (allowFinishAtCreation) {
                return votingModel == PROPERTY;
            } else {
                return votingModel != NONE && voteWeighting.isBalanceIndependent() && (phasingParams.getWhitelist().length > 0 || votingModel != ACCOUNT);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PhasingParams)) {
            return false;
        }
        PhasingParams other = (PhasingParams)obj;
        return other.quorum == this.quorum
                && other.voteWeighting.equals(this.voteWeighting)
                && Arrays.equals(other.getWhitelist(), this.getWhitelist())
                && other.getLinkedTransactionsIds().equals(this.getLinkedTransactionsIds())
                && other.hashVoting.equals(this.hashVoting)
                && other.compositeVoting.equals(this.compositeVoting)
                && other.senderPropertyVoting.equals(this.senderPropertyVoting)
                && other.recipientPropertyVoting.equals(this.recipientPropertyVoting);
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode = 31 * hashCode + Long.hashCode(quorum);
        for (long voter : getWhitelist()) {
            hashCode = 31 * hashCode + Long.hashCode(voter);
        }
        hashCode = 31 * hashCode + voteWeighting.hashCode();
        for (ChainTransactionId transactionId : getLinkedTransactionsIds()) {
            hashCode = 31 * hashCode + transactionId.hashCode();
        }

        hashCode = 31 * hashCode + hashVoting.hashCode();

        hashCode = 31 * hashCode + compositeVoting.hashCode();

        hashCode = 31 * hashCode + senderPropertyVoting.hashCode();
        hashCode = 31 * hashCode + recipientPropertyVoting.hashCode();

        return hashCode;
    }

    @Override
    public String toString() {
        JSONObject resultJson = new JSONObject();
        putMyJSON(resultJson);
        return JSON.toJSONString(resultJson);
    }

    public static int setCommonColumnValues(PhasingParams params, PreparedStatement pstmt, int i) throws SQLException {
        VoteWeighting voteWeighting = params.getVoteWeighting();
        pstmt.setByte(++i, voteWeighting.getVotingModel().getCode());
        DbUtils.setLongZeroToNull(pstmt, ++i, params.getQuorum());
        DbUtils.setLongZeroToNull(pstmt, ++i, voteWeighting.getMinBalance());
        DbUtils.setLongZeroToNull(pstmt, ++i, voteWeighting.getHoldingId());
        pstmt.setByte(++i, voteWeighting.getMinBalanceModel().getCode());
        i = params.senderPropertyVoting.save(pstmt, i);
        i = params.recipientPropertyVoting.save(pstmt, i);
        return i;
    }

    static long[] readWhitelist(ResultSet rs) throws SQLException {
        Long[] whitelist = DbUtils.getArray(rs, "whitelist", Long[].class);
        return whitelist == null ? null : Convert.toArray(whitelist);
    }

    static PropertyVoting readPropertyVoting(ResultSet rs, String prefix, VoteWeighting voteWeighting) throws SQLException {
        PropertyVoting propertyVoting = null;
        if (voteWeighting.getVotingModel() == PROPERTY) {
            propertyVoting = new PropertyVoting(rs.getLong(prefix + "property_setter_id"),
                    rs.getString(prefix + "property_name"), rs.getString(prefix + "property_value"));
        }
        return propertyVoting;
    }
}
