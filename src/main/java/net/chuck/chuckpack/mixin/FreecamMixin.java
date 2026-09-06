package net.chuck.chuckpack.mixin;

import baritone.api.pathing.goals.GoalBlock;
import meteordevelopment.meteorclient.pathing.PathManagers;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(meteordevelopment.meteorclient.systems.modules.render.Freecam.class)
public class FreecamMixin {
    // Vanilla Meteor Freecam.setGoal() does PathManagers.get().moveTo(pos) which is GoalGetToBlock (via moveTo(pos,false))
    // goto X Y Z uses GoalBlock exact. Make freecam click use exact GoalBlock like goto.
    @Inject(method = "setGoal", at = @At("HEAD"), cancellable = true)
    private void chuckpack$exactGoalBlock(CallbackInfo ci) {
        try {
        } catch (Throwable ignored) {}
    }

    // Redirect the PathManagers.moveTo call inside Freecam.setGoal to use GoalBlock exact same as goto X Y Z (vanilla Meteor uses GoalGetToBlock via moveTo)
    @org.spongepowered.asm.mixin.injection.Redirect(
        method = "setGoal",
        at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/pathing/IPathManager;moveTo(Lnet/minecraft/core/BlockPos;)V")
    )
    private void chuckpack$redirectMoveToExact(meteordevelopment.meteorclient.pathing.IPathManager instance, net.minecraft.core.BlockPos pos) {
        try {
            // Use reflection to avoid class_2338 remap issues - GoalBlock takes BlockPos
            Class<?> goalClass = Class.forName("baritone.api.pathing.goals.GoalBlock");
            Object goal = goalClass.getConstructor(net.minecraft.core.BlockPos.class).newInstance(pos);
            baritone.api.BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath((baritone.api.pathing.goals.Goal) goal);
        } catch (Throwable t) {
            try {
                instance.moveTo(pos);
            } catch (Throwable ignored) {}
        }
    }
}
