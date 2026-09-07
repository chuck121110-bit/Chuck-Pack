package net.chuck.chuckpack.modules.misc;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
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
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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
            return BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(id));
        }

        @Override
        public String toString() {
            return id;
        }
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> toastAlert = sgGeneral.add(new BoolSetting.Builder()
        .name("Toast alerts")
        .description("Shows StaffMonitor toast notifications.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore friends")
        .description("Do not monitor players on your friends list.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> vanishDetection = sgGeneral.add(new BoolSetting.Builder()
        .name("Vanish detection")
        .description("Detects probable vanish cycles after filtering tab-list rotations and player initialization.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> vanishKnownStaffOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("Vanish: known ops only")
        .description("Only tracks players in StaffMonitor's saved, configured, or bundled staff lists.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> vanishPlayerLimit = sgGeneral.add(new IntSetting.Builder()
        .name("Vanish player limit")
        .description("Disables vanish-cycle detection when the server advertises or shows this many players. Set to 0 or Infinite (after 1000) to disable the limit.")
        .defaultValue(20)
        .range(0, 1001)
        .sliderRange(0, 1001)
        .build()
    );

    private final Setting<Boolean> suppressTabRotations = sgGeneral.add(new BoolSetting.Builder()
        .name("Suppress tab rotations")
        .description("Ignores balanced add/remove player-info batches, which servers commonly use to rotate a synthetic tab list.")
        .defaultValue(true)
        .build()
    );

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

    private final Map<UUID, GameType> gamemodeStates = new HashMap<>();
    private final Map<UUID, String> hiddenPlayers = new HashMap<>();
    private final Set<String> staffNames = new HashSet<>();
    private final Set<String> mojangStaffNames = new HashSet<>();
    private final Set<UUID> mojangStaffUuids = new HashSet<>();
    private final LinkedHashSet<String> savedStaffNames = new LinkedHashSet<>();
    private final Set<UUID> alertedStaff = new HashSet<>();
    private String lastServerKey = "unknown";
    private String savedStaffServerKey = "unknown";
    private boolean hiddenPlayerAlertsActive;
    // Vanish detection state (ported from Wurst 0.59+)
    private boolean serverHasVanish;
    private final Map<UUID, Long> vanishCandidates = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, Long> pendingVanishCycles = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, Long> recentEntitySpawns = Collections.synchronizedMap(new HashMap<>());
    private final List<PlayerInfoEvent> pendingPlayerInfoEvents = Collections.synchronizedList(new ArrayList<>());
    private final Map<UUID, String> playerNames = new HashMap<>();
    private long nextMojangStaffRetry;
    private long lastMojangStaffError;
    private int staffQuitTicks = -1;
    private String staffQuitReason;

    public StaffMonitor() {
        super(Categories.Misc, "Staff Monitor", "Detects staff members, GameType switches and hidden (off-tab) players, and can auto-quit when staff are present. Ported from Wurst StaffMonitor (vanish-cycle detection, toast alerts).");
    }

    /** Used by safety-aware modules without invoking StaffMonitor's action. */
    public boolean hasDetectedStaff() {
        return !alertedStaff.isEmpty() || !hiddenPlayers.isEmpty();
    }

    @Override
    public void onActivate() {
        gamemodeStates.clear();
        vanishCandidates.clear();
        pendingVanishCycles.clear();
        recentEntitySpawns.clear();
        pendingPlayerInfoEvents.clear();
        serverHasVanish = false;
        hiddenPlayers.clear();
        alertedStaff.clear();
        savedStaffNames.clear();
        playerNames.clear();
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
        vanishCandidates.clear();
        pendingVanishCycles.clear();
        recentEntitySpawns.clear();
        pendingPlayerInfoEvents.clear();
        serverHasVanish = false;
        hiddenPlayers.clear();
        alertedStaff.clear();
        staffNames.clear();
        mojangStaffNames.clear();
        mojangStaffUuids.clear();
        savedStaffNames.clear();
        playerNames.clear();
        hiddenPlayerAlertsActive = false;
        cancelStaffQuit();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!vanishDetection.get()) return;
        Packet<?> packet = event.packet;
        if (packet instanceof ClientboundSelectKnownPacks packs) {
            for (KnownPack pack : packs.knownPacks()) {
                String id = (pack.namespace() + ":" + pack.id() + ":" + pack.version()).toLowerCase(Locale.ROOT);
                if (id.contains("vanish")) serverHasVanish = true;
            }
            return;
        }
        long now = System.currentTimeMillis();
        if (packet instanceof ClientboundAddEntityPacket add) {
            UUID id = add.getUUID();
            if (id != null) {
                recentEntitySpawns.put(id, now);
                pendingVanishCycles.remove(id);
            }
            return;
        }
        if (packet instanceof ClientboundPlayerInfoRemovePacket removed) {
            for (UUID id : removed.profileIds()) {
                String name = playerNames.get(id);
                // Fallback: try to resolve name from current tab list cache if missing
                if (name == null) {
                    // Attempt to keep name from hiddenPlayers or gamemodeStates? Use UNKNOWN
                    name = "unknown";
                }
                pendingPlayerInfoEvents.add(new PlayerInfoEvent(id, name, false, now));
            }
            return;
        }
        if (packet instanceof ClientboundPlayerInfoUpdatePacket update
            && update.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
            for (ClientboundPlayerInfoUpdatePacket.Entry entry : update.entries()) {
                if (entry.profile() == null) continue;
                String name = entry.profile().name();
                UUID pid = entry.profileId();
                playerNames.put(pid, name);
                pendingPlayerInfoEvents.add(new PlayerInfoEvent(pid, name, true, now));
            }
        }
    }

    private void processPlayerInfoEvents() {
        long now = System.currentTimeMillis();
        List<PlayerInfoEvent> ready = new ArrayList<>();
        synchronized (pendingPlayerInfoEvents) {
            for (int i = pendingPlayerInfoEvents.size() - 1; i >= 0; i--) {
                PlayerInfoEvent ev = pendingPlayerInfoEvents.get(i);
                if (now - ev.timestamp < 125) continue;
                ready.add(0, ev);
                pendingPlayerInfoEvents.remove(i);
            }
        }
        for (int start = 0; start < ready.size();) {
            long batchStart = ready.get(start).timestamp;
            int end = start + 1;
            while (end < ready.size() && ready.get(end).timestamp - batchStart <= 125) end++;
            processPlayerInfoBatch(ready.subList(start, end), now);
            start = end;
        }
    }

    private void processPlayerInfoBatch(List<PlayerInfoEvent> batch, long now) {
        Set<UUID> added = new HashSet<>();
        Set<UUID> removed = new HashSet<>();
        for (PlayerInfoEvent ev : batch) if (ev.added) added.add(ev.id); else removed.add(ev.id);

        boolean initialization = !Collections.disjoint(added, removed);
        for (UUID id : added) {
            Long spawnedAt = recentEntitySpawns.get(id);
            if (spawnedAt != null && now - spawnedAt < 1500) initialization = true;
        }
        boolean rotation = suppressTabRotations.get() && !added.isEmpty() && !removed.isEmpty()
            && Math.abs(added.size() - removed.size()) <= 1;
        if (initialization || rotation) {
            for (UUID id : added) vanishCandidates.remove(id);
            for (UUID id : removed) vanishCandidates.remove(id);
            return;
        }

        for (PlayerInfoEvent ev : batch) {
            if (!shouldTrackVanish(ev.id, ev.name)) continue;
            if (!ev.added) {
                vanishCandidates.put(ev.id, ev.timestamp);
                continue;
            }
            Long removedAt = vanishCandidates.remove(ev.id);
            if (removedAt == null) continue;
            Long spawnedAt = recentEntitySpawns.get(ev.id);
            if (spawnedAt == null || now - spawnedAt >= 1500) {
                pendingVanishCycles.put(ev.id, removedAt);
            }
        }
    }

    private boolean shouldTrackVanish(UUID id, String name) {
        int limit = vanishPlayerLimit.get();
        if (limit > 0 && limit <= 1000 && getObservedPlayerCount() >= limit) return false;
        return !vanishKnownStaffOnly.get() || isStaff(id, name);
    }

    private int getObservedPlayerCount() {
        if (mc.getConnection() == null) return 0;
        int count = mc.getConnection().getOnlinePlayers().size();
        ServerData server = mc.getCurrentServer();
        if (server != null && server.players != null) {
            try {
                // server.players is ServerData.Players or similar containing online count
                // Use reflection-safe access via string? But we can just use size above.
                // Keep max logic if field accessible.
                if (server.players.online() > count) count = server.players.online();
            } catch (Throwable ignored) {}
        }
        return count;
    }

    private static final class PlayerInfoEvent {
        final UUID id;
        final String name;
        final boolean added;
        final long timestamp;
        PlayerInfoEvent(UUID id, String name, boolean added, long timestamp) {
            this.id = id;
            this.name = name;
            this.added = added;
            this.timestamp = timestamp;
        }
    }

    private void processPendingVanishCycles() {
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Long> entry : new HashMap<>(pendingVanishCycles).entrySet()) {
            if (now - entry.getValue() < 1500) continue;
            if (!pendingVanishCycles.remove(entry.getKey(), entry.getValue())) continue;
            String name = playerNames.get(entry.getKey());
            if (name == null) continue;
            long seconds = Math.max(0, (now - entry.getValue()) / 1000);
            String message = name + " Vanish cycle detected (" + seconds + "s hidden" + (serverHasVanish ? ", Vanish installed" : "") + ")";
            if (chatAlert.get()) ChatUtils.sendMsg(Component.literal("[StaffMonitor] " + message));
            if (toastAlert.get()) showToast(Component.literal("Vanish cycle detected"), Component.literal(message));
            if (soundAlert.get() && mc.level != null && mc.player != null) {
                SoundEvent event = sound.get().resolve();
                float target = (float) (volume.get() / 100.0);
                if (event != null && target > 0F) {
                    mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), event, SoundSource.PLAYERS, Math.max(0.2F, target), 1.6F, false);
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        tickStaffQuit();

        if (mc.getConnection() == null) {
            gamemodeStates.clear();
            hiddenPlayers.clear();
            hiddenPlayerAlertsActive = false;
            savedStaffNames.clear();
            cancelStaffQuit();
            return;
        }
        if (mojangStaff.get() && mojangStaffNames.isEmpty() && mojangStaffUuids.isEmpty())
            loadMojangStaff();

        processPlayerInfoEvents();
        processPendingVanishCycles();

        String serverKeyNow = resolveServerKey();
        if (!serverKeyNow.equals(lastServerKey)) {
            gamemodeStates.clear();
            hiddenPlayers.clear();
            alertedStaff.clear();
            vanishCandidates.clear();
            pendingVanishCycles.clear();
            pendingPlayerInfoEvents.clear();
            serverHasVanish = false;
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

        HashMap<UUID, GameType> nextStates = new HashMap<>();
        for (PlayerInfo entry : mc.getConnection().getOnlinePlayers()) {
            UUID id = entry.getProfile().id();
            String name = entry.getProfile().name();
            if (shouldIgnorePlayer(id, name))
                continue;

            GameType currentMode = entry.getGameMode();
            playerNames.put(id, name);
            nextStates.put(id, currentMode);

            // Hidden staff alert
            if (hiddenStaffAlerts.get()
                && isStaff(id, name)
                && alertedStaff.add(id))
                alertStaff(entry);

            GameType previous = gamemodeStates.get(id);
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

            // GameType changed — alert on any transition
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

        if (mc.level == null) {
            hiddenPlayers.clear();
            return;
        }

        HashMap<UUID, String> nextHiddenPlayers = new HashMap<>();
        for (Player player : mc.level.players()) {
            if (shouldIgnorePlayerEntity(player))
                continue;

            UUID id = player.getUUID();
            if (mc.getConnection().getPlayerInfo(id) != null)
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
        if (mc.getConnection() == null)
            return;

        for (PlayerInfo entry : mc.getConnection().getOnlinePlayers()) {
            UUID id = entry.getProfile().id();
            String name = entry.getProfile().name();
            if (shouldIgnorePlayer(id, name))
                continue;

            gamemodeStates.put(id, entry.getGameMode());
            playerNames.put(id, name);
        }
    }

    private void snapshotHiddenPlayers() {
        hiddenPlayers.clear();
        if (mc.getConnection() == null || mc.level == null)
            return;

        for (Player player : mc.level.players()) {
            if (shouldIgnorePlayerEntity(player))
                continue;

            UUID id = player.getUUID();
            if (mc.getConnection().getPlayerInfo(id) != null)
                continue;

            hiddenPlayers.put(id, player.getName().getString());
        }
    }

    private boolean shouldIgnorePlayerEntity(Player player) {
        if (player == null || FakePlayerManager.getFakePlayers().contains(player))
            return true;

        String nameStr = player.getName().getString();
        if (ignoreNpcNames.get() && isLikelyNpcName(nameStr))
            return true;

        return shouldIgnorePlayer(player.getUUID(), nameStr);
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

    private void showToast(Component title, Component message) {
        try {
            if (mc.gui.toastManager() == null) return;
            Runnable show = () -> SystemToast.add(mc.gui.toastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION, title, message);
            if (mc.isSameThread()) show.run();
            else mc.execute(show);
        } catch (Throwable ignored) {}
    }

    private void alert(PlayerInfo entry, GameType mode, boolean entered) {
        String name = entry.getProfile().name();
        String modeLabel = mode == null ? "unknown" : mode.getSerializedName();
        if (toastAlert.get()) {
            showToast(Component.literal("StaffMonitor"), Component.literal(name + " " + modeLabel));
        }
        if (chatAlert.get()) {
            String action = entered ? "entered" : "left";
            ChatUtils.sendMsg(Component.literal(String.format(Locale.ROOT,
                "[StaffMonitor] %s %s %s mode.", name, action, modeLabel)));
        }

        if (soundAlert.get() && mc.level != null && mc.player != null) {
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
                mc.level.playLocalSound(x, y, z, event, SoundSource.PLAYERS, 1F,
                    entered ? 1.2F : 0.85F, false);
            if (remainder > 0F)
                mc.level.playLocalSound(x, y, z, event, SoundSource.PLAYERS,
                    remainder, entered ? 1.2F : 0.85F, false);
        }
    }

    private void alertStaff(PlayerInfo entry) {
        String name = entry.getProfile().name();
        if (chatAlert.get())
            ChatUtils.sendMsg(Component.literal(String.format(Locale.ROOT,
                "[StaffMonitor] Staff member %s is online.", name)));
        if (toastAlert.get())
            showToast(Component.literal("StaffMonitor"), Component.literal("Staff " + name + " online"));

        if (soundAlert.get() && mc.level != null && mc.player != null) {
            SoundEvent event = sound.get().resolve();
            float target = (float) (volume.get() / 100.0);
            if (event == null || target <= 0F)
                return;

            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(),
                mc.player.getZ(), event, SoundSource.PLAYERS,
                Math.max(0.2F, target), 1.6F, false);
        }
    }

    private void alertHiddenPlayer(String name, boolean appeared) {
        if (chatAlert.get()) {
            String action = appeared ? "appeared off-tab" : "disappeared";
            ChatUtils.sendMsg(Component.literal(String.format(Locale.ROOT,
                "[StaffMonitor] %s %s.", name, action)));
        }
        if (toastAlert.get()) {
            showToast(Component.literal("Hidden player"), Component.literal(name + (appeared ? " appeared off-tab" : " disappeared")));
        }

        if (soundAlert.get() && mc.level != null && mc.player != null) {
            SoundEvent event = sound.get().resolve();
            float target = (float) (volume.get() / 100.0);
            if (event == null || target <= 0F)
                return;

            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(),
                mc.player.getZ(), event, SoundSource.PLAYERS,
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
        if (!mojangStaff.get()) return;
        long now = System.currentTimeMillis();
        if (now < nextMojangStaffRetry) return;

        mojangStaffNames.clear();
        mojangStaffUuids.clear();

        loadMojangStaffFile("/ChuckPack/staff/mojang-names.txt", false);
        // Fallback to Wurst resource path if ChuckPack path not found (for compatibility)
        if (mojangStaffNames.isEmpty()) loadMojangStaffFile("/wurst/staff/mojang-names.txt", false);
        loadMojangStaffFile("/ChuckPack/staff/mojang-uuids.txt", true);
        if (mojangStaffUuids.isEmpty()) loadMojangStaffFile("/wurst/staff/mojang-uuids.txt", true);

        if (mojangStaffNames.isEmpty() && mojangStaffUuids.isEmpty()) {
            nextMojangStaffRetry = now + 5 * 60_000L;
            if (now - lastMojangStaffError >= 10 * 60_000L) {
                lastMojangStaffError = now;
                ChatUtils.error("StaffMonitor: couldn't load the bundled Mojang staff list, so \"Mojang Staff\" won't work.");
            }
        }
    }

    private void loadMojangStaffFile(String resource, boolean uuidFile) {
        String content = readBundledResource(resource);
        if (content == null) return;
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
    }

    private static String readBundledResource(String resource) {
        try (InputStream in = StaffMonitor.class.getResourceAsStream(resource)) {
            if (in != null) return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // fall through
        }
        return readFromOwnJar(resource);
    }

    private static String readFromOwnJar(String resource) {
        try {
            URL codeSource = StaffMonitor.class.getProtectionDomain().getCodeSource().getLocation();
            if (codeSource == null) return null;
            Path jarPath;
            if (codeSource.getProtocol().equals("file")) {
                Path p = Paths.get(codeSource.toURI());
                if (Files.isDirectory(p)) return null;
                jarPath = p;
            } else if (codeSource.getProtocol().equals("jar")) {
                String spec = codeSource.getFile();
                int bang = spec.indexOf("!/");
                if (bang < 0) return null;
                jarPath = Paths.get(URI.create(spec.substring(0, bang)));
            } else return null;
            try (JarFile jar = new JarFile(jarPath.toFile())) {
                ZipEntry entry = jar.getEntry(resource.substring(1));
                if (entry == null) return null;
                try (InputStream in = jar.getInputStream(entry)) {
                    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            return null;
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
        if (!quitOnStaffEnter.get() || mc.getConnection() == null)
            return;

        for (PlayerInfo entry : mc.getConnection().getOnlinePlayers()) {
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
            ChatUtils.sendMsg(Component.literal(String.format(Locale.ROOT,
                "[StaffMonitor] Staff member %s is online.", staffName)));

        if (soundAlert.get() && mc.level != null && mc.player != null) {
            SoundEvent event = sound.get().resolve();
            float target = (float) (volume.get() / 100.0);
            if (event != null && target > 0F) {
                mc.level.playLocalSound(mc.player.getX(), mc.player.getY(),
                    mc.player.getZ(), event, SoundSource.PLAYERS,
                    Math.max(0.2F, target), 1.6F, false);
            }
        }

        AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
        if (autoReconnect != null && autoReconnect.isActive())
            autoReconnect.disable();

        ChatUtils.sendMsg(Component.literal(String.format(Locale.ROOT,
            "[StaffMonitor] Staff detected: %s. Quitting in %ds.", staffName,
            quitDelay.get())));
    }

    private void tickStaffQuit() {
        if (staffQuitTicks < 0)
            return;

        if (mc.getConnection() == null || mc.level == null) {
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
        if (mc.level == null) {
            cancelStaffQuit();
            return;
        }

        AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
        if (autoReconnect != null && autoReconnect.isActive())
            autoReconnect.disable();

        mc.level.disconnect(Component.literal("StaffMonitor quit: " + staffQuitReason));
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

    private static boolean isStaffMode(GameType mode) {
        return mode == GameType.CREATIVE || mode == GameType.SPECTATOR;
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
        if (id != null && mc.getUser() != null
            && id.equals(mc.getUser().getProfileId()))
            return true;

        if (mc.player != null && id != null && id.equals(mc.player.getUUID()))
            return true;

        if (name == null)
            return false;

        if (mc.getUser() != null
            && name.equalsIgnoreCase(mc.getUser().getName()))
            return true;

        if (ignoreFriends.get() && Friends.get() != null && Friends.get().get(name) != null) return true;
        return false;
    }

    private String resolveServerKey() {
        ServerData info = mc.getCurrentServer();
        if (info != null) {
            if (info.ip != null && !info.ip.isEmpty())
                return info.ip.replace(':', '_');
            if (info.isRealm())
                return "realms_" + (info.name == null ? "" : info.name);
            if (info.name != null && !info.name.isEmpty())
                return "server_" + info.name;
        }
        if (mc.hasSingleplayerServer())
            return "singleplayer";
        return "unknown";
    }

    private String resolveStorageServerKey() {
        return resolveServerKey().replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
