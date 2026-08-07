package xaeroplus.feature.highlights;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.Closeable;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import org.rfresh.sqlite.JDBC;
import org.rfresh.sqlite.SQLiteConnection;
import org.rfresh.sqlite.SQLiteErrorCode;
import xaero.map.WorldMap;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.db.DatabaseMigrator;
import xaeroplus.feature.highlights.db.V0ToV1Migration;
import xaeroplus.feature.highlights.db.V1ToV2Migration;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.Wait;

public class ChunkHighlightDatabase implements Closeable {
   public static final int MAX_HIGHLIGHTS_LIST = 25000;
   private static final int DATABASE_VERSION = 2;
   private Connection connection;
   protected final String databaseName;
   protected final Path dbPath;
   private static final DatabaseMigrator MIGRATOR = new DatabaseMigrator(List.of(new V0ToV1Migration(), new V1ToV2Migration()));
   boolean recoveryAttempted = false;
   private static final int MAX_RETRIES = 3;
   private final boolean init;

   public ChunkHighlightDatabase(String worldId, String databaseName) {
      this.databaseName = databaseName;

      try {
         Class<JDBC> jdbcClass = JDBC.class;
         this.dbPath = WorldMap.saveFolder.toPath().resolve(worldId).resolve(databaseName + ".db");
         this.init = !this.dbPath.toFile().exists();
         this.connection = DriverManager.getConnection("jdbc:rfresh_sqlite:" + String.valueOf(this.dbPath));
         ((SQLiteConnection)this.connection).setBusyTimeout(5000);
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Error while creating chunk highlight database: {} for worldId: {}", new Object[]{databaseName, worldId, e});
         throw new RuntimeException(e);
      }
   }

