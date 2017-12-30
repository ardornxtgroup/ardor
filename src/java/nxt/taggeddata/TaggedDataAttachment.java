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

package nxt.taggeddata;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.Appendix;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionType;
import nxt.crypto.Crypto;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;

public final class TaggedDataAttachment extends Attachment.AbstractAttachment implements Appendix.Prunable {

    public static final Parser appendixParser = new Parser() {
        @Override
        public AbstractAppendix parse(ByteBuffer buffer) throws NxtException.NotValidException {
            return new TaggedDataAttachment(buffer);
        }
        @Override
        public AbstractAppendix parse(JSONObject attachmentData) throws NxtException.NotValidException {
            if (!Appendix.hasAppendix(TaggedDataTransactionType.TAGGED_DATA_UPLOAD.getName(), attachmentData)) {
                return null;
            }
            return new TaggedDataAttachment(attachmentData);
        }
    };

    private final byte[] hash;
    private final String name;
    private final String description;
    private final String tags;
    private final String type;
    private final String channel;
    private final boolean isText;
    private final String filename;
    private final byte[] data;
    private volatile TaggedDataHome.TaggedData taggedData;

    TaggedDataAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
        super(buffer);
        byte flags = buffer.get();
        if ((flags & 1) != 0) {
            this.name = Convert.readString(buffer, buffer.getShort(), Constants.MAX_TAGGED_DATA_NAME_LENGTH);
            this.description = Convert.readString(buffer, buffer.getShort(), Constants.MAX_TAGGED_DATA_DESCRIPTION_LENGTH);
            this.tags = Convert.readString(buffer, buffer.getShort(), Constants.MAX_TAGGED_DATA_TAGS_LENGTH);
            this.type = Convert.readString(buffer, buffer.getShort(), Constants.MAX_TAGGED_DATA_TYPE_LENGTH);
            this.channel = Convert.readString(buffer, buffer.getShort(), Constants.MAX_TAGGED_DATA_CHANNEL_LENGTH);
            this.filename = Convert.readString(buffer, buffer.getShort(), Constants.MAX_TAGGED_DATA_FILENAME_LENGTH);
            this.isText = (flags & 2) != 0;
            int length = buffer.getInt();
            if (length > Constants.MAX_TAGGED_DATA_DATA_LENGTH) {
                throw new NxtException.NotValidException("Invalid tagged data length " + length);
            }
            this.data = new byte[length];
            buffer.get(this.data);
            this.hash = null;
        } else {
            this.hash = new byte[32];
            buffer.get(hash);
            this.name = null;
            this.description = null;
            this.tags = null;
            this.type = null;
            this.channel = null;
            this.isText = false;
            this.filename = null;
            this.data = null;
        }
    }

    TaggedDataAttachment(JSONObject attachmentData) {
        super(attachmentData);
        String dataJSON = (String) attachmentData.get("data");
        if (dataJSON != null) {
            this.name = (String) attachmentData.get("name");
            this.description = (String) attachmentData.get("description");
            this.tags = (String) attachmentData.get("tags");
            this.type = (String) attachmentData.get("type");
            this.channel = Convert.nullToEmpty((String) attachmentData.get("channel"));
            this.isText = Boolean.TRUE.equals(attachmentData.get("isText"));
            this.data = isText ? Convert.toBytes(dataJSON) : Convert.parseHexString(dataJSON);
            this.filename = (String) attachmentData.get("filename");
            this.hash = null;
        } else {
            this.hash = Convert.parseHexString(Convert.emptyToNull((String)attachmentData.get("hash")));
            this.name = null;
            this.description = null;
            this.tags = null;
            this.type = null;
            this.channel = null;
            this.isText = false;
            this.filename = null;
            this.data = null;
        }
    }

    public TaggedDataAttachment(String name, String description, String tags, String type, String channel, boolean isText, String filename, byte[] data)
            throws NxtException.NotValidException {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.type = type;
        this.channel = channel;
        this.isText = isText;
        this.data = data;
        this.filename = filename;
        this.hash = null;
        if (isText && !Arrays.equals(data, Convert.toBytes(Convert.toString(data)))) {
            throw new NxtException.NotValidException("Data is not UTF-8 text");
        }
    }

    @Override
    public TransactionType getTransactionType() {
        return TaggedDataTransactionType.TAGGED_DATA_UPLOAD;
    }

    @Override
    protected int getMySize() {
        return 1 + 32;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.put((byte)0);
        buffer.put(getHash());
    }

    @Override
    public int getMyFullSize() {
        if (!hasPrunableData()) {
            throw new IllegalStateException("Prunable data not available");
        }
        return 1 + 2 + Convert.toBytes(getName()).length + 2 + Convert.toBytes(getDescription()).length +
                2 + Convert.toBytes(getType()).length + 2 + Convert.toBytes(getChannel()).length +
                2 + Convert.toBytes(getTags()).length + 2 + Convert.toBytes(getFilename()).length +
                4 + getData().length;
    }

    @Override
    public void putMyPrunableBytes(ByteBuffer buffer) {
        if (!hasPrunableData()) {
            throw new IllegalStateException("Prunable data not available");
        }
        byte flags = 1;
        if (isText()) {
            flags |= 2;
        }
        buffer.put(flags);
        byte[] nameBytes = Convert.toBytes(getName());
        buffer.putShort((short)nameBytes.length);
        buffer.put(nameBytes);
        byte[] descriptionBytes = Convert.toBytes(getDescription());
        buffer.putShort((short)descriptionBytes.length);
        buffer.put(descriptionBytes);
        byte[] tagsBytes = Convert.toBytes(getTags());
        buffer.putShort((short)tagsBytes.length);
        buffer.put(tagsBytes);
        byte[] typeBytes = Convert.toBytes(getType());
        buffer.putShort((short)typeBytes.length);
        buffer.put(typeBytes);
        byte[] channelBytes = Convert.toBytes(getChannel());
        buffer.putShort((short)channelBytes.length);
        buffer.put(channelBytes);
        byte[] filenameBytes = Convert.toBytes(getFilename());
        buffer.putShort((short)filenameBytes.length);
        buffer.put(filenameBytes);
        buffer.putInt(getData().length);
        buffer.put(getData());
    }

    @Override
    protected void putMyJSON(JSONObject attachment) {
        if (taggedData != null) {
            attachment.put("name", taggedData.getName());
            attachment.put("description", taggedData.getDescription());
            attachment.put("tags", taggedData.getTags());
            attachment.put("type", taggedData.getType());
            attachment.put("channel", taggedData.getChannel());
            attachment.put("isText", taggedData.isText());
            attachment.put("filename", taggedData.getFilename());
            attachment.put("data", taggedData.isText() ? Convert.toString(taggedData.getData()) : Convert.toHexString(taggedData.getData()));
        } else if (data != null) {
            attachment.put("name", name);
            attachment.put("description", description);
            attachment.put("tags", tags);
            attachment.put("type", type);
            attachment.put("channel", channel);
            attachment.put("isText", isText);
            attachment.put("filename", filename);
            attachment.put("data", isText ? Convert.toString(data) : Convert.toHexString(data));
        }
        attachment.put("hash", Convert.toHexString(getHash()));
    }

    @Override
    public byte[] getHash() {
        if (hash != null) {
            return hash;
        }
        if (data == null) {
            return null;
        }
        MessageDigest digest = Crypto.sha256();
        digest.update(Convert.toBytes(name));
        digest.update(Convert.toBytes(description));
        digest.update(Convert.toBytes(tags));
        digest.update(Convert.toBytes(type));
        digest.update(Convert.toBytes(channel));
        digest.update((byte)(isText ? 1 : 0));
        digest.update(Convert.toBytes(filename));
        digest.update(data);
        return digest.digest();
    }

    public final String getName() {
        if (taggedData != null) {
            return taggedData.getName();
        }
        return name;
    }

    public final String getDescription() {
        if (taggedData != null) {
            return taggedData.getDescription();
        }
        return description;
    }

    public final String getTags() {
        if (taggedData != null) {
            return taggedData.getTags();
        }
        return tags;
    }

    public final String getType() {
        if (taggedData != null) {
            return taggedData.getType();
        }
        return type;
    }

    public final String getChannel() {
        if (taggedData != null) {
            return taggedData.getChannel();
        }
        return channel;
    }

    public final boolean isText() {
        if (taggedData != null) {
            return taggedData.isText();
        }
        return isText;
    }

    public final String getFilename() {
        if (taggedData != null) {
            return taggedData.getFilename();
        }
        return filename;
    }

    public final byte[] getData() {
        if (taggedData != null) {
            return taggedData.getData();
        }
        return data;
    }

    @Override
    public void loadPrunable(Transaction transaction, boolean includeExpiredPrunable) {
        if (data == null && taggedData == null && shouldLoadPrunable(transaction, includeExpiredPrunable)) {
            taggedData = ((ChildChain) transaction.getChain()).getTaggedDataHome().getData(transaction.getFullHash());
        }
    }

    @Override
    public boolean hasPrunableData() {
        return (taggedData != null || data != null);
    }

    @Override
    public void restorePrunableData(Transaction transaction, int blockTimestamp, int height) {
        ((ChildChain) transaction.getChain()).getTaggedDataHome().restore(transaction, this, blockTimestamp, height);
    }

}
