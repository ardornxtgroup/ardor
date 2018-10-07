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

package nxt.blockchain;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.account.AccountRestrictions;
import nxt.account.PublicKeyAnnouncementAppendix;
import nxt.ae.AssetControl;
import nxt.crypto.Crypto;
import nxt.db.DbUtils;
import nxt.messaging.EncryptToSelfMessageAppendix;
import nxt.messaging.EncryptedMessageAppendix;
import nxt.messaging.MessageAppendix;
import nxt.util.Convert;
import nxt.util.JSON;
import nxt.util.Logger;
import nxt.voting.PhasingAppendix;
import nxt.voting.PhasingPollHome;
import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

public final class ChildTransactionImpl extends TransactionImpl implements ChildTransaction {

    public static final class BuilderImpl extends TransactionImpl.BuilderImpl implements ChildTransaction.Builder {

        private ChainTransactionId referencedTransactionId;
        private long fxtTransactionId;
        private long feeRateNQTPerFXT;

        private BuilderImpl(int chainId, byte version, byte[] senderPublicKey, long amount, long fee, short deadline,
                    Attachment.AbstractAttachment attachment) {
            super(chainId, version, senderPublicKey, amount, fee, deadline, attachment);
        }

        private BuilderImpl(int chainId, byte version, byte[] senderPublicKey, long amount, long fee, short deadline,
                            List<Appendix.AbstractAppendix> appendages) {
            super(chainId, version, senderPublicKey, amount, fee, deadline, appendages);
        }

        @Override
        public ChildTransactionImpl build(String secretPhrase, boolean isVoucher) throws NxtException.NotValidException {
            preBuild(secretPhrase, isVoucher);
            return new ChildTransactionImpl(this, secretPhrase, isVoucher);
        }

        @Override
        public ChildTransactionImpl build(String secretPhrase) throws NxtException.NotValidException {
            return build(secretPhrase, false);
        }

        @Override
        public ChildTransactionImpl build() throws NxtException.NotValidException {
            return build(null);
        }

        @Override
        public BuilderImpl referencedTransaction(ChainTransactionId referencedTransactionId) {
            this.referencedTransactionId = referencedTransactionId;
            return this;
        }

        BuilderImpl fxtTransactionId(long fxtTransactionId) {
            this.fxtTransactionId = fxtTransactionId;
            return this;
        }

        @Override
        public BuilderImpl feeRateNQTPerFXT(long feeRateNQTPerFXT) {
            this.feeRateNQTPerFXT = feeRateNQTPerFXT;
            return this;
        }

    }

    private final ChildChain childChain;
    private final long fee;
    private final byte[] signature;
    private final ChainTransactionId referencedTransactionId;
    private final MessageAppendix message;
    private final EncryptedMessageAppendix encryptedMessage;
    private final EncryptToSelfMessageAppendix encryptToSelfMessage;
    private final PublicKeyAnnouncementAppendix publicKeyAnnouncement;
    private final PhasingAppendix phasing;

    private volatile long fxtTransactionId;

