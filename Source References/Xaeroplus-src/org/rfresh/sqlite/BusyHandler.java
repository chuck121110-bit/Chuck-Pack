package org.rfresh.sqlite;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class BusyHandler {
   private static void commitHandler(Connection conn, BusyHandler busyHandler) throws SQLException {
      if (!(conn instanceof SQLiteConnection)) {
         throw new SQLException("connection must be to an SQLite db");
      } else if (conn.isClosed()) {
         throw new SQLException("connection closed");
      } else {
         SQLiteConnection sqliteConnection = (SQLiteConnection)conn;
         sqliteConnection.getDatabase().busy_handler(busyHandler);
      }
   }

   public static final void setHandler(Connection conn, BusyHandler busyHandler) throws SQLException {
      commitHandler(conn, busyHandler);
   }

   public static final void clearHandler(Connection conn) throws SQLException {
      commitHandler(conn, (BusyHandler)null);
   }

   protected abstract int callback(int var1) throws SQLException;
}
