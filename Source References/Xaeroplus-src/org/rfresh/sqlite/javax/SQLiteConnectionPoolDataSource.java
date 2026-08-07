package org.rfresh.sqlite.javax;

import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import org.rfresh.sqlite.SQLiteConfig;
import org.rfresh.sqlite.SQLiteDataSource;

public class SQLiteConnectionPoolDataSource extends SQLiteDataSource implements ConnectionPoolDataSource {
   public SQLiteConnectionPoolDataSource() {
   }

   public SQLiteConnectionPoolDataSource(SQLiteConfig config) {
      super(config);
   }

   public PooledConnection getPooledConnection() throws SQLException {
      return this.getPooledConnection((String)null, (String)null);
   }

   public PooledConnection getPooledConnection(String user, String password) throws SQLException {
      return new SQLitePooledConnection(this.getConnection(user, password));
   }
}
