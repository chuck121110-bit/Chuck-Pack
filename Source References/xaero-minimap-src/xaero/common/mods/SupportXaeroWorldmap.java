package xaero.common.mods;

import com.mojang.blaze3d.textures.GpuTexture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_1937;
import net.minecraft.class_2378;
import net.minecraft.class_2874;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5321;
import org.joml.Matrix4f;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.common.graphics.CustomRenderTypes;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRenderer;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.common.minimap.highlight.HighlighterRegistry;
import xaero.common.minimap.region.MinimapTile;
import xaero.common.minimap.render.MinimapRendererHelper;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.radar.render.element.RadarRenderer;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.render.util.MultiTextureRenderUtil;
import xaero.hud.render.util.RenderBufferUtil;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.graphics.GpuTextureAndView;
import xaero.lib.client.graphics.shader.WorldMapShaderHelper;
import xaero.lib.client.graphics.util.ImmediateRenderUtil;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.config.context.BuiltInEditConfigScreenContexts;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.option.ConfigOption;
import xaero.lib.common.config.single.SingleConfigManager;
import xaero.map.MapProcessor;
import xaero.map.WorldMap;
import xaero.map.WorldMapSession;
import xaero.map.common.config.option.WorldMapProfiledConfigOptions;
import xaero.map.config.primary.option.WorldMapPrimaryClientConfigOptions;
import xaero.map.config.util.WorldMapClientConfigUtils;
import xaero.map.gui.GuiMap;
import xaero.map.gui.GuiWorldMapSettings;
import xaero.map.region.MapRegion;
import xaero.map.region.MapTileChunk;
import xaero.map.region.texture.LeafRegionTexture;
import xaero.map.world.MapDimension;

public class SupportXaeroWorldmap {
   public static int WORLDMAP_COMPATIBILITY_VERSION = 20;
   public static final String MINIMAP_MW = "minimap";
   public int compatibilityVersion;
   private static final HashMap<MapTileChunk, Long> seedsUsed = new HashMap();
   public static final int black = -16777216;
   public static final int slime = -2142047936;
   private IXaeroMinimap modMain;
   private int destinationCaving = Integer.MAX_VALUE;
   private long lastDestinationCavingSwitch;
   private int previousRenderedCaveLayer = Integer.MAX_VALUE;
   private int lastRenderedCaveLayer = Integer.MAX_VALUE;
   private ArrayList<MapRegion> regionBuffer = new ArrayList();

   public SupportXaeroWorldmap(IXaeroMinimap modMain) {
      this.modMain = modMain;

      try {
         this.compatibilityVersion = WorldMap.MINIMAP_COMPATIBILITY_VERSION;
      } catch (NoSuchFieldError var3) {
      }

      if (this.compatibilityVersion < 3) {
         throw new RuntimeException("Xaero's World Map 1.11.0 or newer required!");
      }
   }

