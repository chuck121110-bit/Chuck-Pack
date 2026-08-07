package org.rfresh.sqlite.jdbc3;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.rfresh.sqlite.SQLiteConnection;
import org.rfresh.sqlite.core.CoreDatabaseMetaData;
import org.rfresh.sqlite.core.CoreStatement;
import org.rfresh.sqlite.util.Logger;
import org.rfresh.sqlite.util.LoggerFactory;
import org.rfresh.sqlite.util.QueryUtils;
import org.rfresh.sqlite.util.StringUtils;

public abstract class JDBC3DatabaseMetaData extends CoreDatabaseMetaData {
   private static String driverName;
   private static String driverVersion;
   protected static final Pattern TYPE_INTEGER;
   protected static final Pattern TYPE_VARCHAR;
   protected static final Pattern TYPE_FLOAT;
   private static final Map<String, Integer> RULE_MAP;
   protected static final Pattern PK_UNNAMED_PATTERN;
   protected static final Pattern PK_NAMED_PATTERN;

   protected JDBC3DatabaseMetaData(SQLiteConnection conn) {
      super(conn);
   }

   public Connection getConnection() {
      return this.conn;
   }

   public int getDatabaseMajorVersion() throws SQLException {
      return Integer.parseInt(this.conn.libversion().split("\\.")[0]);
   }

   public int getDatabaseMinorVersion() throws SQLException {
      return Integer.parseInt(this.conn.libversion().split("\\.")[1]);
   }

   public int getDriverMajorVersion() {
      return Integer.parseInt(driverVersion.split("\\.")[0]);
   }

   public int getDriverMinorVersion() {
      return Integer.parseInt(driverVersion.split("\\.")[1]);
   }

   public int getJDBCMajorVersion() {
      return 4;
   }

   public int getJDBCMinorVersion() {
      return 2;
   }

   public int getDefaultTransactionIsolation() {
      return 8;
   }

   public int getMaxBinaryLiteralLength() {
      return 0;
   }

   public int getMaxCatalogNameLength() {
      return 0;
   }

   public int getMaxCharLiteralLength() {
      return 0;
   }

   public int getMaxColumnNameLength() {
      return 0;
   }

   public int getMaxColumnsInGroupBy() {
      return 0;
   }

   public int getMaxColumnsInIndex() {
      return 0;
   }

   public int getMaxColumnsInOrderBy() {
      return 0;
   }

   public int getMaxColumnsInSelect() {
      return 0;
   }

   public int getMaxColumnsInTable() {
      return 0;
   }

   public int getMaxConnections() {
      return 0;
   }

   public int getMaxCursorNameLength() {
      return 0;
   }

   public int getMaxIndexLength() {
      return 0;
   }

   public int getMaxProcedureNameLength() {
      return 0;
   }

   public int getMaxRowSize() {
      return 0;
   }

   public int getMaxSchemaNameLength() {
      return 0;
   }

   public int getMaxStatementLength() {
      return 0;
   }

   public int getMaxStatements() {
      return 0;
   }

   public int getMaxTableNameLength() {
      return 0;
   }

   public int getMaxTablesInSelect() {
      return 0;
   }

   public int getMaxUserNameLength() {
      return 0;
   }

   public int getResultSetHoldability() {
      return 2;
   }

   public int getSQLStateType() {
      return 2;
   }

   public String getDatabaseProductName() {
      return "SQLite";
   }

   public String getDatabaseProductVersion() throws SQLException {
      return this.conn.libversion();
   }

   public String getDriverName() {
      return driverName;
   }

   public String getDriverVersion() {
      return driverVersion;
   }

   public String getExtraNameCharacters() {
      return "";
   }

   public String getCatalogSeparator() {
      return ".";
   }

   public String getCatalogTerm() {
      return "catalog";
   }

   public String getSchemaTerm() {
      return "schema";
   }

   public String getProcedureTerm() {
      return "not_implemented";
   }

   public String getSearchStringEscape() {
      return "\\";
   }

   public String getIdentifierQuoteString() {
      return "\"";
   }

   public String getSQLKeywords() {
      return "ABORT,ACTION,AFTER,ANALYZE,ATTACH,AUTOINCREMENT,BEFORE,CASCADE,CONFLICT,DATABASE,DEFERRABLE,DEFERRED,DESC,DETACH,EXCLUSIVE,EXPLAIN,FAIL,GLOB,IGNORE,INDEX,INDEXED,INITIALLY,INSTEAD,ISNULL,KEY,LIMIT,NOTNULL,OFFSET,PLAN,PRAGMA,QUERY,RAISE,REGEXP,REINDEX,RENAME,REPLACE,RESTRICT,TEMP,TEMPORARY,TRANSACTION,VACUUM,VIEW,VIRTUAL";
   }

   public String getNumericFunctions() {
      return "";
   }

   public String getStringFunctions() {
      return "";
   }

   public String getSystemFunctions() {
      return "";
   }

   public String getTimeDateFunctions() {
      return "DATE,TIME,DATETIME,JULIANDAY,STRFTIME";
   }

   public String getURL() {
      return this.conn.getUrl();
   }

   public String getUserName() {
      return null;
   }

   public boolean allProceduresAreCallable() {
      return false;
   }

   public boolean allTablesAreSelectable() {
      return true;
   }

   public boolean dataDefinitionCausesTransactionCommit() {
      return false;
   }

   public boolean dataDefinitionIgnoredInTransactions() {
      return false;
   }

   public boolean doesMaxRowSizeIncludeBlobs() {
      return false;
   }

   public boolean deletesAreDetected(int type) {
      return false;
   }

   public boolean insertsAreDetected(int type) {
      return false;
   }

   public boolean isCatalogAtStart() {
      return true;
   }

   public boolean locatorsUpdateCopy() {
      return false;
   }

   public boolean nullPlusNonNullIsNull() {
      return true;
   }

   public boolean nullsAreSortedAtEnd() {
      return !this.nullsAreSortedAtStart();
   }

   public boolean nullsAreSortedAtStart() {
      return true;
   }

   public boolean nullsAreSortedHigh() {
      return true;
   }

   public boolean nullsAreSortedLow() {
      return !this.nullsAreSortedHigh();
   }

   public boolean othersDeletesAreVisible(int type) {
      return false;
   }

   public boolean othersInsertsAreVisible(int type) {
      return false;
   }

   public boolean othersUpdatesAreVisible(int type) {
      return false;
   }

   public boolean ownDeletesAreVisible(int type) {
      return false;
   }

   public boolean ownInsertsAreVisible(int type) {
      return false;
   }

   public boolean ownUpdatesAreVisible(int type) {
      return false;
   }

   public boolean storesLowerCaseIdentifiers() {
      return false;
   }

   public boolean storesLowerCaseQuotedIdentifiers() {
      return false;
   }

   public boolean storesMixedCaseIdentifiers() {
      return true;
   }

   public boolean storesMixedCaseQuotedIdentifiers() {
      return false;
   }

   public boolean storesUpperCaseIdentifiers() {
      return false;
   }

   public boolean storesUpperCaseQuotedIdentifiers() {
      return false;
   }

