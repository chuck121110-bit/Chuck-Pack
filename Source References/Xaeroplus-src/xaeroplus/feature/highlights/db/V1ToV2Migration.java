package xaeroplus.feature.highlights.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.db.DatabaseMigration;

public class V1ToV2Migration implements DatabaseMigration {
   private static final int VERSION = 2;

   public boolean shouldMigrate(final String databaseName, final Connection connection, final boolean init) throws SQLException {
      try {
         return this.getMetadataVersion(connection) < 2;
      } catch (SQLException ex) {
         throw ex;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public void doMigration(final String databaseName, final Connection connection, final boolean init) throws SQLException, InterruptedException {
      for(String tableName : this.getHighlightTableNames(connection)) {
         if (!this.isWithoutRowid(tableName, connection)) {
            this.migrateTable(databaseName, connection, tableName, init);
         }
      }

      this.setMetadataVersion(connection);
   }

   private List<String> getHighlightTableNames(final Connection connection) throws SQLException {
      ArrayList<String> tableNames = new ArrayList();
      Statement statement = connection.createStatement();

      try {
         ResultSet resultSet = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'");

         try {
            while(resultSet.next()) {
               String tableName = resultSet.getString("name");
               if (!"metadata".equals(tableName) && !tableName.endsWith("_v2_migration") && this.isHighlightTable(tableName, connection)) {
                  tableNames.add(tableName);
               }
            }
         } catch (Throwable var9) {
            if (resultSet != null) {
               try {
                  resultSet.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (resultSet != null) {
            resultSet.close();
         }
      } catch (Throwable var10) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var7) {
               var10.addSuppressed(var7);
            }
         }

         throw var10;
      }

      if (statement != null) {
         statement.close();
      }

      return tableNames;
   }

   private boolean isHighlightTable(final String tableName, final Connection connection) throws SQLException {
      boolean hasX = false;
      boolean hasZ = false;
      boolean hasFoundTime = false;
      Statement statement = connection.createStatement();

      try {
         String var10001 = this.quoteIdentifier(tableName);
         ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + var10001 + ")");

         while(resultSet.next()) {
            switch (resultSet.getString("name")) {
               case "x":
                  hasX = true;
                  break;
               case "z":
                  hasZ = true;
                  break;
               case "foundTime":
                  hasFoundTime = true;
            }
         }
      } catch (Throwable var11) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }
         }

