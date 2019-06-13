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

package nxt.ae;

import nxt.Constants;
import nxt.Nxt;
import nxt.account.Account;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.VersionedEntityDbTable;
import nxt.util.Convert;
import nxt.util.Listener;
import nxt.util.Listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Asset {

    public static final String ASSET_FREEZE_HEIGHT_PROPERTY = "Freeze";
    public static final String ASSET_MIGRATE_HEIGHT_PROPERTY = "Migrate";

    public enum Event {
        SET_PROPERTY, DELETE_PROPERTY
    }

    private static final DbKey.LongKeyFactory<Asset> assetDbKeyFactory = new DbKey.LongKeyFactory<Asset>("id") {

        @Override
        public DbKey newKey(Asset asset) {
            return asset.dbKey;
        }

    };

    private static final VersionedEntityDbTable<Asset> assetTable = new VersionedEntityDbTable<Asset>("public.asset", assetDbKeyFactory, "name,description") {

        @Override
        protected Asset load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new Asset(rs, dbKey);
        }

        @Override
        protected void save(Connection con, Asset asset) throws SQLException {
            asset.save(con);
        }

        @Override
        public void trim(int height) {
            super.trim(Math.max(0, height - Constants.MAX_DIVIDEND_PAYMENT_ROLLBACK));
        }

        @Override
        public void checkAvailable(int height) {
            if (height + Constants.MAX_DIVIDEND_PAYMENT_ROLLBACK < Nxt.getBlockchainProcessor().getMinRollbackHeight()) {
                throw new IllegalArgumentException("Historical data as of height " + height +" not available.");
            }
            if (height > Nxt.getBlockchain().getHeight()) {
                throw new IllegalArgumentException("Height " + height + " exceeds blockchain height " + Nxt.getBlockchain().getHeight());
            }
        }

    };

    private static final DbKey.LongKeyFactory<AssetProperty> assetPropertyDbKeyFactory = new DbKey.LongKeyFactory<AssetProperty>("id") {
        @Override
        public DbKey newKey(AssetProperty property) {
            return property.dbKey;
        }
    };

    private static final VersionedEntityDbTable<AssetProperty> assetPropertyTable = new VersionedEntityDbTable<AssetProperty>("public.asset_property", assetPropertyDbKeyFactory) {

        @Override
        protected AssetProperty load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new AssetProperty(rs, dbKey);
        }

        @Override
        protected void save(Connection con, AssetProperty assetProperty) throws SQLException {
            assetProperty.save(con);
        }
    };

    public static DbIterator<Asset> getAllAssets(int from, int to) {
        return assetTable.getAll(from, to);
    }

    public static int getCount() {
        return assetTable.getCount();
    }

    public static Asset getAsset(long id) {
        return assetTable.get(assetDbKeyFactory.newKey(id));
    }

    public static Asset getAsset(long id, int height) {
        return assetTable.get(assetDbKeyFactory.newKey(id), height);
    }

    public static DbIterator<Asset> getAssetsIssuedBy(long accountId, int from, int to) {
        return assetTable.getManyBy(new DbClause.LongClause("account_id", accountId), from, to);
    }

    public static DbIterator<Asset> searchAssets(String query, int from, int to) {
        return assetTable.search(query, DbClause.EMPTY_CLAUSE, from, to, " ORDER BY ft.score DESC ");
    }

    static void addAsset(Transaction transaction, AssetIssuanceAttachment attachment) {
        Asset asset = new Asset(transaction, attachment);
        assetTable.insert(asset);
        AssetHistory.addAssetIncrease(transaction, asset.getId(), asset.getQuantityQNT());
    }

    static void deleteAsset(Transaction transaction, long assetId, long quantityQNT) {
        Asset asset = getAsset(assetId);
        asset.quantityQNT = Math.max(0, asset.quantityQNT - quantityQNT);
        assetTable.insert(asset);
        AssetHistory.addAssetDelete(transaction, assetId, quantityQNT);
    }

    static void increaseAsset(Transaction transaction, long assetId, long quantityQNT) {
        Asset asset = getAsset(assetId);
        asset.quantityQNT = Math.addExact(asset.quantityQNT, quantityQNT);
        assetTable.insert(asset);
        AssetHistory.addAssetIncrease(transaction, assetId, quantityQNT);
    }

    public static void importAsset(long assetId, long issuerId, String name, String description, byte decimals, long quantityQNT) {
        Asset asset = new Asset(assetId, issuerId, name, description, decimals, quantityQNT);
        assetTable.insert(asset);
        AssetHistory.addAssetIncrease(assetId, issuerId, quantityQNT);
    }

    public static void init() {
        AssetFreezeMonitor.init();
        AssetMigrateMonitor.init();
    }

    private final long assetId;
    private final DbKey dbKey;
    private final long accountId;
    private final String name;
    private final String description;
    private long quantityQNT;
    private final byte decimals;
    private boolean hasPhasingControl;

    private Asset(Transaction transaction, AssetIssuanceAttachment attachment) {
        this.assetId = transaction.getId();
        this.dbKey = assetDbKeyFactory.newKey(this.assetId);
        this.accountId = transaction.getSenderId();
        this.name = attachment.getName();
        this.description = attachment.getDescription();
        this.quantityQNT = attachment.getQuantityQNT();
        this.decimals = attachment.getDecimals();
        this.hasPhasingControl = false;
    }

    private Asset(ResultSet rs, DbKey dbKey) throws SQLException {
        this.assetId = rs.getLong("id");
        this.dbKey = dbKey;
        this.accountId = rs.getLong("account_id");
        this.name = rs.getString("name");
        this.description = rs.getString("description");
        this.quantityQNT = rs.getLong("quantity");
        this.decimals = rs.getByte("decimals");
        this.hasPhasingControl = rs.getBoolean("has_control_phasing");
    }

    private Asset(long assetId, long issuerId, String name, String description, byte decimals, long quantityQNT) {
        this.assetId = assetId;
        this.dbKey = assetDbKeyFactory.newKey(this.assetId);
        this.accountId = issuerId;
        this.name = name;
        this.description = description;
        this.decimals = decimals;
        this.quantityQNT = quantityQNT;
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO asset "
                + "(id, account_id, name, description, quantity, decimals, has_control_phasing, height, latest) "
                + "KEY(id, height) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE)")) {
            int i = 0;
            pstmt.setLong(++i, this.assetId);
            pstmt.setLong(++i, this.accountId);
            pstmt.setString(++i, this.name);
            pstmt.setString(++i, this.description);
            pstmt.setLong(++i, this.quantityQNT);
            pstmt.setByte(++i, this.decimals);
            pstmt.setBoolean(++i, this.hasPhasingControl);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.executeUpdate();
        }
    }

    public long getId() {
        return assetId;
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

    public long getQuantityQNT() {
        return quantityQNT;
    }

    public byte getDecimals() {
        return decimals;
    }

    public boolean hasPhasingControl() {
        return hasPhasingControl;
    }

    void setHasPhasingControl(boolean hasPhasingControl) {
        if (this.hasPhasingControl != hasPhasingControl) {
            this.hasPhasingControl = hasPhasingControl;
            assetTable.insert(this);
        }
    }

    public DbIterator<Account.AccountAsset> getAccounts(int from, int to) {
        return Account.getAssetAccounts(this.assetId, from, to);
    }

    public DbIterator<Account.AccountAsset> getAccounts(int height, int from, int to) {
        return Account.getAssetAccounts(this.assetId, height, from, to);
    }

    public DbIterator<TradeHome.Trade> getTrades(ChildChain childChain, int from, int to) {
        return childChain.getTradeHome().getAssetTrades(this.assetId, from, to);
    }

    public DbIterator<AssetTransfer> getAssetTransfers(int from, int to) {
        return AssetTransfer.getAssetTransfers(this.assetId, from, to);
    }

    private static final Listeners<AssetProperty, Event> propertyListeners = new Listeners<>();

    public static boolean addListener(Listener<AssetProperty> listener, Event eventType) {
        return propertyListeners.addListener(listener, eventType);
    }

    public static boolean removeListener(Listener<AssetProperty> listener, Event eventType) {
        return propertyListeners.removeListener(listener, eventType);
    }

    public void setProperty(long transactionId, Account senderAccount, String property, String value) {
        value = Convert.emptyToNull(value);
        AssetProperty entity = getProperty(senderAccount.getId(), property);
        if (entity == null) {
            entity = new AssetProperty(transactionId, assetId, senderAccount.getId(), property, value);
        } else {
            entity.setValue(value);
        }
        assetPropertyTable.insert(entity);
        propertyListeners.notify(entity, Event.SET_PROPERTY);
    }

    public static AssetProperty getProperty(long propertyId) {
        return assetPropertyTable.get(assetPropertyDbKeyFactory.newKey(propertyId));
    }

    public AssetProperty getProperty(long setterId, String property) {
        return assetPropertyTable.getBy(getPropertiesClause(this.assetId, setterId, property));
    }

    public static DbIterator<AssetProperty> getProperties(long assetId, long setterId, String property, int firstIndex, int lastIndex) {
        return assetPropertyTable.getManyBy(getPropertiesClause(assetId, setterId, property), firstIndex, lastIndex);
    }

    private static DbClause getPropertiesClause(long assetId, long setterId, String property) {
        if (assetId == 0 && setterId == 0) {
            throw new IllegalArgumentException("At least one of assetId and setterId must be specified");
        }
        DbClause clause = null;
        if (assetId != 0) {
            clause = new DbClause.LongClause("asset_id", assetId);
        }
        if (setterId != 0) {
            DbClause setterClause = new DbClause.LongClause("setter_id", setterId);
            if (clause != null) {
                clause = clause.and(setterClause);
            } else {
                clause = setterClause;
            }
        }
        if (property != null) {
            clause = clause.and(new DbClause.StringClause("property", property));
        }
        return clause;
    }

    public static void deleteProperty(long propertyId) {
        AssetProperty assetProperty = assetPropertyTable.get(assetPropertyDbKeyFactory.newKey(propertyId));
        if (assetProperty == null) {
            return;
        }
        assetPropertyTable.delete(assetProperty);
        propertyListeners.notify(assetProperty, Event.DELETE_PROPERTY);
    }

    public static class AssetProperty {
        private final long id;
        private final DbKey dbKey;
        private final long assetId;
        private final long setterId;
        private final String property;
        private String value;

        public AssetProperty(long id, long assetId, long setterId, String property, String value) {
            this.id = id;
            this.dbKey = assetPropertyDbKeyFactory.newKey(id);
            this.assetId = assetId;
            this.setterId = setterId;
            this.property = property;
            this.value = value;
        }

        private AssetProperty(ResultSet rs, DbKey dbKey) throws SQLException {
            this.id = rs.getLong("id");
            this.dbKey = dbKey;
            this.assetId = rs.getLong("asset_id");
            this.setterId = rs.getLong("setter_id");
            this.property = rs.getString("property");
            this.value = rs.getString("value");
        }

        public long getId() {
            return id;
        }

        public long getAssetId() {
            return assetId;
        }

        public long getSetterId() {
            return setterId;
        }

        public String getProperty() {
            return property;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO asset_property "
                    + "(id, asset_id, setter_id, property, value, height, latest) "
                    + "KEY (id, height) VALUES (?, ?, ?, ?, ?, ?, TRUE)")) {
                int i = 0;
                pstmt.setLong(++i, this.id);
                pstmt.setLong(++i, this.assetId);
                pstmt.setLong(++i, this.setterId);
                pstmt.setString(++i, this.property);
                DbUtils.setString(pstmt, ++i, this.value);
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        @Override
        public String toString() {
            return "AssetProperty{" +
                    "id=" + id +
                    ", assetId=" + assetId +
                    ", setterId=" + setterId +
                    ", property='" + property + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}
