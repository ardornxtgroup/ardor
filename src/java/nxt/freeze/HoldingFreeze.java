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

package nxt.freeze;

import nxt.Nxt;
import nxt.account.HoldingType;
import nxt.db.DbClause;
import nxt.db.DbClause.IntClause;
import nxt.db.DbClause.StringClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.VersionedEntityDbTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class HoldingFreeze {

    static void init() {
    }

    private static final DbKey.LongStringKeyFactory<HoldingFreeze> DB_KEY_FACTORY = new DbKey.LongStringKeyFactory<HoldingFreeze>("holding_id", "holding_type") {
        @Override
        public DbKey newKey(HoldingFreeze holdingFreeze) {
            return holdingFreeze.dbKey;
        }
    };

    private static final VersionedEntityDbTable<HoldingFreeze> DB_TABLE = new VersionedEntityDbTable<HoldingFreeze>("public.holding_freeze", DB_KEY_FACTORY) {
        @Override
        protected HoldingFreeze load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new HoldingFreeze(rs, dbKey);
        }

        @Override
        protected void save(Connection con, HoldingFreeze freeze) throws SQLException {
            freeze.save(con);
        }
    };

    private final DbKey dbKey;
    private final HoldingType holdingType;
    private final long holdingId;
    private final int minHeight;
    private int actualHeight;

    HoldingFreeze(long holdingId, HoldingType holdingType, int minHeight, int actualHeight) {
        this.dbKey = DB_KEY_FACTORY.newKey(holdingId, holdingType.name());
        this.holdingId = holdingId;
        this.holdingType = holdingType;
        this.minHeight = minHeight;
        this.actualHeight = actualHeight;
    }

    private HoldingFreeze(ResultSet rs, DbKey dbKey) throws SQLException {
        this.dbKey = dbKey;
        this.holdingId = rs.getLong("holding_id");
        this.holdingType = HoldingType.valueOf(rs.getString("holding_type"));
        this.minHeight = rs.getInt("min_height");
        this.actualHeight = rs.getInt("actual_height");
    }

    private void save(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "MERGE INTO holding_freeze "
                        + " (holding_id, holding_type, min_height, actual_height, height, latest) "
                        + " KEY (holding_id, holding_type, height) VALUES (?, ?, ?, ?, ?, TRUE)")) {
            int i = 0;
            statement.setLong(++i, holdingId);
            statement.setString(++i, holdingType.name());
            statement.setInt(++i, minHeight);
            statement.setInt(++i, actualHeight);
            statement.setInt(++i, Nxt.getBlockchain().getHeight());
            statement.executeUpdate();
        }
    }

    static HoldingFreeze getById(long holdingId, HoldingType holdingType) {
        return DB_TABLE.get(DB_KEY_FACTORY.newKey(holdingId, holdingType.name()));
    }

    static void insert(HoldingFreeze freeze) {
        DB_TABLE.insert(freeze);
    }

    static DbIterator<HoldingFreeze> getAll() {
        return DB_TABLE.getAll(0, -1);
    }

    static DbIterator<HoldingFreeze> getFreezes(HoldingType type, int height) {
        DbClause clause = new IntClause("actual_height", height)
                .and(new StringClause("holding_type", type.name()));
        return DB_TABLE.getManyBy(clause, -1, -1);
    }

    static DbIterator<HoldingFreeze> getFreezes(int height) {
        DbClause clause = new IntClause("actual_height", height);
        return DB_TABLE.getManyBy(clause, -1, -1);
    }

    long getHoldingId() {
        return holdingId;
    }

    int getMinHeight() {
        return minHeight;
    }

    int getActualHeight() {
        return actualHeight;
    }

    HoldingType getHoldingType() {
        return holdingType;
    }

    void setActualHeight(int actualHeight) {
        this.actualHeight = actualHeight;
    }

    boolean isFrozen(int height) {
        return this.actualHeight != 0 && this.actualHeight <= height;
    }

    @Override
    public String toString() {
        return "HoldingFreeze{" +
                "holdingId=" + holdingId +
                ", holdingType=" + holdingType +
                ", minHeight=" + minHeight +
                ", actualHeight=" + actualHeight +
                '}';
    }

}
