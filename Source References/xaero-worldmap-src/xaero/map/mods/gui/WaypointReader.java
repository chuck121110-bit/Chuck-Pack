package xaero.map.mods.gui;

import java.util.ArrayList;
import net.minecraft.class_124;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_437;
import xaero.lib.client.config.ClientConfigManager;
import xaero.map.WorldMap;
import xaero.map.common.config.option.WorldMapProfiledConfigOptions;
import xaero.map.element.render.ElementReader;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.gui.GuiMap;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.gui.util.GuiUtils;
import xaero.map.mods.SupportMods;

public class WaypointReader extends ElementReader<Waypoint, WaypointRenderContext, WaypointRenderer> {
   public boolean waypointIsGood(Waypoint w, WaypointRenderContext context) {
      return (!w.getPurpose().isDeath() || context.deathpoints) && (w.isGlobal() || context.userScale >= context.minZoomForLocalWaypoints);
   }

   public boolean isHidden(Waypoint element, WaypointRenderContext context) {
      return !this.waypointIsGood(element, context) || !context.showDisabledWaypoints && element.isDisabled();
   }

   public boolean isInteractable(ElementRenderLocation location, Waypoint element) {
      return true;
   }

   public float getBoxScale(ElementRenderLocation location, Waypoint element, WaypointRenderContext context) {
      return context.worldmapWaypointsScale;
   }

   public double getRenderX(Waypoint element, WaypointRenderContext context, float partialTicks) {
      return element.getRenderX();
   }

   public double getRenderY(Waypoint element, WaypointRenderContext context, float partialTicks) {
      return (double)element.getY();
   }

   public boolean hasYCoordinate() {
      return true;
   }

   public double getRenderZ(Waypoint element, WaypointRenderContext context, float partialTicks) {
      return element.getRenderZ();
   }

   public int getInteractionBoxLeft(Waypoint element, WaypointRenderContext context, float partialTicks) {
      return -this.getInteractionBoxRight(element, context, partialTicks);
   }

   public int getInteractionBoxRight(Waypoint element, WaypointRenderContext context, float partialTicks) {
      return element.getSymbol().length() > 1 ? 21 : 14;
   }

   public int getInteractionBoxTop(Waypoint element, WaypointRenderContext context, float partialTicks) {
      return context.waypointBackgrounds ? -41 : -12;
   }

   public int getInteractionBoxBottom(Waypoint element, WaypointRenderContext context, float partialTicks) {
      return context.waypointBackgrounds ? 0 : 12;
   }

   public int getLeftSideLength(Waypoint element, class_310 mc) {
      return 9 + element.getCachedNameLength();
   }

   public String getMenuName(Waypoint element) {
      String name = element.getName();
      if (element.isGlobal()) {
         name = "* " + name;
      }

      return name;
   }

   public int getMenuTextFillLeftPadding(Waypoint element) {
      return (element.isDisabled() ? 11 : 0) + (element.isTemporary() ? 10 : 0);
   }

   public String getFilterName(Waypoint element) {
      String var10000 = this.getMenuName(element);
      return var10000 + " " + element.getSymbol();
   }

