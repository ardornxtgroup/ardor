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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Create Statement and PrepareStatement for use with FilteredConnection
 */
public interface FilteredFactory {

    /**
     * Create a FilteredStatement for the supplied Statement
     *
     * @param   stmt                Statement
     * @param   con                 Connection
     * @return                      Wrapped statement
     * @throws  SQLException        SQLException
     */
    Statement createStatement(FilteredConnection con, Statement stmt) throws SQLException;

    /**
     * Create a FilteredPreparedStatement for the supplied PreparedStatement
     *
     * @param   stmt                Prepared statement
     * @param   sql                 SQL statement
     * @param   con                 Connection
     * @throws  SQLException        SQLException
     * @return                      Wrapped prepared statement
     */
    PreparedStatement createPreparedStatement(FilteredConnection con, PreparedStatement stmt, String sql) throws SQLException;
}
