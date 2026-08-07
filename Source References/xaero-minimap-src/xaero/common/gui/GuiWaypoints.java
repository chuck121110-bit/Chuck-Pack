package xaero.common.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;
import net.minecraft.class_1074;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_11910;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4184;
import net.minecraft.class_4185;
import net.minecraft.class_4280;
import net.minecraft.class_437;
import net.minecraft.class_3675.class_307;
import xaero.common.HudMod;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.entity.EntityUtils;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.config.primary.option.MinimapPrimaryClientConfigOptions;
import xaero.hud.minimap.controls.key.MinimapKeyMappings;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointRenderInfo;
import xaero.hud.minimap.waypoint.WaypointsSort;
import xaero.hud.minimap.waypoint.thirdparty.ThirdPartyWaypointManager;
import xaero.hud.minimap.waypoint.thirdparty.ThirdPartyWaypoints;
import xaero.hud.minimap.waypoint.util.WaypointUtils;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.MinimapWorldManager;
import xaero.hud.path.XaeroPath;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.controls.util.KeyMappingUtils;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.util.GuiUtils;
import xaero.lib.client.gui.widget.MyTinyButton;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.lib.client.gui.widget.dropdown.DropDownWidget;
import xaero.lib.client.gui.widget.dropdown.IDropDownWidgetCallback;
import xaero.lib.client.gui.widget.dropdown.DropDownWidget.Builder;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.single.SingleConfigManager;
import xaero.lib.common.util.KeySortableByOther;

public class GuiWaypoints extends ScreenBase implements IDropDownWidgetCallback {
   private static final int FRAME_TOP_SIZE = 58;
   private static final int FRAME_BOTTOM_SIZE = 61;
   public static double distanceDivided;
   public static boolean showingThirdPartyDeleted;
   private List list;
   private MinimapWorld displayedWorld;
   private ConcurrentSkipListSet<Integer> selectedListSet;
   private GuiWaypointContainers containers;
   private GuiWaypointWorlds worlds;
   private GuiWaypointSets sets;
   private DropDownWidget containersDD;
   private DropDownWidget worldsDD;
   private DropDownWidget setsDD;
   private MinimapSession session;
   private MinimapWorldManager manager;
   private int draggingFromX;
   private int draggingFromY;
   private int draggingFromSlot;
   private Waypoint draggingWaypoint;
   private boolean displayingTeleportableWorld;
   private int shiftSelectFirst;
   private ArrayList<Waypoint> waypointsSorted;
   private ArrayList<Waypoint> thirdPartyWaypointsSorted;
   private final XaeroPath frozenAutoWorldPath;
   private class_4185 deleteButton;
   private class_4185 editButton;
   private class_4185 teleportButton;
   private class_4185 disableEnableButton;
   private class_4185 clearButton;
   private class_4185 shareButton;
   private class_342 filterBox;
   private String filterValue;
   private boolean filterResponderPaused;
   private final HudMod modMain;
   private boolean leftMouseButtonDown;
   private String previousDistanceText;
   private Tooltip previousDistanceTooltip;

   public GuiWaypoints(HudMod modMain, MinimapSession session, class_437 par1GuiScreen, class_437 escapeScreen) {
      super(par1GuiScreen, escapeScreen, class_2561.method_43471("gui.xaero_waypoints"));
      this.modMain = modMain;
      this.session = session;
      this.manager = session.getWorldManager();
      this.frozenAutoWorldPath = session.getWorldState().getAutoWorldPath();
      this.displayedWorld = this.manager.getCurrentWorld(this.frozenAutoWorldPath);
      if (this.displayedWorld != null) {
         this.selectedListSet = new ConcurrentSkipListSet();
         this.draggingFromX = -1;
         this.draggingFromY = -1;
         this.draggingFromSlot = -1;
         XaeroPath currentContainer = this.displayedWorld.getContainer().getRoot().getPath();
         this.containers = new GuiWaypointContainers(modMain, this.manager, currentContainer, this.frozenAutoWorldPath);
         this.worlds = new GuiWaypointWorlds(this.manager.getRootWorldContainer((String)this.containers.getCurrentKey()), session, this.displayedWorld.getFullPath(), this.frozenAutoWorldPath);
         this.displayingTeleportableWorld = session.getWaypointSession().getTeleport().isWorldTeleportable(this.displayedWorld);
         this.waypointsSorted = new ArrayList();
         this.filterValue = "";
      }
   }