   public void drawMinimap(MinimapSession minimapSession, class_4587 matrixStack, MinimapRendererHelper helper, int xFloored, int zFloored, int minViewX, int minViewZ, int maxViewX, int maxViewZ, boolean zooming, double zoom, double mapDimensionScale, class_4588 overlayBufferBuilder, MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers) {
      WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
      if (worldmapSession != null) {
         MapProcessor mapProcessor = worldmapSession.getMapProcessor();
         synchronized(mapProcessor.renderThreadPauseSync) {
            if (!mapProcessor.isRenderingPaused()) {
               if (mapProcessor.getCurrentDimension() == null) {
                  return;
               }

               int compatibilityVersion = this.compatibilityVersion;
               String worldString = mapProcessor.getCurrentWorldId();
               if (worldString == null) {
                  return;
               }

               int mapX = xFloored >> 4;
               int mapZ = zFloored >> 4;
               int chunkX = mapX >> 2;
               int chunkZ = mapZ >> 2;
               int tileX = mapX & 3;
               int tileZ = mapZ & 3;
               int insideX = xFloored & 15;
               int insideZ = zFloored & 15;
               ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               int minX = Math.min(minViewX, (mapX >> 2) - 4);
               int maxX = Math.max(maxViewX, (mapX >> 2) + 4);
               int minZ = Math.min(minViewZ, (mapZ >> 2) - 4);
               int maxZ = Math.max(maxViewZ, (mapZ >> 2) + 4);
               boolean slimeChunks = MinimapConfigClientUtils.getEffectiveSlimeChunks(minimapSession);
               mapProcessor.initMinimapRender(xFloored, zFloored);
               int renderedCaveLayer = mapProcessor.getCurrentCaveLayer();
               float brightness = this.getMinimapBrightness();
               if (renderedCaveLayer != this.lastRenderedCaveLayer) {
                  this.previousRenderedCaveLayer = this.lastRenderedCaveLayer;
               }

               class_1657 player = class_310.method_1551().field_1724;
               boolean noCaveMaps = !MinimapConfigClientUtils.getEffectiveCaveModeAllowed();
               Consumer<GpuTexture> binder = MultiTextureRenderTypeRendererProvider::defaultTextureBind;
               Consumer<GpuTexture> finalizer = null;
               class_1921 mapRenderType = CustomRenderTypes.MINIMAP_WORLD_MAP;
               if (zooming) {
                  mapRenderType = CustomRenderTypes.MINIMAP_WORLD_MAP_ZOOM;
               }

               MultiTextureRenderTypeRenderer mapWithLightRenderer = multiTextureRenderTypeRenderers.getRenderer(binder, finalizer, mapRenderType);
               MultiTextureRenderTypeRenderer mapNoLightRenderer = multiTextureRenderTypeRenderers.getRenderer(binder, finalizer, mapRenderType);
               MinimapWorld world = minimapSession.getWorldManager().getAutoWorld();
               Long seed = slimeChunks && world != null ? MinimapConfigClientUtils.getEffectiveSlimeChunksSeed(world) : null;
               this.renderChunks(matrixStack, minX, maxX, minZ, maxZ, minViewX, maxViewX, minViewZ, maxViewZ, mapProcessor, noCaveMaps, slimeChunks, chunkX, chunkZ, tileX, tileZ, insideX, insideZ, seed, mapWithLightRenderer, mapNoLightRenderer, helper, overlayBufferBuilder);
               WorldMapShaderHelper.setBrightness(brightness);
               WorldMapShaderHelper.setWithLight(true);
               multiTextureRenderTypeRenderers.draw(mapWithLightRenderer);
               WorldMapShaderHelper.setWithLight(false);
               multiTextureRenderTypeRenderers.draw(mapNoLightRenderer);
               this.lastRenderedCaveLayer = renderedCaveLayer;
               mapProcessor.finalizeMinimapRender();
            }

         }
      }
   }

   private void renderChunks(class_4587 matrixStack, int minX, int maxX, int minZ, int maxZ, int minViewX, int maxViewX, int minViewZ, int maxViewZ, MapProcessor mapProcessor, boolean noCaveMaps, boolean slimeChunks, int chunkX, int chunkZ, int tileX, int tileZ, int insideX, int insideZ, Long seed, MultiTextureRenderTypeRenderer mapWithLightRenderer, MultiTextureRenderTypeRenderer mapNoLightRenderer, MinimapRendererHelper helper, class_4588 overlayBufferBuilder) {
      Matrix4f matrix = matrixStack.method_23760().method_23761();

      for(int i = minX; i <= maxX; ++i) {
         for(int j = minZ; j <= maxZ; ++j) {
            MapRegion region = mapProcessor.getMinimapMapRegion(i >> 3, j >> 3);
            mapProcessor.beforeMinimapRegionRender(region);
            if (i >= minViewX && i <= maxViewX && j >= minViewZ && j <= maxViewZ) {
               MapTileChunk chunk = region == null ? null : region.getChunk(i & 7, j & 7);
               boolean chunkIsVisible = chunk != null && chunk.getLeafTexture().getGlColorTexture() != null;
               if (!chunkIsVisible && (!noCaveMaps || this.previousRenderedCaveLayer == Integer.MAX_VALUE)) {
                  MapRegion previousLayerRegion = mapProcessor.getLeafMapRegion(this.previousRenderedCaveLayer, i >> 3, j >> 3, false);
                  if (previousLayerRegion != null) {
                     MapTileChunk previousLayerChunk = previousLayerRegion.getChunk(i & 7, j & 7);
                     if (previousLayerChunk != null && previousLayerChunk.getLeafTexture().getGlColorTexture() != null) {
                        region = previousLayerRegion;
                        chunk = previousLayerChunk;
                        chunkIsVisible = true;
                     }
                  }
               }

               if (chunkIsVisible) {
                  this.bumpLoadedRegion(mapProcessor, region);
                  int drawX = 64 * (chunk.getX() - chunkX) - 16 * tileX - insideX;
                  int drawZ = 64 * (chunk.getZ() - chunkZ) - 16 * tileZ - insideZ;
                  this.prepareMapTexturedRect(matrix, (float)drawX, (float)drawZ, 0, 0, 64.0F, 64.0F, chunk, mapNoLightRenderer, mapWithLightRenderer, helper);
                  if (slimeChunks) {
                     this.renderSlimeChunks(chunk, seed, drawX, drawZ, matrixStack, helper, overlayBufferBuilder);
                  }
               }
            }
         }
      }

   }

