package xaeroplus.feature.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.rfresh.sqlite.SQLiteErrorCode;

public interface DatabaseMigration {
   boolean shouldMigrate(String databaseName, Connection connection, boolean init) throws SQLException;

   void doMigration(String databaseName, Connection connection, boolean init) throws SQLException, InterruptedException;

   static boolean isCorruptDatabase(final Throwable throwable) {
      for(Throwable current = throwable; current != null; current = current.getCause()) {
         if (current instanceof SQLException sqlException) {
            if (sqlException.getErrorCode() == SQLiteErrorCode.SQLITE_CORRUPT.code) {
               return true;
            }
         }
      }

      return false;
   }

   static void executeCancellable(final Connection connection, final String sql) throws SQLException, InterruptedException {
      Statement statement = connection.createStatement();

      try {
         CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
               statement.execute(sql);
            } catch (SQLException e) {
               throw new RuntimeException(e);
            }
         });

         while(!future.isDone()) {
            try {
               Thread.sleep(100L);
            } catch (InterruptedException e) {
               statement.cancel();
               throw e;
            }
         }

         if (future.isCompletedExceptionally()) {
            try {
               future.join();
            } catch (CancellationException var9) {
            } catch (CompletionException e) {
               Throwable cause = e.getCause();
               if (cause instanceof SQLException) {
                  SQLException sqlException = (SQLException)cause;
                  throw sqlException;
               }

               if (cause instanceof InterruptedException) {
                  InterruptedException interruptedException = (InterruptedException)cause;
                  throw interruptedException;
               }

               throw e;
            }
         }
      } catch (Throwable var11) {
         if (statement != null) {
            try {
               statement.close();
            } catch (Throwable var7) {
               var11.addSuppressed(var7);
            }
         }

         throw var11;
      }

      if (statement != null) {
         statement.close();
      }

   }
}
