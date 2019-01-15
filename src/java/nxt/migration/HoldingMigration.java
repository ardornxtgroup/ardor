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

package nxt.migration;

import nxt.Nxt;
import nxt.account.HoldingType;
import nxt.blockchain.ChildChain;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.VersionedEntityDbTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class HoldingMigration {

    static void init() {
    }

    private static final DbKey.LongKeyFactory DB_KEY_FACTORY = new DbKey.LongKeyFactory<HoldingMigration>("child_chain_id") {
        @Override
        public DbKey newKey(HoldingMigration holdingMigration) {
            return holdingMigration.dbKey;
        }
    };

    private static final VersionedEntityDbTable<HoldingMigration> DB_TABLE = new VersionedEntityDbTable<HoldingMigration>("public.holding_migrate", DB_KEY_FACTORY) {
        @Override
        protected HoldingMigration load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new HoldingMigration(rs, dbKey);
        }

        @Override
        protected void save(Connection con, HoldingMigration holdingMigration) throws SQLException {
            holdingMigration.save(con);
        }
    };

    private final DbKey dbKey;
    private final long holdingId;
    private final HoldingType holdingType;
    private final ChildChain childChain;
    private final int minHeight;
    private int actualHeight;

    HoldingMigration(long holdingId, HoldingType holdingType, ChildChain childChain, int minHeight, int actualHeight) {
        this.dbKey = DB_KEY_FACTORY.newKey(childChain.getId());
        this.holdingId = holdingId;
        this.holdingType = holdingType;
        this.minHeight = minHeight;
        this.actualHeight = actualHeight;
        this.childChain = childChain;
    }

    private HoldingMigration(ResultSet rs, DbKey dbKey) throws SQLException {
        this.dbKey = dbKey;
        this.holdingId = rs.getLong("holding_id");
        this.holdingType = HoldingType.valueOf(rs.getString("holding_type"));
        this.childChain = ChildChain.getChildChain(rs.getInt("child_chain_id"));
        this.minHeight = rs.getInt("min_height");
        this.actualHeight = rs.getInt("actual_height");
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO holding_migrate "
                + "(holding_id, holding_type, child_chain_id, min_height, actual_height, height, latest) "
                + "KEY (child_chain_id, height) VALUES (?, ?, ?, ?, ?, ?, TRUE)")) {
            int i = 0;
            pstmt.setLong(++i, holdingId);
            pstmt.setString(++i, holdingType.name());
            pstmt.setInt(++i, childChain.getId());
            pstmt.setInt(++i, minHeight);
            pstmt.setInt(++i, actualHeight);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.executeUpdate();
        }
    }

    static HoldingMigration getByChildChain(ChildChain childChain) {
        return DB_TABLE.get(DB_KEY_FACTORY.newKey(childChain.getId()));
    }

    static HoldingMigration getByHoldingId(long holdingId, HoldingType holdingType) {
        return DB_TABLE.getBy(new DbClause.LongClause("holding_id", holdingId)
                .and(new DbClause.StringClause("holding_type", holdingType.name())));
    }

    static void insert(HoldingMigration target) {
        DB_TABLE.insert(target);
    }

    static DbIterator<HoldingMigration> getAll() {
        return DB_TABLE.getAll(0, -1);
    }

    static DbIterator<HoldingMigration> getMigrations(int height) {
        DbClause clause = new DbClause.IntClause("actual_height", height);
        return DB_TABLE.getManyBy(clause, -1, -1);
    }

    void setActualHeight(int actualHeight) {
        this.actualHeight = actualHeight;
    }

    long getHoldingId() {
        return holdingId;
    }

    ChildChain getChildChain() {
        return childChain;
    }

    int getMinHeight() {
        return minHeight;
    }

    HoldingType getHoldingType() {
        return holdingType;
    }

    int getActualHeight() {
        return actualHeight;
    }

    @Override
    public String toString() {
        return "HoldingMigration{" +
                "holdingId=" + holdingId +
                ", holdingType=" + holdingType +
                ", childChain=" + childChain.getId() +
                ", minHeight=" + minHeight +
                ", actualHeight=" + actualHeight +
                '}';
    }

}
