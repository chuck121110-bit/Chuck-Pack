package xaeroplus.feature.db;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_370;
import org.rfresh.sqlite.SQLiteConnection;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.util.NotificationUtil;

public class DatabaseMigrator {
   private static final MigrationMonitor MIGRATION_MONITOR = new MigrationMonitor();
   private static final Semaphore HEAVY_OPERATION_PERMITS = new Semaphore(1, true);
   private final List<DatabaseMigration> migrations;

   public DatabaseMigrator(final List<DatabaseMigration> migrations) {
      this.migrations = migrations;
   }

   public Connection migrate(Path dbPath, String databaseName, Connection connection, final boolean init) throws Exception {
      AtomicBoolean recoveryAttempted = new AtomicBoolean(false);

      while(true) {
         try {
            if (init) {
               return this.migrateInit(dbPath, databaseName, connection);
            }

            return this.migrateExisting(dbPath, databaseName, connection);
         } catch (Exception var7) {
            MIGRATION_MONITOR.onMigrationEnd(databaseName, false);
            if (!DatabaseMigration.isCorruptDatabase(var7) || !recoveryAttempted.compareAndSet(false, true)) {
               if (var7 instanceof InterruptedException) {
                  XaeroPlus.LOGGER.warn("Migration interrupted: {}", databaseName);
               } else {
                  XaeroPlus.LOGGER.error("Failed migrating database: {}", databaseName, var7);
                  NotificationUtil.inGameNotification("Database: " + databaseName + " failed to migrate!");
                  NotificationUtil.inGameNotification("More info will be in your log");
               }

               throw var7;
            }

            XaeroPlus.LOGGER.error("Corruption detected in {} database", databaseName, var7);
            connection = this.recoverCorruptDatabase(databaseName, dbPath, connection);
            NotificationUtil.inGameNotification("Database: " + databaseName + " recovered successfully! Retrying migration...");
         }
      }
   }

   private Connection migrateExisting(final Path dbPath, String databaseName, Connection connection) throws SQLException, InterruptedException {
      for(DatabaseMigration migration : this.migrations) {
         if (migration.shouldMigrate(databaseName, connection, false)) {
            long beforeMigration = System.nanoTime();
            XaeroPlus.LOGGER.info("Found database: {} that needs migration", databaseName);
            MIGRATION_MONITOR.onMigrationStart(databaseName);
            this.executeConcurrencyLimited(databaseName, "migration", () -> {
               this.validateAvailableDiskSpace(dbPath);
               long beforeBackup = System.nanoTime();
               String backupPath = this.backupDatabase(dbPath, databaseName, connection);
               long afterBackup = System.nanoTime();
               XaeroPlus.LOGGER.info("Backed up database: {} to {} in {} ms", new Object[]{databaseName, backupPath, TimeUnit.NANOSECONDS.toMillis(afterBackup - beforeBackup)});
               long beforeRunMigration = System.nanoTime();
               this.runMigration(databaseName, connection, migration, false);
               long afterRunMigration = System.nanoTime();
               XaeroPlus.LOGGER.info("Ran migration: {} in {} ms", migration.getClass().getSimpleName(), TimeUnit.NANOSECONDS.toMillis(afterRunMigration - beforeRunMigration));
               long beforeVacuum = System.nanoTime();
               this.vacuum(connection);
               long afterVacuum = System.nanoTime();
               XaeroPlus.LOGGER.info("Vacuumed database: {} in {} ms", databaseName, TimeUnit.NANOSECONDS.toMillis(afterVacuum - beforeVacuum));
            });
            long afterMigration = System.nanoTime();
            XaeroPlus.LOGGER.info("completed {} migration duration in {} ms", databaseName, TimeUnit.NANOSECONDS.toMillis(afterMigration - beforeMigration));
            MIGRATION_MONITOR.onMigrationEnd(databaseName, true);
         }
      }

      return connection;
   }

   private Connection migrateInit(final Path dbPath, String databaseName, Connection connection) throws SQLException, InterruptedException {
      for(DatabaseMigration migration : this.migrations) {
         if (migration.shouldMigrate(databaseName, connection, true)) {
            this.runMigration(databaseName, connection, migration, true);
         }
      }

      return connection;
   }

