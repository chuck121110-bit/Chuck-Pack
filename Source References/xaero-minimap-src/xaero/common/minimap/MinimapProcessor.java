package xaero.common.minimap;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_10366;
import net.minecraft.class_11278;
import net.minecraft.class_1297;
import net.minecraft.class_151;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import net.minecraft.class_7923;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.common.anim.MultiplyAnimationHelper;
import xaero.common.core.IGuiRenderer;
import xaero.common.minimap.mcworld.MinimapClientWorldData;
import xaero.common.minimap.mcworld.MinimapClientWorldDataHelper;
import xaero.common.minimap.render.MinimapRenderer;
import xaero.common.minimap.render.MinimapRendererHelper;
import xaero.common.minimap.write.MinimapWriter;
import xaero.common.misc.Misc;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.player.tracker.synced.ClientSyncedTrackedPlayerManager;
import xaero.hud.minimap.radar.RadarSession;
import xaero.hud.minimap.radar.category.EntityRadarCategoryManager;
import xaero.hud.minimap.render.MinimapPipRenderState;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.graphics.IGameRenderer;
import xaero.lib.client.graphics.XaeroBufferProvider;

public class MinimapProcessor {
   public static final boolean DEBUG = false;
   public static final int FRAME = 9;
   private IXaeroMinimap modMain;
   private MinimapSession minimapSession;
   private MinimapWriter minimapWriter;
   private RadarSession radarSession;
   private Minimap minimap;
   private EntityRadarCategoryManager entityCategoryManager;
   private ClientSyncedTrackedPlayerManager syncedTrackedPlayerManager;
   private double minimapZoom;
   private boolean toResetImage;
   private boolean enlargedMap;
   private boolean manualCaveMode;
   private boolean noMinimapMessageReceived;
   private boolean fairPlayOnlyMessageReceived;
   private boolean consideringNetherFairPlayMessage;
   private double lastMapDimensionScale = (double)1.0F;
   private class_5321<class_1937> lastMapDimension;
   private double lastPlayerDimDiv = (double)1.0F;
   private class_1792 minimapItem;
   private final MinimapPipRenderState renderState;
   private final MinimapPipRenderState.Enlarged enlargedRenderState;

   public MinimapProcessor(IXaeroMinimap modMain, MinimapSession minimapSession, MinimapWriter minimapWriter, RadarSession radarSession, ClientSyncedTrackedPlayerManager syncedTrackedPlayerManager) {
      this.modMain = modMain;
      this.minimapSession = minimapSession;
      this.minimapWriter = minimapWriter;
      this.radarSession = radarSession;
      this.minimapZoom = (double)1.0F;
      this.toResetImage = true;
      this.minimap = modMain.getMinimap();
      this.syncedTrackedPlayerManager = syncedTrackedPlayerManager;
      this.updateMinimapItem();
      this.renderState = new MinimapPipRenderState();
      this.enlargedRenderState = new MinimapPipRenderState.Enlarged();
   }

   public int getMinimapSize() {
      int minimapSizeConfig = MinimapConfigClientUtils.getEffectiveMinimapSize();
      return this.enlargedMap ? 500 : minimapSizeConfig * 2;
   }

   public int getMinimapBufferSize(int minimapSize) {
      int bufferSize = 128 * (int)Math.pow((double)2.0F, Math.ceil(Math.log((double)minimapSize / (double)128.0F) / Math.log((double)2.0F)));
      if (bufferSize < 128) {
         return 128;
      } else {
         return bufferSize > 512 ? 512 : bufferSize;
      }
   }

   public boolean isEnlargedMap() {
      return this.enlargedMap;
   }

   public void setEnlargedMap(boolean enlargedMap) {
      this.enlargedMap = enlargedMap;
   }

   public double getMinimapZoom() {
      return this.minimapZoom;
   }

   public boolean isCaveModeDisplayed() {
      return this.minimapWriter.getLoadedCaving() != Integer.MAX_VALUE;
   }

   public double getTargetZoom() {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      double target = MinimapConfigClientUtils.getActualZoom(MinimapProfiledConfigOptions.ZOOM);
      if (this.enlargedMap) {
         double enlargedZoomConfig = MinimapConfigClientUtils.getActualZoom(MinimapProfiledConfigOptions.ZOOM_ENLARGED);
         if (enlargedZoomConfig > (double)0.0F) {
            target = enlargedZoomConfig;
         }
      }

      if (this.isCaveModeDisplayed()) {
         target *= (double)Math.round(target * (double)(Integer)configManager.getEffective(MinimapProfiledConfigOptions.CAVE_ZOOM));
      }

      if (target > (double)6.0F) {
         target = (double)6.0F;
      }

      return target;
   }

