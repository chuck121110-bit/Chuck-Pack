package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Swarm.class)
public class SwarmMixin {

    @Inject(method = "onGameLeft", at = @At("HEAD"), cancellable = true)
    private void chuckpack\$preventGameLeft(GameLeftEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onGameJoin", at = @At("HEAD"), cancellable = true)
    private void chuckpack\$preventGameJoin(GameJoinedEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onTick", at = @At("HEAD"))
    private void chuckpack\$onTick(TickEvent.Post event, CallbackInfo ci) {
        Swarm self = (Swarm) (Object) this;
        if (!self.isHost()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Host-side inventory notifications
        net.chuck.chuckpack.modules.combat.SwarmGuard guard = net.chuck.chuckpack.modules.combat.SwarmGuard.get();
        if (guard != null) guard.hostTick(mc);

        Player host = mc.player;
        if (host.hurtTime > 0) {
            LivingEntity lastAttacker = host.getLastHurtByMob();
            if (lastAttacker instanceof Player player && !Friends.get().isFriend(player)) {
                self.host.sendMessage("swarm ChuckPack-retaliate " + player.getName().getString());
            }
        }
    }
}
