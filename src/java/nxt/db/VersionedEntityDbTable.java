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

package nxt.db;


import nxt.Constants;
import nxt.Nxt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class VersionedEntityDbTable<T> extends EntityDbTable<T> {

    protected VersionedEntityDbTable(String schemaTable, DbKey.Factory<T> dbKeyFactory) {
        super(schemaTable, dbKeyFactory, true, null);
    }

    protected VersionedEntityDbTable(String schemaTable, DbKey.Factory<T> dbKeyFactory, String fullTextSearchColumns) {
        super(schemaTable, dbKeyFactory, true, fullTextSearchColumns);
    }

    public final boolean delete(T t) {
        return delete(t, false);
    }

    public final boolean delete(T t, boolean keepInCache) {
        if (t == null) {
            return false;
        }
        if (!db.isInTransaction()) {
            throw new IllegalStateException("Not in transaction");
        }
        DbKey dbKey = dbKeyFactory.newKey(t);
        try (Connection con = getConnection();
             PreparedStatement pstmtCount = con.prepareStatement("SELECT 1 FROM " + schemaTable
                     + dbKeyFactory.getPKClause() + " AND height < ? LIMIT 1")) {
            int i = dbKey.setPK(pstmtCount);
            pstmtCount.setInt(i, Nxt.getBlockchain().getHeight());
            try (ResultSet rs = pstmtCount.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement pstmt = con.prepareStatement("UPDATE " + schemaTable
                            + " SET latest = FALSE " + dbKeyFactory.getPKClause() + " AND latest = TRUE LIMIT 1")) {
                        dbKey.setPK(pstmt);
                        pstmt.executeUpdate();
                        save(con, t);
                        pstmt.executeUpdate(); // delete after the save
                    }
                    return true;
                } else {
                    try (PreparedStatement pstmtDelete = con.prepareStatement("DELETE FROM " + schemaTable + dbKeyFactory.getPKClause())) {
                        dbKey.setPK(pstmtDelete);
                        return pstmtDelete.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        } finally {
            if (!keepInCache) {
                db.getCache(schemaTable).remove(dbKey);
            }
        }
    }

    static void popOff(final TransactionalDb db, final String schema, final String schemaTable, final int height, final DbKey.Factory dbKeyFactory) {
        if (!db.isInTransaction()) {
            throw new IllegalStateException("Not in transaction");
        }
        try (Connection con = db.getConnection(schema);
             PreparedStatement pstmtSelectToDelete = con.prepareStatement("SELECT DISTINCT " + dbKeyFactory.getPKColumns()
                     + " FROM " + schemaTable + " WHERE height > ?");
             PreparedStatement pstmtDelete = con.prepareStatement("DELETE FROM " + schemaTable
                     + " WHERE height > ?");
             PreparedStatement pstmtSetLatest = con.prepareStatement("UPDATE " + schemaTable
                     + " SET latest = TRUE " + dbKeyFactory.getPKClause() + " AND height ="
                     + " (SELECT MAX(height) FROM " + schemaTable + dbKeyFactory.getPKClause() + ")")) {
            pstmtSelectToDelete.setInt(1, height);
            List<DbKey> dbKeys = new ArrayList<>();
            try (ResultSet rs = pstmtSelectToDelete.executeQuery()) {
                while (rs.next()) {
                    dbKeys.add(dbKeyFactory.newKey(rs));
                }
            }
            /*
            if (dbKeys.size() > 0 && Logger.isDebugEnabled()) {
                Logger.logDebugMessage(String.format("rollback table %s found %d records to update to latest", table, dbKeys.size()));
            }
            */
            pstmtDelete.setInt(1, height);
            int deletedRecordsCount = pstmtDelete.executeUpdate();
            /*
            if (deletedRecordsCount > 0 && Logger.isDebugEnabled()) {
                Logger.logDebugMessage(String.format("rollback table %s deleting %d records", table, deletedRecordsCount));
            }
            */
            for (DbKey dbKey : dbKeys) {
                int i = 1;
                i = dbKey.setPK(pstmtSetLatest, i);
                i = dbKey.setPK(pstmtSetLatest, i);
                pstmtSetLatest.executeUpdate();
                //Db.getCache(schemaTable).remove(dbKey);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static void trim(final TransactionalDb db, final String schema, final String schemaTable, final int height, final DbKey.Factory dbKeyFactory) {
        if (!db.isInTransaction()) {
            throw new IllegalStateException("Not in transaction");
        }
        try (Connection con = db.getConnection(schema);
             PreparedStatement pstmtSelect = con.prepareStatement("SELECT " + dbKeyFactory.getPKColumns() + ", MAX(height) AS max_height"
                     + " FROM " + schemaTable + " WHERE height < ? GROUP BY " + dbKeyFactory.getPKColumns() + " HAVING COUNT(DISTINCT height) > 1");
             PreparedStatement pstmtDelete = con.prepareStatement("DELETE FROM " + schemaTable + dbKeyFactory.getPKClause()
                     + " AND height < ? AND height >= 0 LIMIT " + Constants.BATCH_COMMIT_SIZE);
             PreparedStatement pstmtDeleteDeleted = con.prepareStatement("DELETE FROM " + schemaTable + " WHERE height < ? AND height >= 0 AND latest = FALSE "
                     + " AND (" + dbKeyFactory.getPKColumns() + ") NOT IN (SELECT (" + dbKeyFactory.getPKColumns() + ") FROM "
                     + schemaTable + " WHERE height >= ?) LIMIT " + Constants.BATCH_COMMIT_SIZE)) {
            pstmtSelect.setInt(1, height);
            try (ResultSet rs = pstmtSelect.executeQuery()) {
                int count = 0;
                int deleted;
                while (rs.next()) {
                    DbKey dbKey = dbKeyFactory.newKey(rs);
                    int i = 1;
                    i = dbKey.setPK(pstmtDelete, i);
                    pstmtDelete.setInt(i, rs.getInt("max_height"));
                    do {
                        deleted = pstmtDelete.executeUpdate();
                        if ((count += deleted) >= Constants.BATCH_COMMIT_SIZE) {
                            db.commitTransaction();
                            count = 0;
                        }
                    } while (deleted >= Constants.BATCH_COMMIT_SIZE);
                }
                pstmtDeleteDeleted.setInt(1, height);
                pstmtDeleteDeleted.setInt(2, height);
                do {
                    deleted = pstmtDeleteDeleted.executeUpdate();
                    db.commitTransaction();
                } while (deleted >= Constants.BATCH_COMMIT_SIZE);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

}
