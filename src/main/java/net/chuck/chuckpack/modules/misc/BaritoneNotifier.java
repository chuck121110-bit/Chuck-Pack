package net.chuck.chuckpack.modules.misc;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.BlockChangeEvent;
import baritone.api.event.events.BlockInteractEvent;
import baritone.api.event.events.ChatEvent;
import baritone.api.event.events.ChunkEvent;
import baritone.api.event.events.PacketEvent;
import baritone.api.event.events.PathEvent;
import baritone.api.event.events.PlayerUpdateEvent;
import baritone.api.event.events.RenderEvent;
import baritone.api.event.events.RotationMoveEvent;
import baritone.api.event.events.SprintStateEvent;
import baritone.api.event.events.TabCompleteEvent;
import baritone.api.event.events.TickEvent;
import baritone.api.event.events.WorldEvent;
import baritone.api.event.listener.IGameEventListener;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.chuck.chuckpack.util.WindowsNotify;
import net.minecraft.world.item.ItemStack;

/**
 * Windows (OS-level) notifications for Baritone background work:
 * goal reached, path calc failed, inventory full while mining.
 */
public class BaritoneNotifier extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> goalReached = sgGeneral.add(new BoolSetting.Builder()
        .name("goal-reached")
        .description("Windows notification when Baritone reaches its goal.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pathFailed = sgGeneral.add(new BoolSetting.Builder()
        .name("path-failed")
        .description("Windows notification when Baritone path calculation fails.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> inventoryFull = sgGeneral.add(new BoolSetting.Builder()
        .name("inventory-full")
        .description("Windows notification when your inventory is full while Baritone is mining.")
        .defaultValue(true)
        .build()
    );

    private IGameEventListener listener;
    private boolean fullNotified;
    private int tickCounter;
    private long lastGoalNotify;
    private long lastFailNotify;

    private static final long GOAL_COOLDOWN_MS = 30000;
    private static final long FAIL_COOLDOWN_MS = 15000;

    public BaritoneNotifier() {
        super(Categories.Misc, "baritone-notifier", "Windows notifications for Baritone background events (goal reached, path failed, inventory full).");
    }

    @Override
    public void onActivate() {
        fullNotified = false;
        tickCounter = 0;
        try {
            IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            listener = new IGameEventListener() {
                @Override public void onTick(TickEvent event) {}
                @Override public void onPostTick(TickEvent event) {}
                @Override public void onPlayerUpdate(PlayerUpdateEvent event) {}
                @Override public void onSendChatMessage(ChatEvent event) {}
                @Override public void onPreTabComplete(TabCompleteEvent event) {}
                @Override public void onChunkEvent(ChunkEvent event) {}
                @Override public void onBlockChange(BlockChangeEvent event) {}
                @Override public void onRenderPass(RenderEvent event) {}
                @Override public void onWorldEvent(WorldEvent event) {}
                @Override public void onSendPacket(PacketEvent event) {}
                @Override public void onReceivePacket(PacketEvent event) {}
                @Override public void onPlayerRotationMove(RotationMoveEvent event) {}
                @Override public void onPlayerSprintState(SprintStateEvent event) {}
                @Override public void onBlockInteract(BlockInteractEvent event) {}
                @Override public void onPlayerDeath() {}

                @Override
                public void onPathEvent(PathEvent event) {
                    long now = System.currentTimeMillis();
                    if (event == PathEvent.AT_GOAL && goalReached.get()) {
                        if (now - lastGoalNotify >= GOAL_COOLDOWN_MS) {
                            lastGoalNotify = now;
                            WindowsNotify.send("Baritone", "Goal reached.");
                        }
                    } else if ((event == PathEvent.CALC_FAILED || event == PathEvent.NEXT_CALC_FAILED) && pathFailed.get()) {
                        if (now - lastFailNotify >= FAIL_COOLDOWN_MS) {
                            lastFailNotify = now;
                            WindowsNotify.send("Baritone", "Path calculation failed.");
                        }
                    }
                }
            };
            baritone.getGameEventHandler().registerEventListener(listener);
        } catch (Throwable ignored) {}
    }

    @Override
    public void onDeactivate() {
        listener = null;
        fullNotified = false;
    }

    @EventHandler
    private void onTick(Pre event) {
        if (!inventoryFull.get() || mc.player == null) return;
        if ((tickCounter++ % 20) != 0) return;

        boolean mining;
        try {
            mining = BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().isActive();
        } catch (Throwable ignored) {
            return;
        }
        if (!mining) {
            fullNotified = false;
            return;
        }

        boolean full = true;
        int size = mc.player.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                full = false;
                break;
            }
        }

        if (full && !fullNotified) {
            fullNotified = true;
            WindowsNotify.send("Baritone", "Inventory is full, mining paused.");
        } else if (!full) {
            fullNotified = false;
        }
    }
}
