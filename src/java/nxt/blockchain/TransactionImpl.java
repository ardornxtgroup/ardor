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
import nxt.account.AccountRestrictions;
import nxt.crypto.Crypto;
import nxt.messaging.PrunableEncryptedMessageAppendix;
import nxt.messaging.PrunablePlainMessageAppendix;
import nxt.util.Convert;
import nxt.util.Filter;
import nxt.util.JSON;
import nxt.util.Logger;
import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class TransactionImpl implements Transaction {

    public static abstract class BuilderImpl implements Builder {

        private final short deadline;
        final byte[] senderPublicKey;
        private final long amount;
        private final TransactionType type;
        private final byte version;
        final long fee;
        final int chainId;

        private List<Appendix.AbstractAppendix> appendageList;
        private SortedMap<Integer, Appendix.AbstractAppendix> appendageMap;
        private int appendagesSize;

        private long recipientId;
        byte[] signature;
        private long blockId;
        private int height = Integer.MAX_VALUE;
        private long id;
        long senderId;
        private int timestamp = Integer.MAX_VALUE;
        private int blockTimestamp = -1;
        private byte[] fullHash;
        private boolean ecBlockSet = false;
        private int ecBlockHeight;
        private long ecBlockId;
        private short index = -1;

        private BuilderImpl(int chainId, byte version, byte[] senderPublicKey, long amount, long fee, short deadline, TransactionType type) {
            this.version = version;
            this.deadline = deadline;
            this.senderPublicKey = senderPublicKey;
            this.amount = amount;
            this.fee = fee;
            this.chainId = chainId;
            this.type = type;
        }

        BuilderImpl(int chainId, byte version, byte[] senderPublicKey, long amount, long fee, short deadline,
                    Attachment.AbstractAttachment attachment) {
            this(chainId, version, senderPublicKey, amount, fee, deadline, attachment.getTransactionType());
            this.appendageMap = new TreeMap<>();
            this.appendageMap.put(attachment.getAppendixType(), attachment);
        }

        BuilderImpl(int chainId, byte version, byte[] senderPublicKey, long amount, long fee, short deadline,
                    List<Appendix.AbstractAppendix> appendages) {
            this(chainId, version, senderPublicKey, amount, fee, deadline, ((Attachment)appendages.get(0)).getTransactionType());
            this.appendageList = appendages;
        }

        final void preBuild(String secretPhrase) {
            if (appendageMap != null) {
                appendageList = new ArrayList<>(appendageMap.values());
            }
            if (timestamp == Integer.MAX_VALUE) {
                timestamp = Nxt.getEpochTime();
            }
            if (!ecBlockSet) {
                Block ecBlock = BlockchainImpl.getInstance().getECBlock(timestamp);
                this.ecBlockHeight = ecBlock.getHeight();
                this.ecBlockId = ecBlock.getId();
            }
            int appendagesSize = 0;
            for (Appendix appendage : appendageList) {
                if (secretPhrase != null && appendage instanceof Appendix.Encryptable) {
                    ((Appendix.Encryptable) appendage).encrypt(secretPhrase);
                }
                appendagesSize += appendage.getSize();
            }
            this.appendagesSize = appendagesSize;
        }

        @Override
        public abstract TransactionImpl build() throws NxtException.NotValidException;

        @Override
        public abstract TransactionImpl build(String secretPhrase) throws NxtException.NotValidException;

        @Override
        public final BuilderImpl recipientId(long recipientId) {
            this.recipientId = recipientId;
            return this;
        }

        @Override
        public final BuilderImpl appendix(Appendix appendix) {
            if (appendix != null) {
                if (this.appendageMap == null) {
                    this.appendageMap = new TreeMap<>();
                    this.appendageList.forEach(abstractAppendix -> this.appendageMap.put(abstractAppendix.getAppendixType(), abstractAppendix));
                    this.appendageList = null;
                }
                this.appendageMap.put(appendix.getAppendixType(), (Appendix.AbstractAppendix)appendix);
            }
            return this;
        }

        @Override
        public final BuilderImpl timestamp(int timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        @Override
        public final BuilderImpl ecBlockHeight(int height) {
            this.ecBlockHeight = height;
            this.ecBlockSet = true;
            return this;
        }

        @Override
        public final BuilderImpl ecBlockId(long blockId) {
            this.ecBlockId = blockId;
            this.ecBlockSet = true;
            return this;
        }

        final BuilderImpl id(long id) {
            this.id = id;
            return this;
        }

        final BuilderImpl signature(byte[] signature) {
            this.signature = signature;
            return this;
        }

        final BuilderImpl blockId(long blockId) {
            this.blockId = blockId;
            return this;
        }

        public final BuilderImpl height(int height) {
            this.height = height;
            return this;
        }

        final BuilderImpl senderId(long senderId) {
            this.senderId = senderId;
            return this;
        }

        final BuilderImpl fullHash(byte[] fullHash) {
            this.fullHash = fullHash;
            return this;
        }

        final BuilderImpl blockTimestamp(int blockTimestamp) {
            this.blockTimestamp = blockTimestamp;
            return this;
        }

        final BuilderImpl index(short index) {
            this.index = index;
            return this;
        }

        final TransactionType getTransactionType() {
            return type;
        }

        final BuilderImpl prunableAttachments(JSONObject prunableAttachments) throws NxtException.NotValidException {
            if (prunableAttachments != null) {
                for (Appendix.Parser parser : AppendixParsers.getPrunableParsers()) {
                    appendix(parser.parse(prunableAttachments));
                }
            }
            return this;
        }

    }

    private final short deadline;
    private volatile byte[] senderPublicKey;
    private final long recipientId;
    private final long amount;
    private final TransactionType type;
    private final int ecBlockHeight;
    private final long ecBlockId;
    private final byte version;
    private final int timestamp;
    final Attachment.AbstractAttachment attachment;
    private final List<Appendix.AbstractAppendix> appendages;
    private final int appendagesSize;

    private final PrunablePlainMessageAppendix prunablePlainMessage;
    private final PrunableEncryptedMessageAppendix prunableEncryptedMessage;

    private volatile int height;
    private volatile long blockId;
    private volatile BlockImpl block;
    private volatile int blockTimestamp;
    private volatile short index;
    private volatile long id;
    private volatile String stringId;
    private volatile long senderId;
    private volatile byte[] fullHash;
    volatile byte[] bytes = null;
    volatile byte[] prunableBytes = null;


    TransactionImpl(BuilderImpl builder) {
        this.timestamp = builder.timestamp;
        this.deadline = builder.deadline;
        this.senderPublicKey = builder.senderPublicKey;
        this.recipientId = builder.recipientId;
        this.amount = builder.amount;
        this.type = builder.type;
        this.attachment = (Attachment.AbstractAttachment)builder.appendageList.get(0);
        this.appendages = Collections.unmodifiableList(builder.appendageList);
        this.appendagesSize = builder.appendagesSize;
        this.version = builder.version;
        this.blockId = builder.blockId;
        this.height = builder.height;
        this.index = builder.index;
        this.id = builder.id;
        this.senderId = builder.senderId;
        this.blockTimestamp = builder.blockTimestamp;
        this.fullHash = builder.fullHash;
        this.ecBlockHeight = builder.ecBlockHeight;
        this.ecBlockId = builder.ecBlockId;
        PrunablePlainMessageAppendix prunablePlainMessageAppendix = null;
        PrunableEncryptedMessageAppendix prunableEncryptedMessageAppendix = null;
        for (Appendix.AbstractAppendix appendix : this.appendages) {
            switch (appendix.getAppendixType()) {
                case PrunablePlainMessageAppendix.appendixType:
                    prunablePlainMessageAppendix = (PrunablePlainMessageAppendix) appendix;
                    break;
                case PrunableEncryptedMessageAppendix.appendixType:
                    prunableEncryptedMessageAppendix = (PrunableEncryptedMessageAppendix) appendix;
                    break;
            }
        }
        this.prunablePlainMessage = prunablePlainMessageAppendix;
        this.prunableEncryptedMessage = prunableEncryptedMessageAppendix;
    }

    @Override
    public short getDeadline() {
        return deadline;
    }

    @Override
    public byte[] getSenderPublicKey() {
        if (senderPublicKey == null) {
            senderPublicKey = Account.getPublicKey(senderId);
        }
        return senderPublicKey;
    }

    @Override
    public long getRecipientId() {
        return recipientId;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public int getHeight() {
        return height;
    }

    void setHeight(int height) {
        this.height = height;
    }

    @Override
    public TransactionType getType() {
        return type;
    }

    @Override
    public byte getVersion() {
        return version;
    }

    @Override
    public long getBlockId() {
        return blockId;
    }

    @Override
    public BlockImpl getBlock() {
        if (block == null && blockId != 0) {
            block = BlockchainImpl.getInstance().getBlock(blockId);
        }
        return block;
    }

    void setBlock(BlockImpl block) {
        this.block = block;
        this.blockId = block.getId();
        this.height = block.getHeight();
        this.blockTimestamp = block.getTimestamp();
    }

    void unsetBlock() {
        this.block = null;
        this.blockId = 0;
        this.blockTimestamp = -1;
        // must keep the height set, as transactions already having been included in a popped-off block before
        // get priority when sorted for inclusion in a new block
    }

    @Override
    public short getIndex() {
        if (index == -1) {
            throw new IllegalStateException("Transaction index has not been set");
        }
        return index;
    }

    void setIndex(int index) {
        this.index = (short) index;
    }

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public int getBlockTimestamp() {
        return blockTimestamp;
    }

    @Override
    public int getExpiration() {
        return timestamp + deadline * 60;
    }

    @Override
    public PrunablePlainMessageAppendix getPrunablePlainMessage() {
        if (prunablePlainMessage != null) {
            prunablePlainMessage.loadPrunable(this);
        }
        return prunablePlainMessage;
    }

    boolean hasPrunablePlainMessage() {
        return prunablePlainMessage != null;
    }

    @Override
    public PrunableEncryptedMessageAppendix getPrunableEncryptedMessage() {
        if (prunableEncryptedMessage != null) {
            prunableEncryptedMessage.loadPrunable(this);
        }
        return prunableEncryptedMessage;
    }

    boolean hasPrunableEncryptedMessage() {
        return prunableEncryptedMessage != null;
    }

    @Override
    public Attachment.AbstractAttachment getAttachment() {
        attachment.loadPrunable(this);
        return attachment;
    }

    @Override
    public List<Appendix.AbstractAppendix> getAppendages() {
        return getAppendages(false);
    }

    @Override
    public List<Appendix.AbstractAppendix> getAppendages(boolean includeExpiredPrunable) {
        for (Appendix.AbstractAppendix appendage : appendages) {
            appendage.loadPrunable(this, includeExpiredPrunable);
        }
        return appendages;
    }

    @Override
    public List<Appendix> getAppendages(Filter<Appendix> filter, boolean includeExpiredPrunable) {
        List<Appendix> result = new ArrayList<>();
        appendages.forEach(appendix -> {
            if (filter.ok(appendix)) {
                appendix.loadPrunable(this, includeExpiredPrunable);
                result.add(appendix);
            }
        });
        return result;
    }

    List<Appendix.AbstractAppendix> appendages() {
        return appendages;
    }

    @Override
    public final long getId() {
        if (id == 0) {
            if (getSignature() == null) {
                throw new IllegalStateException("Transaction is not signed yet");
            }
            byte[] data = zeroSignature(getBytes());
            byte[] signatureHash = Crypto.sha256().digest(getSignature());
            MessageDigest digest = Crypto.sha256();
            digest.update(data);
            fullHash = digest.digest(signatureHash);
            BigInteger bigInteger = new BigInteger(1, new byte[]{fullHash[7], fullHash[6], fullHash[5], fullHash[4], fullHash[3], fullHash[2], fullHash[1], fullHash[0]});
            id = bigInteger.longValue();
            stringId = getChain().getId() + ":" + Convert.toHexString(getFullHash());
        }
        return id;
    }

    @Override
    public final String getStringId() {
        if (stringId == null) {
            getId();
            if (stringId == null) {
                stringId = getChain().getId() + ":" + Convert.toHexString(getFullHash());
            }
        }
        return stringId;
    }

    @Override
    public final byte[] getFullHash() {
        if (fullHash == null) {
            getId();
        }
        return fullHash;
    }

    @Override
    public final long getSenderId() {
        if (senderId == 0) {
            senderId = Account.getId(getSenderPublicKey());
        }
        return senderId;
    }

    public final byte[] getBytes() {
        return Arrays.copyOf(bytes(), bytes.length);
    }

    public final byte[] getPrunableBytes() {
        return Arrays.copyOf(prunableBytes(), prunableBytes.length);
    }

    public final byte[] bytes() {
        if (bytes == null) {
            try {
                bytes = generateBytes(false).array();
            } catch (RuntimeException e) {
                if (getSignature() != null) {
                    Logger.logDebugMessage("Failed to get transaction bytes for transaction: " + JSON.toJSONString(getJSONObject()));
                }
                throw e;
            }
        }
        return bytes;
    }

    public final byte[] prunableBytes() {
        if (prunableBytes == null) {
            try {
                prunableBytes = generateBytes(true).array();
            } catch (RuntimeException e) {
                if (getSignature() != null) {
                    Logger.logDebugMessage("Failed to get transaction bytes for transaction: " + JSON.toJSONString(getJSONObject()));
                }
                throw e;
            }
        }
        return prunableBytes;
    }


    ByteBuffer generateBytes(boolean includePrunable) {
        ByteBuffer buffer = ByteBuffer.allocate(includePrunable ? getFullSize() : getSize());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(getChain().getId());
        buffer.put(getType().getType());
        buffer.put(getType().getSubtype());
        buffer.put(getVersion());
        buffer.putInt(getTimestamp());
        buffer.putShort(getDeadline());
        buffer.put(getSenderPublicKey());
        buffer.putLong(getRecipientId());
        buffer.putLong(getAmount());
        buffer.putLong(getFee());
        buffer.put(getSignature() != null ? getSignature() : new byte[64]);
        buffer.putInt(getECBlockHeight());
        buffer.putLong(getECBlockId());
        putAppendages(buffer, includePrunable);
        return buffer;
    }

    public final byte[] getUnsignedBytes() {
        return zeroSignature(getBytes());
    }

    @Override
    public JSONObject getJSONObject() {
        JSONObject json = new JSONObject();
        json.put("chain", getChain().getId());
        json.put("type", type.getType());
        json.put("subtype", type.getSubtype());
        json.put("timestamp", timestamp);
        json.put("deadline", deadline);
        json.put("senderPublicKey", Convert.toHexString(getSenderPublicKey()));
        if (type.canHaveRecipient()) {
            json.put("recipient", Long.toUnsignedString(recipientId));
        }
        json.put("amountNQT", amount);
        json.put("feeNQT", getFee());
        json.put("ecBlockHeight", ecBlockHeight);
        json.put("ecBlockId", Long.toUnsignedString(ecBlockId));
        json.put("signature", Convert.toHexString(getSignature()));
        if (getSignature() != null) {
            json.put("fullHash", Convert.toHexString(getFullHash()));
        }
        JSONObject attachmentJSON = new JSONObject();
        for (Appendix.AbstractAppendix appendage : appendages) {
            appendage.loadPrunable(this);
            attachmentJSON.putAll(appendage.getJSONObject());
        }
        if (!attachmentJSON.isEmpty()) {
            json.put("attachment", attachmentJSON);
        }
        json.put("version", version);
        return json;
    }

    @Override
    public JSONObject getPrunableAttachmentJSON() {
        JSONObject prunableJSON = null;
        for (Appendix.AbstractAppendix appendage : appendages) {
            if (appendage instanceof Appendix.Prunable) {
                appendage.loadPrunable(this);
                if (prunableJSON == null) {
                    prunableJSON = appendage.getJSONObject();
                } else {
                    prunableJSON.putAll(appendage.getJSONObject());
                }
            }
        }
        return prunableJSON;
    }

    @Override
    public int getECBlockHeight() {
        return ecBlockHeight;
    }

    @Override
    public long getECBlockId() {
        return ecBlockId;
    }

    public boolean verifySignature() {
        return checkSignature() && Account.setOrVerify(getSenderId(), getSenderPublicKey());
    }

    private volatile boolean hasValidSignature = false;

    private boolean checkSignature() {
        if (!hasValidSignature) {
            byte[] bytes = getBytes();
            hasValidSignature = getSignature() != null && Crypto.verify(getSignature(), zeroSignature(bytes), getSenderPublicKey());
            if (!hasValidSignature) {
                Logger.logWarningMessage("Invalid signature for transaction bytes " + Convert.toHexString(bytes));
            }
        }
        return hasValidSignature;
    }

    private static final int SIGNATURE_OFFSET = 4 + 1 + 1 + 1 + 4 + 2 + 32 + 8 + 8 + 8;

    int getSize() {
        return SIGNATURE_OFFSET + 64 + 4 + 8 + 4 + appendagesSize;
    }

    @Override
    public final int getFullSize() {
        int fullSize = getSize() - appendagesSize;
        for (Appendix.AbstractAppendix appendage : getAppendages()) {
            fullSize += appendage.getFullSize();
        }
        return fullSize;
    }

    private byte[] zeroSignature(byte[] data) {
        for (int i = SIGNATURE_OFFSET; i < SIGNATURE_OFFSET + 64; i++) {
            data[i] = 0;
        }
        return data;
    }

    @Override
    public void validate() throws NxtException.ValidationException {
        if (timestamp <= 0 || deadline < 1 || getFee() < 0
                || getFee() > Constants.MAX_BALANCE_NQT
                || amount < 0
                || amount > Constants.MAX_BALANCE_NQT
                || type == null) {
            throw new NxtException.NotValidException("Invalid transaction parameters:\n type: " + type + ", timestamp: " + timestamp
                    + ", deadline: " + deadline + ", fee: " + getFee() + ", amount: " + amount);
        }
        if (attachment == null || type != attachment.getTransactionType()) {
            throw new NxtException.NotValidException("Invalid attachment " + attachment + " for transaction of type " + type);
        }

        if (!type.canHaveRecipient()) {
            if (recipientId != 0 || getAmount() != 0) {
                throw new NxtException.NotValidException("Transactions of this type must have recipient == 0, amount == 0");
            }
        }

        if (type.mustHaveRecipient()) {
            if (recipientId == 0) {
                throw new NxtException.NotValidException("Transactions of this type must have a valid recipient");
            }
        }

    }

    void validateId() throws NxtException.ValidationException {
        if (getId() == 0L) {
            throw new NxtException.NotValidException("Invalid transaction id 0");
        }
    }

    final void validateEcBlock() throws NxtException.ValidationException {
        if (ecBlockId != 0) {
            if (Nxt.getBlockchain().getHeight() < ecBlockHeight) {
                throw new NxtException.NotCurrentlyValidException("ecBlockHeight " + ecBlockHeight
                        + " exceeds blockchain height " + Nxt.getBlockchain().getHeight());
            }
            if (BlockDb.findBlockIdAtHeight(ecBlockHeight) != ecBlockId) {
                throw new NxtException.NotCurrentlyValidException("ecBlockHeight " + ecBlockHeight
                        + " does not match ecBlockId " + Long.toUnsignedString(ecBlockId)
                        + ", transaction was generated on a fork");
            }
        } else {
            throw new NxtException.NotValidException("To prevent transaction replay attacks, using ecBlockId=0 is no longer allowed.");
        }
    }

    @Override
    public final long getMinimumFeeFQT() {
        if (blockId != 0) {
            return getMinimumFeeFQT(height - 1);
        }
        return getMinimumFeeFQT(Nxt.getBlockchain().getHeight());
    }

    long getMinimumFeeFQT(int blockchainHeight) {
        long totalFee = 0;
        for (Appendix.AbstractAppendix appendage : appendages) {
            appendage.loadPrunable(this);
            if (blockchainHeight < appendage.getBaselineFeeHeight()) {
                return 0; // No need to validate fees before baseline block
            }
            Fee fee = appendage.getFee(this, blockchainHeight);
            totalFee = Math.addExact(totalFee, fee.getFee(this, appendage));
        }
        if (recipientId != 0
                && ! (Nxt.getBlockchainProcessor().isScanning() && blockchainHeight < Nxt.getBlockchainProcessor().getInitialScanHeight() - Constants.MAX_ROLLBACK)
                && ! Account.hasAccount(recipientId, blockchainHeight)) {
            totalFee += Fee.NEW_ACCOUNT_FEE;
        }
        return totalFee;
    }

    abstract boolean hasAllReferencedTransactions(int timestamp, int count);

    public abstract boolean attachmentIsPhased();

    public final boolean attachmentIsDuplicate(Map<TransactionType, Map<String, Integer>> duplicates, boolean atAcceptanceHeight) {
        if (!attachmentIsPhased() && !atAcceptanceHeight) {
            // can happen for phased transactions having non-phasable attachment
            return false;
        }
        if (atAcceptanceHeight) {
            if (AccountRestrictions.isBlockDuplicate(this, duplicates)) {
                return true;
            }
            // all are checked at acceptance height for block duplicates
            if (getType().isBlockDuplicate(this, duplicates)) {
                return true;
            }
            // phased are not further checked at acceptance height
            if (attachmentIsPhased()) {
                return false;
            }
        }
        // non-phased at acceptance height, and phased at execution height
        return getType().isDuplicate(this, duplicates);
    }

    final boolean isUnconfirmedDuplicate(Map<TransactionType, Map<String, Integer>> duplicates) {
        return getType().isUnconfirmedDuplicate(this, duplicates);
    }

    // returns false iff double spending
    final boolean applyUnconfirmed() {
        Account senderAccount = Account.getAccount(getSenderId());
        return senderAccount != null && getType().applyUnconfirmed(this, senderAccount);
    }

    final void undoUnconfirmed() {
        Account senderAccount = Account.getAccount(getSenderId());
        getType().undoUnconfirmed(this, senderAccount);
    }

    abstract void apply();

    abstract void save(Connection con, String schemaTable) throws SQLException;

    abstract UnconfirmedTransaction newUnconfirmedTransaction(long arrivalTime, boolean isBundled);

    public static TransactionImpl parseTransaction(byte[] transactionBytes) throws NxtException.NotValidException {
        TransactionImpl transaction = newTransactionBuilder(transactionBytes).build();
        if (transaction.getSignature() != null && !transaction.checkSignature()) {
            throw new NxtException.NotValidException("Invalid transaction signature for transaction " + JSON.toJSONString(transaction.getJSONObject()));
        }
        return transaction;
    }

    public static TransactionImpl parseTransaction(byte[] transactionBytes, JSONObject prunableAttachments) throws NxtException.NotValidException {
        TransactionImpl transaction = newTransactionBuilder(transactionBytes, prunableAttachments).build();
        if (transaction.getSignature() != null && !transaction.checkSignature()) {
            throw new NxtException.NotValidException("Invalid transaction signature for transaction " + JSON.toJSONString(transaction.getJSONObject()));
        }
        return transaction;
    }

    static TransactionImpl loadTransaction(Chain chain, ResultSet rs) throws NxtException.NotValidException {
        try {
            byte type = rs.getByte("type");
            byte subtype = rs.getByte("subtype");
            int timestamp = rs.getInt("timestamp");
            short deadline = rs.getShort("deadline");
            long amount = rs.getLong("amount");
            long fee = rs.getLong("fee");
            int ecBlockHeight = rs.getInt("ec_block_height");
            long ecBlockId = rs.getLong("ec_block_id");
            byte[] signature = rs.getBytes("signature");
            long blockId = rs.getLong("block_id");
            int height = rs.getInt("height");
            long id = rs.getLong("id");
            long senderId = rs.getLong("sender_id");
            byte[] attachmentBytes = rs.getBytes("attachment_bytes");
            int blockTimestamp = rs.getInt("block_timestamp");
            byte[] fullHash = rs.getBytes("full_hash");
            byte version = rs.getByte("version");
            short transactionIndex = rs.getShort("transaction_index");

            ByteBuffer buffer = null;
            if (attachmentBytes != null) {
                buffer = ByteBuffer.wrap(attachmentBytes);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            }
            TransactionType transactionType = TransactionType.findTransactionType(type, subtype);
            List<Appendix.AbstractAppendix> appendages = getAppendages(transactionType, buffer);
            TransactionImpl.BuilderImpl builder = chain.newTransactionBuilder(version, amount, fee, deadline, appendages, rs);
            builder.timestamp(timestamp)
                    .signature(signature)
                    .blockId(blockId)
                    .height(height)
                    .id(id)
                    .senderId(senderId)
                    .blockTimestamp(blockTimestamp)
                    .fullHash(fullHash)
                    .ecBlockHeight(ecBlockHeight)
                    .ecBlockId(ecBlockId)
                    .index(transactionIndex);
            if (transactionType.canHaveRecipient()) {
                long recipientId = rs.getLong("recipient_id");
                if (! rs.wasNull()) {
                    builder.recipientId(recipientId);
                }
            }
            return builder.build();
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static TransactionImpl.BuilderImpl newTransactionBuilder(byte[] bytes) throws NxtException.NotValidException {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            int chainId = buffer.getInt();
            byte type = buffer.get();
            byte subtype = buffer.get();
            byte version = buffer.get();
            int timestamp = buffer.getInt();
            short deadline = buffer.getShort();
            byte[] senderPublicKey = new byte[32];
            buffer.get(senderPublicKey);
            long recipientId = buffer.getLong();
            long amount = buffer.getLong();
            long fee = buffer.getLong();
            byte[] signature = new byte[64];
            buffer.get(signature);
            signature = Convert.emptyToNull(signature);
            int ecBlockHeight = buffer.getInt();
            long ecBlockId = buffer.getLong();
            TransactionType transactionType = TransactionType.findTransactionType(type, subtype);
            if (transactionType == null) {
                throw new NxtException.NotValidException("Invalid transaction type: " + type + ", " + subtype);
            }
            List<Appendix.AbstractAppendix> appendages = getAppendages(transactionType, buffer);
            TransactionImpl.BuilderImpl builder = Chain.getChain(chainId).newTransactionBuilder(version, senderPublicKey, amount, fee, deadline,
                        appendages, buffer);
            builder.timestamp(timestamp)
                    .signature(signature)
                    .ecBlockHeight(ecBlockHeight)
                    .ecBlockId(ecBlockId);
            if (transactionType.canHaveRecipient()) {
                builder.recipientId(recipientId);
            }
            if (buffer.hasRemaining()) {
                throw new NxtException.NotValidException("Transaction bytes too long, " + buffer.remaining() + " extra bytes");
            }
            return builder;
        } catch (NxtException.NotValidException|RuntimeException e) {
            Logger.logDebugMessage("Failed to parse transaction bytes: " + Convert.toHexString(bytes));
            throw e;
        }
    }

    public static TransactionImpl.BuilderImpl newTransactionBuilder(byte[] bytes, JSONObject prunableAttachments) throws NxtException.NotValidException {
        TransactionImpl.BuilderImpl builder = newTransactionBuilder(bytes);
        builder.prunableAttachments(prunableAttachments);
        return builder;
    }

    public static TransactionImpl.BuilderImpl newTransactionBuilder(JSONObject transactionData) throws NxtException.NotValidException {
        try {
            int chainId = ((Long) transactionData.get("chain")).intValue();
            byte type = ((Long) transactionData.get("type")).byteValue();
            byte subtype = ((Long) transactionData.get("subtype")).byteValue();
            int timestamp = ((Long) transactionData.get("timestamp")).intValue();
            short deadline = ((Long) transactionData.get("deadline")).shortValue();
            byte[] senderPublicKey = Convert.parseHexString((String) transactionData.get("senderPublicKey"));
            long amount = Convert.parseLong(transactionData.get("amountNQT"));
            long fee = Convert.parseLong(transactionData.get("feeNQT"));
            byte[] signature = Convert.parseHexString((String) transactionData.get("signature"));
            byte version = ((Long) transactionData.get("version")).byteValue();
            JSONObject attachmentData = (JSONObject) transactionData.get("attachment");
            int ecBlockHeight = ((Long) transactionData.get("ecBlockHeight")).intValue();
            long ecBlockId = Convert.parseUnsignedLong((String) transactionData.get("ecBlockId"));

            TransactionType transactionType = TransactionType.findTransactionType(type, subtype);
            if (transactionType == null) {
                throw new NxtException.NotValidException("Invalid transaction type: " + type + ", " + subtype);
            }
            List<Appendix.AbstractAppendix> appendages = new ArrayList<>();
            appendages.add(transactionType.parseAttachment(attachmentData));
            if (attachmentData != null) {
                for (Appendix.Parser parser : AppendixParsers.getParsers()) {
                    Appendix.AbstractAppendix appendix = parser.parse(attachmentData);
                    if (appendix != null) {
                        appendages.add(appendix);
                    }
                }
            }
            TransactionImpl.BuilderImpl builder = Chain.getChain(chainId).newTransactionBuilder(version, senderPublicKey, amount, fee, deadline,
                    appendages, transactionData);
            builder.timestamp(timestamp)
                    .signature(signature)
                    .ecBlockHeight(ecBlockHeight)
                    .ecBlockId(ecBlockId);
            if (transactionType.canHaveRecipient()) {
                long recipientId = Convert.parseUnsignedLong((String) transactionData.get("recipient"));
                builder.recipientId(recipientId);
            }
            return builder;
        } catch (NxtException.NotValidException|RuntimeException e) {
            Logger.logDebugMessage("Failed to parse transaction: " + JSON.toJSONString(transactionData));
            throw e;
        }
    }

    static List<Appendix.AbstractAppendix> getAppendages(TransactionType transactionType, ByteBuffer buffer) throws NxtException.NotValidException {
        if (buffer == null) {
            return Collections.singletonList(transactionType.parseAttachment(buffer));
        }
        int flags = buffer.getInt();
        Appendix.AbstractAppendix attachment = transactionType.parseAttachment(buffer);
        if (flags == 0) {
            return Collections.singletonList(attachment);
        }
        List<Appendix.AbstractAppendix> list = new ArrayList<>();
        list.add(attachment);
        Collection<Appendix.Parser> parsers = AppendixParsers.getParsers();
        int position = 1;
        for (Appendix.Parser parser : parsers) {
            if ((flags & position) != 0) {
                list.add(parser.parse(buffer));
            }
            position <<= 1;
        }
        return list;
    }

    void putAppendages(ByteBuffer buffer, boolean includePrunable) {
        int flags = 0;
        for (Appendix.AbstractAppendix appendage : appendages()) {
            flags |= appendage.getAppendixType();
        }
        buffer.putInt(flags);
        for (Appendix.AbstractAppendix appendage : appendages()) {
            if (includePrunable) {
                appendage.loadPrunable(this);
                appendage.putPrunableBytes(buffer);
            } else {
                appendage.putBytes(buffer);
            }
        }
    }

}
