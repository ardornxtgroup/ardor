package nxt.http.responses;

import java.util.List;

public interface TaggedDataResponse {
    byte[] getTransactionFullHash();

    long getAccount();

    String getName();

    String getDescription();

    String getTags();

    List<String> getParsedTags();

    String getType();

    String getChannel();

    String getFilename();

    boolean isText();

    byte[] getData();

    int getTransactionTimestamp();

    int getBlockTimestamp();
}