   public void method_25426() {
      super.method_25426();
      if (this.displayedWorld != null) {
         this.updateSortedList();
         this.list = new List();
         this.sets = new GuiWaypointSets(true, this.displayedWorld, this.displayedWorld.getCurrentWaypointSetId());
         this.method_37063(this.deleteButton = new MyTinyButton(this.field_22789 / 2 + 129, this.field_22790 - 53, class_2561.method_43471("gui.xaero_delete"), (b) -> {
            if (this.isSomethingSelected()) {
               this.undrag();
               boolean shouldRestore = true;

               for(int i : this.selectedListSet) {
                  Waypoint w = this.list.getWaypoint(i);
                  if (!w.isEffectivelyDeleted()) {
                     shouldRestore = false;
                     w.setTemporary(true);
                  }
               }

               if (shouldRestore) {
                  for(int i : this.selectedListSet) {
                     Waypoint w = this.list.getWaypoint(i);
                     w.setTemporary(false);
                     if (w.isThirdParty()) {
                        w.setThirdPartyDeleted(false);
                     }
                  }
               }

               try {
                  this.session.getWorldManagerIO().saveWorld(this.displayedWorld);
               } catch (IOException e) {
                  MinimapLogs.LOGGER.error("suppressed exception", e);
               }

               this.session.getWorldManagerIO().getRootConfigIO().save(this.displayedWorld.getContainer().getRoot());
            }
         }));
         this.method_37063(class_4185.method_46430(class_2561.method_43469("gui.done", new Object[0]), (b) -> this.goBack()).method_46434(this.field_22789 / 2 - 100, this.field_22790 - 29, 200, 20).method_46431());
         this.method_37063(this.editButton = new MyTinyButton(this.field_22789 / 2 - 203, this.field_22790 - 53, class_2561.method_43469("gui.xaero_add_edit", new Object[0]), (b) -> {
            if (this.isAddEditEnabled()) {
               ArrayList<Waypoint> selectedWaypoints = (ArrayList)this.getSelectedWaypointsList().stream().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
               this.field_22787.method_1507(new GuiAddWaypoint(this.modMain, this.session, this, this.escape, selectedWaypoints, this.displayedWorld.getContainer().getRoot().getPath(), this.displayedWorld, this.displayedWorld.getCurrentWaypointSetId(), selectedWaypoints.isEmpty()));
               this.list.setSelected((List.WaypointEntry)null);
            }
         }));
         this.method_37063(this.teleportButton = new MyTinyButton(this.field_22789 / 2 - 120, this.field_22790 - 53, class_2561.method_43470(class_1074.method_4662("gui.xaero_waypoint_teleport", new Object[0]) + " (T)"), (b) -> {
            if (this.canTeleport()) {
               this.displayingTeleportableWorld = this.session.getWaypointSession().getTeleport().isWorldTeleportable(this.displayedWorld);
               this.session.getWaypointSession().getTeleport().teleportToWaypoint(this.list.getWaypoint((Integer)this.selectedListSet.first()), this.displayedWorld, this);
            }
         }));
         this.method_37063(this.disableEnableButton = new MyTinyButton(this.field_22789 / 2 + 46, this.field_22790 - 53, class_2561.method_43469("gui.xaero_disable_enable", new Object[0]), (b) -> {
            if (this.isSomethingSelected()) {
               ArrayList<Waypoint> selectedWaypoints = this.getSelectedWaypointsList();
               boolean effectivelyDeleted = allWaypointsAre(selectedWaypoints, Waypoint::isEffectivelyDeleted);
               if (effectivelyDeleted) {
                  for(Waypoint selected : selectedWaypoints) {
                     if (selected.isThirdParty()) {
                        selected.getThirdPartyRenderOverride().clear();
                        selected.setThirdPartyDeleted(true);
                     } else {
                        this.displayedWorld.getCurrentWaypointSet().remove(selected);
                     }
                  }

                  this.selectedListSet.clear();
               } else if (allWaypointsAre(selectedWaypoints, Waypoint::isDisabled)) {
                  for(Waypoint selected : selectedWaypoints) {
                     this.setDisabled(selected, false);
                  }
               } else {
                  for(Waypoint selected : selectedWaypoints) {
                     this.setDisabled(selected, true);
                  }
               }

               this.updateSortedList();
               if (!effectivelyDeleted && (this.isFiltering() || this.displayedWorld.getRootConfig().getSortType() != WaypointsSort.NONE)) {
                  this.selectWaypoints(selectedWaypoints);
               }

               try {
                  this.session.getWorldManagerIO().saveWorld(this.displayedWorld);
               } catch (IOException e) {
                  MinimapLogs.LOGGER.error("suppressed exception", e);
               }

               this.session.getWorldManagerIO().getRootConfigIO().save(this.displayedWorld.getContainer().getRoot());
            }
         }));
         this.method_37063(this.clearButton = new MyTinyButton(this.field_22789 / 2 + 130, 32, class_2561.method_43469("gui.xaero_clear", new Object[0]), (b) -> {
            XaeroPath worldKeys = (XaeroPath)this.worlds.getCurrentKey();
            String name = this.sets.getOptions()[this.sets.getCurrentSet()];
            if (this.shouldDeleteSet()) {
               this.field_22787.method_1507(new GuiDeleteSet(class_1074.method_4662(name, new Object[0]), worldKeys, name, this, this.escape, this.modMain, this.session));
            } else {
               this.field_22787.method_1507(new GuiClearSet(class_1074.method_4662(name, new Object[0]), worldKeys, name, this, this.escape, this.modMain, this.session));
            }

         }));
         this.method_37063(new MyTinyButton(this.field_22789 / 2 - 203, 32, class_2561.method_43469("gui.xaero_options", new Object[0]), (b) -> this.field_22787.method_1507(new GuiWaypointsOptions(this.modMain, this.session, this, this.escape, this.displayedWorld, this.frozenAutoWorldPath))));
         this.method_37063(this.shareButton = new MyTinyButton(this.field_22789 / 2 - 37, this.field_22790 - 53, class_2561.method_43469("gui.xaero_share", new Object[0]), (b) -> {
            if (this.isOneSelected()) {
               Waypoint selected = this.selectedListSet.isEmpty() ? null : this.list.getWaypoint((Integer)this.selectedListSet.first());
               if (selected != null) {
                  this.session.getWaypointSession().getSharing().shareWaypoint(this, selected, this.displayedWorld);
               }

            }
         }));
         this.method_25429(this.containersDD = this.createContainersDropdown());
         this.method_25429(this.worldsDD = this.createWorldsDropdown());
         this.method_25429(this.setsDD = this.createSetsDropdown());
         this.method_37063(this.filterBox = new class_342(this.field_22793, this.field_22789 / 2 - 203, 60, 75, 20, class_2561.method_43471("gui.xaero_waypoint_filter_box")));
         this.filterBox.method_1852(this.filterValue);
         this.filterBox.method_1863(this::onFilterTyped);
         this.method_25429(this.list);
      }
   }

