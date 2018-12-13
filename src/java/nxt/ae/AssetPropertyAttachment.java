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

package nxt.ae;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import nxt.util.bbh.StringRw;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

import static nxt.util.bbh.LengthRwPrimitiveType.BYTE;
import static nxt.util.bbh.LengthRwPrimitiveType.UBYTE;

public class AssetPropertyAttachment extends Attachment.AbstractAttachment  {
    public static final StringRw PROPERTY_NAME_RW = new StringRw(BYTE, Constants.MAX_ASSET_PROPERTY_NAME_LENGTH);
    public static final StringRw PROPERTY_VALUE_RW = new StringRw(UBYTE, Constants.MAX_ASSET_PROPERTY_VALUE_LENGTH);

    private final long assetId;
    private final String property;
    private final String value;

    AssetPropertyAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        this.assetId = buffer.getLong();
        this.property = PROPERTY_NAME_RW.readFromBuffer(buffer).trim();
        this.value = PROPERTY_VALUE_RW.readFromBuffer(buffer).trim();
    }

    AssetPropertyAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.assetId = Convert.parseUnsignedLong((String)attachmentData.get("asset"));
        this.property = Convert.nullToEmpty((String) attachmentData.get("property")).trim();
        this.value = Convert.nullToEmpty((String) attachmentData.get("value")).trim();
    }

    public AssetPropertyAttachment(long assetId, String property, String value) {
        this.assetId = assetId;
        this.property = property.trim();
        this.value = Convert.nullToEmpty(value).trim();
    }

    @Override
    protected int getMySize() {
        return 8 + PROPERTY_NAME_RW.getSize(property) + PROPERTY_VALUE_RW.getSize(value);
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.putLong(assetId);
        PROPERTY_NAME_RW.writeToBuffer(property, buffer);
        PROPERTY_VALUE_RW.writeToBuffer(value, buffer);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("asset", Long.toUnsignedString(assetId));
        attachment.put("property", property);
        attachment.put("value", value);
    }

    @Override
    public TransactionType getTransactionType() {
        return AssetExchangeTransactionType.ASSET_PROPERTY_SET;
    }

    public long getAssetId() {
        return assetId;
    }

    public String getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }
}
