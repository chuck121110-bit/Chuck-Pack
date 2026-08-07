package net.aero.aeropack.modules.misc;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.aero.aeropack.modules.render.CoordinateLogout;

import java.util.Collection;

public class AntiSocial extends Module {
    private static final double HARD_RANGE = 25000.0;

    public AntiSocial() {
        super(Categories.Misc, "anti-social",
            "Prevents other players from seeing you. Disconnects when a non-friend player is able to spot you.");
    }

    private boolean disconnected = false;

    @Override
    public void onActivate() {
        disconnected = false;
    }

    @Override
    public void onDeactivate() {
        disconnected = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (disconnected) return;
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        String myName = mc.player.getName().getString();
        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        Collection<PlayerListEntry> players = mc.getNetworkHandler().getPlayerList();
        if (players == null) return;

        for (PlayerListEntry entry : players) {
            String name = entry.getProfile().name();
            if (name.equals(myName)) continue;

            if (mc.world != null) {
                for (PlayerEntity player : mc.world.getPlayers()) {
                    if (player.getName().getString().equals(name)) {
                        if (Friends.get().isFriend(player)) break;

                        double dx = player.getX() - px;
                        double dy = player.getY() - py;
                        double dz = player.getZ() - pz;
                        double distSq = dx * dx + dy * dy + dz * dz;

                        if (distSq <= HARD_RANGE * HARD_RANGE) {
                            disconnected = true;
                            CoordinateLogout.hardDisconnect("Anti-Social: Player nearby (" + name + ")");
                            return;
                        }
                        break;
                    }
                }
            }
        }
    }
}
