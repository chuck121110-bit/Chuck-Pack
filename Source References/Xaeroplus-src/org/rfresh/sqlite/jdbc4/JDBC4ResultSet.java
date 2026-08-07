package org.rfresh.sqlite.jdbc4;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import org.rfresh.sqlite.core.CoreStatement;
import org.rfresh.sqlite.jdbc3.JDBC3ResultSet;

public class JDBC4ResultSet extends JDBC3ResultSet implements ResultSet, ResultSetMetaData {
   public JDBC4ResultSet(CoreStatement stmt) {
      super(stmt);
   }

   public void close() throws SQLException {
      boolean wasOpen = this.isOpen();
      super.close();
      if (wasOpen && this.stmt instanceof JDBC4Statement) {
         JDBC4Statement stat = (JDBC4Statement)this.stmt;
         if (stat.closeOnCompletion && !stat.isClosed()) {
            stat.close();
         }
      }

   }

   public <T> T unwrap(Class<T> iface) throws ClassCastException {
      return (T)iface.cast(this);
   }

   public boolean isWrapperFor(Class<?> iface) {
      return iface.isInstance(this);
   }

   public RowId getRowId(int columnIndex) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public RowId getRowId(String columnLabel) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateRowId(int columnIndex, RowId x) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateRowId(String columnLabel, RowId x) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public int getHoldability() throws SQLException {
      return 0;
   }

   public boolean isClosed() throws SQLException {
      return !this.isOpen();
   }

   public void updateNString(int columnIndex, String nString) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateNString(String columnLabel, String nString) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public NClob getNClob(int columnIndex) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public NClob getNClob(String columnLabel) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public SQLXML getSQLXML(int columnIndex) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public SQLXML getSQLXML(String columnLabel) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public String getNString(int columnIndex) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public String getNString(String columnLabel) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public Reader getNCharacterStream(int col) throws SQLException {
      String data = this.getString(col);
      return this.getNCharacterStreamInternal(data);
   }

   private Reader getNCharacterStreamInternal(String data) {
      if (data == null) {
         return null;
      } else {
         Reader reader = new StringReader(data);
         return reader;
      }
   }

   public Reader getNCharacterStream(String col) throws SQLException {
      String data = this.getString(col);
      return this.getNCharacterStreamInternal(data);
   }

