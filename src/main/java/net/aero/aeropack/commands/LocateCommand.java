/*
 * Adapted from Nora Tweaks (CC0-1.0, https://github.com/noramibu/Nora-Tweaks)
 * which was partially adapted from Meteor Rejects.
 */
package net.aero.aeropack.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import cubitect.Cubiomes;
import cubitect.Cubiomes.Pos;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.aero.aeropack.util.config.Seeds;
import net.aero.aeropack.util.config.Seeds.Seed;
import net.aero.aeropack.util.config.WorldGenUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Locale;

public class LocateCommand extends Command {
    private static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(o -> {
        if (o instanceof Cubiomes.StructureType type) {
            return Component.literal(String.format("%s not found.", Utils.nameToTitle(type.toString().replace('_', '-'))));
        }
        return Component.literal("Not found.");
    });
    private static final DynamicCommandExceptionType INVALID_FEATURE = new DynamicCommandExceptionType(o ->
        Component.literal(String.format("%s is not a valid feature.", o))
    );

    public LocateCommand() {
        super("seed-locate", "Locate structures using the stored seed.", "seed-loc");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(literal("feature")
            .then(argument("feature", StringArgumentType.word())
                .suggests((ctx, builder1) -> SharedSuggestionProvider.suggest(
                    Arrays.stream(Cubiomes.StructureType.values())
                        .map(type -> type.name().toLowerCase(Locale.ROOT)).toList(),
                    builder1
                ))
                .executes(ctx -> {
                    Cubiomes.StructureType feature = parseFeature(StringArgumentType.getString(ctx, "feature"));
                if (mc.player == null) return SINGLE_SUCCESS;

                BlockPos playerPos = mc.player.blockPosition();
                Seed seed = Seeds.get().getSeed();
                if (seed == null) throw NOT_FOUND.create(feature);

                Cubiomes.MCVersion cubiomesVersion = seed.version;
                Pos pos;
                if (cubiomesVersion != null) {
                    pos = Cubiomes.GetNearestStructure(feature, playerPos.getX(), playerPos.getZ(), seed.seed, cubiomesVersion);
                } else {
                    BlockPos fallback = WorldGenUtils.locateFeature(feature, playerPos);
                    if (fallback == null) throw NOT_FOUND.create(feature);
                    pos = new Pos();
                    pos.x = fallback.getX();
                    pos.z = fallback.getZ();
                }

                if (pos == null) throw NOT_FOUND.create(feature);

                int distance = (int) Math.hypot(pos.x - playerPos.getX(), pos.z - playerPos.getZ());
                MutableComponent component = Component.literal(String.format("%s located at ", Utils.nameToTitle(feature.toString().replace('_', '-'))));
                component.append(ChatUtils.formatCoords(new Vec3(pos.x, 0, pos.z)));
                component.append(".");
                if (distance > 0) {
                    component.append(String.format(" (%d blocks away)", distance));
                }
                info(component);
                return SINGLE_SUCCESS;
            })));
    }

    private static Cubiomes.StructureType parseFeature(String input) throws CommandSyntaxException {
        for (Cubiomes.StructureType type : Cubiomes.StructureType.values()) {
            if (type.name().equalsIgnoreCase(input)) return type;
        }
        throw INVALID_FEATURE.create(input);
    }
}
