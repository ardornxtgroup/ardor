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

package nxt.dgs;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.Attachment;
import nxt.blockchain.TransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public final class ListingAttachment extends Attachment.AbstractAttachment {

    private final String name;
    private final String description;
    private final String tags;
    private final int quantity;
    private final long priceNQT;

    ListingAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        this.name = Convert.readString(buffer, buffer.getShort(), Constants.MAX_DGS_LISTING_NAME_LENGTH);
        this.description = Convert.readString(buffer, buffer.getShort(), Constants.MAX_DGS_LISTING_DESCRIPTION_LENGTH);
        this.tags = Convert.readString(buffer, buffer.getShort(), Constants.MAX_DGS_LISTING_TAGS_LENGTH);
        this.quantity = buffer.getInt();
        this.priceNQT = buffer.getLong();
    }

    ListingAttachment(JSONObject attachmentData) {
        super(attachmentData);
        this.name = (String) attachmentData.get("name");
        this.description = (String) attachmentData.get("description");
        this.tags = (String) attachmentData.get("tags");
        this.quantity = ((Long) attachmentData.get("quantity")).intValue();
        this.priceNQT = Convert.parseLong(attachmentData.get("priceNQT"));
    }

    public ListingAttachment(String name, String description, String tags, int quantity, long priceNQT) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.quantity = quantity;
        this.priceNQT = priceNQT;
    }

    @Override
    protected int getMySize() {
        return 2 + Convert.toBytes(name).length + 2 + Convert.toBytes(description).length + 2
                    + Convert.toBytes(tags).length + 4 + 8;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        byte[] nameBytes = Convert.toBytes(name);
        buffer.putShort((short) nameBytes.length);
        buffer.put(nameBytes);
        byte[] descriptionBytes = Convert.toBytes(description);
        buffer.putShort((short) descriptionBytes.length);
        buffer.put(descriptionBytes);
        byte[] tagsBytes = Convert.toBytes(tags);
        buffer.putShort((short) tagsBytes.length);
        buffer.put(tagsBytes);
        buffer.putInt(quantity);
        buffer.putLong(priceNQT);
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        attachment.put("name", name);
        attachment.put("description", description);
        attachment.put("tags", tags);
        attachment.put("quantity", quantity);
        attachment.put("priceNQT", priceNQT);
    }

    @Override
    public TransactionType getTransactionType() {
        return DigitalGoodsTransactionType.LISTING;
    }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public String getTags() { return tags; }

    public int getQuantity() { return quantity; }

    public long getPriceNQT() { return priceNQT; }

}
