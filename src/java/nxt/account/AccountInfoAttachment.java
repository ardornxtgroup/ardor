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

package nxt.account;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class AccountInfoAttachment extends Attachment.AbstractAttachment {

    private final String name;
    private final String description;

    AccountInfoAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        this.name = Convert.readString(buffer, buffer.get(), Constants.MAX_ACCOUNT_NAME_LENGTH);
        this.description = Convert.readString(buffer, buffer.getShort(), Constants.MAX_ACCOUNT_DESCRIPTION_LENGTH);
    }

    AccountInfoAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.name = Convert.nullToEmpty((String) attachmentData.get("name"));
        this.description = Convert.nullToEmpty((String) attachmentData.get("description"));
    }

    public AccountInfoAttachment(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    protected int getMySize() {
        return 1 + Convert.toBytes(name).length + 2 + Convert.toBytes(description).length;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        byte[] name = Convert.toBytes(this.name);
        byte[] description = Convert.toBytes(this.description);
        buffer.put((byte)name.length);
        buffer.put(name);
        buffer.putShort((short) description.length);
        buffer.put(description);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("name", name);
        attachment.put("description", description);
    }

    @Override
    public TransactionType getTransactionType() {
        return AccountPropertyTransactionType.ACCOUNT_INFO;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
