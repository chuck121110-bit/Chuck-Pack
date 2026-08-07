package org.rfresh.sqlite.core;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.rfresh.sqlite.BusyHandler;
import org.rfresh.sqlite.Collation;
import org.rfresh.sqlite.Function;
import org.rfresh.sqlite.ProgressHandler;
import org.rfresh.sqlite.SQLiteCommitListener;
import org.rfresh.sqlite.SQLiteConfig;
import org.rfresh.sqlite.SQLiteErrorCode;
import org.rfresh.sqlite.SQLiteException;
import org.rfresh.sqlite.SQLiteUpdateListener;

public abstract class DB implements Codes {
   private final String url;
   private final String fileName;
   private final SQLiteConfig config;
   private final AtomicBoolean closed = new AtomicBoolean(true);
   volatile SafeStmtPtr begin;
   volatile SafeStmtPtr commit;
   private final Set<SafeStmtPtr> stmts = ConcurrentHashMap.newKeySet();
   private final Set<SQLiteUpdateListener> updateListeners = new HashSet();
   private final Set<SQLiteCommitListener> commitListeners = new HashSet();

   public DB(String url, String fileName, SQLiteConfig config) throws SQLException {
      this.url = url;
      this.fileName = fileName;
      this.config = config;
   }

   public String getUrl() {
      return this.url;
   }

   public boolean isClosed() {
      return this.closed.get();
   }

   public SQLiteConfig getConfig() {
      return this.config;
   }

   public abstract void interrupt() throws SQLException;

   public abstract void busy_timeout(int var1) throws SQLException;

   public abstract void busy_handler(BusyHandler var1) throws SQLException;

   abstract String errmsg() throws SQLException;

   public abstract String libversion() throws SQLException;

   public abstract long changes() throws SQLException;

   public abstract long total_changes() throws SQLException;

   public abstract int shared_cache(boolean var1) throws SQLException;

   public abstract int enable_load_extension(boolean var1) throws SQLException;

   public final synchronized void exec(String sql, boolean autoCommit) throws SQLException {
      SafeStmtPtr pointer = this.prepare(sql);

      try {
         int rc = pointer.safeRunInt(DB::step);
         switch (rc) {
            case 100:
               return;
            case 101:
               this.ensureAutoCommit(autoCommit);
               return;
            default:
               this.throwex(rc);
         }
      } finally {
         pointer.close();
      }
   }

   public final synchronized void open(String file, int openFlags) throws SQLException {
      this._open(file, openFlags);
      this.closed.set(false);
      if (this.fileName.startsWith("file:") && !this.fileName.contains("cache=")) {
         this.shared_cache(this.config.isEnabledSharedCache());
      }

      this.enable_load_extension(this.config.isEnabledLoadExtension());
      this.busy_timeout(this.config.getBusyTimeout());
   }

   public final synchronized void close() throws SQLException {
      for(SafeStmtPtr element : this.stmts) {
         element.close();
      }

      if (this.begin != null) {
         this.begin.close();
      }

      if (this.commit != null) {
         this.commit.close();
      }

      this.closed.set(true);
      this._close();
   }

   public final synchronized void prepare(CoreStatement stmt) throws SQLException {
      if (stmt.sql == null) {
         throw new NullPointerException();
      } else {
         if (stmt.pointer != null) {
            stmt.pointer.close();
         }

         stmt.pointer = this.prepare(stmt.sql);
         boolean added = this.stmts.add(stmt.pointer);
         if (!added) {
            throw new IllegalStateException("Already added pointer to statements set");
         }
      }
   }

   public synchronized int finalize(SafeStmtPtr safePtr, long ptr) throws SQLException {
      int var4;
      try {
         var4 = this.finalize(ptr);
      } finally {
         this.stmts.remove(safePtr);
      }

      return var4;
   }

   protected abstract void _open(String var1, int var2) throws SQLException;

   protected abstract void _close() throws SQLException;

   public abstract int _exec(String var1) throws SQLException;

   protected abstract SafeStmtPtr prepare(String var1) throws SQLException;

