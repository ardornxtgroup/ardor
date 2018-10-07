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
import static nxt.util.bbh.LengthRwPrimitiveType.SHORT;

public final class AssetIssuanceAttachment extends Attachment.AbstractAttachment {
    public static final StringRw NAME_RW = new StringRw(BYTE, Constants.MAX_ASSET_NAME_LENGTH);
    public static final StringRw DESCRIPTION_RW = new StringRw(SHORT, Constants.MAX_ASSET_DESCRIPTION_LENGTH);

    private final String name;
    private final String description;
    private final long quantityQNT;
    private final byte decimals;

    AssetIssuanceAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        this.name = NAME_RW.readFromBuffer(buffer);
        this.description = DESCRIPTION_RW.readFromBuffer(buffer);
        this.quantityQNT = buffer.getLong();
        this.decimals = buffer.get();
    }

    AssetIssuanceAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.name = (String) attachmentData.get("name");
        this.description = Convert.nullToEmpty((String) attachmentData.get("description"));
        this.quantityQNT = Convert.parseLong(attachmentData.get("quantityQNT"));
        this.decimals = ((Long) attachmentData.get("decimals")).byteValue();
    }

    public AssetIssuanceAttachment(String name, String description, long quantityQNT, byte decimals) {
        this.name = name;
        this.description = Convert.nullToEmpty(description);
        this.quantityQNT = quantityQNT;
        this.decimals = decimals;
    }

    @Override
    protected int getMySize() {
        return 1 + Convert.toBytes(name).length + 2 + Convert.toBytes(description).length + 8 + 1;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        byte[] name = Convert.toBytes(this.name);
        byte[] description = Convert.toBytes(this.description);
        buffer.put((byte)name.length);
        buffer.put(name);
        buffer.putShort((short) description.length);
        buffer.put(description);
        buffer.putLong(quantityQNT);
        buffer.put(decimals);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("name", name);
        attachment.put("description", description);
        attachment.put("quantityQNT", quantityQNT);
        attachment.put("decimals", decimals);
    }

    @Override
    public TransactionType getTransactionType() {
        return AssetExchangeTransactionType.ASSET_ISSUANCE;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getQuantityQNT() {
        return quantityQNT;
    }

    public byte getDecimals() {
        return decimals;
    }
}
