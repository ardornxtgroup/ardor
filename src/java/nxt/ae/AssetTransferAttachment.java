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
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class AssetTransferAttachment extends Attachment.AbstractAttachment {

    private final long assetId;
    private final long quantityQNT;

    AssetTransferAttachment(ByteBuffer buffer) {
        super(buffer);
        this.assetId = buffer.getLong();
        this.quantityQNT = buffer.getLong();
    }

    AssetTransferAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.assetId = Convert.parseUnsignedLong((String) attachmentData.get("asset"));
        this.quantityQNT = Convert.parseLong(attachmentData.get("quantityQNT"));
    }

    public AssetTransferAttachment(long assetId, long quantityQNT) {
        this.assetId = assetId;
        this.quantityQNT = quantityQNT;
    }

    @Override
    protected int getMySize() {
        return 8 + 8;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(assetId);
        buffer.putLong(quantityQNT);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("asset", Long.toUnsignedString(assetId));
        attachment.put("quantityQNT", quantityQNT);
    }

    @Override
    public TransactionType getTransactionType() {
        return AssetExchangeTransactionType.ASSET_TRANSFER;
    }

    public long getAssetId() {
        return assetId;
    }

    public long getQuantityQNT() {
        return quantityQNT;
    }

}