    private ChildTransactionImpl(BuilderImpl builder, String secretPhrase, boolean isVoucher) throws NxtException.NotValidException {
        super(builder);
        this.childChain = ChildChain.getChildChain(builder.chainId);
        this.referencedTransactionId = builder.referencedTransactionId;
        this.fxtTransactionId = builder.fxtTransactionId;
        MessageAppendix messageAppendix = null;
        EncryptedMessageAppendix encryptedMessageAppendix = null;
        PublicKeyAnnouncementAppendix publicKeyAnnouncementAppendix = null;
        EncryptToSelfMessageAppendix encryptToSelfMessageAppendix = null;
        PhasingAppendix phasingAppendix = null;
        for (Appendix.AbstractAppendix appendix : appendages()) {
            switch (appendix.getAppendixType()) {
                case MessageAppendix.appendixType:
                    messageAppendix = (MessageAppendix) appendix;
                    break;
                case EncryptedMessageAppendix.appendixType:
                    encryptedMessageAppendix = (EncryptedMessageAppendix) appendix;
                    break;
                case PublicKeyAnnouncementAppendix.appendixType:
                    publicKeyAnnouncementAppendix = (PublicKeyAnnouncementAppendix) appendix;
                    break;
                case EncryptToSelfMessageAppendix.appendixType:
                    encryptToSelfMessageAppendix = (EncryptToSelfMessageAppendix) appendix;
                    break;
                case PhasingAppendix.appendixType:
                    phasingAppendix = (PhasingAppendix) appendix;
                    break;
            }
        }
        this.message = messageAppendix;
        this.encryptedMessage = encryptedMessageAppendix;
        this.publicKeyAnnouncement = publicKeyAnnouncementAppendix;
        this.encryptToSelfMessage = encryptToSelfMessageAppendix;
        this.phasing = phasingAppendix;
        if (builder.fee < 0) {
            long minFeeFQT = getMinimumFeeFQT(Nxt.getBlockchain().getHeight());
            if (builder.feeRateNQTPerFXT < 0) {
                throw new NxtException.NotValidException(String.format("Please include fee in %s equivalent to at least %f %s",
                        childChain.getName(), ((double) minFeeFQT) / Constants.ONE_FXT, FxtChain.FXT.getName()));
            }
            BigInteger[] fee = BigInteger.valueOf(minFeeFQT).multiply(BigInteger.valueOf(builder.feeRateNQTPerFXT))
                    .divideAndRemainder(Constants.ONE_FXT_BIG_INTEGER);
            this.fee = fee[1].equals(BigInteger.ZERO) ? fee[0].longValueExact() : fee[0].longValueExact() + 1;
        } else {
            this.fee = builder.fee;
        }
        if (builder.signature != null && secretPhrase != null) {
            throw new NxtException.NotValidException("Transaction is already signed");
        } else if (builder.signature != null) {
            this.signature = builder.signature;
        } else if (secretPhrase != null) {
            byte[] senderPublicKey = builder.senderPublicKey != null ? builder.senderPublicKey : Account.getPublicKey(builder.senderId);
            if (senderPublicKey != null && ! Arrays.equals(senderPublicKey, Crypto.getPublicKey(secretPhrase)) && !isVoucher) {
                throw new NxtException.NotValidException("Secret phrase doesn't match transaction sender public key");
            }
            this.signature = Crypto.sign(bytes(), secretPhrase);
            bytes = null;
        } else {
            this.signature = null;
        }
    }

    @Override
    public ChildChain getChain() {
        return childChain;
    }

    @Override
    public long getFxtTransactionId() {
        return fxtTransactionId;
    }

    void setFxtTransaction(ChildBlockFxtTransactionImpl fxtTransaction) {
        this.fxtTransactionId = fxtTransaction.getId();
        setBlock(fxtTransaction.getBlock());
    }

    void unsetFxtTransaction() {
        this.fxtTransactionId = 0;
        unsetBlock();
        setIndex(-1);
    }

    @Override
    public ChainTransactionId getReferencedTransactionId() {
        return referencedTransactionId;
    }

    @Override
    public MessageAppendix getMessage() {
        return message;
    }

    @Override
    public EncryptedMessageAppendix getEncryptedMessage() {
        return encryptedMessage;
    }

    @Override
    public EncryptToSelfMessageAppendix getEncryptToSelfMessage() {
        return encryptToSelfMessage;
    }

    @Override
    public PhasingAppendix getPhasing() {
        return phasing;
    }

    @Override
    public boolean isPhased() {
        return phasing != null;
    }

    @Override
    public boolean attachmentIsPhased() {
        return attachment.isPhased(this);
    }

    @Override
    public long getFee() {
        return fee;
    }

    @Override
    public byte[] getSignature() {
        return signature;
    }

