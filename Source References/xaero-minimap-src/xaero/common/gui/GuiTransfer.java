package xaero.common.gui;

import net.minecraft.class_1074;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.MinimapWorldManager;
import xaero.hud.path.XaeroPath;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.MySmallButton;
import xaero.lib.client.gui.widget.dropdown.DropDownWidget;
import xaero.lib.client.gui.widget.dropdown.IDropDownWidgetCallback;
import xaero.lib.client.gui.widget.dropdown.DropDownWidget.Builder;

public class GuiTransfer extends ScreenBase implements IDropDownWidgetCallback {
   private MySmallButton transferButton;
   private GuiWaypointContainers containers1;
   private GuiWaypointWorlds worlds1;
   private GuiWaypointContainers containers2;
   private GuiWaypointWorlds worlds2;
   private DropDownWidget containers1DD;
   private DropDownWidget worlds1DD;
   private DropDownWidget containers2DD;
   private DropDownWidget worlds2DD;
   private MinimapSession session;
   private MinimapWorldManager manager;
   private final XaeroPath frozenAutoWorldPath;
   private boolean dropped = false;
   private final IXaeroMinimap modMain;

   public GuiTransfer(IXaeroMinimap modMain, MinimapSession session, class_437 par1, class_437 escapeScreen) {
      super(par1, escapeScreen, class_2561.method_43471("gui.xaero_transfer_all"));
      this.modMain = modMain;
      this.session = session;
      this.manager = session.getWorldManager();
      this.frozenAutoWorldPath = session.getWorldState().getAutoWorldPath();
      XaeroPath currentWorldPath = session.getWorldState().getCurrentWorldPath(this.frozenAutoWorldPath);
      XaeroPath currentContainerPath = currentWorldPath.getRoot();
      this.containers1 = new GuiWaypointContainers((HudMod)modMain, this.manager, currentContainerPath, this.frozenAutoWorldPath);
      this.containers2 = new GuiWaypointContainers((HudMod)modMain, this.manager, currentContainerPath, this.frozenAutoWorldPath);
      this.worlds1 = new GuiWaypointWorlds(this.manager.getRootWorldContainer((String)this.containers1.getCurrentKey()), session, currentWorldPath, this.frozenAutoWorldPath);
      this.worlds2 = new GuiWaypointWorlds(this.manager.getRootWorldContainer((String)this.containers2.getCurrentKey()), session, currentWorldPath, this.frozenAutoWorldPath);
   }

   public void method_25426() {
      super.method_25426();
      this.method_37063(this.transferButton = new MySmallButton(5, this.field_22789 / 2 - 155, this.field_22790 / 7 + 120, class_2561.method_43469("gui.xaero_transfer", new Object[0]), (b) -> this.transfer()));
      this.transferButton.field_22763 = false;
      this.method_37063(new MySmallButton(6, this.field_22789 / 2 + 5, this.field_22790 / 7 + 120, class_2561.method_43469("gui.xaero_cancel", new Object[0]), (b) -> this.openParent()));
      this.method_25429(this.worlds1DD = this.createWorlds1DD());
      this.method_25429(this.worlds2DD = this.createWorlds2DD());
      this.method_25429(this.containers1DD = this.createContainers1DD());
      this.method_25429(this.containers2DD = this.createContainers2DD());
   }

   private DropDownWidget createWorlds1DD() {
      return Builder.begin().setOptions(this.worlds1.options).setX(this.field_22789 / 2 + 2).setY(this.field_22790 / 7 + 20).setW(200).setSelected(this.worlds1.current).setCallback(this).setContainer(this).setNarrationTitle(class_2561.method_43471("gui.xaero_dropdown_transfer_worlds1")).build();
   }

   private DropDownWidget createWorlds2DD() {
      return Builder.begin().setOptions(this.worlds2.options).setX(this.field_22789 / 2 + 2).setY(this.field_22790 / 7 + 50).setW(200).setSelected(this.worlds2.current).setCallback(this).setContainer(this).setNarrationTitle(class_2561.method_43471("gui.xaero_dropdown_transfer_worlds2")).build();
   }