   public ArrayList<RightClickOption> getRightClickOptions(final Waypoint element, IRightClickableElement target) {
      final ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      class_310 minecraft = class_310.method_1551();
      ArrayList<RightClickOption> rightClickOptions = new ArrayList();
      rightClickOptions.add(new RightClickOption(element.getName(), rightClickOptions.size(), target) {
         public void onAction(class_437 screen) {
            SupportMods.xaeroMinimap.openWaypoint((GuiMap)screen, element);
         }
      });
      rightClickOptions.add(new RightClickOption("", class_2583.field_24360.method_10977(class_124.field_1080), rightClickOptions.size(), target) {
         protected String getName() {
            return (Boolean)configManager.getEffective(WorldMapProfiledConfigOptions.COORDINATES) && !SupportMods.xaeroMinimap.hidingWaypointCoordinates() ? String.format("X: %d, Y: %s, Z: %d", element.getX(), element.isyIncluded() ? "" + element.getY() : "~", element.getZ()) : "coords hidden";
         }

         public void onAction(class_437 screen) {
            SupportMods.xaeroMinimap.openWaypoint((GuiMap)screen, element);
         }
      });
      rightClickOptions.add((new RightClickOption("gui.xaero_right_click_waypoint_edit", rightClickOptions.size(), target) {
         public void onAction(class_437 screen) {
            SupportMods.xaeroMinimap.openWaypoint((GuiMap)screen, element);
         }
      }).setNameFormatArgs(new Object[]{GuiUtils.getBoundKeyComponent("E")}));
      rightClickOptions.add((new RightClickOption("gui.xaero_right_click_waypoint_teleport", rightClickOptions.size(), target) {
         public void onAction(class_437 screen) {
            SupportMods.xaeroMinimap.teleportToWaypoint(screen, element);
         }

         public boolean isActive() {
            return SupportMods.xaeroMinimap.canTeleport(SupportMods.xaeroMinimap.getWaypointWorld());
         }
      }).setNameFormatArgs(new Object[]{GuiUtils.getBoundKeyComponent("T")}));
      rightClickOptions.add(new RightClickOption("gui.xaero_right_click_waypoint_share", rightClickOptions.size(), target) {
         public void onAction(class_437 screen) {
            SupportMods.xaeroMinimap.shareWaypoint(element, (GuiMap)screen, SupportMods.xaeroMinimap.getWaypointWorld());
         }
      });
      rightClickOptions.add((new RightClickOption("", rightClickOptions.size(), target) {
         public String getName() {
            return element.isTemporary() ? "gui.xaero_right_click_waypoint_restore" : (element.isDisabled() ? "gui.xaero_right_click_waypoint_enable" : "gui.xaero_right_click_waypoint_disable");
         }

         public void onAction(class_437 screen) {
            if (element.isTemporary()) {
               SupportMods.xaeroMinimap.toggleTemporaryWaypoint(element);
            } else {
               SupportMods.xaeroMinimap.disableWaypoint(element);
            }

         }
      }).setNameFormatArgs(new Object[]{GuiUtils.getBoundKeyComponent("H")}));
      rightClickOptions.add((new RightClickOption("", rightClickOptions.size(), target) {
         public String getName() {
            return element.isTemporary() ? "gui.xaero_right_click_waypoint_delete_confirm" : "gui.xaero_right_click_waypoint_delete";
         }

         public void onAction(class_437 screen) {
            if (element.isTemporary()) {
               SupportMods.xaeroMinimap.deleteWaypoint(element);
            } else {
               SupportMods.xaeroMinimap.toggleTemporaryWaypoint(element);
            }

         }
      }).setNameFormatArgs(new Object[]{GuiUtils.getBoundKeyComponent("DEL")}));
      return rightClickOptions;
   }

   public boolean isRightClickValid(Waypoint element) {
      return SupportMods.xaeroMinimap.waypointExists(element);
   }

   public int getRightClickTitleBackgroundColor(Waypoint element) {
      return element.getColor();
   }

   public boolean shouldScaleBoxWithOptionalScale() {
      return true;
   }

   public int getRenderBoxLeft(Waypoint element, WaypointRenderContext context, float partialTicks) {
      int left = this.getInteractionBoxLeft(element, context, partialTicks);
      return element.getAlpha() <= 0.0F ? left : Math.min(left, -element.getCachedNameLength() * 3 / 2);
   }

   public int getRenderBoxRight(Waypoint element, WaypointRenderContext context, float partialTicks) {
      int right = this.getInteractionBoxRight(element, context, partialTicks) + 12;
      return element.getAlpha() <= 0.0F ? right : Math.max(right, element.getCachedNameLength() * 3 / 2);
   }

   public int getRenderBoxTop(Waypoint element, WaypointRenderContext context, float partialTicks) {
      return this.getInteractionBoxTop(element, context, partialTicks);
   }

   public int getRenderBoxBottom(Waypoint element, WaypointRenderContext context, float partialTicks) {
      return this.getInteractionBoxBottom(element, context, partialTicks);
   }
}