    @Override
    public JSONObject getJSONObject() {
        JSONObject json = super.getJSONObject();
        if (referencedTransactionId != null) {
            json.put("referencedTransaction", referencedTransactionId.getJSON());
        }
        return json;
    }

    @Override
    public long getMinimumFeeFQT(int blockchainHeight) {
        long totalFee = super.getMinimumFeeFQT(blockchainHeight);
        if (referencedTransactionId != null) {
            totalFee = Math.addExact(totalFee, blockchainHeight < Constants.LIGHT_CONTRACTS_BLOCK ? Constants.ONE_FXT : Constants.ONE_FXT / 100);
        }
        return totalFee;
    }

    @Override
    boolean hasAllReferencedTransactions(int timestamp, int count) {
        if (referencedTransactionId == null) {
            return timestamp - getTimestamp() < Constants.MAX_REFERENCED_TRANSACTION_TIMESPAN && count < 10;
        }
        TransactionImpl referencedTransaction = (TransactionImpl)referencedTransactionId.getTransaction();
        return referencedTransaction != null
                && referencedTransaction.getHeight() < getHeight()
                && referencedTransaction.hasAllReferencedTransactions(timestamp, count + 1);
    }

    @Override
    ByteBuffer generateBytes(boolean includePrunable) {
        ByteBuffer buffer = super.generateBytes(includePrunable);
        if (referencedTransactionId != null) {
            referencedTransactionId.put(buffer);
        } else {
            buffer.putInt(0);
            buffer.put(new byte[32]);
        }
        return buffer;
    }

    @Override
    protected int getSize() {
        return super.getSize() + ChainTransactionId.BYTE_SIZE;
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof ChildTransactionImpl && this.getId() == ((Transaction)o).getId()
                && Arrays.equals(this.getFullHash(), ((Transaction)o).getFullHash());
    }

    @Override
    public final int hashCode() {
        return (int)(getId() ^ (getId() >>> 32));
    }

    @Override
    public void validate() throws NxtException.ValidationException {
        try {
            super.validate();
            if (ChildTransactionType.findTransactionType(getType().getType(), getType().getSubtype()) == null) {
                throw new NxtException.NotValidException("Invalid transaction type " + getType().getName() + " for ChildTransaction");
            }
            if (referencedTransactionId != null) {
                if (referencedTransactionId.getFullHash().length != 32) {
                    throw new NxtException.NotValidException("Invalid referenced transaction full hash " + Convert.toHexString(referencedTransactionId.getFullHash()));
                }
                if (referencedTransactionId.getChain() == null) {
                    throw new NxtException.NotValidException("Invalid referenced transaction chain " + referencedTransactionId.getChainId());
                }
            }
            boolean validatingAtFinish = phasing != null && getSignature() != null && childChain.getPhasingPollHome().getPoll(this) != null;
            int appendixType = -1;
            for (Appendix.AbstractAppendix appendage : appendages()) {
                if (appendage.getAppendixType() <= appendixType) {
                    throw new NxtException.NotValidException("Duplicate or not in order appendix " + appendage.getAppendixName());
                }
                appendixType = appendage.getAppendixType();
                if (!appendage.isAllowed(childChain)) {
                    throw new NxtException.NotValidException("Appendix not allowed on child chain " + appendage.getAppendixName());
                }
                appendage.loadPrunable(this);
                if (!appendage.verifyVersion()) {
                    throw new NxtException.NotValidException("Invalid attachment version " + appendage.getVersion());
                }
                if (validatingAtFinish) {
                    appendage.validateAtFinish(this);
                } else {
                    appendage.validate(this);
                }
            }

            if (getFullSize() > Constants.MAX_CHILDBLOCK_PAYLOAD_LENGTH) {
                throw new NxtException.NotValidException("Transaction size " + getFullSize() + " exceeds maximum payload size");
            }
            if (!validatingAtFinish) {
                validateEcBlock();
                AccountRestrictions.checkTransaction(this);
                AssetControl.checkTransaction(this);
            }
        } catch (NxtException.NotValidException e) {
            if (getSignature() != null) {
                Logger.logMessage("Invalid child transaction " + getStringId());
            }
            throw e;
        }
    }