   public void instantZoom() {
      this.minimapZoom = this.getTargetZoom();
   }

   public void updateZoom() {
      double target = this.getTargetZoom();
      double off = target - this.minimapZoom;
      if (!(off > 0.01) && !(off < -0.01)) {
         off = (double)0.0F;
      } else {
         off = (double)((float)MultiplyAnimationHelper.animate(off, 0.8));
      }

      this.minimapZoom = target - off;
   }

   public MinimapWriter getMinimapWriter() {
      return this.minimapWriter;
   }

   public boolean canUseFrameBuffer() {
      return true;
   }

   public int getFBOBufferSize() {
      return 512;
   }

   public void onClientTick() {
      class_1937 world = null;
      class_1657 player = class_310.method_1551().field_1724;
      if (player != null && player.method_73183() instanceof class_638) {
         world = player.method_73183();
      }

      class_1297 renderEntity = class_310.method_1551().method_1560();
      this.radarSession.update((class_638)world, renderEntity, player);
      this.modMain.getSupportMods().onClientTick();
   }

   public void onPlayerTick() {
   }

   public void checkFBO() {
      if (this.minimap.getMinimapFBORenderer().isLoadedFBO() && !this.canUseFrameBuffer()) {
         this.minimap.getMinimapFBORenderer().setLoadedFBO(false);
         this.minimap.getMinimapFBORenderer().deleteFramebuffers();
         this.toResetImage = true;
      }

      boolean mapSafeMode = (Boolean)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.SAFE_MODE);
      if (!this.minimap.getMinimapFBORenderer().isLoadedFBO() && !mapSafeMode && !this.minimap.getMinimapFBORenderer().isTriedFBO()) {
         if (class_310.method_1551().method_18506() != null) {
            return;
         }

         this.minimap.getMinimapFBORenderer().loadFrameBuffer(this);
      }

   }

   public void onRender(int x, int y, int width, int height, double scale, float minimapScale, int size, int boxSize, float partial, XaeroBufferProvider cvc, int pipWidth, int pipHeight) {
      MinimapRendererHelper.restoreDefaultShaderBlendState();
      RenderSystem.getModelViewStack().pushMatrix();
      RenderSystem.getModelViewStack().identity();
      RenderSystem.getModelViewStack().translate(0.0F, 0.0F, -11000.0F);

      try {
         class_310 mc = class_310.method_1551();
         IGuiRenderer guiRenderer = (IGuiRenderer)((IGameRenderer)mc.field_1773).xaero_lib_getGuiRenderer();
         class_11278 orthoProjectionCache = guiRenderer.xaero_mm_getGuiProjectionMatrixBuffer();
         RenderSystem.setProjectionMatrix(orthoProjectionCache.method_71092((float)pipWidth, (float)pipHeight), class_10366.field_54954);
         this.minimap.getMatrixStack().method_22903();
         this.minimap.getMatrixStack().method_22904((double)0.0F, (double)0.0F, 0.01);
         this.getRenderer().renderMinimap(this.minimapSession, this, x, y, width, height, scale, minimapScale, size, partial, cvc);
         this.minimap.getMatrixStack().method_22909();
      } catch (Throwable e) {
         this.minimap.setCrashedWith(e);
      }

      RenderSystem.getModelViewStack().popMatrix();
      MinimapRendererHelper.restoreDefaultShaderBlendState();
   }

   public MinimapRenderer getRenderer() {
      return (MinimapRenderer)(this.minimap.usingFBO() ? this.minimap.getMinimapFBORenderer() : this.minimap.getMinimapSafeModeRenderer());
   }

   public static boolean hasMinimapItem(class_1657 player) {
      MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
      if (session == null) {
         return true;
      } else {
         MinimapProcessor processor = session.getProcessor();
         return processor.minimapItem == null || Misc.hasItem(player, processor.minimapItem);
      }
   }

   public boolean isToResetImage() {
      return this.toResetImage;
   }

   public void setToResetImage(boolean toResetImage) {
      this.toResetImage = toResetImage;
   }

   public RadarSession getRadarSession() {
      return this.radarSession;
   }

   public void cleanup() {
      this.minimapWriter.cleanup();
   }

   public boolean isManualCaveMode() {
      return this.manualCaveMode || this.modMain.getSupportMods().shouldUseWorldMapCaveChunks() && this.modMain.getSupportMods().worldmapSupport.getManualCaveStart() != Integer.MAX_VALUE;
   }

   public void toggleManualCaveMode() {
      this.manualCaveMode = !this.isManualCaveMode();
   }

   public Minimap getMinimap() {
      return this.minimap;
   }

   public boolean getNoMinimapMessageReceived() {
      return HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getServerSynced().isChannelPresentOnServer() ? false : this.noMinimapMessageReceived;
   }

   public void setNoMinimapMessageReceived(boolean noMinimapMessageReceived) {
      this.noMinimapMessageReceived = noMinimapMessageReceived;
   }

   public boolean getForcedFairPlay() {
      return this.fairPlayOnlyMessageReceived;
   }

   public void setFairPlayOnlyMessageReceived(boolean fairPlayOnlyMessageReceived) {
      this.fairPlayOnlyMessageReceived = fairPlayOnlyMessageReceived;
   }

   public ClientSyncedTrackedPlayerManager getSyncedTrackedPlayerManager() {
      return this.syncedTrackedPlayerManager;
   }

   public boolean serverHasMod() {
      MinimapClientWorldData worldData = MinimapClientWorldDataHelper.getCurrentWorldData();
      return worldData != null && worldData.serverLevelId != null;
   }

   public void setServerModNetworkVersion(int networkVersion) {
      MinimapClientWorldData worldData = MinimapClientWorldDataHelper.getCurrentWorldData();
      if (worldData != null) {
         worldData.setServerModNetworkVersion(networkVersion);
      }
   }

   public int getServerModNetworkVersion() {
      MinimapClientWorldData worldData = MinimapClientWorldDataHelper.getCurrentWorldData();
      return worldData == null ? 0 : worldData.getServerModNetworkVersion();
   }

   public double getLastMapDimensionScale() {
      return this.lastMapDimensionScale;
   }

   public void setLastMapDimensionScale(double lastMapDimensionScale) {
      this.lastMapDimensionScale = lastMapDimensionScale;
   }

   public class_5321<class_1937> getLastMapDimension() {
      return this.lastMapDimension;
   }

   public void setLastMapDimension(class_5321<class_1937> lastMapDimension) {
      this.lastMapDimension = lastMapDimension;
   }

   public MinimapSession getSession() {
      return this.minimapSession;
   }

   public void updateMinimapItem() {
      String minimapItemString = ((String)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.MINIMAP_ITEM)).trim();
      if (!minimapItemString.isEmpty() && !minimapItemString.equals("-")) {
         class_2960 minimapItemRL;
         try {
            minimapItemRL = class_2960.method_60654(minimapItemString);
         } catch (class_151 rle) {
            this.minimapItem = null;
            MinimapLogs.LOGGER.error("Tried setting the minimap required item to a misformatted ID: {}; Error: {}", minimapItemString, rle.getMessage());
            return;
         }

         this.minimapItem = (class_1792)class_7923.field_41178.method_63535(minimapItemRL);
         if (this.minimapItem == class_1802.field_8162) {
            this.minimapItem = null;
            MinimapLogs.LOGGER.error("Tried setting the minimap required item to an invalid ID: {}", minimapItemString);
         } else {
            MinimapLogs.LOGGER.info("Minimap item set: " + this.minimapItem.method_63680().getString());
         }
      } else {
         this.minimapItem = null;
         MinimapLogs.LOGGER.info("Minimap required item set to nothing.");
      }
   }

   public class_1792 getMinimapItem() {
      return this.minimapItem;
   }

   public void setConsideringNetherFairPlayMessage(boolean consideringNetherFairPlay) {
      this.consideringNetherFairPlayMessage = consideringNetherFairPlay;
   }

   public boolean isConsideringNetherFairPlayMessage() {
      return this.consideringNetherFairPlayMessage;
   }

   public MinimapPipRenderState getRenderState() {
      return this.renderState;
   }

   public MinimapPipRenderState.Enlarged getEnlargedRenderState() {
      return this.enlargedRenderState;
   }
}