   private void setDisabled(Waypoint waypoint, boolean value) {
      WaypointRenderInfo renderInfoDest = waypoint.getRenderInfoEditDest();
      renderInfoDest.setDisabled(value);
      if (waypoint != renderInfoDest) {
         renderInfoDest.nullEverythingMatching(waypoint);
      }

   }

   private DropDownWidget createSetsDropdown() {
      return Builder.begin().setOptions(this.sets.getOptions()).setX(this.field_22789 / 2 - 100).setY(33).setW(200).setSelected(this.sets.getCurrentSet()).setCallback(this).setContainer(this).setNarrationTitle(class_2561.method_43471("gui.xaero_dropdown_waypoint_set")).build();
   }

   private DropDownWidget createContainersDropdown() {
      return Builder.begin().setOptions(this.containers.options).setX(this.field_22789 / 2 - 202).setY(17).setW(200).setSelected(this.containers.current).setCallback(this).setContainer(this).setNarrationTitle(class_2561.method_43471("gui.xaero_dropdown_waypoint_container")).build();
   }

   private DropDownWidget createWorldsDropdown() {
      return Builder.begin().setOptions(this.worlds.options).setX(this.field_22789 / 2 + 2).setY(17).setW(200).setSelected(this.worlds.current).setCallback(this).setContainer(this).setNarrationTitle(class_2561.method_43471("gui.xaero_dropdown_waypoint_world")).build();
   }

   private ArrayList<Waypoint> getSelectedWaypointsList() {
      return (ArrayList)this.selectedListSet.stream().map((i) -> this.list.getWaypoint(i)).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
   }

   public static boolean allWaypointsAre(ArrayList<Waypoint> waypoints, Predicate<Waypoint> predicate) {
      boolean allTrue = true;

      for(Waypoint w : waypoints) {
         if (!predicate.test(w)) {
            allTrue = false;
            break;
         }
      }

      return allTrue;
   }

   public boolean shouldDeleteSet() {
      return !this.sets.getOptions()[this.sets.getCurrentSet()].equals("gui.xaero_default") && this.displayedWorld.getCurrentWaypointSet().isEmpty();
   }

   private void undrag() {
      this.draggingFromX = -1;
      this.draggingFromY = -1;
      this.draggingFromSlot = -1;
      this.draggingWaypoint = null;
   }

