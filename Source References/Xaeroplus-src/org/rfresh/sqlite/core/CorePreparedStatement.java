package org.rfresh.sqlite.core;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import org.rfresh.sqlite.SQLiteConnection;
import org.rfresh.sqlite.SQLiteConnectionConfig;
import org.rfresh.sqlite.date.FastDateFormat;
import org.rfresh.sqlite.jdbc3.JDBC3Connection;
import org.rfresh.sqlite.jdbc4.JDBC4Statement;

public abstract class CorePreparedStatement extends JDBC4Statement {
   protected int columnCount;
   protected int paramCount;
   protected int batchQueryCount;

   protected CorePreparedStatement(SQLiteConnection conn, String sql) throws SQLException {
      super(conn);
      this.sql = sql;
      DB db = conn.getDatabase();
      db.prepare((CoreStatement)this);
      this.rs.colsMeta = (String[])this.pointer.safeRun(DB::column_names);
      this.columnCount = this.pointer.safeRunInt(DB::column_count);
      this.paramCount = this.pointer.safeRunInt(DB::bind_parameter_count);
      this.batchQueryCount = 0;
      this.batch = null;
      this.batchPos = 0;
   }

   public int[] executeBatch() throws SQLException {
      return Arrays.stream(this.executeLargeBatch()).mapToInt((l) -> (int)l).toArray();
   }

   public long[] executeLargeBatch() throws SQLException {
      if (this.batchQueryCount == 0) {
         return new long[0];
      } else {
         if (this.conn instanceof JDBC3Connection) {
            ((JDBC3Connection)this.conn).tryEnforceTransactionMode();
         }

         return (long[])this.withConnectionTimeout(() -> {
            long[] var1;
            try {
               var1 = this.conn.getDatabase().executeBatch(this.pointer, this.batchQueryCount, this.batch, this.conn.getAutoCommit());
            } finally {
               this.clearBatch();
            }

            return var1;
         });
      }
   }

   public void clearBatch() throws SQLException {
      super.clearBatch();
      this.batchQueryCount = 0;
   }

   protected void batch(int pos, Object value) throws SQLException {
      this.checkOpen();
      if (this.batch == null) {
         this.batch = new Object[this.paramCount];
      }

      this.batch[this.batchPos + pos - 1] = value;
   }

   protected void setDateByMilliseconds(int pos, Long value, Calendar calendar) throws SQLException {
      SQLiteConnectionConfig config = this.conn.getConnectionConfig();
      switch (config.getDateClass()) {
         case TEXT:
            this.batch(pos, FastDateFormat.getInstance(config.getDateStringFormat(), calendar.getTimeZone()).format((Date)(new java.sql.Date(value))));
            break;
         case REAL:
            this.batch(pos, new Double((double)value / (double)8.64E7F + (double)2440587.5F));
            break;
         default:
            this.batch(pos, new Long(value / config.getDateMultiplier()));
      }

   }
}
