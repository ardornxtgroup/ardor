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
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public abstract class AbstractShufflingAttachment extends Attachment.AbstractAttachment implements ShufflingAttachment {

    private final byte[] shufflingFullHash;
    private final byte[] shufflingStateHash;

    AbstractShufflingAttachment(ByteBuffer buffer) {
        super(buffer);
        this.shufflingFullHash = new byte[32];
        buffer.get(shufflingFullHash);
        this.shufflingStateHash = new byte[32];
        buffer.get(this.shufflingStateHash);
    }

    AbstractShufflingAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.shufflingFullHash = Convert.parseHexString((String)attachmentData.get("shufflingFullHash"));
        this.shufflingStateHash = Convert.parseHexString((String) attachmentData.get("shufflingStateHash"));
    }

    AbstractShufflingAttachment(byte[] shufflingFullHash, byte[] shufflingStateHash) {
        this.shufflingFullHash = shufflingFullHash;
        this.shufflingStateHash = shufflingStateHash;
    }

    @Override
    protected int getMySize() {
        return 32 + 32;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.put(shufflingFullHash);
        buffer.put(shufflingStateHash);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("shufflingFullHash", Convert.toHexString(shufflingFullHash));
        attachment.put("shufflingStateHash", Convert.toHexString(shufflingStateHash));
    }

    @Override
    public final byte[] getShufflingFullHash() {
        return shufflingFullHash;
    }

    @Override
    public final byte[] getShufflingStateHash() {
        return shufflingStateHash;
    }

}
