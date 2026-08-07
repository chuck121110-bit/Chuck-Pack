package xaeroplus.mixin.client;

import java.util.ArrayList;
import net.minecraft.class_2561;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.gui.GuiWaypoints;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.Tooltip;
import xaeroplus.Globals;
import xaeroplus.feature.extensions.MinimapGuiTexturedButton;
import xaeroplus.settings.Settings;

@Mixin(
   value = {GuiWaypoints.class},
   remap = false
)
public abstract class MixinGuiWaypoints extends ScreenBase {
   @Unique
   private MinimapGuiTexturedButton toggleAllButton;
   @Shadow
   private MinimapWorld displayedWorld;
   @Shadow
   private ArrayList<Waypoint> waypointsSorted;

   protected MixinGuiWaypoints(final class_437 parent, final class_437 escape, final class_2561 titleIn) {
      super(parent, escape, titleIn);
   }

   @Shadow
   protected abstract boolean isOneSelected();

   @Shadow
   private void updateSortedList() {
   }

   @Inject(
      method = {"method_25426"},
      at = {@At("HEAD")},
      remap = true
   )
   public void initGui(CallbackInfo ci) {
      this.toggleAllButton = new MinimapGuiTexturedButton(this.field_22789 / 2 + 182, this.field_22790 - 29, 20, 20, 2, 18, 17, 17, Globals.guiTextures, (b) -> {
         this.waypointsSorted.stream().findFirst().ifPresent((firstWaypoint) -> {
            boolean firstIsEnabled = firstWaypoint.isDisabled();
            this.waypointsSorted.forEach((waypoint) -> waypoint.setDisabled(!firstIsEnabled));
         });
         this.updateSortedList();
      }, () -> new Tooltip(class_2561.method_43470("[XP] ").method_10852(class_2561.method_43471("xaeroplus.gui.waypoints.toggle_enable_all"))), 256, 256);
      if (Settings.REGISTRY.waypointsListUIAdditions.get()) {
         this.method_37063(this.toggleAllButton);
      }
   }

   @Redirect(
      method = {"updateButtons"},
      at = @At(
   value = "INVOKE",
   target = "Lxaero/common/gui/GuiWaypoints;isOneSelected()Z"
)
   )
   public boolean shareButtonRedirect(final GuiWaypoints instance) {
      return Settings.REGISTRY.disableWaypointSharing.get() ? false : this.isOneSelected();
   }
}
