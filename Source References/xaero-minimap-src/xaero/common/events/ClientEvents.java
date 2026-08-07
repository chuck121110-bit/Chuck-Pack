package xaero.common.events;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.class_10366;
import net.minecraft.class_1041;
import net.minecraft.class_1059;
import net.minecraft.class_10725;
import net.minecraft.class_1074;
import net.minecraft.class_11278;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1936;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2556;
import net.minecraft.class_2561;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4358;
import net.minecraft.class_437;
import net.minecraft.class_4398;
import net.minecraft.class_442;
import net.minecraft.class_4439;
import net.minecraft.class_4666;
import net.minecraft.class_4877;
import net.minecraft.class_500;
import net.minecraft.class_638;
import org.apache.commons.lang3.StringUtils;
import xaero.common.HudMod;
import xaero.common.XaeroMinimapSession;
import xaero.common.core.XaeroMinimapCore;
import xaero.common.effect.Effects;
import xaero.common.gui.GuiAddWaypoint;
import xaero.common.gui.GuiWaypoints;
import xaero.common.minimap.MinimapProcessor;
import xaero.common.minimap.mcworld.MinimapClientWorldDataHelper;
import xaero.common.misc.Misc;
import xaero.hud.HudSession;
import xaero.hud.controls.key.KeyMappingTickHandler;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.element.render.world.MinimapElementWorldRendererHandler;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointSession;
import xaero.lib.common.reflection.util.ReflectionUtils;
import xaero.lib.patreon.Patreon;

public class ClientEvents {
   protected HudMod modMain;
   private class_437 lastGuiOpen;
   private Field realmsTaskField;
   private Field realmsTaskServerField;
   public class_4877 latestRealm;

   public ClientEvents(HudMod modMain) {
      this.modMain = modMain;
   }

   public class_437 handleGuiOpen(class_437 gui) {
      if (!this.modMain.isFirstStageLoaded()) {
         return gui;
      } else {
         if (gui instanceof class_442 || gui instanceof class_500) {
            this.modMain.getSettings().resetServerSettings();
         }

         class_310 mc = class_310.method_1551();
         if (gui instanceof class_4398) {
            try {
               if (this.realmsTaskField == null) {
                  this.realmsTaskField = ReflectionUtils.getFieldReflection(class_4398.class, "queuedTasks", "field_46707", "Ljava/util/List;", "f_302752_");
                  this.realmsTaskField.setAccessible(true);
               }

               if (this.realmsTaskServerField == null) {
                  this.realmsTaskServerField = ReflectionUtils.getFieldReflection(class_4439.class, "server", "field_20224", "Lnet/minecraft/class_4877;", "f_90327_");
                  this.realmsTaskServerField.setAccessible(true);
               }

               class_4398 realmsTaskScreen = (class_4398)gui;

               for(class_4358 task : (List)this.realmsTaskField.get(realmsTaskScreen)) {
                  if (task instanceof class_4439) {
                     class_4439 realmsTask = (class_4439)task;
                     class_4877 realm = (class_4877)this.realmsTaskServerField.get(realmsTask);
                     if (realm != null && (this.latestRealm == null || realm.field_22599 != this.latestRealm.field_22599)) {
                        this.latestRealm = realm;
                     }
                  }
               }
            } catch (Exception e) {
               MinimapLogs.LOGGER.error("suppressed exception", e);
            }
         } else if ((gui instanceof GuiAddWaypoint || gui instanceof GuiWaypoints) && (Misc.hasEffect(Effects.NO_WAYPOINTS) || Misc.hasEffect(Effects.NO_WAYPOINTS_HARMFUL))) {
            gui = null;
         }

         this.lastGuiOpen = gui;
         return gui;
      }
   }

