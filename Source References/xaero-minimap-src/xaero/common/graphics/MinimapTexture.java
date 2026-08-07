package xaero.common.graphics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.OptionalDouble;
import net.minecraft.class_1049;
import net.minecraft.class_12247;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3300;
import xaero.lib.client.graphics.GpuTextureAndView;
import xaero.lib.client.graphics.util.TextureUtils;

public class MinimapTexture extends class_1049 {
   public final ByteBuffer buffer;
   public final IntBuffer intBuffer;
   boolean loaded;
   private GpuTextureAndView textureAndView;
   private class_12247.class_12337 textureAndSampler;

   public void loadIfNeeded() throws IOException {
      if (!this.loaded) {
         this.load(class_310.method_1551().method_1478());
         this.loaded = true;
      }

   }

   public MinimapTexture(class_2960 location) throws IOException {
      super(location);
      this.buffer = TextureUtils.allocateByteBuffer(1048576, ByteOrder.LITTLE_ENDIAN);
      this.intBuffer = this.buffer.asIntBuffer();
      this.loaded = false;
   }

   public void load(class_3300 resourceManager_1) throws IOException {
      this.field_56974 = RenderSystem.getDevice().createTexture("minimap_safe_mode", 5, TextureFormat.RGBA8, 512, 512, 1, 1);
      this.field_60597 = RenderSystem.getDevice().createTextureView(this.field_56974);
      this.textureAndView = new GpuTextureAndView(this.field_56974, this.field_60597);
      this.textureAndSampler = new class_12247.class_12337(this.field_60597, RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.of((double)1.0F)));
   }

   public GpuTextureAndView getTextureAndView() {
      return this.textureAndView;
   }

   public class_12247.class_12337 getTas() {
      return this.textureAndSampler;
   }
}
