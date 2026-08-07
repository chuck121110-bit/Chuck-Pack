package xaero.common.graphics;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.minecraft.class_12137;
import net.minecraft.class_12245;
import net.minecraft.class_12246;
import net.minecraft.class_12247;
import net.minecraft.class_12250;
import net.minecraft.class_1921;
import xaero.hud.minimap.radar.icon.creator.render.form.model.RadarIconModelPrerenderer;
import xaero.hud.render.TextureLocations;
import xaero.lib.XaeroLib;
import xaero.lib.client.graphics.ITextureBinding;
import xaero.lib.client.graphics.XaeroBufferProvider;
import xaero.lib.client.graphics.XaeroRenderType;

public class CustomRenderTypes {
   public static final class_1921 GUI_NEAREST;
   public static final class_1921 GUI_NEAREST_PRE;
   public static final class_1921 GUI_BILINEAR;
   public static final class_1921 GUI_BILINEAR_PRE;
   public static final class_1921 GUI_BILINEAR_NO_DEPTH;
   public static final class_1921 COLORED_WAYPOINTS_BGS;
   public static final class_1921 MAP_CHUNK_OVERLAY;
   public static final class_1921 MAP_LINES;
   public static final class_1921 MINIMAP_WORLD_MAP;
   public static final class_1921 MINIMAP_WORLD_MAP_ZOOM;
   public static final class_1921 MINIMAP;
   public static final class_1921 MINIMAP_ZOOM;
   public static final class_1921 RADAR_NAME_BGS;
   public static final class_1921 DEPTH_CLEAR;

