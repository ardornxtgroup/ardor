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

package nxt.aliases;

import nxt.Nxt;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.VersionedEntityDbTable;
import nxt.util.Listener;
import nxt.util.Listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public final class AliasHome {

    public static AliasHome forChain(ChildChain childChain) {
        if (childChain.getAliasHome() != null) {
            throw new IllegalStateException("already set");
        }
        return new AliasHome(childChain);
    }

    private final DbKey.LongKeyFactory<Alias> aliasDbKeyFactory;
    private final VersionedEntityDbTable<Alias> aliasTable;
    private final DbKey.LongKeyFactory<Offer> offerDbKeyFactory;
    private final VersionedEntityDbTable<Offer> offerTable;
    private final ChildChain childChain;

    private AliasHome(ChildChain childChain) {
        this.childChain = childChain;
        this.aliasDbKeyFactory = new DbKey.LongKeyFactory<Alias>("id") {
            @Override
            public DbKey newKey(Alias alias) {
                return alias.dbKey;
            }
        };
        this.aliasTable = new VersionedEntityDbTable<Alias>(childChain.getSchemaTable("alias"), aliasDbKeyFactory) {
            @Override
            protected Alias load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new Alias(rs, dbKey);
            }
            @Override
            protected void save(Connection con, Alias alias) throws SQLException {
                alias.save(con);
            }
            @Override
            protected String defaultSort() {
                return " ORDER BY alias_name_lower ";
            }
        };
        this.offerDbKeyFactory = new DbKey.LongKeyFactory<Offer>("id") {
            @Override
            public DbKey newKey(Offer offer) {
                return offer.dbKey;
            }
        };
        this.offerTable = new VersionedEntityDbTable<Offer>(childChain.getSchemaTable("alias_offer"), offerDbKeyFactory) {
            @Override
            protected Offer load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new Offer(rs, dbKey);
            }
            @Override
            protected void save(Connection con, Offer offer) throws SQLException {
                offer.save(con);
            }
        };
    }

    public int getCount() {
        return aliasTable.getCount();
    }

    public int getAccountAliasCount(long accountId) {
        return aliasTable.getCount(new DbClause.LongClause("account_id", accountId));
    }

    public DbIterator<Alias> getAliasesByOwner(long accountId, int from, int to) {
        return aliasTable.getManyBy(new DbClause.LongClause("account_id", accountId), from, to);
    }

    public Alias getAlias(String aliasName) {
        return aliasTable.getBy(new DbClause.StringClause("alias_name_lower", aliasName.toLowerCase(Locale.ROOT)));
    }

    public DbIterator<Alias> getAliasesLike(String aliasName, int from, int to) {
        return aliasTable.getManyBy(new DbClause.LikeClause("alias_name_lower", aliasName.toLowerCase(Locale.ROOT)), from, to);
    }

    public Alias getAlias(long id) {
        return aliasTable.get(aliasDbKeyFactory.newKey(id));
    }

    public Offer getOffer(Alias alias) {
        return offerTable.getBy(new DbClause.LongClause("id", alias.getId()).and(new DbClause.LongClause("price", DbClause.Op.NE, Long.MAX_VALUE)));
    }

    void deleteAlias(final String aliasName) {
        final Alias alias = getAlias(aliasName);
        final Offer offer = getOffer(alias);
        if (offer != null) {
            offer.priceNQT = Long.MAX_VALUE;
            offerTable.delete(offer);
        }
        aliasTable.delete(alias);
        aliasListeners.notify(alias, Event.DELETE_ALIAS);
    }

    void addOrUpdateAlias(Transaction transaction, AliasAssignmentAttachment attachment) {
        Alias alias = getAlias(attachment.getAliasName());
        if (alias == null) {
            alias = new Alias(transaction, attachment);
        } else {
            alias.accountId = transaction.getSenderId();
            alias.aliasURI = attachment.getAliasURI();
            alias.timestamp = Nxt.getBlockchain().getLastBlockTimestamp();
        }
        aliasTable.insert(alias);
        aliasListeners.notify(alias, Event.SET_ALIAS);
    }

    public void importAlias(long id, long accountId, String aliasName, String aliasURI) {
        Alias alias = new Alias(id, accountId, aliasName, aliasURI);
        aliasTable.insert(alias);
    }

    void sellAlias(Transaction transaction, AliasSellAttachment attachment) {
        final String aliasName = attachment.getAliasName();
        final long priceNQT = attachment.getPriceNQT();
        final long buyerId = transaction.getRecipientId();
        if (priceNQT > 0) {
            Alias alias = getAlias(aliasName);
            Offer offer = getOffer(alias);
            if (offer == null) {
                offerTable.insert(new Offer(alias.id, priceNQT, buyerId));
            } else {
                offer.priceNQT = priceNQT;
                offer.buyerId = buyerId;
                offerTable.insert(offer);
            }
        } else {
            changeOwner(buyerId, aliasName);
        }

    }

    void changeOwner(long newOwnerId, String aliasName) {
        Alias alias = getAlias(aliasName);
        alias.accountId = newOwnerId;
        alias.timestamp = Nxt.getBlockchain().getLastBlockTimestamp();
        aliasTable.insert(alias);
        Offer offer = getOffer(alias);
        if (offer != null) {
            offer.priceNQT = Long.MAX_VALUE;
            offerTable.delete(offer);
        }
    }


    public final class Offer {

        private long priceNQT;
        private long buyerId;
        private final long aliasId;
        private final DbKey dbKey;

        private Offer(long aliasId, long priceNQT, long buyerId) {
            this.priceNQT = priceNQT;
            this.buyerId = buyerId;
            this.aliasId = aliasId;
            this.dbKey = offerDbKeyFactory.newKey(this.aliasId);
        }

        private Offer(ResultSet rs, DbKey dbKey) throws SQLException {
            this.aliasId = rs.getLong("id");
            this.dbKey = dbKey;
            this.priceNQT = rs.getLong("price");
            this.buyerId = rs.getLong("buyer_id");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO alias_offer (id, price, buyer_id, "
                    + "height) KEY (id, height) VALUES (?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, this.aliasId);
                pstmt.setLong(++i, this.priceNQT);
                DbUtils.setLongZeroToNull(pstmt, ++i, this.buyerId);
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        public long getId() {
            return aliasId;
        }

        public long getPriceNQT() {
            return priceNQT;
        }

        public long getBuyerId() {
            return buyerId;
        }

        public ChildChain getChildChain() {
            return childChain;
        }

    }

    public final class Alias {

        private long accountId;
        private final long id;
        private final DbKey dbKey;
        private final String aliasName;
        private String aliasURI;
        private int timestamp;

        private Alias(Transaction transaction, AliasAssignmentAttachment attachment) {
            this.id = transaction.getId();
            this.dbKey = aliasDbKeyFactory.newKey(this.id);
            this.accountId = transaction.getSenderId();
            this.aliasName = attachment.getAliasName();
            this.aliasURI = attachment.getAliasURI();
            this.timestamp = Nxt.getBlockchain().getLastBlockTimestamp();
        }

        private Alias(ResultSet rs, DbKey dbKey) throws SQLException {
            this.id = rs.getLong("id");
            this.dbKey = dbKey;
            this.accountId = rs.getLong("account_id");
            this.aliasName = rs.getString("alias_name");
            this.aliasURI = rs.getString("alias_uri");
            this.timestamp = rs.getInt("timestamp");
        }

        private Alias(long id, long accountId, String aliasName, String aliasURI) {
            this.id = id;
            this.accountId = accountId;
            this.aliasName = aliasName;
            this.aliasURI = aliasURI;
            this.dbKey = aliasDbKeyFactory.newKey(this.id);
            this.timestamp = 0;
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO alias (id, account_id, alias_name, alias_name_lower, "
                    + "alias_uri, timestamp, height) KEY (id, height) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, this.id);
                pstmt.setLong(++i, this.accountId);
                pstmt.setString(++i, this.aliasName);
                pstmt.setString(++i, this.aliasName.toLowerCase(Locale.ROOT));
                pstmt.setString(++i, this.aliasURI);
                pstmt.setInt(++i, this.timestamp);
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        public long getId() {
            return id;
        }

        public String getAliasName() {
            return aliasName;
        }

        public String getAliasURI() {
            return aliasURI;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public long getAccountId() {
            return accountId;
        }

        public Offer getOffer() {
            return AliasHome.this.getOffer(this);
        }

        public ChildChain getChildChain() {
            return childChain;
        }

    }

    private static final Listeners<Alias, Event> aliasListeners = new Listeners<>();

    public enum Event {
        SET_ALIAS, DELETE_ALIAS
    }

    public static boolean addListener(Listener<Alias> listener, Event eventType) {
        return aliasListeners.addListener(listener, eventType);
    }

    public static boolean removeListener(Listener<Alias> listener, Event eventType) {
        return aliasListeners.removeListener(listener, eventType);
    }
}