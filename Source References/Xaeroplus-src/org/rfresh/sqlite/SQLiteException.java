package org.rfresh.sqlite;

import java.sql.SQLException;

public class SQLiteException extends SQLException {
   private SQLiteErrorCode resultCode;

   public SQLiteException(String message, SQLiteErrorCode resultCode) {
      super(message, (String)null, resultCode.code & 255);
      this.resultCode = resultCode;
   }

   public SQLiteErrorCode getResultCode() {
      return this.resultCode;
   }
}