   private DropDownWidget createContainers1DD() {
      return Builder.begin().setOptions(this.containers1.options).setX(this.field_22789 / 2 - 202).setY(this.field_22790 / 7 + 20).setW(200).setSelected(this.containers1.current).setCallback(this).setContainer(this).setNarrationTitle(class_2561.method_43471("gui.xaero_dropdown_transfer_containers1")).build();
   }

   private DropDownWidget createContainers2DD() {
      return Builder.begin().setOptions(this.containers2.options).setX(this.field_22789 / 2 - 202).setY(this.field_22790 / 7 + 50).setW(200).setSelected(this.containers2.current).setCallback(this).setContainer(this).setNarrationTitle(class_2561.method_43471("gui.xaero_dropdown_transfer_containers2")).build();
   }

   private void openParent() {
      if (this.parent instanceof GuiWaypoints) {
         this.field_22787.method_1507(new GuiWaypoints((HudMod)this.modMain, this.session, ((GuiWaypoints)this.parent).parent, this.escape));
      } else {
         this.goBack();
      }

   }

   public void transfer() {
      try {
         XaeroPath keys1 = (XaeroPath)this.worlds1.getCurrentKey();
         XaeroPath keys2 = (XaeroPath)this.worlds2.getCurrentKey();
         MinimapWorld from = this.manager.getWorld(keys1);
         MinimapWorld to = this.manager.getWorld(keys2);

         for(WaypointSet fromSet : from.getIterableWaypointSets()) {
            WaypointSet toSet = to.getWaypointSet(fromSet.getName());
            if (toSet == null) {
               toSet = WaypointSet.Builder.begin().setName(fromSet.getName()).build();
            }

            for(Waypoint w : fromSet.getWaypoints()) {
               Waypoint copy = new Waypoint(w.getX(), w.getY(), w.getZ(), w.getName(), w.getInitials(), w.getWaypointColor(), w.getPurpose(), w.isTemporary(), w.isYIncluded());
               copy.setRotation(w.isRotation());
               copy.setDisabled(w.isDisabled());
               copy.setYaw(w.getYaw());
               copy.setVisibility(w.getVisibility());
               toSet.add(copy);
            }

            to.addWaypointSet(toSet);
         }

         if (keys2 != null) {
            this.session.getWorldState().setCustomWorldPath(keys2);
         }

         this.openParent();
         this.session.getWorldManagerIO().saveWorld(to);
      } catch (Exception e) {
         MinimapLogs.LOGGER.error("suppressed exception", e);
      }

   }

   protected void renderPreDropdown(class_332 guiGraphics, int mouseX, int mouseY, float partial) {
      super.renderPreDropdown(guiGraphics, mouseX, mouseY, partial);
      guiGraphics.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 5, -1);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_from", new Object[0]).replace("§§", ":") + ":", this.field_22789 / 2, this.field_22790 / 7 + 10, -1);
      guiGraphics.method_25300(this.field_22793, class_1074.method_4662("gui.xaero_to", new Object[0]).replace("§§", ":") + ":", this.field_22789 / 2, this.field_22790 / 7 + 40, -1);
   }

   public boolean onSelected(DropDownWidget menu, int selected) {
      if (menu == this.containers1DD) {
         this.containers1.current = selected;
         this.worlds1 = new GuiWaypointWorlds(this.manager.getRootWorldContainer((String)this.containers1.getCurrentKey()), this.session, this.session.getWorldState().getCurrentWorldPath(this.frozenAutoWorldPath), this.frozenAutoWorldPath);
         this.replaceWidget(this.worlds1DD, this.worlds1DD = this.createWorlds1DD());
      } else if (menu == this.containers2DD) {
         this.containers2.current = selected;
         this.worlds2 = new GuiWaypointWorlds(this.manager.getRootWorldContainer((String)this.containers2.getCurrentKey()), this.session, this.session.getWorldState().getCurrentWorldPath(this.frozenAutoWorldPath), this.frozenAutoWorldPath);
         this.replaceWidget(this.worlds2DD, this.worlds2DD = this.createWorlds2DD());
      } else if (menu == this.worlds1DD) {
         this.worlds1.current = selected;
      } else if (menu == this.worlds2DD) {
         this.worlds2.current = selected;
      }

      this.transferButton.field_22763 = this.containers1.current != this.containers2.current || this.worlds1.current != this.worlds2.current;
      return true;
   }
}
