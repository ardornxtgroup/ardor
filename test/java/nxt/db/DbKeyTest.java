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

package nxt.db;

import org.junit.Test;

import static nxt.db.DbKey.SmartFactory.pkClause;
import static nxt.db.DbKey.SmartFactory.pkColumns;
import static nxt.db.DbKey.SmartFactory.selfJoinClause;
import static org.junit.Assert.assertEquals;

public class DbKeyTest {

    @Test
    public void testSmartFactorySelfJoinClause() {
        assertEquals("a.col1 = b.col1", selfJoinClause("col1"));
        assertEquals("a.col1 = b.col1 AND a.col2 = b.col2", selfJoinClause("col1", "col2"));
    }

    @Test
    public void testSmartFactoryPkColumns() {
        assertEquals("col1", pkColumns("col1"));
        assertEquals("col1, col2", pkColumns("col1", "col2"));
    }

    @Test
    public void testSmartFactoryPkClause() {
        assertEquals(" WHERE col1 = ? ", pkClause("col1"));
        assertEquals(" WHERE col1 = ? AND col2 = ? ", pkClause("col1", "col2"));
    }
}