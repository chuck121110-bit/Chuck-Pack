package net.chuck.chuckpack.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.chuck.chuckpack.util.XaeroWaypointHelper;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ChatWaypointCommand extends Command {
    public ChatWaypointCommand() {
        super("chatwaypoint", "Create a 'chat waypoint' (random color) at the given coordinates.", "cwp");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(argument("x", IntegerArgumentType.integer()).then(argument("y",
            IntegerArgumentType.integer(-64, 320)).then(argument("z", IntegerArgumentType.integer()).executes(ctx -> {
                int x = IntegerArgumentType.getInteger(ctx, "x");
                int y = IntegerArgumentType.getInteger(ctx, "y");
                int z = IntegerArgumentType.getInteger(ctx, "z");
                if (XaeroWaypointHelper.addWaypoint("chat waypoint", x, y, z, true)) {
                    info("Created (highlight)chat waypoint (default)at (highlight)%d %d %d", x, y, z);
                } else {
                    error("Could not create waypoint (Xaero minimap not available).");
                }
                return SINGLE_SUCCESS;
            }))));
    }
}