   protected abstract int finalize(long var1) throws SQLException;

   public abstract int step(long var1) throws SQLException;

   public abstract int reset(long var1) throws SQLException;

   public abstract int clear_bindings(long var1) throws SQLException;

   abstract int bind_parameter_count(long var1) throws SQLException;

   public abstract int column_count(long var1) throws SQLException;

   public abstract int column_type(long var1, int var3) throws SQLException;

   public abstract String column_decltype(long var1, int var3) throws SQLException;

   public abstract String column_table_name(long var1, int var3) throws SQLException;

   public abstract String column_name(long var1, int var3) throws SQLException;

   public abstract String column_text(long var1, int var3) throws SQLException;

   public abstract byte[] column_blob(long var1, int var3) throws SQLException;

   public abstract double column_double(long var1, int var3) throws SQLException;

   public abstract long column_long(long var1, int var3) throws SQLException;

   public abstract int column_int(long var1, int var3) throws SQLException;

   abstract int bind_null(long var1, int var3) throws SQLException;

   abstract int bind_int(long var1, int var3, int var4) throws SQLException;

   abstract int bind_long(long var1, int var3, long var4) throws SQLException;

   abstract int bind_double(long var1, int var3, double var4) throws SQLException;

   abstract int bind_text(long var1, int var3, String var4) throws SQLException;

   abstract int bind_blob(long var1, int var3, byte[] var4) throws SQLException;

   public abstract void result_null(long var1) throws SQLException;

   public abstract void result_text(long var1, String var3) throws SQLException;

   public abstract void result_blob(long var1, byte[] var3) throws SQLException;

   public abstract void result_double(long var1, double var3) throws SQLException;

   public abstract void result_long(long var1, long var3) throws SQLException;

   public abstract void result_int(long var1, int var3) throws SQLException;

   public abstract void result_error(long var1, String var3) throws SQLException;

   public abstract String value_text(Function var1, int var2) throws SQLException;

   public abstract byte[] value_blob(Function var1, int var2) throws SQLException;

   public abstract double value_double(Function var1, int var2) throws SQLException;

   public abstract long value_long(Function var1, int var2) throws SQLException;

   public abstract int value_int(Function var1, int var2) throws SQLException;

   public abstract int value_type(Function var1, int var2) throws SQLException;

   public abstract int create_function(String var1, Function var2, int var3, int var4) throws SQLException;

   public abstract int destroy_function(String var1) throws SQLException;

   public abstract int create_collation(String var1, Collation var2) throws SQLException;

   public abstract int destroy_collation(String var1) throws SQLException;

   public abstract int backup(String var1, String var2, ProgressObserver var3) throws SQLException;

   public abstract int backup(String var1, String var2, ProgressObserver var3, int var4, int var5, int var6) throws SQLException;

   public abstract int restore(String var1, String var2, ProgressObserver var3) throws SQLException;

   public abstract int restore(String var1, String var2, ProgressObserver var3, int var4, int var5, int var6) throws SQLException;

   public abstract int recoverDatabase(String var1) throws SQLException;

   public abstract int limit(int var1, int var2) throws SQLException;

   public abstract void register_progress_handler(int var1, ProgressHandler var2) throws SQLException;

   public abstract void clear_progress_handler() throws SQLException;

   abstract boolean[][] column_metadata(long var1) throws SQLException;

   public final synchronized String[] column_names(long stmt) throws SQLException {
      String[] names = new String[this.column_count(stmt)];

      for(int i = 0; i < names.length; ++i) {
         names[i] = this.column_name(stmt, i);
      }

      return names;
   }

