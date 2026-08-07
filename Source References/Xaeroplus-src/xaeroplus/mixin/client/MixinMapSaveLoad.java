package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipOutputStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.WorldMap;
import xaero.map.file.MapSaveLoad;
import xaeroplus.Globals;
import xaeroplus.settings.Settings;

@Mixin(
   value = {MapSaveLoad.class},
   remap = false
)
public abstract class MixinMapSaveLoad {
   @Inject(
      method = {"getOldFolder"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getOldFolder(final String oldUnfixedMainId, final String dim, final CallbackInfoReturnable<Path> cir) {
      if (!Globals.nullOverworldDimensionFolder) {
         if (oldUnfixedMainId == null) {
            cir.setReturnValue((Object)null);
         }

         String dimIdFixed = Objects.equals(dim, "null") ? "0" : dim;
         cir.setReturnValue(WorldMap.saveFolder.toPath().resolve(oldUnfixedMainId + "_" + dimIdFixed));
      }

   }

   @Redirect(
      method = {"saveRegion"},
      at = @At(
   value = "NEW",
   args = {"class=Ljava/io/DataOutputStream;"}
)
   )
   public DataOutputStream replaceSaveRegionZipOutputStream(final OutputStream out, @Local(name = {"zipOut"}) final ZipOutputStream zipOut, @Share("zipOutShare") final LocalRef<ZipOutputStream> zipOutShare) {
      if (!Settings.REGISTRY.fastZipWrite.get()) {
         return new DataOutputStream(out);
      } else {
         Globals.zipFastByteBuffer.reset();
         zipOutShare.set(zipOut);
         return new DataOutputStream(Globals.zipFastByteBuffer);
      }
   }

   @Inject(
      method = {"saveRegion"},
      at = {@At(
   value = "INVOKE",
   target = "Ljava/util/zip/ZipOutputStream;closeEntry()V"
)}
   )
   public void saveRegionWriteZipOutputStream(final CallbackInfoReturnable<Boolean> cir, @Local(name = {"zipOut"}) final ZipOutputStream zipOut) throws IOException {
      if (Settings.REGISTRY.fastZipWrite.get()) {
         Globals.zipFastByteBuffer.writeTo(zipOut);
         Globals.zipFastByteBuffer.reset();
      }
   }

   @Inject(
      method = {"saveRegion"},
      at = {@At(
   value = "INVOKE",
   target = "Ljava/io/DataOutputStream;close()V"
)}
   )
   public void closeZipOutputStream(final CallbackInfoReturnable<Boolean> cir, @Share("zipOutShare") final LocalRef<ZipOutputStream> zipOutShare) throws IOException {
      if (Settings.REGISTRY.fastZipWrite.get()) {
         try {
            ((ZipOutputStream)zipOutShare.get()).close();
         } catch (IOException e) {
            throw e;
         } catch (Exception e) {
            throw new IOException(e);
         }
      }
   }
}
