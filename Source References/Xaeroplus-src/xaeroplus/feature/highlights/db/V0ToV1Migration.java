package xaeroplus.feature.highlights.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.db.DatabaseMigration;

public class V0ToV1Migration implements DatabaseMigration {
   public boolean shouldMigrate(String databaseName, Connection connection, boolean init) {
      try {
         return this.tableExists("0", connection) || this.tableExists("-1", connection) || this.tableExists("1", connection);
      } catch (Exception e) {
         if (DatabaseMigration.isCorruptDatabase(e)) {
            throw new RuntimeException(e);
         } else {
            XaeroPlus.LOGGER.error("Failed checking if {} database should migrate", databaseName, e);
            return false;
         }
      }
   }

   public void doMigration(String databaseName, final Connection connection, final boolean init) {
      try {
         this.mergeTables("0", this.getTableName(class_1937.field_25179), connection);
         this.mergeTables("-1", this.getTableName(class_1937.field_25180), connection);
         this.mergeTables("1", this.getTableName(class_1937.field_25181), connection);
         Statement statement = connection.createStatement();

         try {
            String var10001 = this.getTableName(class_1937.field_25179);
            statement.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS \"unique_xz_" + var10001 + "\" ON \"" + this.getTableName(class_1937.field_25179) + "\" (x, z)");
            var10001 = this.getTableName(class_1937.field_25180);
            statement.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS \"unique_xz_" + var10001 + "\" ON \"" + this.getTableName(class_1937.field_25180) + "\" (x, z)");
            var10001 = this.getTableName(class_1937.field_25181);
            statement.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS \"unique_xz_" + var10001 + "\" ON \"" + this.getTableName(class_1937.field_25181) + "\" (x, z)");
            statement.executeUpdate("DROP INDEX IF EXISTS unique_xzO");
            statement.executeUpdate("DROP INDEX IF EXISTS unique_xzN");
            statement.executeUpdate("DROP INDEX IF EXISTS unique_xzE");
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

      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Failed creating new tables for {} database", databaseName, e);
         throw new RuntimeException(e);
      }
   }

   private String getTableName(class_5321<class_1937> dimension) {
      return dimension.method_29177().toString();
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

   private void mergeTables(String src, String dest, Connection connection) throws SQLException {
      Statement statement = connection.createStatement();

      try {
         if (this.tableExists(dest, connection)) {
            if (this.tableExists(src, connection)) {
               statement.executeUpdate("INSERT OR IGNORE INTO \"" + dest + "\" SELECT * FROM \"" + src + "\"");
               statement.executeUpdate("DROP TABLE IF EXISTS \"" + src + "\"");
            }
         } else {
            statement.executeUpdate("ALTER TABLE \"" + src + "\" RENAME TO \"" + dest + "\"");
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

   }
}
