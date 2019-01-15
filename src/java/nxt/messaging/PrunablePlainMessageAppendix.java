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
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public class PrunablePlainMessageAppendix extends Appendix.AbstractAppendix implements Appendix.Prunable {

    public static final int appendixType = 8;
    public static final String appendixName = "PrunablePlainMessage";

    public static final Parser appendixParser = new Parser() {
        @Override
        public AbstractAppendix parse(ByteBuffer buffer) throws NxtException.NotValidException {
            return new PrunablePlainMessageAppendix(buffer);
        }
        @Override
        public AbstractAppendix parse(JSONObject attachmentData) {
            if (!Appendix.hasAppendix(appendixName, attachmentData)) {
                return null;
            }
            return new PrunablePlainMessageAppendix(attachmentData);
        }
    };

    private static final Fee PRUNABLE_MESSAGE_FEE = new Fee.SizeBasedFee(Constants.ONE_FXT / 100) {
        @Override
        public int getSize(TransactionImpl transaction, Appendix appendix) {
            return appendix.getFullSize();
        }
    };

    private final byte[] hash;
    private final byte[] message;
    private final boolean isText;
    private volatile PrunableMessageHome.PrunableMessage prunableMessage;

    private PrunablePlainMessageAppendix(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        byte flags = buffer.get();
        if ((flags & 1) != 0) {
            this.isText = (flags & 2) != 0;
            int messageLength = buffer.getInt();
            if (messageLength > Constants.MAX_PRUNABLE_MESSAGE_LENGTH) {
                throw new NxtException.NotValidException("Invalid prunable message length: " + messageLength);
            }
            this.message = new byte[messageLength];
            buffer.get(this.message);
            this.hash = null;
        } else {
            this.hash = new byte[32];
            buffer.get(this.hash);
            this.message = null;
            this.isText = false;
        }
    }

    private PrunablePlainMessageAppendix(JSONObject attachmentData) {
        super(attachmentData);
        String hashString = Convert.emptyToNull((String) attachmentData.get("messageHash"));
        String messageString = Convert.emptyToNull((String) attachmentData.get("message"));
        if (hashString != null && messageString == null) {
            this.hash = Convert.parseHexString(hashString);
            this.message = null;
            this.isText = false;
        } else {
            this.hash = null;
            this.isText = Boolean.TRUE.equals(attachmentData.get("messageIsText"));
            this.message = Convert.toBytes(messageString, isText);
        }
    }

    public PrunablePlainMessageAppendix(byte[] message) {
        this(message, false);
    }

    public PrunablePlainMessageAppendix(String string) {
        this(Convert.toBytes(string), true);
    }

    public PrunablePlainMessageAppendix(String string, boolean isText) {
        this(Convert.toBytes(string, isText), isText);
    }

    public PrunablePlainMessageAppendix(byte[] message, boolean isText) {
        this.message = message;
        this.isText = isText;
        this.hash = null;
    }

    @Override
    public int getAppendixType() {
        return appendixType;
    }

    @Override
    public String getAppendixName() {
        return appendixName;
    }

    @Override
    public Fee getBaselineFee(Transaction transaction) {
        return PRUNABLE_MESSAGE_FEE;
    }

    @Override
    protected int getMySize() {
        return 1 + 32;
    }

    @Override
    public int getMyFullSize() {
        if (!hasPrunableData()) {
            throw new IllegalStateException("Prunable data not available");
        }
        return 1 + 4 + getMessage().length;
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
        buffer.put(flags);
        buffer.putInt(getMessage().length);
        buffer.put(getMessage());
    }

    @Override
    protected void putMyJSON(JSONObject json) {
        if (prunableMessage != null) {
            json.put("message", Convert.toString(prunableMessage.getMessage(), prunableMessage.messageIsText()));
            json.put("messageIsText", prunableMessage.messageIsText());
        } else if (message != null) {
            json.put("message", Convert.toString(message, isText));
            json.put("messageIsText", isText);
        }
        json.put("messageHash", Convert.toHexString(getHash()));
    }

    @Override
    public void validate(Transaction transaction) throws NxtException.ValidationException {
        if (transaction instanceof ChildTransaction && ((ChildTransaction)transaction).getMessage() != null) {
            throw new NxtException.NotValidException("Cannot have both message and prunable message attachments");
        }
        byte[] msg = getMessage();
        if (msg != null && msg.length > Constants.MAX_PRUNABLE_MESSAGE_LENGTH) {
            throw new NxtException.NotValidException("Invalid prunable message length: " + msg.length);
        }
        if (msg == null && Nxt.getEpochTime() - transaction.getTimestamp() < Constants.MIN_PRUNABLE_LIFETIME) {
            throw new NxtException.NotCurrentlyValidException("Message has been pruned prematurely");
        }
    }

    @Override
    public void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
        if (Nxt.getEpochTime() - transaction.getTimestamp() < Constants.MAX_PRUNABLE_LIFETIME) {
            transaction.getChain().getPrunableMessageHome().add((TransactionImpl)transaction, this);
        }
    }

    public byte[] getMessage() {
        if (prunableMessage != null) {
            return prunableMessage.getMessage();
        }
        return message;
    }

    public boolean isText() {
        if (prunableMessage != null) {
            return prunableMessage.messageIsText();
        }
        return isText;
    }

    @Override
    public byte[] getHash() {
        if (hash != null) {
            return hash;
        }
        MessageDigest digest = Crypto.sha256();
        digest.update((byte)(isText ? 1 : 0));
        digest.update(message);
        return digest.digest();
    }

    @Override
    public final void loadPrunable(Transaction transaction, boolean includeExpiredPrunable) {
        if (!hasPrunableData() && shouldLoadPrunable(transaction, includeExpiredPrunable)) {
            PrunableMessageHome.PrunableMessage prunableMessage = transaction.getChain().getPrunableMessageHome()
                    .getPrunableMessage(transaction.getFullHash());
            if (prunableMessage != null && prunableMessage.getMessage() != null) {
                this.prunableMessage = prunableMessage;
            }
        }
    }

    @Override
    public boolean isPhasable() {
        return false;
    }

    @Override
    public boolean isAllowed(Chain chain) {
        return true;
    }

    @Override
    public final boolean hasPrunableData() {
        return (prunableMessage != null || message != null);
    }

    @Override
    public void restorePrunableData(Transaction transaction, int blockTimestamp, int height) {
        transaction.getChain().getPrunableMessageHome().add((TransactionImpl)transaction, this, blockTimestamp, height);
    }
}
