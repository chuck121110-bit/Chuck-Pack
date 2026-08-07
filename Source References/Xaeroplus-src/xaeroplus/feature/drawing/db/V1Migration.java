package xaeroplus.feature.drawing.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.db.DatabaseMigration;

public class V1Migration implements DatabaseMigration {
   public boolean shouldMigrate(final String databaseName, final Connection connection, final boolean init) {
      try {
         Statement statement = connection.createStatement();

         boolean var6;
         try {
            ResultSet resultSet = statement.executeQuery("SELECT version FROM metadata WHERE version = 1");

            try {
               var6 = !resultSet.next();
            } catch (Throwable var10) {
               if (resultSet != null) {
                  try {
                     resultSet.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (resultSet != null) {
               resultSet.close();
            }
         } catch (Throwable var11) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var8) {
                  var11.addSuppressed(var8);
               }
            }

            throw var11;
         }

         if (statement != null) {
            statement.close();
         }

         return var6;
      } catch (Exception e) {
         if (DatabaseMigration.isCorruptDatabase(e)) {
            throw new RuntimeException(e);
         } else {
            XaeroPlus.LOGGER.error("Failed checking whether {} database needs ellipse migration", databaseName, e);
            return true;
         }
      }
   }

   public void doMigration(final String databaseName, final Connection connection, final boolean init) {
      this.createEllipseTable(databaseName, connection, class_1937.field_25179);
      this.createEllipseTable(databaseName, connection, class_1937.field_25180);
      this.createEllipseTable(databaseName, connection, class_1937.field_25181);

      try {
         Statement statement = connection.createStatement();

         try {
            statement.executeUpdate("INSERT OR REPLACE INTO metadata (version) VALUES (1)");
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
         XaeroPlus.LOGGER.error("Failed recording ellipse migration for {} database", databaseName, e);
         throw new RuntimeException(e);
      }
   }

   private void createEllipseTable(final String databaseName, final Connection connection, final class_5321<class_1937> dimension) {
      try {
         Statement statement = connection.createStatement();

         try {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS \"" + String.valueOf(dimension.method_29177()) + "-ellipses\" (centerX INTEGER, centerZ INTEGER, radiusX INTEGER, radiusZ INTEGER, color INTEGER, PRIMARY KEY (centerX, centerZ, radiusX, radiusZ))");
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
         XaeroPlus.LOGGER.error("Error creating ellipses table for db: {}", databaseName, e);
         throw new RuntimeException(e);
      }
   }
}
