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

public final class AliasSellAttachment extends Attachment.AbstractAttachment {

    private final String aliasName;
    private final long priceNQT;

    AliasSellAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        this.aliasName = Convert.readString(buffer, buffer.get(), Constants.MAX_ALIAS_LENGTH);
        this.priceNQT = buffer.getLong();
    }

    AliasSellAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.aliasName = Convert.nullToEmpty((String) attachmentData.get("alias"));
        this.priceNQT = Convert.parseLong(attachmentData.get("priceNQT"));
    }

    public AliasSellAttachment(String aliasName, long priceNQT) {
        this.aliasName = aliasName;
        this.priceNQT = priceNQT;
    }

    @Override
    public TransactionType getTransactionType() {
        return AliasTransactionType.ALIAS_SELL;
    }

    @Override
    protected int getMySize() {
        return 1 + Convert.toBytes(aliasName).length + 8;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        byte[] aliasBytes = Convert.toBytes(aliasName);
        buffer.put((byte)aliasBytes.length);
        buffer.put(aliasBytes);
        buffer.putLong(priceNQT);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("alias", aliasName);
        attachment.put("priceNQT", priceNQT);
    }

    public String getAliasName(){
        return aliasName;
    }

    public long getPriceNQT(){
        return priceNQT;
    }
}
