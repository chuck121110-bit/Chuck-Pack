package org.rfresh.sqlite.jdbc4;

import java.sql.SQLException;
import java.sql.Statement;
import org.rfresh.sqlite.SQLiteConnection;
import org.rfresh.sqlite.jdbc3.JDBC3Statement;

public class JDBC4Statement extends JDBC3Statement implements Statement {
   private boolean closed = false;
   boolean closeOnCompletion;

   public JDBC4Statement(SQLiteConnection conn) {
      super(conn);
   }

   public <T> T unwrap(Class<T> iface) throws ClassCastException {
      return (T)iface.cast(this);
   }

   public boolean isWrapperFor(Class<?> iface) {
      return iface.isInstance(this);
   }

   public void close() throws SQLException {
      super.close();
      this.closed = true;
   }

   public boolean isClosed() {
      return this.closed;
   }

   public void closeOnCompletion() throws SQLException {
      if (this.closed) {
         throw new SQLException("statement is closed");
      } else {
         this.closeOnCompletion = true;
      }
   }

   public boolean isCloseOnCompletion() throws SQLException {
      if (this.closed) {
         throw new SQLException("statement is closed");
      } else {
         return this.closeOnCompletion;
      }
   }

   public void setPoolable(boolean poolable) throws SQLException {
   }

   public boolean isPoolable() throws SQLException {
      return false;
   }
}
