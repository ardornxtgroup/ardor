package nxt.db.pool;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

import nxt.util.Logger;

public class H2ConnectionPool implements ConnectionPool {
    private JdbcConnectionPool wrappedPool;
    private volatile int maxActiveConnections;

    @Override
    public void initialize(String dbUrl, String dbUsername, String dbPassword, int maxConnections, int loginTimeout) {
        wrappedPool = JdbcConnectionPool.create(dbUrl, dbUsername, dbPassword);
        wrappedPool.setMaxConnections(maxConnections);
        wrappedPool.setLoginTimeout(loginTimeout);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection con = wrappedPool.getConnection();
        int activeConnections = wrappedPool.getActiveConnections();
        if (activeConnections > maxActiveConnections) {
            maxActiveConnections = activeConnections;
            Logger.logDebugMessage("Database connection pool current size: " + activeConnections);
        }
        return con;
    }
}