         throw var11;
      }

      if (statement != null) {
         statement.close();
      }

      return hasX && hasZ && hasFoundTime;
   }

   private void migrateTable(final String databaseName, final Connection connection, final String tableName, final boolean init) throws SQLException, InterruptedException {
      String newTableName = tableName + "_v2_migration";
      if (!init) {
         XaeroPlus.LOGGER.info("Migrating {} database table {} to V2", databaseName, tableName);
      }

      long before = System.nanoTime();
      String var10001 = this.quoteIdentifier(newTableName);
      DatabaseMigration.executeCancellable(connection, "DROP TABLE IF EXISTS " + var10001);
      long step1 = System.nanoTime();
      if (!init) {
         XaeroPlus.LOGGER.info("migration step 1/5 {} ms", TimeUnit.NANOSECONDS.toMillis(step1 - before));
      }

      var10001 = this.quoteIdentifier(newTableName);
      DatabaseMigration.executeCancellable(connection, "CREATE TABLE " + var10001 + " (x INTEGER, z INTEGER, foundTime INTEGER, PRIMARY KEY (x, z)) WITHOUT ROWID");
      long step2 = System.nanoTime();
      if (!init) {
         XaeroPlus.LOGGER.info("migration step 2/5 {} ms", TimeUnit.NANOSECONDS.toMillis(step2 - step1));
      }

      var10001 = this.quoteIdentifier(newTableName);
      DatabaseMigration.executeCancellable(connection, "INSERT INTO " + var10001 + " (x, z, foundTime) SELECT x, z, foundTime FROM " + this.quoteIdentifier(tableName));
      long step3 = System.nanoTime();
      if (!init) {
         XaeroPlus.LOGGER.info("migration step 3/5 {} ms", TimeUnit.NANOSECONDS.toMillis(step3 - step2));
      }

      var10001 = this.quoteIdentifier(tableName);
      DatabaseMigration.executeCancellable(connection, "DROP TABLE " + var10001);
      long step4 = System.nanoTime();
      if (!init) {
         XaeroPlus.LOGGER.info("migration step 4/5 {} ms", TimeUnit.NANOSECONDS.toMillis(step4 - step3));
      }

      var10001 = this.quoteIdentifier(newTableName);
      DatabaseMigration.executeCancellable(connection, "ALTER TABLE " + var10001 + " RENAME TO " + this.quoteIdentifier(tableName));
      long done = System.nanoTime();
      if (!init) {
         XaeroPlus.LOGGER.info("migration step 5/5 {} ms", TimeUnit.NANOSECONDS.toMillis(done - step4));
      }

      if (!init) {
         XaeroPlus.LOGGER.info("{} migration total {} ms", tableName, TimeUnit.NANOSECONDS.toMillis(done - before));
      }

   }

   private int getMetadataVersion(final Connection connection) throws SQLException {
      if (!this.tableExists("metadata", connection)) {
         return 0;
      } else {
         Statement statement = connection.createStatement();

         int var4;
         label71: {
            try {
               ResultSet resultSet = statement.executeQuery("SELECT version FROM metadata WHERE id = 0");

               label73: {
                  try {
                     if (!resultSet.next()) {
                        break label73;
                     }

                     var4 = resultSet.getInt("version");
                  } catch (Throwable var8) {
                     if (resultSet != null) {
                        try {
                           resultSet.close();
                        } catch (Throwable var7) {
                           var8.addSuppressed(var7);
                        }
                     }

                     throw var8;
                  }

                  if (resultSet != null) {
                     resultSet.close();
                  }
                  break label71;
               }

               if (resultSet != null) {
                  resultSet.close();
               }
            } catch (Throwable var9) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var6) {
                     var9.addSuppressed(var6);
                  }
               }

               throw var9;
            }

            if (statement != null) {
               statement.close();
            }

            return 0;
         }

         if (statement != null) {
            statement.close();
         }

         return var4;
      }
   }

   private void setMetadataVersion(final Connection connection) throws SQLException {
      Statement statement = connection.createStatement();

      try {
         statement.executeUpdate("CREATE TABLE IF NOT EXISTS metadata (id INTEGER PRIMARY KEY, version INTEGER)");
         statement.executeUpdate("INSERT OR REPLACE INTO metadata (id, version) VALUES (0, 2)");
      } catch (Throwable var6) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (statement != null) {
         statement.close();
      }

   }

   private boolean tableExists(final String tableName, final Connection connection) throws SQLException {
      Statement statement = connection.createStatement();

      boolean var5;
      try {
         String var10001 = this.quoteLiteral(tableName);
         ResultSet resultSet = statement.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table' AND name = " + var10001);

         try {
            var5 = resultSet.next();
         } catch (Throwable var9) {
            if (resultSet != null) {
               try {
                  resultSet.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (resultSet != null) {
            resultSet.close();
         }
      } catch (Throwable var10) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var7) {
               var10.addSuppressed(var7);
            }
         }

         throw var10;
      }

      if (statement != null) {
         statement.close();
      }

      return var5;
   }

   private boolean isWithoutRowid(final String tableName, final Connection connection) throws SQLException {
      Statement statement = connection.createStatement();

      boolean var5;
      try {
         String var10001 = this.quoteLiteral(tableName);
         ResultSet resultSet = statement.executeQuery("SELECT sql FROM sqlite_master WHERE type = 'table' AND name = " + var10001);

         try {
            var5 = resultSet.next() && resultSet.getString("sql") != null && resultSet.getString("sql").toUpperCase(Locale.ROOT).contains("WITHOUT ROWID");
         } catch (Throwable var9) {
            if (resultSet != null) {
               try {
                  resultSet.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (resultSet != null) {
            resultSet.close();
         }
      } catch (Throwable var10) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var7) {
               var10.addSuppressed(var7);
            }
         }

         throw var10;
      }

      if (statement != null) {
         statement.close();
      }

      return var5;
   }

   private String quoteIdentifier(final String identifier) {
      return "\"" + identifier.replace("\"", "\"\"") + "\"";
   }

   private String quoteLiteral(final String literal) {
      return "'" + literal.replace("'", "''") + "'";
   }
}