   void initializeDb() {
      try {
         this.connection = MIGRATOR.migrate(this.dbPath, this.databaseName, this.connection, this.init);
         this.validateDbVersion();
         this.setPragmas();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   int getDatabaseMetadataVersion() {
      int version = 2;

      try {
         Statement statement = this.connection.createStatement();

         try {
            ResultSet rs = statement.executeQuery("select version from metadata where id = '0'");

            try {
               rs.next();
               version = rs.getInt("version");
            } catch (Throwable var8) {
               if (rs != null) {
                  try {
                     rs.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (rs != null) {
               rs.close();
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
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Error reading database version: {}", 2, e);
      }

      return version;
   }

   void validateDbVersion() {
      int version = this.getDatabaseMetadataVersion();
      if (version < 2) {
         XaeroPlus.LOGGER.error("Database version mismatch: expected {} found {}", 2, version);
      }

      if (version > 2) {
         throw new IllegalStateException("Database version mismatch: expected 2, found " + version);
      } else {
         this.writeDbVersion();
      }
   }

   void writeDbVersion() {
      try {
         Statement statement = this.connection.createStatement();

         try {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS metadata (id INTEGER PRIMARY KEY, version INTEGER)");
            statement.executeUpdate("INSERT OR REPLACE INTO metadata (id, version) VALUES ('0', 2)");
         } catch (Throwable var5) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (statement != null) {
            statement.close();
         }
      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error writing database version: {}", 2, e);
      }

   }

   public void initializeDimension(class_5321<class_1937> dimension) {
      this.createHighlightsTableIfNotExists(dimension);
   }

   private String getTableName(class_5321<class_1937> dimension) {
      return dimension.method_29177().toString();
   }

   private void setPragmas() {
      try {
         Statement statement = this.connection.createStatement();

         try {
            statement.executeUpdate("pragma journal_mode = WAL;");
            statement.executeUpdate("pragma synchronous = NORMAL;");
         } catch (Throwable var5) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (statement != null) {
            statement.close();
         }

      } catch (SQLException e) {
         throw new RuntimeException("Failed to set sqlite pragmas", e);
      }
   }

   private void recoverCorruptDatabase() {
      if (!this.recoveryAttempted) {
         this.recoveryAttempted = true;
         XaeroPlus.LOGGER.info("Attempting to recover corrupt database: {}", this.databaseName);
         Path var10000 = this.dbPath.getParent();
         String var10001 = this.databaseName;
         Path recoveredDbPath = var10000.resolve("recovered_" + var10001 + "-" + System.currentTimeMillis() + ".db");

         try {
            Statement statement = this.connection.createStatement();

            try {
               statement.executeUpdate("recover to \"" + String.valueOf(recoveredDbPath.toAbsolutePath()) + "\"");
               XaeroPlus.LOGGER.info("Wrote recovered database to: {}", recoveredDbPath);
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
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Error recovering corrupt database: {}", this.databaseName, e);
            return;
         }

         try {
            this.connection.close();
            XaeroPlus.LOGGER.info("Closed DB connection to corrupt database: {}", this.databaseName);
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Error closing connection to corrupt database: {}", this.databaseName, e);
            throw new RuntimeException(e);
         }

         Path originalJournalDbPath = this.dbPath.getParent().resolve(String.valueOf(this.dbPath.getFileName()) + "-journal");
         Path recoveredJournalDbPath = recoveredDbPath.getParent().resolve(String.valueOf(recoveredDbPath.getFileName()) + "-journal");
         var10000 = this.dbPath.getParent();
         var10001 = this.databaseName;
         Path corruptedBackDbPath = var10000.resolve("corrupted_" + var10001 + "-" + System.currentTimeMillis() + ".db");
         Path corruptedBackJournalDbPath = corruptedBackDbPath.getParent().resolve(String.valueOf(corruptedBackDbPath.getFileName()) + "-journal");
         CopyOption[] copyOptions;
         if (Globals.atomicMoveAvailable) {
            copyOptions = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE};
         } else {
            copyOptions = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};
         }

         try {
            Files.move(this.dbPath, corruptedBackDbPath, copyOptions);
            if (originalJournalDbPath.toFile().exists()) {
               Files.move(originalJournalDbPath, corruptedBackJournalDbPath, copyOptions);
            }

            Files.move(recoveredDbPath, this.dbPath, copyOptions);
            if (recoveredJournalDbPath.toFile().exists()) {
               Files.move(recoveredJournalDbPath, originalJournalDbPath, copyOptions);
            }

            XaeroPlus.LOGGER.info("Replaced corrupt database with recovered: {}", this.databaseName);
            this.connection = DriverManager.getConnection("jdbc:rfresh_sqlite:" + String.valueOf(this.dbPath));
            XaeroPlus.LOGGER.info("Opened DB connection to recovered database: {}", this.databaseName);
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Error reopening connection to recovered database: {}", this.databaseName, e);
            throw new RuntimeException(e);
         }

         try {
            Files.delete(corruptedBackDbPath);
            if (corruptedBackJournalDbPath.toFile().exists()) {
               Files.delete(corruptedBackJournalDbPath);
            }

            XaeroPlus.LOGGER.info("Deleted corrupted database backup: {}", corruptedBackDbPath);
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Error deleting corrupted backup database: {}", this.databaseName, e);
         }

         XaeroPlus.LOGGER.info("Completed recovering corrupt database: {}", this.databaseName);
      }
   }

   private void createHighlightsTableIfNotExists(class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.createHighlightsTableIfNotExists0(dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying creation of highlights table in {} database in dimension: {} (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean createHighlightsTableIfNotExists0(class_5321<class_1937> dimension) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension);
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS \"" + var10001 + "\" (x INTEGER, z INTEGER, foundTime INTEGER, PRIMARY KEY (x, z)) WITHOUT ROWID");
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

         return true;
      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error creating highlights table for db: {} in dimension: {}", new Object[]{this.databaseName, dimension.method_29177(), e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
            XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
            this.recoverCorruptDatabase();
         }

         return false;
      }
   }

   public void insertHighlightList(final Long2LongMap chunks, final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.insertHighlightList0(chunks, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying insert of {} chunks into {} database in dimension: {} (attempt {}/{})", new Object[]{chunks.size(), this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean insertHighlightList0(final Long2LongMap chunks, final class_5321<class_1937> dimension) {
      if (chunks.isEmpty()) {
         return true;
      } else {
         try {
            int batchSize = 25000;
            ObjectIterator<Long2LongMap.Entry> it = Long2LongMaps.fastIterator(chunks);
            StringBuilder sb = new StringBuilder(50 * Math.min(batchSize, chunks.size()) + 75);

            while(it.hasNext()) {
               sb.setLength(0);
               sb.append("INSERT OR IGNORE INTO \"").append(this.getTableName(dimension)).append("\" (x, z, foundTime) VALUES ");
               boolean trailingComma = false;

               for(int i = 0; i < batchSize && it.hasNext(); ++i) {
                  Long2LongMap.Entry entry = (Long2LongMap.Entry)it.next();
                  long chunk = entry.getLongKey();
                  int chunkX = ChunkUtils.longToChunkX(chunk);
                  int chunkZ = ChunkUtils.longToChunkZ(chunk);
                  long foundTime = entry.getLongValue();
                  sb.append("(").append(chunkX).append(", ").append(chunkZ).append(", ").append(foundTime).append(")");
                  sb.append(", ");
                  trailingComma = true;
               }

               if (trailingComma) {
                  sb.replace(sb.length() - 2, sb.length(), "");
               }

               Statement stmt = this.connection.createStatement();

               try {
                  stmt.executeUpdate(sb.toString());
               } catch (Throwable var16) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var15) {
                        var16.addSuppressed(var15);
                     }
                  }

                  throw var16;
               }

               if (stmt != null) {
                  stmt.close();
               }
            }

            return true;
         } catch (SQLException e) {
            XaeroPlus.LOGGER.error("Error inserting {} chunks into {} database in dimension: {}", new Object[]{chunks.size(), this.databaseName, dimension.method_29177(), e});
            if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
               XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
               this.recoverCorruptDatabase();
            }

            return false;
         }
      }
   }

   public void getHighlightsInWindow(final class_5321<class_1937> dimension, final int regionXMin, final int regionXMax, final int regionZMin, final int regionZMax, HighlightConsumer consumer) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.getHighlightsInWindow0(dimension, regionXMin, regionXMax, regionZMin, regionZMax, consumer)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying get highlights from {} database in dimension: {}, (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean getHighlightsInWindow0(final class_5321<class_1937> dimension, final int regionXMin, final int regionXMax, final int regionZMin, final int regionZMax, HighlightConsumer consumer) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension);
            ResultSet resultSet = statement.executeQuery("SELECT x, z, foundTime FROM \"" + var10001 + "\" WHERE x >= " + ChunkUtils.regionCoordToChunkCoord(regionXMin) + " AND x <= " + ChunkUtils.regionCoordToChunkCoord(regionXMax) + " AND z >= " + ChunkUtils.regionCoordToChunkCoord(regionZMin) + " AND z <= " + ChunkUtils.regionCoordToChunkCoord(regionZMax));

            try {
               while(resultSet.next()) {
                  consumer.accept(resultSet.getInt("x"), resultSet.getInt("z"), resultSet.getLong("foundTime"));
               }
            } catch (Throwable var13) {
               if (resultSet != null) {
                  try {
                     resultSet.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }
               }

               throw var13;
            }

            if (resultSet != null) {
               resultSet.close();
            }
         } catch (Throwable var14) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var11) {
                  var14.addSuppressed(var11);
               }
            }

            throw var14;
         }

         if (statement != null) {
            statement.close();
         }

         return true;
      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error getting chunks from {} database in dimension: {}, window: {}-{}, {}-{}", new Object[]{this.databaseName, dimension.method_29177(), regionXMin, regionXMax, regionZMin, regionZMax, e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
            XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
            this.recoverCorruptDatabase();
         }

         return false;
      }
   }

   public void getHighlightsInWindowAndOutsidePrevWindow(final class_5321<class_1937> dimension, final int regionXMin, final int regionXMax, final int regionZMin, final int regionZMax, final int prevRegionXMin, final int prevRegionXMax, final int prevRegionZMin, final int prevRegionZMax, HighlightConsumer consumer) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.getHighlightsInWindowAndOutsidePrevWindow0(dimension, regionXMin, regionXMax, regionZMin, regionZMax, prevRegionXMin, prevRegionXMax, prevRegionZMin, prevRegionZMax, consumer)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying get of highlights from {} database in dimension: {}, (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean getHighlightsInWindowAndOutsidePrevWindow0(final class_5321<class_1937> dimension, final int regionXMin, final int regionXMax, final int regionZMin, final int regionZMax, final int prevRegionXMin, final int prevRegionXMax, final int prevRegionZMin, final int prevRegionZMax, HighlightConsumer consumer) {
      int xMin = ChunkUtils.regionCoordToChunkCoord(regionXMin);
      int xMax = ChunkUtils.regionCoordToChunkCoord(regionXMax);
      int zMin = ChunkUtils.regionCoordToChunkCoord(regionZMin);
      int zMax = ChunkUtils.regionCoordToChunkCoord(regionZMax);
      int prevXMin = ChunkUtils.regionCoordToChunkCoord(prevRegionXMin);
      int prevXMax = ChunkUtils.regionCoordToChunkCoord(prevRegionXMax);
      int prevZMin = ChunkUtils.regionCoordToChunkCoord(prevRegionZMin);
      int prevZMax = ChunkUtils.regionCoordToChunkCoord(prevRegionZMax);

      try {
         Statement statement = this.connection.createStatement();

         try {
            ResultSet resultSet = statement.executeQuery("SELECT x, z, foundTime FROM \"" + this.getTableName(dimension) + "\" WHERE x BETWEEN " + xMin + " AND " + xMax + " AND z BETWEEN " + zMin + " AND " + zMax + " AND NOT (x BETWEEN " + prevXMin + " AND " + prevXMax + " AND z BETWEEN " + prevZMin + " AND " + prevZMax + ")");

            try {
               while(resultSet.next()) {
                  consumer.accept(resultSet.getInt("x"), resultSet.getInt("z"), resultSet.getLong("foundTime"));
               }
            } catch (Throwable var25) {
               if (resultSet != null) {
                  try {
                     resultSet.close();
                  } catch (Throwable var24) {
                     var25.addSuppressed(var24);
                  }
               }

               throw var25;
            }

            if (resultSet != null) {
               resultSet.close();
            }
         } catch (Throwable var26) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var23) {
                  var26.addSuppressed(var23);
               }
            }

            throw var26;
         }

         if (statement != null) {
            statement.close();
         }

         return true;
      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error getting chunks from {} database in dimension: {}, window: {}-{}, {}-{}", new Object[]{this.databaseName, dimension.method_29177(), regionXMin, regionXMax, regionZMin, regionZMax, e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
            XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
            this.recoverCorruptDatabase();
         }

         return false;
      }
   }

   public void removeHighlight(final int x, final int z, final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.removeHighlight0(x, z, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying removal of highlight from {} database in dimension: {} (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean removeHighlight0(final int x, final int z, final class_5321<class_1937> dimension) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            statement.executeUpdate("DELETE FROM \"" + this.getTableName(dimension) + "\" WHERE x = " + x + " AND z = " + z);
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

         return true;
      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error while removing highlight from {} database in dimension: {}, at {}, {}", new Object[]{this.databaseName, dimension.method_29177(), x, z, e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
            XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
            this.recoverCorruptDatabase();
         }

         return false;
      }
   }

   public void removeHighlights(final LongCollection toRemove, final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.removeHighlights0(toRemove, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying removal of highlight set from {} database in dimension: {} (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean removeHighlights0(final LongCollection toRemove, final class_5321<class_1937> dimension) {
      if (toRemove.isEmpty()) {
         return true;
      } else {
         try {
            int batchSize = 500;
            LongIterator it = toRemove.longIterator();
            StringBuilder sb = new StringBuilder(50 * Math.min(batchSize, toRemove.size()) + 75);

            while(it.hasNext()) {
               sb.setLength(0);
               sb.append("DELETE FROM \"").append(this.getTableName(dimension)).append("\" WHERE ");

               for(int i = 0; i < batchSize && it.hasNext(); ++i) {
                  long chunk = it.nextLong();
                  int chunkX = ChunkUtils.longToChunkX(chunk);
                  int chunkZ = ChunkUtils.longToChunkZ(chunk);
                  sb.append("(x=").append(chunkX).append(" AND z=").append(chunkZ).append(")");
                  if (i < batchSize - 1 && it.hasNext()) {
                     sb.append(" OR ");
                  }
               }

               Statement statement = this.connection.createStatement();

               try {
                  statement.executeUpdate(sb.toString());
               } catch (Throwable var12) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                     }
                  }

                  throw var12;
               }

               if (statement != null) {
                  statement.close();
               }
            }

            return true;
         } catch (SQLException e) {
            XaeroPlus.LOGGER.error("Error while removing highlight set from {} database in dimension: {} with {} highlights", new Object[]{this.databaseName, dimension.method_29177(), toRemove.size(), e});
            if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
               XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
               this.recoverCorruptDatabase();
            }

            return false;
         }
      }
   }

   public void close() {
      try {
         this.connection.close();
      } catch (Exception e) {
         XaeroPlus.LOGGER.warn("Failed closing {} database connection", this.databaseName, e);
      }

   }

   @FunctionalInterface
   public interface HighlightConsumer {
      void accept(int x, int z, long foundTime);
   }
}
