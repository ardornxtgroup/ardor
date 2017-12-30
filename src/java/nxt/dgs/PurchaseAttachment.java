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

package nxt.dgs;

import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class PurchaseAttachment extends Attachment.AbstractAttachment {

    private final long goodsId;
    private final int quantity;
    private final long priceNQT;
    private final int deliveryDeadlineTimestamp;

    PurchaseAttachment(ByteBuffer buffer) {
        super(buffer);
        this.goodsId = buffer.getLong();
        this.quantity = buffer.getInt();
        this.priceNQT = buffer.getLong();
        this.deliveryDeadlineTimestamp = buffer.getInt();
    }

    PurchaseAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.goodsId = Convert.parseUnsignedLong((String)attachmentData.get("goods"));
        this.quantity = ((Long)attachmentData.get("quantity")).intValue();
        this.priceNQT = Convert.parseLong(attachmentData.get("priceNQT"));
        this.deliveryDeadlineTimestamp = ((Long)attachmentData.get("deliveryDeadlineTimestamp")).intValue();
    }

    public PurchaseAttachment(long goodsId, int quantity, long priceNQT, int deliveryDeadlineTimestamp) {
        this.goodsId = goodsId;
        this.quantity = quantity;
        this.priceNQT = priceNQT;
        this.deliveryDeadlineTimestamp = deliveryDeadlineTimestamp;
    }

    @Override
    protected int getMySize() {
        return 8 + 4 + 8 + 4;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(goodsId);
        buffer.putInt(quantity);
        buffer.putLong(priceNQT);
        buffer.putInt(deliveryDeadlineTimestamp);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("goods", Long.toUnsignedString(goodsId));
        attachment.put("quantity", quantity);
        attachment.put("priceNQT", priceNQT);
        attachment.put("deliveryDeadlineTimestamp", deliveryDeadlineTimestamp);
    }

    @Override
    public TransactionType getTransactionType() {
        return DigitalGoodsTransactionType.PURCHASE;
    }

    public long getGoodsId() { return goodsId; }

    public int getQuantity() { return quantity; }

    public long getPriceNQT() { return priceNQT; }

    public int getDeliveryDeadlineTimestamp() { return deliveryDeadlineTimestamp; }

}