   public boolean supportsAlterTableWithAddColumn() {
      return true;
   }

   public boolean supportsAlterTableWithDropColumn() {
      return true;
   }

   public boolean supportsANSI92EntryLevelSQL() {
      return false;
   }

   public boolean supportsANSI92FullSQL() {
      return false;
   }

   public boolean supportsANSI92IntermediateSQL() {
      return false;
   }

   public boolean supportsBatchUpdates() {
      return true;
   }

   public boolean supportsCatalogsInDataManipulation() {
      return false;
   }

   public boolean supportsCatalogsInIndexDefinitions() {
      return false;
   }

   public boolean supportsCatalogsInPrivilegeDefinitions() {
      return false;
   }

   public boolean supportsCatalogsInProcedureCalls() {
      return false;
   }

   public boolean supportsCatalogsInTableDefinitions() {
      return false;
   }

   public boolean supportsColumnAliasing() {
      return true;
   }

   public boolean supportsConvert() {
      return false;
   }

   public boolean supportsConvert(int fromType, int toType) {
      return false;
   }

   public boolean supportsCorrelatedSubqueries() {
      return false;
   }

   public boolean supportsDataDefinitionAndDataManipulationTransactions() {
      return true;
   }

   public boolean supportsDataManipulationTransactionsOnly() {
      return false;
   }

   public boolean supportsDifferentTableCorrelationNames() {
      return false;
   }

   public boolean supportsExpressionsInOrderBy() {
      return true;
   }

   public boolean supportsMinimumSQLGrammar() {
      return true;
   }

   public boolean supportsCoreSQLGrammar() {
      return true;
   }

   public boolean supportsExtendedSQLGrammar() {
      return false;
   }

   public boolean supportsLimitedOuterJoins() {
      return true;
   }

   public boolean supportsFullOuterJoins() throws SQLException {
      String[] version = this.conn.libversion().split("\\.");
      return Integer.parseInt(version[0]) >= 3 && Integer.parseInt(version[1]) >= 39;
   }

   public boolean supportsGetGeneratedKeys() {
      return true;
   }

   public boolean supportsGroupBy() {
      return true;
   }

   public boolean supportsGroupByBeyondSelect() {
      return false;
   }

   public boolean supportsGroupByUnrelated() {
      return false;
   }

   public boolean supportsIntegrityEnhancementFacility() {
      return false;
   }

   public boolean supportsLikeEscapeClause() {
      return false;
   }

   public boolean supportsMixedCaseIdentifiers() {
      return true;
   }

   public boolean supportsMixedCaseQuotedIdentifiers() {
      return false;
   }

   public boolean supportsMultipleOpenResults() {
      return false;
   }

   public boolean supportsMultipleResultSets() {
      return false;
   }

   public boolean supportsMultipleTransactions() {
      return true;
   }

   public boolean supportsNamedParameters() {
      return true;
   }

   public boolean supportsNonNullableColumns() {
      return true;
   }

   public boolean supportsOpenCursorsAcrossCommit() {
      return false;
   }

   public boolean supportsOpenCursorsAcrossRollback() {
      return false;
   }

   public boolean supportsOpenStatementsAcrossCommit() {
      return false;
   }

   public boolean supportsOpenStatementsAcrossRollback() {
      return false;
   }

   public boolean supportsOrderByUnrelated() {
      return false;
   }

   public boolean supportsOuterJoins() {
      return true;
   }

   public boolean supportsPositionedDelete() {
      return false;
   }

   public boolean supportsPositionedUpdate() {
      return false;
   }

   public boolean supportsResultSetConcurrency(int t, int c) {
      return t == 1003 && c == 1007;
   }

   public boolean supportsResultSetHoldability(int h) {
      return h == 2;
   }

   public boolean supportsResultSetType(int t) {
      return t == 1003;
   }

   public boolean supportsSavepoints() {
      return true;
   }

   public boolean supportsSchemasInDataManipulation() {
      return false;
   }

   public boolean supportsSchemasInIndexDefinitions() {
      return false;
   }

   public boolean supportsSchemasInPrivilegeDefinitions() {
      return false;
   }

   public boolean supportsSchemasInProcedureCalls() {
      return false;
   }

   public boolean supportsSchemasInTableDefinitions() {
      return false;
   }

   public boolean supportsSelectForUpdate() {
      return false;
   }

   public boolean supportsStatementPooling() {
      return false;
   }

   public boolean supportsStoredProcedures() {
      return false;
   }

   public boolean supportsSubqueriesInComparisons() {
      return false;
   }

   public boolean supportsSubqueriesInExists() {
      return true;
   }

   public boolean supportsSubqueriesInIns() {
      return true;
   }

   public boolean supportsSubqueriesInQuantifieds() {
      return false;
   }

   public boolean supportsTableCorrelationNames() {
      return false;
   }

   public boolean supportsTransactionIsolationLevel(int level) {
      return level == 8;
   }

   public boolean supportsTransactions() {
      return true;
   }

   public boolean supportsUnion() {
      return true;
   }

   public boolean supportsUnionAll() {
      return true;
   }

   public boolean updatesAreDetected(int type) {
      return false;
   }

   public boolean usesLocalFilePerTable() {
      return false;
   }

   public boolean usesLocalFiles() {
      return true;
   }

   public boolean isReadOnly() throws SQLException {
      return this.conn.isReadOnly();
   }

   public ResultSet getAttributes(String c, String s, String t, String a) throws SQLException {
      if (this.getAttributes == null) {
         this.getAttributes = this.conn.prepareStatement("select null as TYPE_CAT, null as TYPE_SCHEM, null as TYPE_NAME, null as ATTR_NAME, null as DATA_TYPE, null as ATTR_TYPE_NAME, null as ATTR_SIZE, null as DECIMAL_DIGITS, null as NUM_PREC_RADIX, null as NULLABLE, null as REMARKS, null as ATTR_DEF, null as SQL_DATA_TYPE, null as SQL_DATETIME_SUB, null as CHAR_OCTET_LENGTH, null as ORDINAL_POSITION, null as IS_NULLABLE, null as SCOPE_CATALOG, null as SCOPE_SCHEMA, null as SCOPE_TABLE, null as SOURCE_DATA_TYPE limit 0;");
      }

      return this.getAttributes.executeQuery();
   }

   public ResultSet getBestRowIdentifier(String c, String s, String t, int scope, boolean n) throws SQLException {
      if (this.getBestRowIdentifier == null) {
         this.getBestRowIdentifier = this.conn.prepareStatement("select null as SCOPE, null as COLUMN_NAME, null as DATA_TYPE, null as TYPE_NAME, null as COLUMN_SIZE, null as BUFFER_LENGTH, null as DECIMAL_DIGITS, null as PSEUDO_COLUMN limit 0;");
      }

      return this.getBestRowIdentifier.executeQuery();
   }

