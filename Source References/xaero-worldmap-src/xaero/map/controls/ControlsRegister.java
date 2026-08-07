package xaero.map.controls;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_304.class_11900;

public class ControlsRegister {
   public static final class_304.class_11900 CATEGORY = class_11900.method_74698(class_2960.method_60655("xaeroworldmap", "controls"));
   public static final class_304 keyOpenMap;
   public static final class_304 keyOpenSettings;
   public static final class_304 keyOpenServerSettings;
   public static final class_304 keyZoomIn;
   public static final class_304 keyZoomOut;
   public static final class_304 keyQuickConfirm;
   public static final class_304 keyToggleDimension;
   public static class_304 keyToggleTrackedPlayers;
   public static class_304 keyTogglePacChunkClaims;
   public final List<class_304> keybindings;

   public ControlsRegister() {
      this.keybindings = Lists.newArrayList(new class_304[]{keyOpenMap, keyOpenSettings, keyOpenServerSettings, keyZoomIn, keyZoomOut, keyQuickConfirm, keyToggleDimension});
   }

   public void register(Consumer<class_304> registry, Consumer<class_304.class_11900> categoryRegistry) {
      categoryRegistry.accept(CATEGORY);

      for(class_304 kb : this.keybindings) {
         registry.accept(kb);
      }

      try {
         Class.forName("xaero.common.IXaeroMinimap");
      } catch (ClassNotFoundException var5) {
         keyToggleTrackedPlayers = new class_304("gui.xaero_toggle_tracked_players", -1, CATEGORY);
         registry.accept(keyToggleTrackedPlayers);
         keyTogglePacChunkClaims = new class_304("gui.xaero_toggle_pac_chunk_claims", -1, CATEGORY);
         registry.accept(keyTogglePacChunkClaims);
      }

   }

   static {
      keyOpenMap = new class_304("gui.xaero_open_map", 77, CATEGORY);
      keyOpenSettings = new class_304("gui.xaero_open_settings", 93, CATEGORY);
      keyOpenServerSettings = new class_304("gui.xaero_world_map_server_settings", -1, CATEGORY);
      keyZoomIn = new class_304("gui.xaero_map_zoom_in", -1, CATEGORY);
      keyZoomOut = new class_304("gui.xaero_map_zoom_out", -1, CATEGORY);
      keyQuickConfirm = new class_304("gui.xaero_quick_confirm", 344, CATEGORY);
      keyToggleDimension = new class_304("gui.xaero_toggle_dimension", -1, CATEGORY);
   }
}
