package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.zip.ZipOutputStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.region.LeveledRegion;
import xaero.map.region.texture.RegionTexture;
import xaeroplus.Globals;
import xaeroplus.settings.Settings;

@Mixin(
   value = {LeveledRegion.class},
   remap = false
)
public abstract class MixinLeveledRegion<T extends RegionTexture<T>> {
   @Redirect(
      method = {"saveCacheTextures"},
      at = @At(
   value = "NEW",
   args = {"class=Ljava/io/DataOutputStream;"}
)
   )
   public DataOutputStream replaceSaveCacheTexturesZipOutputStream(final OutputStream out) {
      if (!Settings.REGISTRY.fastZipWrite.get()) {
         return new DataOutputStream(out);
      } else {
         Globals.zipFastByteBuffer.reset();
         return new DataOutputStream(Globals.zipFastByteBuffer);
      }
   }

   @Inject(
      method = {"saveCacheTextures"},
      at = {@At(
   value = "INVOKE",
   target = "Ljava/util/zip/ZipOutputStream;closeEntry()V"
)}
   )
   public void writeSaveCacheTexturesZipOutputStream(final File tempFile, final boolean debugConfig, final int extraAttempts, final CallbackInfoReturnable<Boolean> cir, @Local(name = {"zipOutput"}) ZipOutputStream zipOutputRef) {
      if (Settings.REGISTRY.fastZipWrite.get()) {
         try {
            Globals.zipFastByteBuffer.writeTo(zipOutputRef);
            Globals.zipFastByteBuffer.reset();
         } catch (IOException e) {
            throw new UncheckedIOException(e);
         }
      }
   }
}