   public ResultSet getColumnPrivileges(String c, String s, String t, String colPat) throws SQLException {
      if (this.getColumnPrivileges == null) {
         this.getColumnPrivileges = this.conn.prepareStatement("select null as TABLE_CAT, null as TABLE_SCHEM, null as TABLE_NAME, null as COLUMN_NAME, null as GRANTOR, null as GRANTEE, null as PRIVILEGE, null as IS_GRANTABLE limit 0;");
      }

      return this.getColumnPrivileges.executeQuery();
   }

   public ResultSet getColumns(String c, String s, String tblNamePattern, String colNamePattern) throws SQLException {
      this.checkOpen();
      StringBuilder sql = new StringBuilder(700);
      sql.append("select null as TABLE_CAT, null as TABLE_SCHEM, tblname as TABLE_NAME, ").append("cn as COLUMN_NAME, ct as DATA_TYPE, tn as TYPE_NAME, colSize as COLUMN_SIZE, ").append("2000000000 as BUFFER_LENGTH, colDecimalDigits as DECIMAL_DIGITS, 10   as NUM_PREC_RADIX, ").append("colnullable as NULLABLE, null as REMARKS, colDefault as COLUMN_DEF, ").append("0    as SQL_DATA_TYPE, 0    as SQL_DATETIME_SUB, 2000000000 as CHAR_OCTET_LENGTH, ").append("ordpos as ORDINAL_POSITION, (case colnullable when 0 then 'NO' when 1 then 'YES' else '' end)").append("    as IS_NULLABLE, null as SCOPE_CATALOG, null as SCOPE_SCHEMA, ").append("null as SCOPE_TABLE, null as SOURCE_DATA_TYPE, ").append("(case colautoincrement when 0 then 'NO' when 1 then 'YES' else '' end) as IS_AUTOINCREMENT, ").append("(case colgenerated when 0 then 'NO' when 1 then 'YES' else '' end) as IS_GENERATEDCOLUMN from (");
      boolean colFound = false;
      ResultSet rs = null;

      try {
         rs = this.getTables(c, s, tblNamePattern, (String[])null);

         while(rs.next()) {
            String tableName = rs.getString(3);
            Statement statColAutoinc = this.conn.createStatement();
            ResultSet rsColAutoinc = null;

            boolean isAutoIncrement;
            try {
               statColAutoinc = this.conn.createStatement();
               rsColAutoinc = statColAutoinc.executeQuery("SELECT LIKE('%autoincrement%', LOWER(sql)) FROM sqlite_schema WHERE LOWER(name) = LOWER('" + this.escape(tableName) + "') AND TYPE IN ('table', 'view')");
               rsColAutoinc.next();
               isAutoIncrement = rsColAutoinc.getInt(1) == 1;
            } finally {
               if (rsColAutoinc != null) {
                  try {
                     rsColAutoinc.close();
                  } catch (Exception e) {
                     JDBC3DatabaseMetaData.LogHolder.logger.error(() -> "Could not close ResultSet", e);
                  }
               }

               if (statColAutoinc != null) {
                  try {
                     statColAutoinc.close();
                  } catch (Exception e) {
                     JDBC3DatabaseMetaData.LogHolder.logger.error(() -> "Could not close statement", e);
                  }
               }

            }

            String pragmaStatement = "PRAGMA table_xinfo('" + this.escape(tableName) + "')";
            Statement colstat = this.conn.createStatement();

            try {
               ResultSet rscol = colstat.executeQuery(pragmaStatement);

               try {
                  for(int i = 0; rscol.next(); ++i) {
                     String colName = rscol.getString(2);
                     String colType = rscol.getString(3);
                     String colNotNull = rscol.getString(4);
                     String colDefault = rscol.getString(5);
                     boolean isPk = "1".equals(rscol.getString(6));
                     String colHidden = rscol.getString(7);
                     int colNullable = 2;
                     if (colNotNull != null) {
                        colNullable = colNotNull.equals("0") ? 1 : 0;
                     }

                     if (colFound) {
                        sql.append(" union all ");
                     }

                     colFound = true;
                     int iColumnSize = 2000000000;
                     int iDecimalDigits = 10;
                     colType = colType == null ? "TEXT" : colType.toUpperCase();
                     int colAutoIncrement = 0;
                     if (isPk && isAutoIncrement) {
                        colAutoIncrement = 1;
                     }

                     int colJavaType;
                     if (TYPE_INTEGER.matcher(colType).find()) {
                        colJavaType = 4;
                        iDecimalDigits = 0;
                     } else if (TYPE_VARCHAR.matcher(colType).find()) {
                        colJavaType = 12;
                        iDecimalDigits = 0;
                     } else if (TYPE_FLOAT.matcher(colType).find()) {
                        colJavaType = 6;
                     } else {
                        colJavaType = 12;
                     }

                     int iStartOfDimension = colType.indexOf(40);
                     if (iStartOfDimension > 0) {
                        int iEndOfDimension = colType.indexOf(41, iStartOfDimension);
                        if (iEndOfDimension > 0) {
                           int iDimensionSeparator = colType.indexOf(44, iStartOfDimension);
                           String sInteger;
                           String sDecimal;
                           if (iDimensionSeparator > 0) {
                              sInteger = colType.substring(iStartOfDimension + 1, iDimensionSeparator);
                              sDecimal = colType.substring(iDimensionSeparator + 1, iEndOfDimension);
                           } else {
                              sInteger = colType.substring(iStartOfDimension + 1, iEndOfDimension);
                              sDecimal = null;
                           }

                           try {
                              int iInteger = Integer.parseUnsignedInt(sInteger.trim());
                              if (sDecimal != null) {
                                 iDecimalDigits = Integer.parseUnsignedInt(sDecimal.trim());
                                 iColumnSize = iInteger + iDecimalDigits;
                              } else {
                                 iDecimalDigits = 0;
                                 iColumnSize = iInteger;
                              }
                           } catch (NumberFormatException var65) {
                           }
                        }

                        colType = colType.substring(0, iStartOfDimension).trim();
                     }

                     int colGenerated = 0;
                     if ("2".equals(colHidden) || "3".equals(colHidden)) {
                        colGenerated = 1;
                     }

                     sql.append("select ").append(i + 1).append(" as ordpos, ").append(colNullable).append(" as colnullable,").append(colJavaType).append(" as ct, ").append(iColumnSize).append(" as colSize, ").append(iDecimalDigits).append(" as colDecimalDigits, ").append("'").append(this.escape(tableName)).append("' as tblname, ").append("'").append(this.escape(colName)).append("' as cn, ").append("'").append(this.escape(colType)).append("' as tn, ").append(quote(colDefault == null ? null : this.escape(colDefault))).append(" as colDefault,").append(colAutoIncrement).append(" as colautoincrement,").append(colGenerated).append(" as colgenerated");
                     if (colNamePattern != null) {
                        sql.append(" where upper(cn) like upper('").append(this.escape(colNamePattern)).append("') ESCAPE '").append(this.getSearchStringEscape()).append("'");
                     }
                  }
               } catch (Throwable var67) {
                  if (rscol != null) {
                     try {
                        rscol.close();
                     } catch (Throwable var62) {
                        var67.addSuppressed(var62);
                     }
                  }

                  throw var67;
               }

               if (rscol != null) {
                  rscol.close();
               }
            } catch (Throwable var68) {
               if (colstat != null) {
                  try {
                     colstat.close();
                  } catch (Throwable var61) {
                     var68.addSuppressed(var61);
                  }
               }

               throw var68;
            }

            if (colstat != null) {
               colstat.close();
            }
         }
      } finally {
         if (rs != null) {
            try {
               rs.close();
            } catch (Exception e) {
               JDBC3DatabaseMetaData.LogHolder.logger.error(() -> "Could not close ResultSet", e);
            }
         }

      }

      if (colFound) {
         sql.append(") order by TABLE_SCHEM, TABLE_NAME, ORDINAL_POSITION;");
      } else {
         sql.append("select null as ordpos, null as colnullable, null as ct, null as colsize, null as colDecimalDigits, null as tblname, null as cn, null as tn, null as colDefault, null as colautoincrement, null as colgenerated) limit 0;");
      }

      Statement stat = this.conn.createStatement();
      return ((CoreStatement)stat).executeQuery(sql.toString(), true);
   }

