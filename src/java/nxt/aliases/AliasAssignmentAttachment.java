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

package nxt.aliases;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class AliasAssignmentAttachment extends Attachment.AbstractAttachment {

    private final String aliasName;
    private final String aliasURI;

    AliasAssignmentAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        aliasName = Convert.readString(buffer, buffer.get(), Constants.MAX_ALIAS_LENGTH).trim();
        aliasURI = Convert.readString(buffer, buffer.getShort(), Constants.MAX_ALIAS_URI_LENGTH).trim();
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
        return 1 + Convert.toBytes(aliasName).length + 2 + Convert.toBytes(aliasURI).length;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        byte[] alias = Convert.toBytes(this.aliasName);
        byte[] uri = Convert.toBytes(this.aliasURI);
        buffer.put((byte)alias.length);
        buffer.put(alias);
        buffer.putShort((short) uri.length);
        buffer.put(uri);
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
