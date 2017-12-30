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

public final class ReserveIncreaseAttachment extends Attachment.AbstractAttachment implements MonetarySystemAttachment {

    private final long currencyId;
    private final long amountPerUnitNQT;

    public ReserveIncreaseAttachment(ByteBuffer buffer) {
        super(buffer);
        this.currencyId = buffer.getLong();
        this.amountPerUnitNQT = buffer.getLong();
    }

    public ReserveIncreaseAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.currencyId = Convert.parseUnsignedLong((String)attachmentData.get("currency"));
        this.amountPerUnitNQT = Convert.parseLong(attachmentData.get("amountPerUnitNQT"));
    }

    public ReserveIncreaseAttachment(long currencyId, long amountPerUnitNQT) {
        this.currencyId = currencyId;
        this.amountPerUnitNQT = amountPerUnitNQT;
    }

    @Override
    protected int getMySize() {
        return 8 + 8;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(currencyId);
        buffer.putLong(amountPerUnitNQT);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("currency", Long.toUnsignedString(currencyId));
        attachment.put("amountPerUnitNQT", amountPerUnitNQT);
    }

    @Override
    public TransactionType getTransactionType() {
        return MonetarySystemTransactionType.RESERVE_INCREASE;
    }

    @Override
    public long getCurrencyId() {
        return currencyId;
    }

    public long getAmountPerUnitNQT() {
        return amountPerUnitNQT;
    }

}