   public ResultSet getCrossReference(String pc, String ps, String pt, String fc, String fs, String ft) throws SQLException {
      if (pt == null) {
         return this.getExportedKeys(fc, fs, ft);
      } else if (ft == null) {
         return this.getImportedKeys(pc, ps, pt);
      } else {
         String query = "select " + quote(this.escape(pc)) + " as PKTABLE_CAT, " + quote(this.escape(ps)) + " as PKTABLE_SCHEM, " + quote(this.escape(pt)) + " as PKTABLE_NAME, '' as PKCOLUMN_NAME, " + quote(this.escape(fc)) + " as FKTABLE_CAT, " + quote(this.escape(fs)) + " as FKTABLE_SCHEM, " + quote(this.escape(ft)) + " as FKTABLE_NAME, '' as FKCOLUMN_NAME, -1 as KEY_SEQ, 3 as UPDATE_RULE, 3 as DELETE_RULE, '' as FK_NAME, '' as PK_NAME, " + 5 + " as DEFERRABILITY limit 0 ";
         return ((CoreStatement)this.conn.createStatement()).executeQuery(query, true);
      }
   }

   public ResultSet getSchemas() throws SQLException {
      if (this.getSchemas == null) {
         this.getSchemas = this.conn.prepareStatement("select null as TABLE_SCHEM, null as TABLE_CATALOG limit 0;");
      }

      return this.getSchemas.executeQuery();
   }

   public ResultSet getCatalogs() throws SQLException {
      if (this.getCatalogs == null) {
         this.getCatalogs = this.conn.prepareStatement("select null as TABLE_CAT limit 0;");
      }

      return this.getCatalogs.executeQuery();
   }

   public ResultSet getPrimaryKeys(String c, String s, String table) throws SQLException {
      PrimaryKeyFinder pkFinder = new PrimaryKeyFinder(this, table);
      String[] columns = pkFinder.getColumns();
      Statement stat = this.conn.createStatement();
      StringBuilder sql = new StringBuilder(512);
      sql.append("select null as TABLE_CAT, null as TABLE_SCHEM, '").append(this.escape(table)).append("' as TABLE_NAME, cn as COLUMN_NAME, ks as KEY_SEQ, pk as PK_NAME from (");
      if (columns == null) {
         sql.append("select null as cn, null as pk, 0 as ks) limit 0;");
         return ((CoreStatement)stat).executeQuery(sql.toString(), true);
      } else {
         String pkName = pkFinder.getName();
         if (pkName != null) {
            pkName = "'" + pkName + "'";
         }

         for(int i = 0; i < columns.length; ++i) {
            if (i > 0) {
               sql.append(" union ");
            }

            sql.append("select ").append(pkName).append(" as pk, '").append(this.escape(this.unquoteIdentifier(columns[i]))).append("' as cn, ").append(i + 1).append(" as ks");
         }

         return ((CoreStatement)stat).executeQuery(sql.append(") order by cn;").toString(), true);
      }
   }

   public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
      PrimaryKeyFinder pkFinder = new PrimaryKeyFinder(this, table);
      String[] pkColumns = pkFinder.getColumns();
      Statement stat = this.conn.createStatement();
      catalog = catalog != null ? quote(this.escape(catalog)) : null;
      schema = schema != null ? quote(this.escape(schema)) : null;
      StringBuilder exportedKeysQuery = new StringBuilder(512);
      String target = null;
      int count = 0;
      if (pkColumns != null) {
         ResultSet rs = stat.executeQuery("select name from sqlite_schema where type = 'table'");

         ArrayList<String> tableList;
         try {
            tableList = new ArrayList();

            while(rs.next()) {
               String tblname = rs.getString(1);
               tableList.add(tblname);
               if (tblname.equalsIgnoreCase(table)) {
                  target = tblname;
               }
            }
         } catch (Throwable var29) {
            if (rs != null) {
               try {
                  rs.close();
               } catch (Throwable var28) {
                  var29.addSuppressed(var28);
               }
            }

            throw var29;
         }

         if (rs != null) {
            rs.close();
         }

         for(String tbl : tableList) {
            ImportedKeyFinder impFkFinder = new ImportedKeyFinder(this, tbl);

            for(ImportedKeyFinder.ForeignKey foreignKey : impFkFinder.getFkList()) {
               String PKTabName = foreignKey.getPkTableName();
               if (PKTabName != null && PKTabName.equalsIgnoreCase(target)) {
                  for(int j = 0; j < foreignKey.getColumnMappingCount(); ++j) {
                     int keySeq = j + 1;
                     String[] columnMapping = foreignKey.getColumnMapping(j);
                     String PKColName = columnMapping[1];
                     PKColName = PKColName == null ? "" : PKColName;
                     String FKColName = columnMapping[0];
                     FKColName = FKColName == null ? "" : FKColName;
                     boolean usePkName = false;

                     for(String pkColumn : pkColumns) {
                        if (pkColumn != null && pkColumn.equalsIgnoreCase(PKColName)) {
                           usePkName = true;
                           break;
                        }
                     }

                     String pkName = usePkName && pkFinder.getName() != null ? pkFinder.getName() : "";
                     exportedKeysQuery.append(count > 0 ? " union all select " : "select ").append(keySeq).append(" as ks, '").append(this.escape(tbl)).append("' as fkt, '").append(this.escape(FKColName)).append("' as fcn, '").append(this.escape(PKColName)).append("' as pcn, '").append(this.escape(pkName)).append("' as pkn, ").append(RULE_MAP.get(foreignKey.getOnUpdate())).append(" as ur, ").append(RULE_MAP.get(foreignKey.getOnDelete())).append(" as dr, ");
                     String fkName = foreignKey.getFkName();
                     if (fkName != null) {
                        exportedKeysQuery.append("'").append(this.escape(fkName)).append("' as fkn");
                     } else {
                        exportedKeysQuery.append("'' as fkn");
                     }

                     ++count;
                  }
               }
            }
         }
      }

