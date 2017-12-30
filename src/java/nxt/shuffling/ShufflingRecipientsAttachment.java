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

package nxt.shuffling;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class ShufflingRecipientsAttachment extends AbstractShufflingAttachment {

    private final byte[][] recipientPublicKeys;

    ShufflingRecipientsAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        int count = buffer.get();
        if (count > Constants.MAX_NUMBER_OF_SHUFFLING_PARTICIPANTS || count < 0) {
            throw new NxtException.NotValidException("Invalid data count " + count);
        }
        this.recipientPublicKeys = new byte[count][];
        for (int i = 0; i < count; i++) {
            this.recipientPublicKeys[i] = new byte[32];
            buffer.get(this.recipientPublicKeys[i]);
        }
    }

    ShufflingRecipientsAttachment(JSONObject attachmentData) {
        super(attachmentData);
        JSONArray jsonArray = (JSONArray)attachmentData.get("recipientPublicKeys");
        this.recipientPublicKeys = new byte[jsonArray.size()][];
        for (int i = 0; i < this.recipientPublicKeys.length; i++) {
            this.recipientPublicKeys[i] = Convert.parseHexString((String)jsonArray.get(i));
        }
    }

    ShufflingRecipientsAttachment(byte[] shufflingFullHash, byte[][] recipientPublicKeys, byte[] shufflingStateHash) {
        super(shufflingFullHash, shufflingStateHash);
        this.recipientPublicKeys = recipientPublicKeys;
    }

    @Override
    protected int getMySize() {
        int size = super.getMySize();
        size += 1;
        size += 32 * recipientPublicKeys.length;
        return size;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        super.putMyBytes(buffer);
        buffer.put((byte)recipientPublicKeys.length);
        for (byte[] bytes : recipientPublicKeys) {
            buffer.put(bytes);
        }
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        super.putMyJSON(attachment);
        JSONArray jsonArray = new JSONArray();
        attachment.put("recipientPublicKeys", jsonArray);
        for (byte[] bytes : recipientPublicKeys) {
            jsonArray.add(Convert.toHexString(bytes));
        }
    }

    @Override
    public TransactionType getTransactionType() {
        return ShufflingTransactionType.SHUFFLING_RECIPIENTS;
    }

    public byte[][] getRecipientPublicKeys() {
        return recipientPublicKeys;
    }

}
