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

package nxt.aliases;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import nxt.util.bbh.StringRw;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

import static nxt.util.bbh.LengthRwPrimitiveType.BYTE;
import static nxt.util.bbh.LengthRwPrimitiveType.SHORT;

public final class AliasAssignmentAttachment extends Attachment.AbstractAttachment {

    public static final StringRw ALIAS_NAME_RW = new StringRw(BYTE, Constants.MAX_ALIAS_LENGTH);
    public static final StringRw ALIAS_URI_RW = new StringRw(SHORT, Constants.MAX_ALIAS_URI_LENGTH);

    private final String aliasName;
    private final String aliasURI;

    AliasAssignmentAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        aliasName = ALIAS_NAME_RW.readFromBuffer(buffer).trim();
        aliasURI = ALIAS_URI_RW.readFromBuffer(buffer).trim();
    }

    AliasAssignmentAttachment(JSONObject attachmentData) {
        super(attachmentData);
        aliasName = Convert.nullToEmpty((String) attachmentData.get("alias")).trim();
        aliasURI = Convert.nullToEmpty((String) attachmentData.get("uri")).trim();
    }

    public AliasAssignmentAttachment(String aliasName, String aliasURI) {
        this.aliasName = aliasName.trim();
        this.aliasURI = aliasURI.trim();
    }

    @Override
    protected int getMySize() {
        return ALIAS_NAME_RW.getSize(aliasName) + ALIAS_URI_RW.getSize(aliasURI);
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        ALIAS_NAME_RW.writeToBuffer(aliasName, buffer);
        ALIAS_URI_RW.writeToBuffer(aliasURI, buffer);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("alias", aliasName);
        attachment.put("uri", aliasURI);
    }

    @Override
    public TransactionType getTransactionType() {
        return AliasTransactionType.ALIAS_ASSIGNMENT;
    }

    public String getAliasName() {
        return aliasName;
    }

    public String getAliasURI() {
        return aliasURI;
    }
}
