package org.rfresh.sqlite;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class ProgressHandler {
   public static final void setHandler(Connection conn, int vmCalls, ProgressHandler progressHandler) throws SQLException {
      if (!(conn instanceof SQLiteConnection)) {
         throw new SQLException("connection must be to an SQLite db");
      } else if (conn.isClosed()) {
         throw new SQLException("connection closed");
      } else {
         SQLiteConnection sqliteConnection = (SQLiteConnection)conn;
         sqliteConnection.getDatabase().register_progress_handler(vmCalls, progressHandler);
      }
   }

   public static final void clearHandler(Connection conn) throws SQLException {
      SQLiteConnection sqliteConnection = (SQLiteConnection)conn;
      sqliteConnection.getDatabase().clear_progress_handler();
   }

   protected abstract int progress() throws SQLException;
}
