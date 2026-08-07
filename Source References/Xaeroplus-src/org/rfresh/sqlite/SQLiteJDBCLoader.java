package org.rfresh.sqlite;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;
import org.rfresh.sqlite.util.AndroidSignatureIgnore;
import org.rfresh.sqlite.util.LibraryLoaderUtil;
import org.rfresh.sqlite.util.Logger;
import org.rfresh.sqlite.util.LoggerFactory;
import org.rfresh.sqlite.util.OSInfo;
import org.rfresh.sqlite.util.StringUtils;

@AndroidSignatureIgnore(
   explanation = "The loader is not used on Android"
)
public class SQLiteJDBCLoader {
   private static final Logger logger = LoggerFactory.getLogger(SQLiteJDBCLoader.class);
   private static final String LOCK_EXT = ".lck";
   private static boolean extracted = false;

   public static synchronized boolean initialize() throws Exception {
      if (!extracted) {
         cleanup();
      }

      loadSQLiteNativeLibrary();
      return extracted;
   }

   private static File getTempDir() {
      return new File(System.getProperty("org.rfresh.sqlite.tmpdir", System.getProperty("java.io.tmpdir")));
   }

   static void cleanup() {
      String searchPattern = "sqlite-" + getVersion();

      try {
         Stream<Path> dirList = Files.list(getTempDir().toPath());

         try {
            dirList.filter((path) -> !path.getFileName().toString().endsWith(".lck") && path.getFileName().toString().startsWith(searchPattern)).forEach((nativeLib) -> {
               Path lckFile = Paths.get(nativeLib + ".lck");
               if (Files.notExists(lckFile, new LinkOption[0])) {
                  try {
                     Files.delete(nativeLib);
                  } catch (Exception e) {
                     logger.error(() -> "Failed to delete old native lib", e);
                  }
               }

            });
         } catch (Throwable var5) {
            if (dirList != null) {
               try {
                  dirList.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (dirList != null) {
            dirList.close();
         }
      } catch (IOException e) {
         logger.error(() -> "Failed to open directory", e);
      }

   }

   public static boolean isNativeMode() throws Exception {
      initialize();
      return extracted;
   }

   static String md5sum(InputStream input) throws IOException {
      BufferedInputStream in = new BufferedInputStream(input);

      String var5;
      try {
         MessageDigest digest = MessageDigest.getInstance("MD5");
         DigestInputStream digestInputStream = new DigestInputStream(in, digest);

         while(digestInputStream.read() >= 0) {
         }

         ByteArrayOutputStream md5out = new ByteArrayOutputStream();
         md5out.write(digest.digest());
         var5 = md5out.toString();
      } catch (NoSuchAlgorithmException e) {
         throw new IllegalStateException("MD5 algorithm is not available: " + e);
      } finally {
         in.close();
      }

      return var5;
   }

   private static boolean contentsEquals(InputStream in1, InputStream in2) throws IOException {
      if (!(in1 instanceof BufferedInputStream)) {
         in1 = new BufferedInputStream(in1);
      }

      if (!(in2 instanceof BufferedInputStream)) {
         in2 = new BufferedInputStream(in2);
      }

      for(int ch = in1.read(); ch != -1; ch = in1.read()) {
         int ch2 = in2.read();
         if (ch != ch2) {
            return false;
         }
      }

      int ch2 = in2.read();
      return ch2 == -1;
   }

   private static boolean extractAndLoadLibraryFile(String libFolderForCurrentOS, String libraryFileName, String targetFolder) throws FileException {
      String nativeLibraryFilePath = libFolderForCurrentOS + "/" + libraryFileName;
      String uuid = UUID.randomUUID().toString();
      String extractedLibFileName = String.format("sqlite-%s-%s-%s", getVersion(), uuid, libraryFileName);
      String extractedLckFileName = extractedLibFileName + ".lck";
      Path extractedLibFile = Paths.get(targetFolder, extractedLibFileName);
      Path extractedLckFile = Paths.get(targetFolder, extractedLckFileName);

      try {
         try {
            InputStream reader = getResourceAsStream(nativeLibraryFilePath);

            try {
               if (Files.notExists(extractedLckFile, new LinkOption[0])) {
                  Files.createFile(extractedLckFile);
               }

               Files.copy(reader, extractedLibFile, new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
            } catch (Throwable var26) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var23) {
                     var26.addSuppressed(var23);
                  }
               }

               throw var26;
            }

            if (reader != null) {
               reader.close();
            }
         } finally {
            extractedLibFile.toFile().deleteOnExit();
            extractedLckFile.toFile().deleteOnExit();
         }

         extractedLibFile.toFile().setReadable(true);
         extractedLibFile.toFile().setWritable(true, true);
         extractedLibFile.toFile().setExecutable(true);
         InputStream nativeIn = getResourceAsStream(nativeLibraryFilePath);

         try {
            InputStream extractedLibIn = Files.newInputStream(extractedLibFile);

            try {
               if (!contentsEquals(nativeIn, extractedLibIn)) {
                  throw new FileException(String.format("Failed to write a native library file at %s", extractedLibFile));
               }
            } catch (Throwable var24) {
               if (extractedLibIn != null) {
                  try {
                     extractedLibIn.close();
                  } catch (Throwable var22) {
                     var24.addSuppressed(var22);
                  }
               }

               throw var24;
            }

            if (extractedLibIn != null) {
               extractedLibIn.close();
            }
         } catch (Throwable var25) {
            if (nativeIn != null) {
               try {
                  nativeIn.close();
               } catch (Throwable var21) {
                  var25.addSuppressed(var21);
               }
            }

            throw var25;
         }

         if (nativeIn != null) {
            nativeIn.close();
         }

         return loadNativeLibrary(targetFolder, extractedLibFileName);
      } catch (IOException e) {
         logger.error(() -> "Unexpected IOException", e);
         return false;
      }
   }

   private static InputStream getResourceAsStream(String name) {
      String resolvedName = name.substring(1);
      ClassLoader cl = SQLiteJDBCLoader.class.getClassLoader();
      URL url = cl.getResource(resolvedName);
      if (url == null) {
         return null;
      } else {
         try {
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
         } catch (IOException e) {
            logger.error(() -> "Could not connect", e);
            return null;
         }
      }
   }

   private static boolean loadNativeLibrary(String path, String name) {
      File libPath = new File(path, name);
      if (libPath.exists()) {
         try {
            System.load((new File(path, name)).getAbsolutePath());
            return true;
         } catch (UnsatisfiedLinkError e) {
            logger.error(() -> MessageFormat.format("Failed to load native library: {0}. osinfo: {1}", name, OSInfo.getNativeLibFolderPathForCurrentOS()), e);
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean loadNativeLibraryJdk() {
      try {
         System.loadLibrary("sqlitejdbc");
         return true;
      } catch (UnsatisfiedLinkError e) {
         logger.error(() -> "Failed to load native library through System.loadLibrary", e);
         return false;
      }
   }

   private static void loadSQLiteNativeLibrary() throws Exception {
      if (!extracted) {
         List<String> triedPaths = new LinkedList();
         String sqliteNativeLibraryPath = System.getProperty("org.rfresh.sqlite.lib.path");
         String sqliteNativeLibraryName = System.getProperty("org.rfresh.sqlite.lib.name");
         if (sqliteNativeLibraryName == null) {
            sqliteNativeLibraryName = LibraryLoaderUtil.getNativeLibName();
         }

         if (sqliteNativeLibraryPath != null) {
            if (loadNativeLibrary(sqliteNativeLibraryPath, sqliteNativeLibraryName)) {
               extracted = true;
               return;
            }

            triedPaths.add(sqliteNativeLibraryPath);
         }

         sqliteNativeLibraryPath = LibraryLoaderUtil.getNativeLibResourcePath();
         boolean hasNativeLib = LibraryLoaderUtil.hasNativeLib(sqliteNativeLibraryPath, sqliteNativeLibraryName);
         if (hasNativeLib) {
            String tempFolder = getTempDir().getAbsolutePath();
            if (extractAndLoadLibraryFile(sqliteNativeLibraryPath, sqliteNativeLibraryName, tempFolder)) {
               extracted = true;
               return;
            }

            triedPaths.add(sqliteNativeLibraryPath);
         }

         String javaLibraryPath = System.getProperty("java.library.path", "");

         for(String ldPath : javaLibraryPath.split(File.pathSeparator)) {
            if (!ldPath.isEmpty()) {
               if (loadNativeLibrary(ldPath, sqliteNativeLibraryName)) {
                  extracted = true;
                  return;
               }

               triedPaths.add(ldPath);
            }
         }

         if (loadNativeLibraryJdk()) {
            extracted = true;
         } else {
            extracted = false;
            throw new NativeLibraryNotFoundException(String.format("No native library found for os.name=%s, os.arch=%s, paths=[%s]", OSInfo.getOSName(), OSInfo.getArchName(), StringUtils.join(triedPaths, File.pathSeparator)));
         }
      }
   }

   private static void getNativeLibraryFolderForTheCurrentOS() {
      String osName = OSInfo.getOSName();
      String archName = OSInfo.getArchName();
   }

   public static int getMajorVersion() {
      String[] c = getVersion().split("\\.");
      return c.length > 0 ? Integer.parseInt(c[0]) : 1;
   }

   public static int getMinorVersion() {
      String[] c = getVersion().split("\\.");
      return c.length > 1 ? Integer.parseInt(c[1]) : 0;
   }

   public static String getVersion() {
      return SQLiteJDBCLoader.VersionHolder.VERSION;
   }

   public static final class VersionHolder {
      private static final String VERSION;

      static {
         URL versionFile = VersionHolder.class.getResource("/META-INF/maven/org.rfresh.xerial/sqlite-jdbc/pom.properties");
         if (versionFile == null) {
            versionFile = VersionHolder.class.getResource("/META-INF/maven/org.rfresh.xerial/sqlite-jdbc/VERSION");
         }

         String version = "unknown";

         try {
            if (versionFile != null) {
               Properties versionData = new Properties();
               versionData.load(versionFile.openStream());
               version = versionData.getProperty("version", version);
               version = version.trim().replaceAll("[^0-9\\.]", "");
            }
         } catch (IOException e) {
            LoggerFactory.getLogger(VersionHolder.class).error(() -> MessageFormat.format("Could not read version from file: {0}", versionFile), e);
         }

         VERSION = version;
      }
   }
}
