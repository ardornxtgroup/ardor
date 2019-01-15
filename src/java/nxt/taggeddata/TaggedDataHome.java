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

package nxt.taggeddata;

import nxt.Constants;
import nxt.Nxt;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionImpl;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.PrunableDbTable;
import nxt.db.VersionedPersistentDbTable;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.Search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class TaggedDataHome {

    public static TaggedDataHome forChain(ChildChain childChain) {
        if (childChain.getTaggedDataHome() != null) {
            throw new IllegalStateException("already set");
        }
        return new TaggedDataHome(childChain);
    }

    private final ChildChain childChain;
    private final DbKey.HashKeyFactory<TaggedData> taggedDataKeyFactory;
    private final PrunableDbTable<TaggedData> taggedDataTable;
    private final DbKey.StringKeyFactory<Tag> tagDbKeyFactory;
    private final VersionedPersistentDbTable<Tag> tagTable;

    private TaggedDataHome(ChildChain childChain) {
        this.childChain = childChain;
        this.taggedDataKeyFactory = new DbKey.HashKeyFactory<TaggedData>("full_hash", "id") {
            @Override
            public DbKey newKey(TaggedData taggedData) {
                return taggedData.dbKey;
            }
        };
        this.taggedDataTable = new PrunableDbTable<TaggedData>(childChain.getSchemaTable("tagged_data"), taggedDataKeyFactory,
                "name,description,tags") {
            @Override
            protected TaggedData load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new TaggedData(rs, dbKey);
            }
            @Override
            protected void save(Connection con, TaggedData taggedData) throws SQLException {
                taggedData.save(con);
            }
            @Override
            protected String defaultSort() {
                return " ORDER BY block_timestamp DESC, height DESC, db_id DESC ";
            }
            @Override
            protected void prune() {
                if (Constants.ENABLE_PRUNING) {
                    try (Connection con = taggedDataTable.getConnection();
                         PreparedStatement pstmtSelect = con.prepareStatement("SELECT parsed_tags "
                                 + "FROM tagged_data WHERE transaction_timestamp < ?")) {
                        int expiration = Nxt.getEpochTime() - Constants.MAX_PRUNABLE_LIFETIME;
                        pstmtSelect.setInt(1, expiration);
                        Map<String, Integer> expiredTags = new HashMap<>();
                        try (ResultSet rs = pstmtSelect.executeQuery()) {
                            while (rs.next()) {
                                Object[] array = (Object[]) rs.getArray("parsed_tags").getArray();
                                for (Object tag : array) {
                                    Integer count = expiredTags.get(tag);
                                    expiredTags.put((String) tag, count != null ? count + 1 : 1);
                                }
                            }
                        }
                        deleteTags(expiredTags);
                    } catch (SQLException e) {
                        throw new RuntimeException(e.toString(), e);
                    }
                }
                super.prune();
            }
        };
        this.tagDbKeyFactory = new DbKey.StringKeyFactory<Tag>("tag") {
            @Override
            public DbKey newKey(Tag tag) {
                return tag.dbKey;
            }
        };
        this.tagTable = new VersionedPersistentDbTable<Tag>(childChain.getSchemaTable("data_tag"), tagDbKeyFactory) {
            @Override
            protected Tag load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new Tag(rs, dbKey);
            }
            @Override
            protected void save(Connection con, Tag tag) throws SQLException {
                tag.save(con);
            }
            @Override
            public String defaultSort() {
                return " ORDER BY tag_count DESC, tag ASC ";
            }
        };
    }

    public int getTagCount() {
        return tagTable.getCount();
    }

    public DbIterator<Tag> getAllTags(int from, int to) {
        return tagTable.getAll(from, to);
    }

    public DbIterator<Tag> getTagsLike(String prefix, int from, int to) {
        DbClause dbClause = new DbClause.LikeClause("tag", prefix);
        return tagTable.getManyBy(dbClause, from, to, " ORDER BY tag ");
    }

    private void addTags(TaggedData taggedData) {
        for (String tagValue : taggedData.getParsedTags()) {
            Tag tag = tagTable.get(tagDbKeyFactory.newKey(tagValue));
            if (tag == null) {
                tag = new Tag(tagValue, Nxt.getBlockchain().getHeight());
            }
            tag.count += 1;
            tagTable.insert(tag);
        }
    }

    private void addTags(TaggedData taggedData, int height) {
        try (Connection con = tagTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("UPDATE data_tag SET tag_count = tag_count + 1 WHERE tag = ? AND height >= ?")) {
            for (String tagValue : taggedData.getParsedTags()) {
                pstmt.setString(1, tagValue);
                pstmt.setInt(2, height);
                int updated = pstmt.executeUpdate();
                if (updated == 0) {
                    Tag tag = new Tag(tagValue, height);
                    tag.count += 1;
                    tagTable.insert(tag);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    private void deleteTags(Map<String,Integer> expiredTags) {
        try (Connection con = tagTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("UPDATE data_tag SET tag_count = tag_count - ? WHERE tag = ?");
             PreparedStatement pstmtDelete = con.prepareStatement("DELETE FROM data_tag WHERE tag_count <= 0")) {
            for (Map.Entry<String,Integer> entry : expiredTags.entrySet()) {
                pstmt.setInt(1, entry.getValue());
                pstmt.setString(2, entry.getKey());
                pstmt.executeUpdate();
                Logger.logDebugMessage("Reduced tag count for " + entry.getKey() + " by " + entry.getValue());
            }
            int deleted = pstmtDelete.executeUpdate();
            if (deleted > 0) {
                Logger.logDebugMessage("Deleted " + deleted + " tags");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public int getCount() {
        return taggedDataTable.getCount();
    }

    public DbIterator<TaggedData> getAll(int from, int to) {
        return taggedDataTable.getAll(from, to);
    }

    public TaggedData getData(byte[] transactionFullHash) {
        return taggedDataTable.get(taggedDataKeyFactory.newKey(transactionFullHash));
    }

    public DbIterator<TaggedData> getData(String channel, long accountId, int from, int to) {
        if (channel == null && accountId == 0) {
            throw new IllegalArgumentException("Either channel, or accountId, or both, must be specified");
        }
        return taggedDataTable.getManyBy(getDbClause(channel, accountId), from, to);
    }

    public DbIterator<TaggedData> searchData(String query, String channel, long accountId, int from, int to) {
        return taggedDataTable.search(query, getDbClause(channel, accountId), from, to,
                " ORDER BY ft.score DESC, tagged_data.block_timestamp DESC, tagged_data.db_id DESC ");
    }

    private DbClause getDbClause(String channel, long accountId) {
        DbClause dbClause = DbClause.EMPTY_CLAUSE;
        if (channel != null) {
            dbClause = new DbClause.StringClause("channel", channel);
        }
        if (accountId != 0) {
            DbClause accountClause = new DbClause.LongClause("account_id", accountId);
            dbClause = dbClause != DbClause.EMPTY_CLAUSE ? dbClause.and(accountClause) : accountClause;
        }
        return dbClause;
    }

    void add(TransactionImpl transaction, TaggedDataAttachment attachment) {
        if (Nxt.getEpochTime() - transaction.getTimestamp() < Constants.MAX_PRUNABLE_LIFETIME && attachment.getData() != null) {
            TaggedData taggedData = taggedDataTable.get(taggedDataKeyFactory.newKey(transaction.getFullHash(), transaction.getId()));
            if (taggedData == null) {
                taggedData = new TaggedData(transaction, attachment);
                taggedDataTable.insert(taggedData);
                addTags(taggedData);
            }
        }
    }

    void restore(Transaction transaction, TaggedDataAttachment attachment, int blockTimestamp, int height) {
        TaggedData taggedData = new TaggedData(transaction, attachment, blockTimestamp, height);
        taggedDataTable.insert(taggedData);
        addTags(taggedData, height);
    }

    public boolean isPruned(byte[] transactionFullHash) {
        try (Connection con = taggedDataTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT 1 FROM tagged_data WHERE id = ? AND full_hash = ?")) {
            pstmt.setLong(1, Convert.fullHashToId(transactionFullHash));
            pstmt.setBytes(2, transactionFullHash);
            try (ResultSet rs = pstmt.executeQuery()) {
                return !rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public final class Tag {

        private final String tag;
        private final DbKey dbKey;
        private final int height;
        private int count;

        private Tag(String tag, int height) {
            this.tag = tag;
            this.dbKey = tagDbKeyFactory.newKey(this.tag);
            this.height = height;
        }

        private Tag(ResultSet rs, DbKey dbKey) throws SQLException {
            this.tag = rs.getString("tag");
            this.dbKey = dbKey;
            this.count = rs.getInt("tag_count");
            this.height = rs.getInt("height");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO data_tag (tag, tag_count, height, latest) "
                    + "KEY (tag, height) VALUES (?, ?, ?, TRUE)")) {
                int i = 0;
                pstmt.setString(++i, this.tag);
                pstmt.setInt(++i, this.count);
                pstmt.setInt(++i, this.height);
                pstmt.executeUpdate();
            }
        }

        public String getTag() {
            return tag;
        }

        public int getCount() {
            return count;
        }

    }

    public final class TaggedData {

        private final long id;
        private final byte[] transactionFullHash;
        private final DbKey dbKey;
        private final long accountId;
        private final String name;
        private final String description;
        private final String tags;
        private final String[] parsedTags;
        private final byte[] data;
        private final String type;
        private final String channel;
        private final boolean isText;
        private final String filename;
        private final int transactionTimestamp;
        private final int blockTimestamp;
        private final int height;

        private TaggedData(Transaction transaction, TaggedDataAttachment attachment) {
            this(transaction, attachment, Nxt.getBlockchain().getLastBlockTimestamp(), Nxt.getBlockchain().getHeight());
        }

        private TaggedData(Transaction transaction, TaggedDataAttachment attachment, int blockTimestamp, int height) {
            this.id = transaction.getId();
            this.transactionFullHash = transaction.getFullHash();
            this.dbKey = taggedDataKeyFactory.newKey(this.transactionFullHash, this.id);
            this.accountId = transaction.getSenderId();
            this.name = attachment.getName();
            this.description = attachment.getDescription();
            this.tags = attachment.getTags();
            this.parsedTags = Search.parseTags(tags, 3, 20, 5);
            this.data = attachment.getData();
            this.type = attachment.getType();
            this.channel = attachment.getChannel();
            this.isText = attachment.isText();
            this.filename = attachment.getFilename();
            this.blockTimestamp = blockTimestamp;
            this.transactionTimestamp = transaction.getTimestamp();
            this.height = height;
        }

        private TaggedData(ResultSet rs, DbKey dbKey) throws SQLException {
            this.id = rs.getLong("id");
            this.transactionFullHash = rs.getBytes("full_hash");
            this.dbKey = dbKey;
            this.accountId = rs.getLong("account_id");
            this.name = rs.getString("name");
            this.description = rs.getString("description");
            this.tags = rs.getString("tags");
            this.parsedTags = DbUtils.getArray(rs, "parsed_tags", String[].class);
            this.data = rs.getBytes("data");
            this.type = rs.getString("type");
            this.channel = rs.getString("channel");
            this.isText = rs.getBoolean("is_text");
            this.filename = rs.getString("filename");
            this.blockTimestamp = rs.getInt("block_timestamp");
            this.transactionTimestamp = rs.getInt("transaction_timestamp");
            this.height = rs.getInt("height");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO tagged_data (id, full_hash, account_id, name, description, tags, parsed_tags, "
                    + "type, channel, data, is_text, filename, block_timestamp, transaction_timestamp, height) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, this.id);
                pstmt.setBytes(++i, this.transactionFullHash);
                pstmt.setLong(++i, this.accountId);
                pstmt.setString(++i, this.name);
                pstmt.setString(++i, this.description);
                pstmt.setString(++i, this.tags);
                DbUtils.setArray(pstmt, ++i, this.parsedTags);
                pstmt.setString(++i, this.type);
                pstmt.setString(++i, this.channel);
                pstmt.setBytes(++i, this.data);
                pstmt.setBoolean(++i, this.isText);
                pstmt.setString(++i, this.filename);
                pstmt.setInt(++i, this.blockTimestamp);
                pstmt.setInt(++i, this.transactionTimestamp);
                pstmt.setInt(++i, height);
                pstmt.executeUpdate();
            }
        }

        public long getId() {
            return id;
        }

        public byte[] getTransactionFullHash() {
            return transactionFullHash;
        }

        public long getAccountId() {
            return accountId;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getTags() {
            return tags;
        }

        public String[] getParsedTags() {
            return parsedTags;
        }

        public byte[] getData() {
            return data;
        }

        public String getType() {
            return type;
        }

        public String getChannel() {
            return channel;
        }

        public boolean isText() {
            return isText;
        }

        public String getFilename() {
            return filename;
        }

        public int getTransactionTimestamp() {
            return transactionTimestamp;
        }

        public int getBlockTimestamp() {
            return blockTimestamp;
        }

        public ChildChain getChildChain() {
            return childChain;
        }

    }

}
