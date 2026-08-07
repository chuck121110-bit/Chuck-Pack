package xaeroplus.feature.drawing.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.db.DatabaseMigration;

public class V0Migration implements DatabaseMigration {
   public boolean shouldMigrate(final String databaseName, final Connection connection, final boolean init) {
      try {
         if (!this.tableExists("metadata", connection)) {
            return true;
         } else {
            Statement statement = connection.createStatement();

            boolean var6;
            label67: {
               try {
                  ResultSet resultSet = statement.executeQuery("SELECT version FROM metadata where version = 0");
                  if (!resultSet.next()) {
                     var6 = true;
                     break label67;
                  }
               } catch (Throwable var8) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (statement != null) {
                  statement.close();
               }

               return false;
            }

            if (statement != null) {
               statement.close();
            }

            return var6;
         }
      } catch (Exception e) {
         if (DatabaseMigration.isCorruptDatabase(e)) {
            throw new RuntimeException(e);
         } else {
            XaeroPlus.LOGGER.error("Failed checking if {} database should migrate", databaseName, e);
            return false;
         }
      }
   }

   public void doMigration(final String databaseName, final Connection connection, final boolean init) {
      this.createMetadataTable(databaseName, connection);
      this.createLinesTable(databaseName, connection, class_1937.field_25179);
      this.createLinesTable(databaseName, connection, class_1937.field_25180);
      this.createLinesTable(databaseName, connection, class_1937.field_25181);
      this.createHighlightsTable(databaseName, connection, class_1937.field_25179);
      this.createHighlightsTable(databaseName, connection, class_1937.field_25180);
      this.createHighlightsTable(databaseName, connection, class_1937.field_25181);
      this.createTextsTable(databaseName, connection, class_1937.field_25179);
      this.createTextsTable(databaseName, connection, class_1937.field_25180);
      this.createTextsTable(databaseName, connection, class_1937.field_25181);
   }

   private void createHighlightsTable(final String databaseName, final Connection connection, final class_5321<class_1937> dimension) {
      try {
         Statement statement = connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension, "highlights");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS \"" + var10001 + "\" (x INTEGER, z INTEGER, color INTEGER, PRIMARY KEY (x, z))");
         } catch (Throwable var8) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (statement != null) {
            statement.close();
         }

      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error creating highlights table for db: {}", databaseName, e);
         throw new RuntimeException(e);
      }
   }

   private void createLinesTable(final String databaseName, final Connection connection, class_5321<class_1937> dimension) {
      try {
         Statement statement = connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension, "lines");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS \"" + var10001 + "\" (x1 INTEGER, z1 INTEGER, x2 INTEGER, z2 INTEGER, color INTEGER, PRIMARY KEY (x1, z1, x2, z2))");
         } catch (Throwable var8) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (statement != null) {
            statement.close();
         }

      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error creating lines table for db: {}", databaseName, e);
         throw new RuntimeException(e);
      }
   }

   private void createTextsTable(final String databaseName, final Connection connection, class_5321<class_1937> dimension) {
      try {
         Statement statement = connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension, "texts");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS \"" + var10001 + "\" (value TEXT, x INTEGER, z INTEGER, color INTEGER, scale REAL, PRIMARY KEY (x, z))");
         } catch (Throwable var8) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (statement != null) {
            statement.close();
         }

      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error creating texts table for db: {}", databaseName, e);
         throw new RuntimeException(e);
      }
   }

   private String getTableName(class_5321<class_1937> dimension, String type) {
      String var10000 = dimension.method_29177().toString();
      return var10000 + "-" + type;
   }

   private void createMetadataTable(String databaseName, Connection connection) {
      try {
         Statement statement = connection.createStatement();

         try {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS metadata (version INTEGER PRIMARY KEY, time DATETIME NOT NULL default CURRENT_TIMESTAMP)");
            statement.executeUpdate("INSERT OR REPLACE INTO metadata (version) VALUES (0)");
         } catch (Throwable var7) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (statement != null) {
            statement.close();
         }

      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error creating metadata table for db: {}", databaseName, e);
         throw new RuntimeException(e);
      }
   }

   private boolean tableExists(String tableName, Connection connection) throws SQLException {
      Statement statement = connection.createStatement();

      boolean var5;
      try {
         ResultSet resultSet = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' and name='" + tableName + "'");
         var5 = resultSet.next();
      } catch (Throwable var7) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (statement != null) {
         statement.close();
      }

      return var5;
   }
}
