package xaero.hud.minimap.player.tracker;

import net.minecraft.class_1068;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_640;
import net.minecraft.class_742;
import net.minecraft.class_327.class_6415;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.common.graphics.CustomRenderTypes;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.hud.entity.EntityUtils;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderer;
import xaero.hud.render.util.RenderBufferUtil;
import xaero.lib.XaeroLib;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.graphics.XaeroBufferProvider;

public final class PlayerTrackerMinimapElementRenderer extends MinimapElementRenderer<PlayerTrackerMinimapElement<?>, PlayerTrackerMinimapElementRenderContext> {
   private final double WORLD_MINIMUM_DISTANCE = (double)10.0F;
   private final double WORLD_FADING_LENGTH = (double)10.0F;
   private XaeroBufferProvider minimapBufferSource;
   private final PlayerTrackerMinimapElementCollector elementCollector;
   private final PlayerTrackerIconRenderer playerTrackerIconRenderer;
   private final IXaeroMinimap modMain;
   private float nameScale;

   private PlayerTrackerMinimapElementRenderer(PlayerTrackerMinimapElementCollector elementCollector, IXaeroMinimap modMain, PlayerTrackerMinimapElementRenderContext context, PlayerTrackerMinimapElementRenderProvider<PlayerTrackerMinimapElementRenderContext> provider, PlayerTrackerMinimapElementReader reader, PlayerTrackerIconRenderer playerTrackerIconRenderer) {
      super(reader, provider, context);
      this.elementCollector = elementCollector;
      this.modMain = modMain;
      this.playerTrackerIconRenderer = playerTrackerIconRenderer;
   }

   public class_2960 getPlayerSkin(class_1657 player, class_640 info) {
      class_2960 skinTextureLocation = player instanceof class_742 ? ((class_742)player).method_52814().comp_1626().comp_3627() : info.method_52810().comp_1626().comp_3627();
      if (skinTextureLocation == null) {
         skinTextureLocation = class_1068.method_4648(player.method_5667()).comp_1626().comp_3627();
      }

      return skinTextureLocation;
   }

   public void preRender(MinimapElementRenderInfo renderInfo, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider rendererProvider) {
      vanillaBufferSource.method_22993();
      this.minimapBufferSource = XaeroLib.INSTANCE.getClient().getBufferProvider();
      (this.context).coloredBackgroundConsumer = this.minimapBufferSource.getBuffer(CustomRenderTypes.COLORED_WAYPOINTS_BGS);
      (this.context).uniqueTextureUIObjectRenderer = rendererProvider.getRenderer(MultiTextureRenderTypeRendererProvider::defaultTextureBind, CustomRenderTypes.GUI_NEAREST);
      (this.context).renderEntityDimId = renderInfo.renderEntityDimension;
      (this.context).mapDimId = renderInfo.mapDimension;
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      float trackedPlayerWorldIconScale = MinimapConfigClientUtils.getUIScale(configManager, MinimapProfiledConfigOptions.TRACKED_PLAYER_WORLD_ICON_SCALE);
      float trackedPlayerMinimapIconScale = MinimapConfigClientUtils.getUIScale(configManager, MinimapProfiledConfigOptions.TRACKED_PLAYER_MINIMAP_ICON_SCALE);
      (this.context).iconScale = renderInfo.location == MinimapElementRenderLocation.IN_WORLD ? trackedPlayerWorldIconScale : trackedPlayerMinimapIconScale;
      this.nameScale = MinimapConfigClientUtils.getUIScale(configManager, MinimapProfiledConfigOptions.TRACKED_PLAYER_WORLD_NAME_SCALE);
   }

   public void postRender(MinimapElementRenderInfo renderInfo, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider rendererProvider) {
      rendererProvider.draw((this.context).uniqueTextureUIObjectRenderer);
      this.minimapBufferSource.endBatch();
      this.elementCollector.resetRenderedOnRadarFlags();
   }