   public boolean method_25402(class_11909 event, boolean doubleClick) {
      if (event.method_74245() == 0) {
         this.leftMouseButtonDown = true;
      }

      if (this.openDropdown == null) {
         if (KeyMappingUtils.inputMatches(class_307.field_1672, event.method_74245(), MinimapKeyMappings.WAYPOINT_MENU, 0)) {
            this.goBack();
            return true;
         }

         if (event.method_74245() == 0) {
            double par1 = event.comp_4798();
            double par2 = event.comp_4799();
            if (par2 >= (double)58.0F && par2 < (double)(this.field_22790 - 61) && !this.isFiltering() && this.displayedWorld.getRootConfig().getSortType() == WaypointsSort.NONE) {
               this.draggingFromX = (int)par1;
               this.draggingFromY = (int)par2;
               this.draggingFromSlot = this.list.getEntryAt(par1, par2);
               if (this.draggingFromSlot >= this.displayedWorld.getCurrentWaypointSet().size()) {
                  this.draggingFromSlot = -1;
               }
            }
         } else {
            this.list.setSelected((List.WaypointEntry)null);
         }
      }

      if (!super.method_25402(event, doubleClick)) {
         if (this.method_25399() == this.filterBox) {
            this.method_25395((class_364)null);
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean method_25406(class_11909 event) {
      if (event.method_74245() == 0) {
         this.leftMouseButtonDown = false;
      }

      try {
         if (this.draggingWaypoint != null) {
            this.session.getWorldManagerIO().saveWorld(this.displayedWorld);
         }
      } catch (IOException e) {
         MinimapLogs.LOGGER.error("suppressed exception", e);
      }

      this.undrag();
      return !super.method_25406(event) ? this.list.method_25406(event) : true;
   }

   public boolean method_16803(class_11908 event) {
      if (this.filterBox.method_25370()) {
         return super.method_16803(event);
      } else {
         switch (event.comp_4795()) {
            case 84:
               if (this.teleportButton.field_22763) {
                  this.teleportButton.method_25348(new class_11909((double)0.0F, (double)0.0F, new class_11910(0, 0)), false);
               }

               return true;
            case 261:
               if (this.disableEnableButton.field_22763) {
                  for(int i : this.selectedListSet) {
                     this.list.getWaypoint(i).setTemporary(true);
                  }

                  this.disableEnableButton.method_25348(new class_11909((double)0.0F, (double)0.0F, new class_11910(0, 0)), false);
               }

               return true;
            default:
               return super.method_16803(event);
         }
      }
   }

   public void method_25420(class_332 guiGraphics, int par1, int par2, float par3) {
      super.method_25420(guiGraphics, par1, par2, par3);
      this.list.method_25394(guiGraphics, par1, par2, par3);
   }

   public void method_25394(class_332 guiGraphics, int par1, int par2, float par3) {
      if (this.displayedWorld == null) {
         this.field_22787.method_1507(this.parent);
      } else if (this.field_22787.field_1724 == null) {
         this.field_22787.method_1507((class_437)null);
      } else {
         this.updateButtons();
         boolean renderingFilterBoxHint = !this.filterBox.method_25370() && this.filterBox.method_1882().isEmpty();
         if (renderingFilterBoxHint) {
            this.filterResponderPaused = true;
            GuiUtils.setFieldText(this.filterBox, class_2561.method_43471("gui.xaero_waypoint_filter_box_suggestion").getString(), -11184811);
            this.filterBox.method_1883(0, false);
            this.filterResponderPaused = false;
         }

         super.method_25394(guiGraphics, par1, par2, par3);
         if (renderingFilterBoxHint) {
            this.filterResponderPaused = true;
            GuiUtils.setFieldText(this.filterBox, "", -1);
            this.filterResponderPaused = false;
         }

         this.renderHoveredWaypointDistance(guiGraphics, par1, par2);
      }
   }

   private void renderHoveredWaypointDistance(class_332 guiGraphics, int mouseX, int mouseY) {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      boolean configValue = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_DISTANCE_IN_MENU);
      if (configValue) {
         int hoveredEntry = this.list.getEntryAt((double)mouseX, (double)mouseY);
         if (hoveredEntry != -1 && !this.leftMouseButtonDown) {
            class_1297 renderEntity = this.field_22787.method_1560();
            if (renderEntity != null) {
               float partialTicks = this.field_22787.method_61966().method_60637(true);
               if (this.field_22787.method_1493()) {
                  partialTicks = 1.0F;
               }

               Waypoint hoveredWaypoint = this.list.getWaypoint(hoveredEntry);
               double waypointCoordinateScale = this.session.getDimensionHelper().getDimCoordinateScale(this.displayedWorld);
               String distanceText = WaypointUtils.getDistanceTextForCurrentWorld(hoveredWaypoint, waypointCoordinateScale, EntityUtils.getEntityX(renderEntity, partialTicks), EntityUtils.getEntityY(renderEntity, partialTicks), EntityUtils.getEntityZ(renderEntity, partialTicks));
               Tooltip distanceTooltip = this.previousDistanceTooltip;
               if (!distanceText.equals(this.previousDistanceText)) {
                  distanceTooltip = new Tooltip(distanceText);
                  this.previousDistanceText = distanceText;
                  this.previousDistanceTooltip = distanceTooltip;
               }

               if (this.openDropdown == null || !this.openDropdown.method_49606()) {
                  distanceTooltip.drawBox(guiGraphics, mouseX, mouseY, this.field_22789, this.field_22790);
               }
            }
         }
      }
   }

   protected void renderPreDropdown(class_332 guiGraphics, int mouseX, int mouseY, float partial) {
      super.renderPreDropdown(guiGraphics, mouseX, mouseY, partial);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_world_server", new Object[0]), this.field_22789 / 2 - 102, 5, -1);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_subworld_dimension", new Object[0]), this.field_22789 / 2 + 102, 5, -1);
      if (this.draggingFromSlot != -1) {
         int distance = (int)Math.sqrt(Math.pow((double)(mouseX - this.draggingFromX), (double)2.0F) + Math.pow((double)(mouseY - this.draggingFromY), (double)2.0F));
         int toSlot = Math.min(this.displayedWorld.getCurrentWaypointSet().size() - 1, this.list.getEntryAt((double)mouseX, (double)mouseY));
         if (distance > 4 && this.draggingWaypoint == null) {
            this.draggingWaypoint = this.displayedWorld.getCurrentWaypointSet().get(this.draggingFromSlot);
            this.list.setSelected((List.WaypointEntry)null);
         }

         if (this.draggingWaypoint != null && this.draggingFromSlot != toSlot && toSlot != -1) {
            int direction = toSlot > this.draggingFromSlot ? 1 : -1;

            for(int i = this.draggingFromSlot; i != toSlot; i += direction) {
               this.displayedWorld.getCurrentWaypointSet().set(i, this.displayedWorld.getCurrentWaypointSet().get(i + direction));
            }

            this.displayedWorld.getCurrentWaypointSet().set(toSlot, this.draggingWaypoint);
            this.draggingFromSlot = toSlot;
            this.updateSortedList();
         }

         int fromCenter = this.draggingFromX - this.list.method_25368() / 2;
         this.list.drawWaypointSlot(guiGraphics, this.draggingWaypoint, mouseX - 108 - fromCenter, mouseY - this.list.getItemHeight() / 4);
      }

   }

