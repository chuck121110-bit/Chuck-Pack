package org.rfresh.sqlite.util;

import org.rfresh.sqlite.SQLiteJDBCLoader;

public class LibraryLoaderUtil {
   public static final String NATIVE_LIB_BASE_NAME = "sqlitejdbc";

   public static String getNativeLibResourcePath() {
      String packagePath = SQLiteJDBCLoader.class.getPackage().getName().replace(".", "/");
      return String.format("/%s/native/%s", packagePath, OSInfo.getNativeLibFolderPathForCurrentOS());
   }

   public static String getNativeLibName() {
      return System.mapLibraryName("sqlitejdbc");
   }

   public static boolean hasNativeLib(String path, String libraryName) {
      return SQLiteJDBCLoader.class.getResource(path + "/" + libraryName) != null;
   }
}
