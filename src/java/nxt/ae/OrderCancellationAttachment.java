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

package nxt.ae;

import nxt.blockchain.Attachment;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public abstract class OrderCancellationAttachment extends Attachment.AbstractAttachment {

    private final long orderId;
    private final byte[] orderHash;

    OrderCancellationAttachment(ByteBuffer buffer) {
        super(buffer);
        orderHash = new byte[32];
        buffer.get(orderHash);
        orderId = Convert.fullHashToId(orderHash);
    }

    OrderCancellationAttachment(JSONObject attachmentData) {
        super(attachmentData);
        orderHash = Convert.parseHexString((String)attachmentData.get("orderHash"));
        orderId = Convert.fullHashToId(orderHash);
    }

    OrderCancellationAttachment(byte[] orderHash) {
        this.orderHash = orderHash;
        this.orderId = Convert.fullHashToId(this.orderHash);
    }

    @Override
    protected int getMySize() {
        return 32;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.put(orderHash);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("orderHash", Convert.toHexString(orderHash));
    }

    public byte[] getOrderHash() {
        return orderHash;
    }

    public long getOrderId() {
        return orderId;
    }
}