   private void updateButtons() {
      this.deleteButton.field_22763 = this.disableEnableButton.field_22763 = this.isSomethingSelected();
      this.shareButton.field_22763 = this.isOneSelected();
      this.teleportButton.field_22763 = this.canTeleport();
      this.editButton.field_22763 = this.isAddEditEnabled();
      this.clearButton.method_25355(class_2561.method_43469(this.shouldDeleteSet() ? "gui.xaero_delete_set" : "gui.xaero_clear", new Object[0]));
      ArrayList<Waypoint> selectedWaypointsList = this.getSelectedWaypointsList();
      if (this.isSomethingSelected() && allWaypointsAre(selectedWaypointsList, Waypoint::isEffectivelyDeleted)) {
         this.disableEnableButton.method_25355(class_2561.method_43471("gui.xaero_delete"));
         this.disableEnableButton.field_22763 = !allWaypointsAre(selectedWaypointsList, Waypoint::isThirdPartyDeleted);
         this.deleteButton.method_25355(class_2561.method_43471("gui.xaero_restore"));
      } else {
         this.deleteButton.method_25355(class_2561.method_43471("gui.xaero_delete"));
         String[] enabledisable = class_1074.method_4662("gui.xaero_disable_enable", new Object[0]).split("/");
         this.disableEnableButton.method_25355(class_2561.method_43470(enabledisable[!allWaypointsAre(selectedWaypointsList, Waypoint::isDisabled) ? 0 : 1]));
      }

   }

   private boolean isAddEditEnabled() {
      return true;
   }

   private boolean isSomethingSelected() {
      return !this.selectedListSet.isEmpty();
   }

   private boolean isOneSelected() {
      return this.selectedListSet.size() == 1;
   }

   public void clearSelection() {
      this.selectedListSet.clear();
   }

   private boolean canTeleport() {
      if (!this.isOneSelected()) {
         return false;
      } else if (!this.displayedWorld.getRootConfig().isTeleportationEnabled()) {
         return false;
      } else if (this.displayingTeleportableWorld) {
         return true;
      } else {
         ClientConfigManager configManager = this.modMain.getHudConfigs().getClientConfigManager();
         SingleConfigManager<Config> primaryConfigManager = configManager.getPrimaryConfigManager();
         return (Boolean)primaryConfigManager.getEffective(MinimapPrimaryClientConfigOptions.WRONG_WORLD_TELEPORT);
      }
   }

   public boolean onSelected(DropDownWidget menu, int selectedIndex) {
      this.clearFilter(false);
      if (menu != this.containersDD && menu != this.worldsDD) {
         if (menu == this.setsDD) {
            this.list.setSelected((List.WaypointEntry)null);
            if (selectedIndex == menu.size() - 1) {
               MinimapLogs.LOGGER.info("New waypoint set gui");
               this.field_22787.method_1507(new GuiNewSet(this.modMain, this.session, this, this.escape, this.displayedWorld));
               return false;
            } else {
               this.sets.setCurrentSet(selectedIndex);
               this.displayedWorld.setCurrentWaypointSetId(this.sets.getCurrentSetKey());
               this.updateSortedList();

               try {
                  this.session.getWorldManagerIO().saveWorld(this.displayedWorld);
               } catch (IOException e) {
                  MinimapLogs.LOGGER.error("suppressed exception", e);
               }

               return true;
            }
         } else {
            return false;
         }
      } else {
         if (menu == this.containersDD) {
            this.containers.current = selectedIndex;
            if (this.containers.current != this.containers.auto) {
               MinimapWorld firstWorld = this.manager.getRootWorldContainer((String)this.containers.getCurrentKey()).getFirstWorld();
               this.session.getWorldState().setCustomWorldPath(firstWorld.getFullPath());
            } else {
               this.session.getWorldState().setCustomWorldPath((XaeroPath)null);
            }

            this.displayedWorld = this.manager.getCurrentWorld(this.frozenAutoWorldPath);
            this.updateSortedList();
            this.worlds = new GuiWaypointWorlds(this.manager.getRootWorldContainer((String)this.containers.getCurrentKey()), this.session, this.displayedWorld.getFullPath(), this.frozenAutoWorldPath);
            this.replaceWidget(this.worldsDD, this.worldsDD = this.createWorldsDropdown());
         } else {
            this.worlds.current = selectedIndex;
            if (this.worlds.current != this.worlds.auto) {
               XaeroPath selectedWorldPath = (XaeroPath)this.worlds.getCurrentKey();
               this.session.getWorldState().setCustomWorldPath(selectedWorldPath);
            } else {
               this.session.getWorldState().setCustomWorldPath((XaeroPath)null);
            }

            this.displayedWorld = this.manager.getCurrentWorld(this.frozenAutoWorldPath);
            this.updateSortedList();
         }

         this.displayingTeleportableWorld = this.session.getWaypointSession().getTeleport().isWorldTeleportable(this.displayedWorld);
         this.list.setSelected((List.WaypointEntry)null);
         this.sets = new GuiWaypointSets(true, this.displayedWorld, this.displayedWorld.getCurrentWaypointSetId());
         this.replaceWidget(this.setsDD, this.setsDD = this.createSetsDropdown());
         return true;
      }
   }