   public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateClob(int columnIndex, Reader reader) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateClob(String columnLabel, Reader reader) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateNClob(int columnIndex, Reader reader) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public void updateNClob(String columnLabel, Reader reader) throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
      if (type == null) {
         throw new SQLException("requested type cannot be null");
      } else if (type == String.class) {
         return (T)type.cast(this.getString(columnIndex));
      } else if (type == Boolean.class) {
         return (T)type.cast(this.getBoolean(columnIndex));
      } else if (type == BigDecimal.class) {
         return (T)type.cast(this.getBigDecimal(columnIndex));
      } else if (type == byte[].class) {
         return (T)type.cast(this.getBytes(columnIndex));
      } else if (type == Date.class) {
         return (T)type.cast(this.getDate(columnIndex));
      } else if (type == Time.class) {
         return (T)type.cast(this.getTime(columnIndex));
      } else if (type == Timestamp.class) {
         return (T)type.cast(this.getTimestamp(columnIndex));
      } else if (type == LocalDate.class) {
         try {
            Date date = this.getDate(columnIndex);
            return (T)(date != null ? type.cast(LocalDate.of(date.getYear() + 1900, date.getMonth() + 1, date.getDate())) : null);
         } catch (SQLException var4) {
            return (T)type.cast(LocalDate.parse(this.getString(columnIndex)));
         }
      } else if (type == LocalTime.class) {
         try {
            Time time = this.getTime(columnIndex);
            return (T)(time != null ? type.cast(LocalTime.of(time.getHours(), time.getMinutes(), time.getSeconds())) : null);
         } catch (SQLException var5) {
            return (T)type.cast(LocalTime.parse(this.getString(columnIndex)));
         }
      } else if (type == LocalDateTime.class) {
         try {
            Timestamp timestamp = this.getTimestamp(columnIndex);
            return (T)(timestamp != null ? type.cast(LocalDateTime.of(timestamp.getYear() + 1900, timestamp.getMonth() + 1, timestamp.getDate(), timestamp.getHours(), timestamp.getMinutes(), timestamp.getSeconds(), timestamp.getNanos())) : null);
         } catch (SQLException var6) {
            return (T)type.cast(LocalDateTime.parse(this.getString(columnIndex)));
         }
      } else {
         int columnType = this.safeGetColumnType(this.markCol(columnIndex));
         if (type == Double.class) {
            if (columnType != 1 && columnType != 2) {
               throw new SQLException("Bad value for type Double");
            } else {
               return (T)type.cast(this.getDouble(columnIndex));
            }
         } else if (type == Long.class) {
            if (columnType != 1 && columnType != 2) {
               throw new SQLException("Bad value for type Long");
            } else {
               return (T)type.cast(this.getLong(columnIndex));
            }
         } else if (type == Float.class) {
            if (columnType != 1 && columnType != 2) {
               throw new SQLException("Bad value for type Float");
            } else {
               return (T)type.cast(this.getFloat(columnIndex));
            }
         } else if (type == Integer.class) {
            if (columnType != 1 && columnType != 2) {
               throw new SQLException("Bad value for type Integer");
            } else {
               return (T)type.cast(this.getInt(columnIndex));
            }
         } else {
            throw this.unsupported();
         }
      }
   }

   public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
      return (T)this.getObject(this.findColumn(columnLabel), type);
   }

   protected SQLException unsupported() {
      return new SQLFeatureNotSupportedException("not implemented by SQLite JDBC driver");
   }

   public Array getArray(int i) throws SQLException {
      throw this.unsupported();
   }

   public Array getArray(String col) throws SQLException {
      throw this.unsupported();
   }

   public InputStream getAsciiStream(int col) throws SQLException {
      String data = this.getString(col);
      return this.getAsciiStreamInternal(data);
   }

   public InputStream getAsciiStream(String col) throws SQLException {
      String data = this.getString(col);
      return this.getAsciiStreamInternal(data);
   }

   private InputStream getAsciiStreamInternal(String data) {
      if (data == null) {
         return null;
      } else {
         try {
            InputStream inputStream = new ByteArrayInputStream(data.getBytes("ASCII"));
            return inputStream;
         } catch (UnsupportedEncodingException var4) {
            return null;
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public BigDecimal getBigDecimal(int col, int s) throws SQLException {
      throw this.unsupported();
   }

   /** @deprecated */
   @Deprecated
   public BigDecimal getBigDecimal(String col, int s) throws SQLException {
      throw this.unsupported();
   }

   public Blob getBlob(int col) throws SQLException {
      throw this.unsupported();
   }

   public Blob getBlob(String col) throws SQLException {
      throw this.unsupported();
   }

   public Clob getClob(int col) throws SQLException {
      String clob = this.getString(col);
      return clob == null ? null : new SqliteClob(this, clob);
   }

   public Clob getClob(String col) throws SQLException {
      String clob = this.getString(col);
      return clob == null ? null : new SqliteClob(this, clob);
   }

   public Object getObject(int col, Map map) throws SQLException {
      throw this.unsupported();
   }

   public Object getObject(String col, Map map) throws SQLException {
      throw this.unsupported();
   }

   public Ref getRef(int i) throws SQLException {
      throw this.unsupported();
   }

   public Ref getRef(String col) throws SQLException {
      throw this.unsupported();
   }

   public InputStream getUnicodeStream(int col) throws SQLException {
      return this.getAsciiStream(col);
   }

   public InputStream getUnicodeStream(String col) throws SQLException {
      return this.getAsciiStream(col);
   }

   public URL getURL(int col) throws SQLException {
      throw this.unsupported();
   }

   public URL getURL(String col) throws SQLException {
      throw this.unsupported();
   }

   public void insertRow() throws SQLException {
      throw new SQLException("ResultSet is TYPE_FORWARD_ONLY");
   }

   public void moveToCurrentRow() throws SQLException {
      throw new SQLException("ResultSet is TYPE_FORWARD_ONLY");
   }

   public void moveToInsertRow() throws SQLException {
      throw new SQLException("ResultSet is TYPE_FORWARD_ONLY");
   }

   public boolean last() throws SQLException {
      throw new SQLException("ResultSet is TYPE_FORWARD_ONLY");
   }

   public boolean previous() throws SQLException {
      throw new SQLException("ResultSet is TYPE_FORWARD_ONLY");
   }

   public boolean relative(int rows) throws SQLException {
      throw new SQLException("ResultSet is TYPE_FORWARD_ONLY");
   }

   public boolean absolute(int row) throws SQLException {
      throw new SQLException("ResultSet is TYPE_FORWARD_ONLY");
   }

   public void afterLast() throws SQLException {
      throw new SQLException("ResultSet is TYPE_FORWARD_ONLY");
   }

   public void beforeFirst() throws SQLException {
      throw new SQLException("ResultSet is TYPE_FORWARD_ONLY");
   }

   public boolean first() throws SQLException {
      throw new SQLException("ResultSet is TYPE_FORWARD_ONLY");
   }

   public void cancelRowUpdates() throws SQLException {
      throw this.unsupported();
   }

   public void deleteRow() throws SQLException {
      throw this.unsupported();
   }

   public void updateArray(int col, Array x) throws SQLException {
      throw this.unsupported();
   }

   public void updateArray(String col, Array x) throws SQLException {
      throw this.unsupported();
   }

   public void updateAsciiStream(int col, InputStream x, int l) throws SQLException {
      throw this.unsupported();
   }

   public void updateAsciiStream(String col, InputStream x, int l) throws SQLException {
      throw this.unsupported();
   }

   public void updateBigDecimal(int col, BigDecimal x) throws SQLException {
      throw this.unsupported();
   }

   public void updateBigDecimal(String col, BigDecimal x) throws SQLException {
      throw this.unsupported();
   }

   public void updateBinaryStream(int c, InputStream x, int l) throws SQLException {
      throw this.unsupported();
   }

   public void updateBinaryStream(String c, InputStream x, int l) throws SQLException {
      throw this.unsupported();
   }

   public void updateBlob(int col, Blob x) throws SQLException {
      throw this.unsupported();
   }

   public void updateBlob(String col, Blob x) throws SQLException {
      throw this.unsupported();
   }

   public void updateBoolean(int col, boolean x) throws SQLException {
      throw this.unsupported();
   }

   public void updateBoolean(String col, boolean x) throws SQLException {
      throw this.unsupported();
   }

   public void updateByte(int col, byte x) throws SQLException {
      throw this.unsupported();
   }

   public void updateByte(String col, byte x) throws SQLException {
      throw this.unsupported();
   }

   public void updateBytes(int col, byte[] x) throws SQLException {
      throw this.unsupported();
   }

   public void updateBytes(String col, byte[] x) throws SQLException {
      throw this.unsupported();
   }

   public void updateCharacterStream(int c, Reader x, int l) throws SQLException {
      throw this.unsupported();
   }

   public void updateCharacterStream(String c, Reader r, int l) throws SQLException {
      throw this.unsupported();
   }

   public void updateClob(int col, Clob x) throws SQLException {
      throw this.unsupported();
   }

   public void updateClob(String col, Clob x) throws SQLException {
      throw this.unsupported();
   }

   public void updateDate(int col, Date x) throws SQLException {
      throw this.unsupported();
   }

   public void updateDate(String col, Date x) throws SQLException {
      throw this.unsupported();
   }

   public void updateDouble(int col, double x) throws SQLException {
      throw this.unsupported();
   }

   public void updateDouble(String col, double x) throws SQLException {
      throw this.unsupported();
   }

   public void updateFloat(int col, float x) throws SQLException {
      throw this.unsupported();
   }

   public void updateFloat(String col, float x) throws SQLException {
      throw this.unsupported();
   }

   public void updateInt(int col, int x) throws SQLException {
      throw this.unsupported();
   }

   public void updateInt(String col, int x) throws SQLException {
      throw this.unsupported();
   }

   public void updateLong(int col, long x) throws SQLException {
      throw this.unsupported();
   }

   public void updateLong(String col, long x) throws SQLException {
      throw this.unsupported();
   }

   public void updateNull(int col) throws SQLException {
      throw this.unsupported();
   }

   public void updateNull(String col) throws SQLException {
      throw this.unsupported();
   }

   public void updateObject(int c, Object x) throws SQLException {
      throw this.unsupported();
   }

   public void updateObject(int c, Object x, int s) throws SQLException {
      throw this.unsupported();
   }

   public void updateObject(String col, Object x) throws SQLException {
      throw this.unsupported();
   }

   public void updateObject(String c, Object x, int s) throws SQLException {
      throw this.unsupported();
   }

   public void updateRef(int col, Ref x) throws SQLException {
      throw this.unsupported();
   }

   public void updateRef(String c, Ref x) throws SQLException {
      throw this.unsupported();
   }

   public void updateRow() throws SQLException {
      throw this.unsupported();
   }

   public void updateShort(int c, short x) throws SQLException {
      throw this.unsupported();
   }

   public void updateShort(String c, short x) throws SQLException {
      throw this.unsupported();
   }

   public void updateString(int c, String x) throws SQLException {
      throw this.unsupported();
   }

   public void updateString(String c, String x) throws SQLException {
      throw this.unsupported();
   }

   public void updateTime(int c, Time x) throws SQLException {
      throw this.unsupported();
   }

   public void updateTime(String c, Time x) throws SQLException {
      throw this.unsupported();
   }

   public void updateTimestamp(int c, Timestamp x) throws SQLException {
      throw this.unsupported();
   }

   public void updateTimestamp(String c, Timestamp x) throws SQLException {
      throw this.unsupported();
   }

   public void refreshRow() throws SQLException {
      throw this.unsupported();
   }

   class SqliteClob implements NClob {
      private String data;

      protected SqliteClob(JDBC4ResultSet this$0, String data) {
         this.data = data;
      }

      public void free() throws SQLException {
         this.data = null;
      }

      public InputStream getAsciiStream() throws SQLException {
         return JDBC4ResultSet.this.getAsciiStreamInternal(this.data);
      }

      public Reader getCharacterStream() throws SQLException {
         return JDBC4ResultSet.this.getNCharacterStreamInternal(this.data);
      }

      public Reader getCharacterStream(long arg0, long arg1) throws SQLException {
         return JDBC4ResultSet.this.getNCharacterStreamInternal(this.data);
      }

      public String getSubString(long position, int length) throws SQLException {
         if (this.data == null) {
            throw new SQLException("no data");
         } else if (position < 1L) {
            throw new SQLException("Position must be greater than or equal to 1");
         } else if (length < 0) {
            throw new SQLException("Length must be greater than or equal to 0");
         } else {
            int start = (int)position - 1;
            return this.data.substring(start, Math.min(start + length, this.data.length()));
         }
      }

      public long length() throws SQLException {
         if (this.data == null) {
            throw new SQLException("no data");
         } else {
            return (long)this.data.length();
         }
      }

      public long position(String arg0, long arg1) throws SQLException {
         JDBC4ResultSet.this.unsupported();
         return -1L;
      }

      public long position(Clob arg0, long arg1) throws SQLException {
         JDBC4ResultSet.this.unsupported();
         return -1L;
      }

      public OutputStream setAsciiStream(long arg0) throws SQLException {
         JDBC4ResultSet.this.unsupported();
         return null;
      }

      public Writer setCharacterStream(long arg0) throws SQLException {
         JDBC4ResultSet.this.unsupported();
         return null;
      }

      public int setString(long arg0, String arg1) throws SQLException {
         JDBC4ResultSet.this.unsupported();
         return -1;
      }

      public int setString(long arg0, String arg1, int arg2, int arg3) throws SQLException {
         JDBC4ResultSet.this.unsupported();
         return -1;
      }

      public void truncate(long arg0) throws SQLException {
         JDBC4ResultSet.this.unsupported();
      }
   }
}