      boolean hasImportedKey = count > 0;
      StringBuilder sql = new StringBuilder(512);
      sql.append("select ").append(catalog).append(" as PKTABLE_CAT, ").append(schema).append(" as PKTABLE_SCHEM, ").append(quote(this.escape(target))).append(" as PKTABLE_NAME, ").append(hasImportedKey ? "pcn" : "''").append(" as PKCOLUMN_NAME, ").append(catalog).append(" as FKTABLE_CAT, ").append(schema).append(" as FKTABLE_SCHEM, ").append(hasImportedKey ? "fkt" : "''").append(" as FKTABLE_NAME, ").append(hasImportedKey ? "fcn" : "''").append(" as FKCOLUMN_NAME, ").append(hasImportedKey ? "ks" : "-1").append(" as KEY_SEQ, ").append(hasImportedKey ? "ur" : "3").append(" as UPDATE_RULE, ").append(hasImportedKey ? "dr" : "3").append(" as DELETE_RULE, ").append(hasImportedKey ? "fkn" : "''").append(" as FK_NAME, ").append(hasImportedKey ? "pkn" : "''").append(" as PK_NAME, ").append(5).append(" as DEFERRABILITY ");
      if (hasImportedKey) {
         sql.append("from (").append(exportedKeysQuery).append(") ORDER BY FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, KEY_SEQ");
      } else {
         sql.append("limit 0");
      }

