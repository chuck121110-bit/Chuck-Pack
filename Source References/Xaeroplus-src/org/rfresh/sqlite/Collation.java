package org.rfresh.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import org.rfresh.sqlite.core.DB;

public abstract class Collation {
   private SQLiteConnection conn;
   private DB db;

   public static final void create(Connection conn, String name, Collation f) throws SQLException {
      if (conn != null && conn instanceof SQLiteConnection) {
         if (conn.isClosed()) {
            throw new SQLException("connection closed");
         } else {
            f.conn = (SQLiteConnection)conn;
            f.db = f.conn.getDatabase();
            if (f.db.create_collation(name, f) != 0) {
               throw new SQLException("error creating collation");
            }
         }
      } else {
         throw new SQLException("connection must be to an SQLite db");
      }
   }

   public static final void destroy(Connection conn, String name) throws SQLException {
      if (conn != null && conn instanceof SQLiteConnection) {
         ((SQLiteConnection)conn).getDatabase().destroy_collation(name);
      } else {
         throw new SQLException("connection must be to an SQLite db");
      }
   }

   protected abstract int xCompare(String var1, String var2);
}
