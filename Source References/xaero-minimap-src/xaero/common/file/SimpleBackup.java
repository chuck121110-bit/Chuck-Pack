package xaero.common.file;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

public class SimpleBackup {
   public static Path moveToBackup(Path directory) {
      return moveToBackup(directory.getParent(), directory);
   }

   public static Path moveToBackup(Path backupFolderParent, Path directory) {
      Path backupFolder;
      for(backupFolder = backupFolderParent.resolve("backup"); Files.exists(backupFolder, new LinkOption[0]); backupFolder = backupFolder.getParent().resolve(backupFolder.getFileName().toString() + "-")) {
      }

      Path backupPath = backupFolder.resolve(directory.getFileName());

      try {
         Files.createDirectories(backupFolder);
         Files.move(directory, backupPath);
         return backupPath;
      } catch (IOException e) {
         throw new RuntimeException("Failed to backup a directory! Can't continue.", e);
      }
   }

   public static void copyDirectoryWithContents(final Path from, final Path to, int maxDepth, final CopyOption... copyOptions) throws IOException {
      Files.walkFileTree(from, EnumSet.of(FileVisitOption.FOLLOW_LINKS), maxDepth, new SimpleFileVisitor<Path>() {
         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = to.resolve(from.relativize(dir));
            if (!Files.exists(targetPath, new LinkOption[0])) {
               Files.createDirectory(targetPath);
            }

            return FileVisitResult.CONTINUE;
         }

         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, to.resolve(from.relativize(file)), copyOptions);
            return FileVisitResult.CONTINUE;
         }
      });
   }
}
