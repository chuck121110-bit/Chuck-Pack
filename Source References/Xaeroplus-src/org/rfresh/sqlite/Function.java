package org.rfresh.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import org.rfresh.sqlite.core.DB;

public abstract class Function {
   public static final int FLAG_DETERMINISTIC = 2048;
   private SQLiteConnection conn;
   private DB db;
   long context = 0L;
   long value = 0L;
   int args = 0;

   public static void create(Connection conn, String name, Function f) throws SQLException {
      create(conn, name, f, 0);
   }

   public static void create(Connection conn, String name, Function f, int flags) throws SQLException {
      create(conn, name, f, -1, flags);
   }

   public static void create(Connection conn, String name, Function f, int nArgs, int flags) throws SQLException {
      if (!(conn instanceof SQLiteConnection)) {
         throw new SQLException("connection must be to an SQLite db");
      } else if (conn.isClosed()) {
         throw new SQLException("connection closed");
      } else {
         f.conn = (SQLiteConnection)conn;
         f.db = f.conn.getDatabase();
         if (nArgs >= -1 && nArgs <= 127) {
            if (f.db.create_function(name, f, nArgs, flags) != 0) {
               throw new SQLException("error creating function");
            }
         } else {
            throw new SQLException("invalid args provided: " + nArgs);
         }
      }
   }

   public static void destroy(Connection conn, String name, int nArgs) throws SQLException {
      if (!(conn instanceof SQLiteConnection)) {
         throw new SQLException("connection must be to an SQLite db");
      } else {
         ((SQLiteConnection)conn).getDatabase().destroy_function(name);
      }
   }

   public static void destroy(Connection conn, String name) throws SQLException {
      destroy(conn, name, -1);
   }

   protected abstract void xFunc() throws SQLException;

   protected final synchronized int args() throws SQLException {
      this.checkContext();
      return this.args;
   }

   protected final synchronized void result(byte[] value) throws SQLException {
      this.checkContext();
      this.db.result_blob(this.context, value);
   }

   protected final synchronized void result(double value) throws SQLException {
      this.checkContext();
      this.db.result_double(this.context, value);
   }

   protected final synchronized void result(int value) throws SQLException {
      this.checkContext();
      this.db.result_int(this.context, value);
   }

   protected final synchronized void result(long value) throws SQLException {
      this.checkContext();
      this.db.result_long(this.context, value);
   }

   protected final synchronized void result() throws SQLException {
      this.checkContext();
      this.db.result_null(this.context);
   }

   protected final synchronized void result(String value) throws SQLException {
      this.checkContext();
      this.db.result_text(this.context, value);
   }

   protected final synchronized void error(String err) throws SQLException {
      this.checkContext();
      this.db.result_error(this.context, err);
   }

   protected final synchronized String value_text(int arg) throws SQLException {
      this.checkValue(arg);
      return this.db.value_text(this, arg);
   }

   protected final synchronized byte[] value_blob(int arg) throws SQLException {
      this.checkValue(arg);
      return this.db.value_blob(this, arg);
   }

   protected final synchronized double value_double(int arg) throws SQLException {
      this.checkValue(arg);
      return this.db.value_double(this, arg);
   }

   protected final synchronized int value_int(int arg) throws SQLException {
      this.checkValue(arg);
      return this.db.value_int(this, arg);
   }

   protected final synchronized long value_long(int arg) throws SQLException {
      this.checkValue(arg);
      return this.db.value_long(this, arg);
   }

   protected final synchronized int value_type(int arg) throws SQLException {
      this.checkValue(arg);
      return this.db.value_type(this, arg);
   }

   private void checkContext() throws SQLException {
      if (this.conn == null || this.conn.getDatabase() == null || this.context == 0L) {
         throw new SQLException("no context, not allowed to read value");
      }
   }

   private void checkValue(int arg) throws SQLException {
      if (this.conn != null && this.conn.getDatabase() != null && this.value != 0L) {
         if (arg >= this.args) {
            throw new SQLException("arg " + arg + " out bounds [0," + this.args + ")");
         }
      } else {
         throw new SQLException("not in value access state");
      }
   }

   public abstract static class Aggregate extends Function implements Cloneable {
      protected final void xFunc() {
      }

      protected abstract void xStep() throws SQLException;

      protected abstract void xFinal() throws SQLException;

      public Object clone() throws CloneNotSupportedException {
         return super.clone();
      }
   }

   public abstract static class Window extends Aggregate {
      protected abstract void xInverse() throws SQLException;

      protected abstract void xValue() throws SQLException;
   }
}