   public boolean renderElement(PlayerTrackerMinimapElement<?> e, boolean highlighted, boolean outOfBounds, double optionalDepth, float optionalScale, double partialX, double partialY, MinimapElementRenderInfo renderInfo, MinimapElementGraphics guiGraphics, class_4597.class_4598 vanillaBufferSource) {
      if (!outOfBounds && renderInfo.location != MinimapElementRenderLocation.IN_WORLD && e.wasRenderedOnRadar()) {
         return false;
      } else {
         class_4587 matrixStack = guiGraphics.pose();
         class_310 mc = class_310.method_1551();
         class_640 info = mc.method_1562().method_2871(e.getPlayerId());
         if (info == null) {
            return false;
         } else {
            class_1657 clientPlayer = mc.field_1687.method_18470(e.getPlayerId());
            double trackedX = clientPlayer == null ? e.getX() : EntityUtils.getEntityX(clientPlayer, renderInfo.partialTicks);
            double trackedY = clientPlayer == null ? e.getY() : EntityUtils.getEntityY(clientPlayer, renderInfo.partialTicks);
            double trackedZ = clientPlayer == null ? e.getZ() : EntityUtils.getEntityZ(clientPlayer, renderInfo.partialTicks);
            double offX = trackedX - renderInfo.renderEntityPos.field_1352;
            double offY = trackedY - renderInfo.renderEntityPos.field_1351;
            double offZ = trackedZ - renderInfo.renderEntityPos.field_1350;
            double distance = Math.sqrt(offX * offX + offY * offY + offZ * offZ);
            if (distance < (double)10.0F) {
               return false;
            } else {
               matrixStack.method_22903();
               matrixStack.method_22904((double)0.0F, (double)0.0F, optionalDepth);
               boolean inWorld = renderInfo.location == MinimapElementRenderLocation.IN_WORLD;
               float alpha = inWorld ? 0.5F : 1.0F;
               if (highlighted && inWorld) {
                  alpha = 0.8F;
               }

               if (!highlighted && inWorld && distance < (double)20.0F) {
                  alpha *= (float)((distance - (double)10.0F) / (double)10.0F);
               }

               matrixStack.method_22904((double)0.0F, (double)0.0F, 0.01);
               matrixStack.method_22903();
               matrixStack.method_22905((this.context).iconScale, (this.context).iconScale, 1.0F);
               RenderBufferUtil.addColoredRect(matrixStack.method_23760().method_23761(), (this.context).coloredBackgroundConsumer, -5.0F, -5.0F, 10, 10, 1.0F, 1.0F, 1.0F, alpha);
               this.playerTrackerIconRenderer.renderIcon(mc, (this.context).uniqueTextureUIObjectRenderer, matrixStack, clientPlayer, this.getPlayerSkin(clientPlayer, info), info, alpha);
               matrixStack.method_22909();
               if (highlighted && inWorld) {
                  matrixStack.method_46416(-5.0F * (this.context).iconScale, 0.0F, 0.0F);
                  matrixStack.method_22905(this.nameScale, this.nameScale, 1.0F);
                  String playerName = info.method_2966().name();
                  int playerNameWidth = mc.field_1772.method_1727(playerName);
                  float labelAlpha = 0.3529412F;
                  RenderBufferUtil.addColoredRect(matrixStack.method_23760().method_23761(), (this.context).coloredBackgroundConsumer, (float)(-playerNameWidth - 1), -5.0F, playerNameWidth + 1, 10, 0.0F, 0.0F, 0.0F, labelAlpha);
                  mc.field_1772.method_27521(playerName, (float)(-playerNameWidth), -4.0F, -1, false, matrixStack.method_23760().method_23761(), this.minimapBufferSource, class_6415.field_33993, 0, 15728880);
               }

               matrixStack.method_22909();
               return true;
            }
         }
      }
   }

   public boolean shouldRender(MinimapElementRenderLocation location) {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      return location != MinimapElementRenderLocation.IN_WORLD && (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.TRACKED_PLAYERS_ON_MINIMAP) || location == MinimapElementRenderLocation.IN_WORLD && (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.TRACKED_PLAYERS_IN_WORLD);
   }

   public int getOrder() {
      return 100;
   }

   public PlayerTrackerMinimapElementCollector getCollector() {
      return this.elementCollector;
   }

   public static final class Builder {
      private final IXaeroMinimap modMain;

      private Builder(IXaeroMinimap modMain) {
         this.modMain = modMain;
      }

      private Builder setDefault() {
         return this;
      }

      public PlayerTrackerMinimapElementRenderer build() {
         PlayerTrackerMinimapElementCollector collector = new PlayerTrackerMinimapElementCollector(this.modMain.getRenderedPlayerTrackerManager());
         return new PlayerTrackerMinimapElementRenderer(collector, this.modMain, new PlayerTrackerMinimapElementRenderContext(), new PlayerTrackerMinimapElementRenderProvider(collector), new PlayerTrackerMinimapElementReader(), new PlayerTrackerIconRenderer());
      }

      public static Builder begin(IXaeroMinimap modMain) {
         return (new Builder(modMain)).setDefault();
      }
   }
}
