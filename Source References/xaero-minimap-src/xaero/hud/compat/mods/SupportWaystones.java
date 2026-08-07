package xaero.hud.compat.mods;

import java.util.UUID;
import java.util.function.Consumer;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneTypes;
import net.blay09.mods.waystones.api.WaystoneVisibility;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.blay09.mods.waystones.api.event.WaystoneRemoveReceivedEvent;
import net.blay09.mods.waystones.api.event.WaystoneUpdatedEvent;
import net.blay09.mods.waystones.api.event.WaystonesListReceivedEvent;
import net.minecraft.class_1074;
import net.minecraft.class_1767;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.thirdparty.ThirdPartyWaypointManager;
import xaero.hud.minimap.waypoint.thirdparty.ThirdPartyWaypoints;
import xaero.hud.minimap.world.container.MinimapWorldContainer;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;

public class SupportWaystones {
   public void registerClientEvents() {
      WaystonesListReceivedEvent.EVENT.register((Consumer)(event) -> {
         if (class_310.method_1551().method_18854()) {
            MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
            if (session != null) {
               MinimapWorldRootContainer rootContainer = session.getWorldManager().getAutoRootContainer();
               this.removeAllOfOrigin(event.waystoneType(), rootContainer);

               for(Waystone waystone : event.waystones()) {
                  this.addWaypoint(waystone, rootContainer);
               }

            }
         }
      });
      WaystoneUpdatedEvent.EVENT.register((Consumer)(event) -> {
         if (class_310.method_1551().method_18854()) {
            MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
            if (session != null) {
               MinimapWorldRootContainer rootContainer = session.getWorldManager().getAutoRootContainer();
               this.addWaypoint(event.waystone(), rootContainer);
            }
         }
      });
      WaystoneRemoveReceivedEvent.EVENT.register((Consumer)(event) -> {
         if (class_310.method_1551().method_18854()) {
            MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
            if (session != null) {
               MinimapWorldRootContainer rootContainer = session.getWorldManager().getAutoRootContainer();
               class_2960 originId = this.getDiscoveredOriginId(event.waystoneType());
               class_2960 globalOriginId = this.getGlobalOriginId(event.waystoneType());
               this.removeWaypoint(event.waystoneId(), originId, globalOriginId, rootContainer);
            }
         }
      });
   }

   public void onWaystoneDeactivation(Waystone waystone) {
      if (class_310.method_1551().method_18854()) {
         MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
         if (session != null) {
            MinimapWorldRootContainer rootContainer = session.getWorldManager().getAutoRootContainer();
            class_2960 originId = this.getDiscoveredOriginId(waystone.getWaystoneType());
            class_2960 globalOriginId = this.getGlobalOriginId(waystone.getWaystoneType());
            this.removeWaypoint(waystone.getWaystoneUid(), originId, globalOriginId, rootContainer);
         }
      }
   }

   private void addWaypoint(Waystone waystone, MinimapWorldRootContainer rootContainer) {
      class_2960 waystoneType = waystone.getWaystoneType();
      class_2960 originId = this.getDiscoveredOriginId(waystoneType);
      class_2960 globalOriginId = this.getGlobalOriginId(waystoneType);
      if (!waystone.isValid()) {
         this.removeWaypoint(waystone.getWaystoneUid(), originId, globalOriginId, rootContainer);
      } else {
         boolean activationType = waystone.getVisibility() == WaystoneVisibility.ACTIVATION;
         boolean isSharestone = WaystoneTypes.isSharestone(waystone.getWaystoneType());
         if (activationType && !isSharestone && !WaystonesAPI.isWaystoneActivated(class_310.method_1551().field_1724, waystone)) {
            this.removeWaypoint(waystone.getWaystoneUid(), originId, globalOriginId, rootContainer);
         } else {
            boolean isGlobal = waystone.getVisibility() == WaystoneVisibility.GLOBAL;
            if (isSharestone || activationType || isGlobal) {
               class_5321<class_1937> dimension = waystone.getDimension();
               String subContainerNode = rootContainer.getSession().getDimensionHelper().getDimensionDirectoryName(dimension);
               MinimapWorldContainer dimensionContainer = rootContainer.addSubContainer(rootContainer.getPath().resolve(subContainerNode));
               ThirdPartyWaypoints waystoneWaypoints = this.getWithConfigCheck(dimensionContainer.getThirdPartyWaypointManager(), activationType ? originId : globalOriginId);
               String waystoneName = waystone.hasName() ? waystone.getName().getString() : class_1074.method_4662("gui.xaero_default_waystone_name", new Object[0]);
               String initial = waystoneName.isEmpty() ? "X" : ("" + waystoneName.charAt(0)).toUpperCase();
               WaypointColor waypointColor = null;
               if (isSharestone) {
                  String waystoneTypePath = waystone.getWaystoneType().method_12832();
                  String dyeName = waystoneTypePath.substring(0, waystoneTypePath.length() - 11);
                  class_1767 dyeColor = class_1767.method_7793(dyeName, (class_1767)null);
                  if (dyeColor != null) {
                     waypointColor = WaypointColor.fromDye(dyeColor);
                  }
               }

               if (waypointColor == null) {
                  waypointColor = WaypointColor.fromIndex((int)((waystone.getWaystoneUid().getLeastSignificantBits() & 2147483647L) % (long)WaypointColor.values().length));
               }

               Waypoint waypoint = new Waypoint(waystone.getPos().method_10263(), waystone.getPos().method_10264(), waystone.getPos().method_10260(), waystoneName, initial, waypointColor, WaypointPurpose.NORMAL);
               waystoneWaypoints.add(waystone.getWaystoneUid().toString(), waypoint);
            }
         }
      }
   }

   private void removeAllOfOrigin(class_2960 originId, MinimapWorldRootContainer rootContainer) {
      for(MinimapWorldContainer subContainer : rootContainer.getSubContainers()) {
         subContainer.getThirdPartyWaypointManager().clearOrigin(originId);
      }

   }

   private void removeWaypoint(UUID waystoneId, class_2960 originId, class_2960 globalOriginId, MinimapWorldRootContainer rootContainer) {
      for(MinimapWorldContainer subContainer : rootContainer.getSubContainers()) {
         this.getWithConfigCheck(subContainer.getThirdPartyWaypointManager(), originId).remove(waystoneId.toString());
      }

   }

   private void removeWaypointFromAllOrigins(UUID waystoneId, MinimapWorldRootContainer rootContainer) {
      for(MinimapWorldContainer subContainer : rootContainer.getSubContainers()) {
         for(ThirdPartyWaypoints thirdPartyWaypoints : subContainer.getThirdPartyWaypointManager().getAll()) {
            thirdPartyWaypoints.remove(waystoneId.toString());
         }
      }

   }

   private ThirdPartyWaypoints getWithConfigCheck(ThirdPartyWaypointManager manager, class_2960 originId) {
      ThirdPartyWaypoints result = manager.get(originId);
      if (!result.hasEnabledStateGetter()) {
         if (WaystoneTypes.isSharestone(originId)) {
            result.setEnabledStateGetter(MinimapConfigClientUtils::areOtherWaystonesEnabled);
         } else {
            result.setEnabledStateGetter(MinimapConfigClientUtils::areDiscoveredWaystonesEnabled);
         }
      }

      return result;
   }

   private class_2960 getDiscoveredOriginId(class_2960 waystoneType) {
      return waystoneType;
   }

   private class_2960 getGlobalOriginId(class_2960 waystoneType) {
      return waystoneType;
   }
}
