package net.chuck.chuckpack.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WItem;
import meteordevelopment.meteorclient.gui.widgets.WQuad;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.chuck.chuckpack.modules.world.BaseFinder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.ChunkPos;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FlaggedChunksScreen extends WindowScreen {
    private final BaseFinder module;
    private final Set<ChunkPos> baseChunks;
    private final Map<ChunkPos, Set<String>> chunkTriggerReasons;
    private final Map<ChunkPos, Map<Block, Integer>> chunkBlockCounts;
    private final Map<ChunkPos, Map<String, Integer>> chunkEntityCounts;
    private ChunkPos selectedChunk;
    private int lastSnapshotHash;
    private int tickCounter;
    private boolean lastTriggersExpanded = true;
    private boolean lastBlockExpanded = true;
    private boolean lastEntityExpanded = true;

    private static final Map<String, ItemStack> TRIGGER_ICONS = Map.ofEntries(
        Map.entry("Portal", new ItemStack(Items.OBSIDIAN)),
        Map.entry("Bedrock", new ItemStack(Items.BEDROCK)),
        Map.entry("Nether Roof", new ItemStack(Items.NETHERRACK)),
        Map.entry("Sign", new ItemStack(Items.OAK_SIGN)),
        Map.entry("Sky Build", new ItemStack(Items.BEACON)),
        Map.entry("Bubble Column", new ItemStack(Items.HEART_OF_THE_SEA)),
        Map.entry("Spawner", new ItemStack(Items.SPAWNER)),
        Map.entry("Loaded from File", new ItemStack(Items.BOOK)),
        Map.entry("Manual Add", new ItemStack(Items.EMERALD))
    );

    private static final Map<String, ItemStack> ENTITY_ICONS = Map.of(
        "Item Frame", new ItemStack(Items.ITEM_FRAME),
        "Ender Pearl", new ItemStack(Items.ENDER_PEARL),
        "Villager", new ItemStack(Items.VILLAGER_SPAWN_EGG),
        "NameTag", new ItemStack(Items.NAME_TAG),
        "Boat", new ItemStack(Items.OAK_BOAT),
        "Entity Cluster", new ItemStack(Items.SPAWNER)
    );

    public FlaggedChunksScreen(GuiTheme theme, BaseFinder module, Set<ChunkPos> baseChunks,
                                Map<ChunkPos, Set<String>> chunkTriggerReasons,
                                Map<ChunkPos, Map<Block, Integer>> chunkBlockCounts,
                                Map<ChunkPos, Map<String, Integer>> chunkEntityCounts) {
        super(theme, "Flagged Chunks");
        this.module = module;
        this.baseChunks = baseChunks;
        this.chunkTriggerReasons = chunkTriggerReasons;
        this.chunkBlockCounts = chunkBlockCounts;
        this.chunkEntityCounts = chunkEntityCounts;
        this.selectedChunk = null;
        this.lastSnapshotHash = computeSnapshotHash();
        this.tickCounter = 0;
    }

    @Override
    public void initWidgets() {
        if (selectedChunk == null) {
            buildChunkList();
        } else {
            buildChunkDetail();
        }
        lastSnapshotHash = computeSnapshotHash();
    }

    @Override
    public void tick() {
        super.tick();
        if (++tickCounter < 10) return;
        tickCounter = 0;

        int current = computeSnapshotHash();
        if (current != lastSnapshotHash) {
            reload();
        }
    }

    private int computeSnapshotHash() {
        int h = baseChunks.size();
        for (Map.Entry<ChunkPos, Set<String>> e : chunkTriggerReasons.entrySet()) {
            h = h * 31 + e.getValue().size();
        }
        for (Map.Entry<ChunkPos, Map<Block, Integer>> e : chunkBlockCounts.entrySet()) {
            h = h * 31 + e.getValue().size();
        }
        for (Map.Entry<ChunkPos, Map<String, Integer>> e : chunkEntityCounts.entrySet()) {
            h = h * 31 + e.getValue().size();
        }
        return h;
    }

    private void buildChunkList() {
        add(theme.label("Flagged Chunks (" + baseChunks.size() + ")")).expandX();

        WTable table = add(theme.table()).expandX().widget();

        List<ChunkPos> sorted = new ArrayList<>();
        for (ChunkPos c : baseChunks) sorted.add(c);
        sorted.sort(Comparator.comparingInt((ChunkPos c) -> c.x()).thenComparingInt((ChunkPos c) -> c.z()));

        for (ChunkPos LevelChunk : sorted) {
            table.add(theme.label(LevelChunk.getMiddleBlockX() + ", " + LevelChunk.getMiddleBlockZ())).expandX();

            WButton openBtn = table.add(theme.button("Open")).widget();
            openBtn.action = () -> {
                selectedChunk = LevelChunk;
                reload();
            };

            WButton pulseBtn = table.add(theme.button("Pulse")).widget();
            pulseBtn.action = () -> module.pulseChunk(LevelChunk);

            table.row();
        }
    }

    private void buildChunkDetail() {
        add(theme.label("LevelChunk: " + selectedChunk.getMiddleBlockX() + ", " + selectedChunk.getMiddleBlockZ())).expandX();

        WHorizontalList buttons = add(theme.horizontalList()).expandX().widget();

        WButton backBtn = buttons.add(theme.button("Back")).widget();
        backBtn.action = () -> {
            selectedChunk = null;
            reload();
        };

        WButton pulseBtn = buttons.add(theme.button("Pulse")).widget();
        pulseBtn.action = () -> module.pulseChunk(selectedChunk);

        add(theme.horizontalSeparator()).expandX();

        WSection triggersSection = add(theme.section("Triggers", lastTriggersExpanded)).expandX().widget();
        triggersSection.action = () -> lastTriggersExpanded = triggersSection.isExpanded();
        buildTriggersSection(triggersSection);

        WSection blockSection = add(theme.section("Block Data", lastBlockExpanded)).expandX().widget();
        blockSection.action = () -> lastBlockExpanded = blockSection.isExpanded();
        buildBlockSection(blockSection);

        WSection entitySection = add(theme.section("Entity Data", lastEntityExpanded)).expandX().widget();
        entitySection.action = () -> lastEntityExpanded = entitySection.isExpanded();
        buildEntitySection(entitySection);
    }

    private void buildTriggersSection(WSection section) {
        Set<String> reasons = chunkTriggerReasons.get(selectedChunk);
        if (reasons == null || reasons.isEmpty()) {
            section.add(theme.label("No trigger data for this LevelChunk")).expandX();
            return;
        }

        List<String> filtered = new ArrayList<>();
        for (String reason : reasons) {
            if (reason.startsWith("List ")) continue;
            if (ENTITY_ICONS.containsKey(reason)) continue;
            filtered.add(reason);
        }
        filtered.sort(Comparator.naturalOrder());

        if (filtered.isEmpty()) {
            section.add(theme.label("No trigger data for this LevelChunk")).expandX();
            return;
        }

        WTable triggerTable = section.add(theme.table()).expandX().widget();
        for (String reason : filtered) {
            ItemStack icon = TRIGGER_ICONS.getOrDefault(reason, new ItemStack(Items.REDSTONE_TORCH));
            triggerTable.add(theme.item(icon));
            triggerTable.add(theme.label(reason)).expandX();
            triggerTable.row();
        }
    }

    private void buildBlockSection(WSection section) {
        Map<Block, Integer> counts = chunkBlockCounts.get(selectedChunk);
        if (counts != null && !counts.isEmpty()) {
            List<Map.Entry<Block, Integer>> sorted = new ArrayList<>(counts.entrySet());
            sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

            WTable blockTable = section.add(theme.table()).expandX().widget();
            for (Map.Entry<Block, Integer> entry : sorted) {
                Block block = entry.getKey();
                int count = entry.getValue();

                ItemStack stack = new ItemStack(block.asItem());
                if (stack.isEmpty()) {
                    stack = new ItemStack(block);
                }

                blockTable.add(theme.item(stack));
                blockTable.add(theme.label(block.getName().getString() + " x" + count)).expandX();
                blockTable.row();
            }
        } else {
            section.add(theme.label("No block data for this LevelChunk")).expandX();
        }
    }

    private void buildEntitySection(WSection section) {
        Map<String, Integer> counts = chunkEntityCounts.get(selectedChunk);

        if (counts != null && !counts.isEmpty()) {
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
            sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

            WTable entityTable = section.add(theme.table()).expandX().widget();
            for (Map.Entry<String, Integer> entry : sorted) {
                String entityName = entry.getKey();
                int count = entry.getValue();

                ItemStack icon = ENTITY_ICONS.getOrDefault(entityName, new ItemStack(Items.SPAWNER));
                entityTable.add(theme.item(icon));
                entityTable.add(theme.label(entityName + " x" + count)).expandX();
                entityTable.row();
            }
        } else {
            section.add(theme.label("No entity data for this LevelChunk")).expandX();
        }
    }
}
