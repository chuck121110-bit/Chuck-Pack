package org.rfresh.sqlite;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import org.rfresh.sqlite.date.FastDateFormat;

public class SQLiteConnectionConfig implements Cloneable {
   private SQLiteConfig.DateClass dateClass;
   private SQLiteConfig.DatePrecision datePrecision;
   private String dateStringFormat;
   private FastDateFormat dateFormat;
   private int transactionIsolation;
   private SQLiteConfig.TransactionMode transactionMode;
   private boolean autoCommit;
   private boolean getGeneratedKeys;
   private static final Map<SQLiteConfig.TransactionMode, String> beginCommandMap = new EnumMap(SQLiteConfig.TransactionMode.class);

   public static SQLiteConnectionConfig fromPragmaTable(Properties pragmaTable) {
      return new SQLiteConnectionConfig(SQLiteConfig.DateClass.getDateClass(pragmaTable.getProperty(SQLiteConfig.Pragma.DATE_CLASS.pragmaName, SQLiteConfig.DateClass.INTEGER.name())), SQLiteConfig.DatePrecision.getPrecision(pragmaTable.getProperty(SQLiteConfig.Pragma.DATE_PRECISION.pragmaName, SQLiteConfig.DatePrecision.MILLISECONDS.name())), pragmaTable.getProperty(SQLiteConfig.Pragma.DATE_STRING_FORMAT.pragmaName, "yyyy-MM-dd HH:mm:ss.SSS"), 8, SQLiteConfig.TransactionMode.getMode(pragmaTable.getProperty(SQLiteConfig.Pragma.TRANSACTION_MODE.pragmaName, SQLiteConfig.TransactionMode.DEFERRED.name())), true, Boolean.parseBoolean(pragmaTable.getProperty(SQLiteConfig.Pragma.JDBC_GET_GENERATED_KEYS.pragmaName, "true")));
   }

   public SQLiteConnectionConfig(SQLiteConfig.DateClass dateClass, SQLiteConfig.DatePrecision datePrecision, String dateStringFormat, int transactionIsolation, SQLiteConfig.TransactionMode transactionMode, boolean autoCommit, boolean getGeneratedKeys) {
      this.dateClass = SQLiteConfig.DateClass.INTEGER;
      this.datePrecision = SQLiteConfig.DatePrecision.MILLISECONDS;
      this.dateStringFormat = "yyyy-MM-dd HH:mm:ss.SSS";
      this.dateFormat = FastDateFormat.getInstance(this.dateStringFormat);
      this.transactionIsolation = 8;
      this.transactionMode = SQLiteConfig.TransactionMode.DEFERRED;
      this.autoCommit = true;
      this.getGeneratedKeys = true;
      this.setDateClass(dateClass);
      this.setDatePrecision(datePrecision);
      this.setDateStringFormat(dateStringFormat);
      this.setTransactionIsolation(transactionIsolation);
      this.setTransactionMode(transactionMode);
      this.setAutoCommit(autoCommit);
      this.setGetGeneratedKeys(getGeneratedKeys);
   }

   public SQLiteConnectionConfig copyConfig() {
      return new SQLiteConnectionConfig(this.dateClass, this.datePrecision, this.dateStringFormat, this.transactionIsolation, this.transactionMode, this.autoCommit, this.getGeneratedKeys);
   }

   public long getDateMultiplier() {
      return this.datePrecision == SQLiteConfig.DatePrecision.MILLISECONDS ? 1L : 1000L;
   }

   public SQLiteConfig.DateClass getDateClass() {
      return this.dateClass;
   }

   public void setDateClass(SQLiteConfig.DateClass dateClass) {
      this.dateClass = dateClass;
   }

   public SQLiteConfig.DatePrecision getDatePrecision() {
      return this.datePrecision;
   }

   public void setDatePrecision(SQLiteConfig.DatePrecision datePrecision) {
      this.datePrecision = datePrecision;
   }

   public String getDateStringFormat() {
      return this.dateStringFormat;
   }

   public void setDateStringFormat(String dateStringFormat) {
      this.dateStringFormat = dateStringFormat;
      this.dateFormat = FastDateFormat.getInstance(dateStringFormat);
   }

   public FastDateFormat getDateFormat() {
      return this.dateFormat;
   }

   public boolean isAutoCommit() {
      return this.autoCommit;
   }

   public void setAutoCommit(boolean autoCommit) {
      this.autoCommit = autoCommit;
   }

   public int getTransactionIsolation() {
      return this.transactionIsolation;
   }

   public void setTransactionIsolation(int transactionIsolation) {
      this.transactionIsolation = transactionIsolation;
   }

   public SQLiteConfig.TransactionMode getTransactionMode() {
      return this.transactionMode;
   }

   public void setTransactionMode(SQLiteConfig.TransactionMode transactionMode) {
      this.transactionMode = transactionMode;
   }

   public boolean isGetGeneratedKeys() {
      return this.getGeneratedKeys;
   }

   public void setGetGeneratedKeys(boolean getGeneratedKeys) {
      this.getGeneratedKeys = getGeneratedKeys;
   }

   String transactionPrefix() {
      return (String)beginCommandMap.get(this.transactionMode);
   }

   static {
      beginCommandMap.put(SQLiteConfig.TransactionMode.DEFERRED, "begin;");
      beginCommandMap.put(SQLiteConfig.TransactionMode.IMMEDIATE, "begin immediate;");
      beginCommandMap.put(SQLiteConfig.TransactionMode.EXCLUSIVE, "begin exclusive;");
   }
}
