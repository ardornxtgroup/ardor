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

package nxt.dbschema;

import nxt.Constants;
import nxt.Nxt;
import nxt.blockchain.ChildChain;
import nxt.db.BasicDb;
import nxt.db.DbVersion;
import nxt.db.TransactionalDb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class Db {

    public static final String PREFIX = Constants.isTestnet ? "nxt.testDb" : "nxt.db";
    public static final TransactionalDb db = new TransactionalDb(new BasicDb.DbProperties()
            .maxCacheSize(Nxt.getIntProperty("nxt.dbCacheKB"))
            .dbUrl(Nxt.getStringProperty(PREFIX + "Url"))
            .dbType(Nxt.getStringProperty(PREFIX + "Type"))
            .dbDir(Nxt.getStringProperty(PREFIX + "Dir"))
            .dbParams(Nxt.getStringProperty(PREFIX + "Params"))
            .dbUsername(Nxt.getStringProperty(PREFIX + "Username"))
            .dbPassword(Nxt.getStringProperty(PREFIX + "Password", null, true))
            .maxConnections(Nxt.getIntProperty("nxt.maxDbConnections"))
            .loginTimeout(Nxt.getIntProperty("nxt.dbLoginTimeout"))
            .defaultLockTimeout(Nxt.getIntProperty("nxt.dbDefaultLockTimeout") * 1000)
            .maxMemoryRows(Nxt.getIntProperty("nxt.dbMaxMemoryRows"))
    );

    public static Connection getConnection() throws SQLException {
        return db.getConnection("PUBLIC");
    }

    public static void init() {
        Init.init();
    }

    private static class Init {
        private static void init() {}
        static {
            List<DbVersion> dbVersions = new ArrayList<>();
            dbVersions.add(new FxtDbVersion(db));
            ChildChain.getAll().forEach(childchain -> dbVersions.add(new ChildDbVersion(db, childchain.getName())));
            db.init(dbVersions);
        }
    }

    public static void shutdown() {
        db.shutdown();
    }

    private Db() {} // never

}
