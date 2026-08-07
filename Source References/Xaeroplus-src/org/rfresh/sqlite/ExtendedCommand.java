package org.rfresh.sqlite;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.rfresh.sqlite.core.DB;

public class ExtendedCommand {
   public static SQLExtension parse(String sql) throws SQLException {
      if (sql == null) {
         return null;
      } else if (sql.length() > 5 && sql.substring(0, 6).toLowerCase().equals("backup")) {
         return ExtendedCommand.BackupCommand.parse(sql);
      } else if (sql.length() > 6 && sql.substring(0, 7).toLowerCase().equals("restore")) {
         return ExtendedCommand.RestoreCommand.parse(sql);
      } else {
         return sql.length() > 6 && sql.substring(0, 7).toLowerCase().equals("recover") ? ExtendedCommand.RecoverCommand.parse(sql) : null;
      }
   }

   public static String removeQuotation(String s) {
      if (s == null) {
         return s;
      } else if (s.startsWith("\"") && s.endsWith("\"") || s.startsWith("'") && s.endsWith("'")) {
         return s.length() >= 2 ? s.substring(1, s.length() - 1) : s;
      } else {
         return s;
      }
   }

   public static class BackupCommand implements SQLExtension {
      public final String srcDB;
      public final String destFile;
      private static Pattern backupCmd = Pattern.compile("backup(\\s+(\"[^\"]*\"|'[^']*'|\\S+))?\\s+to\\s+(\"[^\"]*\"|'[^']*'|\\S+)", 2);

      public BackupCommand(String srcDB, String destFile) {
         this.srcDB = srcDB;
         this.destFile = destFile;
      }

      public static BackupCommand parse(String sql) throws SQLException {
         if (sql != null) {
            Matcher m = backupCmd.matcher(sql);
            if (m.matches()) {
               String dbName = ExtendedCommand.removeQuotation(m.group(2));
               String dest = ExtendedCommand.removeQuotation(m.group(3));
               if (dbName == null || dbName.length() == 0) {
                  dbName = "main";
               }

               return new BackupCommand(dbName, dest);
            }
         }

         throw new SQLException("syntax error: " + sql);
      }

      public void execute(DB db) throws SQLException {
         int rc = db.backup(this.srcDB, this.destFile, (DB.ProgressObserver)null);
         if (rc != SQLiteErrorCode.SQLITE_OK.code) {
            throw DB.newSQLException(rc, "Backup failed");
         }
      }
   }

   public static class RestoreCommand implements SQLExtension {
      public final String targetDB;
      public final String srcFile;
      private static Pattern restoreCmd = Pattern.compile("restore(\\s+(\"[^\"]*\"|'[^']*'|\\S+))?\\s+from\\s+(\"[^\"]*\"|'[^']*'|\\S+)", 2);

      public RestoreCommand(String targetDB, String srcFile) {
         this.targetDB = targetDB;
         this.srcFile = srcFile;
      }

      public static RestoreCommand parse(String sql) throws SQLException {
         if (sql != null) {
            Matcher m = restoreCmd.matcher(sql);
            if (m.matches()) {
               String dbName = ExtendedCommand.removeQuotation(m.group(2));
               String dest = ExtendedCommand.removeQuotation(m.group(3));
               if (dbName == null || dbName.length() == 0) {
                  dbName = "main";
               }

               return new RestoreCommand(dbName, dest);
            }
         }

         throw new SQLException("syntax error: " + sql);
      }

      public void execute(DB db) throws SQLException {
         int rc = db.restore(this.targetDB, this.srcFile, (DB.ProgressObserver)null);
         if (rc != SQLiteErrorCode.SQLITE_OK.code) {
            throw DB.newSQLException(rc, "Restore failed");
         }
      }
   }

   public static class RecoverCommand implements SQLExtension {
      private final String destFilePath;
      private static Pattern recoverCmd = Pattern.compile("recover(\\s+(\"[^\"]*\"|'[^']*'|\\S+))?\\s+to\\s+(\"[^\"]*\"|'[^']*'|\\S+)", 2);

      public RecoverCommand(String destFilePath) {
         this.destFilePath = destFilePath;
      }

      public static RecoverCommand parse(String sql) throws SQLException {
         if (sql != null) {
            Matcher m = recoverCmd.matcher(sql);
            if (m.matches()) {
               String dest = ExtendedCommand.removeQuotation(m.group(3));
               return new RecoverCommand(dest);
            }
         }

         throw new SQLException("syntax error: " + sql);
      }

      public void execute(DB db) throws SQLException {
         int rc = db.recoverDatabase(this.destFilePath);
         if (rc != SQLiteErrorCode.SQLITE_OK.code) {
            throw DB.newSQLException(rc, "Recover failed");
         }
      }
   }

   public interface SQLExtension {
      void execute(DB var1) throws SQLException;
   }
}
