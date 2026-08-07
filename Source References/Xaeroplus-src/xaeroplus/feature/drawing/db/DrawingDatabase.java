package xaeroplus.feature.drawing.db;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
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
import java.util.function.Consumer;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import org.rfresh.sqlite.JDBC;
import org.rfresh.sqlite.NativeLibraryNotFoundException;
import org.rfresh.sqlite.SQLiteConnection;
import org.rfresh.sqlite.SQLiteErrorCode;
import xaero.map.WorldMap;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.db.DatabaseMigrator;
import xaeroplus.feature.render.ellipse.Ellipse;
import xaeroplus.feature.render.line.Line;
import xaeroplus.feature.render.text.Text;
import xaeroplus.module.impl.TickTaskExecutor;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.NotificationUtil;
import xaeroplus.util.Wait;

public class DrawingDatabase implements Closeable {
   public static final int MAX_HIGHLIGHTS_LIST = 25000;
   public static final String HIGHLIGHTS_TABLE = "highlights";
   public static final String LINES_TABLE = "lines";
   public static final String ELLIPSES_TABLE = "ellipses";
   public static final String TEXTS_TABLE = "texts";
   private Connection connection;
   public final String databaseName;
   protected final Path dbPath;
   private static final DatabaseMigrator MIGRATOR = new DatabaseMigrator(List.of(new V0Migration(), new V1Migration()));
   boolean recoveryAttempted = false;
   static boolean nativeLibraryErrorSent = false;
   private static final int MAX_RETRIES = 3;

   public DrawingDatabase(String worldId, String databaseName) {
      this.databaseName = databaseName;

      try {
         Class<JDBC> jdbcClass = JDBC.class;
         this.dbPath = WorldMap.saveFolder.toPath().resolve(worldId).resolve(databaseName + ".db");
         boolean init = !this.dbPath.toFile().exists();
         this.connection = DriverManager.getConnection("jdbc:rfresh_sqlite:" + String.valueOf(this.dbPath));
         ((SQLiteConnection)this.connection).setBusyTimeout(5000);
         this.connection = MIGRATOR.migrate(this.dbPath, databaseName, this.connection, init);
         this.setPragmas();
      } catch (Exception var6) {
         if (!nativeLibraryErrorSent) {
            Throwable var5 = var6.getCause();
            if (var5 instanceof NativeLibraryNotFoundException) {
               NativeLibraryNotFoundException nativeException = (NativeLibraryNotFoundException)var5;
               nativeLibraryErrorSent = true;
               TickTaskExecutor.INSTANCE.execute(() -> NotificationUtil.errorNotification("Error initializing Drawing database, Drawing features will not work.\n" + nativeException.getMessage()));
            }
         }

         XaeroPlus.LOGGER.error("Error while creating drawing database: {} for worldId: {}", new Object[]{databaseName, worldId, var6});
         throw new RuntimeException(var6);
      }
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

   private void createHighlightsTable(final String databaseName, final Connection connection, final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.createHighlightsTable0(databaseName, connection, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying creating highlights table for db: {} (attempt {}/{})", new Object[]{databaseName, tryCount, 3});
         Wait.waitMs(50);
      }

      throw new RuntimeException("Failed to create highlights table for db: " + databaseName);
   }

   private boolean createHighlightsTable0(final String databaseName, final Connection connection, final class_5321<class_1937> dimension) {
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

         return true;
      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error creating highlights table for db: {}", databaseName, e);
         return false;
      }
   }

   private void createLinesTable(final String databaseName, final Connection connection, class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.createLinesTable0(databaseName, connection, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying creating lines table for db: {} (attempt {}/{})", new Object[]{databaseName, tryCount, 3});
         Wait.waitMs(50);
      }

      throw new RuntimeException("Failed to create lines table for db: " + databaseName);
   }