   public void handleRenderGameOverlayEventPre(class_332 guiGraphics, float partialTicks) {
      if (!class_310.method_1551().field_1690.field_1842) {
         MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
         if (minimapSession != null) {
            class_1041 mainwindow = class_310.method_1551().method_22683();
            GpuBufferSlice projectionMatrixBU = RenderSystem.getProjectionMatrixBuffer();
            class_10366 projectionTypeBU = RenderSystem.getProjectionType();
            MinimapElementWorldRendererHandler worldRendererHandler = HudMod.INSTANCE.getMinimap().getWorldRendererHandler();
            class_11278 orthoProjectionCache = worldRendererHandler.getOrthoProjectionCache();
            GpuBufferSlice orthoBufferSlice = orthoProjectionCache.method_71092((float)mainwindow.method_4489(), (float)mainwindow.method_4506());
            RenderSystem.setProjectionMatrix(orthoBufferSlice, class_10366.field_54954);
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().identity();
            class_310 mc = class_310.method_1551();
            class_243 renderPos = mc.field_1773.method_19418().method_71156();
            worldRendererHandler.prepareRender(XaeroMinimapCore.waypointsProjection, XaeroMinimapCore.waypointModelView);
            worldRendererHandler.render(renderPos, partialTicks, (class_276)null, mc.field_1687.method_8597().comp_646(), mc.field_1687.method_27983());
            RenderSystem.getModelViewStack().popMatrix();
            RenderSystem.setProjectionMatrix(projectionMatrixBU, projectionTypeBU);
         }

      }
   }

   public void handleRenderGameOverlayEventPost() {
      if (this.modMain.isLoadedClient()) {
         this.modMain.getHud().getEventHandler().handleRenderGameOverlayEventPost();
      }
   }

