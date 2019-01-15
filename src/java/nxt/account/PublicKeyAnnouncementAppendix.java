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

package nxt.account;

import nxt.NxtException;
import nxt.blockchain.Appendix;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.crypto.Crypto;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class PublicKeyAnnouncementAppendix extends Appendix.AbstractAppendix {

    public static final int appendixType = 32;
    public static final String appendixName = "PublicKeyAnnouncement";

    public static final Parser appendixParser = new Parser() {
        @Override
        public AbstractAppendix parse(ByteBuffer buffer) {
            return new PublicKeyAnnouncementAppendix(buffer);
        }

        @Override
        public AbstractAppendix parse(JSONObject attachmentData) {
            if (!Appendix.hasAppendix(appendixName, attachmentData)) {
                return null;
            }
            return new PublicKeyAnnouncementAppendix(attachmentData);
        }
    };

    private final byte[] publicKey;

    private PublicKeyAnnouncementAppendix(ByteBuffer buffer) {
        super(buffer);
        this.publicKey = new byte[32];
        buffer.get(this.publicKey);
    }

    private PublicKeyAnnouncementAppendix(JSONObject attachmentData) {
        super(attachmentData);
        this.publicKey = Convert.parseHexString((String)attachmentData.get("recipientPublicKey"));
    }

    public PublicKeyAnnouncementAppendix(byte[] publicKey) {
        this.publicKey = publicKey;
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
    protected int getMySize() {
        return 32;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.put(publicKey);
    }

    @Override
    protected void putMyJSON(JSONObject json) {
        json.put("recipientPublicKey", Convert.toHexString(publicKey));
    }

    @Override
    public void validate(Transaction transaction) throws NxtException.ValidationException {
        if (transaction.getRecipientId() == 0) {
            throw new NxtException.NotValidException("PublicKeyAnnouncement cannot be attached to transactions with no recipient");
        }
        if (!Crypto.isCanonicalPublicKey(publicKey)) {
            throw new NxtException.NotValidException("Invalid recipient public key: " + Convert.toHexString(publicKey));
        }
        long recipientId = transaction.getRecipientId();
        if (Account.getId(this.publicKey) != recipientId) {
            throw new NxtException.NotValidException("Announced public key does not match recipient accountId");
        }
        byte[] recipientPublicKey = Account.getPublicKey(recipientId);
        if (recipientPublicKey != null && ! Arrays.equals(publicKey, recipientPublicKey)) {
            throw new NxtException.NotCurrentlyValidException("A different public key for this account has already been announced");
        }
    }

    @Override
    public void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
        if (Account.setOrVerify(recipientAccount.getId(), publicKey)) {
            recipientAccount.apply(this.publicKey);
        }
    }

    @Override
    public boolean isPhasable() {
        return false;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    @Override
    public boolean isAllowed(Chain chain) {
        return chain instanceof ChildChain;
    }

}
