package net.chuck.chuckpack.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.chuck.chuckpack.modules.movement.AutoFly;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class AutoFlyCommand extends Command {
    public AutoFlyCommand() {
        super("autofly", "Fly to explore for bases with stuck reroute. Usage: .autofly explore [radius] [height] | .autofly stop", "af");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(literal("explore").executes(ctx -> {
            AutoFly af = Modules.get().get(AutoFly.class);
            if (af == null) {
                error("AutoFly not found");
                return SINGLE_SUCCESS;
            }
            af.startExplore(2048, 120);
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("explore")
            .then(argument("radius", IntegerArgumentType.integer(256, 10000)).executes(ctx -> {
                int radius = IntegerArgumentType.getInteger(ctx, "radius");
                AutoFly af = Modules.get().get(AutoFly.class);
                if (af == null) {
                    error("AutoFly not found");
                    return SINGLE_SUCCESS;
                }
                af.startExplore(radius, 120);
                return SINGLE_SUCCESS;
            })
            .then(argument("height", IntegerArgumentType.integer(-64, 319)).executes(ctx -> {
                int radius = IntegerArgumentType.getInteger(ctx, "radius");
                int height = IntegerArgumentType.getInteger(ctx, "height");
                AutoFly af = Modules.get().get(AutoFly.class);
                if (af == null) {
                    error("AutoFly not found");
                    return SINGLE_SUCCESS;
                }
                af.startExplore(radius, height);
                return SINGLE_SUCCESS;
            }))));

        builder.then(literal("stop").executes(ctx -> {
            AutoFly af = Modules.get().get(AutoFly.class);
            if (af == null) {
                error("AutoFly not found");
                return SINGLE_SUCCESS;
            }
            if (af.isExploring()) af.stopExplore();
            else if (af.isActive()) af.toggle();
            else info("AutoFly not active");
            return SINGLE_SUCCESS;
        }));
    }
}
