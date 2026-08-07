package org.rfresh.sqlite;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import org.rfresh.sqlite.jdbc4.JDBC4Connection;
import org.rfresh.sqlite.util.Logger;
import org.rfresh.sqlite.util.LoggerFactory;

public class JDBC implements Driver {
   private static final Logger logger = LoggerFactory.getLogger(JDBC.class);
   public static final String PREFIX = "jdbc:rfresh_sqlite:";

   public int getMajorVersion() {
      return SQLiteJDBCLoader.getMajorVersion();
   }

   public int getMinorVersion() {
      return SQLiteJDBCLoader.getMinorVersion();
   }

   public boolean jdbcCompliant() {
      return false;
   }

   public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return null;
   }

   public boolean acceptsURL(String url) {
      return isValidURL(url);
   }

   public static boolean isValidURL(String url) {
      return url != null && url.toLowerCase().startsWith("jdbc:rfresh_sqlite:");
   }

   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
      return SQLiteConfig.getDriverPropertyInfo();
   }

   public Connection connect(String url, Properties info) throws SQLException {
      return !isValidURL(url) ? null : createConnection(url, info);
   }

   static String extractAddress(String url) {
      return url.substring("jdbc:rfresh_sqlite:".length());
   }

   public static SQLiteConnection createConnection(String url, Properties prop) throws SQLException {
      if (!isValidURL(url)) {
         throw new SQLException("invalid database address: " + url);
      } else {
         url = url.trim();
         return new JDBC4Connection(url, extractAddress(url), prop);
      }
   }

   static {
      try {
         DriverManager.registerDriver(new JDBC());
      } catch (SQLException e) {
         logger.error(() -> "Could not register driver", e);
      }

   }
}