   final synchronized int sqlbind(long stmt, int pos, Object v) throws SQLException {
      ++pos;
      if (v == null) {
         return this.bind_null(stmt, pos);
      } else if (v instanceof Integer) {
         return this.bind_int(stmt, pos, (Integer)v);
      } else if (v instanceof Short) {
         return this.bind_int(stmt, pos, ((Short)v).intValue());
      } else if (v instanceof Long) {
         return this.bind_long(stmt, pos, (Long)v);
      } else if (v instanceof Float) {
         return this.bind_double(stmt, pos, ((Float)v).doubleValue());
      } else if (v instanceof Double) {
         return this.bind_double(stmt, pos, (Double)v);
      } else if (v instanceof String) {
         return this.bind_text(stmt, pos, (String)v);
      } else if (v instanceof byte[]) {
         return this.bind_blob(stmt, pos, (byte[])v);
      } else {
         throw new SQLException("unexpected param type: " + v.getClass());
      }
   }

   final synchronized long[] executeBatch(SafeStmtPtr stmt, int count, Object[] vals, boolean autoCommit) throws SQLException {
      return (long[])stmt.safeRun((db, ptr) -> this.executeBatch(ptr, count, vals, autoCommit));
   }

   private synchronized long[] executeBatch(long stmt, int count, Object[] vals, boolean autoCommit) throws SQLException {
      if (count < 1) {
         throw new SQLException("count (" + count + ") < 1");
      } else {
         int params = this.bind_parameter_count(stmt);
         long[] changes = new long[count];

         try {
            for(int i = 0; i < count; ++i) {
               this.reset(stmt);

               for(int j = 0; j < params; ++j) {
                  int rc = this.sqlbind(stmt, j, vals[i * params + j]);
                  if (rc != 0) {
                     this.throwex(rc);
                  }
               }

               int rc = this.step(stmt);
               if (rc != 101) {
                  this.reset(stmt);
                  if (rc == 100) {
                     throw new BatchUpdateException("batch entry " + i + ": query returns results", (String)null, 0, Arrays.stream(changes).mapToInt((l) -> (int)l).toArray(), (Throwable)null);
                  }

                  this.throwex(rc);
               }

               changes[i] = this.changes();
            }
         } finally {
            this.ensureAutoCommit(autoCommit);
         }

         this.reset(stmt);
         return changes;
      }
   }

   public final synchronized boolean execute(CoreStatement stmt, Object[] vals) throws SQLException {
      int statusCode = stmt.pointer.safeRunInt((db, ptr) -> this.execute(ptr, vals));
      switch (statusCode & 255) {
         case 5:
         case 6:
         case 19:
         case 21:
            throw this.newSQLException(statusCode);
         case 100:
            return true;
         case 101:
            this.ensureAutoCommit(stmt.conn.getAutoCommit());
            return false;
         default:
            stmt.pointer.close();
            throw this.newSQLException(statusCode);
      }
   }

   private synchronized int execute(long ptr, Object[] vals) throws SQLException {
      if (vals != null) {
         int params = this.bind_parameter_count(ptr);
         if (params > vals.length) {
            throw new SQLException("assertion failure: param count (" + params + ") > value count (" + vals.length + ")");
         }

         for(int i = 0; i < params; ++i) {
            int rc = this.sqlbind(ptr, i, vals[i]);
            if (rc != 0) {
               this.throwex(rc);
            }
         }
      }

      int statusCode = this.step(ptr);
      if ((statusCode & 255) == 101) {
         this.reset(ptr);
      }

      return statusCode;
   }

   final synchronized boolean execute(String sql, boolean autoCommit) throws SQLException {
      int statusCode = this._exec(sql);
      switch (statusCode) {
         case 0:
            return false;
         case 100:
            return true;
         case 101:
            this.ensureAutoCommit(autoCommit);
            return false;
         default:
            throw this.newSQLException(statusCode);
      }
   }

   public final synchronized long executeUpdate(CoreStatement stmt, Object[] vals) throws SQLException {
      try {
         if (this.execute(stmt, vals)) {
            throw new SQLException("query returns results");
         }
      } finally {
         if (!stmt.pointer.isClosed()) {
            stmt.pointer.safeRunInt(DB::reset);
         }

      }

      return this.changes();
   }

   abstract void set_commit_listener(boolean var1);

   abstract void set_update_listener(boolean var1);

   public synchronized void addUpdateListener(SQLiteUpdateListener listener) {
      if (this.updateListeners.add(listener) && this.updateListeners.size() == 1) {
         this.set_update_listener(true);
      }

   }

