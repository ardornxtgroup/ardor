package nxt.http.responses;

import nxt.addons.JO;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.util.List;

public class TaggedDataResponseImpl implements TaggedDataResponse {

    byte[] transactionFullHash;
    long account;
    String name;
    String description;
    String tags;
    List<String> parsedTags;
    String type;
    String channel;
    String filename;
    boolean isText;
    byte[] data;
    int transactionTimestamp;
    int blockTimestamp;

    public TaggedDataResponseImpl(JSONObject response) {
        this(new JO(response));
    }

    public TaggedDataResponseImpl(JO taggedDataJson) {
        transactionFullHash = taggedDataJson.parseHexString("transactionFullHash");
        account = taggedDataJson.getEntityId("account");
        name = taggedDataJson.getString("name");
        description = taggedDataJson.getString("description");
        tags = taggedDataJson.getString("tags");
        parsedTags = taggedDataJson.getArray("parsedTags").values();
        type = taggedDataJson.getString("type");
        channel = taggedDataJson.getString("channel");
        filename = taggedDataJson.getString("filename");
        isText = taggedDataJson.getBoolean("isText");
        data = Convert.toBytes(taggedDataJson.getString("data"), isText);
        transactionTimestamp = taggedDataJson.getInt("transactionTimestamp");
        blockTimestamp = taggedDataJson.getInt("blockTimestamp");
    }

    @Override
    public byte[] getTransactionFullHash() {
        return transactionFullHash;
    }

    @Override
    public long getAccount() {
        return account;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getTags() {
        return tags;
    }

    @Override
    public List<String> getParsedTags() {
        return parsedTags;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getChannel() {
        return channel;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public boolean isText() {
        return isText;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public int getTransactionTimestamp() {
        return transactionTimestamp;
    }

    @Override
    public int getBlockTimestamp() {
        return blockTimestamp;
    }
}
