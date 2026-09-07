package net.chuck.chuckpack.modules.misc;

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
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ChunkPos;

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
        if (!blockOverloadedContainers.get() || mc.player == null || mc.level == null)
            return;

        if (!(event.result instanceof BlockHitResult hit))
            return;

        if (!(mc.level.getBlockEntity(hit.getBlockPos()) instanceof BaseContainerBlockEntity))
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
        if (packet instanceof ServerboundContainerClickPacket
            || packet instanceof ServerboundContainerButtonClickPacket
            || packet instanceof ServerboundUseItemOnPacket)
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
        if (!(packet instanceof ClientboundContainerSetContentPacket)
            && !(packet instanceof ClientboundContainerSetSlotPacket)
            && !(packet instanceof ClientboundSetEntityDataPacket))
            return;

        InspectionResult result = checkItemStackCarrierPacket(packet);
        if (!result.dangerous())
            return;

        event.cancel();
        // The server may have opened the screen before sending its contents.
        // Remove that screen as well so the oversized container data cannot be
        // interacted with after its contents packet was rejected.
        if (mc.gui.screen() instanceof AbstractContainerScreen<?>)
            mc.setScreenAndShow(null);
        recordBlocked(result);
    }

    private InspectionResult inspectPacket(Packet<?> packet) {
        if (packet == null)
            return InspectionResult.safe();

        try {
            if (packet instanceof ClientboundRemoveEntitiesPacket remove) {
                for (int id : remove.getEntityIds()) {
                    bannedEntityIds.remove(id);
                    entityIdToChunk.remove(id);
                }
                return InspectionResult.safe();
            }

            if (packet instanceof ClientboundBundlePacket bundle)
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
        if (packet instanceof ClientboundLevelChunkWithLightPacket chunkPkt)
            return checkChunkPacket(chunkPkt);

        if (packet instanceof ClientboundBlockEntityDataPacket bePkt)
            return checkBlockEntityPacket(bePkt);

        if (packet instanceof ClientboundSetEntityDataPacket metadata)
            return checkEntityMetadataPacket(metadata);

        if (packet instanceof ClientboundContainerSetContentPacket content)
            return checkItemStackCarrierPacket(content);
        if (packet instanceof ClientboundContainerSetSlotPacket slot)
            return checkItemStackCarrierPacket(slot);
        if (packet instanceof ClientboundSetEquipmentPacket equipment)
            return checkEquipmentPacket(equipment);

        return InspectionResult.safe();
    }

    private InspectionResult checkChunkPacket(ClientboundLevelChunkWithLightPacket packet) {
        ChunkPos chunkPos = new ChunkPos(packet.getX(), packet.getZ());
        if (isBannedChunk(chunkPos))
            return InspectionResult.dangerous(PacketKind.LevelChunk,
                "quarantined LevelChunk", -1, chunkPos, null);

        try {
            int sizeBytes =
                packet.getChunkData().getReadBuffer().readableBytes();
            double sizeMB = sizeBytes / (1024.0 * 1024.0);

            if (sizeMB > maxSuspiciousPacketSizeMb.get()) {
                quarantineChunk(chunkPos);
                return InspectionResult.dangerous(PacketKind.LevelChunk,
                    "LevelChunk payload "
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
                return InspectionResult.dangerous(PacketKind.LevelChunk,
                    "LevelChunk inspection exception: "
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
            CompoundTag tag = findCompoundTag(record);
            if (tag == null)
                continue;

            InspectionResult result = checkTagAgainstLimits(tag,
                PacketKind.LevelChunk, fallbackChunk, null);
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
        Object chunkData, Object records, Object record, ChunkPos LevelChunk) {
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

        return replaceDangerousBlockEntityRecords(chunkData, records, LevelChunk);
    }

    private int replaceDangerousBlockEntityRecords(Object chunkData,
        Object records, ChunkPos LevelChunk) {
        if (!(records instanceof Iterable<?> iterable))
            return 0;

        java.util.ArrayList<Object> safeRecords = new java.util.ArrayList<>();
        int removed = 0;
        for (Object candidate : iterable) {
            CompoundTag tag = findCompoundTag(candidate);
            InspectionResult result = tag == null ? InspectionResult.safe()
                : checkTagAgainstLimits(tag, PacketKind.LevelChunk, LevelChunk, null);
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

    private void debugSanitizedChunk(ChunkPos LevelChunk, int strippedRecords) {
        if (mc.player == null)
            return;

        sendRed("Sanitized LevelChunk [" + chunkX(LevelChunk)
            + ", " + chunkZ(LevelChunk) + "] - stripped " + strippedRecords
            + " dangerous block entity record(s).");
    }

    private InspectionResult checkBlockEntityPacket(
        ClientboundBlockEntityDataPacket packet) {
        ChunkPos chunkPos = ChunkPos.containing(packet.getPos());
        if (isBannedChunk(chunkPos))
            return InspectionResult.dangerous(PacketKind.BLOCK_ENTITY,
                "block entity in quarantined LevelChunk", -1, chunkPos, null);

        CompoundTag tag = packet.getTag();
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
        ClientboundSetEntityDataPacket packet) {
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
            ChunkPos LevelChunk = entityIdToChunk.get(entityId);
            if (LevelChunk != null)
                quarantineChunk(LevelChunk);
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
        ClientboundSetEquipmentPacket p) {
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

    private InspectionResult checkBundlePacket(ClientboundBundlePacket bundle) {
        bundlesScanned++;

        for (Packet<?> sub : bundle.subPackets()) {
            InspectionResult result = inspectPacket(sub);
            if (result.dangerous())
                return result.withKind(PacketKind.BUNDLE);
        }

        return InspectionResult.safe();
    }

    private InspectionResult checkQuarantinePacket(Packet<?> packet) {
        ChunkPos affectedChunk = getAffectedChunk(packet);
        if (affectedChunk != null && isBannedChunk(affectedChunk))
            return InspectionResult.dangerous(PacketKind.LevelChunk,
                "follow-up packet for quarantined LevelChunk", -1, affectedChunk,
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
                "entity spawned or moved inside quarantined LevelChunk", -1,
                entityIdToChunk.get(entityId), entityId);

        return InspectionResult.safe();
    }

    private ChunkPos getAffectedChunk(Packet<?> packet) {
        if (packet instanceof ClientboundLevelChunkWithLightPacket p)
            return new ChunkPos(p.getX(), p.getZ());

        if (packet instanceof ClientboundBlockEntityDataPacket p)
            return ChunkPos.containing(p.getPos());

        if (packet instanceof ClientboundBlockUpdatePacket p)
            return ChunkPos.containing(p.getPos());

        if (packet instanceof ClientboundSectionBlocksUpdatePacket p)
            return getSectionUpdateChunk(p);

        if (packet instanceof ClientboundLightUpdatePacket p)
            return new ChunkPos(p.getX(), p.getZ());

        return null;
    }

    private ChunkPos getSectionUpdateChunk(ClientboundSectionBlocksUpdatePacket packet) {
        Object sectionPos = invokeNoArg(packet, "getSectionPos");
        if (sectionPos == null)
            sectionPos = invokeNoArg(packet, "sectionPos");
        if (sectionPos == null)
            sectionPos = readFieldByTypeName(packet, "SectionPos");

        Object LevelChunk = invokeNoArg(sectionPos, "LevelChunk");
        if (LevelChunk instanceof ChunkPos chunkPos)
            return chunkPos;

        Integer x = readIntByMethods(sectionPos, "x", "getX", "getXChunk");
        Integer z = readIntByMethods(sectionPos, "z", "getZ", "getZChunk");
        if (x != null && z != null)
            return new ChunkPos(x, z);

        return null;
    }

    private void updateTrackedEntityChunk(Packet<?> packet) {
        if (packet instanceof ClientboundAddEntityPacket add) {
            int id = add.getId();
            ChunkPos LevelChunk = chunkFromCoordinates(add.getX(), add.getZ());
            entityIdToChunk.put(id, LevelChunk);
            if (isBannedChunk(LevelChunk))
                quarantineEntity(id);
            return;
        }

        int entityId = readPacketEntityId(packet);
        if (entityId < 0)
            return;

        ChunkPos newChunk = null;
        if (packet instanceof ClientboundMoveEntityPacket tp) {
            if (mc.level != null) {
                Entity movedEntity = mc.level.getEntity(entityId);
                if (movedEntity != null)
                    newChunk = chunkFromCoordinates(movedEntity.getX(), movedEntity.getZ());
            }
        } else if (packet instanceof ClientboundSetEntityLinkPacket sync) {
            if (mc.level != null) {
                Entity linkedEntity = mc.level.getEntity(sync.getDestId());
                if (linkedEntity != null)
                    newChunk = chunkFromCoordinates(linkedEntity.getX(), linkedEntity.getZ());
            }
        } else if (packet instanceof ClientboundMoveEntityPacket) {
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
            case LevelChunk -> chunksBlocked++;
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
        if (result.LevelChunk() != null)
            details.append(" LevelChunk [").append(chunkX(result.LevelChunk()))
                .append(", ").append(chunkZ(result.LevelChunk())).append("]");
        if (result.entityId() != null)
            details.append(" entity #").append(result.entityId());
        if (result.estimatedBytes() >= 0)
            details.append(" size ")
                .append(formatBytes(result.estimatedBytes()));
        return details.toString();
    }

    private int chunkX(ChunkPos LevelChunk) {
        return LevelChunk == null ? 0 : LevelChunk.x();
    }

    private int chunkZ(ChunkPos LevelChunk) {
        return LevelChunk == null ? 0 : LevelChunk.z();
    }

    private boolean isBannedChunk(ChunkPos LevelChunk) {
        return shouldHardQuarantineChunks() && LevelChunk != null
            && bannedChunks.contains(LevelChunk);
    }

    private boolean isBannedEntity(int entityId) {
        return entityId >= 0 && bannedEntityIds.contains(entityId);
    }

    private void quarantineChunk(ChunkPos LevelChunk) {
        if (!shouldHardQuarantineChunks() || LevelChunk == null)
            return;

        if (bannedChunks.add(LevelChunk))
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

    private InspectionResult checkTagAgainstLimits(CompoundTag tag,
        PacketKind kind, ChunkPos LevelChunk, Integer entityId) {
        try {
            long estimatedSize = estimateTagSize(tag);
            if (isDangerousTag(tag))
                return InspectionResult.dangerous(kind, "dangerous NBT tag",
                    estimatedSize, LevelChunk, entityId);

        } catch (Throwable t) {
            if (failClosedOnExceptions.get())
                return InspectionResult.dangerous(kind,
                    "NBT inspection exception: " + t.getClass().getSimpleName(),
                    -1, LevelChunk, entityId);
        }

        return InspectionResult.safe();
    }

    private InspectionResult checkItemStackAgainstLimits(ItemStack stack,
        PacketKind kind, ChunkPos LevelChunk, Integer entityId) {
        try {
            if (isDangerousItemStack(stack))
                return InspectionResult.dangerous(kind,
                    "dangerous item stack NBT/components",
                    estimateItemStackNbtSize(stack), LevelChunk, entityId);

        } catch (Throwable t) {
            if (failClosedOnExceptions.get())
                return InspectionResult
                    .dangerous(kind,
                        "item stack inspection exception: "
                            + t.getClass().getSimpleName(),
                        -1, LevelChunk, entityId);
        }

        return InspectionResult.safe();
    }

    private InspectionResult checkObjectForDangerousPayload(Object value,
        PacketKind kind, ChunkPos LevelChunk, Integer entityId) {
        if (value instanceof ItemStack stack)
            return checkItemStackAgainstLimits(stack, kind, LevelChunk, entityId);

        if (value instanceof CompoundTag tag)
            return checkTagAgainstLimits(tag, kind, LevelChunk, entityId);

        long estimated = estimateObjectNbtOrComponentSize(value);
        if (estimated > maxItemStackBytes()
            || estimated > maxSuspiciousPacketBytes())
            return InspectionResult.dangerous(kind,
                "large NBT/component-like metadata value", estimated, LevelChunk,
                entityId);

        for (ItemStack stack : findItemStacks(value)) {
            InspectionResult result =
                checkItemStackAgainstLimits(stack, kind, LevelChunk, entityId);
            if (result.dangerous())
                return result;
        }

        CompoundTag tag = findCompoundTag(value);
        if (tag != null)
            return checkTagAgainstLimits(tag, kind, LevelChunk, entityId);

        return InspectionResult.safe();
    }

    private boolean isDangerousTag(CompoundTag tag) {
        if (tag == null)
            return false;

        long estimatedSize = estimateTagSize(tag);
        if (estimatedSize > maxBlockEntityBytes()
            || estimatedSize > maxSuspiciousPacketBytes())
            return true;

        ArrayDeque<Tag> queue = new ArrayDeque<>();
        queue.add(tag);
        int visited = 0;

        while (!queue.isEmpty() && visited++ < 4096) {
            Tag current = queue.removeFirst();
            if (current instanceof CompoundTag compound) {
                if (compound.sizeInBytes() > maxBlockEntityBytes())
                    return true;

                for (String key : compound.keySet()) {
                    Tag child = compound.get(key);
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
                    if (child instanceof Tag childTag)
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

        if (mc.level == null)
            return Long.MAX_VALUE;

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(
            Unpooled.buffer(), mc.level.registryAccess());
        try {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
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
        if (value instanceof CompoundTag tag)
            return tag.sizeInBytes();
        if (value instanceof Tag tag)
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
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
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

    private long estimateTagSize(Tag tag) {
        if (tag == null)
            return 0;

        // Do not call getSizeInBytes here. It recursively visits the whole
        // structure and can overflow the client stack on deliberately deep NBT.
        ArrayDeque<Tag> pending = new ArrayDeque<>();
        Set<Tag> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        pending.add(tag);
        long total = 0;
        int visited = 0;
        long limit = maxSuspiciousPacketBytes();

        while (!pending.isEmpty()) {
            Tag current = pending.removeFirst();
            if (!seen.add(current) || ++visited > 4096)
                return limit + 1;

            // This is intentionally conservative. It only needs to distinguish
            // safe data from data large enough to reject.
            total = saturatingAdd(total, 16);
            if (current instanceof CompoundTag compound) {
                for (String key : compound.keySet()) {
                    total = saturatingAdd(total, key.length() * 2L + 4);
                    Tag child = compound.get(key);
                    if (child != null)
                        pending.addLast(child);
                }
            } else if (current instanceof Iterable<?> iterable) {
                for (Object child : iterable)
                    if (child instanceof Tag childTag)
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

    private List<?> getPacketDataList(ClientboundSetEntityDataPacket packet) {
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

    private CompoundTag findCompoundTag(Object obj) {
        if (obj instanceof CompoundTag tag)
            return tag;

        for (String methodName : new String[]{"tag", "getTag", "getNbt", "nbt",
            "getCompoundTag"}) {
            Object value = invokeNoArg(obj, methodName);
            if (value instanceof CompoundTag tag)
                return tag;
        }

        Object fieldValue = readFieldBySimpleNameContains(obj, "CompoundTag",
            "CompoundTag");
        if (fieldValue instanceof CompoundTag tag)
            return tag;

        return null;
    }

    private int readPacketEntityId(Packet<?> packet) {
        if (packet instanceof ClientboundSetEntityDataPacket metadata)
            return metadata.id();
        if (packet instanceof ClientboundAddEntityPacket add)
            return add.getId();
        if (packet instanceof ClientboundMoveEntityPacket tp)
            return readPacketEntityIdReflective(tp);
        if (packet instanceof ClientboundSetEntityLinkPacket sync)
            return sync.getSourceId();
        if (packet instanceof ClientboundSetEntityMotionPacket motion)
            return motion.id();
        if (packet instanceof ClientboundSetEquipmentPacket equipment)
            return equipment.getEntity();
        if (packet instanceof ClientboundRotateHeadPacket rotate)
            return readPacketEntityIdReflective(rotate);
        if (packet instanceof ClientboundEntityEventPacket event)
            return readPacketEntityIdReflective(event);
        if (packet instanceof ClientboundMoveEntityPacket move)
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
        ConnectionProtocol state, ByteBuf buf) {
        NbtFilter hack = getActiveInstance();
        if (hack == null || state == null || buf == null)
            return false;

        if (state != ConnectionProtocol.PLAY)
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

    private static Integer resolveChunkPacketId(ConnectionProtocol state) {
        Object2IntMap<?> map = findObject2IntMap(state,
            Collections.newSetFromMap(new IdentityHashMap<>()), 0);
        if (map == null)
            return null;

        try {
            for (Object key : map.keySet()) {
                if (key instanceof PacketType<?> packetType) {
                    Identifier id = packetType.id();
                    if ("LevelChunk_load".equals(id.getPath()))
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
        LevelChunk("LevelChunk"),
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
        String reason, long estimatedBytes, ChunkPos LevelChunk, Integer entityId) {
        private static InspectionResult safe() {
            return new InspectionResult(false, PacketKind.PACKET, "", -1, null,
                null);
        }

        private static InspectionResult dangerous(PacketKind kind,
            String reason, long estimatedBytes, ChunkPos LevelChunk,
            Integer entityId) {
            return new InspectionResult(true, kind, reason, estimatedBytes,
                LevelChunk, entityId);
        }

        private InspectionResult withKind(PacketKind newKind) {
            return new InspectionResult(dangerous, newKind, reason,
                estimatedBytes, LevelChunk, entityId);
        }
    }

    private record RawPacketId(int id, int bytesRead) {
    }
}
