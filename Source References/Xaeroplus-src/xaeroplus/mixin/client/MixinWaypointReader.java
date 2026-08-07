package xaeroplus.mixin.client;

import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.class_309;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.lib.client.controls.util.KeyMappingUtils;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointReader;
import xaeroplus.settings.Settings;
import xaeroplus.util.BaritoneExecutor;
import xaeroplus.util.BaritoneHelper;

@Mixin(
   value = {WaypointReader.class},
   remap = false
)
public class MixinWaypointReader {
   @Inject(
      method = {"getRightClickOptions(Lxaero/map/mods/gui/Waypoint;Lxaero/map/gui/IRightClickableElement;)Ljava/util/ArrayList;"},
      at = {@At("RETURN")}
   )
   public void getRightClickOptionsReturn(final Waypoint element, final IRightClickableElement target, final CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
      if (Settings.REGISTRY.worldMapUIAdditions.get()) {
         ArrayList<RightClickOption> options = (ArrayList)cir.getReturnValue();
         int index = 3;
         options.add(index++, new RightClickOption("xaeroplus.gui.world_map.copy_coordinates", options.size(), target) {
            public void onAction(final class_437 screen) {
               class_309 var10000 = class_310.method_1551().field_1774;
               int var10001 = element.getX();
               var10000.method_1455(var10001 + " " + element.getY() + " " + element.getZ());
            }
         });
         if (BaritoneHelper.isBaritonePresent()) {
            final int goalX = class_3532.method_15357(element.getRenderX() - (double)0.5F);
            final int goalZ = class_3532.method_15357(element.getRenderZ() - (double)0.5F);
            final boolean isYPresent = element.isyIncluded();
            final int goalY = isYPresent ? element.getY() : 64;
            options.add(index++, (new RightClickOption("xaeroplus.gui.world_map.baritone_goal_here", options.size(), target) {
               public void onAction(class_437 screen) {
                  if (isYPresent) {
                     BaritoneExecutor.goal(goalX, goalY, goalZ);
                  } else {
                     BaritoneExecutor.goal(goalX, goalZ);
                  }

               }
            }).setNameFormatArgs(new Object[]{KeyMappingUtils.getKeyName(Settings.REGISTRY.worldMapBaritoneGoalHereKeybindSetting.getKeyBinding())}));
            options.add(index++, (new RightClickOption("xaeroplus.gui.world_map.baritone_path_here", options.size(), target) {
               public void onAction(class_437 screen) {
                  if (isYPresent) {
                     BaritoneExecutor.path(goalX, goalY, goalZ);
                  } else {
                     BaritoneExecutor.path(goalX, goalZ);
                  }

               }
            }).setNameFormatArgs(new Object[]{KeyMappingUtils.getKeyName(Settings.REGISTRY.worldMapBaritonePathHereKeybindSetting.getKeyBinding())}));
            if (BaritoneHelper.isBaritoneElytraPresent()) {
               options.addAll(5, Arrays.asList((new RightClickOption("xaeroplus.gui.world_map.baritone_elytra_here", options.size(), target) {
                  public void onAction(class_437 screen) {
                     if (isYPresent) {
                        BaritoneExecutor.elytra(goalX, goalY, goalZ);
                     } else {
                        BaritoneExecutor.elytra(goalX, goalZ);
                     }

                  }
               }).setNameFormatArgs(new Object[]{KeyMappingUtils.getKeyName(Settings.REGISTRY.worldMapBaritoneElytraHereKeybindSetting.getKeyBinding())})));
            }
         }

         if (Settings.REGISTRY.disableWaypointSharing.get()) {
            options.removeIf((option) -> ((AccessorRightClickOption)option).invokeGetName().equals("gui.xaero_right_click_waypoint_share"));
         }

         if (Settings.REGISTRY.disableTeleportation.get()) {
            options.removeIf((option) -> ((AccessorRightClickOption)option).invokeGetName().equals("gui.xaero_right_click_waypoint_teleport"));
         }

      }
   }
}
