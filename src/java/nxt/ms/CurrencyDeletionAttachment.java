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

package nxt.ms;

import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class CurrencyDeletionAttachment extends Attachment.AbstractAttachment implements MonetarySystemAttachment {

    private final long currencyId;

    public CurrencyDeletionAttachment(ByteBuffer buffer) {
        super(buffer);
        this.currencyId = buffer.getLong();
    }

    public CurrencyDeletionAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.currencyId = Convert.parseUnsignedLong((String)attachmentData.get("currency"));
    }

    public CurrencyDeletionAttachment(long currencyId) {
        this.currencyId = currencyId;
    }

    @Override
    protected int getMySize() {
        return 8;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(currencyId);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("currency", Long.toUnsignedString(currencyId));
    }

    @Override
    public TransactionType getTransactionType() {
        return MonetarySystemTransactionType.CURRENCY_DELETION;
    }

    @Override
    public long getCurrencyId() {
        return currencyId;
    }
}
