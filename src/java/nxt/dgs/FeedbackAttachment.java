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

public final class FeedbackAttachment extends Attachment.AbstractAttachment {

    private final long purchaseId;

    FeedbackAttachment(ByteBuffer buffer) {
        super(buffer);
        this.purchaseId = buffer.getLong();
    }

    FeedbackAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.purchaseId = Convert.parseUnsignedLong((String)attachmentData.get("purchase"));
    }

    public FeedbackAttachment(long purchaseId) {
        this.purchaseId = purchaseId;
    }

    @Override
    protected int getMySize() {
        return 8;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(purchaseId);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("purchase", Long.toUnsignedString(purchaseId));
    }

    @Override
    public TransactionType getTransactionType() {
        return DigitalGoodsTransactionType.FEEDBACK;
    }

    public long getPurchaseId() { return purchaseId; }

}
