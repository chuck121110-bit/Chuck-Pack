package xaero.hud.minimap.radar.category;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.radar.category.serialization.EntityRadarCategorySerializationHandler;
import xaero.lib.common.util.IOUtils;

public final class EntityRadarCategoryFileIO {
   private final Path saveLocationPath;
   private final EntityRadarCategorySerializationHandler serializationHandler;

   private EntityRadarCategoryFileIO(@Nonnull Path saveLocationPath, @Nonnull EntityRadarCategorySerializationHandler serializationHandler) {
      this.saveLocationPath = saveLocationPath;
      this.serializationHandler = serializationHandler;
   }

   public void saveRootCategory(EntityRadarCategory category) {
      Path saveLocationTempPath = this.saveLocationPath.resolveSibling(this.saveLocationPath.getFileName().toString() + ".temp");
      String serializedData = this.serializationHandler.serialize(category);
      this.saveRootCategory(saveLocationTempPath, serializedData, 10);
   }

   public void saveRootCategory(Path saveLocationTempPath, String serializedData, int attempts) {
      try {
         FileOutputStream fileOutput = new FileOutputStream(saveLocationTempPath.toFile());

         try {
            BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput);

            try {
               OutputStreamWriter writer = new OutputStreamWriter(bufferedOutput, StandardCharsets.UTF_8);

               try {
                  writer.write(serializedData);
                  writer.close();
                  IOUtils.safeMoveAndReplace(saveLocationTempPath, this.saveLocationPath, true);
               } catch (Throwable var13) {
                  try {
                     writer.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }

                  throw var13;
               }

               writer.close();
            } catch (Throwable var14) {
               try {
                  bufferedOutput.close();
               } catch (Throwable var11) {
                  var14.addSuppressed(var11);
               }

               throw var14;
            }

            bufferedOutput.close();
         } catch (Throwable var15) {
            try {
               fileOutput.close();
            } catch (Throwable var10) {
               var15.addSuppressed(var10);
            }

            throw var15;
         }

         fileOutput.close();
      } catch (IOException e) {
         if (attempts <= 1) {
            MinimapLogs.LOGGER.error("suppressed exception", e);
            return;
         }

         MinimapLogs.LOGGER.info("Failed to save entity radar categories. Retrying... " + attempts);

         try {
            Thread.sleep(100L);
         } catch (InterruptedException var9) {
         }

         --attempts;
         this.saveRootCategory(saveLocationTempPath, serializedData, attempts);
      }

   }

   public EntityRadarCategory loadRootCategory() throws IOException {
      FileInputStream fileInput = new FileInputStream(this.saveLocationPath.toFile());

      String serializedData;
      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader(fileInput, "UTF8"));

         try {
            StringBuilder stringBuilder = new StringBuilder();
            reader.lines().forEach((line) -> stringBuilder.append(line).append('\n'));
            serializedData = stringBuilder.toString();
         } catch (Throwable var9) {
            try {
               reader.close();
            } catch (Throwable var7) {
               var9.addSuppressed(var7);
            }

            throw var9;
         }

         reader.close();
      } catch (Throwable var10) {
         try {
            fileInput.close();
         } catch (Throwable var6) {
            var10.addSuppressed(var6);
         }

         throw var10;
      }

      fileInput.close();

      try {
         return (EntityRadarCategory)this.serializationHandler.deserialize(serializedData);
      } catch (Throwable t) {
         MinimapLogs.LOGGER.error("A minimap entity radar config file is not usable (is likely corrupt)! Resolving...");
         Path backupPath = IOUtils.quickFileBackupMove(this.saveLocationPath);
         MinimapLogs.LOGGER.error(String.format("The broken file was backed up to %s and ignored.", backupPath), t);
         return null;
      }
   }

   public static final class Builder {
      private Path saveLocationPath;
      private final EntityRadarCategorySerializationHandler.Builder serializationHandlerBuilder;

      private Builder(EntityRadarCategorySerializationHandler.Builder serializationHandlerBuilder) {
         this.serializationHandlerBuilder = serializationHandlerBuilder;
      }

      private Builder setDefault() {
         this.saveLocationPath = null;
         return this;
      }

      public Builder setSaveLocationPath(Path saveLocationPath) {
         this.saveLocationPath = saveLocationPath;
         return this;
      }

      public EntityRadarCategoryFileIO build() {
         if (this.saveLocationPath != null && this.serializationHandlerBuilder != null) {
            return new EntityRadarCategoryFileIO(this.saveLocationPath, (EntityRadarCategorySerializationHandler)this.serializationHandlerBuilder.build());
         } else {
            throw new IllegalStateException("required fields not set!");
         }
      }

      public static Builder begin(EntityRadarCategorySerializationHandler.Builder serializationHandlerBuilder) {
         return (new Builder(serializationHandlerBuilder)).setDefault();
      }
   }
}
