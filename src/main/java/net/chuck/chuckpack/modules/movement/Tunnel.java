package net.chuck.chuckpack.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Tunnel extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAuto = settings.createGroup("Auto Modules");

    // --- General ---

    private final Setting<Boolean> disableNoFall = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-nofall")
        .description("Disables NoFall while tunneling to prevent fall damage.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> tunnelLength = sgGeneral.add(new IntSetting.Builder()
        .name("tunnel-length")
        .description("How many blocks forward to tunnel.")
        .defaultValue(50)
        .min(1)
        .sliderRange(1, 200)
        .build()
    );

    private final Setting<Integer> breakDelay = sgGeneral.add(new IntSetting.Builder()
        .name("break-delay")
        .description("Ticks between breaking each block in the cross-section.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0, 5)
        .build()
    );

    private final Setting<Boolean> leaveOnFullInventory = sgGeneral.add(new BoolSetting.Builder()
        .name("leave-on-full-inventory")
        .description("Stops tunneling when your inventory is full.")
        .defaultValue(true)
        .build()
    );

    // --- Auto Modules ---

    private final Setting<Boolean> autoEat = sgAuto.add(new BoolSetting.Builder()
        .name("auto-eat")
        .description("Automatically enables AutoEat while tunneling.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> autoLog = sgAuto.add(new BoolSetting.Builder()
        .name("auto-log")
        .description("Automatically enables AutoLog while tunneling.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> killAura = sgAuto.add(new BoolSetting.Builder()
        .name("kill-aura")
        .description("Automatically enables KillAura while tunneling.")
        .defaultValue(false)
        .build()
    );

    // --- State ---

    private int tunnelSize = 0;
    private boolean active = false;
    private List<BlockPos> currentCrossSection = new ArrayList<>();
    private int crossSectionIndex = 0;
    private BlockPos tunnelStart = null;
    private int blocksBroken = 0;
    private int tickCounter = 0;
    private BlockPos currentBreakTarget = null;
    private boolean wasNoFallEnabled = false;
    private boolean noFallToggled = false;
    private boolean wasAutoEatEnabled = false;
    private boolean autoEatToggled = false;
    private boolean wasAutoLogEnabled = false;
    private boolean autoLogToggled = false;
    private boolean wasKillAuraEnabled = false;
    private boolean killAuraToggled = false;

    public Tunnel() {
        super(Categories.Movement, "Tunnel", "Digs a tunnel of a specified cross-section. Type .tunnel NxN in chat (e.g., .tunnel 3x3, .tunnel 2x2).");
    }

    @Override
    public void onActivate() {
        active = false;
        tunnelSize = 0;
        blocksBroken = 0;
        tickCounter = 0;
        currentBreakTarget = null;
        currentCrossSection.clear();
        crossSectionIndex = 0;
        tunnelStart = null;
    }

    @Override
    public void onDeactivate() {
        active = false;
        tunnelSize = 0;
        currentCrossSection.clear();
        PathManagers.get().stop();
        restoreAllModules();
    }

    private void restoreAllModules() {
        if (noFallToggled && wasNoFallEnabled) {
            Modules.get().get(NoFall.class).toggle();
        }
        noFallToggled = false;
        wasNoFallEnabled = false;

        if (autoEatToggled && wasAutoEatEnabled) {
            Modules.get().get(AutoEat.class).toggle();
        }
        autoEatToggled = false;
        wasAutoEatEnabled = false;

        if (autoLogToggled && wasAutoLogEnabled) {
            Modules.get().get(net.chuck.chuckpack.modules.misc.AutoLogin.class).toggle();
        }
        autoLogToggled = false;
        wasAutoLogEnabled = false;

        if (killAuraToggled && wasKillAuraEnabled) {
            Modules.get().get(KillAura.class).toggle();
        }
        killAuraToggled = false;
        wasKillAuraEnabled = false;
    }

    public void startTunnel(int size) {
        if (mc.player == null || mc.level == null) return;
        if (size < 1 || size > 9) {
            error("Tunnel size must be between 1 and 9.");
            return;
        }

        tunnelSize = size;
        active = true;
        blocksBroken = 0;
        tickCounter = 0;
        currentBreakTarget = null;
        tunnelStart = mc.player.blockPosition();
        currentCrossSection.clear();
        crossSectionIndex = 0;

        enableAutoModules();

        info("Starting %dx%d tunnel. Length: %d blocks.", size, size, tunnelLength.get());
        calculateNextCrossSection();
    }

    private void enableAutoModules() {
        if (disableNoFall.get()) {
            wasNoFallEnabled = Modules.get().get(NoFall.class).isActive();
            if (wasNoFallEnabled && !Modules.get().get(NoFall.class).isActive()) {
                Modules.get().get(NoFall.class).toggle();
                noFallToggled = true;
            }
        }

        if (autoEat.get()) {
            wasAutoEatEnabled = Modules.get().get(AutoEat.class).isActive();
            if (!wasAutoEatEnabled) {
                Modules.get().get(AutoEat.class).toggle();
                autoEatToggled = true;
            }
        }

        if (autoLog.get()) {
            wasAutoLogEnabled = Modules.get().get(net.chuck.chuckpack.modules.misc.AutoLogin.class).isActive();
            if (!wasAutoLogEnabled) {
                Modules.get().get(net.chuck.chuckpack.modules.misc.AutoLogin.class).toggle();
                autoLogToggled = true;
            }
        }

        if (killAura.get()) {
            wasKillAuraEnabled = Modules.get().get(KillAura.class).isActive();
            if (!wasKillAuraEnabled) {
                Modules.get().get(KillAura.class).toggle();
                killAuraToggled = true;
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null || !active) return;

        if (tunnelStart != null && blocksBroken >= tunnelLength.get()) {
            info("Tunnel complete! Dug %d blocks.", blocksBroken);
            toggle();
            return;
        }

        if (leaveOnFullInventory.get() && isInventoryFull()) {
            info("Inventory full! Stopping tunnel. Dug %d blocks.", blocksBroken);
            toggle();
            return;
        }

        tickCounter++;
        if (tickCounter % (breakDelay.get() + 1) != 0) return;

        if (currentCrossSection.isEmpty() || crossSectionIndex >= currentCrossSection.size()) {
            calculateNextCrossSection();
            if (currentCrossSection.isEmpty()) return;
        }

        BlockPos target = currentCrossSection.get(crossSectionIndex);
        BlockState state = mc.level.getBlockState(target);

        if (state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
            crossSectionIndex++;
            if (crossSectionIndex >= currentCrossSection.size()) {
                calculateNextCrossSection();
            }
            return;
        }

        faceBlock(target);

        float destroyProgress = state.getDestroyProgress(mc.player, mc.level, target);

        if (destroyProgress >= 1.0f) {
            mc.gameMode.destroyBlock(target);
            blocksBroken++;
            currentBreakTarget = null;
            crossSectionIndex++;
            if (crossSectionIndex >= currentCrossSection.size()) {
                calculateNextCrossSection();
            }
        } else {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private boolean isInventoryFull() {
        if (mc.player == null) return false;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void calculateNextCrossSection() {
        if (mc.player == null || mc.level == null || !active) return;

        currentCrossSection.clear();
        crossSectionIndex = 0;

        Direction facing = mc.player.getDirection();
        BlockPos playerPos = mc.player.blockPosition();

        int sectionsDone = blocksBroken > 0 ? blocksBroken / (tunnelSize * tunnelSize) : 0;
        BlockPos sectionPos = playerPos.relative(facing, Math.max(1, sectionsDone + 1));

        int half = tunnelSize / 2;

        for (int dx = 0; dx < tunnelSize; dx++) {
            for (int dy = 0; dy < tunnelSize; dy++) {
                int offsetX = dx - half;
                int offsetY = dy - half;

                BlockPos blockPos;
                switch (facing) {
                    case NORTH, SOUTH -> blockPos = sectionPos.offset(offsetX, offsetY, 0);
                    case EAST, WEST -> blockPos = sectionPos.offset(0, offsetY, offsetX);
                    default -> blockPos = sectionPos.offset(offsetX, offsetY, 0);
                }

                currentCrossSection.add(blockPos);
            }
        }
    }

    private void faceBlock(BlockPos target) {
        if (mc.player == null) return;

        Vec3 playerPos = mc.player.getEyePosition();
        Vec3 targetCenter = Vec3.atCenterOf(target);
        Vec3 direction = targetCenter.subtract(playerPos).normalize();

        float yaw = (float) (Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.asin(direction.y));

        mc.player.setYRot(yaw);
        mc.player.setYHeadRot(yaw);
        mc.player.setXRot(pitch);
    }

    public boolean isActive() { return active; }
    public int getTunnelSize() { return tunnelSize; }
    public int getBlocksBroken() { return blocksBroken; }
}