   private void runMigration(final String databaseName, final Connection connection, final DatabaseMigration migration, final boolean init) throws SQLException, InterruptedException {
      boolean committed = false;

      try {
         connection.setAutoCommit(false);
         migration.doMigration(databaseName, connection, init);
         if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Migration interrupted");
         }

         long beforeCommit = System.nanoTime();
         connection.commit();
         long afterCommit = System.nanoTime();
         if (!init) {
            XaeroPlus.LOGGER.info("Committed migration: {} in {} ms", migration.getClass().getSimpleName(), TimeUnit.NANOSECONDS.toMillis(afterCommit - beforeCommit));
         }

         committed = true;
      } finally {
         try {
            if (!committed) {
               connection.rollback();
            }

            connection.setAutoCommit(true);
         } catch (SQLException e) {
            if (!e.getMessage().contains("no transaction is active")) {
               XaeroPlus.LOGGER.error("Failed rolling back migration for database: {}", databaseName, e);
            }
         }

      }

   }

   private void executeConcurrencyLimited(final String databaseName, final String operation, final SqlOperation sqlOperation) throws SQLException, InterruptedException {
      boolean permitAcquired = HEAVY_OPERATION_PERMITS.tryAcquire(100L, TimeUnit.HOURS);
      if (!permitAcquired) {
         throw new RuntimeException("Failed to acquire permit for database " + operation + ": " + databaseName);
      } else {
         try {
            sqlOperation.run();
         } finally {
            HEAVY_OPERATION_PERMITS.release();
         }

      }
   }

   private Path getBackupPath(Path dbPath) {
      return dbPath.getParent().resolve("XaeroPlus-db-backups");
   }

   public String backupDatabase(Path dbPath, String databaseName, Connection connection) throws SQLException, InterruptedException {
      Path backupPath = this.getBackupPath(dbPath);
      if (!backupPath.toFile().exists()) {
         backupPath.toFile().mkdirs();
      }

      String dbBackupLocation = backupPath.resolve(databaseName + "-" + Instant.now().toEpochMilli() + ".db").toString();
      DatabaseMigration.executeCancellable(connection, "BACKUP TO '" + dbBackupLocation + "'");
      return dbBackupLocation;
   }

   private void vacuum(final Connection connection) throws SQLException, InterruptedException {
      DatabaseMigration.executeCancellable(connection, "VACUUM");
   }

   private Connection recoverCorruptDatabase(String databaseName, Path dbPath, Connection connection) throws Exception {
      NotificationUtil.inGameNotification("Database: " + databaseName + " is corrupt! Attempting to recover...");
      XaeroPlus.LOGGER.info("Attempting to recover corrupt database: {}", databaseName);
      Path recoveredDbPath = dbPath.getParent().resolve("recovered_" + databaseName + "-" + System.currentTimeMillis() + ".db");

      try {
         DatabaseMigration.executeCancellable(connection, "recover to \"" + String.valueOf(recoveredDbPath.toAbsolutePath()) + "\"");
         XaeroPlus.LOGGER.info("Wrote recovered database to: {}", recoveredDbPath);
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Error recovering corrupt database: {}", databaseName, e);
         NotificationUtil.inGameNotification("Database: " + databaseName + " failed to recover!");
         throw e;
      }

      try {
         connection.close();
         XaeroPlus.LOGGER.info("Closed DB connection to corrupt database: {}", databaseName);
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Error closing connection to corrupt database: {}", databaseName, e);
         throw e;
      }

      Path originalJournalDbPath = dbPath.getParent().resolve(String.valueOf(dbPath.getFileName()) + "-journal");
      Path recoveredJournalDbPath = recoveredDbPath.getParent().resolve(String.valueOf(recoveredDbPath.getFileName()) + "-journal");
      Path corruptedBackDbPath = dbPath.getParent().resolve("corrupted_" + databaseName + "-" + System.currentTimeMillis() + ".db");
      Path corruptedBackJournalDbPath = corruptedBackDbPath.getParent().resolve(String.valueOf(corruptedBackDbPath.getFileName()) + "-journal");
      CopyOption[] copyOptions;
      if (Globals.atomicMoveAvailable) {
         copyOptions = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE};
      } else {
         copyOptions = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};
      }

      try {
         Files.move(dbPath, corruptedBackDbPath, copyOptions);
         if (originalJournalDbPath.toFile().exists()) {
            Files.move(originalJournalDbPath, corruptedBackJournalDbPath, copyOptions);
         }

         Files.move(recoveredDbPath, dbPath, copyOptions);
         if (recoveredJournalDbPath.toFile().exists()) {
            Files.move(recoveredJournalDbPath, originalJournalDbPath, copyOptions);
         }

         XaeroPlus.LOGGER.info("Replaced corrupt database with recovered: {}", databaseName);
         connection = DriverManager.getConnection("jdbc:rfresh_sqlite:" + String.valueOf(dbPath));
         if (connection instanceof SQLiteConnection sqliteConnection) {
            sqliteConnection.setBusyTimeout(5000);
         }

         XaeroPlus.LOGGER.info("Opened DB connection to recovered database: {}", databaseName);
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Error reopening connection to recovered database: {}", databaseName, e);
         throw e;
      }

      try {
         Files.delete(corruptedBackDbPath);
         if (corruptedBackJournalDbPath.toFile().exists()) {
            Files.delete(corruptedBackJournalDbPath);
         }

         XaeroPlus.LOGGER.info("Deleted corrupted database backup: {}", corruptedBackDbPath);
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Error deleting corrupted backup database: {}", databaseName, e);
      }

      XaeroPlus.LOGGER.info("Completed recovering corrupt database: {}", databaseName);
      return connection;
   }

   private void validateAvailableDiskSpace(Path dbPath) {
      if (dbPath.toFile().exists()) {
         try {
            long dbSize = Files.size(dbPath);
            XaeroPlus.LOGGER.info("Database size: {} mb", dbSize / 1048576L);
            long freeSpace = dbPath.getParent().toFile().getUsableSpace();
            if (freeSpace < dbSize * 3L) {
               String var10002 = dbPath.toFile().getName();
               throw new RuntimeException("Not enough available disk space to migrate database: " + var10002 + " - " + freeSpace / 1048576L + "mb available");
            }
         } catch (IOException e) {
            throw new RuntimeException("Failed to check available disk space for database: " + dbPath.toFile().getName(), e);
         }
      }
   }

   public static class MigrationMonitor {
      private final Map<String, MigrationStatus> migrations = new HashMap();
      private final class_370.class_9037 toastId = new class_370.class_9037();
      boolean toastActive = false;

      public MigrationMonitor() {
         XaeroPlus.EVENT_BUS.register(this);
      }

      public synchronized void onMigrationStart(String id) {
         this.migrations.put(id, DatabaseMigrator.MigrationMonitor.MigrationStatus.IN_PROGRESS);
      }

      public synchronized void onMigrationEnd(String id, boolean success) {
         if (this.migrations.containsKey(id)) {
            this.migrations.put(id, success ? DatabaseMigrator.MigrationMonitor.MigrationStatus.COMPLETED : DatabaseMigrator.MigrationMonitor.MigrationStatus.FAILED);
         }

      }

      synchronized void reset() {
         this.migrations.clear();
         this.toastActive = false;
      }

      @EventHandler
      public synchronized void onTick(ClientTickEvent.Pre event) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1724 != null) {
            int inProgressMigrations = 0;
            int completedMigrations = 0;
            int failedMigrations = 0;

            for(MigrationStatus status : this.migrations.values()) {
               switch (status.ordinal()) {
                  case 0:
                     ++inProgressMigrations;
                     break;
                  case 1:
                     ++completedMigrations;
                     break;
                  case 2:
                     ++failedMigrations;
               }
            }

            if (inProgressMigrations <= 0) {
               if (this.toastActive) {
                  class_370.method_1990(mc.method_1566(), this.toastId, class_2561.method_43470("XaeroPlus"), class_2561.method_43469("xaeroplus.gui.toast.database_migration_done", new Object[]{completedMigrations, failedMigrations}));
               }

               this.toastActive = false;
            } else {
               class_370.method_1990(mc.method_1566(), this.toastId, class_2561.method_43470("XaeroPlus"), class_2561.method_43469("xaeroplus.gui.toast.database_migration_in_progress", new Object[]{inProgressMigrations, completedMigrations, failedMigrations}));
               this.toastActive = true;
            }
         }
      }

      @EventHandler(
         priority = 1000
      )
      public synchronized void onWorldChange(XaeroWorldChangeEvent event) {
         switch (event.worldChangeType()) {
            case EXIT_WORLD:
            case ENTER_WORLD:
               this.reset();
            default:
         }
      }

      static enum MigrationStatus {
         IN_PROGRESS,
         COMPLETED,
         FAILED;

         // $FF: synthetic method
         private static MigrationStatus[] $values() {
            return new MigrationStatus[]{IN_PROGRESS, COMPLETED, FAILED};
         }
      }
   }

   @FunctionalInterface
   private interface SqlOperation {
      void run() throws SQLException, InterruptedException;
   }
}
