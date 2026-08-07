package xaero.map.graphics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.minecraft.class_12137;
import net.minecraft.class_12246;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import xaero.lib.XaeroLib;
import xaero.lib.client.graphics.XaeroBufferProvider;
import xaero.lib.client.graphics.XaeroRenderType;
import xaero.map.WorldMap;
import xaero.map.region.texture.ExportLeafRegionTexture;

public class CustomRenderTypes {
   public static final class_1921 GUI_NEAREST;
   public static final class_1921 GUI_BILINEAR;
   public static final class_1921 GUI_BILINEAR_PRE;
   public static final class_1921 MAP_BILINEAR;
   public static final class_1921 MAP;
   public static final class_1921 MAP_EXPORT;
   public static final class_1921 MAP_BRANCH;
   public static final class_1921 MAP_COLOR_OVERLAY;
   public static final class_1921 MAP_FRAME;
   public static final class_1921 MAP_COLOR_FILLER;
   public static final class_1921 MAP_ELEMENT_TEXT_BG;

   public static void applyFixedOrder() {
      XaeroBufferProvider bufferProvider = XaeroLib.INSTANCE.getClient().getBufferProvider();
      bufferProvider.addToFixedOrder(MAP_COLOR_FILLER);
      bufferProvider.addToFixedOrder(MAP_FRAME);
      bufferProvider.addToFixedOrder(GUI_NEAREST);
      bufferProvider.addToFixedOrder(GUI_BILINEAR);
      bufferProvider.addToFixedOrder(GUI_BILINEAR_PRE);
      bufferProvider.addToFixedOrder(MAP_COLOR_OVERLAY);
      bufferProvider.addToFixedOrder(MAP);
      bufferProvider.addToFixedOrder(MAP_BILINEAR);
      bufferProvider.addToFixedOrder(MAP_ELEMENT_TEXT_BG);
      bufferProvider.addToFixedOrder(MAP_BRANCH);
   }

   static {
      Supplier<class_12137> NEAREST_NO_MIPMAPS = () -> RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, 1, OptionalDouble.empty());
      Supplier<class_12137> NEAREST_MIPMAPS = () -> RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, 1, OptionalDouble.of((double)4.0F));
      Supplier<class_12137> BILINEAR_NO_MIPMAPS = () -> RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.of((double)1.0F));
      Supplier<class_12137> BILINEAR_MIPMAPS = () -> RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, 1, OptionalDouble.of((double)5.0F));
      Supplier<class_12137> MAP_SAMPLER = () -> RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, 1, OptionalDouble.of((double)1.0F));
      Supplier<class_12137> MAP_EXPORT_SAMPLER = () -> RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, 1, OptionalDouble.of((double)ExportLeafRegionTexture.MIP_MAP_LEVELS));
      GUI_NEAREST = XaeroRenderType.createRenderType("xaero_wm_gui_nearest", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TEX_TRANSLUCENT).method_75929(786432).method_76560("Sampler0", WorldMap.guiTextures, NEAREST_NO_MIPMAPS).method_75931(class_12246.field_63980));
      GUI_BILINEAR = XaeroRenderType.createRenderType("xaero_wm_gui_bilinear", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TEX_TRANSLUCENT).method_75929(786432).method_76560("Sampler0", WorldMap.guiTextures, BILINEAR_MIPMAPS).method_75931(class_12246.field_63980));
      GUI_BILINEAR_PRE = XaeroRenderType.createRenderType("xaero_wm_gui_bilinear_pre", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TEX_PREMULTIPLIED).method_75929(786432).method_76560("Sampler0", WorldMap.guiTextures, BILINEAR_MIPMAPS).method_75931(class_12246.field_63980));
      MAP = XaeroRenderType.createRenderType("xaero_wm_map_with_light", class_12247.method_75927(XaeroRenderType.RP_MAP).method_75929(786432).method_76560("Sampler0", WorldMap.guiTextures, MAP_SAMPLER).method_75931(class_12246.field_63980));
      MAP_BILINEAR = XaeroRenderType.createRenderType("xaero_wm_map_with_light_bilinear", class_12247.method_75927(XaeroRenderType.RP_MAP).method_75929(786432).method_76560("Sampler0", WorldMap.guiTextures, BILINEAR_NO_MIPMAPS).method_75931(class_12246.field_63980));
      MAP_EXPORT = XaeroRenderType.createRenderType("xaero_wm_map_export_with_light", class_12247.method_75927(XaeroRenderType.RP_MAP).method_75929(786432).method_76560("Sampler0", WorldMap.guiTextures, MAP_EXPORT_SAMPLER).method_75931(class_12246.field_63980));
      MAP_BRANCH = XaeroRenderType.createRenderType("xaero_wm_map_branch", class_12247.method_75927(XaeroRenderType.RP_MAP_BRANCH).method_75929(1536).method_76560("Sampler0", WorldMap.guiTextures, MAP_SAMPLER).method_75931(class_12246.field_63980));
      MAP_COLOR_OVERLAY = XaeroRenderType.createRenderType("xaero_wm_world_map_overlay", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_NO_CULL).method_75929(786432).method_75931(class_12246.field_63980));
      MAP_FRAME = XaeroRenderType.createRenderType("xaero_wm_frame_texture", class_12247.method_75927(XaeroRenderType.RP_MAP_FRAME).method_75929(1536).method_76560("Sampler0", WorldMap.guiTextures, BILINEAR_NO_MIPMAPS).method_75931(class_12246.field_63980));
      MAP_COLOR_FILLER = XaeroRenderType.createRenderType("xaero_wm_world_map_filler", class_12247.method_75927(XaeroRenderType.RP_COLOR_FILLER).method_75929(1536).method_75931(class_12246.field_63980));
      MAP_ELEMENT_TEXT_BG = XaeroRenderType.createRenderType("xaero_wm_world_map_waypoint_name_bg", class_12247.method_75927(XaeroRenderType.RP_POSITION_COLOR_TRANSLUCENT).method_75929(786432).method_75931(class_12246.field_63980));
   }
}
