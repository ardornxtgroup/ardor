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

package nxt.dgs;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.crypto.EncryptedData;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public class DeliveryAttachment extends Attachment.AbstractAttachment {

    private final long purchaseId;
    private EncryptedData goods;
    private final long discountNQT;
    private final boolean goodsIsText;

    DeliveryAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        this.purchaseId = buffer.getLong();
        int length = buffer.getInt();
        goodsIsText = length < 0;
        if (length < 0) {
            length &= Integer.MAX_VALUE;
        }
        this.goods = EncryptedData.readEncryptedData(buffer, length, Constants.MAX_DGS_GOODS_LENGTH);
        this.discountNQT = buffer.getLong();
    }

    DeliveryAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.purchaseId = Convert.parseUnsignedLong((String) attachmentData.get("purchase"));
        this.goods = new EncryptedData(Convert.parseHexString((String)attachmentData.get("goodsData")),
                Convert.parseHexString((String)attachmentData.get("goodsNonce")));
        this.discountNQT = Convert.parseLong(attachmentData.get("discountNQT"));
        this.goodsIsText = Boolean.TRUE.equals(attachmentData.get("goodsIsText"));
    }

    public DeliveryAttachment(long purchaseId, EncryptedData goods, boolean goodsIsText, long discountNQT) {
        this.purchaseId = purchaseId;
        this.goods = goods;
        this.discountNQT = discountNQT;
        this.goodsIsText = goodsIsText;
    }

    @Override
    protected int getMySize() {
        return 8 + 4 + goods.getSize() + 8;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(purchaseId);
        buffer.putInt(goodsIsText ? goods.getData().length | Integer.MIN_VALUE : goods.getData().length);
        buffer.put(goods.getData());
        buffer.put(goods.getNonce());
        buffer.putLong(discountNQT);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("purchase", Long.toUnsignedString(purchaseId));
        attachment.put("goodsData", Convert.toHexString(goods.getData()));
        attachment.put("goodsNonce", Convert.toHexString(goods.getNonce()));
        attachment.put("discountNQT", discountNQT);
        attachment.put("goodsIsText", goodsIsText);
    }

    @Override
    public final TransactionType getTransactionType() {
        return DigitalGoodsTransactionType.DELIVERY;
    }

    public final long getPurchaseId() {
        return purchaseId;
    }

    public final EncryptedData getGoods() {
        return goods;
    }

    final void setGoods(EncryptedData goods) {
        this.goods = goods;
    }

    int getGoodsDataLength() {
        return goods.getData().length;
    }

    public final long getDiscountNQT() {
        return discountNQT;
    }

    public final boolean goodsIsText() {
        return goodsIsText;
    }

}
