package org.rfresh.sqlite.jdbc4;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.util.Properties;
import org.rfresh.sqlite.jdbc3.JDBC3Connection;

public class JDBC4Connection extends JDBC3Connection {
   public JDBC4Connection(String url, String fileName, Properties prop) throws SQLException {
      super(url, fileName, prop);
   }

   public Statement createStatement(int rst, int rsc, int rsh) throws SQLException {
      this.checkOpen();
      this.checkCursor(rst, rsc, rsh);
      return new JDBC4Statement(this);
   }

   public PreparedStatement prepareStatement(String sql, int rst, int rsc, int rsh) throws SQLException {
      this.checkOpen();
      this.checkCursor(rst, rsc, rsh);
      return new JDBC4PreparedStatement(this, sql);
   }

   public boolean isClosed() throws SQLException {
      return super.isClosed();
   }

   public <T> T unwrap(Class<T> iface) throws ClassCastException {
      return (T)iface.cast(this);
   }

   public boolean isWrapperFor(Class<?> iface) {
      return iface.isInstance(this);
   }

   public Clob createClob() throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public Blob createBlob() throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public NClob createNClob() throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public SQLXML createSQLXML() throws SQLException {
      throw new SQLFeatureNotSupportedException();
   }

   public boolean isValid(int timeout) throws SQLException {
      if (this.isClosed()) {
         return false;
      } else {
         Statement statement = this.createStatement();

         boolean var3;
         try {
            var3 = statement.execute("select 1");
         } finally {
            statement.close();
         }

         return var3;
      }
   }

   public void setClientInfo(String name, String value) throws SQLClientInfoException {
   }

   public void setClientInfo(Properties properties) throws SQLClientInfoException {
   }

   public String getClientInfo(String name) throws SQLException {
      return null;
   }

   public Properties getClientInfo() throws SQLException {
      return null;
   }

   public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
      return null;
   }
}
