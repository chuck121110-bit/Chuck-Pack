package net.aero.aeropack.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Set;

public class SwarmDropHelper {

    // ── Ore items (what `drop ores` drops) ────────────────────────────────
    // Includes ALL variants: stone, deepslate, nether, raw ores

    private static final Set<Item> ORE_ITEMS = Set.of(
        // Coal
        net.minecraft.item.Items.COAL_ORE,
        net.minecraft.item.Items.DEEPSLATE_COAL_ORE,
        net.minecraft.item.Items.COAL,

        // Iron
        net.minecraft.item.Items.IRON_ORE,
        net.minecraft.item.Items.DEEPSLATE_IRON_ORE,
        net.minecraft.item.Items.RAW_IRON,
        net.minecraft.item.Items.IRON_NUGGET,
        net.minecraft.item.Items.IRON_INGOT,

        // Gold
        net.minecraft.item.Items.GOLD_ORE,
        net.minecraft.item.Items.DEEPSLATE_GOLD_ORE,
        net.minecraft.item.Items.NETHER_GOLD_ORE,
        net.minecraft.item.Items.RAW_GOLD,
        net.minecraft.item.Items.GOLD_NUGGET,
        net.minecraft.item.Items.GOLD_INGOT,

        // Copper
        net.minecraft.item.Items.COPPER_ORE,
        net.minecraft.item.Items.DEEPSLATE_COPPER_ORE,
        net.minecraft.item.Items.RAW_COPPER,
        net.minecraft.item.Items.COPPER_INGOT,

        // Diamond
        net.minecraft.item.Items.DIAMOND_ORE,
        net.minecraft.item.Items.DEEPSLATE_DIAMOND_ORE,
        net.minecraft.item.Items.DIAMOND,

        // Emerald
        net.minecraft.item.Items.EMERALD_ORE,
        net.minecraft.item.Items.DEEPSLATE_EMERALD_ORE,
        net.minecraft.item.Items.EMERALD,

        // Lapis
        net.minecraft.item.Items.LAPIS_ORE,
        net.minecraft.item.Items.DEEPSLATE_LAPIS_ORE,
        net.minecraft.item.Items.LAPIS_LAZULI,

        // Redstone
        net.minecraft.item.Items.REDSTONE_ORE,
        net.minecraft.item.Items.DEEPSLATE_REDSTONE_ORE,
        net.minecraft.item.Items.REDSTONE,

        // Quartz
        net.minecraft.item.Items.NETHER_QUARTZ_ORE,
        net.minecraft.item.Items.QUARTZ,

        // Netherite
        net.minecraft.item.Items.ANCIENT_DEBRIS,
        net.minecraft.item.Items.NETHERITE_SCRAP,
        net.minecraft.item.Items.NETHERITE_INGOT
    );

    public static boolean isOre(ItemStack stack) {
        return ORE_ITEMS.contains(stack.getItem());
    }

    // ── Valuable materials (what `drop valuables` drops) ──────────────────
    // Things that are worth keeping but are NOT ores (ores are separate)

    private static final Set<Item> VALUABLE_ITEMS = Set.of(
        // Special valuables
        net.minecraft.item.Items.GOLDEN_APPLE,
        net.minecraft.item.Items.ENCHANTED_GOLDEN_APPLE,
        net.minecraft.item.Items.ENDER_PEARL,
        net.minecraft.item.Items.ENDER_EYE,
        net.minecraft.item.Items.BLAZE_ROD,
        net.minecraft.item.Items.NETHER_STAR,
        net.minecraft.item.Items.HEART_OF_THE_SEA,
        net.minecraft.item.Items.ECHO_SHARD,
        net.minecraft.item.Items.AMETHYST_SHARD,
        net.minecraft.item.Items.PRISMARINE_CRYSTALS,
        net.minecraft.item.Items.PRISMARINE_SHARD,
        net.minecraft.item.Items.SHULKER_SHELL,
        net.minecraft.item.Items.NAUTILUS_SHELL,
        net.minecraft.item.Items.ARMADILLO_SCUTE,
        net.minecraft.item.Items.TURTLE_SCUTE,
        net.minecraft.item.Items.SLIME_BALL,
        net.minecraft.item.Items.GHAST_TEAR,
        net.minecraft.item.Items.MAGMA_CREAM,
        net.minecraft.item.Items.GLOWSTONE_DUST,
        net.minecraft.item.Items.OBSIDIAN,
        net.minecraft.item.Items.CRYING_OBSIDIAN,
        net.minecraft.item.Items.REDSTONE_LAMP,

        // Rare gear (not armor/tools, but worth keeping)
        net.minecraft.item.Items.TOTEM_OF_UNDYING,
        net.minecraft.item.Items.ELYTRA,
        net.minecraft.item.Items.SPYGLASS,
        net.minecraft.item.Items.COMPASS,
        net.minecraft.item.Items.RECOVERY_COMPASS,
        net.minecraft.item.Items.CLOCK,
        net.minecraft.item.Items.MAP,
        net.minecraft.item.Items.BUNDLE,
        net.minecraft.item.Items.HEAVY_CORE
    );

    public static boolean isValuable(ItemStack stack) {
        return VALUABLE_ITEMS.contains(stack.getItem());
    }

