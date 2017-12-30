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

import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class ShufflingRegistrationAttachment extends Attachment.AbstractAttachment implements ShufflingAttachment {

    private final byte[] shufflingFullHash;

    ShufflingRegistrationAttachment(ByteBuffer buffer) {
        super(buffer);
        this.shufflingFullHash = new byte[32];
        buffer.get(this.shufflingFullHash);
    }

    ShufflingRegistrationAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.shufflingFullHash = Convert.parseHexString((String) attachmentData.get("shufflingFullHash"));
    }

    public ShufflingRegistrationAttachment(byte[] shufflingFullHash) {
        this.shufflingFullHash = shufflingFullHash;
    }

    @Override
    public TransactionType getTransactionType() {
        return ShufflingTransactionType.SHUFFLING_REGISTRATION;
    }

    @Override
    protected int getMySize() {
        return 32;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.put(shufflingFullHash);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("shufflingFullHash", Convert.toHexString(shufflingFullHash));
    }

    @Override
    public byte[] getShufflingFullHash() {
        return shufflingFullHash;
    }

    @Override
    public byte[] getShufflingStateHash() {
        return shufflingFullHash;
    }

}
