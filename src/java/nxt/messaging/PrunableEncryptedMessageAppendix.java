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

package nxt.messaging;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.blockchain.Appendix;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.Fee;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionImpl;
import nxt.crypto.Crypto;
import nxt.crypto.EncryptedData;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public class PrunableEncryptedMessageAppendix extends Appendix.AbstractAppendix implements Appendix.Prunable {

    public static final int appendixType = 16;
    public static final String appendixName = "PrunableEncryptedMessage";

    public static final Parser appendixParser = new Parser() {
        @Override
        public AbstractAppendix parse(ByteBuffer buffer) throws NxtException.NotValidException {
            return new PrunableEncryptedMessageAppendix(buffer);
        }
        @Override
        public AbstractAppendix parse(JSONObject attachmentData) throws NxtException.NotValidException {
            if (!Appendix.hasAppendix(appendixName, attachmentData)) {
                return null;
            }
            JSONObject encryptedMessageJSON = (JSONObject)attachmentData.get("encryptedMessage");
            if (encryptedMessageJSON != null && encryptedMessageJSON.get("data") == null) {
                return new UnencryptedPrunableEncryptedMessageAppendix(attachmentData);
            }
            return new PrunableEncryptedMessageAppendix(attachmentData);
        }
    };

    private static final Fee PRUNABLE_ENCRYPTED_DATA_FEE = new Fee.SizeBasedFee(Constants.ONE_FXT / 100) {
        @Override
        public int getSize(TransactionImpl transaction, Appendix appendix) {
            return appendix.getFullSize();
        }
    };

    private final byte[] hash;
    private EncryptedData encryptedData;
    private final boolean isText;
    private final boolean isCompressed;
    private volatile PrunableMessageHome.PrunableMessage prunableMessage;

    private PrunableEncryptedMessageAppendix(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        byte flags = buffer.get();
        if ((flags & 1) != 0) {
            this.isText = (flags & 2) != 0;
            this.isCompressed = (flags & 4) != 0;
            int length = buffer.getInt();
            this.encryptedData = EncryptedData.readEncryptedData(buffer, length, Constants.MAX_PRUNABLE_ENCRYPTED_MESSAGE_LENGTH);
            this.hash = null;
        } else {
            this.hash = new byte[32];
            buffer.get(this.hash);
            this.encryptedData = null;
            this.isText = false;
            this.isCompressed = false;
        }
    }

    PrunableEncryptedMessageAppendix(JSONObject attachmentJSON) {
        super(attachmentJSON);
        String hashString = Convert.emptyToNull((String) attachmentJSON.get("encryptedMessageHash"));
        JSONObject encryptedMessageJSON = (JSONObject) attachmentJSON.get("encryptedMessage");
        if (hashString != null && encryptedMessageJSON == null) {
            this.hash = Convert.parseHexString(hashString);
            this.encryptedData = null;
            this.isText = false;
            this.isCompressed = false;
        } else {
            this.hash = null;
            byte[] data = Convert.parseHexString((String) encryptedMessageJSON.get("data"));
            byte[] nonce = Convert.parseHexString((String) encryptedMessageJSON.get("nonce"));
            this.encryptedData = new EncryptedData(data, nonce);
            this.isText = Boolean.TRUE.equals(encryptedMessageJSON.get("isText"));
            this.isCompressed = Boolean.TRUE.equals(encryptedMessageJSON.get("isCompressed"));
        }
    }

    public PrunableEncryptedMessageAppendix(EncryptedData encryptedData, boolean isText, boolean isCompressed) {
        this.encryptedData = encryptedData;
        this.isText = isText;
        this.isCompressed = isCompressed;
        this.hash = null;
    }

    @Override
    public final Fee getBaselineFee(Transaction transaction) {
        return PRUNABLE_ENCRYPTED_DATA_FEE;
    }

    @Override
    protected final int getMySize() {
        return 1 + 32;
    }

    @Override
    public final int getMyFullSize() {
        if (!hasPrunableData()) {
            throw new IllegalStateException("Prunable data not available");
        }
        return 1 + 4 + getEncryptedData().getSize();
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.put((byte)0);
        buffer.put(getHash());
    }

    @Override
    public void putMyPrunableBytes(ByteBuffer buffer) {
        if (!hasPrunableData()) {
            throw new IllegalStateException("Prunable data not available");
        }
        byte flags = 1;
        if (isText()) {
            flags |= 2;
        }
        if (isCompressed()) {
            flags |= 4;
        }
        buffer.put(flags);
        buffer.putInt(getEncryptedDataLength());
        buffer.put(getEncryptedData().getData());
        buffer.put(getEncryptedData().getNonce());
    }

    @Override
    protected void putMyJSON(JSONObject json) {
        if (prunableMessage != null) {
            JSONObject encryptedMessageJSON = new JSONObject();
            json.put("encryptedMessage", encryptedMessageJSON);
            encryptedMessageJSON.put("data", Convert.toHexString(prunableMessage.getEncryptedData().getData()));
            encryptedMessageJSON.put("nonce", Convert.toHexString(prunableMessage.getEncryptedData().getNonce()));
            encryptedMessageJSON.put("isText", prunableMessage.encryptedMessageIsText());
            encryptedMessageJSON.put("isCompressed", prunableMessage.isCompressed());
        } else if (encryptedData != null) {
            JSONObject encryptedMessageJSON = new JSONObject();
            json.put("encryptedMessage", encryptedMessageJSON);
            encryptedMessageJSON.put("data", Convert.toHexString(encryptedData.getData()));
            encryptedMessageJSON.put("nonce", Convert.toHexString(encryptedData.getNonce()));
            encryptedMessageJSON.put("isText", isText);
            encryptedMessageJSON.put("isCompressed", isCompressed);
        }
        json.put("encryptedMessageHash", Convert.toHexString(getHash()));
    }

    @Override
    public int getAppendixType() {
        return appendixType;
    }

    @Override
    public final String getAppendixName() {
        return appendixName;
    }

    @Override
    public void validate(Transaction transaction) throws NxtException.ValidationException {
        if (transaction instanceof ChildTransaction && ((ChildTransaction)transaction).getEncryptedMessage() != null) {
            throw new NxtException.NotValidException("Cannot have both encrypted and prunable encrypted message attachments");
        }
        EncryptedData ed = getEncryptedData();
        if (ed == null && Nxt.getEpochTime() - transaction.getTimestamp() < Constants.MIN_PRUNABLE_LIFETIME) {
            throw new NxtException.NotCurrentlyValidException("Encrypted message has been pruned prematurely");
        }
        if (ed != null) {
            if (ed.getData().length > Constants.MAX_PRUNABLE_ENCRYPTED_MESSAGE_LENGTH) {
                throw new NxtException.NotValidException(String.format("Message length %d exceeds max prunable encrypted message length %d",
                        ed.getData().length, Constants.MAX_PRUNABLE_ENCRYPTED_MESSAGE_LENGTH));
            }
            if ((ed.getNonce().length != 32 && ed.getData().length > 0)
                    || (ed.getNonce().length != 0 && ed.getData().length == 0)) {
                throw new NxtException.NotValidException("Invalid nonce length " + ed.getNonce().length);
            }
        }
        if (transaction.getRecipientId() == 0) {
            throw new NxtException.NotValidException("Encrypted messages cannot be attached to transactions with no recipient");
        }
    }

    @Override
    public void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
        if (Nxt.getEpochTime() - transaction.getTimestamp() < Constants.MAX_PRUNABLE_LIFETIME) {
            transaction.getChain().getPrunableMessageHome().add((TransactionImpl)transaction, this);
        }
    }

    public final EncryptedData getEncryptedData() {
        if (prunableMessage != null) {
            return prunableMessage.getEncryptedData();
        }
        return encryptedData;
    }

    final void setEncryptedData(EncryptedData encryptedData) {
        this.encryptedData = encryptedData;
    }

    int getEncryptedDataLength() {
        return getEncryptedData() == null ? 0 : getEncryptedData().getData().length;
    }

    public final boolean isText() {
        if (prunableMessage != null) {
            return prunableMessage.encryptedMessageIsText();
        }
        return isText;
    }

    public final boolean isCompressed() {
        if (prunableMessage != null) {
            return prunableMessage.isCompressed();
        }
        return isCompressed;
    }

    @Override
    public final byte[] getHash() {
        if (hash != null) {
            return hash;
        }
        MessageDigest digest = Crypto.sha256();
        digest.update((byte)(isText ? 1 : 0));
        digest.update((byte)(isCompressed ? 1 : 0));
        digest.update(encryptedData.getData());
        digest.update(encryptedData.getNonce());
        return digest.digest();
    }

    @Override
    public void loadPrunable(Transaction transaction, boolean includeExpiredPrunable) {
        if (!hasPrunableData() && shouldLoadPrunable(transaction, includeExpiredPrunable)) {
            PrunableMessageHome.PrunableMessage prunableMessage = transaction.getChain().getPrunableMessageHome()
                    .getPrunableMessage(transaction.getFullHash());
            if (prunableMessage != null && prunableMessage.getEncryptedData() != null) {
                this.prunableMessage = prunableMessage;
            }
        }
    }

    @Override
    public final boolean isPhasable() {
        return false;
    }

    @Override
    public boolean isAllowed(Chain chain) {
        return true;
    }

    @Override
    public final boolean hasPrunableData() {
        return (prunableMessage != null || encryptedData != null);
    }

    @Override
    public void restorePrunableData(Transaction transaction, int blockTimestamp, int height) {
        transaction.getChain().getPrunableMessageHome().add((TransactionImpl)transaction, this, blockTimestamp, height);
    }
}
