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

package nxt.dgs;

import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class QuantityChangeAttachment extends Attachment.AbstractAttachment {

    private final long goodsId;
    private final int deltaQuantity;

    QuantityChangeAttachment(ByteBuffer buffer) {
        super(buffer);
        this.goodsId = buffer.getLong();
        this.deltaQuantity = buffer.getInt();
    }

    QuantityChangeAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.goodsId = Convert.parseUnsignedLong((String)attachmentData.get("goods"));
        this.deltaQuantity = ((Long)attachmentData.get("deltaQuantity")).intValue();
    }

    public QuantityChangeAttachment(long goodsId, int deltaQuantity) {
        this.goodsId = goodsId;
        this.deltaQuantity = deltaQuantity;
    }

    @Override
    protected int getMySize() {
        return 8 + 4;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(goodsId);
        buffer.putInt(deltaQuantity);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("goods", Long.toUnsignedString(goodsId));
        attachment.put("deltaQuantity", deltaQuantity);
    }

    @Override
    public TransactionType getTransactionType() {
        return DigitalGoodsTransactionType.QUANTITY_CHANGE;
    }

    public long getGoodsId() { return goodsId; }

    public int getDeltaQuantity() { return deltaQuantity; }

}
