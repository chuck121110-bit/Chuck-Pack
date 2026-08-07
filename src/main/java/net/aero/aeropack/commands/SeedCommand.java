/*
 * Adapted from Nora Tweaks (CC0-1.0, https://github.com/noramibu/Nora-Tweaks)
 * which was partially adapted from Meteor Rejects.
 */
package net.aero.aeropack.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import cubitect.Cubiomes;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.Utils;
import net.aero.aeropack.util.config.Seeds;
import net.aero.aeropack.util.config.Seeds.Seed;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Locale;

public class SeedCommand extends Command {
    private static final SimpleCommandExceptionType NO_SEED = new SimpleCommandExceptionType(Component.literal("No seed for current Level saved."));
    private static final SimpleCommandExceptionType INVALID_VERSION = new SimpleCommandExceptionType(Component.literal("Unknown Minecraft version."));

    public SeedCommand() {
        super("seed", "Get or set the seed for the current world.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(ctx -> {
            Seed seed = Seeds.get().getSeed();
            if (seed == null) throw NO_SEED.create();
            info(seed.toText());
            return SINGLE_SUCCESS;
        });

        builder.then(literal("list").executes(ctx -> {
            Seeds.get().seeds.forEach((name, storedSeed) -> {
                if (storedSeed == null) return;
                MutableComponent component = Component.literal(name + " ");
                component.append(storedSeed.toText());
                info(component);
            });
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("delete").executes(ctx -> {
            Seed seed = Seeds.get().getSeed();
            if (seed != null) {
                MutableComponent component = Component.literal("Deleted ");
                component.append(seed.toText());
                info(component);
            }
            Seeds.get().removeSeed(Utils.getWorldName());
            return SINGLE_SUCCESS;
        }));

        builder.then(argument("seed", StringArgumentType.string()).executes(ctx -> {
            Seeds.get().setSeed(StringArgumentType.getString(ctx, "seed"));
            return SINGLE_SUCCESS;
        }));

        builder.then(
            argument("seed", StringArgumentType.string())
                .then(argument("version", StringArgumentType.word())
                    .suggests((ctx, builder1) -> SharedSuggestionProvider.suggest(
                        Arrays.asList("1.21.10", "1.21.11", Cubiomes.MCVersion.MC_1_21_WD.name().toLowerCase(Locale.ROOT)),
                        builder1
                    ))
                    .executes(ctx -> {
                        Seeds.get().setSeed(
                            StringArgumentType.getString(ctx, "seed"),
                            parseVersion(StringArgumentType.getString(ctx, "version"))
                        );
                        return SINGLE_SUCCESS;
                    }))
        );
    }

    private static Cubiomes.MCVersion parseVersion(String input) throws CommandSyntaxException {
        Cubiomes.MCVersion version = null;
        try {
            version = Cubiomes.MCVersion.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            version = Seeds.resolveForPublic(input);
        }
        if (version == null) throw INVALID_VERSION.create();
        return version;
    }
}
