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

import nxt.Nxt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class DerivedDbTable extends Table {

    protected DerivedDbTable(String schemaTable) {
        super(schemaTable);
        Nxt.getBlockchainProcessor().registerDerivedTable(this);
    }

    public void popOffTo(int height) {
        if (!db.isInTransaction()) {
            throw new IllegalStateException("Not in transaction");
        }
        try (Connection con = getConnection();
             PreparedStatement pstmtDelete = con.prepareStatement("DELETE FROM " + schemaTable + " WHERE height > ?")) {
            pstmtDelete.setInt(1, height);
            pstmtDelete.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public void rollback(int height) {
        popOffTo(height);
    }

    public void trim(int height) {
        //nothing to trim
    }

    public void createSearchIndex(Connection con) throws SQLException {
        //implemented in EntityDbTable only
    }

    public boolean isPersistent() {
        return false;
    }

}
