package net.aero.aeropack.modules.misc;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.aero.aeropack.modules.misc.oppstats.OppStatsScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class OppStats extends Module {
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final Pattern LOBBY_BOT_NAME =
        Pattern.compile("^\\s*[:;.,'`~!@#\\-_=+]?\\d{1,4}\\s*$");

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> ignoreNpcs = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore NPCs")
        .description("Filters likely NPC entities and identities from OppStats.")
        .defaultValue(true)
        .build()
    );

    private final Map<UUID, OppRecord> records = new HashMap<>();
    private final LinkedHashSet<UUID> lastOnline = new LinkedHashSet<>();
    private String lastServerKey = "unknown";
    private Path currentFile;
    private boolean dirty;
    private long lastSaveAt;

    public OppStats() {
        super(Categories.Misc, "Opp Stats", "Tracks players per server and builds a progressive dossier.");
    }

    @Override
    public void onActivate() {
        records.clear();
        lastOnline.clear();
        lastServerKey = resolveServerKey();
        currentFile = resolveDataFile(lastServerKey);
        loadCurrentServerData();
        bootstrapFromTablist();
    }

    @Override
    public void onDeactivate() {
        saveIfNeeded(true);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.getConnection() == null)
            return;

        String keyNow = resolveServerKey();
        if (!keyNow.equals(lastServerKey)) {
            saveIfNeeded(true);
            records.clear();
            lastOnline.clear();
            lastServerKey = keyNow;
            currentFile = resolveDataFile(lastServerKey);
            loadCurrentServerData();
            bootstrapFromTablist();
        }

        long now = System.currentTimeMillis();
        Set<UUID> onlineNow = new LinkedHashSet<>();
        for (PlayerListEntry entry : mc.getConnection().getPlayerList()) {
            UUID id = entry.getProfile().id();
            String name = entry.getProfile().name();
            if (ignoreNpcs.get() && isBotLikeIdentity(id, name))
                continue;

            onlineNow.add(id);
            OppRecord rec = records.computeIfAbsent(id, k -> new OppRecord(id, name));
            rec.name = name;
            rec.online = true;
            rec.ping = entry.getLatency();
            rec.gamemode = entry.getGameMode() == null ? "N/A" : entry.getGameMode().name();
            rec.tabSeenAt = now;
            if (!lastOnline.contains(id)) {
                rec.joinCount++;
                rec.lastJoinAt = now;
                rec.addEvent("Joined server");
            }
            dirty = true;
        }
        if (ignoreNpcs.get())
            records.entrySet().removeIf(e -> isHardLobbyBotName(e.getValue().name));

        for (UUID wasOnline : lastOnline) {
            if (onlineNow.contains(wasOnline))
                continue;
            OppRecord rec = records.get(wasOnline);
            if (rec == null)
                continue;
            rec.online = false;
            rec.lastLeaveAt = now;
            rec.addEvent("Left server");
            dirty = true;
        }
        lastOnline.clear();
        lastOnline.addAll(onlineNow);

        if (mc.level != null && mc.player != null) {
            for (PlayerEntity p : mc.level.getPlayers()) {
                if (p == mc.player)
                    continue;
                if (!onlineNow.contains(p.getUUID()))
                    continue;
                if (ignoreNpcs.get() && isBotLikeEntity(p.getUUID(), p.getName().getString()))
                    continue;

                OppRecord rec = records.computeIfAbsent(p.getUUID(),
                    k -> new OppRecord(p.getUUID(), p.getName().getString()));
                updateFromLivePlayer(rec, p, now);
                dirty = true;
            }
        }

        saveIfNeeded(false);
    }

    private void updateFromLivePlayer(OppRecord rec, PlayerEntity p, long now) {
        rec.name = p.getName().getString();
        rec.lastPos = new Vec3d(p.getX(), p.getY(), p.getZ());
        rec.lastSeenAt = now;
        rec.distance = mc.player == null ? Double.NaN : p.distanceTo(mc.player);
        rec.health = p.getHealth();
        rec.absorption = p.getAbsorptionAmount();
        rec.armorValue = p.getArmor();
        rec.mainHand = observeEquipment(rec, "Main hand", rec.mainHand, p.getMainItemStack(), now);
        rec.offHand = observeEquipment(rec, "Off hand", rec.offHand, p.getOffhandItem(), now);
        rec.helmet = observeEquipment(rec, "Helmet", rec.helmet, p.getEquippedStack(EquipmentSlot.HEAD), now);
        rec.chest = observeEquipment(rec, "Chestplate", rec.chest, p.getEquippedStack(EquipmentSlot.CHEST), now);
        rec.legs = observeEquipment(rec, "Leggings", rec.legs, p.getEquippedStack(EquipmentSlot.LEGS), now);
        rec.boots = observeEquipment(rec, "Boots", rec.boots, p.getEquippedStack(EquipmentSlot.FEET), now);
    }

    private String observeEquipment(OppRecord rec, String slot, String previous, ItemStack stack, long now) {
        String next = stackInfo(stack);
        if (next.equals(previous))
            return next;

        if (!"N/A".equals(next)) {
            rec.addRecentItem(slot + ": " + next, now);
            if (previous != null && !"N/A".equals(previous))
                rec.addEvent(slot + " changed to " + compactStackInfo(next));
        }

        return next;
    }

    private void bootstrapFromTablist() {
        if (mc.getConnection() == null)
            return;

        long now = System.currentTimeMillis();
        for (PlayerListEntry entry : mc.getConnection().getPlayerList()) {
            UUID id = entry.getProfile().id();
            String name = entry.getProfile().name();
            if (ignoreNpcs.get() && isBotLikeIdentity(id, name))
                continue;
            OppRecord rec = records.computeIfAbsent(id, k -> new OppRecord(id, name));
            rec.name = name;
            rec.online = true;
            rec.ping = entry.getLatency();
            rec.gamemode = entry.getGameMode() == null ? "N/A" : entry.getGameMode().name();
            rec.tabSeenAt = now;
            lastOnline.add(id);
        }
    }

    public List<OppRecord> getOnlineRecords() {
        return records.values().stream().filter(r -> r.online)
            .filter(r -> !isHardLobbyBotName(r.name))
            .sorted(Comparator.comparing((OppRecord r) -> r.name.toLowerCase(Locale.ROOT)))
            .toList();
    }

    public List<OppRecord> getHistoricalRecords() {
        return records.values().stream().filter(r -> !r.online)
            .filter(r -> !isHardLobbyBotName(r.name))
            .sorted(Comparator.comparing((OppRecord r) -> r.name.toLowerCase(Locale.ROOT)))
            .toList();
    }

    public String formatForClipboard(OppRecord r) {
        String pos = r.lastPos == null ? "N/A" : String.format(Locale.ROOT,
            "(%.2f, %.2f, %.2f)", r.lastPos.x, r.lastPos.y, r.lastPos.z);
        String dist = Double.isNaN(r.distance) ? "N/A"
            : String.format(Locale.ROOT, "%.2f", r.distance);
        String lastSeen = r.lastSeenAt <= 0 ? "N/A"
            : TS_FORMAT.format(Instant.ofEpochMilli(r.lastSeenAt));
        return "Name: " + r.name + "\nUUID: " + r.uuid + "\nOnline: " + r.online
            + "\nPosition: " + pos + "\nDistance: " + dist
            + "\nTarget status: health=" + formatFloat(r.health)
            + ", absorption=" + formatFloat(r.absorption) + ", armor="
            + r.armorValue + ", ping=" + r.ping + ", gamemode="
            + nullToNA(r.gamemode) + "\nMain hand: " + r.mainHand
            + "\nOff hand: " + r.offHand + "\nHelmet: " + r.helmet
            + "\nChestplate: " + r.chest + "\nLeggings: " + r.legs + "\nBoots: "
            + r.boots + "\nRecent observed items:\n"
            + (r.recentItems.isEmpty() ? "N/A" : String.join("\n", r.recentItems))
            + "\nJoins: " + r.joinCount + "\nLast join: "
            + formatEpoch(r.lastJoinAt) + "\nLast leave: "
            + formatEpoch(r.lastLeaveAt) + "\nLast seen: " + lastSeen
            + "\nEvent log:\n" + String.join("\n", r.events);
    }

    public String formatLastSeen(OppRecord r) {
        return formatEpoch(r.lastSeenAt);
    }

    public String formatEpoch(long epochMs) {
        if (epochMs <= 0)
            return "N/A";
        return TS_FORMAT.format(Instant.ofEpochMilli(epochMs));
    }

    private String formatFloat(float v) {
        if (Float.isNaN(v))
            return "N/A";
        return String.format(Locale.ROOT, "%.1f", v);
    }

    private String stackInfo(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return "N/A";
        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        String ench = getEnchantmentSummary(stack);
        String dur = stack.isDamageable()
            ? (stack.getMaxDamage() - stack.getDamage()) + "/" + stack.getMaxDamage()
            : "N/A";
        return stack.getName().getString() + " x" + stack.getCount()
            + " [id=" + itemId + ", durability=" + dur + ", enchants="
            + (ench.isBlank() ? "none" : ench) + "]";
    }

    private String compactStackInfo(String info) {
        int idIndex = info.indexOf(" [id=");
        return idIndex > 0 ? info.substring(0, idIndex) : info;
    }

    private String getEnchantmentSummary(ItemStack stack) {
        ArrayList<String> parts = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        appendEnchantments(parts, seen, stack.getComponents()
            .getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT));
        appendEnchantments(parts, seen, stack.getComponents()
            .getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT));
        LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
        appendLoreEnchantments(parts, seen, lore);
        return String.join(", ", parts);
    }

    private static void appendEnchantments(List<String> out, Set<String> seen,
        ItemEnchantmentsComponent enchantments) {
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantments.getEnchantmentEntries()) {
            RegistryEntry<Enchantment> holder = entry.getKey();
            int lvl = entry.getIntValue();
            String key = holder.getKey().map(k -> k.getValue().toString()).orElse("?") + "#" + lvl;
            if (!seen.add(key))
                continue;
            out.add(Enchantment.getName(holder, lvl).getString());
        }
    }

    private static void appendLoreEnchantments(List<String> out, Set<String> seen, LoreComponent lore) {
        if (lore == null)
            return;

        for (Text line : lore.lines()) {
            String text = line.getString().trim();
            if (text.isBlank())
                continue;
            if (!seen.add(text + "#lore"))
                continue;
            out.add(text);
        }
    }

    private void saveIfNeeded(boolean force) {
        if (!dirty || currentFile == null)
            return;
        long now = System.currentTimeMillis();
        if (!force && now - lastSaveAt < 2000L)
            return;

        JsonObject root = new JsonObject();
        JsonArray arr = new JsonArray();
        for (OppRecord rec : records.values())
            arr.add(rec.toJson());
        root.add("players", arr);
        try {
            Files.createDirectories(currentFile.getParent());
            Files.writeString(currentFile,
                new GsonBuilder().setPrettyPrinting().create().toJson(root),
                StandardCharsets.UTF_8);
            dirty = false;
            lastSaveAt = now;
        } catch (Exception e) {
            ChatUtils.error("OppStats save failed: " + e.getMessage());
        }
    }

    private void loadCurrentServerData() {
        if (currentFile == null || !Files.exists(currentFile))
            return;
        try {
            JsonObject root = JsonParser
                .parseString(Files.readString(currentFile, StandardCharsets.UTF_8))
                .getAsJsonObject();
            if (!root.has("players") || !root.get("players").isJsonArray())
                return;
            for (var el : root.getAsJsonArray("players")) {
                if (!el.isJsonObject())
                    continue;
                OppRecord rec = OppRecord.fromJson(el.getAsJsonObject());
                records.put(rec.uuid, rec);
            }

            // Saved online state is stale across sessions.
            // Current tab list should be the only source of truth.
            for (OppRecord rec : records.values())
                rec.online = false;
        } catch (Exception e) {
            ChatUtils.error("OppStats load failed: " + e.getMessage());
        }
    }

    private Path resolveDataFile(String serverKey) {
        String safe = serverKey.replaceAll("[^a-zA-Z0-9._-]", "_");
        return MeteorClient.FOLDER.toPath().resolve("oppstats").resolve(safe + ".json");
    }

    private String resolveServerKey() {
        ServerInfo info = mc.getCurrentServer();
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

    public void openScreen() {
        Screen prev = mc.currentScreen;
        mc.setScreen(new OppStatsScreen(prev, this));
    }

    @Override
    public meteordevelopment.meteorclient.gui.widgets.WWidget getWidget(meteordevelopment.meteorclient.gui.GuiTheme theme) {
        openScreen();
        return theme.label("");
    }

    private String nullToNA(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private boolean isBotLikeIdentity(UUID uuid, String name) {
        if (isHardLobbyBotName(name))
            return true;
        return isLikelyNpc(uuid, name);
    }

    private boolean isBotLikeEntity(UUID uuid, String displayName) {
        if (isHardLobbyBotName(displayName))
            return true;
        return isLikelyNpc(uuid, displayName);
    }

    private boolean isLikelyNpc(UUID uuid, String name) {
        if (uuid == null || uuid.equals(new UUID(0L, 0L)))
            return true;
        if (name == null || name.isBlank())
            return true;

        String lower = name.toLowerCase(Locale.ROOT);
        return lower.contains("[npc]") || lower.contains("_npc")
            || lower.startsWith("[npc]") || lower.contains("[cit]")
            || lower.matches("cit-[0-9a-f-]{6,}");
    }

    private boolean isHardLobbyBotName(String rawName) {
        if (rawName == null)
            return true;

        String name = rawName.trim();
        if (name.isEmpty())
            return true;
        if (LOBBY_BOT_NAME.matcher(name).matches())
            return true;

        // Normalize weird lobby prefixes/symbols and re-check.
        String normalized = name.replaceAll("[^A-Za-z0-9_.]", "");
        if (normalized.matches("^\\.?\\d{1,4}$"))
            return true;

        int digits = 0;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isDigit(c)) {
                digits++;
                continue;
            }
            if (Character.isLetter(c) || c == '_')
                return false;
        }

        return digits > 0 && digits <= 4;
    }

    public static final class OppRecord {
        public final UUID uuid;
        public String name;
        public boolean online;
        public int ping = -1;
        public String gamemode = "N/A";
        public Vec3d lastPos;
        public double distance = Double.NaN;
        public float health = Float.NaN;
        public float absorption = Float.NaN;
        public int armorValue = -1;
        public String mainHand = "N/A";
        public String offHand = "N/A";
        public String helmet = "N/A";
        public String chest = "N/A";
        public String legs = "N/A";
        public String boots = "N/A";
        public long lastSeenAt;
        public long tabSeenAt;
        public long lastJoinAt;
        public long lastLeaveAt;
        public int joinCount;
        public final ArrayList<String> events = new ArrayList<>();
        public final ArrayList<String> recentItems = new ArrayList<>();

        public OppRecord(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name == null ? "unknown" : name;
        }

        public void addEvent(String text) {
            String line = TS_FORMAT.format(Instant.now()) + " - " + text;
            events.add(0, line);
            while (events.size() > 80)
                events.remove(events.size() - 1);
        }

        public void addRecentItem(String text, long epochMs) {
            String line = TS_FORMAT.format(Instant.ofEpochMilli(epochMs)) + " - " + text;
            recentItems.removeIf(s -> s.endsWith(text));
            recentItems.add(0, line);
            while (recentItems.size() > 36)
                recentItems.remove(recentItems.size() - 1);
        }

        JsonObject toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("uuid", uuid.toString());
            o.addProperty("name", name);
            o.addProperty("online", online);
            o.addProperty("ping", ping);
            o.addProperty("gamemode", gamemode);
            if (lastPos != null) {
                o.addProperty("x", lastPos.x);
                o.addProperty("y", lastPos.y);
                o.addProperty("z", lastPos.z);
            }
            o.addProperty("distance", distance);
            o.addProperty("health", health);
            o.addProperty("absorption", absorption);
            o.addProperty("armorValue", armorValue);
            o.addProperty("mainHand", mainHand);
            o.addProperty("offHand", offHand);
            o.addProperty("helmet", helmet);
            o.addProperty("chest", chest);
            o.addProperty("legs", legs);
            o.addProperty("boots", boots);
            o.addProperty("lastSeenAt", lastSeenAt);
            o.addProperty("tabSeenAt", tabSeenAt);
            o.addProperty("lastJoinAt", lastJoinAt);
            o.addProperty("lastLeaveAt", lastLeaveAt);
            o.addProperty("joinCount", joinCount);
            JsonArray ev = new JsonArray();
            for (String e : events)
                ev.add(e);
            o.add("events", ev);
            JsonArray recent = new JsonArray();
            for (String item : recentItems)
                recent.add(item);
            o.add("recentItems", recent);
            return o;
        }

        static OppRecord fromJson(JsonObject o) throws IOException {
            if (!o.has("uuid"))
                throw new IOException("Missing UUID");
            UUID id = UUID.fromString(o.get("uuid").getAsString());
            OppRecord r = new OppRecord(id, get(o, "name", "unknown"));
            r.online = getBool(o, "online", false);
            r.ping = getInt(o, "ping", -1);
            r.gamemode = get(o, "gamemode", "N/A");
            if (o.has("x") && o.has("y") && o.has("z"))
                r.lastPos = new Vec3d(o.get("x").getAsDouble(),
                    o.get("y").getAsDouble(), o.get("z").getAsDouble());
            r.distance = getDouble(o, "distance", Double.NaN);
            r.health = (float) getDouble(o, "health", Double.NaN);
            r.absorption = (float) getDouble(o, "absorption", Double.NaN);
            r.armorValue = getInt(o, "armorValue", -1);
            r.mainHand = get(o, "mainHand", "N/A");
            r.offHand = get(o, "offHand", "N/A");
            r.helmet = get(o, "helmet", "N/A");
            r.chest = get(o, "chest", "N/A");
            r.legs = get(o, "legs", "N/A");
            r.boots = get(o, "boots", "N/A");
            r.lastSeenAt = getLong(o, "lastSeenAt", 0L);
            r.tabSeenAt = getLong(o, "tabSeenAt", 0L);
            r.lastJoinAt = getLong(o, "lastJoinAt", 0L);
            r.lastLeaveAt = getLong(o, "lastLeaveAt", 0L);
            r.joinCount = getInt(o, "joinCount", 0);
            if (o.has("events") && o.get("events").isJsonArray())
                for (var ev : o.getAsJsonArray("events"))
                    r.events.add(ev.getAsString());
            if (o.has("recentItems") && o.get("recentItems").isJsonArray())
                for (var item : o.getAsJsonArray("recentItems"))
                    r.recentItems.add(item.getAsString());
            return r;
        }

        private static String get(JsonObject o, String key, String fallback) {
            return o.has(key) ? o.get(key).getAsString() : fallback;
        }

        private static int getInt(JsonObject o, String key, int fallback) {
            return o.has(key) ? o.get(key).getAsInt() : fallback;
        }

        private static long getLong(JsonObject o, String key, long fallback) {
            return o.has(key) ? o.get(key).getAsLong() : fallback;
        }

        private static double getDouble(JsonObject o, String key, double fallback) {
            return o.has(key) ? o.get(key).getAsDouble() : fallback;
        }

        private static boolean getBool(JsonObject o, String key, boolean fallback) {
            return o.has(key) ? o.get(key).getAsBoolean() : fallback;
        }
    }
}
