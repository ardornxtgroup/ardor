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

package nxt.aliases;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class AliasDeleteAttachment extends Attachment.AbstractAttachment {

    private final String aliasName;

    AliasDeleteAttachment(final ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        this.aliasName = AliasAssignmentAttachment.ALIAS_NAME_RW.readFromBuffer(buffer);
    }

    AliasDeleteAttachment(final JSONObject attachmentData) {
        super(attachmentData);
        this.aliasName = Convert.nullToEmpty((String) attachmentData.get("alias"));
    }

    public AliasDeleteAttachment(final String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public TransactionType getTransactionType() {
        return AliasTransactionType.ALIAS_DELETE;
    }

    @Override
    protected int getMySize() {
        return 1 + Convert.toBytes(aliasName).length;
    }

    @Override
    protected void putMyBytes(final ByteBuffer buffer) {
        byte[] aliasBytes = Convert.toBytes(aliasName);
        buffer.put((byte)aliasBytes.length);
        buffer.put(aliasBytes);
    }

    @Override
    protected void putMyJSON(final JSONObject attachment) {
        attachment.put("alias", aliasName);
    }

    public String getAliasName(){
        return aliasName;
    }
}
