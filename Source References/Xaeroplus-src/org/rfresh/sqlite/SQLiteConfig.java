package org.rfresh.sqlite;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class SQLiteConfig {
   public static final String DEFAULT_DATE_STRING_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
   private static final int DEFAULT_MAX_LENGTH = 1000000000;
   private static final int DEFAULT_MAX_COLUMN = 2000;
   private static final int DEFAULT_MAX_SQL_LENGTH = 1000000;
   private static final int DEFAULT_MAX_FUNCTION_ARG = 100;
   private static final int DEFAULT_MAX_ATTACHED = 10;
   private static final int DEFAULT_MAX_PAGE_COUNT = 1073741823;
   private final Properties pragmaTable;
   private int openModeFlag;
   private int busyTimeout;
   private boolean explicitReadOnly;
   private final SQLiteConnectionConfig defaultConnectionConfig;
   static final Set<String> pragmaSet = new TreeSet();

   public SQLiteConfig() {
      this(new Properties());
   }

   public SQLiteConfig(Properties prop) {
      this.openModeFlag = 0;
      this.pragmaTable = prop;
      String openMode = this.pragmaTable.getProperty(SQLiteConfig.Pragma.OPEN_MODE.pragmaName);
      if (openMode != null) {
         this.openModeFlag = Integer.parseInt(openMode);
      } else {
         this.setOpenMode(SQLiteOpenMode.READWRITE);
         this.setOpenMode(SQLiteOpenMode.CREATE);
      }

      this.setSharedCache(Boolean.parseBoolean(this.pragmaTable.getProperty(SQLiteConfig.Pragma.SHARED_CACHE.pragmaName, "false")));
      this.setOpenMode(SQLiteOpenMode.OPEN_URI);
      this.setBusyTimeout(Integer.parseInt(this.pragmaTable.getProperty(SQLiteConfig.Pragma.BUSY_TIMEOUT.pragmaName, "3000")));
      this.defaultConnectionConfig = SQLiteConnectionConfig.fromPragmaTable(this.pragmaTable);
      this.explicitReadOnly = Boolean.parseBoolean(this.pragmaTable.getProperty(SQLiteConfig.Pragma.JDBC_EXPLICIT_READONLY.pragmaName, "false"));
   }

   public SQLiteConnectionConfig newConnectionConfig() {
      return this.defaultConnectionConfig.copyConfig();
   }

   public Connection createConnection(String url) throws SQLException {
      return JDBC.createConnection(url, this.toProperties());
   }

   public void apply(Connection conn) throws SQLException {
      HashSet<String> pragmaParams = new HashSet();

      for(Pragma each : SQLiteConfig.Pragma.values()) {
         pragmaParams.add(each.pragmaName);
      }

      if (conn instanceof SQLiteConnection) {
         SQLiteConnection sqliteConn = (SQLiteConnection)conn;
         sqliteConn.setLimit(SQLiteLimits.SQLITE_LIMIT_ATTACHED, this.parseLimitPragma(SQLiteConfig.Pragma.LIMIT_ATTACHED, 10));
         sqliteConn.setLimit(SQLiteLimits.SQLITE_LIMIT_COLUMN, this.parseLimitPragma(SQLiteConfig.Pragma.LIMIT_COLUMN, 2000));
         sqliteConn.setLimit(SQLiteLimits.SQLITE_LIMIT_COMPOUND_SELECT, this.parseLimitPragma(SQLiteConfig.Pragma.LIMIT_COMPOUND_SELECT, -1));
         sqliteConn.setLimit(SQLiteLimits.SQLITE_LIMIT_EXPR_DEPTH, this.parseLimitPragma(SQLiteConfig.Pragma.LIMIT_EXPR_DEPTH, -1));
         sqliteConn.setLimit(SQLiteLimits.SQLITE_LIMIT_FUNCTION_ARG, this.parseLimitPragma(SQLiteConfig.Pragma.LIMIT_FUNCTION_ARG, 100));
         sqliteConn.setLimit(SQLiteLimits.SQLITE_LIMIT_LENGTH, this.parseLimitPragma(SQLiteConfig.Pragma.LIMIT_LENGTH, 1000000000));
         sqliteConn.setLimit(SQLiteLimits.SQLITE_LIMIT_LIKE_PATTERN_LENGTH, this.parseLimitPragma(SQLiteConfig.Pragma.LIMIT_LIKE_PATTERN_LENGTH, -1));
         sqliteConn.setLimit(SQLiteLimits.SQLITE_LIMIT_SQL_LENGTH, this.parseLimitPragma(SQLiteConfig.Pragma.LIMIT_SQL_LENGTH, 1000000));
         sqliteConn.setLimit(SQLiteLimits.SQLITE_LIMIT_TRIGGER_DEPTH, this.parseLimitPragma(SQLiteConfig.Pragma.LIMIT_TRIGGER_DEPTH, -1));
         sqliteConn.setLimit(SQLiteLimits.SQLITE_LIMIT_VARIABLE_NUMBER, this.parseLimitPragma(SQLiteConfig.Pragma.LIMIT_VARIABLE_NUMBER, -1));
         sqliteConn.setLimit(SQLiteLimits.SQLITE_LIMIT_VDBE_OP, this.parseLimitPragma(SQLiteConfig.Pragma.LIMIT_VDBE_OP, -1));
         sqliteConn.setLimit(SQLiteLimits.SQLITE_LIMIT_WORKER_THREADS, this.parseLimitPragma(SQLiteConfig.Pragma.LIMIT_WORKER_THREADS, -1));
         sqliteConn.setLimit(SQLiteLimits.SQLITE_LIMIT_PAGE_COUNT, this.parseLimitPragma(SQLiteConfig.Pragma.LIMIT_PAGE_COUNT, 1073741823));
      }

      pragmaParams.remove(SQLiteConfig.Pragma.OPEN_MODE.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.SHARED_CACHE.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LOAD_EXTENSION.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.DATE_PRECISION.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.DATE_CLASS.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.DATE_STRING_FORMAT.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.PASSWORD.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.HEXKEY_MODE.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LIMIT_ATTACHED.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LIMIT_COLUMN.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LIMIT_COMPOUND_SELECT.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LIMIT_EXPR_DEPTH.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LIMIT_FUNCTION_ARG.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LIMIT_LENGTH.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LIMIT_LIKE_PATTERN_LENGTH.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LIMIT_SQL_LENGTH.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LIMIT_TRIGGER_DEPTH.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LIMIT_VARIABLE_NUMBER.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LIMIT_VDBE_OP.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LIMIT_WORKER_THREADS.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.LIMIT_PAGE_COUNT.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.JDBC_EXPLICIT_READONLY.pragmaName);
      pragmaParams.remove(SQLiteConfig.Pragma.JDBC_GET_GENERATED_KEYS.pragmaName);
      Statement stat = conn.createStatement();

      try {
         if (this.pragmaTable.containsKey(SQLiteConfig.Pragma.PASSWORD.pragmaName)) {
            String password = this.pragmaTable.getProperty(SQLiteConfig.Pragma.PASSWORD.pragmaName);
            if (password != null && !password.isEmpty()) {
               String hexkeyMode = this.pragmaTable.getProperty(SQLiteConfig.Pragma.HEXKEY_MODE.pragmaName);
               String passwordPragma;
               if (SQLiteConfig.HexKeyMode.SSE.name().equalsIgnoreCase(hexkeyMode)) {
                  passwordPragma = "pragma hexkey = '%s'";
               } else if (SQLiteConfig.HexKeyMode.SQLCIPHER.name().equalsIgnoreCase(hexkeyMode)) {
                  passwordPragma = "pragma key = \"x'%s'\"";
               } else {
                  passwordPragma = "pragma key = '%s'";
               }

               stat.execute(String.format(passwordPragma, password.replace("'", "''")));
               stat.execute("select 1 from sqlite_schema");
            }
         }

         for(Object each : this.pragmaTable.keySet()) {
            String key = each.toString();
            if (pragmaParams.contains(key)) {
               String value = this.pragmaTable.getProperty(key);
               if (value != null) {
                  stat.execute(String.format("pragma %s=%s", key, value));
               }
            }
         }
      } finally {
         if (stat != null) {
            stat.close();
         }

      }

   }

   private void set(Pragma pragma, boolean flag) {
      this.setPragma(pragma, Boolean.toString(flag));
   }

   private void set(Pragma pragma, int num) {
      this.setPragma(pragma, Integer.toString(num));
   }

   private boolean getBoolean(Pragma pragma, String defaultValue) {
      return Boolean.parseBoolean(this.pragmaTable.getProperty(pragma.pragmaName, defaultValue));
   }

   private int parseLimitPragma(Pragma pragma, int defaultValue) {
      if (!this.pragmaTable.containsKey(pragma.pragmaName)) {
         return defaultValue;
      } else {
         String valueString = this.pragmaTable.getProperty(pragma.pragmaName);

         try {
            return Integer.parseInt(valueString);
         } catch (NumberFormatException var5) {
            return defaultValue;
         }
      }
   }

   public boolean isEnabledSharedCache() {
      return this.getBoolean(SQLiteConfig.Pragma.SHARED_CACHE, "false");
   }

   public boolean isEnabledLoadExtension() {
      return this.getBoolean(SQLiteConfig.Pragma.LOAD_EXTENSION, "false");
   }

   public int getOpenModeFlags() {
      return this.openModeFlag;
   }

   public void setPragma(Pragma pragma, String value) {
      this.pragmaTable.put(pragma.pragmaName, value);
   }

   public Properties toProperties() {
      this.pragmaTable.setProperty(SQLiteConfig.Pragma.OPEN_MODE.pragmaName, Integer.toString(this.openModeFlag));
      this.pragmaTable.setProperty(SQLiteConfig.Pragma.TRANSACTION_MODE.pragmaName, this.defaultConnectionConfig.getTransactionMode().getValue());
      this.pragmaTable.setProperty(SQLiteConfig.Pragma.DATE_CLASS.pragmaName, this.defaultConnectionConfig.getDateClass().getValue());
      this.pragmaTable.setProperty(SQLiteConfig.Pragma.DATE_PRECISION.pragmaName, this.defaultConnectionConfig.getDatePrecision().getValue());
      this.pragmaTable.setProperty(SQLiteConfig.Pragma.DATE_STRING_FORMAT.pragmaName, this.defaultConnectionConfig.getDateStringFormat());
      this.pragmaTable.setProperty(SQLiteConfig.Pragma.JDBC_EXPLICIT_READONLY.pragmaName, this.explicitReadOnly ? "true" : "false");
      this.pragmaTable.setProperty(SQLiteConfig.Pragma.JDBC_GET_GENERATED_KEYS.pragmaName, this.defaultConnectionConfig.isGetGeneratedKeys() ? "true" : "false");
      return this.pragmaTable;
   }

   static DriverPropertyInfo[] getDriverPropertyInfo() {
      Pragma[] pragma = SQLiteConfig.Pragma.values();
      DriverPropertyInfo[] result = new DriverPropertyInfo[pragma.length];
      int index = 0;

      for(Pragma p : SQLiteConfig.Pragma.values()) {
         DriverPropertyInfo di = new DriverPropertyInfo(p.pragmaName, (String)null);
         di.choices = p.choices;
         di.description = p.description;
         di.required = false;
         result[index++] = di;
      }

      return result;
   }

   public boolean isExplicitReadOnly() {
      return this.explicitReadOnly;
   }

   public void setExplicitReadOnly(boolean readOnly) {
      this.explicitReadOnly = readOnly;
   }

   public void setOpenMode(SQLiteOpenMode mode) {
      this.openModeFlag |= mode.flag;
   }

   public void resetOpenMode(SQLiteOpenMode mode) {
      this.openModeFlag &= ~mode.flag;
   }

   public void setSharedCache(boolean enable) {
      this.set(SQLiteConfig.Pragma.SHARED_CACHE, enable);
   }

   public void enableLoadExtension(boolean enable) {
      this.set(SQLiteConfig.Pragma.LOAD_EXTENSION, enable);
   }

   public void setReadOnly(boolean readOnly) {
      if (readOnly) {
         this.setOpenMode(SQLiteOpenMode.READONLY);
         this.resetOpenMode(SQLiteOpenMode.CREATE);
         this.resetOpenMode(SQLiteOpenMode.READWRITE);
      } else {
         this.setOpenMode(SQLiteOpenMode.READWRITE);
         this.setOpenMode(SQLiteOpenMode.CREATE);
         this.resetOpenMode(SQLiteOpenMode.READONLY);
      }

   }

   public void setCacheSize(int numberOfPages) {
      this.set(SQLiteConfig.Pragma.CACHE_SIZE, numberOfPages);
   }

   public void enableCaseSensitiveLike(boolean enable) {
      this.set(SQLiteConfig.Pragma.CASE_SENSITIVE_LIKE, enable);
   }

   /** @deprecated */
   @Deprecated
   public void enableCountChanges(boolean enable) {
      this.set(SQLiteConfig.Pragma.COUNT_CHANGES, enable);
   }

   public void setDefaultCacheSize(int numberOfPages) {
      this.set(SQLiteConfig.Pragma.DEFAULT_CACHE_SIZE, numberOfPages);
   }

   public void deferForeignKeys(boolean enable) {
      this.set(SQLiteConfig.Pragma.DEFER_FOREIGN_KEYS, enable);
   }

   /** @deprecated */
   @Deprecated
   public void enableEmptyResultCallBacks(boolean enable) {
      this.set(SQLiteConfig.Pragma.EMPTY_RESULT_CALLBACKS, enable);
   }

   public void setEncoding(Encoding encoding) {
      this.setPragma(SQLiteConfig.Pragma.ENCODING, encoding.typeName);
   }

   public void enforceForeignKeys(boolean enforce) {
      this.set(SQLiteConfig.Pragma.FOREIGN_KEYS, enforce);
   }

   /** @deprecated */
   @Deprecated
   public void enableFullColumnNames(boolean enable) {
      this.set(SQLiteConfig.Pragma.FULL_COLUMN_NAMES, enable);
   }

   public void enableFullSync(boolean enable) {
      this.set(SQLiteConfig.Pragma.FULL_SYNC, enable);
   }

   public void incrementalVacuum(int numberOfPagesToBeRemoved) {
      this.set(SQLiteConfig.Pragma.INCREMENTAL_VACUUM, numberOfPagesToBeRemoved);
   }

   public void setJournalMode(JournalMode mode) {
      this.setPragma(SQLiteConfig.Pragma.JOURNAL_MODE, mode.name());
   }

   public void setJournalSizeLimit(int limit) {
      this.set(SQLiteConfig.Pragma.JOURNAL_SIZE_LIMIT, limit);
   }

   public void useLegacyFileFormat(boolean use) {
      this.set(SQLiteConfig.Pragma.LEGACY_FILE_FORMAT, use);
   }

   public void setLegacyAlterTable(boolean flag) {
      this.set(SQLiteConfig.Pragma.LEGACY_ALTER_TABLE, flag);
   }

   public void setLockingMode(LockingMode mode) {
      this.setPragma(SQLiteConfig.Pragma.LOCKING_MODE, mode.name());
   }

   public void setPageSize(int numBytes) {
      this.set(SQLiteConfig.Pragma.PAGE_SIZE, numBytes);
   }

   public void setMaxPageCount(int numPages) {
      this.set(SQLiteConfig.Pragma.MAX_PAGE_COUNT, numPages);
   }

   public void setReadUncommitted(boolean useReadUncommittedIsolationMode) {
      this.set(SQLiteConfig.Pragma.READ_UNCOMMITTED, useReadUncommittedIsolationMode);
   }

   public void enableRecursiveTriggers(boolean enable) {
      this.set(SQLiteConfig.Pragma.RECURSIVE_TRIGGERS, enable);
   }

   public void enableReverseUnorderedSelects(boolean enable) {
      this.set(SQLiteConfig.Pragma.REVERSE_UNORDERED_SELECTS, enable);
   }

   public void enableShortColumnNames(boolean enable) {
      this.set(SQLiteConfig.Pragma.SHORT_COLUMN_NAMES, enable);
   }

   public void setSynchronous(SynchronousMode mode) {
      this.setPragma(SQLiteConfig.Pragma.SYNCHRONOUS, mode.name());
   }

   public void setHexKeyMode(HexKeyMode mode) {
      this.setPragma(SQLiteConfig.Pragma.HEXKEY_MODE, mode.name());
   }

   public void setTempStore(TempStore storeType) {
      this.setPragma(SQLiteConfig.Pragma.TEMP_STORE, storeType.name());
   }

   public void setTempStoreDirectory(String directoryName) {
      this.setPragma(SQLiteConfig.Pragma.TEMP_STORE_DIRECTORY, String.format("'%s'", directoryName));
   }

   public void setUserVersion(int version) {
      this.set(SQLiteConfig.Pragma.USER_VERSION, version);
   }

   public void setApplicationId(int id) {
      this.set(SQLiteConfig.Pragma.APPLICATION_ID, id);
   }

   public void setWalAutocheckpoint(int pages) {
      this.set(SQLiteConfig.Pragma.WAL_AUTOCHECKPOINT, pages);
   }

   public void setTransactionMode(TransactionMode transactionMode) {
      this.defaultConnectionConfig.setTransactionMode(transactionMode);
   }

   public void setTransactionMode(String transactionMode) {
      this.setTransactionMode(SQLiteConfig.TransactionMode.getMode(transactionMode));
   }

   public TransactionMode getTransactionMode() {
      return this.defaultConnectionConfig.getTransactionMode();
   }

   public void setDatePrecision(String datePrecision) {
      this.defaultConnectionConfig.setDatePrecision(SQLiteConfig.DatePrecision.getPrecision(datePrecision));
   }

   public void setDateClass(String dateClass) {
      this.defaultConnectionConfig.setDateClass(SQLiteConfig.DateClass.getDateClass(dateClass));
   }

   public void setDateStringFormat(String dateStringFormat) {
      this.defaultConnectionConfig.setDateStringFormat(dateStringFormat);
   }

   public void setBusyTimeout(int milliseconds) {
      this.setPragma(SQLiteConfig.Pragma.BUSY_TIMEOUT, Integer.toString(milliseconds));
      this.busyTimeout = milliseconds;
   }

   public int getBusyTimeout() {
      return this.busyTimeout;
   }

   public boolean isGetGeneratedKeys() {
      return this.defaultConnectionConfig.isGetGeneratedKeys();
   }

   public void setGetGeneratedKeys(boolean generatedKeys) {
      this.defaultConnectionConfig.setGetGeneratedKeys(generatedKeys);
   }

   static {
      for(Pragma pragma : SQLiteConfig.Pragma.values()) {
         pragmaSet.add(pragma.pragmaName);
      }

   }

   static class OnOff {
      private static final String[] Values = new String[]{"true", "false"};
   }

   public static enum Pragma {
      OPEN_MODE("open_mode", "Database open-mode flag", (String[])null),
      SHARED_CACHE("shared_cache", "Enable SQLite Shared-Cache mode, native driver only", SQLiteConfig.OnOff.Values),
      LOAD_EXTENSION("enable_load_extension", "Enable SQLite load_extension() function, native driver only", SQLiteConfig.OnOff.Values),
      CACHE_SIZE("cache_size", "Maximum number of database disk pages that SQLite will hold in memory at once per open database file", (String[])null),
      MMAP_SIZE("mmap_size", "Maximum number of bytes that are set aside for memory-mapped I/O on a single database", (String[])null),
      CASE_SENSITIVE_LIKE("case_sensitive_like", "Installs a new application-defined LIKE function that is either case sensitive or insensitive depending on the value", SQLiteConfig.OnOff.Values),
      COUNT_CHANGES("count_changes", "Deprecated", SQLiteConfig.OnOff.Values),
      DEFAULT_CACHE_SIZE("default_cache_size", "Deprecated", (String[])null),
      DEFER_FOREIGN_KEYS("defer_foreign_keys", "When the defer_foreign_keys PRAGMA is on, enforcement of all foreign key constraints is delayed until the outermost transaction is committed. The defer_foreign_keys pragma defaults to OFF so that foreign key constraints are only deferred if they are created as \"DEFERRABLE INITIALLY DEFERRED\". The defer_foreign_keys pragma is automatically switched off at each COMMIT or ROLLBACK. Hence, the defer_foreign_keys pragma must be separately enabled for each transaction. This pragma is only meaningful if foreign key constraints are enabled, of course.", SQLiteConfig.OnOff.Values),
      EMPTY_RESULT_CALLBACKS("empty_result_callback", "Deprecated", SQLiteConfig.OnOff.Values),
      ENCODING("encoding", "Set the encoding that the main database will be created with if it is created by this session", toStringArray(SQLiteConfig.Encoding.values())),
      FOREIGN_KEYS("foreign_keys", "Set the enforcement of foreign key constraints", SQLiteConfig.OnOff.Values),
      FULL_COLUMN_NAMES("full_column_names", "Deprecated", SQLiteConfig.OnOff.Values),
      FULL_SYNC("fullsync", "Whether or not the F_FULLFSYNC syncing method is used on systems that support it. Only Mac OS X supports F_FULLFSYNC.", SQLiteConfig.OnOff.Values),
      INCREMENTAL_VACUUM("incremental_vacuum", "Causes up to N pages to be removed from the freelist. The database file is truncated by the same amount. The incremental_vacuum pragma has no effect if the database is not in auto_vacuum=incremental mode or if there are no pages on the freelist. If there are fewer than N pages on the freelist, or if N is less than 1, or if the \"(N)\" argument is omitted, then the entire freelist is cleared.", (String[])null),
      JOURNAL_MODE("journal_mode", "Set the journal mode for databases associated with the current database connection", toStringArray(SQLiteConfig.JournalMode.values())),
      JOURNAL_SIZE_LIMIT("journal_size_limit", "Limit the size of rollback-journal and WAL files left in the file-system after transactions or checkpoints", (String[])null),
      LEGACY_ALTER_TABLE("legacy_alter_table", "Use legacy alter table behavior", SQLiteConfig.OnOff.Values),
      LEGACY_FILE_FORMAT("legacy_file_format", "No-op", SQLiteConfig.OnOff.Values),
      LOCKING_MODE("locking_mode", "Set the database connection locking-mode", toStringArray(SQLiteConfig.LockingMode.values())),
      PAGE_SIZE("page_size", "Set the page size of the database. The page size must be a power of two between 512 and 65536 inclusive.", (String[])null),
      MAX_PAGE_COUNT("max_page_count", "Set the maximum number of pages in the database file", (String[])null),
      READ_UNCOMMITTED("read_uncommitted", "Set READ UNCOMMITTED isolation", SQLiteConfig.OnOff.Values),
      RECURSIVE_TRIGGERS("recursive_triggers", "Set the recursive trigger capability", SQLiteConfig.OnOff.Values),
      REVERSE_UNORDERED_SELECTS("reverse_unordered_selects", "When enabled, this PRAGMA causes many SELECT statements without an ORDER BY clause to emit their results in the reverse order from what they normally would", SQLiteConfig.OnOff.Values),
      SECURE_DELETE("secure_delete", "When secure_delete is on, SQLite overwrites deleted content with zeros", new String[]{"true", "false", "fast"}),
      SHORT_COLUMN_NAMES("short_column_names", "Deprecated", SQLiteConfig.OnOff.Values),
      SYNCHRONOUS("synchronous", "Set the \"synchronous\" flag", toStringArray(SQLiteConfig.SynchronousMode.values())),
      TEMP_STORE("temp_store", "When temp_store is DEFAULT (0), the compile-time C preprocessor macro SQLITE_TEMP_STORE is used to determine where temporary tables and indices are stored. When temp_store is MEMORY (2) temporary tables and indices are kept as if they were in pure in-memory databases. When temp_store is FILE (1) temporary tables and indices are stored in a file. The temp_store_directory pragma can be used to specify the directory containing temporary files when FILE is specified. When the temp_store setting is changed, all existing temporary tables, indices, triggers, and views are immediately deleted.", toStringArray(SQLiteConfig.TempStore.values())),
      TEMP_STORE_DIRECTORY("temp_store_directory", "Deprecated", (String[])null),
      USER_VERSION("user_version", "Set the value of the user-version integer at offset 60 in the database header. The user-version is an integer that is available to applications to use however they want. SQLite makes no use of the user-version itself.", (String[])null),
      APPLICATION_ID("application_id", "Set the 32-bit signed big-endian \"Application ID\" integer located at offset 68 into the database header. Applications that use SQLite as their application file-format should set the Application ID integer to a unique integer so that utilities such as file(1) can determine the specific file type rather than just reporting \"SQLite3 Database\"", (String[])null),
      WAL_AUTOCHECKPOINT("wal_autocheckpoint", "The wal_autocheckpoint pragma sets the write-ahead log auto-checkpoint interval. If the argument N is specified, then the auto-checkpoint is adjusted to fire whenever the WAL has N or more pages. Passing zero or a negative value turns off automatic checkpointing entirely. The default auto-checkpoint interval is 1000 or SQLITE_DEFAULT_WAL_AUTOCHECKPOINT.", (String[])null),
      LIMIT_LENGTH("limit_length", "The maximum size of any string or BLOB or table row, in bytes.", (String[])null),
      LIMIT_SQL_LENGTH("limit_sql_length", "The maximum length of an SQL statement, in bytes.", (String[])null),
      LIMIT_COLUMN("limit_column", "The maximum number of columns in a table definition or in the result set of a SELECT or the maximum number of columns in an index or in an ORDER BY or GROUP BY clause.", (String[])null),
      LIMIT_EXPR_DEPTH("limit_expr_depth", "The maximum depth of the parse tree on any expression.", (String[])null),
      LIMIT_COMPOUND_SELECT("limit_compound_select", "The maximum number of terms in a compound SELECT statement.", (String[])null),
      LIMIT_VDBE_OP("limit_vdbe_op", "The maximum number of instructions in a virtual machine program used to implement an SQL statement. If sqlite3_prepare_v2() or the equivalent tries to allocate space for more than this many opcodes in a single prepared statement, an SQLITE_NOMEM error is returned.", (String[])null),
      LIMIT_FUNCTION_ARG("limit_function_arg", "The maximum number of arguments on a function.", (String[])null),
      LIMIT_ATTACHED("limit_attached", "The maximum number of attached databases.", (String[])null),
      LIMIT_LIKE_PATTERN_LENGTH("limit_like_pattern_length", "The maximum length of the pattern argument to the LIKE or GLOB operators.", (String[])null),
      LIMIT_VARIABLE_NUMBER("limit_variable_number", "The maximum index number of any parameter in an SQL statement.", (String[])null),
      LIMIT_TRIGGER_DEPTH("limit_trigger_depth", "The maximum depth of recursion for triggers.", (String[])null),
      LIMIT_WORKER_THREADS("limit_worker_threads", "The maximum number of auxiliary worker threads that a single prepared statement may start.", (String[])null),
      LIMIT_PAGE_COUNT("limit_page_count", "The maximum number of pages allowed in a single database file.", (String[])null),
      TRANSACTION_MODE("transaction_mode", "Set the transaction mode", toStringArray(SQLiteConfig.TransactionMode.values())),
      DATE_PRECISION("date_precision", "\"seconds\": Read and store integer dates as seconds from the Unix Epoch (SQLite standard).\n\"milliseconds\": (DEFAULT) Read and store integer dates as milliseconds from the Unix Epoch (Java standard).", toStringArray(SQLiteConfig.DatePrecision.values())),
      DATE_CLASS("date_class", "\"integer\": (Default) store dates as number of seconds or milliseconds from the Unix Epoch\n\"text\": store dates as a string of text\n\"real\": store dates as Julian Dates", toStringArray(SQLiteConfig.DateClass.values())),
      DATE_STRING_FORMAT("date_string_format", "Format to store and retrieve dates stored as text. Defaults to \"yyyy-MM-dd HH:mm:ss.SSS\"", (String[])null),
      BUSY_TIMEOUT("busy_timeout", "Sets a busy handler that sleeps for a specified amount of time when a table is locked", (String[])null),
      HEXKEY_MODE("hexkey_mode", "Mode of the secret key", toStringArray(SQLiteConfig.HexKeyMode.values())),
      PASSWORD("password", "Database password", (String[])null),
      JDBC_EXPLICIT_READONLY("jdbc.explicit_readonly", "Set explicit read only transactions", (String[])null),
      JDBC_GET_GENERATED_KEYS("jdbc.get_generated_keys", "Enable retrieval of generated keys", SQLiteConfig.OnOff.Values);

      public final String pragmaName;
      public final String[] choices;
      public final String description;

      private Pragma(String pragmaName) {
         this(pragmaName, (String[])null);
      }

      private Pragma(String pragmaName, String[] choices) {
         this(pragmaName, (String)null, choices);
      }

      private Pragma(String pragmaName, String description, String[] choices) {
         this.pragmaName = pragmaName;
         this.description = description;
         this.choices = choices;
      }

      private static String[] toStringArray(PragmaValue[] list) {
         String[] result = new String[list.length];

         for(int i = 0; i < list.length; ++i) {
            result[i] = list[i].getValue();
         }

         return result;
      }

      public final String getPragmaName() {
         return this.pragmaName;
      }

      // $FF: synthetic method
      private static Pragma[] $values() {
         return new Pragma[]{OPEN_MODE, SHARED_CACHE, LOAD_EXTENSION, CACHE_SIZE, MMAP_SIZE, CASE_SENSITIVE_LIKE, COUNT_CHANGES, DEFAULT_CACHE_SIZE, DEFER_FOREIGN_KEYS, EMPTY_RESULT_CALLBACKS, ENCODING, FOREIGN_KEYS, FULL_COLUMN_NAMES, FULL_SYNC, INCREMENTAL_VACUUM, JOURNAL_MODE, JOURNAL_SIZE_LIMIT, LEGACY_ALTER_TABLE, LEGACY_FILE_FORMAT, LOCKING_MODE, PAGE_SIZE, MAX_PAGE_COUNT, READ_UNCOMMITTED, RECURSIVE_TRIGGERS, REVERSE_UNORDERED_SELECTS, SECURE_DELETE, SHORT_COLUMN_NAMES, SYNCHRONOUS, TEMP_STORE, TEMP_STORE_DIRECTORY, USER_VERSION, APPLICATION_ID, WAL_AUTOCHECKPOINT, LIMIT_LENGTH, LIMIT_SQL_LENGTH, LIMIT_COLUMN, LIMIT_EXPR_DEPTH, LIMIT_COMPOUND_SELECT, LIMIT_VDBE_OP, LIMIT_FUNCTION_ARG, LIMIT_ATTACHED, LIMIT_LIKE_PATTERN_LENGTH, LIMIT_VARIABLE_NUMBER, LIMIT_TRIGGER_DEPTH, LIMIT_WORKER_THREADS, LIMIT_PAGE_COUNT, TRANSACTION_MODE, DATE_PRECISION, DATE_CLASS, DATE_STRING_FORMAT, BUSY_TIMEOUT, HEXKEY_MODE, PASSWORD, JDBC_EXPLICIT_READONLY, JDBC_GET_GENERATED_KEYS};
      }
   }

   public static enum Encoding implements PragmaValue {
      UTF8("'UTF-8'"),
      UTF16("'UTF-16'"),
      UTF16_LITTLE_ENDIAN("'UTF-16le'"),
      UTF16_BIG_ENDIAN("'UTF-16be'"),
      UTF_8(UTF8),
      UTF_16(UTF16),
      UTF_16LE(UTF16_LITTLE_ENDIAN),
      UTF_16BE(UTF16_BIG_ENDIAN);

      public final String typeName;

      private Encoding(String typeName) {
         this.typeName = typeName;
      }

      private Encoding(Encoding encoding) {
         this.typeName = encoding.getValue();
      }

      public String getValue() {
         return this.typeName;
      }

      public static Encoding getEncoding(String value) {
         return valueOf(value.replaceAll("-", "_").toUpperCase());
      }

      // $FF: synthetic method
      private static Encoding[] $values() {
         return new Encoding[]{UTF8, UTF16, UTF16_LITTLE_ENDIAN, UTF16_BIG_ENDIAN, UTF_8, UTF_16, UTF_16LE, UTF_16BE};
      }
   }

   public static enum JournalMode implements PragmaValue {
      DELETE,
      TRUNCATE,
      PERSIST,
      MEMORY,
      WAL,
      OFF;

      public String getValue() {
         return this.name();
      }

      // $FF: synthetic method
      private static JournalMode[] $values() {
         return new JournalMode[]{DELETE, TRUNCATE, PERSIST, MEMORY, WAL, OFF};
      }
   }

   public static enum LockingMode implements PragmaValue {
      NORMAL,
      EXCLUSIVE;

      public String getValue() {
         return this.name();
      }

      // $FF: synthetic method
      private static LockingMode[] $values() {
         return new LockingMode[]{NORMAL, EXCLUSIVE};
      }
   }

   public static enum SynchronousMode implements PragmaValue {
      OFF,
      NORMAL,
      FULL;

      public String getValue() {
         return this.name();
      }

      // $FF: synthetic method
      private static SynchronousMode[] $values() {
         return new SynchronousMode[]{OFF, NORMAL, FULL};
      }
   }

   public static enum TempStore implements PragmaValue {
      DEFAULT,
      FILE,
      MEMORY;

      public String getValue() {
         return this.name();
      }

      // $FF: synthetic method
      private static TempStore[] $values() {
         return new TempStore[]{DEFAULT, FILE, MEMORY};
      }
   }

   public static enum HexKeyMode implements PragmaValue {
      NONE,
      SSE,
      SQLCIPHER;

      public String getValue() {
         return this.name();
      }

      // $FF: synthetic method
      private static HexKeyMode[] $values() {
         return new HexKeyMode[]{NONE, SSE, SQLCIPHER};
      }
   }

   public static enum TransactionMode implements PragmaValue {
      DEFERRED,
      IMMEDIATE,
      EXCLUSIVE;

      public String getValue() {
         return this.name();
      }

      public static TransactionMode getMode(String mode) {
         return valueOf(mode.toUpperCase());
      }

      // $FF: synthetic method
      private static TransactionMode[] $values() {
         return new TransactionMode[]{DEFERRED, IMMEDIATE, EXCLUSIVE};
      }
   }

   public static enum DatePrecision implements PragmaValue {
      SECONDS,
      MILLISECONDS;

      public String getValue() {
         return this.name();
      }

      public static DatePrecision getPrecision(String precision) {
         return valueOf(precision.toUpperCase());
      }

      // $FF: synthetic method
      private static DatePrecision[] $values() {
         return new DatePrecision[]{SECONDS, MILLISECONDS};
      }
   }

   public static enum DateClass implements PragmaValue {
      INTEGER,
      TEXT,
      REAL;

      public String getValue() {
         return this.name();
      }

      public static DateClass getDateClass(String dateClass) {
         return valueOf(dateClass.toUpperCase());
      }

      // $FF: synthetic method
      private static DateClass[] $values() {
         return new DateClass[]{INTEGER, TEXT, REAL};
      }
   }

   private interface PragmaValue {
      String getValue();
   }
}
