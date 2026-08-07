package xaero.common.mods;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.common.mods.pac.SupportOpenPartiesAndClaims;
import xaero.hud.compat.mods.SupportWaystones;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.module.MinimapSession;
import xaero.lib.platform.Services;

public abstract class SupportMods {
   public SupportXaeroWorldmap worldmapSupport = null;
   public SupportOpenPartiesAndClaims xaeroPac;
   private IXaeroMinimap modMain;
   public boolean optifine;
   public boolean vivecraft;
   public boolean iris;
   public boolean ftbTeams;
   public SupportIris supportIris;
   public SupportFramedBlocks supportFramedBlocks;
   private SupportWaystones supportWaystones;
   private boolean checkedLater;

   private void ensureCheckedLater() {
      if (!this.checkedLater) {
         throw new IllegalStateException();
      }
   }

   public boolean worldmap() {
      return this.worldmapSupport != null;
   }

   public boolean pac() {
      this.ensureCheckedLater();
      return this.xaeroPac != null;
   }

   public boolean shouldUseWorldMapChunks() {
      return this.worldmap() && (Boolean)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.DISPLAY_WORLD_MAP_CHUNKS);
   }

   public boolean shouldUseWorldMapCaveChunks() {
      return this.shouldUseWorldMapChunks() && this.worldmapSupport.caveLayersAreUsable();
   }

   public boolean framedBlocks() {
      return this.supportFramedBlocks != null;
   }

   public boolean waystones() {
      return this.supportWaystones != null;
   }

   public SupportWaystones getSupportWaystones() {
      return this.supportWaystones;
   }

   public static void checkForMinimapDuplicates(String otherModMainClass) {
      try {
         Class.forName(otherModMainClass);
         throw new RuntimeException("Better PVP contains Xaero's Minimap by default. Do not install Better PVP and Xaero's Minimap together!");
      } catch (ClassNotFoundException var2) {
      }
   }

   public SupportMods(IXaeroMinimap modMain) {
      this.modMain = modMain;

      try {
         Class wmClassTest = Class.forName("xaero.map.WorldMap");
         this.worldmapSupport = new SupportXaeroWorldmap(modMain);
         MinimapLogs.LOGGER.info("Xaero's Minimap: World Map found!");
      } catch (ClassNotFoundException var14) {
      }

      try {
         Class optifineClassTest = Class.forName("optifine.Patcher");
         this.optifine = true;
         MinimapLogs.LOGGER.info("Optifine!");
      } catch (ClassNotFoundException var13) {
         this.optifine = false;
         MinimapLogs.LOGGER.info("No Optifine!");
      }

      try {
         Class vivecraftClassTest = Class.forName("org.vivecraft.api.VRData");
         this.vivecraft = true;

         try {
            Class<?> vrStateClass = Class.forName("org.vivecraft.VRState");
            Method checkVRMethod = vrStateClass.getDeclaredMethod("checkVR");
            this.vivecraft = (Boolean)checkVRMethod.invoke((Object)null);
         } catch (ClassNotFoundException var7) {
         } catch (NoSuchMethodException var8) {
         } catch (IllegalAccessException var9) {
         } catch (IllegalArgumentException var10) {
         } catch (InvocationTargetException var11) {
         }
      } catch (ClassNotFoundException var12) {
      }

      if (this.vivecraft) {
         MinimapLogs.LOGGER.info("Xaero's Minimap: Vivecraft!");
      } else {
         MinimapLogs.LOGGER.info("Xaero's Minimap: No Vivecraft!");
      }

      try {
         Class mmClassTest = Class.forName("xfacthd.framedblocks.FramedBlocks");
         this.supportFramedBlocks = new SupportFramedBlocks();
         MinimapLogs.LOGGER.info("Xaero's Minimap: Framed Blocks found!");
      } catch (ClassNotFoundException var6) {
      }

      try {
         Class.forName("net.irisshaders.iris.api.v0.IrisApi");
         this.supportIris = new SupportIris();
         this.iris = true;
         MinimapLogs.LOGGER.info("Xaero's Minimap: Iris found!");
      } catch (Exception var5) {
      }

      if (Services.PLATFORM.isModLoaded("waystones")) {
         this.supportWaystones = new SupportWaystones();
         MinimapLogs.LOGGER.info("Xaero's Minimap: Waystones found!");
      }

   }

   public void checkLater() {
      this.checkedLater = true;

      try {
         Class pacClassTest = Class.forName("xaero.pac.OpenPartiesAndClaims");
         this.xaeroPac = new SupportOpenPartiesAndClaims(this.modMain);
         this.xaeroPac.register();
         MinimapLogs.LOGGER.info("Xaero's Minimap: Open Parties And Claims found!");
      } catch (ClassNotFoundException var2) {
      }

   }

   public void registerClientEvents() {
      if (this.supportWaystones != null) {
         this.supportWaystones.registerClientEvents();
      }

   }

   public void onClientTick() {
   }

   public void onMinimapSessionStarted(MinimapSession minimapSession) {
   }
}
