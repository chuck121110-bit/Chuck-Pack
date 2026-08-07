package net.aero.aeropack.modules.misc.villagerroller;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import org.apache.commons.io.FilenameUtils;

import java.util.stream.StreamSupport;

public class VillagerRoller extends Module {
    private static final Path CONFIG_PATH = MeteorClient.FOLDER.toPath().resolve("VillagerRoller");

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSound = settings.createGroup("Sound");
    private final SettingGroup sgChatFeedback = settings.createGroup("Chat feedback", false);

    private final Setting<Boolean> disableIfFound = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-when-found").description("Disable enchantment from list if found")
        .defaultValue(true).build());
    private final Setting<Boolean> disconnectIfFound = sgGeneral.add(new BoolSetting.Builder()
        .name("disconnect-when-found").description("Disconnect when enchantment found")
        .defaultValue(false).build());
    private final Setting<Boolean> saveListToConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("save-list-to-config").description("Save and load rolling list to config")
        .defaultValue(true).build());
    private final Setting<Boolean> enablePlaySound = sgGeneral.add(new BoolSetting.Builder()
        .name("enable-sound").description("Play sound when desired trade found")
        .defaultValue(true).build());
    private final Setting<List<SoundEvent>> sound = sgSound.add(new SoundEventListSetting.Builder()
        .name("sound-to-play").description("Sound to play on desired trade")
        .defaultValue(Collections.singletonList(SoundEvents.PLAYER_LEVELUP)).build());
    private final Setting<Double> soundPitch = sgSound.add(new DoubleSetting.Builder()
        .name("sound-pitch").description("Sound pitch")
        .defaultValue(1.0).min(0.0).sliderRange(0.0, 8.0).build());
    private final Setting<Double> soundVolume = sgSound.add(new DoubleSetting.Builder()
        .name("sound-volume").description("Sound volume")
        .defaultValue(1.0).min(0.0).sliderRange(0.0, 1.0).build());
    private final Setting<Boolean> pauseOnScreen = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-on-screens").description("Pause if screen is open")
        .defaultValue(true).build());
    private final Setting<Boolean> headRotateOnPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate-place").description("Look to block while placing")
        .defaultValue(true).build());
    private final Setting<Integer> failedToPlaceDelay = sgGeneral.add(new IntSetting.Builder()
        .name("place-fail-delay").description("Delay after failed place (ms)")
        .defaultValue(1500).min(0).sliderRange(0, 10000).build());
    private final Setting<Boolean> failedToPlaceDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("place-fail-disable").description("Disable if block placement fails")
        .defaultValue(false).build());
    private final Setting<Integer> maxProfessionWaitTime = sgGeneral.add(new IntSetting.Builder()
        .name("max-profession-wait-time").description("Max wait for profession (ms, 0=unlimited)")
        .defaultValue(0).min(0).sliderRange(0, 10000).build());
    private final Setting<Boolean> onlyTradeable = sgGeneral.add(new BoolSetting.Builder()
        .name("only-tradeable").description("Hide non-tradeable enchantments")
        .defaultValue(false).build());
    private final Setting<Boolean> sortEnchantments = sgGeneral.add(new BoolSetting.Builder()
        .name("sort-enchantments").description("Sort enchantments by name")
        .defaultValue(true).build());
    private final Setting<Boolean> instantRebreak = sgGeneral.add(new BoolSetting.Builder()
        .name("CivBreak").description("Use CivBreak to mine lectern instantly")
        .defaultValue(false).build());
    private final Setting<Integer> interactRetry = sgGeneral.add(new IntSetting.Builder()
        .name("interact-retry").description("Retry interact after N ticks if no response")
        .defaultValue(0).min(0).sliderRange(0, 200).build());
    private final Setting<Boolean> retrieveLectern = sgGeneral.add(new BoolSetting.Builder()
        .name("retrieve-lectern").description("Pathfind to pick up dropped lectern if not in hotbar")
        .defaultValue(true).build());
    private final Setting<Boolean> cfSetup = sgChatFeedback.add(new BoolSetting.Builder()
        .name("setup").description("Show setup hints")
        .defaultValue(true).build());
    private final Setting<Boolean> cfPausedOnScreen = sgChatFeedback.add(new BoolSetting.Builder()
        .name("paused-on-screen").description("Show paused message")
        .defaultValue(true).build());
    private final Setting<Boolean> cfLowerLevel = sgChatFeedback.add(new BoolSetting.Builder()
        .name("found-lower-level").description("Show lower level message")
        .defaultValue(true).build());
    private final Setting<Boolean> cfTooExpensive = sgChatFeedback.add(new BoolSetting.Builder()
        .name("found-too-expensive").description("Show too expensive message")
        .defaultValue(true).build());
    private final Setting<Boolean> cfIgnored = sgChatFeedback.add(new BoolSetting.Builder()
        .name("found-not-on-the-list").description("Show not in list message")
        .defaultValue(true).build());
    private final Setting<Boolean> cfProfessionTimeout = sgChatFeedback.add(new BoolSetting.Builder()
        .name("profession-timeout").description("Show profession timeout message")
        .defaultValue(true).build());
    private final Setting<Boolean> cfPlaceFailed = sgChatFeedback.add(new BoolSetting.Builder()
        .name("place-failed").description("Show place failed message")
        .defaultValue(true).build());
    private final Setting<Boolean> cfDiscrepancy = sgChatFeedback.add(new BoolSetting.Builder()
        .name("discrepancy").description("Show discrepancy message")
        .defaultValue(true).build());
    private final Setting<Boolean> cfSentRetryInteract = sgChatFeedback.add(new BoolSetting.Builder()
        .name("sent-retry-interact").description("Show retry interact message")
        .defaultValue(true).build());

    private State currentState = State.DISABLED;
    private Villager rollingVillager;
    private BlockPos rollingBlockPos;
    private Block rollingBlock;
    private final List<RollingEnchantment> searchingEnchants = new ArrayList<>();
    private long failedToPlacePrevMsg;
    private long currentProfessionWaitTime;
    private long waitingForTradesTicks;
    private int retrieveTicks;
    private int professionClearWaitTicks;
    private int professionNewWaitTicks;

    public VillagerRoller() {
        super(Categories.Misc, "villager-roller", "Rolls villager trades for enchantments. Ported from Villager Roller by maxsupermanhd.");
    }

    @Override
    public void onActivate() {
        if (toggleOnBindRelease) {
            toggleOnBindRelease = false;
            if (cfSetup.get()) warning("Turned off 'Toggle on bind release' for you.");
        }
        currentState = State.WAITING_FOR_TARGET_BLOCK;
        retrieveTicks = 0;
        if (cfSetup.get()) info("Attack the lectern block you want to roll.");
    }

    @Override
    public void onDeactivate() {
        currentState = State.DISABLED;
        if (PathManagers.get().isPathing()) PathManagers.get().stop();
    }

    @Override
    public String getInfoString() {
        return currentState.toString();
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        if (saveListToConfig.get()) {
            ListTag l = new ListTag();
            for (RollingEnchantment e : searchingEnchants) l.add(e.toTag());
            tag.put("rolling", l);
        }
        return tag;
    }

    @Override
    public Module fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (saveListToConfig.get()) {
            ListTag l = tag.getList("rolling").orElse(new ListTag());
            searchingEnchants.clear();
            for (Tag e : l) {
                if (e.getId() != 10) info("Invalid list element");
                else searchingEnchants.add(new RollingEnchantment().fromTag((CompoundTag) e));
            }
        }
        return this;
    }

    private boolean loadSearchingFromFile(File f) {
        if (!f.exists() || !f.canRead()) { error("File does not exist or cannot be read"); return false; }
        CompoundTag r;
        try { r = NbtIo.read(f.toPath()); }
        catch (IOException e) { e.printStackTrace(); error("Failed to load NBT from file"); return false; }
        if (r == null) { error("Failed to load NBT from file"); return false; }
        ListTag l = r.getList("rolling").orElse(new ListTag());
        searchingEnchants.clear();
        for (Tag e : l) {
            if (e.getId() != 10) { error("Invalid list element"); return false; }
            searchingEnchants.add(new RollingEnchantment().fromTag((CompoundTag) e));
        }
        return true;
    }

    public boolean saveSearchingToFile(File f) {
        ListTag l = new ListTag();
        for (RollingEnchantment e : searchingEnchants) l.add(e.toTag());
        CompoundTag c = new CompoundTag();
        c.put("rolling", l);
        if (Files.notExists(f.getParentFile().toPath()) && !f.getParentFile().mkdirs()) {
            error("Failed to make directories"); return false;
        }
        try { NbtIo.write(c, f.toPath()); return true; }
        catch (IOException e) { e.printStackTrace(); return false; }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        fillWidget(theme, list);
        return list;
    }

    private void fillWidget(GuiTheme theme, WVerticalList list) {
        WSection loadDataSection = list.add(theme.section("Config Saving")).expandX().widget();
        WTable control = loadDataSection.add(theme.table()).expandX().widget();
        WTextBox savedConfigName = control.add(theme.textBox("default")).expandWidgetX().expandCellX().expandX().widget();
        WButton save = control.add(theme.button("Save")).expandX().widget();
        save.action = () -> {
            if (saveSearchingToFile(new File(new File(MeteorClient.FOLDER, "VillagerRoller"), savedConfigName.get() + ".nbt")))
                info("Saved successfully");
            else error("Save failed");
            list.clear(); fillWidget(theme, list);
        };
        control.row();

        ArrayList<String> configs = new ArrayList<>();
        if (Files.notExists(CONFIG_PATH)) {
            if (!CONFIG_PATH.toFile().mkdirs()) error("Failed to create directory [{}]", CONFIG_PATH);
        } else {
            try (DirectoryStream<Path> configDir = Files.newDirectoryStream(CONFIG_PATH)) {
                for (Path config : configDir) configs.add(FilenameUtils.removeExtension(config.getFileName().toString()));
            } catch (IOException e) { error("Failed to list directory", e); }
        }
        if (!configs.isEmpty()) {
            WDropdown<String> loadedConfigName = control.add(theme.dropdown(configs.toArray(new String[0]), "default")).expandWidgetX().expandCellX().expandX().widget();
            WButton load = control.add(theme.button("Load")).expandX().widget();
            load.action = () -> {
                if (loadSearchingFromFile(new File(new File(MeteorClient.FOLDER, "VillagerRoller"), loadedConfigName.get() + ".nbt"))) {
                    list.clear(); fillWidget(theme, list); info("Loaded successfully");
                } else error("Failed to load file.");
            };
        }

        WSection enchantments = list.add(theme.section("Enchantments")).expandX().widget();
        WTable table = enchantments.add(theme.table()).expandX().widget();
        table.add(theme.item(Items.BOOK.getDefaultInstance()));
        table.add(theme.label("Enchantment")).expandX();
        table.add(theme.label("Level")).minWidth(40.0F).expandX();
        table.add(theme.label("Cost")).minWidth(40.0F).expandX();
        table.add(theme.label("Enabled"));
        table.add(theme.label("Remove"));
        table.row();

        if (sortEnchantments.get()) {
            searchingEnchants.removeIf(e -> e.getEnchantment() == null);
            searchingEnchants.sort(Comparator.comparing(RollingEnchantment::getEnchantment));
        }

        Optional<Registry<Enchantment>> reg = mc.level != null
            ? mc.level.registryAccess().lookup(Registries.ENCHANTMENT)
            : Optional.empty();

        for (int i = 0; i < searchingEnchants.size(); i++) {
            RollingEnchantment e = searchingEnchants.get(i);
            Optional<Holder.Reference<Enchantment>> en = reg.flatMap(r -> r.get(e.getEnchantment()));

            net.minecraft.world.item.ItemStack book = Items.BOOK.getDefaultInstance();
            int maxlevel = 255;
            if (en.isPresent()) {
                book = EnchantmentHelper.createBook(new EnchantmentInstance(en.get(), en.get().value().getMaxLevel()));
                maxlevel = en.get().value().getMaxLevel();
            }

            table.add(theme.item(book));
            WHorizontalList label = theme.horizontalList();
            WButton c = label.add(theme.button("Change")).widget();
            final int idx = i;
            c.action = () -> {
                mc.setScreen(new EnchantmentSelectScreen(theme, onlyTradeable.get(), sel -> {
                    searchingEnchants.set(idx, sel);
                    list.clear(); fillWidget(theme, list);
                }));
            };
            if (en.isPresent()) label.add(theme.label(Names.get(en.get())));
            else label.add(theme.label(e.getEnchantment().toString()));
            table.add(label);

            WIntEdit lev = table.add(theme.intEdit(e.getMinLevel(), 0, maxlevel, true)).minWidth(40.0F).expandX().widget();
            lev.action = () -> e.setMinLevel(lev.get());
            lev.tooltip = "Minimum level, 0 = max only";

            WIntEdit cost = table.add(theme.intEdit(e.getMaxCost(), 0, 64, false)).minWidth(40.0F).expandX().widget();
            cost.action = () -> e.setMaxCost(cost.get());
            cost.tooltip = "Max cost in emeralds, 0 = no limit";

            WCheckbox enabled = table.add(theme.checkbox(e.isEnabled())).widget();
            enabled.action = () -> e.setEnabled(enabled.checked);
            WMinus del = table.add(theme.minus()).widget();
            del.action = () -> { list.clear(); searchingEnchants.remove(e); fillWidget(theme, list); };
            table.row();
        }

        WTable controls = list.add(theme.table()).expandX().widget();
        WButton removeAll = controls.add(theme.button("Remove all")).expandX().widget();
        removeAll.action = () -> { list.clear(); searchingEnchants.clear(); fillWidget(theme, list); };
        WButton add = controls.add(theme.button("Add")).expandX().widget();
        add.action = () -> mc.setScreen(new EnchantmentSelectScreen(theme, onlyTradeable.get(), e -> {
            e.setMinLevel(1); e.setMaxCost(64); e.setEnabled(true);
            searchingEnchants.add(e); list.clear(); fillWidget(theme, list);
        }));
        WButton addAll = controls.add(theme.button("Add all")).expandX().widget();
        addAll.action = () -> {
            list.clear(); searchingEnchants.clear();
            reg.ifPresent(r -> {
                for (Holder<Enchantment> entry : getEnchants(onlyTradeable.get())) {
                    Identifier id = entry.unwrapKey().map(ResourceKey::identifier).orElse(null);
                    if (id != null) searchingEnchants.add(new RollingEnchantment(id,
                        entry.value().getMaxLevel(), RollingEnchantment.getMinimumPrice(entry), true));
                }
            });
            fillWidget(theme, list);
        };
        controls.row();

        WButton setOptimalForAll = controls.add(theme.button("Set optimal for all")).expandX().widget();
        setOptimalForAll.action = () -> {
            list.clear();
            reg.ifPresent(r -> {
                for (RollingEnchantment e : searchingEnchants) {
                    r.get(e.getEnchantment()).ifPresent(en ->
                        e.setMaxCost(RollingEnchantment.getMinimumPrice(en)));
                }
            });
            fillWidget(theme, list);
        };
        WButton priceBumpUp = controls.add(theme.button("+1 to price for all")).expandX().widget();
        priceBumpUp.action = () -> {
            list.clear();
            for (RollingEnchantment e : searchingEnchants) if (e.getMaxCost() < 64) e.setMaxCost(e.getMaxCost() + 1);
            fillWidget(theme, list);
        };
        WButton priceBumpDown = controls.add(theme.button("-1 to price for all")).expandX().widget();
        priceBumpDown.action = () -> {
            list.clear();
            for (RollingEnchantment e : searchingEnchants) if (e.getMaxCost() > 0) e.setMaxCost(e.getMaxCost() - 1);
            fillWidget(theme, list);
        };
        controls.row();
        WButton setZeroForAll = controls.add(theme.button("Set zero price for all")).expandX().widget();
        setZeroForAll.action = () -> { list.clear(); searchingEnchants.forEach(e -> e.setMaxCost(0)); fillWidget(theme, list); };
        WButton enableAll = controls.add(theme.button("Enable all")).expandX().widget();
        enableAll.action = () -> { list.clear(); searchingEnchants.forEach(e -> e.setEnabled(true)); fillWidget(theme, list); };
        WButton disableAll = controls.add(theme.button("Disable all")).expandX().widget();
        disableAll.action = () -> { list.clear(); searchingEnchants.forEach(e -> e.setEnabled(false)); fillWidget(theme, list); };
        controls.row();
    }

    private List<Holder<Enchantment>> getEnchants(boolean onlyTradeable) {
        if (mc.level == null) return Collections.emptyList();
        Registry<Enchantment> reg = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<Holder<Enchantment>> available = new ArrayList<>();
        for (Holder<Enchantment> e : reg.listElements().toList()) {
            if (!onlyTradeable || e.is(EnchantmentTags.TRADEABLE)) available.add(e);
        }
        return available;
    }

    public void triggerInteract() {
        if (pauseOnScreen.get() && mc.screen != null) {
            if (cfPausedOnScreen.get()) info("Rolling paused, interact with villager to continue");
            return;
        }
        Vec3 playerPos = mc.player.getEyePosition();
        Vec3 villagerPos = rollingVillager.getEyePosition();

        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
            mc.player, playerPos, villagerPos,
            mc.player.getBoundingBox().minmax(rollingVillager.getBoundingBox()).inflate(1.0),
            entity -> entity.isAlive() && entity != mc.player,
            playerPos.distanceTo(villagerPos));

        if (entityHitResult == null) {
            mc.gameMode.interact(mc.player, rollingVillager, new EntityHitResult(rollingVillager), InteractionHand.MAIN_HAND);
        } else {
            InteractionResult result = mc.gameMode.interact(mc.player, rollingVillager, entityHitResult, InteractionHand.MAIN_HAND);
            if (!result.consumesAction()) {
                mc.gameMode.interact(mc.player, rollingVillager, entityHitResult, InteractionHand.MAIN_HAND);
            }
        }
        waitingForTradesTicks = 0;
    }

    private List<Pair<Holder<Enchantment>, Integer>> getEnchants(net.minecraft.world.item.ItemStack stack) {
        List<Pair<Holder<Enchantment>, Integer>> ret = new ArrayList<>();
        ItemEnchantments component = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (component != null) {
            for (Object2IntMap.Entry<Holder<Enchantment>> e : component.entrySet()) {
                ret.add(ObjectIntImmutablePair.of(e.getKey(), e.getIntValue()));
            }
        }
        return ret;
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (currentState == State.ROLLING_WAITING_FOR_VILLAGER_TRADES) {
            Packet<?> p = event.packet;
            if (p instanceof ClientboundMerchantOffersPacket tradePacket) {
                mc.execute(() -> triggerTradeCheck(tradePacket.getOffers()));
            }
        }
    }

    public void triggerTradeCheck(MerchantOffers offers) {
        for (MerchantOffer offer : offers) {
            net.minecraft.world.item.ItemStack sellItem = offer.getResult();
            if (sellItem.getItem() == Items.ENCHANTED_BOOK && sellItem.get(DataComponents.STORED_ENCHANTMENTS) != null) {
                for (Pair<Holder<Enchantment>, Integer> enchant : getEnchants(sellItem)) {
                    int enchantLevel = enchant.right();
                    Registry<Enchantment> reg = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                    String enchantIdString = enchant.left().unwrapKey().map(k -> k.identifier().toString()).orElse("");
                    String enchantName = Names.get(enchant.left());
                    boolean found = false;
                    RollingEnchantment matched = null;

                    for (RollingEnchantment e : searchingEnchants) {
                        if (e.isEnabled() && e.getEnchantment().toString().equals(enchantIdString)) {
                            found = true;
                            matched = e;
                            int maxLevel = enchant.left().value().getMaxLevel();
                            if (e.getMinLevel() <= 0) {
                                if (enchantLevel >= maxLevel) break;
                                if (cfLowerLevel.get()) info(String.format(
                                    "Found %s but not max level: %d (max) > %d (found)", enchantName, maxLevel, enchantLevel));
                            } else {
                                if (e.getMinLevel() <= enchantLevel) break;
                                if (cfLowerLevel.get()) info(String.format(
                                    "Found %s but too low level: %d (requested) > %d (rolled)", enchantName, e.getMinLevel(), enchantLevel));
                            }
                        }
                    }
                    if (found && matched != null && (matched.getMaxCost() <= 0 || offer.getItemCostA().count() <= matched.getMaxCost())) {
                        if (disableIfFound.get()) matched.setEnabled(false);
                        toggle();
                        if (enablePlaySound.get() && !sound.get().isEmpty()) {
                            mc.getSoundManager().play(
                                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                    sound.get().get(0), soundPitch.get().floatValue(), soundVolume.get().floatValue()));
                        }
                        if (disconnectIfFound.get()) {
                            String levelText = enchantLevel <= 1 && enchant.left().value().getMaxLevel() <= 1 ? "" : " " + enchantLevel;
                            mc.disconnectFromWorld(Component.literal(String.format(
                                "Found enchant %s%s for %d emeralds. Disconnecting.", enchantName, levelText, offer.getItemCostA().count())));
                        }
                        return;
                    }
                }
            }
        }
        mc.player.swing(InteractionHand.MAIN_HAND);
        currentState = State.ROLLING_BREAKING_BLOCK;
    }

    @EventHandler
    private void onInteractEntity(InteractEntityEvent event) {
        if (currentState == State.WAITING_FOR_TARGET_VILLAGER && event.entity instanceof Villager villager) {
            rollingVillager = villager;
            currentState = State.ROLLING_BREAKING_BLOCK;
            if (cfSetup.get()) info("Got your villager!");
            event.cancel();
        }
    }

    @EventHandler(priority = 100)
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        if (currentState == State.WAITING_FOR_TARGET_BLOCK) {
            rollingBlockPos = event.blockPos;
            rollingBlock = mc.level.getBlockState(rollingBlockPos).getBlock();
            currentState = State.WAITING_FOR_TARGET_VILLAGER;
            if (instantRebreak.get()) {
                mc.getConnection().getConnection().send(new net.minecraft.network.protocol.game.ServerboundPlayerActionPacket(
                    net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                    rollingBlockPos, Direction.DOWN));
            }
            if (cfSetup.get()) info("Block selected. Now interact with the villager.");
        }
    }

    private void placeFailed(String msg) {
        if (failedToPlacePrevMsg + failedToPlaceDelay.get() <= System.currentTimeMillis()) {
            if (cfPlaceFailed.get()) info(msg);
            failedToPlacePrevMsg = System.currentTimeMillis();
        }
        if (failedToPlaceDisable.get()) toggle();
    }

    private ItemEntity findDroppedLectern() {
        if (mc.level == null || mc.player == null) return null;
        double closestDist = 64.0;
        ItemEntity closest = null;
        for (Entity entity : mc.level.players()) {
            if (entity instanceof ItemEntity itemEntity && itemEntity.getItem().getItem() == Items.LECTERN) {
                double dist = mc.player.distanceTo(entity);
                if (dist < closestDist) { closestDist = dist; closest = itemEntity; }
            }
        }
        return closest;
    }

    private void pathfindToBlock(BlockPos target) {
        PathManagers.get().moveTo(target, true);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) return;

        switch (currentState) {
            case ROLLING_BREAKING_BLOCK -> {
                if (instantRebreak.get()) {
                    mc.getConnection().getConnection().send(new net.minecraft.network.protocol.game.ServerboundPlayerActionPacket(
                        net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                        rollingBlockPos, Direction.DOWN));
                    mc.getConnection().getConnection().send(new net.minecraft.network.protocol.game.ServerboundPlayerActionPacket(
                        net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                        rollingBlockPos, Direction.DOWN));
                }
                if (mc.level.getBlockState(rollingBlockPos).getBlock() == Blocks.AIR) {
                    currentState = State.ROLLING_WAITING_FOR_VILLAGER_PROFESSION_CLEAR;
                    professionClearWaitTicks = 0;
                } else if (!instantRebreak.get() && !BlockUtils.breakBlock(rollingBlockPos, true)) {
                    error("Cannot break specified block");
                    toggle();
                }
            }
            case ROLLING_WAITING_FOR_VILLAGER_PROFESSION_CLEAR -> {
                professionClearWaitTicks++;
                if (professionClearWaitTicks < 5) return;
                if (mc.level.getBlockState(rollingBlockPos).getBlock() == Blocks.LECTERN) {
                    if (cfDiscrepancy.get()) info("Block mining reverted?");
                    currentState = State.ROLLING_BREAKING_BLOCK;
                    return;
                }
                if (rollingVillager.getVillagerData().profession() == VillagerProfession.NONE) {
                    currentState = State.ROLLING_PLACING_BLOCK;
                }
            }
            case ROLLING_PLACING_BLOCK -> {
                FindItemResult item = InvUtils.findInHotbar(Items.LECTERN);
                if (!item.found()) {
                    if (retrieveLectern.get()) {
                        ItemEntity dropped = findDroppedLectern();
                        if (dropped != null) {
                            currentState = State.RETRIEVING_LECTERN;
                            retrieveTicks = 0;
                            info("Lectern not in hotbar, pathfinding to pick it up...");
                            pathfindToBlock(dropped.blockPosition());
                        } else {
                            placeFailed("Lectern not found in hotbar or nearby");
                        }
                    } else {
                        placeFailed("Lectern not found in hotbar");
                    }
                    return;
                }
                if (!BlockUtils.canPlace(rollingBlockPos, true)) { placeFailed("Can't place lectern"); return; }
                if (!BlockUtils.place(rollingBlockPos, item, headRotateOnPlace.get(), 5)) { placeFailed("Failed to place lectern"); return; }
                currentState = State.ROLLING_WAITING_FOR_VILLAGER_PROFESSION_NEW;
                professionNewWaitTicks = 0;
                if (maxProfessionWaitTime.get() > 0) currentProfessionWaitTime = System.currentTimeMillis();
            }
            case ROLLING_WAITING_FOR_VILLAGER_PROFESSION_NEW -> {
                professionNewWaitTicks++;
                if (professionNewWaitTicks < 5) return;
                if (maxProfessionWaitTime.get() > 0
                    && currentProfessionWaitTime + maxProfessionWaitTime.get() <= System.currentTimeMillis()) {
                    if (cfProfessionTimeout.get()) info("Villager did not take profession in time");
                    currentState = State.ROLLING_BREAKING_BLOCK;
                    return;
                }
                if (mc.level.getBlockState(rollingBlockPos).getBlock() == Blocks.AIR) {
                    if (cfDiscrepancy.get()) info("Lectern placement reverted by server");
                    currentState = State.ROLLING_PLACING_BLOCK;
                    return;
                }
                if (mc.level.getBlockState(rollingBlockPos).getBlock() != Blocks.LECTERN) {
                    if (cfDiscrepancy.get()) info("Placed wrong block?!");
                    currentState = State.ROLLING_BREAKING_BLOCK;
                    return;
                }
                if (rollingVillager.getVillagerData().profession() != VillagerProfession.NONE
                    && rollingVillager.getVillagerData().profession() != VillagerProfession.LIBRARIAN) {
                    currentState = State.ROLLING_WAITING_FOR_VILLAGER_TRADES;
                    triggerInteract();
                }
            }
            case ROLLING_WAITING_FOR_VILLAGER_TRADES -> {
                int retryTicks = interactRetry.get();
                if (retryTicks > 0 && waitingForTradesTicks >= retryTicks) {
                    if (cfSentRetryInteract.get()) info("Sending retry interact packet");
                    triggerInteract();
                } else {
                    waitingForTradesTicks++;
                }
            }
            case RETRIEVING_LECTERN -> {
                retrieveTicks++;
                FindItemResult hasLectern = InvUtils.findInHotbar(Items.LECTERN);
                if (hasLectern.found()) {
                    if (PathManagers.get().isPathing()) PathManagers.get().stop();
                    currentState = State.ROLLING_PLACING_BLOCK;
                    info("Lectern retrieved, resuming...");
                    return;
                }
                if (retrieveTicks > 400) {
                    if (PathManagers.get().isPathing()) PathManagers.get().stop();
                    ItemEntity dropped = findDroppedLectern();
                    if (dropped == null) {
                        placeFailed("Lost track of dropped lectern, giving up");
                        currentState = State.ROLLING_PLACING_BLOCK;
                    } else {
                        pathfindToBlock(dropped.blockPosition());
                        retrieveTicks = 0;
                    }
                }
            }
            default -> {}
        }
    }

    private enum State {
        DISABLED,
        WAITING_FOR_TARGET_BLOCK,
        WAITING_FOR_TARGET_VILLAGER,
        ROLLING_BREAKING_BLOCK,
        ROLLING_WAITING_FOR_VILLAGER_PROFESSION_CLEAR,
        ROLLING_PLACING_BLOCK,
        ROLLING_WAITING_FOR_VILLAGER_PROFESSION_NEW,
        ROLLING_WAITING_FOR_VILLAGER_TRADES,
        RETRIEVING_LECTERN
    }
}
