package net.aero.aeropack.modules.combat;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ImprovedAutoArmor extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> ignoreElytra = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-elytra")
        .description("Don't touch chest slot if elytra is equipped.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyBetter = sgGeneral.add(new BoolSetting.Builder()
        .name("only-better")
        .description("Only swap if the new piece is strictly better.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> swapDelay = sgGeneral.add(new IntSetting.Builder()
        .name("swap-delay")
        .description("Ticks between swap attempts.")
        .defaultValue(2)
        .min(1)
        .sliderMax(10)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Disable when full set is equipped.")
        .defaultValue(false)
        .build()
    );

    private int tickCounter = 0;
    private int swapStage = 0;

    private static final EquipmentSlot[] ARMOR_ORDER = {
        EquipmentSlot.FEET,
        EquipmentSlot.LEGS,
        EquipmentSlot.CHEST,
        EquipmentSlot.HEAD
    };

    public ImprovedAutoArmor() {
        super(
            Categories.Combat,
            "Improved Auto Armor",
            "Equips the best armor from your inventory. Safer than Meteor's built-in — verifies swaps and equips bottom-up."
        );
    }

    @Override
    public void onActivate() {
        tickCounter = 0;
        swapStage = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) return;

        tickCounter++;
        if (tickCounter < swapDelay.get()) return;
        tickCounter = 0;

        for (int i = swapStage; i < ARMOR_ORDER.length; i++) {
            EquipmentSlot slot = ARMOR_ORDER[i];

            if (ignoreElytra.get() && slot == EquipmentSlot.CHEST) {
                ItemStack chestStack = mc.player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestStack.is(Items.ELYTRA)) {
                    swapStage = i + 1;
                    continue;
                }
            }

            ItemStack currentArmor = mc.player.getItemBySlot(slot);
            int bestSlot = findBestArmorForSlot(slot, currentArmor);

            if (bestSlot == -1) {
                swapStage = i + 1;
                continue;
            }

            if (onlyBetter.get() && !isBetterArmor(bestSlot, currentArmor)) {
                swapStage = i + 1;
                continue;
            }

            InvUtils.move().from(bestSlot).toArmor(slot.getIndex());
            swapStage = i + 1;
            return;
        }

        if (autoDisable.get() && swapStage >= ARMOR_ORDER.length) {
            toggle();
        }
    }

    private int findBestArmorForSlot(EquipmentSlot slot, ItemStack current) {
        int bestInvSlot = -1;
        int bestScore = scoreArmor(current, slot);

        for (int i = 9; i < 45; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (!isArmorForSlot(stack, slot)) continue;

            int score = scoreArmor(stack, slot);
            if (score > bestScore) {
                bestScore = score;
                bestInvSlot = i;
            }
        }

        return bestInvSlot;
    }

    private boolean isBetterArmor(int invSlot, ItemStack current) {
        EquipmentSlot slot = getArmorSlotForItem(current);
        ItemStack candidate = mc.player.getInventory().getItem(invSlot);
        return scoreArmor(candidate, slot) > scoreArmor(current, slot);
    }

    private boolean isArmorForSlot(ItemStack stack, EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> stack.is(ItemTags.HEAD_ARMOR);
            case CHEST -> stack.is(ItemTags.CHEST_ARMOR);
            case LEGS -> stack.is(ItemTags.LEG_ARMOR);
            case FEET -> stack.is(ItemTags.FOOT_ARMOR);
            default -> false;
        };
    }

    private EquipmentSlot getArmorSlotForItem(ItemStack stack) {
        if (stack.is(ItemTags.FOOT_ARMOR)) return EquipmentSlot.FEET;
        if (stack.is(ItemTags.LEG_ARMOR)) return EquipmentSlot.LEGS;
        if (stack.is(ItemTags.CHEST_ARMOR)) return EquipmentSlot.CHEST;
        if (stack.is(ItemTags.HEAD_ARMOR)) return EquipmentSlot.HEAD;
        return EquipmentSlot.MAINHAND;
    }

    private int scoreArmor(ItemStack stack, EquipmentSlot slot) {
        if (stack == null || stack.isEmpty()) return 0;
        if (!isArmorForSlot(stack, slot)) return 0;

        int score = 0;
        score += stack.getMaxDamage() - stack.getDamageValue();
        score += stack.getEnchantments().size() * 10;

        for (var entry : stack.getEnchantments().entrySet()) {
            score += entry.getValue().intValue() * 5;
        }

        return score;
    }
}