    @Override
    protected void validateId() throws NxtException.ValidationException {
        super.validateId();
        if (PhasingPollHome.hasUnfinishedPhasedTransaction(getId())) {
            throw new NxtException.NotCurrentlyValidException("Phased transaction currently exists with the same id");
        }
        for (Appendix.AbstractAppendix appendage : appendages()) {
            appendage.validateId(this);
        }
    }

    @Override
    public void apply() {
        Account senderAccount = Account.getAccount(getSenderId());
        senderAccount.apply(getSenderPublicKey());
        Account recipientAccount = null;
        if (getRecipientId() != 0) {
            recipientAccount = Account.getAccount(getRecipientId());
            if (recipientAccount == null) {
                recipientAccount = Account.addOrGetAccount(getRecipientId());
            }
        }
        AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(this);
        if (referencedTransactionId != null) {
            senderAccount.addToUnconfirmedBalance(FxtChain.FXT, getType().getLedgerEvent(), eventId, (long) 0, Constants.UNCONFIRMED_POOL_DEPOSIT_FQT);
        }
        if (attachmentIsPhased()) {
            childChain.getBalanceHome().getBalance(getSenderId()).addToBalance(getType().getLedgerEvent(), eventId, 0, -fee);
        }
        for (Appendix.AbstractAppendix appendage : appendages()) {
            if (!appendage.isPhased(this)) {
                appendage.loadPrunable(this);
                appendage.apply(this, senderAccount, recipientAccount);
            }
        }
    }

