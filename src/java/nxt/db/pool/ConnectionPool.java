package nxt.db.pool;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionPool {

    void initialize(String dbUrl, String dbUsername, String dbPassword, int maxConnections, int loginTimeout);

    Connection getConnection() throws SQLException;
}
