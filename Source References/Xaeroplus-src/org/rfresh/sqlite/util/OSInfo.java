package org.rfresh.sqlite.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Stream;

public class OSInfo {
   protected static ProcessRunner processRunner = new ProcessRunner();
   private static final HashMap<String, String> archMapping = new HashMap();
   public static final String X86 = "x86";
   public static final String X86_64 = "x86_64";
   public static final String IA64_32 = "ia64_32";
   public static final String IA64 = "ia64";
   public static final String PPC = "ppc";
   public static final String PPC64 = "ppc64";
   public static final String RISCV64 = "riscv64";

   public static void main(String[] args) {
      if (args.length >= 1) {
         if ("--os".equals(args[0])) {
            System.out.print(getOSName());
            return;
         }

         if ("--arch".equals(args[0])) {
            System.out.print(getArchName());
            return;
         }
      }

      System.out.print(getNativeLibFolderPathForCurrentOS());
   }

   public static String getNativeLibFolderPathForCurrentOS() {
      return getOSName() + "/" + getArchName();
   }

   public static String getOSName() {
      return translateOSNameToFolderName(System.getProperty("os.name"));
   }

   public static boolean isAndroid() {
      return isAndroidRuntime() || isAndroidTermux() || isRunningAndroid();
   }

   private static boolean isRunningAndroid() {
      File androidGLES = new File("/system/lib/libGLESv1_CM.so");
      File android64GLES = new File("/system/lib64/libGLESv1_CM.so");
      return android64GLES.exists() || androidGLES.exists();
   }

   public static boolean isAndroidRuntime() {
      return System.getProperty("java.runtime.name", "").toLowerCase().contains("android");
   }

   public static boolean isAndroidTermux() {
      try {
         return processRunner.runAndWaitFor("uname -o").toLowerCase().contains("android");
      } catch (Exception var1) {
         return false;
      }
   }