   private boolean createLinesTable0(final String databaseName, final Connection connection, class_5321<class_1937> dimension) {
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

         return true;
      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error creating lines table for db: {}", databaseName, e);
         return false;
      }
   }

   private void createEllipsesTable(final String databaseName, final Connection connection, final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.createEllipsesTable0(databaseName, connection, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying creating ellipses table for db: {} (attempt {}/{})", new Object[]{databaseName, tryCount, 3});
         Wait.waitMs(50);
      }

      throw new RuntimeException("Failed to create ellipses table for db: " + databaseName);
   }

   private boolean createEllipsesTable0(final String databaseName, final Connection connection, final class_5321<class_1937> dimension) {
      try {
         Statement statement = connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension, "ellipses");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS \"" + var10001 + "\" (centerX INTEGER, centerZ INTEGER, radiusX INTEGER, radiusZ INTEGER, color INTEGER, PRIMARY KEY (centerX, centerZ, radiusX, radiusZ))");
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
         XaeroPlus.LOGGER.error("Error creating ellipses table for db: {}", databaseName, e);
         return false;
      }
   }

   private void createTextsTable(final String databaseName, final Connection connection, class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.createTextsTable0(databaseName, connection, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying creating texts table for db: {} (attempt {}/{})", new Object[]{databaseName, tryCount, 3});
         Wait.waitMs(50);
      }

      throw new RuntimeException("Failed to create texts table for db: " + databaseName);
   }

   private boolean createTextsTable0(final String databaseName, final Connection connection, class_5321<class_1937> dimension) {
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

         return true;
      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error creating texts table for db: {}", databaseName, e);
         return false;
      }
   }

   private String getTableName(class_5321<class_1937> dimension, String type) {
      String var10000 = dimension.method_29177().toString();
      return var10000 + "-" + type;
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

   public void initializeDimension(final class_5321<class_1937> dimension) {
      this.createHighlightsTable(this.databaseName, this.connection, dimension);
      this.createLinesTable(this.databaseName, this.connection, dimension);
      this.createEllipsesTable(this.databaseName, this.connection, dimension);
      this.createTextsTable(this.databaseName, this.connection, dimension);
   }

   public void getLinesInDimension(final class_5321<class_1937> dimension, LineConsumer consumer) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.getLinesInDimension0(dimension, consumer)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying getting lines from {} database in dimension: {} (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean getLinesInDimension0(final class_5321<class_1937> dimension, LineConsumer consumer) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension, "lines");
            ResultSet resultSet = statement.executeQuery("SELECT * FROM \"" + var10001 + "\"");

            try {
               while(resultSet.next()) {
                  consumer.accept(resultSet.getInt("x1"), resultSet.getInt("z1"), resultSet.getInt("x2"), resultSet.getInt("z2"), resultSet.getInt("color"));
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

         return true;
      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error getting lines from {} database in dimension: {}", new Object[]{this.databaseName, dimension.method_29177(), e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
            XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
            this.recoverCorruptDatabase();
         }

         return false;
      }
   }

   public void getEllipsesInDimension(final class_5321<class_1937> dimension, final EllipseConsumer consumer) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.getEllipsesInDimension0(dimension, consumer)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying getting ellipses from {} database in dimension: {} (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean getEllipsesInDimension0(final class_5321<class_1937> dimension, final EllipseConsumer consumer) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension, "ellipses");
            ResultSet resultSet = statement.executeQuery("SELECT * FROM \"" + var10001 + "\"");

            try {
               while(resultSet.next()) {
                  consumer.accept(resultSet.getInt("centerX"), resultSet.getInt("centerZ"), resultSet.getInt("radiusX"), resultSet.getInt("radiusZ"), resultSet.getInt("color"));
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

         return true;
      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error getting ellipses from {} database in dimension: {}", new Object[]{this.databaseName, dimension.method_29177(), e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
            this.recoverCorruptDatabase();
         }

         return false;
      }
   }

   public void getTextsInWindow(final class_5321<class_1937> dimension, final int regionXMin, final int regionXMax, final int regionZMin, final int regionZMax, Consumer<Text> consumer) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.getTextsInWindow0(dimension, regionXMin, regionXMax, regionZMin, regionZMax, consumer)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying getting texts from {} database in dimension: {} (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean getTextsInWindow0(final class_5321<class_1937> dimension, final int regionXMin, final int regionXMax, final int regionZMin, final int regionZMax, Consumer<Text> consumer) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension, "texts");
            ResultSet resultSet = statement.executeQuery("SELECT * FROM \"" + var10001 + "\" WHERE x >= " + ChunkUtils.regionCoordToCoord(regionXMin) + " AND x <= " + ChunkUtils.regionCoordToCoord(regionXMax) + " AND z >= " + ChunkUtils.regionCoordToCoord(regionZMin) + " AND z <= " + ChunkUtils.regionCoordToCoord(regionZMax));

            try {
               while(resultSet.next()) {
                  Text text = new Text(resultSet.getString("value"), resultSet.getInt("x"), resultSet.getInt("z"), resultSet.getInt("color"), resultSet.getFloat("scale"));
                  consumer.accept(text);
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
         XaeroPlus.LOGGER.error("Error getting texts from {} database in dimension: {}", new Object[]{this.databaseName, dimension.method_29177(), e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
            XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
            this.recoverCorruptDatabase();
         }

         return false;
      }
   }

   public void getHighlightsInWindow(final class_5321<class_1937> dimension, final int regionXMin, final int regionXMax, final int regionZMin, final int regionZMax, HighlightConsumer consumer) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.getHighlightsInWindow0(dimension, regionXMin, regionXMax, regionZMin, regionZMax, consumer)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying getting highlights from {} database in dimension: {} (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean getHighlightsInWindow0(final class_5321<class_1937> dimension, final int regionXMin, final int regionXMax, final int regionZMin, final int regionZMax, HighlightConsumer consumer) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension, "highlights");
            ResultSet resultSet = statement.executeQuery("SELECT * FROM \"" + var10001 + "\" WHERE x >= " + ChunkUtils.regionCoordToChunkCoord(regionXMin) + " AND x <= " + ChunkUtils.regionCoordToChunkCoord(regionXMax) + " AND z >= " + ChunkUtils.regionCoordToChunkCoord(regionZMin) + " AND z <= " + ChunkUtils.regionCoordToChunkCoord(regionZMax));

            try {
               while(resultSet.next()) {
                  consumer.accept(resultSet.getInt("x"), resultSet.getInt("z"), resultSet.getInt("color"));
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

   public void insertLinesList(final Object2IntMap<Line> lines, final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.insertLinesList0(lines, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying inserting {} lines into {} database in dimension: {} (attempt {}/{})", new Object[]{lines.size(), this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean insertLinesList0(final Object2IntMap<Line> lines, final class_5321<class_1937> dimension) {
      if (lines.isEmpty()) {
         return true;
      } else {
         try {
            this.createLinesTable(this.databaseName, this.connection, dimension);
            int batchSize = 25000;
            StringBuilder sb = new StringBuilder(50 * Math.min(batchSize, lines.size()) + 75);
            ObjectIterator<Object2IntMap.Entry<Line>> it = Object2IntMaps.fastIterator(lines);

            while(it.hasNext()) {
               sb.setLength(0);
               sb.append("INSERT OR REPLACE INTO \"").append(this.getTableName(dimension, "lines")).append("\" VALUES ");
               boolean trailingComma = false;

               for(int i = 0; i < batchSize && it.hasNext(); ++i) {
                  Object2IntMap.Entry<Line> entry = (Object2IntMap.Entry)it.next();
                  Line line = (Line)entry.getKey();
                  sb.append("(").append(line.x1()).append(", ").append(line.z1()).append(", ").append(line.x2()).append(", ").append(line.z2()).append(", ").append(entry.getIntValue()).append(")");
                  sb.append(", ");
                  trailingComma = true;
               }

               if (trailingComma) {
                  sb.replace(sb.length() - 2, sb.length(), "");
               }

               Statement stmt = this.connection.createStatement();

               try {
                  stmt.executeUpdate(sb.toString());
               } catch (Throwable var11) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (stmt != null) {
                  stmt.close();
               }
            }

            return true;
         } catch (SQLException e) {
            XaeroPlus.LOGGER.error("Error inserting {} lines into {} database in dimension: {}", new Object[]{lines.size(), this.databaseName, dimension.method_29177(), e});
            if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
               XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
               this.recoverCorruptDatabase();
            }

            return false;
         }
      }
   }

   public void insertEllipsesList(final Object2IntMap<Ellipse> ellipses, final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.insertEllipsesList0(ellipses, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying inserting {} ellipses into {} database in dimension: {} (attempt {}/{})", new Object[]{ellipses.size(), this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean insertEllipsesList0(final Object2IntMap<Ellipse> ellipses, final class_5321<class_1937> dimension) {
      if (ellipses.isEmpty()) {
         return true;
      } else {
         try {
            this.createEllipsesTable(this.databaseName, this.connection, dimension);
            int batchSize = 25000;
            StringBuilder sql = new StringBuilder(60 * Math.min(batchSize, ellipses.size()) + 75);
            ObjectIterator<Object2IntMap.Entry<Ellipse>> iterator = Object2IntMaps.fastIterator(ellipses);

            while(iterator.hasNext()) {
               sql.setLength(0);
               sql.append("INSERT OR REPLACE INTO \"").append(this.getTableName(dimension, "ellipses")).append("\" VALUES ");

               for(int i = 0; i < batchSize && iterator.hasNext(); ++i) {
                  Object2IntMap.Entry<Ellipse> entry = (Object2IntMap.Entry)iterator.next();
                  Ellipse ellipse = (Ellipse)entry.getKey();
                  sql.append("(").append(ellipse.centerX()).append(", ").append(ellipse.centerZ()).append(", ").append(ellipse.radiusX()).append(", ").append(ellipse.radiusZ()).append(", ").append(entry.getIntValue()).append("), ");
               }

               sql.setLength(sql.length() - 2);
               Statement statement = this.connection.createStatement();

               try {
                  statement.executeUpdate(sql.toString());
               } catch (Throwable var10) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (statement != null) {
                  statement.close();
               }
            }

            return true;
         } catch (SQLException e) {
            XaeroPlus.LOGGER.error("Error inserting {} ellipses into {} database in dimension: {}", new Object[]{ellipses.size(), this.databaseName, dimension.method_29177(), e});
            if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
               this.recoverCorruptDatabase();
            }

            return false;
         }
      }
   }

   public void insertHighlightList(final Long2LongMap chunks, final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.insertHighlightList0(chunks, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying inserting {} chunks into {} database in dimension: {} (attempt {}/{})", new Object[]{chunks.size(), this.databaseName, dimension.method_29177(), tryCount, 3});
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
               sb.append("INSERT OR REPLACE INTO \"").append(this.getTableName(dimension, "highlights")).append("\" VALUES ");
               boolean trailingComma = false;

               for(int i = 0; i < batchSize && it.hasNext(); ++i) {
                  Long2LongMap.Entry entry = (Long2LongMap.Entry)it.next();
                  long chunk = entry.getLongKey();
                  int chunkX = ChunkUtils.longToChunkX(chunk);
                  int chunkZ = ChunkUtils.longToChunkZ(chunk);
                  long color = entry.getLongValue();
                  sb.append("(").append(chunkX).append(", ").append(chunkZ).append(", ").append(color).append(")");
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

   public void insertTextsList(final Long2ObjectMap<Text> texts, final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.insertTextsList0(texts, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying inserting {} texts into {} database in dimension: {} (attempt {}/{})", new Object[]{texts.size(), this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean insertTextsList0(final Long2ObjectMap<Text> texts, final class_5321<class_1937> dimension) {
      if (texts.isEmpty()) {
         return true;
      } else {
         try {
            int batchSize = 25000;
            ObjectIterator<Long2ObjectMap.Entry<Text>> it = Long2ObjectMaps.fastIterator(texts);
            StringBuilder sb = new StringBuilder(50 * Math.min(batchSize, texts.size()) + 75);

            while(it.hasNext()) {
               sb.setLength(0);
               sb.append("INSERT OR REPLACE INTO \"").append(this.getTableName(dimension, "texts")).append("\" VALUES ");
               boolean trailingComma = false;

               for(int i = 0; i < batchSize && it.hasNext(); ++i) {
                  Text entry = (Text)((Long2ObjectMap.Entry)it.next()).getValue();
                  sb.append("(").append("'").append(entry.value()).append("', ").append(entry.x()).append(", ").append(entry.z()).append(", ").append(entry.color()).append(", ").append(entry.scale()).append(")");
                  sb.append(", ");
                  trailingComma = true;
               }

               if (trailingComma) {
                  sb.replace(sb.length() - 2, sb.length(), "");
               }

               Statement stmt = this.connection.createStatement();

               try {
                  stmt.executeUpdate(sb.toString());
               } catch (Throwable var11) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (stmt != null) {
                  stmt.close();
               }
            }

            return true;
         } catch (SQLException e) {
            XaeroPlus.LOGGER.error("Error inserting {} texts into {} database in dimension: {}", new Object[]{texts.size(), this.databaseName, dimension.method_29177(), e});
            if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
               XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
               this.recoverCorruptDatabase();
            }

            return false;
         }
      }
   }

   public void removeLine(final int x1, final int z1, final int x2, final int z2, final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.removeLine0(x1, z1, x2, z2, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying removing line from {} database in dimension: {}, from ({}, {}) to ({}, {}) (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), x1, z1, x2, z2, tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean removeLine0(final int x1, final int z1, final int x2, final int z2, final class_5321<class_1937> dimension) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            statement.executeUpdate("DELETE FROM \"" + this.getTableName(dimension, "lines") + "\" WHERE x1 = " + x1 + " AND z1 = " + z1 + " AND x2 = " + x2 + " AND z2 = " + z2);
         } catch (Throwable var10) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }
            }

            throw var10;
         }

         if (statement != null) {
            statement.close();
         }

         return true;
      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error while removing line from {} database in dimension: {}", new Object[]{this.databaseName, dimension.method_29177(), e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
            XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
            this.recoverCorruptDatabase();
         }

         return false;
      }
   }

   public void removeAllLines(final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.removeAllLines0(dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying removing all lines from {} database in dimension: {}, (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean removeAllLines0(final class_5321<class_1937> dimension) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension, "lines");
            statement.executeUpdate("DELETE FROM \"" + var10001 + "\"");
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
         XaeroPlus.LOGGER.error("Error while removing all lines from {} database in dimension: {}", new Object[]{this.databaseName, dimension.method_29177(), e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
            XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
            this.recoverCorruptDatabase();
         }

         return false;
      }
   }

   public void removeEllipse(final Ellipse ellipse, final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.removeEllipse0(ellipse, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying removing ellipse from {} database in dimension: {} (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean removeEllipse0(final Ellipse ellipse, final class_5321<class_1937> dimension) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension, "ellipses");
            statement.executeUpdate("DELETE FROM \"" + var10001 + "\" WHERE centerX = " + ellipse.centerX() + " AND centerZ = " + ellipse.centerZ() + " AND radiusX = " + ellipse.radiusX() + " AND radiusZ = " + ellipse.radiusZ());
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

         return true;
      } catch (SQLException e) {
         XaeroPlus.LOGGER.error("Error removing ellipse from {} database in dimension: {}", new Object[]{this.databaseName, dimension.method_29177(), e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
            this.recoverCorruptDatabase();
         }

         return false;
      }
   }

   public void removeAllEllipses(final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.removeAllEllipses0(dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying removing all ellipses from {} database in dimension: {} (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean removeAllEllipses0(final class_5321<class_1937> dimension) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension, "ellipses");
            statement.executeUpdate("DELETE FROM \"" + var10001 + "\"");
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
         XaeroPlus.LOGGER.error("Error removing all ellipses from {} database in dimension: {}", new Object[]{this.databaseName, dimension.method_29177(), e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
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

         XaeroPlus.LOGGER.info("Retrying removing highlight from {} database in dimension: {} (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean removeHighlight0(final int x, final int z, final class_5321<class_1937> dimension) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            statement.executeUpdate("DELETE FROM \"" + this.getTableName(dimension, "highlights") + "\" WHERE x = " + x + " AND z = " + z);
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
               sb.append("DELETE FROM \"").append(this.getTableName(dimension, "highlights")).append("\" WHERE ");

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

   public void removeAllHighlights(final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.removeAllHighlights0(dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying removing all highlights from {} database in dimension: {} (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean removeAllHighlights0(final class_5321<class_1937> dimension) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension, "highlights");
            statement.executeUpdate("DELETE FROM \"" + var10001 + "\"");
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
         XaeroPlus.LOGGER.error("Error while removing all highlights from {} database in dimension: {}", new Object[]{this.databaseName, dimension.method_29177(), e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
            XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
            this.recoverCorruptDatabase();
         }

         return false;
      }
   }

   public void removeText(final int x, final int z, final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.removeText0(x, z, dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying removing text from {} database in dimension: {}, at {}, {} (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), x, z, tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean removeText0(final int x, final int z, final class_5321<class_1937> dimension) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            statement.executeUpdate("DELETE FROM \"" + this.getTableName(dimension, "texts") + "\" WHERE x = " + x + " AND z = " + z);
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
         XaeroPlus.LOGGER.error("Error while removing text from {} database in dimension: {}, at {}, {}", new Object[]{this.databaseName, dimension.method_29177(), x, z, e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
            XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
            this.recoverCorruptDatabase();
         }

         return false;
      }
   }

   public void removeAllTexts(final class_5321<class_1937> dimension) {
      int tryCount = 0;

      while(tryCount++ < 3) {
         if (this.removeAllTexts0(dimension)) {
            return;
         }

         XaeroPlus.LOGGER.info("Retrying removing all texts from {} database in dimension: {}, (attempt {}/{})", new Object[]{this.databaseName, dimension.method_29177(), tryCount, 3});
         Wait.waitMs(50);
      }

   }

   private boolean removeAllTexts0(final class_5321<class_1937> dimension) {
      try {
         Statement statement = this.connection.createStatement();

         try {
            String var10001 = this.getTableName(dimension, "texts");
            statement.executeUpdate("DELETE FROM \"" + var10001 + "\"");
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
         XaeroPlus.LOGGER.error("Error while removing all texts from {} database in dimension: {}", new Object[]{this.databaseName, dimension.method_29177(), e});
         if (e.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
            XaeroPlus.LOGGER.error("Corruption detected in {} database", this.databaseName, e);
            this.recoverCorruptDatabase();
         }

         return false;
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
   public interface EllipseConsumer {
      void accept(int centerX, int centerZ, int radiusX, int radiusZ, int color);
   }

   @FunctionalInterface
   public interface HighlightConsumer {
      void accept(int x, int z, int color);
   }

   @FunctionalInterface
   public interface LineConsumer {
      void accept(int x1, int z1, int x2, int z2, int color);
   }
}