   private void updateSortedList() {
      WaypointsSort sortType = this.displayedWorld.getRootConfig().getSortType();
      this.waypointsSorted = new ArrayList();
      this.thirdPartyWaypointsSorted = new ArrayList();
      ThirdPartyWaypointManager thirdPartyWaypointManager = this.displayedWorld.getContainer().getThirdPartyWaypointManager();
      if (sortType == WaypointsSort.NONE && !this.isFiltering()) {
         for(Waypoint waypoint : this.displayedWorld.getCurrentWaypointSet().getWaypoints()) {
            this.waypointsSorted.add(waypoint);
         }

         if ("gui.xaero_default".equals(this.displayedWorld.getCurrentWaypointSetId())) {
            for(ThirdPartyWaypoints thirdPartyWaypoints : thirdPartyWaypointManager.getAll()) {
               if (thirdPartyWaypoints.isEnabled()) {
                  for(Waypoint waypoint : thirdPartyWaypoints.getWaypoints().values()) {
                     if (showingThirdPartyDeleted || !waypoint.isThirdPartyDeleted()) {
                        this.thirdPartyWaypointsSorted.add(waypoint);
                     }
                  }
               }
            }
         }

      } else {
         distanceDivided = this.session.getDimensionHelper().getDimensionDivision(this.displayedWorld);
         boolean reversed = this.displayedWorld.getRootConfig().isSortReversed();
         ArrayList<KeySortableByOther<Waypoint>> sortableKeys = new ArrayList();
         class_4184 camera = this.field_22787.field_1773.method_19418();

         for(Waypoint w : this.displayedWorld.getCurrentWaypointSet().getWaypoints()) {
            int filterMatch = this.getWaypointFilterMatch(w);
            if (filterMatch != -1) {
               Comparable<?> sortObject = this.getSortObject(w, filterMatch, camera, sortType);
               sortableKeys.add(new KeySortableByOther(w, new Comparable[]{reversed != w.isDisabled(), sortObject}));
            }
         }

         Collections.sort(sortableKeys);

         for(KeySortableByOther<Waypoint> k : sortableKeys) {
            this.waypointsSorted.add((Waypoint)k.getKey());
         }

         if (reversed) {
            Collections.reverse(this.waypointsSorted);
         }

         if ("gui.xaero_default".equals(this.displayedWorld.getCurrentWaypointSetId())) {
            for(ThirdPartyWaypoints thirdPartyWaypoints : thirdPartyWaypointManager.getAll()) {
               if (thirdPartyWaypoints.isEnabled()) {
                  sortableKeys.clear();

                  for(Waypoint w : thirdPartyWaypoints.getWaypoints().values()) {
                     if (showingThirdPartyDeleted || !w.isThirdPartyDeleted()) {
                        int filterMatch = this.getWaypointFilterMatch(w);
                        if (filterMatch != -1) {
                           Comparable<?> sortObject = this.getSortObject(w, filterMatch, camera, sortType);
                           sortableKeys.add(new KeySortableByOther(w, new Comparable[]{reversed != w.isDisabled(), sortObject}));
                        }
                     }
                  }

                  Collections.sort(sortableKeys);
                  if (reversed) {
                     Collections.reverse(sortableKeys);
                  }

                  for(KeySortableByOther<Waypoint> k : sortableKeys) {
                     this.thirdPartyWaypointsSorted.add((Waypoint)k.getKey());
                  }
               }
            }

         }
      }
   }

   private Comparable<?> getSortObject(Waypoint w, int filterMatch, class_4184 camera, WaypointsSort sortType) {
      if (this.isFiltering()) {
         return filterMatch;
      } else if (sortType == WaypointsSort.COLOR) {
         return w.getWaypointColor();
      } else if (sortType == WaypointsSort.ANGLE) {
         return -w.getComparisonAngleCos(camera, distanceDivided);
      } else if (sortType == WaypointsSort.NAME) {
         return w.getComparisonName();
      } else {
         return (Comparable<?>)(sortType == WaypointsSort.SYMBOL ? w.getInitials() : w.getComparisonDistance(camera, distanceDivided));
      }
   }

   private boolean isFiltering() {
      return this.filterValue != null && !this.filterValue.isEmpty();
   }

   private int getWaypointFilterMatch(Waypoint w) {
      if (this.filterValue != null && !this.filterValue.isEmpty()) {
         String localizedName = w.getLocalizedName();
         if (localizedName == null) {
            return -1;
         } else {
            int filterValuePos = localizedName.indexOf(this.filterValue);
            if (filterValuePos == 0) {
               return 0;
            } else {
               String lowerCaseName = localizedName.toLowerCase();
               String lowerCaseFilter = this.filterValue.toLowerCase();
               int caselessFilterValuePos = lowerCaseName.indexOf(lowerCaseFilter);
               if (caselessFilterValuePos == 0) {
                  return 1;
               } else if (filterValuePos != -1) {
                  return 2 + filterValuePos;
               } else {
                  return caselessFilterValuePos != -1 ? 1000000 + caselessFilterValuePos : -1;
               }
            }
         }
      } else {
         return 0;
      }
   }

