package xaeroplus.feature.render.beacon;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_11658;
import net.minecraft.class_11659;
import net.minecraft.class_11661;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_822;
import net.minecraft.class_898;
import xaero.common.HudMod;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.WaypointVisibilityType;
import xaero.hud.minimap.waypoint.render.world.WaypointWorldRenderer;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.channel.ConfigChannel;
import xaeroplus.XaeroPlus;
import xaeroplus.settings.Settings;

public class WaypointBeaconRenderer {
   public static final WaypointBeaconRenderer INSTANCE = new WaypointBeaconRenderer();
   private final List<Waypoint> waypointList = new ArrayList();
   private long lastWaypointRenderListUpdate = -1L;
   private int errorCount = 0;

   public void renderHook(final class_4587 poseStack, final class_11658 levelRenderState, final class_11661 submitNodeStorage) {
      if (Settings.REGISTRY.waypointBeacons.get()) {
         HudMod hudMod = HudMod.INSTANCE;
         if (hudMod != null) {
            Minimap minimap = hudMod.getMinimap();
            if (minimap != null) {
               WaypointWorldRenderer waypointsIngameRenderer = minimap.getWaypointWorldRenderer();
               if (waypointsIngameRenderer != null) {
                  MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
                  if (minimapSession != null) {
                     try {
                        INSTANCE.renderWaypointBeacons(poseStack, levelRenderState, submitNodeStorage);
                     } catch (Exception e) {
                        if (this.errorCount++ < 2) {
                           XaeroPlus.LOGGER.error("Error rendering waypoints", e);
                        }
                     }

                  }
               }
            }
         }
      }
   }

   private void renderWaypointBeacons(final class_4587 matrixStack, final class_11658 levelRenderState, final class_11661 submitNodeStorage) {
      MinimapSession session = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      if (session != null) {
         ConfigChannel hudConfigs = HudMod.INSTANCE.getHudConfigs();
         if (hudConfigs != null) {
            ClientConfigManager clientConfigManager = hudConfigs.getClientConfigManager();
            if ((Boolean)clientConfigManager.getEffective(MinimapProfiledConfigOptions.WAYPOINTS_IN_WORLD)) {
               MinimapWorld currentWorld = session.getWorldManager().getCurrentWorld();
               if (currentWorld != null) {
                  if (System.currentTimeMillis() - this.lastWaypointRenderListUpdate > 50L) {
                     this.updateWaypointRenderList(session, clientConfigManager);
                     this.lastWaypointRenderListUpdate = System.currentTimeMillis();
                  }

                  double dimDiv = session.getDimensionHelper().getDimensionDivision(currentWorld);
                  class_310 mc = class_310.method_1551();
                  if (mc.field_1687 != null && mc.method_1560() != null) {
                     if (mc.field_1724.method_73183() == mc.field_1687) {
                        class_243 cameraPos = mc.method_1560().method_73189();
                        double distanceScale = (Boolean)clientConfigManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_MAX_DISTANCE_DIMENSION_SCALE) ? mc.field_1687.method_8597().comp_646() : (double)1.0F;
                        double waypointsDistance = (double)(Integer)clientConfigManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_MAX_DISTANCE);
                        double waypointsDistanceMin = (Double)clientConfigManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_MIN_DISTANCE_IN_WORLD);

                        for(int i = 0; i < this.waypointList.size(); ++i) {
                           Waypoint w = (Waypoint)this.waypointList.get(i);
                           double offX = (double)w.getX(dimDiv) - cameraPos.field_1352 + (double)0.5F;
                           double offZ = (double)w.getZ(dimDiv) - cameraPos.field_1350 + (double)0.5F;
                           double unscaledDistance2D = Math.sqrt(offX * offX + offZ * offZ);
                           double distance2D = unscaledDistance2D * distanceScale;
                           if (Settings.REGISTRY.limitDeathpointsRenderDistance.get()) {
                              WaypointPurpose purpose = w.getPurpose();
                              if (purpose == WaypointPurpose.DEATH && Settings.REGISTRY.limitDeathpointsRenderDistance.get() && waypointsDistance != (double)0.0F && distance2D > waypointsDistance) {
                                 continue;
                              }
                           }

                           boolean shouldRender = w.isDestination() || (w.getPurpose().isDeath() || w.isGlobal() || w.isTemporary() && (Boolean)clientConfigManager.getEffective(MinimapProfiledConfigOptions.TEMPORARY_WAYPOINTS_GLOBAL) || waypointsDistance == (double)0.0F || !(distance2D > waypointsDistance)) && (waypointsDistanceMin == (double)0.0F || !(unscaledDistance2D < waypointsDistanceMin));
                           if (shouldRender) {
                              this.renderWaypointBeacon(w, dimDiv, matrixStack, levelRenderState, submitNodeStorage);
                           }
                        }

                     }
                  }
               }
            }
         }
      }
   }

   public void updateWaypointRenderList(final MinimapSession session, final ClientConfigManager settings) {
      this.waypointList.clear();
      session.getWaypointSession().getCollector().collect(this.waypointList);
      this.waypointList.removeIf((w) -> {
         if (!w.isDisabled() && w.getVisibility() != WaypointVisibilityType.WORLD_MAP_LOCAL && w.getVisibility() != WaypointVisibilityType.WORLD_MAP_GLOBAL) {
            return !(Boolean)settings.getEffective(MinimapProfiledConfigOptions.DEATHPOINTS) && w.getPurpose().isDeath();
         } else {
            return true;
         }
      });
      this.waypointList.sort(Waypoint::compareTo);
   }

   public void renderWaypointBeacon(final Waypoint waypoint, final double dimDiv, class_4587 matrixStack, class_11658 levelRenderState, class_11659 submitNodeCollector) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 != null && mc.method_1560() != null) {
         class_243 playerVec = mc.method_1560().method_73189();
         class_243 waypointVec = new class_243((double)waypoint.getX(dimDiv), playerVec.field_1351, (double)waypoint.getZ(dimDiv));
         double xzDistance = playerVec.method_1022(waypointVec);
         if (!(xzDistance < (double)Settings.REGISTRY.waypointBeaconDistanceMin.getAsInt())) {
            int farScale = Settings.REGISTRY.waypointBeaconScaleMin.getAsInt();
            double maxRenderDistance = (double)Math.min((Integer)mc.field_1690.method_42503().method_41753() << 4, farScale == 0 ? Integer.MAX_VALUE : farScale << 4);
            if (xzDistance > maxRenderDistance) {
               class_243 delta = waypointVec.method_1020(playerVec).method_1029();
               waypointVec = playerVec.method_1019(new class_243(delta.field_1352 * maxRenderDistance, delta.field_1351 * maxRenderDistance, delta.field_1350 * maxRenderDistance));
            }

            class_898 entityRenderDispatcher = mc.method_1561();
            class_4184 camera = entityRenderDispatcher.field_4686;
            if (camera != null) {
               double viewX = camera.method_71156().method_10216();
               double viewZ = camera.method_71156().method_10215();
               double x = waypointVec.field_1352 - viewX;
               double z = waypointVec.field_1350 - viewZ;
               double y = (double)-100.0F;
               int color = waypoint.getWaypointColor().getHex();
               int animationTime = Math.floorMod(mc.field_1687.method_75260(), 40);
               matrixStack.method_22903();
               matrixStack.method_22904(x, (double)-100.0F, z);
               class_822.method_3545(matrixStack, submitNodeCollector, class_822.field_4338, 1.0F, (float)animationTime, 0, 355, color, 0.2F, 0.25F);
               matrixStack.method_22909();
            }
         }
      }
   }
}
