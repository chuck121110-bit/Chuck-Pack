package net.aero.aeropack.modules.misc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.state.NetworkState;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NbtFilter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> maxBlockEntityNbtSizeKb = sgGeneral.add(new IntSetting.Builder()
        .name("Block entity limit")
        .description("Reject block/entity data larger than this.")
        .defaultValue(4)
        .min(1)
        .max(1024)
        .sliderMin(1)
        .sliderMax(1024)
        .build()
    );

    private final Setting<Integer> maxItemStackNbtSizeKb = sgGeneral.add(new IntSetting.Builder()
        .name("Item stack limit")
        .description("Reject individual items larger than this.")
        .defaultValue(64)
        .min(1)
        .max(8192)
        .sliderMin(1)
        .sliderMax(8192)
        .build()
    );

    private final Setting<Double> maxSuspiciousPacketSizeMb = sgGeneral.add(new DoubleSetting.Builder()
        .name("Packet size limit")
        .description("Reject suspicious NBT packets larger than this.")
        .defaultValue(2)
        .min(0.1)
        .max(32)
        .sliderMin(0.1)
        .sliderMax(32)
        .build()
    );

    private final Setting<Double> inventoryNbtLimitMb = sgGeneral.add(new DoubleSetting.Builder()
        .name("Inventory overload limit")
        .description("Do not open containers when your whole inventory exceeds this size.")
        .defaultValue(2)
        .min(0.5)
        .max(32)
        .sliderMin(0.5)
        .sliderMax(32)
        .build()
    );

    private final Setting<Boolean> blockOverloadedContainers = sgGeneral.add(new BoolSetting.Builder()
        .name("Block overloaded containers")
        .description("Cancel container interaction before a large inventory can create an oversized packet.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> failClosedOnExceptions = sgGeneral.add(new BoolSetting.Builder()
        .name("Block packets on errors")
        .description("Cancel a packet if safely inspecting it fails.")
        .defaultValue(true)
        .build()
    );

    private int chunksBlocked;
    private int blockEntitiesBlocked;
    private int bundlesScanned;
    private int entityMetadataBlocked;
    private int itemStacksBlocked;
    private int quarantinedChunks;
    private int quarantinedEntities;
    private int quietBlockedPackets;
    private long lastStatusMessageMs;
    private long lastInventoryWarningMs;
    private final Set<ChunkPos> bannedChunks = new HashSet<>();
    private final Set<Integer> bannedEntityIds = new HashSet<>();
    private final Map<Integer, ChunkPos> entityIdToChunk = new HashMap<>();

    public NbtFilter() {
        super(Categories.Misc, "NBT Filter", "Rejects suspiciously large NBT and item data packets and prevents oversized container traffic.");
    }

    @Override
    public void onActivate() {
        chunksBlocked = 0;
        blockEntitiesBlocked = 0;
        bundlesScanned = 0;
        entityMetadataBlocked = 0;
        itemStacksBlocked = 0;
        quarantinedChunks = 0;
        quarantinedEntities = 0;
        quietBlockedPackets = 0;
        lastStatusMessageMs = 0;
        bannedChunks.clear();
        bannedEntityIds.clear();
        entityIdToChunk.clear();
    }

    @Override
    public void onDeactivate() {
    }

    @EventHandler
    private void onInteractBlock(InteractBlockEvent event) {
        if (!blockOverloadedContainers.get() || mc.player == null || mc.world == null)
            return;

        if (!(event.result instanceof BlockHitResult hit))
            return;

        if (!(mc.world.getBlockEntity(hit.getBlockPos()) instanceof LockableContainerBlockEntity))
            return;

        long total = getInventoryNbtSize();
        if (total <= inventoryNbtLimitBytes())
            return;

        event.cancel();
        warnInventoryOverload(total);
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!isContainerActionPacket(event.packet))
            return;
        if (shouldBlockContainerAction(event.packet))
            event.cancel();
    }

    private boolean isContainerActionPacket(Packet<?> packet) {
        if (packet instanceof ClickSlotC2SPacket
            || packet instanceof ButtonClickC2SPacket
            || packet instanceof PlayerInteractBlockC2SPacket)
            return true;

        // Keep this compatible with mappings and helper packets that use a
        // different concrete class name but still belong to a container.
        return packet != null && packet.getClass().getSimpleName()
            .toLowerCase(Locale.ROOT).contains("container");
    }

    private boolean shouldBlockContainerAction(Packet<?> packet) {
        if (!blockOverloadedContainers.get())
            return false;

        if (isInventoryOverloaded())
            return true;

        InspectionResult result = checkItemStackCarrierPacket(packet);
        if (!result.dangerous())
            return false;

        sendRed("Container packet blocked: "
            + result.reason() + formatResultDetails(result));
        return true;
    }

    private boolean isInventoryOverloaded() {
        if (!blockOverloadedContainers.get())
            return false;

        long total = getInventoryNbtSize();
        if (total <= inventoryNbtLimitBytes())
            return false;

        warnInventoryOverload(total);
        return true;
    }

    private void warnInventoryOverload(long total) {
        long now = System.currentTimeMillis();
        if (now - lastInventoryWarningMs < 1000L)
            return;
        lastInventoryWarningMs = now;
        sendRed("Container action blocked: your inventory contains "
            + formatBytes(total)
            + " of NBT data (limit " + formatBytes(inventoryNbtLimitBytes())
            + ").");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        Packet<?> packet = event.packet;

        // This module is intended for inventory/container protection. Do not
        // inspect chunks, block entities, entity metadata, or bundles here:
        // those packets can contain server-controlled recursive data and are
        // unrelated to preventing oversized inventory/container packets.
        if (!(packet instanceof InventoryS2CPacket)
            && !(packet instanceof ScreenHandlerSlotUpdateS2CPacket)
            && !(packet instanceof EntityTrackerUpdateS2CPacket))
            return;

        InspectionResult result = checkItemStackCarrierPacket(packet);
        if (!result.dangerous())
            return;

        event.cancel();
        // The server may have opened the screen before sending its contents.
        // Remove that screen as well so the oversized container data cannot be
        // interacted with after its contents packet was rejected.
        if (mc.currentScreen instanceof HandledScreen<?>)
            mc.setScreen(null);
        recordBlocked(result);
    }

    private InspectionResult inspectPacket(Packet<?> packet) {
        if (packet == null)
            return InspectionResult.safe();

        try {
            if (packet instanceof EntitiesDestroyS2CPacket remove) {
                for (int id : remove.getEntityIds()) {
                    bannedEntityIds.remove(id);
                    entityIdToChunk.remove(id);
                }
                return InspectionResult.safe();
            }

            if (packet instanceof BundleS2CPacket bundle)
                return checkBundlePacket(bundle);

            InspectionResult quarantineHit = checkQuarantinePacket(packet);
            if (quarantineHit.dangerous())
                return quarantineHit;

            updateTrackedEntityChunk(packet);
            InspectionResult trackedQuarantineHit =
                checkTrackedEntityQuarantine(packet);
            if (trackedQuarantineHit.dangerous())
                return trackedQuarantineHit;

            return inspectDangerousPacket(packet);

        } catch (Throwable t) {
            if (failClosedOnExceptions.get())
                return InspectionResult.dangerous(PacketKind.PACKET,
                    "inspection exception: " + t.getClass().getSimpleName(), -1,
                    null, null);

            return InspectionResult.safe();
        }
    }

    private InspectionResult inspectDangerousPacket(Packet<?> packet) {
        if (packet instanceof ChunkDataS2CPacket chunkPkt)
            return checkChunkPacket(chunkPkt);

        if (packet instanceof BlockEntityUpdateS2CPacket bePkt)
            return checkBlockEntityPacket(bePkt);

        if (packet instanceof EntityTrackerUpdateS2CPacket metadata)
            return checkEntityMetadataPacket(metadata);

        if (packet instanceof InventoryS2CPacket content)
            return checkItemStackCarrierPacket(content);
        if (packet instanceof ScreenHandlerSlotUpdateS2CPacket slot)
            return checkItemStackCarrierPacket(slot);
        if (packet instanceof EntityEquipmentUpdateS2CPacket equipment)
            return checkEquipmentPacket(equipment);

        return InspectionResult.safe();
    }

    private InspectionResult checkChunkPacket(ChunkDataS2CPacket packet) {
        ChunkPos chunkPos = new ChunkPos(packet.getChunkX(), packet.getChunkZ());
        if (isBannedChunk(chunkPos))
            return InspectionResult.dangerous(PacketKind.CHUNK,
                "quarantined chunk", -1, chunkPos, null);

        try {
            int sizeBytes =
                packet.getChunkData().getSectionsDataBuf().readableBytes();
            double sizeMB = sizeBytes / (1024.0 * 1024.0);

            if (sizeMB > maxSuspiciousPacketSizeMb.get()) {
                quarantineChunk(chunkPos);
                return InspectionResult.dangerous(PacketKind.CHUNK,
                    "chunk payload "
                        + String.format(Locale.ROOT, "%.2f", sizeMB) + " MB",
                    sizeBytes, chunkPos, null);
            }

            InspectionResult beResult = sanitizeChunkBlockEntityRecords(
                packet.getChunkData(), chunkPos);
            if (beResult.dangerous())
                return beResult;

        } catch (Throwable t) {
            if (failClosedOnExceptions.get()) {
                quarantineChunk(chunkPos);
                return InspectionResult.dangerous(PacketKind.CHUNK,
                    "chunk inspection exception: "
                        + t.getClass().getSimpleName(),
                    -1, chunkPos, null);
            }
        }

        return InspectionResult.safe();
    }

    private InspectionResult sanitizeChunkBlockEntityRecords(Object chunkData,
        ChunkPos fallbackChunk) {
        Object records = getChunkBlockEntityRecords(chunkData);
        if (!(records instanceof Iterable<?> iterable))
            return InspectionResult.safe();

        int strippedRecords = 0;
        boolean unsafeRecordFound = false;
        var iterator = iterable.iterator();

        while (iterator.hasNext()) {
            Object record = iterator.next();
            NbtCompound tag = findCompoundTag(record);
            if (tag == null)
                continue;

            InspectionResult result = checkTagAgainstLimits(tag,
                PacketKind.CHUNK, fallbackChunk, null);
            if (result.dangerous()) {
                unsafeRecordFound = true;
                int removed = removeCurrentRecord(iterator,
                    chunkData, records, record, fallbackChunk);
                if (removed > 0) {
                    strippedRecords += removed;
                    if (removed > 1)
                        break;
                    continue;
                }
            }
        }

        if (strippedRecords > 0)
            debugSanitizedChunk(fallbackChunk, strippedRecords);

        if (unsafeRecordFound && shouldHardQuarantineChunks())
            quarantineChunk(fallbackChunk);

        return InspectionResult.safe();
    }

    private Object getChunkBlockEntityRecords(Object chunkData) {
        for (String methodName : new String[]{"getBlockEntitiesData",
            "blockEntitiesData", "getBlockEntityData", "blockEntityData",
            "getBlockEntities", "blockEntities"}) {
            Object records = invokeNoArg(chunkData, methodName);
            if (records instanceof Iterable<?>)
                return records;
        }

        if (chunkData == null)
            return null;

        for (var field : chunkData.getClass().getDeclaredFields()) {
            if (!Iterable.class.isAssignableFrom(field.getType()))
                continue;

            try {
                field.setAccessible(true);
                Object records = field.get(chunkData);
                if (records instanceof Iterable<?>)
                    return records;

            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private int removeCurrentRecord(java.util.Iterator<?> iterator,
        Object chunkData, Object records, Object record, ChunkPos chunk) {
        try {
            iterator.remove();
            return 1;

        } catch (Throwable ignored) {
        }

        if (records instanceof Collection<?> collection) {
            try {
                return collection.remove(record) ? 1 : 0;

            } catch (Throwable ignored) {
            }
        }

        return replaceDangerousBlockEntityRecords(chunkData, records, chunk);
    }

    private int replaceDangerousBlockEntityRecords(Object chunkData,
        Object records, ChunkPos chunk) {
        if (!(records instanceof Iterable<?> iterable))
            return 0;

        java.util.ArrayList<Object> safeRecords = new java.util.ArrayList<>();
        int removed = 0;
        for (Object candidate : iterable) {
            NbtCompound tag = findCompoundTag(candidate);
            InspectionResult result = tag == null ? InspectionResult.safe()
                : checkTagAgainstLimits(tag, PacketKind.CHUNK, chunk, null);
            if (result.dangerous()) {
                removed++;
                continue;
            }

            safeRecords.add(candidate);
        }

        if (removed <= 0)
            return 0;

        if (records instanceof Collection<?> collection) {
            try {
                collection.clear();
                @SuppressWarnings({"rawtypes", "unchecked"})
                Collection raw = collection;
                raw.addAll(safeRecords);
                return removed;

            } catch (Throwable ignored) {
            }
        }

        if (replaceChunkBlockEntityRecordsField(chunkData, safeRecords))
            return removed;

        return 0;
    }

    private boolean replaceChunkBlockEntityRecordsField(Object chunkData,
        java.util.List<Object> safeRecords) {
        if (chunkData == null)
            return false;

        for (var field : chunkData.getClass().getDeclaredFields()) {
            if (!List.class.isAssignableFrom(field.getType()))
                continue;

            try {
                field.setAccessible(true);
                field.set(chunkData, safeRecords);
                return true;

            } catch (Throwable ignored) {
            }
        }

        return false;
    }

    private void debugSanitizedChunk(ChunkPos chunk, int strippedRecords) {
        if (mc.player == null)
            return;

        sendRed("Sanitized chunk [" + chunkX(chunk)
            + ", " + chunkZ(chunk) + "] - stripped " + strippedRecords
            + " dangerous block entity record(s).");
    }

    private InspectionResult checkBlockEntityPacket(
        BlockEntityUpdateS2CPacket packet) {
        ChunkPos chunkPos = new ChunkPos(packet.getPos());
        if (isBannedChunk(chunkPos))
            return InspectionResult.dangerous(PacketKind.BLOCK_ENTITY,
                "block entity in quarantined chunk", -1, chunkPos, null);

        NbtCompound tag = packet.getNbt();
        if (tag == null)
            return InspectionResult.safe();

        InspectionResult result =
            checkTagAgainstLimits(tag, PacketKind.BLOCK_ENTITY, chunkPos, null);
        if (result.dangerous()) {
            if (shouldHardQuarantineChunks())
                quarantineChunk(chunkPos);
            return result;
        }

        return InspectionResult.safe();
    }

    private InspectionResult checkEntityMetadataPacket(
        EntityTrackerUpdateS2CPacket packet) {
        int entityId = packet.id();
        if (isBannedEntity(entityId))
            return InspectionResult.dangerous(PacketKind.ENTITY_METADATA,
                "quarantined entity metadata", -1,
                entityIdToChunk.get(entityId), entityId);

        List<?> dataList = getPacketDataList(packet);
        if (dataList == null)
            return InspectionResult.safe();

        for (Object dataValue : dataList) {
            Object value = extractDataValue(dataValue);
            InspectionResult result = checkObjectForDangerousPayload(value,
                PacketKind.ENTITY_METADATA, entityIdToChunk.get(entityId),
                entityId);
            if (!result.dangerous())
                continue;

            quarantineEntity(entityId);
            ChunkPos chunk = entityIdToChunk.get(entityId);
            if (chunk != null)
                quarantineChunk(chunk);
            return result;
        }

        return InspectionResult.safe();
    }

    private InspectionResult checkItemStackCarrierPacket(Object packet) {
        long total = 0;
        for (ItemStack stack : findItemStacks(packet)) {
            InspectionResult result = checkItemStackAgainstLimits(stack,
                PacketKind.ITEM_STACK, null, null);
            if (result.dangerous())
                return result;

            total = saturatingAdd(total, estimateItemStackNbtSize(stack));
            if (total > inventoryNbtLimitBytes())
                return InspectionResult.dangerous(PacketKind.ITEM_STACK,
                    "container contents exceed the NBT limit", total, null,
                    null);
        }

        return InspectionResult.safe();
    }

    private InspectionResult checkEquipmentPacket(
        EntityEquipmentUpdateS2CPacket p) {
        int entityId = readPacketEntityId(p);
        if (isBannedEntity(entityId))
            return InspectionResult.dangerous(PacketKind.ITEM_STACK,
                "equipment for quarantined entity", -1,
                entityIdToChunk.get(entityId), entityId);

        for (ItemStack stack : findItemStacks(p)) {
            InspectionResult result = checkItemStackAgainstLimits(stack,
                PacketKind.ITEM_STACK, entityIdToChunk.get(entityId), entityId);
            if (result.dangerous()) {
                quarantineEntity(entityId);
                return result;
            }
        }

        return InspectionResult.safe();
    }

    private InspectionResult checkBundlePacket(BundleS2CPacket bundle) {
        bundlesScanned++;

        for (Packet<?> sub : bundle.getPackets()) {
            InspectionResult result = inspectPacket(sub);
            if (result.dangerous())
                return result.withKind(PacketKind.BUNDLE);
        }

        return InspectionResult.safe();
    }

    private InspectionResult checkQuarantinePacket(Packet<?> packet) {
        ChunkPos affectedChunk = getAffectedChunk(packet);
        if (affectedChunk != null && isBannedChunk(affectedChunk))
            return InspectionResult.dangerous(PacketKind.CHUNK,
                "follow-up packet for quarantined chunk", -1, affectedChunk,
                null);

        int entityId = readPacketEntityId(packet);
        if (entityId >= 0 && isBannedEntity(entityId))
            return InspectionResult.dangerous(PacketKind.ENTITY_METADATA,
                "follow-up packet for quarantined entity", -1,
                entityIdToChunk.get(entityId), entityId);

        return InspectionResult.safe();
    }

    private InspectionResult checkTrackedEntityQuarantine(Packet<?> packet) {
        int entityId = readPacketEntityId(packet);
        if (entityId >= 0 && isBannedEntity(entityId))
            return InspectionResult.dangerous(PacketKind.ENTITY_METADATA,
                "entity spawned or moved inside quarantined chunk", -1,
                entityIdToChunk.get(entityId), entityId);

        return InspectionResult.safe();
    }

    private ChunkPos getAffectedChunk(Packet<?> packet) {
        if (packet instanceof ChunkDataS2CPacket p)
            return new ChunkPos(p.getChunkX(), p.getChunkZ());

        if (packet instanceof BlockEntityUpdateS2CPacket p)
            return new ChunkPos(p.getPos());

        if (packet instanceof BlockUpdateS2CPacket p)
            return new ChunkPos(p.getPos());

        if (packet instanceof ChunkDeltaUpdateS2CPacket p)
            return getSectionUpdateChunk(p);

        if (packet instanceof LightUpdateS2CPacket p)
            return new ChunkPos(p.getChunkX(), p.getChunkZ());

        return null;
    }

    private ChunkPos getSectionUpdateChunk(ChunkDeltaUpdateS2CPacket packet) {
        Object sectionPos = invokeNoArg(packet, "getSectionPos");
        if (sectionPos == null)
            sectionPos = invokeNoArg(packet, "sectionPos");
        if (sectionPos == null)
            sectionPos = readFieldByTypeName(packet, "SectionPos");

        Object chunk = invokeNoArg(sectionPos, "chunk");
        if (chunk instanceof ChunkPos chunkPos)
            return chunkPos;

        Integer x = readIntByMethods(sectionPos, "x", "getX", "getXChunk");
        Integer z = readIntByMethods(sectionPos, "z", "getZ", "getZChunk");
        if (x != null && z != null)
            return new ChunkPos(x, z);

        return null;
    }

    private void updateTrackedEntityChunk(Packet<?> packet) {
        if (packet instanceof EntitySpawnS2CPacket add) {
            int id = add.getEntityId();
            ChunkPos chunk = chunkFromCoordinates(add.getX(), add.getZ());
            entityIdToChunk.put(id, chunk);
            if (isBannedChunk(chunk))
                quarantineEntity(id);
            return;
        }

        int entityId = readPacketEntityId(packet);
        if (entityId < 0)
            return;

        ChunkPos newChunk = null;
        if (packet instanceof EntityPositionS2CPacket tp) {
            newChunk = chunkFromVecLike(tp.change().position());
        } else if (packet instanceof EntityPositionSyncS2CPacket sync) {
            newChunk = chunkFromVecLike(sync.values().position());
        } else if (packet instanceof EntityS2CPacket) {
            newChunk = entityIdToChunk.get(entityId);
        }

        if (newChunk == null)
            return;

        entityIdToChunk.put(entityId, newChunk);
        if (isBannedChunk(newChunk))
            quarantineEntity(entityId);
    }

    private ChunkPos chunkFromVecLike(Object vec) {
        if (vec == null)
            return null;

        Double x = readDoubleByMethods(vec, "x", "getX");
        Double z = readDoubleByMethods(vec, "z", "getZ");
        if (x == null || z == null)
            return null;

        return chunkFromCoordinates(x, z);
    }

    private ChunkPos chunkFromCoordinates(double x, double z) {
        return new ChunkPos(((int) Math.floor(x)) >> 4,
            ((int) Math.floor(z)) >> 4);
    }

    private void recordBlocked(InspectionResult result) {
        switch (result.kind()) {
            case CHUNK -> chunksBlocked++;
            case BLOCK_ENTITY -> blockEntitiesBlocked++;
            case ENTITY_METADATA -> entityMetadataBlocked++;
            case ITEM_STACK -> itemStacksBlocked++;
            case BUNDLE -> {
            }
            case PACKET -> {
            }
        }

        quietBlockedPackets++;
        if (mc.player == null)
            return;

        long now = System.currentTimeMillis();
        if (now - lastStatusMessageMs < 1000L && quietBlockedPackets < 10)
            return;

        lastStatusMessageMs = now;
        quietBlockedPackets = 0;
        sendRed("Blocked " + result.kind().displayName
            + ": " + result.reason() + formatResultDetails(result));
    }

    private String formatResultDetails(InspectionResult result) {
        StringBuilder details = new StringBuilder();
        if (result.chunk() != null)
            details.append(" chunk [").append(chunkX(result.chunk()))
                .append(", ").append(chunkZ(result.chunk())).append("]");
        if (result.entityId() != null)
            details.append(" entity #").append(result.entityId());
        if (result.estimatedBytes() >= 0)
            details.append(" size ")
                .append(formatBytes(result.estimatedBytes()));
        return details.toString();
    }

    private int chunkX(ChunkPos chunk) {
        return chunk == null ? 0 : chunk.x;
    }

    private int chunkZ(ChunkPos chunk) {
        return chunk == null ? 0 : chunk.z;
    }

    private boolean isBannedChunk(ChunkPos chunk) {
        return shouldHardQuarantineChunks() && chunk != null
            && bannedChunks.contains(chunk);
    }

    private boolean isBannedEntity(int entityId) {
        return entityId >= 0 && bannedEntityIds.contains(entityId);
    }

    private void quarantineChunk(ChunkPos chunk) {
        if (!shouldHardQuarantineChunks() || chunk == null)
            return;

        if (bannedChunks.add(chunk))
            quarantinedChunks++;
    }

    private boolean shouldHardQuarantineChunks() {
        return false;
    }

    private void quarantineEntity(int entityId) {
        if (entityId < 0)
            return;

        if (bannedEntityIds.add(entityId))
            quarantinedEntities++;
    }

    private InspectionResult checkTagAgainstLimits(NbtCompound tag,
        PacketKind kind, ChunkPos chunk, Integer entityId) {
        try {
            long estimatedSize = estimateTagSize(tag);
            if (isDangerousTag(tag))
                return InspectionResult.dangerous(kind, "dangerous NBT tag",
                    estimatedSize, chunk, entityId);

        } catch (Throwable t) {
            if (failClosedOnExceptions.get())
                return InspectionResult.dangerous(kind,
                    "NBT inspection exception: " + t.getClass().getSimpleName(),
                    -1, chunk, entityId);
        }

        return InspectionResult.safe();
    }

    private InspectionResult checkItemStackAgainstLimits(ItemStack stack,
        PacketKind kind, ChunkPos chunk, Integer entityId) {
        try {
            if (isDangerousItemStack(stack))
                return InspectionResult.dangerous(kind,
                    "dangerous item stack NBT/components",
                    estimateItemStackNbtSize(stack), chunk, entityId);

        } catch (Throwable t) {
            if (failClosedOnExceptions.get())
                return InspectionResult
                    .dangerous(kind,
                        "item stack inspection exception: "
                            + t.getClass().getSimpleName(),
                        -1, chunk, entityId);
        }

        return InspectionResult.safe();
    }

    private InspectionResult checkObjectForDangerousPayload(Object value,
        PacketKind kind, ChunkPos chunk, Integer entityId) {
        if (value instanceof ItemStack stack)
            return checkItemStackAgainstLimits(stack, kind, chunk, entityId);

        if (value instanceof NbtCompound tag)
            return checkTagAgainstLimits(tag, kind, chunk, entityId);

        long estimated = estimateObjectNbtOrComponentSize(value);
        if (estimated > maxItemStackBytes()
            || estimated > maxSuspiciousPacketBytes())
            return InspectionResult.dangerous(kind,
                "large NBT/component-like metadata value", estimated, chunk,
                entityId);

        for (ItemStack stack : findItemStacks(value)) {
            InspectionResult result =
                checkItemStackAgainstLimits(stack, kind, chunk, entityId);
            if (result.dangerous())
                return result;
        }

        NbtCompound tag = findCompoundTag(value);
        if (tag != null)
            return checkTagAgainstLimits(tag, kind, chunk, entityId);

        return InspectionResult.safe();
    }

    private boolean isDangerousTag(NbtCompound tag) {
        if (tag == null)
            return false;

        long estimatedSize = estimateTagSize(tag);
        if (estimatedSize > maxBlockEntityBytes()
            || estimatedSize > maxSuspiciousPacketBytes())
            return true;

        ArrayDeque<NbtElement> queue = new ArrayDeque<>();
        queue.add(tag);
        int visited = 0;

        while (!queue.isEmpty() && visited++ < 4096) {
            NbtElement current = queue.removeFirst();
            if (current instanceof NbtCompound compound) {
                if (compound.getSizeInBytes() > maxBlockEntityBytes())
                    return true;

                for (String key : compound.getKeys()) {
                    NbtElement child = compound.get(key);
                    if (child == null)
                        continue;

                    if (isSuspiciousItemKey(key)
                        && estimateObjectNbtOrComponentSize(
                        child) > maxItemStackBytes())
                        return true;

                    queue.add(child);
                }
            } else if (current instanceof Iterable<?> iterable) {
                for (Object child : iterable)
                    if (child instanceof NbtElement childTag)
                        queue.add(childTag);
            }
        }

        return false;
    }

    private boolean isDangerousItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;

        long size = estimateItemStackNbtSize(stack);
        return size > maxItemStackBytes() || size > maxSuspiciousPacketBytes();
    }

    private long estimateItemStackNbtSize(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return 0;

        if (mc.world == null)
            return Long.MAX_VALUE;

        RegistryByteBuf buf = new RegistryByteBuf(
            Unpooled.buffer(), mc.world.getRegistryManager());
        try {
            ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, stack);
            return buf.writerIndex();

        } catch (Throwable t) {
            return Long.MAX_VALUE;

        } finally {
            buf.release();
        }
    }

    private long estimateObjectNbtOrComponentSize(Object value) {
        return estimateObjectNbtOrComponentSize(value,
            Collections.newSetFromMap(new IdentityHashMap<>()), 0);
    }

    private long estimateObjectNbtOrComponentSize(Object value,
        Set<Object> seen, int depth) {
        if (value == null)
            return 0;
        if (depth > 32 || !seen.add(value))
            return maxSuspiciousPacketBytes() + 1;
        if (value instanceof ItemStack stack)
            return estimateItemStackNbtSize(stack);
        if (value instanceof NbtCompound tag)
            return tag.getSizeInBytes();
        if (value instanceof NbtElement tag)
            return estimateTagSize(tag);
        if (value instanceof Collection<?> collection) {
            long total = 0;
            for (Object entry : collection) {
                total = saturatingAdd(total,
                    estimateObjectNbtOrComponentSize(entry, seen, depth + 1));
                if (total > maxSuspiciousPacketBytes())
                    return total;
            }
            return total;
        }
        return 0;
    }

    private long getInventoryNbtSize() {
        if (mc.player == null)
            return 0;
        var inventory = mc.player.getInventory();
        long total = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            total = saturatingAdd(total, estimateItemStackNbtSize(stack));
            if (total > inventoryNbtLimitBytes())
                return total;
        }
        return total;
    }

    private long saturatingAdd(long left, long right) {
        if (right > Long.MAX_VALUE - left)
            return Long.MAX_VALUE;
        return left + right;
    }

    private long estimateTagSize(NbtElement tag) {
        if (tag == null)
            return 0;

        // Do not call getSizeInBytes here. It recursively visits the whole
        // structure and can overflow the client stack on deliberately deep NBT.
        ArrayDeque<NbtElement> pending = new ArrayDeque<>();
        Set<NbtElement> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        pending.add(tag);
        long total = 0;
        int visited = 0;
        long limit = maxSuspiciousPacketBytes();

        while (!pending.isEmpty()) {
            NbtElement current = pending.removeFirst();
            if (!seen.add(current) || ++visited > 4096)
                return limit + 1;

            // This is intentionally conservative. It only needs to distinguish
            // safe data from data large enough to reject.
            total = saturatingAdd(total, 16);
            if (current instanceof NbtCompound compound) {
                for (String key : compound.getKeys()) {
                    total = saturatingAdd(total, key.length() * 2L + 4);
                    NbtElement child = compound.get(key);
                    if (child != null)
                        pending.addLast(child);
                }
            } else if (current instanceof Iterable<?> iterable) {
                for (Object child : iterable)
                    if (child instanceof NbtElement childTag)
                        pending.addLast(childTag);
            }

            if (total > limit)
                return total;
        }

        return total;
    }

    private boolean isSuspiciousItemKey(String key) {
        if (key == null)
            return false;

        String lower = key.toLowerCase(Locale.ROOT);
        return lower.contains("item") || lower.contains("items")
            || lower.contains("book") || lower.contains("pages")
            || lower.contains("container") || lower.contains("components")
            || lower.contains("tag") || lower.contains("blockentitytag");
    }

    private List<?> getPacketDataList(EntityTrackerUpdateS2CPacket packet) {
        Object result = invokeNoArg(packet, "packedItems");
        if (result instanceof List<?> list)
            return list;

        result = invokeNoArg(packet, "unpackedData");
        if (result instanceof List<?> list)
            return list;

        for (var method : packet.getClass().getMethods()) {
            if (method.getParameterCount() != 0
                || !List.class.isAssignableFrom(method.getReturnType()))
                continue;

            try {
                Object value = method.invoke(packet);
                if (value instanceof List<?> list)
                    return list;

            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private Object extractDataValue(Object dataValue) {
        Object value = invokeNoArg(dataValue, "value");
        return value == null ? dataValue : value;
    }

    private Collection<ItemStack> findItemStacks(Object obj) {
        Set<ItemStack> result =
            Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        collectItemStacks(obj, result, seen, 0);
        return result;
    }

    private void collectItemStacks(Object obj, Set<ItemStack> result,
        Set<Object> seen, int depth) {
        if (obj == null || depth > 5 || seen.contains(obj))
            return;

        seen.add(obj);
        if (obj instanceof ItemStack stack) {
            result.add(stack);
            return;
        }

        if (obj instanceof Iterable<?> iterable) {
            for (Object child : iterable)
                collectItemStacks(child, result, seen, depth + 1);
            return;
        }

        Object value = invokeNoArg(obj, "value");
        if (value != null && value != obj)
            collectItemStacks(value, result, seen, depth + 1);

        for (String methodName : new String[]{"getItem", "getItems", "items",
            "contents", "slots", "equipment", "getSlots", "getCarriedItem",
            "carriedItem", "cursorStack", "first", "second", "getFirst",
            "getSecond"}) {
            Object child = invokeNoArg(obj, methodName);
            if (child != null && child != obj)
                collectItemStacks(child, result, seen, depth + 1);
        }
    }

    private NbtCompound findCompoundTag(Object obj) {
        if (obj instanceof NbtCompound tag)
            return tag;

        for (String methodName : new String[]{"tag", "getTag", "getNbt", "nbt",
            "getCompoundTag"}) {
            Object value = invokeNoArg(obj, methodName);
            if (value instanceof NbtCompound tag)
                return tag;
        }

        Object fieldValue = readFieldBySimpleNameContains(obj, "CompoundTag",
            "NbtCompound");
        if (fieldValue instanceof NbtCompound tag)
            return tag;

        return null;
    }

    private int readPacketEntityId(Packet<?> packet) {
        if (packet instanceof EntityTrackerUpdateS2CPacket metadata)
            return metadata.id();
        if (packet instanceof EntitySpawnS2CPacket add)
            return add.getEntityId();
        if (packet instanceof EntityPositionS2CPacket tp)
            return tp.entityId();
        if (packet instanceof EntityPositionSyncS2CPacket sync)
            return sync.id();
        if (packet instanceof EntityVelocityUpdateS2CPacket motion)
            return motion.getEntityId();
        if (packet instanceof EntityEquipmentUpdateS2CPacket equipment)
            return equipment.getEntityId();
        if (packet instanceof EntitySetHeadYawS2CPacket rotate)
            return readPacketEntityIdReflective(rotate);
        if (packet instanceof EntityStatusS2CPacket event)
            return readPacketEntityIdReflective(event);
        if (packet instanceof EntityS2CPacket move)
            return readPacketEntityIdReflective(move);

        return readPacketEntityIdReflective(packet);
    }

    private int readPacketEntityIdReflective(Object packet) {
        Integer id =
            readIntByMethods(packet, "id", "getId", "entityId", "getEntityId");
        return id == null ? -1 : id;
    }

    private Integer readIntByMethods(Object obj, String... names) {
        for (String name : names) {
            Object value = invokeNoArg(obj, name);
            if (value instanceof Number number)
                return number.intValue();
        }
        return null;
    }

    private Double readDoubleByMethods(Object obj, String... names) {
        for (String name : names) {
            Object value = invokeNoArg(obj, name);
            if (value instanceof Number number)
                return number.doubleValue();
        }
        return null;
    }

    private Object invokeNoArg(Object obj, String methodName) {
        if (obj == null || methodName == null)
            return null;

        try {
            var method = obj.getClass().getMethod(methodName);
            return method.invoke(obj);

        } catch (Throwable ignored) {
            return null;
        }
    }

    private Object readFieldByTypeName(Object obj, String typeNamePart) {
        if (obj == null || typeNamePart == null)
            return null;

        for (var field : obj.getClass().getDeclaredFields()) {
            if (!field.getType().getSimpleName().contains(typeNamePart))
                continue;

            try {
                field.setAccessible(true);
                return field.get(obj);

            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private Object readFieldBySimpleNameContains(Object obj, String... parts) {
        if (obj == null || parts == null)
            return null;

        for (var field : obj.getClass().getDeclaredFields()) {
            String simpleName = field.getType().getSimpleName();
            boolean matches = false;
            for (String part : parts)
                if (simpleName.contains(part)) {
                    matches = true;
                    break;
                }
            if (!matches)
                continue;

            try {
                field.setAccessible(true);
                return field.get(obj);

            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private long maxBlockEntityBytes() {
        return maxBlockEntityNbtSizeKb.get() * 1024L;
    }

    private long maxItemStackBytes() {
        return maxItemStackNbtSizeKb.get() * 1024L;
    }

    private long maxSuspiciousPacketBytes() {
        return (long) (maxSuspiciousPacketSizeMb.get() * 1024D * 1024D);
    }

    private long inventoryNbtLimitBytes() {
        return (long) (inventoryNbtLimitMb.get() * 1024D * 1024D);
    }

    private String formatBytes(long bytes) {
        if (bytes < 0)
            return "unknown";
        if (bytes < 1024)
            return bytes + " B";

        double kib = bytes / 1024D;
        if (kib < 1024)
            return String.format(Locale.ROOT, "%.2f KB", kib);

        return String.format(Locale.ROOT, "%.2f MB", kib / 1024D);
    }

    private void sendRed(String message) {
    }

    public static boolean shouldDropRawClientboundPacket(
        NetworkState<?> state, ByteBuf buf) {
        NbtFilter hack = getActiveInstance();
        if (hack == null || state == null || buf == null)
            return false;

        if (state.id() != NetworkPhase.PLAY
            || state.side() != NetworkSide.CLIENTBOUND)
            return false;

        RawPacketId rawId = readRawPacketId(buf);
        if (rawId == null)
            return false;

        int payloadBytes = buf.readableBytes() - rawId.bytesRead();
        if (payloadBytes <= hack.maxSuspiciousPacketBytes())
            return false;

        Integer chunkPacketId = resolveChunkPacketId(state);
        return chunkPacketId != null && rawId.id() == chunkPacketId;
    }

    private static NbtFilter getActiveInstance() {
        try {
            NbtFilter hack = Modules.get().get(NbtFilter.class);
            return hack != null && hack.isActive() ? hack : null;

        } catch (Throwable t) {
            return null;
        }
    }

    private static RawPacketId readRawPacketId(ByteBuf buf) {
        int readerIndex = buf.readerIndex();
        int readable = buf.readableBytes();
        int value = 0;
        int bytesRead = 0;

        while (bytesRead < Math.min(5, readable)) {
            byte current = buf.getByte(readerIndex + bytesRead);
            value |= (current & 0x7F) << (bytesRead * 7);
            bytesRead++;

            if ((current & 0x80) == 0)
                return new RawPacketId(value, bytesRead);
        }

        return null;
    }

    private static Integer resolveChunkPacketId(NetworkState<?> state) {
        Object2IntMap<?> map = findObject2IntMap(state.codec(),
            Collections.newSetFromMap(new IdentityHashMap<>()), 0);
        if (map == null)
            return null;

        try {
            for (Object key : map.keySet()) {
                if (key instanceof PacketType<?> packetType) {
                    Identifier id = packetType.id();
                    if ("chunk_load".equals(id.getPath()))
                        return map.getInt(key);
                }
            }

        } catch (Throwable ignored) {
        }

        return null;
    }

    private static Object2IntMap<?> findObject2IntMap(Object obj, Set<Object> seen,
        int depth) {
        if (obj == null || depth > 6 || seen.contains(obj))
            return null;

        seen.add(obj);
        if (obj.getClass().getName().contains("Object2Int"))
            return (Object2IntMap<?>) obj;

        for (var field : obj.getClass().getDeclaredFields()) {
            Class<?> type = field.getType();
            if (type.isPrimitive() || type.isEnum()
                || type.getName().startsWith("java.lang"))
                continue;

            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value == null)
                    continue;

                if (value.getClass().getName().contains("Object2Int"))
                    return (Object2IntMap<?>) value;

                Object2IntMap<?> nested = findObject2IntMap(value, seen, depth + 1);
                if (nested != null)
                    return nested;

            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private enum PacketKind {
        CHUNK("chunk"),
        BLOCK_ENTITY("block entity"),
        ENTITY_METADATA("entity metadata"),
        ITEM_STACK("item stack carrier"),
        BUNDLE("bundle"),
        PACKET("packet");

        private final String displayName;

        PacketKind(String displayName) {
            this.displayName = displayName;
        }
    }

    private record InspectionResult(boolean dangerous, PacketKind kind,
        String reason, long estimatedBytes, ChunkPos chunk, Integer entityId) {
        private static InspectionResult safe() {
            return new InspectionResult(false, PacketKind.PACKET, "", -1, null,
                null);
        }

        private static InspectionResult dangerous(PacketKind kind,
            String reason, long estimatedBytes, ChunkPos chunk,
            Integer entityId) {
            return new InspectionResult(true, kind, reason, estimatedBytes,
                chunk, entityId);
        }

        private InspectionResult withKind(PacketKind newKind) {
            return new InspectionResult(dangerous, newKind, reason,
                estimatedBytes, chunk, entityId);
        }
    }

    private record RawPacketId(int id, int bytesRead) {
    }
}
