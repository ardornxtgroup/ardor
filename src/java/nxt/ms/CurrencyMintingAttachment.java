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

package nxt.ms;

import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class CurrencyMintingAttachment extends Attachment.AbstractAttachment implements MonetarySystemAttachment {

    private final long nonce;
    private final long currencyId;
    private final long unitsQNT;
    private final long counter;

    public CurrencyMintingAttachment(ByteBuffer buffer) {
        super(buffer);
        this.nonce = buffer.getLong();
        this.currencyId = buffer.getLong();
        this.unitsQNT = buffer.getLong();
        this.counter = buffer.getLong();
    }

    public CurrencyMintingAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.nonce = Convert.parseLong(attachmentData.get("nonce"));
        this.currencyId = Convert.parseUnsignedLong((String)attachmentData.get("currency"));
        this.unitsQNT = Convert.parseLong(attachmentData.get("unitsQNT"));
        this.counter = Convert.parseLong(attachmentData.get("counter"));
    }

    public CurrencyMintingAttachment(long nonce, long currencyId, long unitsQNT, long counter) {
        this.nonce = nonce;
        this.currencyId = currencyId;
        this.unitsQNT = unitsQNT;
        this.counter = counter;
    }

    @Override
    protected int getMySize() {
        return 8 + 8 + 8 + 8;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(nonce);
        buffer.putLong(currencyId);
        buffer.putLong(unitsQNT);
        buffer.putLong(counter);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("nonce", nonce);
        attachment.put("currency", Long.toUnsignedString(currencyId));
        attachment.put("unitsQNT", unitsQNT);
        attachment.put("counter", counter);
    }

    @Override
    public TransactionType getTransactionType() {
        return MonetarySystemTransactionType.CURRENCY_MINTING;
    }

    public long getNonce() {
        return nonce;
    }

    @Override
    public long getCurrencyId() {
        return currencyId;
    }

    public long getUnitsQNT() {
        return unitsQNT;
    }

    public long getCounter() {
        return counter;
    }

}
