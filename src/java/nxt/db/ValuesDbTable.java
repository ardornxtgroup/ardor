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

package nxt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static nxt.db.EntityDbTable.LATEST;

public abstract class ValuesDbTable<T,V> extends DerivedDbTable {

    private final boolean multiversion;
    protected final DbKey.Factory<T> dbKeyFactory;

    protected ValuesDbTable(String schemaTable, DbKey.Factory<T> dbKeyFactory) {
        this(schemaTable, dbKeyFactory, false);
    }

    ValuesDbTable(String schemaTable, DbKey.Factory<T> dbKeyFactory, boolean multiversion) {
        super(schemaTable);
        this.dbKeyFactory = dbKeyFactory;
        this.multiversion = multiversion;
    }

    protected abstract V load(Connection con, ResultSet rs) throws SQLException;

    protected abstract void save(Connection con, T t, V v) throws SQLException;

    protected void clearCache() {
        db.clearCache(schemaTable);
    }

    public final List<V> get(DbKey dbKey) {
        List<V> values;
        if (db.isInTransaction()) {
            values = (List<V>) db.getCache(schemaTable).get(dbKey);
            if (values != null) {
                return values;
            }
        }
        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM " + schemaTable + dbKeyFactory.getPKClause()
                     + (multiversion ? " AND latest = TRUE" : "") + " ORDER BY db_id")) {
            dbKey.setPK(pstmt);
            values = get(con, pstmt);
            if (db.isInTransaction()) {
                db.getCache(schemaTable).put(dbKey, values);
            }
            return values;
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    private List<V> get(Connection con, PreparedStatement pstmt) {
        try {
            List<V> result = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(load(con, rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    @Override
    public final int getCount() {
        return multiversion ? super.getCount(LATEST) : super.getCount();
    }

    @Override
    public final int getCount(DbClause dbClause) {
        return multiversion ? super.getCount(dbClause.and(LATEST)) : super.getCount(dbClause);
    }

    public final void insert(T t, List<V> values) {
        if (!db.isInTransaction()) {
            throw new IllegalStateException("Not in transaction");
        }
        DbKey dbKey = dbKeyFactory.newKey(t);
        if (dbKey == null) {
            throw new RuntimeException("DbKey not set");
        }
        db.getCache(schemaTable).put(dbKey, values);
        try (Connection con = getConnection()) {
            if (multiversion) {
                try (PreparedStatement pstmt = con.prepareStatement("UPDATE " + schemaTable
                        + " SET latest = FALSE " + dbKeyFactory.getPKClause() + " AND latest = TRUE")) {
                    dbKey.setPK(pstmt);
                    pstmt.executeUpdate();
                }
            }
            for (V v : values) {
                save(con, t, v);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    @Override
    public final void popOffTo(int height) {
        if (multiversion) {
            VersionedEntityDbTable.popOff(db, schema, schemaTable, height, dbKeyFactory);
        } else {
            super.popOffTo(height);
        }
    }

    @Override
    public final void trim(int height) {
        if (multiversion) {
            VersionedEntityDbTable.trim(db, schema, schemaTable, height, dbKeyFactory);
        } else {
            super.trim(height);
        }
    }

}
