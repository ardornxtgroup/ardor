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

import nxt.account.HoldingType;
import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class DividendPaymentAttachment extends Attachment.AbstractAttachment {

    private final long assetId;
    private final int height;
    private final long amountNQT;
    private final long holdingId;
    private final HoldingType holdingType;

    DividendPaymentAttachment(ByteBuffer buffer) {
        super(buffer);
        this.holdingId = buffer.getLong();
        this.holdingType = HoldingType.get(buffer.get());
        this.assetId = buffer.getLong();
        this.height = buffer.getInt();
        this.amountNQT = buffer.getLong();
    }

    DividendPaymentAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.assetId = Convert.parseUnsignedLong((String)attachmentData.get("asset"));
        this.height = ((Long)attachmentData.get("height")).intValue();
        this.amountNQT = Convert.parseLong(attachmentData.get("amountNQTPerShare"));
        this.holdingId = Convert.parseUnsignedLong((String) attachmentData.get("holding"));
        this.holdingType = HoldingType.get(((Long)attachmentData.get("holdingType")).byteValue());
    }

    public DividendPaymentAttachment(long holdingId, HoldingType holdingType, long assetId, int height, long amountNQT) {
        this.holdingId = holdingId;
        this.holdingType = holdingType;
        this.assetId = assetId;
        this.height = height;
        this.amountNQT = amountNQT;
    }

    @Override
    protected int getMySize() {
        return 8 + 1 + 8 + 4 + 8;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(holdingId);
        buffer.put(holdingType.getCode());
        buffer.putLong(assetId);
        buffer.putInt(height);
        buffer.putLong(amountNQT);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("asset", Long.toUnsignedString(assetId));
        attachment.put("height", height);
        attachment.put("amountNQTPerShare", amountNQT);
        attachment.put("holding", Long.toUnsignedString(holdingId));
        attachment.put("holdingType", holdingType.getCode());
    }

    @Override
    public TransactionType getTransactionType() {
        return AssetExchangeTransactionType.DIVIDEND_PAYMENT;
    }

    public long getAssetId() {
        return assetId;
    }

    public int getHeight() {
        return height;
    }

    public long getAmountNQT() {
        return amountNQT;
    }

    public long getHoldingId() {
        return holdingId;
    }

    public HoldingType getHoldingType() {
        return holdingType;
    }

}