      return ((CoreStatement)stat).executeQuery(sql.toString(), true);
   }

   private StringBuilder appendDummyForeignKeyList(StringBuilder sql) {
      sql.append("select -1 as ks, '' as ptn, '' as fcn, '' as pcn, ").append(3).append(" as ur, ").append(3).append(" as dr, ").append(" '' as fkn, ").append(" '' as pkn ").append(") limit 0;");
      return sql;
   }

   public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
      Statement stat = this.conn.createStatement();
      StringBuilder sql = new StringBuilder(700);
      sql.append("select ").append(quote(this.escape(catalog))).append(" as PKTABLE_CAT, ").append(quote(this.escape(schema))).append(" as PKTABLE_SCHEM, ").append("ptn as PKTABLE_NAME, pcn as PKCOLUMN_NAME, ").append(quote(this.escape(catalog))).append(" as FKTABLE_CAT, ").append(quote(this.escape(schema))).append(" as FKTABLE_SCHEM, ").append(quote(this.escape(table))).append(" as FKTABLE_NAME, ").append("fcn as FKCOLUMN_NAME, ks as KEY_SEQ, ur as UPDATE_RULE, dr as DELETE_RULE, fkn as FK_NAME, pkn as PK_NAME, ").append(5).append(" as DEFERRABILITY from (");

      ResultSet rs;
      try {
         rs = stat.executeQuery("pragma foreign_key_list('" + this.escape(table) + "');");
      } catch (SQLException var20) {
         sql = this.appendDummyForeignKeyList(sql);
         return ((CoreStatement)stat).executeQuery(sql.toString(), true);
      }

      ImportedKeyFinder impFkFinder = new ImportedKeyFinder(this, table);
      List<ImportedKeyFinder.ForeignKey> fkNames = impFkFinder.getFkList();

      int i;
      for(i = 0; rs.next(); ++i) {
         int keySeq = rs.getInt(2) + 1;
         int keyId = rs.getInt(1);
         String PKTabName = rs.getString(3);
         String FKColName = rs.getString(4);
         String PKColName = rs.getString(5);
         String pkName = null;

         try {
            PrimaryKeyFinder pkFinder = new PrimaryKeyFinder(this, PKTabName);
            pkName = pkFinder.getName();
            if (PKColName == null) {
               PKColName = pkFinder.getColumns()[0];
            }
         } catch (SQLException var19) {
         }

         String updateRule = rs.getString(6);
         String deleteRule = rs.getString(7);
         if (i > 0) {
            sql.append(" union all ");
         }

         String fkName = null;
         if (fkNames.size() > keyId) {
            fkName = ((ImportedKeyFinder.ForeignKey)fkNames.get(keyId)).getFkName();
         }

         sql.append("select ").append(keySeq).append(" as ks,").append("'").append(this.escape(PKTabName)).append("' as ptn, '").append(this.escape(FKColName)).append("' as fcn, '").append(this.escape(PKColName)).append("' as pcn,").append("case '").append(this.escape(updateRule)).append("'").append(" when 'NO ACTION' then ").append(3).append(" when 'CASCADE' then ").append(0).append(" when 'RESTRICT' then ").append(1).append(" when 'SET NULL' then ").append(2).append(" when 'SET DEFAULT' then ").append(4).append(" end as ur, ").append("case '").append(this.escape(deleteRule)).append("'").append(" when 'NO ACTION' then ").append(3).append(" when 'CASCADE' then ").append(0).append(" when 'RESTRICT' then ").append(1).append(" when 'SET NULL' then ").append(2).append(" when 'SET DEFAULT' then ").append(4).append(" end as dr, ").append(fkName == null ? "''" : quote(fkName)).append(" as fkn, ").append(pkName == null ? "''" : quote(pkName)).append(" as pkn");
      }

      rs.close();
      if (i == 0) {
         sql = this.appendDummyForeignKeyList(sql);
      } else {
         sql.append(") ORDER BY PKTABLE_CAT, PKTABLE_SCHEM, PKTABLE_NAME, KEY_SEQ;");
      }

      return ((CoreStatement)stat).executeQuery(sql.toString(), true);
   }

   public ResultSet getIndexInfo(String c, String s, String table, boolean u, boolean approximate) throws SQLException {
      Statement stat = this.conn.createStatement();
      StringBuilder sql = new StringBuilder(500);
      sql.append("select null as TABLE_CAT, null as TABLE_SCHEM, '").append(this.escape(table)).append("' as TABLE_NAME, un as NON_UNIQUE, null as INDEX_QUALIFIER, n as INDEX_NAME, ").append(Integer.toString(3)).append(" as TYPE, op as ORDINAL_POSITION, ").append("cn as COLUMN_NAME, null as ASC_OR_DESC, 0 as CARDINALITY, 0 as PAGES, null as FILTER_CONDITION from (");
      ResultSet rs = stat.executeQuery("pragma index_list('" + this.escape(table) + "');");
      ArrayList<ArrayList<Object>> indexList = new ArrayList();

      while(rs.next()) {
         indexList.add(new ArrayList());
         ((ArrayList)indexList.get(indexList.size() - 1)).add(rs.getString(2));
         ((ArrayList)indexList.get(indexList.size() - 1)).add(rs.getInt(3));
      }

      rs.close();
      if (indexList.size() == 0) {
         sql.append("select null as un, null as n, null as op, null as cn) limit 0;");
         return ((CoreStatement)stat).executeQuery(sql.toString(), true);
      } else {
         Iterator<ArrayList<Object>> indexIterator = indexList.iterator();
         ArrayList<String> unionAll = new ArrayList();

         while(indexIterator.hasNext()) {
            ArrayList<Object> currentIndex = (ArrayList)indexIterator.next();
            String indexName = currentIndex.get(0).toString();
            rs = stat.executeQuery("pragma index_info('" + this.escape(indexName) + "');");

            while(rs.next()) {
               StringBuilder sqlRow = new StringBuilder();
               String colName = rs.getString(3);
               sqlRow.append("select ").append(1 - (Integer)currentIndex.get(1)).append(" as un,'").append(this.escape(indexName)).append("' as n,").append(rs.getInt(1) + 1).append(" as op,");
               if (colName == null) {
                  sqlRow.append("null");
               } else {
                  sqlRow.append("'").append(this.escape(colName)).append("'");
               }

               sqlRow.append(" as cn");
               unionAll.add(sqlRow.toString());
            }

            rs.close();
         }

         String sqlBlock = StringUtils.join(unionAll, " union all ");
         return ((CoreStatement)stat).executeQuery(sql.append(sqlBlock).append(");").toString(), true);
      }
   }

   public ResultSet getProcedureColumns(String c, String s, String p, String colPat) throws SQLException {
      if (this.getProcedureColumns == null) {
         this.getProcedureColumns = this.conn.prepareStatement("select null as PROCEDURE_CAT, null as PROCEDURE_SCHEM, null as PROCEDURE_NAME, null as COLUMN_NAME, null as COLUMN_TYPE, null as DATA_TYPE, null as TYPE_NAME, null as PRECISION, null as LENGTH, null as SCALE, null as RADIX, null as NULLABLE, null as REMARKS limit 0;");
      }

      return this.getProcedureColumns.executeQuery();
   }

   public ResultSet getProcedures(String c, String s, String p) throws SQLException {
      if (this.getProcedures == null) {
         this.getProcedures = this.conn.prepareStatement("select null as PROCEDURE_CAT, null as PROCEDURE_SCHEM, null as PROCEDURE_NAME, null as UNDEF1, null as UNDEF2, null as UNDEF3, null as REMARKS, null as PROCEDURE_TYPE limit 0;");
      }

      return this.getProcedures.executeQuery();
   }

   public ResultSet getSuperTables(String c, String s, String t) throws SQLException {
      if (this.getSuperTables == null) {
         this.getSuperTables = this.conn.prepareStatement("select null as TABLE_CAT, null as TABLE_SCHEM, null as TABLE_NAME, null as SUPERTABLE_NAME limit 0;");
      }

      return this.getSuperTables.executeQuery();
   }

   public ResultSet getSuperTypes(String c, String s, String t) throws SQLException {
      if (this.getSuperTypes == null) {
         this.getSuperTypes = this.conn.prepareStatement("select null as TYPE_CAT, null as TYPE_SCHEM, null as TYPE_NAME, null as SUPERTYPE_CAT, null as SUPERTYPE_SCHEM, null as SUPERTYPE_NAME limit 0;");
      }

      return this.getSuperTypes.executeQuery();
   }

   public ResultSet getTablePrivileges(String c, String s, String t) throws SQLException {
      if (this.getTablePrivileges == null) {
         this.getTablePrivileges = this.conn.prepareStatement("select  null as TABLE_CAT, null as TABLE_SCHEM, null as TABLE_NAME, null as GRANTOR, null GRANTEE,  null as PRIVILEGE, null as IS_GRANTABLE limit 0;");
      }

      return this.getTablePrivileges.executeQuery();
   }

   public synchronized ResultSet getTables(String c, String s, String tblNamePattern, String[] types) throws SQLException {
      this.checkOpen();
      tblNamePattern = tblNamePattern != null && !"".equals(tblNamePattern) ? this.escape(tblNamePattern) : "%";
      StringBuilder sql = new StringBuilder();
      sql.append("SELECT").append("\n");
      sql.append("  NULL AS TABLE_CAT,").append("\n");
      sql.append("  NULL AS TABLE_SCHEM,").append("\n");
      sql.append("  NAME AS TABLE_NAME,").append("\n");
      sql.append("  TYPE AS TABLE_TYPE,").append("\n");
      sql.append("  NULL AS REMARKS,").append("\n");
      sql.append("  NULL AS TYPE_CAT,").append("\n");
      sql.append("  NULL AS TYPE_SCHEM,").append("\n");
      sql.append("  NULL AS TYPE_NAME,").append("\n");
      sql.append("  NULL AS SELF_REFERENCING_COL_NAME,").append("\n");
      sql.append("  NULL AS REF_GENERATION").append("\n");
      sql.append("FROM").append("\n");
      sql.append("  (").append("\n");
      sql.append("    SELECT\n");
      sql.append("      'sqlite_schema' AS NAME,\n");
      sql.append("      'SYSTEM TABLE' AS TYPE");
      sql.append("    UNION ALL").append("\n");
      sql.append("    SELECT").append("\n");
      sql.append("      NAME,").append("\n");
      sql.append("      UPPER(TYPE) AS TYPE").append("\n");
      sql.append("    FROM").append("\n");
      sql.append("      sqlite_schema").append("\n");
      sql.append("    WHERE").append("\n");
      sql.append("      NAME NOT LIKE 'sqlite\\_%' ESCAPE '\\'").append("\n");
      sql.append("      AND UPPER(TYPE) IN ('TABLE', 'VIEW')").append("\n");
      sql.append("    UNION ALL").append("\n");
      sql.append("    SELECT").append("\n");
      sql.append("      NAME,").append("\n");
      sql.append("      'GLOBAL TEMPORARY' AS TYPE").append("\n");
      sql.append("    FROM").append("\n");
      sql.append("      sqlite_temp_master").append("\n");
      sql.append("    UNION ALL").append("\n");
      sql.append("    SELECT").append("\n");
      sql.append("      NAME,").append("\n");
      sql.append("      'SYSTEM TABLE' AS TYPE").append("\n");
      sql.append("    FROM").append("\n");
      sql.append("      sqlite_schema").append("\n");
      sql.append("    WHERE").append("\n");
      sql.append("      NAME LIKE 'sqlite\\_%' ESCAPE '\\'").append("\n");
      sql.append("  )").append("\n");
      sql.append(" WHERE TABLE_NAME LIKE '");
      sql.append(tblNamePattern);
      sql.append("' ESCAPE '");
      sql.append(this.getSearchStringEscape());
      sql.append("'");
      if (types != null && types.length != 0) {
         sql.append(" AND TABLE_TYPE IN (");
         sql.append((String)Arrays.stream(types).map((t) -> "'" + this.escape(t.toUpperCase()) + "'").collect(Collectors.joining(",")));
         sql.append(")");
      }

      sql.append(" ORDER BY TABLE_TYPE, TABLE_NAME;");
      return ((CoreStatement)this.conn.createStatement()).executeQuery(sql.toString(), true);
   }

   public ResultSet getTableTypes() throws SQLException {
      this.checkOpen();
      String sql = "SELECT 'TABLE' AS TABLE_TYPE UNION SELECT 'VIEW' AS TABLE_TYPE UNION SELECT 'SYSTEM TABLE' AS TABLE_TYPE UNION SELECT 'GLOBAL TEMPORARY' AS TABLE_TYPE;";
      if (this.getTableTypes == null) {
         this.getTableTypes = this.conn.prepareStatement(sql);
      }

      this.getTableTypes.clearParameters();
      return this.getTableTypes.executeQuery();
   }

   public ResultSet getTypeInfo() throws SQLException {
      if (this.getTypeInfo == null) {
         String sql = QueryUtils.valuesQuery(Arrays.asList("TYPE_NAME", "DATA_TYPE", "PRECISION", "LITERAL_PREFIX", "LITERAL_SUFFIX", "CREATE_PARAMS", "NULLABLE", "CASE_SENSITIVE", "SEARCHABLE", "UNSIGNED_ATTRIBUTE", "FIXED_PREC_SCALE", "AUTO_INCREMENT", "LOCAL_TYPE_NAME", "MINIMUM_SCALE", "MAXIMUM_SCALE", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "NUM_PREC_RADIX"), Arrays.asList(Arrays.asList("BLOB", 2004, 0, null, null, null, 1, 0, 3, 1, 0, 0, null, 0, 0, 0, 0, 10), Arrays.asList("INTEGER", 4, 0, null, null, null, 1, 0, 3, 0, 0, 1, null, 0, 0, 0, 0, 10), Arrays.asList("NULL", 0, 0, null, null, null, 1, 0, 3, 1, 0, 0, null, 0, 0, 0, 0, 10), Arrays.asList("REAL", 7, 0, null, null, null, 1, 0, 3, 0, 0, 0, null, 0, 0, 0, 0, 10), Arrays.asList("TEXT", 12, 0, null, null, null, 1, 1, 3, 1, 0, 0, null, 0, 0, 0, 0, 10))) + " order by DATA_TYPE";
         this.getTypeInfo = this.conn.prepareStatement(sql);
      }

      this.getTypeInfo.clearParameters();
      return this.getTypeInfo.executeQuery();
   }

   public ResultSet getUDTs(String c, String s, String t, int[] types) throws SQLException {
      if (this.getUDTs == null) {
         this.getUDTs = this.conn.prepareStatement("select  null as TYPE_CAT, null as TYPE_SCHEM, null as TYPE_NAME,  null as CLASS_NAME,  null as DATA_TYPE, null as REMARKS, null as BASE_TYPE limit 0;");
      }

      this.getUDTs.clearParameters();
      return this.getUDTs.executeQuery();
   }

   public ResultSet getVersionColumns(String c, String s, String t) throws SQLException {
      if (this.getVersionColumns == null) {
         this.getVersionColumns = this.conn.prepareStatement("select null as SCOPE, null as COLUMN_NAME, null as DATA_TYPE, null as TYPE_NAME, null as COLUMN_SIZE, null as BUFFER_LENGTH, null as DECIMAL_DIGITS, null as PSEUDO_COLUMN limit 0;");
      }

      return this.getVersionColumns.executeQuery();
   }

   /** @deprecated */
   @Deprecated
   public ResultSet getGeneratedKeys() throws SQLException {
      throw new SQLFeatureNotSupportedException("not implemented by SQLite JDBC driver");
   }

   public Struct createStruct(String t, Object[] attr) throws SQLException {
      throw new SQLFeatureNotSupportedException("Not yet implemented by SQLite JDBC driver");
   }

   public ResultSet getFunctionColumns(String a, String b, String c, String d) throws SQLException {
      throw new SQLFeatureNotSupportedException("Not yet implemented by SQLite JDBC driver");
   }

   protected void finalize() throws Throwable {
      this.close();
   }

   private String unquoteIdentifier(String name) {
      if (name == null) {
         return name;
      } else {
         name = name.trim();
         if (name.length() > 2 && (name.startsWith("`") && name.endsWith("`") || name.startsWith("\"") && name.endsWith("\"") || name.startsWith("[") && name.endsWith("]"))) {
            name = name.substring(1, name.length() - 1);
         }

         return name;
      }
   }

   static {
      try {
         InputStream sqliteJdbcPropStream = JDBC3DatabaseMetaData.class.getClassLoader().getResourceAsStream("sqlite-jdbc.properties");

         try {
            if (sqliteJdbcPropStream == null) {
               throw new IOException("Cannot load sqlite-jdbc.properties from jar");
            }

            Properties sqliteJdbcProp = new Properties();
            sqliteJdbcProp.load(sqliteJdbcPropStream);
            driverName = sqliteJdbcProp.getProperty("name");
            driverVersion = sqliteJdbcProp.getProperty("version");
         } catch (Throwable var4) {
            if (sqliteJdbcPropStream != null) {
               try {
                  sqliteJdbcPropStream.close();
               } catch (Throwable var3) {
                  var4.addSuppressed(var3);
               }
            }

            throw var4;
         }

         if (sqliteJdbcPropStream != null) {
            sqliteJdbcPropStream.close();
         }
      } catch (Exception var5) {
         driverName = "SQLite JDBC";
         driverVersion = "3.0.0-UNKNOWN";
      }

      TYPE_INTEGER = Pattern.compile(".*(INT|BOOL).*");
      TYPE_VARCHAR = Pattern.compile(".*(CHAR|CLOB|TEXT|BLOB).*");
      TYPE_FLOAT = Pattern.compile(".*(REAL|FLOA|DOUB|DEC|NUM).*");
      RULE_MAP = new HashMap();
      RULE_MAP.put("NO ACTION", 3);
      RULE_MAP.put("CASCADE", 0);
      RULE_MAP.put("RESTRICT", 1);
      RULE_MAP.put("SET NULL", 2);
      RULE_MAP.put("SET DEFAULT", 4);
      PK_UNNAMED_PATTERN = Pattern.compile(".*PRIMARY\\s+KEY\\s*\\((.*?)\\).*", 34);
      PK_NAMED_PATTERN = Pattern.compile(".*CONSTRAINT\\s*(.*?)\\s*PRIMARY\\s+KEY\\s*\\((.*?)\\).*", 34);
   }

   class PrimaryKeyFinder {
      String table;
      String pkName = null;
      String[] pkColumns = null;

      public PrimaryKeyFinder(JDBC3DatabaseMetaData this$0, String table) throws SQLException {
         this.table = table;
         if (!"sqlite_schema".equals(table) && !"sqlite_master".equals(table)) {
            if (table != null && table.trim().length() != 0) {
               Statement stat = this$0.conn.createStatement();

               try {
                  ResultSet rs = stat.executeQuery("select sql from sqlite_schema where lower(name) = lower('" + this$0.escape(table) + "') and type in ('table', 'view')");

                  try {
                     if (!rs.next()) {
                        throw new SQLException("Table not found: '" + table + "'");
                     }

                     Matcher matcher = JDBC3DatabaseMetaData.PK_NAMED_PATTERN.matcher(rs.getString(1));
                     if (matcher.find()) {
                        this.pkName = this$0.unquoteIdentifier(this$0.escape(matcher.group(1)));
                        this.pkColumns = matcher.group(2).split(",");
                     } else {
                        matcher = JDBC3DatabaseMetaData.PK_UNNAMED_PATTERN.matcher(rs.getString(1));
                        if (matcher.find()) {
                           this.pkColumns = matcher.group(1).split(",");
                        }
                     }

                     if (this.pkColumns == null) {
                        ResultSet rs2 = stat.executeQuery("pragma table_info('" + this$0.escape(table) + "');");

                        try {
                           while(rs2.next()) {
                              if (rs2.getBoolean(6)) {
                                 this.pkColumns = new String[]{rs2.getString(2)};
                              }
                           }
                        } catch (Throwable var12) {
                           if (rs2 != null) {
                              try {
                                 rs2.close();
                              } catch (Throwable var11) {
                                 var12.addSuppressed(var11);
                              }
                           }

                           throw var12;
                        }

                        if (rs2 != null) {
                           rs2.close();
                        }
                     }

                     if (this.pkColumns != null) {
                        for(int i = 0; i < this.pkColumns.length; ++i) {
                           this.pkColumns[i] = this$0.unquoteIdentifier(this.pkColumns[i]);
                        }
                     }
                  } catch (Throwable var13) {
                     if (rs != null) {
                        try {
                           rs.close();
                        } catch (Throwable var10) {
                           var13.addSuppressed(var10);
                        }
                     }

                     throw var13;
                  }

                  if (rs != null) {
                     rs.close();
                  }
               } catch (Throwable var14) {
                  if (stat != null) {
                     try {
                        stat.close();
                     } catch (Throwable var9) {
                        var14.addSuppressed(var9);
                     }
                  }

                  throw var14;
               }

               if (stat != null) {
                  stat.close();
               }

            } else {
               throw new SQLException("Invalid table name: '" + this.table + "'");
            }
         }
      }

      public String getName() {
         return this.pkName;
      }

      public String[] getColumns() {
         return this.pkColumns;
      }
   }

   class ImportedKeyFinder {
      private final Pattern FK_NAMED_PATTERN = Pattern.compile("CONSTRAINT\\s*\"?([A-Za-z_][A-Za-z\\d_]*)?\"?\\s*FOREIGN\\s+KEY\\s*\\((.*?)\\)", 34);
      private final String fkTableName;
      private final List<ForeignKey> fkList = new ArrayList();

      public ImportedKeyFinder(JDBC3DatabaseMetaData this$0, String table) throws SQLException {
         if (table != null && table.trim().length() != 0) {
            this.fkTableName = table;
            List<String> fkNames = this.getForeignKeyNames(this.fkTableName);
            Statement stat = this$0.conn.createStatement();

            try {
               ResultSet rs = stat.executeQuery("pragma foreign_key_list('" + this$0.escape(this.fkTableName.toLowerCase()) + "')");

               try {
                  int prevFkId = -1;
                  int count = 0;
                  ForeignKey fk = null;

                  while(rs.next()) {
                     int fkId = rs.getInt(1);
                     String pkTableName = rs.getString(3);
                     String fkColName = rs.getString(4);
                     String pkColName = rs.getString(5);
                     String onUpdate = rs.getString(6);
                     String onDelete = rs.getString(7);
                     String match = rs.getString(8);
                     String fkName = null;
                     if (fkNames.size() > count) {
                        fkName = (String)fkNames.get(count);
                     }

                     if (fkId != prevFkId) {
                        fk = new ForeignKey(this, fkName, pkTableName, this.fkTableName, onUpdate, onDelete, match);
                        this.fkList.add(fk);
                        prevFkId = fkId;
                        ++count;
                     }

                     if (fk != null) {
                        fk.addColumnMapping(fkColName, pkColName);
                     }
                  }
               } catch (Throwable var19) {
                  if (rs != null) {
                     try {
                        rs.close();
                     } catch (Throwable var18) {
                        var19.addSuppressed(var18);
                     }
                  }

                  throw var19;
               }

               if (rs != null) {
                  rs.close();
               }
            } catch (Throwable var20) {
               if (stat != null) {
                  try {
                     stat.close();
                  } catch (Throwable var17) {
                     var20.addSuppressed(var17);
                  }
               }

               throw var20;
            }

            if (stat != null) {
               stat.close();
            }

         } else {
            throw new SQLException("Invalid table name: '" + table + "'");
         }
      }

      private List<String> getForeignKeyNames(String tbl) throws SQLException {
         List<String> fkNames = new ArrayList();
         if (tbl == null) {
            return fkNames;
         } else {
            Statement stat2 = JDBC3DatabaseMetaData.this.conn.createStatement();

            try {
               ResultSet rs = stat2.executeQuery("select sql from sqlite_schema where lower(name) = lower('" + JDBC3DatabaseMetaData.this.escape(tbl) + "')");

               try {
                  if (rs.next()) {
                     Matcher matcher = this.FK_NAMED_PATTERN.matcher(rs.getString(1));

                     while(matcher.find()) {
                        fkNames.add(matcher.group(1));
                     }
                  }
               } catch (Throwable var9) {
                  if (rs != null) {
                     try {
                        rs.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (rs != null) {
                  rs.close();
               }
            } catch (Throwable var10) {
               if (stat2 != null) {
                  try {
                     stat2.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (stat2 != null) {
               stat2.close();
            }

            Collections.reverse(fkNames);
            return fkNames;
         }
      }

      public String getFkTableName() {
         return this.fkTableName;
      }

      public List<ForeignKey> getFkList() {
         return this.fkList;
      }

      class ForeignKey {
         private final String fkName;
         private final String pkTableName;
         private final String fkTableName;
         private final List<String> fkColNames = new ArrayList();
         private final List<String> pkColNames = new ArrayList();
         private final String onUpdate;
         private final String onDelete;
         private final String match;

         ForeignKey(ImportedKeyFinder this$1, String fkName, String pkTableName, String fkTableName, String onUpdate, String onDelete, String match) {
            this.fkName = fkName;
            this.pkTableName = pkTableName;
            this.fkTableName = fkTableName;
            this.onUpdate = onUpdate;
            this.onDelete = onDelete;
            this.match = match;
         }

         public String getFkName() {
            return this.fkName;
         }

         void addColumnMapping(String fkColName, String pkColName) {
            this.fkColNames.add(fkColName);
            this.pkColNames.add(pkColName);
         }

         public String[] getColumnMapping(int colSeq) {
            return new String[]{(String)this.fkColNames.get(colSeq), (String)this.pkColNames.get(colSeq)};
         }

         public int getColumnMappingCount() {
            return this.fkColNames.size();
         }

         public String getPkTableName() {
            return this.pkTableName;
         }

         public String getFkTableName() {
            return this.fkTableName;
         }

         public String getOnUpdate() {
            return this.onUpdate;
         }

         public String getOnDelete() {
            return this.onDelete;
         }

         public String getMatch() {
            return this.match;
         }

         public String toString() {
            return "ForeignKey [fkName=" + this.fkName + ", pkTableName=" + this.pkTableName + ", fkTableName=" + this.fkTableName + ", pkColNames=" + this.pkColNames + ", fkColNames=" + this.fkColNames + "]";
         }
      }
   }

   private static class LogHolder {
      private static final Logger logger = LoggerFactory.getLogger(JDBC3DatabaseMetaData.class);
   }
}