   public synchronized void addCommitListener(SQLiteCommitListener listener) {
      if (this.commitListeners.add(listener) && this.commitListeners.size() == 1) {
         this.set_commit_listener(true);
      }

   }

   public synchronized void removeUpdateListener(SQLiteUpdateListener listener) {
      if (this.updateListeners.remove(listener) && this.updateListeners.isEmpty()) {
         this.set_update_listener(false);
      }

   }

   public synchronized void removeCommitListener(SQLiteCommitListener listener) {
      if (this.commitListeners.remove(listener) && this.commitListeners.isEmpty()) {
         this.set_commit_listener(false);
      }

   }

   void onUpdate(int type, String database, String table, long rowId) {
      Set<SQLiteUpdateListener> listeners;
      synchronized(this) {
         listeners = new HashSet(this.updateListeners);
      }

      for(SQLiteUpdateListener listener : listeners) {
         SQLiteUpdateListener.Type operationType;
         switch (type) {
            case 9:
               operationType = SQLiteUpdateListener.Type.DELETE;
               break;
            case 18:
               operationType = SQLiteUpdateListener.Type.INSERT;
               break;
            case 23:
               operationType = SQLiteUpdateListener.Type.UPDATE;
               break;
            default:
               throw new AssertionError("Unknown type: " + type);
         }

         listener.onUpdate(operationType, database, table, rowId);
      }

   }

   void onCommit(boolean commit) {
      Set<SQLiteCommitListener> listeners;
      synchronized(this) {
         listeners = new HashSet(this.commitListeners);
      }

      for(SQLiteCommitListener listener : listeners) {
         if (commit) {
            listener.onCommit();
         } else {
            listener.onRollback();
         }
      }

   }

   final void throwex() throws SQLException {
      throw new SQLException(this.errmsg());
   }

   public final void throwex(int errorCode) throws SQLException {
      throw this.newSQLException(errorCode);
   }

   static void throwex(int errorCode, String errorMessage) throws SQLException {
      throw newSQLException(errorCode, errorMessage);
   }

   public static SQLiteException newSQLException(int errorCode, String errorMessage) {
      SQLiteErrorCode code = SQLiteErrorCode.getErrorCode(errorCode);
      String msg;
      if (code == SQLiteErrorCode.UNKNOWN_ERROR) {
         msg = String.format("%s:%s (%s)", code, errorCode, errorMessage);
      } else {
         msg = String.format("%s (%s)", code, errorMessage);
      }

      return new SQLiteException(msg, code);
   }

   private SQLiteException newSQLException(int errorCode) throws SQLException {
      return newSQLException(errorCode, this.errmsg());
   }

   final void ensureAutoCommit(boolean autoCommit) throws SQLException {
      if (autoCommit) {
         this.ensureBeginAndCommit();
         this.begin.safeRunConsume((db, beginPtr) -> this.commit.safeRunConsume((db2, commitPtr) -> this.ensureAutocommit(beginPtr, commitPtr)));
      }
   }

   private void ensureBeginAndCommit() throws SQLException {
      if (this.begin == null) {
         synchronized(this) {
            if (this.begin == null) {
               this.begin = this.prepare("begin;");
            }
         }
      }

      if (this.commit == null) {
         synchronized(this) {
            if (this.commit == null) {
               this.commit = this.prepare("commit;");
            }
         }
      }

   }

   private void ensureAutocommit(long beginPtr, long commitPtr) throws SQLException {
      try {
         if (this.step(beginPtr) == 101) {
            int rc = this.step(commitPtr);
            if (rc != 101) {
               this.reset(commitPtr);
               this.throwex(rc);
            }

            return;
         }
      } finally {
         this.reset(beginPtr);
         this.reset(commitPtr);
      }

   }

   public abstract byte[] serialize(String var1) throws SQLException;

   public abstract void deserialize(String var1, byte[] var2) throws SQLException;

   public interface ProgressObserver {
      void progress(int var1, int var2);
   }
}