    // ── Armor ─────────────────────────────────────────────────────────────

    public static boolean isArmor(ItemStack stack) {
        if (!stack.contains(net.minecraft.component.DataComponentTypes.EQUIPPABLE)) return false;
        net.minecraft.component.type.EquippableComponent equip = stack.get(net.minecraft.component.DataComponentTypes.EQUIPPABLE);
        net.minecraft.entity.EquipmentSlot slot = equip.slot();
        return slot == net.minecraft.entity.EquipmentSlot.HEAD
            || slot == net.minecraft.entity.EquipmentSlot.CHEST
            || slot == net.minecraft.entity.EquipmentSlot.LEGS
            || slot == net.minecraft.entity.EquipmentSlot.FEET;
    }

    // ── Tools & weapons ───────────────────────────────────────────────────

    public static boolean isTool(ItemStack stack) {
        if (stack.contains(net.minecraft.component.DataComponentTypes.TOOL)) return true;
        Item item = stack.getItem();
        return item == net.minecraft.item.Items.BOW
            || item == net.minecraft.item.Items.CROSSBOW
            || item == net.minecraft.item.Items.TRIDENT
            || item == net.minecraft.item.Items.SHIELD;
    }

    // ── Food ──────────────────────────────────────────────────────────────

    public static boolean isFood(ItemStack stack) {
        return stack.contains(net.minecraft.component.DataComponentTypes.FOOD);
    }

    // ── Enchanted items ───────────────────────────────────────────────────

    public static boolean isEnchanted(ItemStack stack) {
        if (stack.hasEnchantments()) return true;
        if (stack.getItem() == net.minecraft.item.Items.ENCHANTED_BOOK) return true;
        return false;
    }

    // ── Junk (everything that's NOT any of the above) ────────────────────
    // Junk = basic blocks, mob drops, mob heads, music discs, saddle, name tags, etc.

    public static boolean isJunk(ItemStack stack) {
        return !isOre(stack)
            && !isValuable(stack)
            && !isArmor(stack)
            && !isTool(stack)
            && !isFood(stack)
            && !isEnchanted(stack);
    }

    // ── Slot mapping ──────────────────────────────────────────────────────

    public static int invToScreenSlot(int invIndex) {
        if (invIndex < 9) return invIndex + 36;
        if (invIndex < 36) return invIndex;
        if (invIndex < 40) return 44 - invIndex;
        if (invIndex == 40) return 45;
        return -1;
    }

    // ── Main drop method ──────────────────────────────────────────────────

    public static void dropByType(String type) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;

        if (mc.currentScreen != null) {
            mc.player.closeHandledScreen();
            mc.setScreen(null);
        }

        net.minecraft.entity.player.PlayerInventory inv = mc.player.getInventory();
        int dropped = 0;

        switch (type) {
            case "all" -> {
                for (int i = 0; i < inv.size(); i++) {
                    if (inv.getStack(i).isEmpty()) continue;
                    int slotId = invToScreenSlot(i);
                    if (slotId < 0) continue;
                    mc.gameMode.clickSlot(mc.player.currentScreenHandler.syncId, slotId, 1,
                        net.minecraft.screen.slot.SlotActionType.THROW, mc.player);
                    dropped++;
                }
            }
            case "junk" -> {
                for (int i = 0; i < 36; i++) {
                    ItemStack stack = inv.getStack(i);
                    if (stack.isEmpty()) continue;
                    if (!isJunk(stack)) continue;
                    int slotId = i < 9 ? i + 36 : i;
                    mc.gameMode.clickSlot(mc.player.currentScreenHandler.syncId, slotId, 1,
                        net.minecraft.screen.slot.SlotActionType.THROW, mc.player);
                    dropped++;
                }
            }
            case "ores" -> {
                for (int i = 0; i < 36; i++) {
                    ItemStack stack = inv.getStack(i);
                    if (stack.isEmpty()) continue;
                    if (!isOre(stack)) continue;
                    int slotId = i < 9 ? i + 36 : i;
                    mc.gameMode.clickSlot(mc.player.currentScreenHandler.syncId, slotId, 1,
                        net.minecraft.screen.slot.SlotActionType.THROW, mc.player);
                    dropped++;
                }
            }
            case "hotbar" -> {
                for (int i = 0; i < 9; i++) {
                    if (inv.getStack(i).isEmpty()) continue;
                    mc.gameMode.clickSlot(mc.player.currentScreenHandler.syncId, i + 36, 1,
                        net.minecraft.screen.slot.SlotActionType.THROW, mc.player);
                    dropped++;
                }
            }
            case "valuables" -> {
                for (int i = 0; i < 36; i++) {
                    ItemStack stack = inv.getStack(i);
                    if (stack.isEmpty()) continue;
                    if (!isValuable(stack)) continue;
                    int slotId = i < 9 ? i + 36 : i;
                    mc.gameMode.clickSlot(mc.player.currentScreenHandler.syncId, slotId, 1,
                        net.minecraft.screen.slot.SlotActionType.THROW, mc.player);
                    dropped++;
                }
            }
        }

        meteordevelopment.meteorclient.utils.player.ChatUtils.infoPrefix("Swarm", "Dropped (highlight)%d (highlight)%s stacks.", dropped, type);
    }
}