   public void bumpLoadedRegion(MapProcessor mapProcessor, MapRegion region) {
      if (!mapProcessor.isUploadingPaused() && region.isLoaded()) {
         mapProcessor.getMapWorld().getCurrentDimension().getLayeredMapRegions().bumpLoadedRegion(region);
      }

   }

   public void renderSlimeChunks(MapTileChunk chunk, Long seed, int drawX, int drawZ, class_4587 matrixStack, MinimapRendererHelper helper, class_4588 overlayBufferBuilder) {
      Long savedSeed = (Long)seedsUsed.get(chunk);
      boolean newSeed = seed == null && savedSeed != null || seed != null && !seed.equals(savedSeed);
      if (newSeed) {
         seedsUsed.put(chunk, seed);
      }

      for(int t = 0; t < 16; ++t) {
         if (newSeed || (chunk.getTileGridsCache()[t % 4][t / 4] & 1) == 0) {
            chunk.getTileGridsCache()[t % 4][t / 4] = (byte)(1 | (MinimapTile.isSlimeChunk(this.modMain.getSettings(), chunk.getX() * 4 + t % 4, chunk.getZ() * 4 + t / 4, seed) ? 2 : 0));
         }

         if ((chunk.getTileGridsCache()[t % 4][t / 4] & 2) != 0) {
            int slimeDrawX = drawX + 16 * (t % 4);
            int slimeDrawZ = drawZ + 16 * (t / 4);
            RenderBufferUtil.addColoredRect(matrixStack.method_23760().method_23761(), overlayBufferBuilder, (float)slimeDrawX, (float)slimeDrawZ, 16, 16, -2142047936);
         }
      }

   }