   private void onFilterTyped(String s) {
      if (!this.filterResponderPaused) {
         if (!s.equals(this.filterValue)) {
            this.filterValue = s;
            this.updateSortedList();
            this.list.method_44382((double)0.0F);
            this.selectedListSet.clear();
         }
      }
   }

   public boolean method_25404(class_11908 event) {
      int par1 = event.comp_4795();
      int par2 = event.comp_4796();
      if (!super.method_25404(event)) {
         if (this.filterBox.method_25370()) {
            return false;
         } else if (KeyMappingUtils.inputMatches(par1 != -1 ? class_307.field_1668 : class_307.field_1671, par1 != -1 ? par1 : par2, MinimapKeyMappings.WAYPOINT_MENU, 0)) {
            this.goBack();
            return true;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public void clearFilter(boolean callResponder) {
      this.filterValue = "";
      if (!callResponder) {
         this.filterResponderPaused = true;
      }

      this.filterBox.method_1852(this.filterValue);
      if (!callResponder) {
         this.filterResponderPaused = false;
      }

   }

   private void selectWaypoints(ArrayList<Waypoint> ws) {
      this.selectedListSet.clear();
      boolean scrolled = false;

      for(Waypoint selectedWaypoint : ws) {
         int index;
         if (selectedWaypoint.isThirdParty()) {
            index = this.waypointsSorted.size() + this.thirdPartyWaypointsSorted.indexOf(selectedWaypoint);
         } else {
            index = this.waypointsSorted.indexOf(selectedWaypoint);
         }

         this.selectedListSet.add(index);
         if (!scrolled) {
            this.list.ensureVisible(index);
            scrolled = true;
         }
      }

   }

   class List extends class_4280<WaypointEntry> {
      private int createdCount;

      public List() {
         super(GuiWaypoints.this.field_22787, GuiWaypoints.this.field_22789, Math.max(4, GuiWaypoints.this.field_22790 - 61 - 58), 58, 18);
         this.createEntries(this.getWaypointCount());
      }

      private int getThirdPartyCount() {
         return GuiWaypoints.this.displayedWorld.getContainer().getThirdPartyWaypointManager().getCount();
      }

      private Waypoint getThirdPartyWaypoint(int index) {
         return (Waypoint)GuiWaypoints.this.thirdPartyWaypointsSorted.get(index);
      }

      protected int getWaypointCount() {
         int size = GuiWaypoints.this.waypointsSorted.size();
         size += GuiWaypoints.this.thirdPartyWaypointsSorted.size();
         return size;
      }

      private Waypoint getWaypoint(int slotIndex) {
         if (slotIndex < GuiWaypoints.this.waypointsSorted.size()) {
            return (Waypoint)GuiWaypoints.this.waypointsSorted.get(slotIndex);
         } else {
            int thirdPartyWPIndex = slotIndex - GuiWaypoints.this.waypointsSorted.size();
            return thirdPartyWPIndex < this.getThirdPartyCount() ? this.getThirdPartyWaypoint(thirdPartyWPIndex) : null;
         }
      }

      protected boolean method_73379() {
         return false;
      }

      protected boolean isSelectedItem(int p_148131_1_) {
         return !GuiWaypoints.this.selectedListSet.isEmpty() && GuiWaypoints.this.selectedListSet.contains(p_148131_1_);
      }

      private void createEntries(int count) {
         this.method_25339();
         this.createdCount = count;

         for(int i = 0; i < count; ++i) {
            WaypointEntry entry = new WaypointEntry(i);
            this.method_25321(entry);
         }

      }

      public void method_48579(class_332 guiGraphics, int p_render_1_, int p_render_2_, float p_render_3_) {
         int currentCount = this.getWaypointCount();
         if (currentCount != this.createdCount) {
            this.createEntries(currentCount);
            this.method_44382(this.method_44387());
         }

         super.method_48579(guiGraphics, p_render_1_, p_render_2_, p_render_3_);
      }

      protected void renderItem(class_332 $$0, int mouseX, int mouseY, float partialTicks, WaypointEntry entry) {
         if (this.isSelectedItem(entry.index)) {
            int selectionColor = this.method_25370() ? -1 : -8355712;
            this.method_44398($$0, entry, selectionColor);
         }

         super.method_44397($$0, mouseX, mouseY, partialTicks, entry);
      }

      public boolean method_25370() {
         if (GuiWaypoints.this.openDropdown == null && GuiWaypoints.this.draggingWaypoint == null) {
            return GuiWaypoints.this.method_25399() == this;
         } else {
            return false;
         }
      }

      public void setSelected(WaypointEntry e) {
         if (e == null) {
            GuiWaypoints.this.selectedListSet.clear();
            GuiWaypoints.this.shiftSelectFirst = -1;
         } else {
            this.getWaypoint(e.index);
            int currentSize = GuiWaypoints.this.selectedListSet.size();
            boolean shiftPressed = ScreenBase.hasShiftDown();
            if ((currentSize > 1 || currentSize == 1 && (Integer)GuiWaypoints.this.selectedListSet.first() != e.index) && !ScreenBase.hasControlDown() && !shiftPressed) {
               GuiWaypoints.this.selectedListSet.clear();
            }

            if (currentSize > 0 && shiftPressed) {
               int direction = e.index > GuiWaypoints.this.shiftSelectFirst ? 1 : -1;
               GuiWaypoints.this.selectedListSet.clear();

               for(int i = GuiWaypoints.this.shiftSelectFirst; i != e.index + direction; i += direction) {
                  GuiWaypoints.this.selectedListSet.add(i);
               }
            } else if (GuiWaypoints.this.selectedListSet.contains(e.index)) {
               GuiWaypoints.this.selectedListSet.remove(e.index);
            } else {
               GuiWaypoints.this.shiftSelectFirst = e.index;
               GuiWaypoints.this.selectedListSet.add(e.index);
            }

            super.method_25313(GuiWaypoints.this.selectedListSet.isEmpty() ? null : e);
         }
      }

      public int getItemHeight() {
         return this.field_62109;
      }

      public void drawWaypointSlot(class_332 guiGraphics, Waypoint w, int p_180791_2_, int p_180791_3_) {
         if (w != null) {
            String stateIndicator = w.isThirdPartyDeleted() ? " §4" + class_1074.method_4662("gui.xaero_deleted", new Object[0]) : (w.isDisabled() ? " §4" + class_1074.method_4662("gui.xaero_disabled", new Object[0]) : (w.isTemporary() ? " §4" + class_1074.method_4662("gui.xaero_temporary", new Object[0]) : ""));
            guiGraphics.method_25300(GuiWaypoints.this.field_22793, w.getLocalizedName() + stateIndicator, p_180791_2_ + 110, p_180791_3_ + 1, -1);
            int rectX = p_180791_2_ + 8 + 4;
            int rectY = p_180791_3_ + 6;
            if (w.isGlobal()) {
               guiGraphics.method_25300(GuiWaypoints.this.field_22793, "*", rectX - 25, rectY - 3, -1);
            }

            if (w.isThirdParty()) {
               guiGraphics.method_27535(GuiWaypoints.this.field_22793, class_2561.method_43470(w.getThirdPartyOrigin().toString()).method_27692(class_124.field_1063), p_180791_2_ + 250, p_180791_3_ + 1, -1);
            }

            int opacity = 90;
            GuiWaypoints.this.modMain.getMinimap().getWaypointMapRenderer().drawIconGUI(guiGraphics, w, rectX, rectY, opacity);
         }
      }

      public int getEntryAt(double x, double y) {
         WaypointEntry entry = (WaypointEntry)this.method_25308(x, y);
         return entry == null ? -1 : entry.index;
      }

      public void ensureVisible(int index) {
         this.method_73377((WaypointEntry)this.method_25396().get(index));
      }

      public void recreateEntries() {
         this.createEntries(this.getWaypointCount());
      }

      public boolean method_25402(class_11909 event, boolean doubleClick) {
         if (GuiWaypoints.this.method_25399() == GuiWaypoints.this.filterBox) {
            GuiWaypoints.this.method_25395((class_364)null);
         }

         return super.method_25402(event, doubleClick);
      }

      public class WaypointEntry extends class_4280.class_4281<WaypointEntry> {
         private int index;

         public WaypointEntry(int index) {
            this.index = index;
         }

         public void method_25343(class_332 guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            Waypoint w = List.this.getWaypoint(this.index);
            if (w != GuiWaypoints.this.draggingWaypoint) {
               List.this.drawWaypointSlot(guiGraphics, w, this.method_46426(), this.method_46427() + 2);
            }
         }

         public boolean method_25402(class_11909 event, boolean doubleClick) {
            return event.method_74245() == 0;
         }

         public class_2561 method_37006() {
            Waypoint w = List.this.getWaypoint(this.index);
            String narration = "";
            if (w != null) {
               String var10000 = w.isThirdPartyDeleted() ? " " + class_1074.method_4662("gui.xaero_deleted", new Object[0]) : "";
               String stateIndicators = var10000 + (w.isDisabled() ? " " + class_1074.method_4662("gui.xaero_disabled", new Object[0]) : "") + (w.isTemporary() ? " " + class_1074.method_4662("gui.xaero_temporary", new Object[0]) : "");
               narration = narration + class_1074.method_4662("narrator.select", new Object[]{w.getName()}) + stateIndicators + ", ";
            }

            if (GuiWaypoints.this.selectedListSet.size() != 1) {
               Object[] var10002 = new Object[1];
               String var10005 = class_1074.method_4662("gui.xaero_waypoints", new Object[0]);
               var10002[0] = var10005 + " " + GuiWaypoints.this.selectedListSet.size();
               narration = narration + class_1074.method_4662("narrator.select", var10002);
            }

            return class_2561.method_43470(narration);
         }
      }
   }
}
