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

package nxt.account;

import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class AccountPropertyDeleteAttachment extends Attachment.AbstractAttachment {

    private final long propertyId;

    AccountPropertyDeleteAttachment(ByteBuffer buffer) {
        super(buffer);
        this.propertyId = buffer.getLong();
    }

    AccountPropertyDeleteAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.propertyId = Convert.parseUnsignedLong((String)attachmentData.get("property"));
    }

    public AccountPropertyDeleteAttachment(long propertyId) {
        this.propertyId = propertyId;
    }

    @Override
    protected int getMySize() {
        return 8;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(propertyId);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("property", Long.toUnsignedString(propertyId));
    }

    @Override
    public TransactionType getTransactionType() {
        return AccountPropertyTransactionType.ACCOUNT_PROPERTY_DELETE;
    }

    public long getPropertyId() {
        return propertyId;
    }

}