    @Override
    void save(Connection con, String schemaTable) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO " + schemaTable
                + " (id, deadline, recipient_id, amount, fee, referenced_transaction_chain_id, referenced_transaction_full_hash, referenced_transaction_id, "
                + "height, block_id, signature, timestamp, type, subtype, sender_id, attachment_bytes, "
                + "block_timestamp, full_hash, version, has_message, has_encrypted_message, has_public_key_announcement, "
                + "has_encrypttoself_message, phased, has_prunable_message, has_prunable_encrypted_message, "
                + "has_prunable_attachment, ec_block_height, ec_block_id, transaction_index, fxt_transaction_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            int i = 0;
            pstmt.setLong(++i, getId());
            pstmt.setShort(++i, getDeadline());
            DbUtils.setLongZeroToNull(pstmt, ++i, getRecipientId());
            pstmt.setLong(++i, getAmount());
            pstmt.setLong(++i, getFee());
            if (referencedTransactionId != null) {
                pstmt.setInt(++i, referencedTransactionId.getChainId());
                pstmt.setBytes(++i, referencedTransactionId.getFullHash());
                pstmt.setLong(++i, referencedTransactionId.getTransactionId());
            } else {
                pstmt.setNull(++i, Types.INTEGER);
                pstmt.setNull(++i, Types.BINARY);
                pstmt.setNull(++i, Types.BIGINT);
            }
            pstmt.setInt(++i, getHeight());
            pstmt.setLong(++i, getBlockId());
            pstmt.setBytes(++i, getSignature());
            pstmt.setInt(++i, getTimestamp());
            pstmt.setByte(++i, getType().getType());
            pstmt.setByte(++i, getType().getSubtype());
            pstmt.setLong(++i, getSenderId());
            int bytesLength = 0;
            for (Appendix appendage : getAppendages()) {
                bytesLength += appendage.getSize();
            }
            if (bytesLength == 0) {
                pstmt.setNull(++i, Types.VARBINARY);
            } else {
                ByteBuffer buffer = ByteBuffer.allocate(bytesLength + 4);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                putAppendages(buffer, false);
                pstmt.setBytes(++i, buffer.array());
            }
            pstmt.setInt(++i, getBlockTimestamp());
            pstmt.setBytes(++i, getFullHash());
            pstmt.setByte(++i, getVersion());
            pstmt.setBoolean(++i, message != null);
            pstmt.setBoolean(++i, encryptedMessage != null);
            pstmt.setBoolean(++i, publicKeyAnnouncement != null);
            pstmt.setBoolean(++i, encryptToSelfMessage != null);
            pstmt.setBoolean(++i, phasing != null);
            pstmt.setBoolean(++i, hasPrunablePlainMessage());
            pstmt.setBoolean(++i, hasPrunableEncryptedMessage());
            pstmt.setBoolean(++i, getAttachment() instanceof Appendix.Prunable);
            pstmt.setInt(++i, getECBlockHeight());
            DbUtils.setLongZeroToNull(pstmt, ++i, getECBlockId());
            pstmt.setShort(++i, getIndex());
            pstmt.setLong(++i, getFxtTransactionId());
            pstmt.executeUpdate();
        }
    }

    @Override
    final UnconfirmedChildTransaction newUnconfirmedTransaction(long arrivalTimestamp, boolean isBundled) {
        return new UnconfirmedChildTransaction(this, arrivalTimestamp, isBundled);
    }

    static ChildTransactionImpl.BuilderImpl newTransactionBuilder(int chainId, byte version, long amount, long fee, short deadline,
                                                                  List<Appendix.AbstractAppendix> appendages, ResultSet rs) {
        try {
            ChildTransactionImpl.BuilderImpl builder = new ChildTransactionImpl.BuilderImpl(chainId, version, null,
                    amount, fee, deadline, appendages);
            byte[] referencedTransactionFullHash = rs.getBytes("referenced_transaction_full_hash");
            if (referencedTransactionFullHash != null) {
                int referencedTransactionChainId = rs.getInt("referenced_transaction_chain_id");
                builder.referencedTransaction(new ChainTransactionId(referencedTransactionChainId, referencedTransactionFullHash));
            }
            long fxtTransactionId = rs.getLong("fxt_transaction_id");
            builder.fxtTransactionId(fxtTransactionId);
            return builder;
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static ChildTransactionImpl.BuilderImpl newTransactionBuilder(int chainId, byte version, byte[] senderPublicKey, long amount, long fee, short deadline,
                                                                  List<Appendix.AbstractAppendix> appendages, ByteBuffer buffer) {
        try {
            ChildTransactionImpl.BuilderImpl builder = new BuilderImpl(chainId, version, senderPublicKey, amount, fee, deadline, appendages);
            ChainTransactionId referencedTransaction = ChainTransactionId.parse(buffer);
            builder.referencedTransaction(referencedTransaction);
            return builder;
        } catch (RuntimeException e) {
            Logger.logDebugMessage("Failed to parse transaction bytes: " + Convert.toHexString(buffer.array()));
            throw e;
        }
    }

    static ChildTransactionImpl.BuilderImpl newTransactionBuilder(int chainId, byte version, byte[] senderPublicKey, long amount, long fee, short deadline,
                                                                  Attachment.AbstractAttachment attachment) {
        return new BuilderImpl(chainId, version, senderPublicKey, amount, fee, deadline, attachment);
    }

    static ChildTransactionImpl.BuilderImpl newTransactionBuilder(int chainId, byte version, byte[] senderPublicKey, long amount, long fee, short deadline,
                                                                  List<Appendix.AbstractAppendix> appendages, JSONObject transactionData) {
        try {
            ChildTransactionImpl.BuilderImpl childBuilder = new BuilderImpl(chainId, version, senderPublicKey, amount, fee, deadline, appendages);
            ChainTransactionId referencedTransaction = ChainTransactionId.parse((JSONObject)transactionData.get("referencedTransaction"));
            childBuilder.referencedTransaction(referencedTransaction);
            return childBuilder;
        } catch (RuntimeException e) {
            Logger.logDebugMessage("Failed to parse transaction: " + JSON.toJSONString(transactionData));
            throw e;
        }
    }

}