   public static class_1921 entityIconRenderType(Map<String, ITextureBinding> textures, class_12250 textureTransform, boolean useOverlay, boolean affectsCrumbling, boolean sortOnUpload, class_12245 layeringTransform, RenderPipeline renderPipeline) {
      class_12247.class_12248 builder = class_12247.method_75927(renderPipeline).method_75929(1536);
      textures.forEach((name, binding) -> builder.method_76560(name, binding.xaero_lib_getLocation(), binding.xaero_lib_getSampler()));
      builder.method_76560("Sampler2", RadarIconModelPrerenderer.LIGHTMAP, () -> RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
      builder.method_75933(textureTransform);
      if (useOverlay) {
         builder.method_75935();
      }

      if (affectsCrumbling) {
         builder.method_75936();
      }

      if (sortOnUpload) {
         builder.method_75937();
      }

      builder.method_75930(layeringTransform);
      return XaeroRenderType.createRenderType("xaero_entity_icon", builder.method_75929(1536).method_75931(class_12246.field_63980));
   }

   public static void applyFixedOrder() {
      XaeroBufferProvider bufferProvider = XaeroLib.INSTANCE.getClient().getBufferProvider();
      bufferProvider.addToFixedOrder(GUI_NEAREST);
      bufferProvider.addToFixedOrder(GUI_BILINEAR);
      bufferProvider.addToFixedOrder(GUI_BILINEAR_PRE);
      bufferProvider.addToFixedOrder(GUI_BILINEAR_NO_DEPTH);
      bufferProvider.addToFixedOrder(COLORED_WAYPOINTS_BGS);
      bufferProvider.addToFixedOrder(MAP_CHUNK_OVERLAY);
      bufferProvider.addToFixedOrder(MAP_LINES);
      bufferProvider.addToFixedOrder(RADAR_NAME_BGS);
   }

   static {
      Supplier<class_12137> NEAREST_NO_MIPMAPS = () -> RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, 1, OptionalDouble.empty());
      Supplier<class_12137> NEAREST_MIPMAPS = () -> RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, 1, OptionalDouble.of((double)4.0F));
      Supplier<class_12137> BILINEAR_NO_MIPMAPS = () -> RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.of((double)1.0F));
      Supplier<class_12137> BILINEAR_MIPMAPS = () -> RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.of((double)5.0F));
      Supplier<class_12137> MIN_LINEAR_MAG_NEAREST = () -> RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, 1, OptionalDouble.of((double)1.0F));
      GUI_NEAREST = XaeroRenderType.createRenderType("xaero_gui_nearest", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TEX_TRANSLUCENT).method_75929(786432).method_76560("Sampler0", TextureLocations.GUI_TEXTURES, NEAREST_NO_MIPMAPS).method_75931(class_12246.field_63980));
      GUI_NEAREST_PRE = XaeroRenderType.createRenderType("xaero_gui_nearest", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TEX_PREMULTIPLIED).method_75929(786432).method_76560("Sampler0", TextureLocations.GUI_TEXTURES, NEAREST_NO_MIPMAPS).method_75931(class_12246.field_63980));
      GUI_BILINEAR = XaeroRenderType.createRenderType("xaero_gui_bilinear", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TEX_TRANSLUCENT).method_75929(786432).method_76560("Sampler0", TextureLocations.GUI_TEXTURES, BILINEAR_MIPMAPS).method_75931(class_12246.field_63980));
      GUI_BILINEAR_NO_DEPTH = XaeroRenderType.createRenderType("xaero_gui_no_depth", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TEX_TRANSLUCENT_NO_DEPTH).method_75929(786432).method_76560("Sampler0", TextureLocations.GUI_TEXTURES, BILINEAR_MIPMAPS).method_75931(class_12246.field_63980));
      GUI_BILINEAR_PRE = XaeroRenderType.createRenderType("xaero_gui_bilinear_pre", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TEX_PREMULTIPLIED).method_75929(786432).method_76560("Sampler0", TextureLocations.GUI_TEXTURES, BILINEAR_MIPMAPS).method_75931(class_12246.field_63980));
      COLORED_WAYPOINTS_BGS = XaeroRenderType.createRenderType("xaero_colored_waypoints", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TRANSLUCENT).method_75929(786432).method_75930(class_12245.field_63977).method_75931(class_12246.field_63980));
      RADAR_NAME_BGS = XaeroRenderType.createRenderType("xaero_radar_name_bg", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TRANSLUCENT).method_75929(786432).method_75930(class_12245.field_63977).method_75931(class_12246.field_63980));
      MAP_CHUNK_OVERLAY = XaeroRenderType.createRenderType("xaero_chunk_overlay", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TRANSLUCENT).method_75929(786432).method_75931(class_12246.field_63980));
      MAP_LINES = XaeroRenderType.createRenderType("xaero_lines", class_12247.method_75927(XaeroRenderType.RP_LINES).method_75929(1536).method_75931(class_12246.field_63980));
      DEPTH_CLEAR = XaeroRenderType.createRenderType("xaero_depth_clear", class_12247.method_75927(XaeroRenderType.RP_DEPTH_CLEAR).method_75929(1536).method_75931(class_12246.field_63980));
      MINIMAP_WORLD_MAP = XaeroRenderType.createRenderType("xaero_minimap_worldmap", class_12247.method_75927(XaeroRenderType.RP_MINIMAP_MAP).method_75929(1536).method_76560("Sampler0", TextureLocations.GUI_TEXTURES, MIN_LINEAR_MAG_NEAREST).method_75931(class_12246.field_63980));
      MINIMAP_WORLD_MAP_ZOOM = XaeroRenderType.createRenderType("xaero_minimap_worldmap_zoom", class_12247.method_75927(XaeroRenderType.RP_MINIMAP_MAP).method_75929(1536).method_76560("Sampler0", TextureLocations.GUI_TEXTURES, BILINEAR_MIPMAPS).method_75931(class_12246.field_63980));
      MINIMAP = XaeroRenderType.createRenderType("xaero_minimap", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TEX_PREMULTIPLIED).method_75929(1536).method_76560("Sampler0", TextureLocations.GUI_TEXTURES, MIN_LINEAR_MAG_NEAREST).method_75931(class_12246.field_63980));
      MINIMAP_ZOOM = XaeroRenderType.createRenderType("xaero_minimap_zoom", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TEX_PREMULTIPLIED).method_75929(1536).method_76560("Sampler0", TextureLocations.GUI_TEXTURES, BILINEAR_MIPMAPS).method_75931(class_12246.field_63980));
   }
}