   public boolean handleClientSendChatEvent(String message) {
      if (message.startsWith("xaero_waypoint_add:")) {
         String[] args = message.split(":");
         WaypointSession minimapSession = ((MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession()).getWaypointSession();
         minimapSession.getSharing().onWaypointAdd(args);
         return true;
      } else if (message.equals("xaero_tp_anyway")) {
         WaypointSession minimapSession = ((MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession()).getWaypointSession();
         minimapSession.getTeleport().teleportAnyway();
         return true;
      } else {
         return false;
      }
   }

   public boolean handleClientPlayerChatReceivedEvent(class_2556.class_7602 chatType, class_2561 component, GameProfile gameProfile) {
      return component == null ? false : this.handleChatMessage(gameProfile == null ? class_1074.method_4662("gui.xaero_waypoint_somebody_shared", new Object[0]) : gameProfile.name(), component);
   }

   public boolean handleClientSystemChatReceivedEvent(class_2561 component) {
      if (component == null) {
         return false;
      } else {
         String textString = component.getString();
         if (textString.contains("§r§e§s§e§t§x§a§e§r§o")) {
            XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
            minimapSession.getMinimapProcessor().setNoMinimapMessageReceived(false);
            minimapSession.getMinimapProcessor().setFairPlayOnlyMessageReceived(false);
            minimapSession.getMinimapProcessor().setConsideringNetherFairPlayMessage(false);
         }

         if (textString.contains("§n§o§m§i§n§i§m§a§p")) {
            XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
            minimapSession.getMinimapProcessor().setNoMinimapMessageReceived(true);
         }

         if (textString.contains("§x§a§e§r§o§m§m§n§e§t§h§e§r§i§s§f§a§i§r")) {
            XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
            minimapSession.getMinimapProcessor().setConsideringNetherFairPlayMessage(true);
         }

         if (textString.contains("§f§a§i§r§x§a§e§r§o")) {
            XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
            minimapSession.getMinimapProcessor().setFairPlayOnlyMessageReceived(true);
         }

         String probableName = StringUtils.substringBetween(textString, "<", ">");
         return this.handleChatMessage(probableName == null ? class_1074.method_4662("gui.xaero_waypoint_server_shared", new Object[0]) : probableName, component);
      }
   }

   private boolean handleChatMessage(String playerName, class_2561 text) {
      String textString = text.getString();
      if (!textString.contains("xaero_waypoint:") && !textString.contains("xaero-waypoint:")) {
         return false;
      } else {
         MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
         if (minimapSession == null) {
            return false;
         } else {
            minimapSession.getWaypointSession().getSharing().onWaypointReceived(playerName, textString);
            return true;
         }
      }
   }

   public void handleDrawScreenEventPost(class_437 gui) {
      if (!Patreon.needsNotification() && this.modMain.isOutdated()) {
         this.modMain.setOutdated(false);
      }

   }

   public void handlePlayerSetSpawnEvent(class_2338 newSpawnPoint, class_1937 world) {
      if (world instanceof class_638) {
         MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
         if (minimapSession != null) {
            minimapSession.getWorldStateUpdater().setCurrentWorldSpawn(newSpawnPoint);
         }

         if (MinimapClientWorldDataHelper.getWorldData((class_638)world).serverLevelId == null) {
         }
      }

   }

   public Object getLastGuiOpen() {
      return this.lastGuiOpen;
   }

   public void worldUnload(class_1936 world) {
      if (world instanceof class_638) {
         XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
         if (minimapSession != null) {
            MinimapProcessor minimap = minimapSession.getMinimapProcessor();
            minimap.getRadarSession().update((class_638)null, (class_1297)null, (class_1657)null);
         }
      }

   }

   public void handleClientTickStart() {
      XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
      if (minimapSession != null) {
         MinimapProcessor minimap = minimapSession.getMinimapProcessor();
         minimap.onClientTick();
         if (class_310.method_1551().field_1755 == null) {
            minimapSession.getKeyMappingTickHandler().tick();
         }

         HudSession hudSession = HudSession.getCurrentSession();
         this.modMain.getClientEventsListener().clientTickPost(hudSession);
      }

   }

   public void handlePlayerTickStart(class_1657 player) {
      if (player == class_310.method_1551().field_1724) {
         if (this.modMain.isLoadedClient()) {
            MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
            if (minimapSession != null) {
               try {
                  MinimapProcessor minimap = minimapSession.getProcessor();
                  minimapSession.getWorldStateUpdater().update();
                  minimap.onPlayerTick();
                  class_310 mc = class_310.method_1551();
                  HudSession hudSession = HudSession.getCurrentSession();
                  this.modMain.getClientEventsListener().playerTickPost(hudSession);
               } catch (Throwable t) {
                  this.modMain.getMinimap().setCrashedWith(t);
               }
            }

         }
      }
   }

   public void handleRenderTickStart() {
      if (this.modMain.getMinimap() != null) {
         this.modMain.getMinimap().checkCrashes();
      }

      if (class_310.method_1551().field_1724 != null) {
         if (!this.modMain.isLoadedClient()) {
            return;
         }

         class_638 world = class_310.method_1551().field_1687;
         if (world != null) {
            MinimapClientWorldDataHelper.getWorldData(world).getAttributeSystem().method_76405();
         }

         XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
         if (minimapSession != null) {
            MinimapProcessor minimap = minimapSession.getMinimapProcessor();
            minimap.getMinimapWriter().onRender();
         }
      }

   }

   public boolean handleRenderStatusEffectOverlay(class_332 guiGraphics) {
      return !this.modMain.isLoadedClient() ? false : this.modMain.getClientEventsListener().handleRenderStatusEffectOverlay(guiGraphics);
   }

   public boolean handleRenderCrosshairOverlay(class_332 guiGraphics) {
      XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
      if (minimapSession == null) {
         return false;
      } else {
         return minimapSession.getMinimapProcessor().isEnlargedMap() && (Boolean)this.modMain.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.CENTERED_ENLARGED);
      }
   }

   public boolean handleForceToggleKeyMapping(class_4666 keyMapping) {
      return KeyMappingTickHandler.DISABLE_KEY_MAPPING_OVERRIDES ? false : this.modMain.getClientEventsListener().handleForceToggleKeyMapping(keyMapping);
   }

   public void handleTextureStitchEventPost(class_1059 texture) {
      if (texture == class_310.method_1551().method_72703().method_73025(class_10725.field_56382)) {
         XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
         if (minimapSession != null) {
            minimapSession.getMinimapProcessor().getMinimapWriter().setClearBlockColours(true);
            minimapSession.getMinimapProcessor().getMinimapWriter().onResourceReload();
            minimapSession.getMinimapProcessor().getMinimapWriter().resetShortBlocks();
         }

         Minimap minimap = this.modMain.getMinimap();
         if (minimap != null) {
            minimap.getMinimapFBORenderer().resetEntityIcons();
            this.handleTextureStitchEventPost_onReset();
         }
      }

   }

   protected void handleTextureStitchEventPost_onReset() {
   }
}
