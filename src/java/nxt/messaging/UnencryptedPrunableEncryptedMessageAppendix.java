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
import nxt.NxtException;
import nxt.account.Account;
import nxt.blockchain.Appendix;
import nxt.blockchain.Transaction;
import nxt.crypto.EncryptedData;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class UnencryptedPrunableEncryptedMessageAppendix extends PrunableEncryptedMessageAppendix implements Appendix.Encryptable {

    private final byte[] messageToEncrypt;
    private final byte[] recipientPublicKey;

    UnencryptedPrunableEncryptedMessageAppendix(JSONObject attachmentJSON) {
        super(attachmentJSON);
        setEncryptedData(null);
        JSONObject encryptedMessageJSON = (JSONObject)attachmentJSON.get("encryptedMessage");
        String messageToEncryptString = (String)encryptedMessageJSON.get("messageToEncrypt");
        this.messageToEncrypt = isText() ? Convert.toBytes(messageToEncryptString) : Convert.parseHexString(messageToEncryptString);
        this.recipientPublicKey = Convert.parseHexString((String)attachmentJSON.get("recipientPublicKey"));
    }

    public UnencryptedPrunableEncryptedMessageAppendix(byte[] messageToEncrypt, boolean isText, boolean isCompressed, byte[] recipientPublicKey) {
        super(null, isText, isCompressed);
        this.messageToEncrypt = messageToEncrypt;
        this.recipientPublicKey = recipientPublicKey;
    }

    @Override
    public int getMyFullSize() {
        if (getEncryptedData() != null) {
            return super.getMyFullSize();
        }
        if (!hasPrunableData()) {
            throw new IllegalStateException("Prunable data not available");
        }
        return 1 + 4 + EncryptedData.getEncryptedSize(getPlaintext());
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        if (getEncryptedData() == null) {
            throw new NxtException.NotYetEncryptedException("Prunable encrypted message not yet encrypted");
        }
        super.putMyBytes(buffer);
    }

    @Override
    public void putMyPrunableBytes(ByteBuffer buffer) {
        if (getEncryptedData() == null) {
            throw new NxtException.NotYetEncryptedException("Prunable encrypted message not yet encrypted");
        }
        super.putMyPrunableBytes(buffer);
    }

    @Override
    protected void putMyJSON(JSONObject json) {
        if (getEncryptedData() == null) {
            JSONObject encryptedMessageJSON = new JSONObject();
            encryptedMessageJSON.put("messageToEncrypt", isText() ? Convert.toString(messageToEncrypt) : Convert.toHexString(messageToEncrypt));
            encryptedMessageJSON.put("isText", isText());
            encryptedMessageJSON.put("isCompressed", isCompressed());
            json.put("recipientPublicKey", Convert.toHexString(recipientPublicKey));
            json.put("encryptedMessage", encryptedMessageJSON);
        } else {
            super.putMyJSON(json);
        }
    }

    @Override
    public void validate(Transaction transaction) throws NxtException.ValidationException {
        if (getEncryptedData() == null) {
            int dataLength = getEncryptedDataLength();
            if (dataLength > Constants.MAX_PRUNABLE_ENCRYPTED_MESSAGE_LENGTH) {
                throw new NxtException.NotValidException(String.format("Message length %d exceeds max prunable encrypted message length %d",
                        dataLength, Constants.MAX_PRUNABLE_ENCRYPTED_MESSAGE_LENGTH));
            }
        } else {
            super.validate(transaction);
        }
    }

    @Override
    public void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
        if (getEncryptedData() == null) {
            throw new NxtException.NotYetEncryptedException("Prunable encrypted message not yet encrypted");
        }
        super.apply(transaction, senderAccount, recipientAccount);
    }

    @Override
    public void loadPrunable(Transaction transaction, boolean includeExpiredPrunable) {}

    @Override
    public void encrypt(String secretPhrase) {
        setEncryptedData(EncryptedData.encrypt(getPlaintext(), secretPhrase, recipientPublicKey));
    }

    @Override
    int getEncryptedDataLength() {
        return EncryptedData.getEncryptedDataLength(getPlaintext());
    }

    private byte[] getPlaintext() {
        return isCompressed() && messageToEncrypt.length > 0 ? Convert.compress(messageToEncrypt) : messageToEncrypt;
    }

}
