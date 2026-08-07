package net.aero.aeropack.modules.misc;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerManager;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StaffMonitor extends Module {
    private enum AlertSound {
        HARP("minecraft:block.note_block.harp"),
        BASS("minecraft:block.note_block.bass"),
        BASEDRUM("minecraft:block.note_block.basedrum"),
        SNARE("minecraft:block.note_block.snare"),
        HAT("minecraft:block.note_block.hat"),
        GUITAR("minecraft:block.note_block.guitar"),
        FLUTE("minecraft:block.note_block.flute"),
        BELL("minecraft:block.note_block.bell"),
        CHIME("minecraft:block.note_block.chime"),
        XYLOPHONE("minecraft:block.note_block.xylophone"),
        IRON_XYLOPHONE("minecraft:block.note_block.iron_xylophone"),
        COW_BELL("minecraft:block.note_block.cow_bell"),
        DIDGERIDOO("minecraft:block.note_block.didgeridoo"),
        BIT("minecraft:block.note_block.bit"),
        BANJO("minecraft:block.note_block.banjo"),
        PLING("minecraft:block.note_block.pling");

        private final String id;

        AlertSound(String id) {
            this.id = id;
        }

        private SoundEvent resolve() {
            return Registries.SOUND_EVENT.get(Identifier.of(id));
        }

        @Override
        public String toString() {
            return id;
        }
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> chatAlert = sgGeneral.add(new BoolSetting.Builder()
        .name("Chat alert")
        .description("Whether to alert in chat.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> soundAlert = sgGeneral.add(new BoolSetting.Builder()
        .name("Sound alert")
        .description("Whether to play an alert sound.")
        .defaultValue(true)
        .build()
    );
    private final Setting<AlertSound> sound = sgGeneral.add(new EnumSetting.Builder<AlertSound>()
        .name("Sound")
        .description("The sound to play when alerting.")
        .defaultValue(AlertSound.CHIME)
        .build()
    );
    private final Setting<Integer> volume = sgGeneral.add(new IntSetting.Builder()
        .name("Volume")
        .description("Volume in percent.")
        .defaultValue(100)
        .range(0, 200)
        .sliderRange(0, 200)
        .build()
    );
    private final Setting<Boolean> monitorModeSwitching = sgGeneral.add(new BoolSetting.Builder()
        .name("Monitor mode switching")
        .description("Alert when players switch gamemodes (survival \u2194 creative \u2194 adventure \u2194 spectator).")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> hiddenPlayerAlerts = sgGeneral.add(new BoolSetting.Builder()
        .name("Hidden player alerts")
        .description("Alerts when a player entity is visible to the client but missing from the tab list.\n\nThis can reveal vanished players, but some servers also use off-tab player entities for NPCs.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> ignoreNpcNames = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore NPC names")
        .description("Filters common server NPC/bot names from hidden player alerts, such as CIT-... fake player entries.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> hiddenStaffAlerts = sgGeneral.add(new BoolSetting.Builder()
        .name("Hidden staff alerts")
        .description("Alerts when a player from the local staff list appears in tab.\n\nStaff names are loaded from meteor/staff/<server>.txt and meteor/staff/global.txt, one name per line.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> mojangStaff = sgGeneral.add(new BoolSetting.Builder()
        .name("Mojang Staff")
        .description("Includes the bundled Mojang staff names and UUIDs in staff detection.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> quitOnStaffEnter = sgGeneral.add(new BoolSetting.Builder()
        .name("Quit on staff enter")
        .description("Leaves the server when StaffMonitor detects a staff member entering a monitored mode. AutoReconnect is disabled first.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> quitDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Quit delay")
        .description("Time to wait before quitting after staff are detected.")
        .defaultValue(0)
        .range(0, 60)
        .sliderRange(0, 60)
        .build()
    );

    private final Map<UUID, GameMode> gamemodeStates = new HashMap<>();
    private final Map<UUID, String> hiddenPlayers = new HashMap<>();
    private final Set<String> staffNames = new HashSet<>();
    private final Set<String> mojangStaffNames = new HashSet<>();
    private final Set<UUID> mojangStaffUuids = new HashSet<>();
    private final LinkedHashSet<String> savedStaffNames = new LinkedHashSet<>();
    private final Set<UUID> alertedStaff = new HashSet<>();
    private String lastServerKey = "unknown";
    private String savedStaffServerKey = "unknown";
    private boolean hiddenPlayerAlertsActive;
    private int staffQuitTicks = -1;
    private String staffQuitReason;

    public StaffMonitor() {
        super(Categories.Misc, "Staff Monitor", "Detects staff members, gamemode switches and hidden (off-tab) players, and can auto-quit when staff are present.");
    }

    /** Used by safety-aware modules without invoking StaffMonitor's action. */
    public boolean hasDetectedStaff() {
        return !alertedStaff.isEmpty() || !hiddenPlayers.isEmpty();
    }

    @Override
    public void onActivate() {
        gamemodeStates.clear();
        hiddenPlayers.clear();
        alertedStaff.clear();
        savedStaffNames.clear();
        lastServerKey = resolveServerKey();
        savedStaffServerKey = resolveStorageServerKey();
        hiddenPlayerAlertsActive = hiddenPlayerAlerts.get();
        loadStaffNames();
        loadMojangStaff();
        loadSavedStaffNames();
        snapshotCurrentStates();
        checkForStaffPresence();
        if (hiddenPlayerAlertsActive)
            snapshotHiddenPlayers();
    }

    @Override
    public void onDeactivate() {
        gamemodeStates.clear();
        hiddenPlayers.clear();
        alertedStaff.clear();
        staffNames.clear();
        mojangStaffNames.clear();
        mojangStaffUuids.clear();
        savedStaffNames.clear();
        hiddenPlayerAlertsActive = false;
        cancelStaffQuit();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        tickStaffQuit();

        if (mc.getNetworkHandler() == null) {
            gamemodeStates.clear();
            hiddenPlayers.clear();
            hiddenPlayerAlertsActive = false;
            savedStaffNames.clear();
            cancelStaffQuit();
            return;
        }
        if (mojangStaff.get() && mojangStaffNames.isEmpty()
            && mojangStaffUuids.isEmpty())
            loadMojangStaff();

        String serverKeyNow = resolveServerKey();
        if (!serverKeyNow.equals(lastServerKey)) {
            gamemodeStates.clear();
            hiddenPlayers.clear();
            alertedStaff.clear();
            lastServerKey = serverKeyNow;
            savedStaffServerKey = resolveStorageServerKey();
            hiddenPlayerAlertsActive = hiddenPlayerAlerts.get();
            loadStaffNames();
            loadMojangStaff();
            loadSavedStaffNames();
            snapshotCurrentStates();
            if (hiddenPlayerAlertsActive)
                snapshotHiddenPlayers();
            cancelStaffQuit();
            return;
        }

        boolean modeSwitching = monitorModeSwitching.get();

        updateHiddenPlayers();

        HashMap<UUID, GameMode> nextStates = new HashMap<>();
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            UUID id = entry.getProfile().id();
            String name = entry.getProfile().name();
            if (shouldIgnorePlayer(id, name))
                continue;

            GameMode currentMode = entry.getGameMode();
            nextStates.put(id, currentMode);

            // Hidden staff alert
            if (hiddenStaffAlerts.get()
                && isStaff(id, name)
                && alertedStaff.add(id))
                alertStaff(entry);

            GameMode previous = gamemodeStates.get(id);
            if (previous == null) {
                // First time seeing this player.
                if (modeSwitching && isStaffMode(currentMode)) {
                    alert(entry, currentMode, true);
                    recordSavedStaffMember(name);
                }
                if (quitOnStaffEnter.get() && isStaff(id, name))
                    queueStaffQuit(name);
                continue;
            }

            // Gamemode changed — alert on any transition
            if (modeSwitching && previous != currentMode) {
                alert(entry, currentMode, true);
                if (isStaffMode(previous) || isStaffMode(currentMode))
                    recordSavedStaffMember(name);
            }
        }

        gamemodeStates.clear();
        gamemodeStates.putAll(nextStates);
        tickStaffQuit();
    }

    private void updateHiddenPlayers() {
        if (!hiddenPlayerAlerts.get()) {
            hiddenPlayers.clear();
            hiddenPlayerAlertsActive = false;
            return;
        }

        if (!hiddenPlayerAlertsActive) {
            hiddenPlayerAlertsActive = true;
            snapshotHiddenPlayers();
            return;
        }

        if (mc.world == null) {
            hiddenPlayers.clear();
            return;
        }

        HashMap<UUID, String> nextHiddenPlayers = new HashMap<>();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (shouldIgnorePlayerEntity(player))
                continue;

            UUID id = player.getUuid();
            if (mc.getNetworkHandler().getPlayerListEntry(id) != null)
                continue;

            String playerName = player.getName().getString();
            nextHiddenPlayers.put(id, playerName);
            if (!hiddenPlayers.containsKey(id))
                alertHiddenPlayer(playerName, true);
        }

        for (Map.Entry<UUID, String> entry : hiddenPlayers.entrySet())
            if (!nextHiddenPlayers.containsKey(entry.getKey())
                && !shouldIgnorePlayer(entry.getKey(), entry.getValue()))
                alertHiddenPlayer(entry.getValue(), false);

        hiddenPlayers.clear();
        hiddenPlayers.putAll(nextHiddenPlayers);
    }

    private void snapshotCurrentStates() {
        if (mc.getNetworkHandler() == null)
            return;

        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            UUID id = entry.getProfile().id();
            String name = entry.getProfile().name();
            if (shouldIgnorePlayer(id, name))
                continue;

            gamemodeStates.put(id, entry.getGameMode());
        }
    }

    private void snapshotHiddenPlayers() {
        hiddenPlayers.clear();
        if (mc.getNetworkHandler() == null || mc.world == null)
            return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (shouldIgnorePlayerEntity(player))
                continue;

            UUID id = player.getUuid();
            if (mc.getNetworkHandler().getPlayerListEntry(id) != null)
                continue;

            hiddenPlayers.put(id, player.getName().getString());
        }
    }

    private boolean shouldIgnorePlayerEntity(PlayerEntity player) {
        if (player == null || FakePlayerManager.getFakePlayers().contains(player))
            return true;

        if (ignoreNpcNames.get()
            && isLikelyNpcName(player.getName().getString()))
            return true;

        return shouldIgnorePlayer(player.getUuid(),
            player.getName().getString());
    }

    private boolean isLikelyNpcName(String name) {
        if (name == null)
            return false;

        String stripped = name.strip();
        if (stripped.isEmpty())
            return true;

        String lower = stripped.toLowerCase(Locale.ROOT);
        if (lower.matches("cit-[0-9a-f-]{6,}"))
            return true;

        return lower.contains("npc") || lower.startsWith("[npc]")
            || lower.endsWith("_npc") || lower.startsWith("bot_")
            || lower.endsWith("_bot");
    }

    private void alert(PlayerListEntry entry, GameMode mode, boolean entered) {
        String name = entry.getProfile().name();
        String modeLabel = mode.asString(); // "survival", "creative", "spectator", "adventure"
        if (chatAlert.get()) {
            String action = entered ? "entered" : "left";
            ChatUtils.sendMsg(Text.literal(String.format(Locale.ROOT,
                "[StaffMonitor] %s %s %s mode.", name, action, modeLabel)));
        }

        if (soundAlert.get() && mc.world != null && mc.player != null) {
            SoundEvent event = sound.get().resolve();
            float target = (float) (volume.get() / 100.0);
            if (event == null || target <= 0F)
                return;

            int whole = (int) target;
            float remainder = target - whole;
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            for (int i = 0; i < whole; i++)
                mc.world.playSoundClient(x, y, z, event, SoundCategory.PLAYERS, 1F,
                    entered ? 1.2F : 0.85F, false);
            if (remainder > 0F)
                mc.world.playSoundClient(x, y, z, event, SoundCategory.PLAYERS,
                    remainder, entered ? 1.2F : 0.85F, false);
        }
    }

    private void alertStaff(PlayerListEntry entry) {
        String name = entry.getProfile().name();
        if (chatAlert.get())
            ChatUtils.sendMsg(Text.literal(String.format(Locale.ROOT,
                "[StaffMonitor] Staff member %s is online.", name)));

        if (soundAlert.get() && mc.world != null && mc.player != null) {
            SoundEvent event = sound.get().resolve();
            float target = (float) (volume.get() / 100.0);
            if (event == null || target <= 0F)
                return;

            mc.world.playSoundClient(mc.player.getX(), mc.player.getY(),
                mc.player.getZ(), event, SoundCategory.PLAYERS,
                Math.max(0.2F, target), 1.6F, false);
        }
    }

    private void alertHiddenPlayer(String name, boolean appeared) {
        if (chatAlert.get()) {
            String action = appeared ? "appeared off-tab" : "disappeared";
            ChatUtils.sendMsg(Text.literal(String.format(Locale.ROOT,
                "[StaffMonitor] %s %s.", name, action)));
        }

        if (soundAlert.get() && mc.world != null && mc.player != null) {
            SoundEvent event = sound.get().resolve();
            float target = (float) (volume.get() / 100.0);
            if (event == null || target <= 0F)
                return;

            mc.world.playSoundClient(mc.player.getX(), mc.player.getY(),
                mc.player.getZ(), event, SoundCategory.PLAYERS,
                Math.max(0.2F, target), appeared ? 1.6F : 0.7F, false);
        }
    }

    private void loadStaffNames() {
        staffNames.clear();
        if (!hiddenStaffAlerts.get())
            return;

        Path folder = MeteorClient.FOLDER.toPath().resolve("staff");
        loadStaffFile(folder.resolve("global.txt"));
        loadStaffFile(folder.resolve(lastServerKey + ".txt"));
    }

    private void loadMojangStaff() {
        mojangStaffNames.clear();
        mojangStaffUuids.clear();
        if (!mojangStaff.get())
            return;
        loadMojangStaffFile("/aeropack/staff/mojang-names.txt", false);
        loadMojangStaffFile("/aeropack/staff/mojang-uuids.txt", true);
    }

    private void loadMojangStaffFile(String resource, boolean uuidFile) {
        try (InputStream stream = StaffMonitor.class.getResourceAsStream(resource)) {
            if (stream == null)
                return;
            String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            for (String line : content.split("\\R")) {
                String value = line.strip();
                if (value.isEmpty() || value.startsWith("#"))
                    continue;
                if (uuidFile) {
                    try {
                        mojangStaffUuids.add(UUID.fromString(value));
                    } catch (IllegalArgumentException ignored) {
                        // Ignore malformed bundled entries.
                    }
                } else {
                    mojangStaffNames.add(value.toLowerCase(Locale.ROOT));
                }
            }
        } catch (IOException e) {
            ChatUtils.error("StaffMonitor Mojang staff list failed: " + e.getMessage());
        }
    }

    private void loadSavedStaffNames() {
        savedStaffNames.clear();
        Path file = getSavedStaffFile();
        if (!Files.isRegularFile(file))
            return;

        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String name = line.strip();
                if (name.isEmpty()
                    || containsNameIgnoreCase(savedStaffNames, name))
                    continue;
                savedStaffNames.add(name);
            }
        } catch (IOException e) {
            ChatUtils.error("StaffMonitor saved list failed: " + e.getMessage());
        }
    }

    private void recordSavedStaffMember(String name) {
        if (name == null)
            return;

        String trimmed = name.strip();
        if (trimmed.isEmpty()
            || containsNameIgnoreCase(savedStaffNames, trimmed))
            return;

        savedStaffNames.add(trimmed);
        saveSavedStaffNames();
    }

    private void saveSavedStaffNames() {
        Path file = getSavedStaffFile();
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, savedStaffNames, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
        } catch (IOException e) {
            ChatUtils.error("StaffMonitor saved list failed: " + e.getMessage());
        }
    }

    private Path getSavedStaffFile() {
        return MeteorClient.FOLDER.toPath().resolve("staff-monitor")
            .resolve(savedStaffServerKey + ".txt");
    }

    private void cancelStaffQuit() {
        staffQuitTicks = -1;
        staffQuitReason = null;
    }

    private void checkForStaffPresence() {
        if (!quitOnStaffEnter.get() || mc.getNetworkHandler() == null)
            return;

        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            String name = entry.getProfile().name();
            if (shouldIgnorePlayer(entry.getProfile().id(), name))
                continue;
            if (isStaff(entry.getProfile().id(), name)) {
                queueStaffQuit(name);
                return;
            }
        }
    }

    private void queueStaffQuit(String staffName) {
        if (!quitOnStaffEnter.get() || staffName == null
            || staffName.isBlank())
            return;

        if (staffQuitTicks >= 0)
            return;

        staffQuitTicks = quitDelay.get() * 20;
        staffQuitReason = staffName + " joined the server.";

        if (chatAlert.get())
            ChatUtils.sendMsg(Text.literal(String.format(Locale.ROOT,
                "[StaffMonitor] Staff member %s is online.", staffName)));

        if (soundAlert.get() && mc.world != null && mc.player != null) {
            SoundEvent event = sound.get().resolve();
            float target = (float) (volume.get() / 100.0);
            if (event != null && target > 0F) {
                mc.world.playSoundClient(mc.player.getX(), mc.player.getY(),
                    mc.player.getZ(), event, SoundCategory.PLAYERS,
                    Math.max(0.2F, target), 1.6F, false);
            }
        }

        AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
        if (autoReconnect != null && autoReconnect.isActive())
            autoReconnect.disable();

        ChatUtils.sendMsg(Text.literal(String.format(Locale.ROOT,
            "[StaffMonitor] Staff detected: %s. Quitting in %ds.", staffName,
            quitDelay.get())));
    }

    private void tickStaffQuit() {
        if (staffQuitTicks < 0)
            return;

        if (mc.getNetworkHandler() == null || mc.world == null) {
            cancelStaffQuit();
            return;
        }

        if (staffQuitTicks > 0) {
            staffQuitTicks--;
            return;
        }

        performStaffQuit();
    }

    private void performStaffQuit() {
        if (mc.world == null) {
            cancelStaffQuit();
            return;
        }

        AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
        if (autoReconnect != null && autoReconnect.isActive())
            autoReconnect.disable();

        mc.world.disconnect(Text.literal("StaffMonitor quit: " + staffQuitReason));
        cancelStaffQuit();
    }

    private static boolean containsNameIgnoreCase(Set<String> names, String name) {
        if (names == null || name == null)
            return false;

        for (String existing : names)
            if (existing != null && existing.equalsIgnoreCase(name))
                return true;
        return false;
    }

    private static boolean isStaffMode(GameMode mode) {
        return mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR;
    }

    private void loadStaffFile(Path file) {
        if (!Files.isRegularFile(file))
            return;

        try {
            for (String line : Files.readAllLines(file)) {
                String name = line.strip();
                if (name.isEmpty() || name.startsWith("#"))
                    continue;
                staffNames.add(name.toLowerCase(Locale.ROOT));
            }
        } catch (IOException e) {
            ChatUtils.error("StaffMonitor staff list failed: " + e.getMessage());
        }
    }

    private boolean isStaff(UUID id, String name) {
        if (name == null)
            return false;
        return staffNames.contains(name.toLowerCase(Locale.ROOT))
            || containsNameIgnoreCase(savedStaffNames, name)
            || (mojangStaff.get()
                && (mojangStaffNames.contains(name.toLowerCase(Locale.ROOT))
                    || mojangStaffUuids.contains(id)));
    }

    private boolean shouldIgnorePlayer(UUID id, String name) {
        if (id != null && mc.getSession() != null
            && id.equals(mc.getSession().getUuidOrNull()))
            return true;

        if (mc.player != null && id != null && id.equals(mc.player.getUuid()))
            return true;

        if (name == null)
            return false;

        if (mc.getSession() != null
            && name.equalsIgnoreCase(mc.getSession().getUsername()))
            return true;

        return Friends.get() != null && Friends.get().get(name) != null;
    }

    private String resolveServerKey() {
        ServerInfo info = mc.getCurrentServerEntry();
        if (info != null) {
            if (info.address != null && !info.address.isEmpty())
                return info.address.replace(':', '_');
            if (info.isRealm())
                return "realms_" + (info.name == null ? "" : info.name);
            if (info.name != null && !info.name.isEmpty())
                return "server_" + info.name;
        }
        if (mc.isInSingleplayer())
            return "singleplayer";
        return "unknown";
    }

    private String resolveStorageServerKey() {
        return resolveServerKey().replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