   @AndroidSignatureIgnore(
      explanation = "Should not reach this code path"
   )
   public static boolean isMusl() {
      Path mapFilesDir = Paths.get("/proc/self/map_files");

      try {
         Stream<Path> dirStream = Files.list(mapFilesDir);

         boolean var3;
         label54: {
            try {
               boolean found = dirStream.map(OSInfo::toRealPathOrEmpty).anyMatch((s) -> s.toLowerCase().contains("musl"));
               if (found) {
                  var3 = true;
                  break label54;
               }
            } catch (Throwable var5) {
               if (dirStream != null) {
                  try {
                     dirStream.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (dirStream != null) {
               dirStream.close();
            }

            return isAlpineLinux();
         }

         if (dirStream != null) {
            dirStream.close();
         }

         return var3;
      } catch (Exception var6) {
         return isAlpineLinux();
      }
   }

   @AndroidSignatureIgnore(
      explanation = "Should not reach this code path"
   )
   private static String toRealPathOrEmpty(Path path) {
      try {
         return path.toRealPath().toString();
      } catch (IOException var2) {
         return "";
      }
   }

   @AndroidSignatureIgnore(
      explanation = "Should not reach this code path"
   )
   private static boolean isAlpineLinux() {
      try {
         Stream<String> osLines = Files.lines(Paths.get("/etc/os-release"));

         boolean var1;
         try {
            var1 = osLines.anyMatch((l) -> l.startsWith("ID") && l.contains("alpine"));
         } catch (Throwable var4) {
            if (osLines != null) {
               try {
                  osLines.close();
               } catch (Throwable var3) {
                  var4.addSuppressed(var3);
               }
            }

            throw var4;
         }

         if (osLines != null) {
            osLines.close();
         }

         return var1;
      } catch (Exception var5) {
         return false;
      }
   }

   static String getHardwareName() {
      try {
         return processRunner.runAndWaitFor("uname -m");
      } catch (Throwable e) {
         OSInfo.LogHolder.logger.error(() -> "Error while running uname -m", e);
         return "unknown";
      }
   }

   static String resolveArmArchType() {
      if (System.getProperty("os.name").contains("Linux")) {
         String armType = getHardwareName();
         if (isAndroid()) {
            if (armType.startsWith("aarch64")) {
               return "aarch64";
            }

            return "arm";
         }

         if (armType.startsWith("armv6")) {
            return "armv6";
         }

         if (armType.startsWith("armv7")) {
            return "armv7";
         }

         if (armType.startsWith("armv5")) {
            return "arm";
         }

         if (armType.startsWith("aarch64")) {
            boolean is32bitJVM = "32".equals(System.getProperty("sun.arch.data.model"));
            if (is32bitJVM) {
               return "armv7";
            }

            return "aarch64";
         }

         String abi = System.getProperty("sun.arch.abi");
         if (abi != null && abi.startsWith("gnueabihf")) {
            return "armv7";
         }

         String javaHome = System.getProperty("java.home");

         try {
            int exitCode = Runtime.getRuntime().exec("which readelf").waitFor();
            if (exitCode == 0) {
               String[] cmdarray = new String[]{"/bin/sh", "-c", "find '" + javaHome + "' -name 'libjvm.so' | head -1 | xargs readelf -A | grep 'Tag_ABI_VFP_args: VFP registers'"};
               exitCode = Runtime.getRuntime().exec(cmdarray).waitFor();
               if (exitCode == 0) {
                  return "armv7";
               }
            } else {
               OSInfo.LogHolder.logger.warn(() -> "readelf not found. Cannot check if running on an armhf system, armel architecture will be presumed");
            }
         } catch (InterruptedException | IOException var5) {
         }
      }

      return "arm";
   }

   public static String getArchName() {
      String override = System.getProperty("org.rfresh.sqlite.osinfo.architecture");
      if (override != null) {
         return override;
      } else {
         String osArch = System.getProperty("os.arch");
         if (osArch.startsWith("arm")) {
            osArch = resolveArmArchType();
         } else {
            String lc = osArch.toLowerCase(Locale.US);
            if (archMapping.containsKey(lc)) {
               return (String)archMapping.get(lc);
            }
         }

         return translateArchNameToFolderName(osArch);
      }
   }

   static String translateOSNameToFolderName(String osName) {
      if (osName.contains("Windows")) {
         return "Windows";
      } else if (!osName.contains("Mac") && !osName.contains("Darwin")) {
         if (osName.contains("AIX")) {
            return "AIX";
         } else if (isAndroid()) {
            return "Linux-Android";
         } else if (isMusl()) {
            return "Linux-Musl";
         } else {
            return osName.contains("Linux") ? "Linux" : osName.replaceAll("\\W", "");
         }
      } else {
         return "Mac";
      }
   }

   static String translateArchNameToFolderName(String archName) {
      return archName.replaceAll("\\W", "");
   }

   static {
      archMapping.put("x86", "x86");
      archMapping.put("i386", "x86");
      archMapping.put("i486", "x86");
      archMapping.put("i586", "x86");
      archMapping.put("i686", "x86");
      archMapping.put("pentium", "x86");
      archMapping.put("x86_64", "x86_64");
      archMapping.put("amd64", "x86_64");
      archMapping.put("em64t", "x86_64");
      archMapping.put("universal", "x86_64");
      archMapping.put("ia64", "ia64");
      archMapping.put("ia64w", "ia64");
      archMapping.put("ia64_32", "ia64_32");
      archMapping.put("ia64n", "ia64_32");
      archMapping.put("ppc", "ppc");
      archMapping.put("power", "ppc");
      archMapping.put("powerpc", "ppc");
      archMapping.put("power_pc", "ppc");
      archMapping.put("power_rs", "ppc");
      archMapping.put("ppc64", "ppc64");
      archMapping.put("power64", "ppc64");
      archMapping.put("powerpc64", "ppc64");
      archMapping.put("power_pc64", "ppc64");
      archMapping.put("power_rs64", "ppc64");
      archMapping.put("ppc64el", "ppc64");
      archMapping.put("ppc64le", "ppc64");
      archMapping.put("riscv64", "riscv64");
   }

   private static class LogHolder {
      private static final Logger logger = LoggerFactory.getLogger(OSInfo.class);
   }
}
