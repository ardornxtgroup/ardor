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

import nxt.NxtException;
import nxt.account.Account;
import nxt.blockchain.Appendix;
import nxt.blockchain.Transaction;
import nxt.crypto.EncryptedData;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class UnencryptedEncryptedMessageAppendix extends EncryptedMessageAppendix implements Appendix.Encryptable {

    private final byte[] messageToEncrypt;
    private final byte[] recipientPublicKey;

    UnencryptedEncryptedMessageAppendix(JSONObject attachmentData) {
        super(attachmentData);
        setEncryptedData(null);
        JSONObject encryptedMessageJSON = (JSONObject)attachmentData.get("encryptedMessage");
        String messageToEncryptString = (String)encryptedMessageJSON.get("messageToEncrypt");
        messageToEncrypt = isText() ? Convert.toBytes(messageToEncryptString) : Convert.parseHexString(messageToEncryptString);
        recipientPublicKey = Convert.parseHexString((String)attachmentData.get("recipientPublicKey"));
    }

    public UnencryptedEncryptedMessageAppendix(byte[] messageToEncrypt, boolean isText, boolean isCompressed, byte[] recipientPublicKey) {
        super(null, isText, isCompressed);
        this.messageToEncrypt = messageToEncrypt;
        this.recipientPublicKey = recipientPublicKey;
    }

    @Override
    protected int getMySize() {
        if (getEncryptedData() != null) {
            return super.getMySize();
        }
        return 1 + 2 + EncryptedData.getEncryptedSize(getPlaintext());
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        if (getEncryptedData() == null) {
            throw new NxtException.NotYetEncryptedException("Message not yet encrypted");
        }
        super.putMyBytes(buffer);
    }

    @Override
    protected void putMyJSON(JSONObject json) {
        if (getEncryptedData() == null) {
            JSONObject encryptedMessageJSON = new JSONObject();
            encryptedMessageJSON.put("messageToEncrypt", isText() ? Convert.toString(messageToEncrypt) : Convert.toHexString(messageToEncrypt));
            encryptedMessageJSON.put("isText", isText());
            encryptedMessageJSON.put("isCompressed", isCompressed());
            json.put("encryptedMessage", encryptedMessageJSON);
            json.put("recipientPublicKey", Convert.toHexString(recipientPublicKey));
        } else {
            super.putMyJSON(json);
        }
    }

    @Override
    public void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
        if (getEncryptedData() == null) {
            throw new NxtException.NotYetEncryptedException("Message not yet encrypted");
        }
        super.apply(transaction, senderAccount, recipientAccount);
    }

    @Override
    public void encrypt(String secretPhrase) {
        setEncryptedData(EncryptedData.encrypt(getPlaintext(), secretPhrase, recipientPublicKey));
    }

    private byte[] getPlaintext() {
        return isCompressed() && messageToEncrypt.length > 0 ? Convert.compress(messageToEncrypt) : messageToEncrypt;
    }

    @Override
    int getEncryptedDataLength() {
        return EncryptedData.getEncryptedDataLength(getPlaintext());
    }

}
