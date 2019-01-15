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

package nxt.ae;

import nxt.blockchain.Attachment;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public abstract class OrderPlacementAttachment extends Attachment.AbstractAttachment {

    private final long assetId;
    private final long quantityQNT;
    private final long priceNQT;

    OrderPlacementAttachment(ByteBuffer buffer) {
        super(buffer);
        this.assetId = buffer.getLong();
        this.quantityQNT = buffer.getLong();
        this.priceNQT = buffer.getLong();
    }

    OrderPlacementAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.assetId = Convert.parseUnsignedLong((String) attachmentData.get("asset"));
        this.quantityQNT = Convert.parseLong(attachmentData.get("quantityQNT"));
        this.priceNQT = Convert.parseLong(attachmentData.get("priceNQTPerShare"));
    }

    OrderPlacementAttachment(long assetId, long quantityQNT, long priceNQT) {
        this.assetId = assetId;
        this.quantityQNT = quantityQNT;
        this.priceNQT = priceNQT;
    }

    @Override
    protected int getMySize() {
        return 8 + 8 + 8;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(assetId);
        buffer.putLong(quantityQNT);
        buffer.putLong(priceNQT);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("asset", Long.toUnsignedString(assetId));
        attachment.put("quantityQNT", quantityQNT);
        attachment.put("priceNQTPerShare", priceNQT);
    }

    public long getAssetId() {
        return assetId;
    }

    public long getQuantityQNT() {
        return quantityQNT;
    }

    public long getPriceNQT() {
        return priceNQT;
    }
}
