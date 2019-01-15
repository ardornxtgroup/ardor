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

package nxt.shuffling;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.TransactionType;
import nxt.crypto.Crypto;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public final class ShufflingCancellationAttachment extends AbstractShufflingAttachment {

    private final byte[][] blameData;
    private final byte[][] keySeeds;
    private final long cancellingAccountId;

    ShufflingCancellationAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        int count = buffer.get();
        if (count > Constants.MAX_NUMBER_OF_SHUFFLING_PARTICIPANTS || count <= 0) {
            throw new NxtException.NotValidException("Invalid data count " + count);
        }
        this.blameData = new byte[count][];
        for (int i = 0; i < count; i++) {
            int size = buffer.getInt();
            if (size > Constants.MAX_CHILDBLOCK_PAYLOAD_LENGTH) {
                throw new NxtException.NotValidException("Invalid data size " + size);
            }
            this.blameData[i] = new byte[size];
            buffer.get(this.blameData[i]);
        }
        count = buffer.get();
        if (count > Constants.MAX_NUMBER_OF_SHUFFLING_PARTICIPANTS || count <= 0) {
            throw new NxtException.NotValidException("Invalid keySeeds count " + count);
        }
        this.keySeeds = new byte[count][];
        for (int i = 0; i < count; i++) {
            this.keySeeds[i] = new byte[32];
            buffer.get(this.keySeeds[i]);
        }
        this.cancellingAccountId = buffer.getLong();
    }

    ShufflingCancellationAttachment(JSONObject attachmentData) {
        super(attachmentData);
        JSONArray jsonArray = (JSONArray)attachmentData.get("blameData");
        this.blameData = new byte[jsonArray.size()][];
        for (int i = 0; i < this.blameData.length; i++) {
            this.blameData[i] = Convert.parseHexString((String)jsonArray.get(i));
        }
        jsonArray = (JSONArray)attachmentData.get("keySeeds");
        this.keySeeds = new byte[jsonArray.size()][];
        for (int i = 0; i < this.keySeeds.length; i++) {
            this.keySeeds[i] = Convert.parseHexString((String)jsonArray.get(i));
        }
        this.cancellingAccountId = Convert.parseUnsignedLong((String) attachmentData.get("cancellingAccount"));
    }

    ShufflingCancellationAttachment(byte[] shufflingFullHash, byte[][] blameData, byte[][] keySeeds, byte[] shufflingStateHash, long cancellingAccountId) {
        super(shufflingFullHash, shufflingStateHash);
        this.blameData = blameData;
        this.keySeeds = keySeeds;
        this.cancellingAccountId = cancellingAccountId;
    }

    @Override
    public TransactionType getTransactionType() {
        return ShufflingTransactionType.SHUFFLING_CANCELLATION;
    }

    @Override
    protected int getMySize() {
        int size = super.getMySize();
        size += 1;
        for (byte[] bytes : blameData) {
            size += 4;
            size += bytes.length;
        }
        size += 1;
        size += 32 * keySeeds.length;
        size += 8;
        return size;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        super.putMyBytes(buffer);
        buffer.put((byte) blameData.length);
        for (byte[] bytes : blameData) {
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }
        buffer.put((byte) keySeeds.length);
        for (byte[] bytes : keySeeds) {
            buffer.put(bytes);
        }
        buffer.putLong(cancellingAccountId);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        super.putMyJSON(attachment);
        JSONArray jsonArray = new JSONArray();
        attachment.put("blameData", jsonArray);
        for (byte[] bytes : blameData) {
            jsonArray.add(Convert.toHexString(bytes));
        }
        jsonArray = new JSONArray();
        attachment.put("keySeeds", jsonArray);
        for (byte[] bytes : keySeeds) {
            jsonArray.add(Convert.toHexString(bytes));
        }
        if (cancellingAccountId != 0) {
            attachment.put("cancellingAccount", Long.toUnsignedString(cancellingAccountId));
        }
    }

    public byte[][] getBlameData() {
        return blameData;
    }

    public byte[][] getKeySeeds() {
        return keySeeds;
    }

    public long getCancellingAccountId() {
        return cancellingAccountId;
    }

    public byte[] getHash() {
        MessageDigest digest = Crypto.sha256();
        for (byte[] bytes : blameData) {
            digest.update(bytes);
        }
        return digest.digest();
    }

}