   public boolean getWorldMapWaypoints() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Boolean)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.WAYPOINTS);
   }

   public int getWorldMapColours() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Integer)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.BLOCK_COLORS);
   }

   public boolean getWorldMapFlowers() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Boolean)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.FLOWERS);
   }

   public boolean getWorldMapTerrainDepth() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Boolean)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.TERRAIN_DEPTH);
   }

   public int getWorldMapTerrainSlopes() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Integer)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.TERRAIN_SLOPES);
   }

   public boolean getWorldMapBiomeColorsVanillaMode() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Boolean)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.BIOME_COLORS_IN_VANILLA);
   }

   public boolean getWorldMapIgnoreHeightmaps() {
      WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
      if (worldmapSession == null) {
         return false;
      } else {
         MapProcessor mapProcessor = worldmapSession.getMapProcessor();
         return mapProcessor.getMapWorld().isIgnoreHeightmaps();
      }
   }

   public String tryToGetMultiworldId(class_5321<class_1937> dimId) {
      WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
      MapProcessor mapProcessor = worldmapSession.getMapProcessor();
      synchronized(mapProcessor.uiPauseSync) {
         return mapProcessor.isUIPaused() ? null : this.getMultiworldIdUnsynced(mapProcessor, dimId);
      }
   }

   private String getMultiworldIdUnsynced(MapProcessor mapProcessor, class_5321<class_1937> dimId) {
      MapDimension mapDim = mapProcessor.isMapWorldUsable() && !mapProcessor.isWaitingForWorldUpdate() ? mapProcessor.getMapWorld().createDimensionUnsynced(dimId) : null;
      return mapDim == null ? null : (!mapDim.currentMultiworldWritable ? "minimap" : mapDim.getCurrentMultiworld());
   }

   public List<String> getPotentialMultiworldIds(class_5321<class_1937> dimId) {
      WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
      MapProcessor mapProcessor = worldmapSession.getMapProcessor();
      synchronized(mapProcessor.uiSync) {
         MapDimension mapDim = mapProcessor.getMapWorld().createDimensionUnsynced(dimId);
         return mapDim != null && (mapProcessor.isWaitingForWorldUpdate() || !mapDim.currentMultiworldWritable) ? mapDim.getMultiworldIdsCopy() : null;
      }
   }

   public List<String> getMultiworldIds(class_5321<class_1937> dimId) {
      WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
      MapProcessor mapProcessor = worldmapSession.getMapProcessor();
      synchronized(mapProcessor.uiSync) {
         MapDimension mapDim = mapProcessor.getMapWorld().createDimensionUnsynced(dimId);
         return mapDim == null ? null : mapDim.getMultiworldIdsCopy();
      }
   }

   public String tryToGetMultiworldName(class_5321<class_1937> dimId, String multiworldId) {
      WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
      MapProcessor mapProcessor = worldmapSession.getMapProcessor();
      synchronized(mapProcessor.uiPauseSync) {
         return mapProcessor.isUIPaused() ? null : this.getMultiworldNameUnsynced(mapProcessor, dimId, multiworldId);
      }
   }

   private String getMultiworldNameUnsynced(MapProcessor mapProcessor, class_5321<class_1937> dimId, String multiworldId) {
      MapDimension mapDim = !mapProcessor.isMapWorldUsable() ? null : mapProcessor.getMapWorld().createDimensionUnsynced(dimId);
      return mapDim == null ? null : mapDim.getMultiworldName(multiworldId);
   }

   public void openSettings() {
      class_437 current = class_310.method_1551().field_1755;
      class_310.method_1551().method_1507(this.getSettingsScreen(current));
   }

   public class_437 getSettingsScreen(class_437 current) {
      class_437 currentEscScreen = current instanceof ScreenBase ? ((ScreenBase)current).escape : null;
      return this.getSettingsScreen(current, currentEscScreen);
   }

   public class_437 getSettingsScreen(class_437 current, class_437 currentEscScreen) {
      return new GuiWorldMapSettings(current, currentEscScreen, BuiltInEditConfigScreenContexts.CLIENT);
   }

   public float getMinimapBrightness() {
      WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
      if (worldmapSession == null) {
         return 1.0F;
      } else {
         MapProcessor mapProcessor = worldmapSession.getMapProcessor();
         boolean lightingConfig = (Boolean)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.LIGHTING);
         return mapProcessor.getBrightness(lightingConfig);
      }
   }

   public void prepareMapTexturedRect(Matrix4f matrix, float x, float y, int textureX, int textureY, float width, float height, MapTileChunk chunk, MultiTextureRenderTypeRenderer noLightRenderer, MultiTextureRenderTypeRenderer withLightrenderer, MinimapRendererHelper helper) {
      LeafRegionTexture leafTexture = chunk.getLeafTexture();
      GpuTextureAndView texture = leafTexture.getGlColorTexture();
      if (texture != null) {
         MultiTextureRenderUtil.prepareTexturedRect(matrix, x, y, textureX, (int)height, width, height, -height, 64.0F, texture.texture, leafTexture.getTextureHasLight() ? withLightrenderer : noLightRenderer);
      }
   }

   public boolean getAdjustHeightForCarpetLikeBlocks() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Boolean)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.ADJUST_HEIGHT_FOR_SHORT_BLOCKS);
   }

   public void registerHighlighters(HighlighterRegistry highlighterRegistry) {
      xaero.map.mods.SupportMods.xaeroMinimap.registerMinimapHighlighters(highlighterRegistry);
   }

   public void createRadarRenderWrapper(RadarRenderer radarRenderer) {
      xaero.map.mods.SupportMods.xaeroMinimap.createRadarRendererWrapper(radarRenderer);
   }

   public boolean worldMapIsRenderingRadar() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Boolean)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.MINIMAP_RADAR);
   }

   public boolean getPartialYTeleport() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Boolean)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.PARTIAL_Y_TELEPORT);
   }

   public boolean isStainedGlassDisplayed() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Boolean)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.STAINED_GLASS);
   }

   public boolean isMultiplayerMap() {
      WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
      if (worldmapSession == null) {
         return false;
      } else {
         MapProcessor mapProcessor = worldmapSession.getMapProcessor();
         return mapProcessor.getMapWorld().isMultiplayer();
      }
   }

   public int getManualCaveStart() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      SingleConfigManager<Config> wmPrimaryConfigManager = wmConfigManager.getPrimaryConfigManager();
      int caveModeStart = (Integer)wmPrimaryConfigManager.getEffective(WorldMapPrimaryClientConfigOptions.CAVE_MODE_START);
      return caveModeStart == Integer.MAX_VALUE ? Integer.MAX_VALUE : caveModeStart;
   }

   public boolean hasEnabledCaveLayers() {
      return this.getCaveModeType() == 1;
   }

   public int getCaveModeType() {
      if (!WorldMapClientConfigUtils.getEffectiveCaveModeAllowed()) {
         return 0;
      } else {
         WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
         ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
         int defaultCaveModeType = (Integer)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.DEFAULT_CAVE_MODE_TYPE);
         if (worldmapSession == null) {
            return defaultCaveModeType;
         } else {
            MapProcessor mapProcessor = worldmapSession.getMapProcessor();
            synchronized(mapProcessor.uiPauseSync) {
               if (mapProcessor.isUIPaused()) {
                  return defaultCaveModeType;
               } else {
                  MapDimension mapDim = mapProcessor.getMapWorld().getCurrentDimension();
                  return mapDim != null ? mapDim.getCaveModeType() : defaultCaveModeType;
               }
            }
         }
      }
   }

   public class_437 getWorldMapScreenForOption(ConfigOption<?> option, class_437 current) {
      if (class_310.method_1551().field_1687 == null) {
         return null;
      } else {
         class_437 currentEscScreen = current instanceof ScreenBase ? ((ScreenBase)current).escape : null;
         if (currentEscScreen instanceof GuiMap) {
            currentEscScreen = null;
         }

         WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
         MapProcessor mapProcessor = worldmapSession.getMapProcessor();
         class_437 screen = new GuiMap(current, currentEscScreen, mapProcessor, class_310.method_1551().method_1560());
         if (option == MinimapProfiledConfigOptions.MANUAL_CAVE_MODE_START) {
            ((GuiMap)screen).enableCaveModeOptions();
         }

         return screen;
      }
   }

   public int getCaveModeDepth() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Integer)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.CAVE_MODE_DEPTH);
   }

   public boolean isLegibleCaveMaps() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Boolean)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.LEGIBLE_CAVE_MAPS);
   }

   public boolean getBiomeBlending() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Boolean)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.BIOME_BLENDING);
   }

   public void confirmPlayerRadarRender(class_1657 e) {
      if (WorldMap.trackedPlayerRenderer.getCollector().playerExists(e.method_5667())) {
         WorldMap.trackedPlayerRenderer.getCollector().confirmPlayerRadarRender(e);
      }

   }

   public boolean getDisplayClaims() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Boolean)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.OPAC_CLAIMS);
   }

   public int getClaimsBorderOpacity() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Integer)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.OPAC_CLAIMS_BORDER_OPACITY);
   }

   public int getClaimsFillOpacity() {
      ClientConfigManager wmConfigManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      return (Integer)wmConfigManager.getEffective(WorldMapProfiledConfigOptions.OPAC_CLAIMS_FILL_OPACITY);
   }

   public void toggleChunkClaims() {
      WorldMapClientConfigUtils.tryTogglingCurrentProfileOption(WorldMapProfiledConfigOptions.OPAC_CLAIMS);
   }

   public boolean caveLayersAreUsable() {
      boolean result = this.hasEnabledCaveLayers();
      if (result) {
         WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
         if (worldmapSession == null) {
            return result;
         }

         class_1297 player = class_310.method_1551().method_1560();
         if (player == null) {
            return result;
         }

         MapProcessor mapProcessor = worldmapSession.getMapProcessor();
         MapDimension mapDimension = mapProcessor.getMapWorld().getCurrentDimension();
         if (mapDimension == null) {
            return result;
         }

         if (mapDimension.getDimId() != player.method_73183().method_27983()) {
            return false;
         }
      }

      return result;
   }

   public boolean shouldPreventAutoCaveMode(class_1937 world) {
      WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
      if (worldmapSession == null) {
         return false;
      } else {
         MapProcessor mapProcessor = worldmapSession.getMapProcessor();
         MapDimension mapDimension = mapProcessor.getMapWorld().getCurrentDimension();
         if (mapDimension == null) {
            return false;
         } else {
            return mapDimension.getDimId() != world.method_27983();
         }
      }
   }

   public double getMapDimensionScale() {
      WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
      if (worldmapSession == null) {
         return class_310.method_1551().field_1687.method_8597().comp_646();
      } else {
         MapProcessor mapProcessor = worldmapSession.getMapProcessor();
         synchronized(mapProcessor.renderThreadPauseSync) {
            if (mapProcessor.isRenderingPaused()) {
               return (double)0.0F;
            } else {
               class_2378<class_2874> dimTypes = mapProcessor.getWorldDimensionTypeRegistry();
               return dimTypes == null ? (double)0.0F : mapProcessor.getMapWorld().getCurrentDimension().calculateDimScale(dimTypes);
            }
         }
      }
   }

   public class_5321<class_1937> getMapDimension() {
      WorldMapSession worldmapSession = WorldMapSession.getCurrentSession();
      if (worldmapSession == null) {
         return class_310.method_1551().field_1687.method_27983();
      } else {
         MapProcessor mapProcessor = worldmapSession.getMapProcessor();
         MapDimension mapDimension = mapProcessor.getMapWorld().getCurrentDimension();
         return mapDimension == null ? class_310.method_1551().field_1687.method_27983() : mapDimension.getDimId();
      }
   }
}
