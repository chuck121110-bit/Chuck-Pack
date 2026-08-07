package xaeroplus.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class FileUtil {
   private FileUtil() {
   }

   public static void safeSave(File outputFile, Consumer<Writer> fileWriter) throws RuntimeException {
      try {
         Path tempFilePath = Files.createTempFile("xaeroplus", ".tmp");
         BufferedWriter writer = Files.newBufferedWriter(tempFilePath);

         try {
            fileWriter.accept(writer);
         } catch (Throwable var8) {
            if (writer != null) {
               try {
                  writer.close();
               } catch (Throwable var6) {
                  var8.addSuppressed(var6);
               }
            }

            throw var8;
         }

         if (writer != null) {
            writer.close();
         }

         try {
            Files.move(tempFilePath, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException var7) {
            Files.move(tempFilePath, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
         }

      } catch (Exception e) {
         throw new RuntimeException("Error during safeSave", e);
      }
   }
}
