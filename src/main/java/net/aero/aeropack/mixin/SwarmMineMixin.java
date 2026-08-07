package net.aero.aeropack.mixin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import meteordevelopment.meteorclient.commands.commands.SwarmCommand;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(SwarmCommand.class)
public class SwarmMineMixin {

    @Inject(method = "build", at = @At("TAIL"))
    private void aeropack$replaceMineCommand(LiteralArgumentBuilder<CommandSource> builder, CallbackInfo ci) {
        // ── swarm mine ─────────────────────────────────────────────────────
        builder.then(
            LiteralArgumentBuilder.<CommandSource>literal("mine").then(
                RequiredArgumentBuilder.<CommandSource, String>argument("blocks", StringArgumentType.greedyString())
                    .suggests((context, suggestionsBuilder) -> {
                        String input = suggestionsBuilder.getRemaining();
                        int lastSpace = input.lastIndexOf(' ');
                        String prefix = lastSpace >= 0 ? input.substring(lastSpace + 1) : input;

                        java.util.Set<String> alreadyTyped = new java.util.HashSet<>();
                        if (lastSpace >= 0) {
                            for (String word : input.substring(0, lastSpace).trim().split("\\s+")) {
                                if (!word.isBlank()) alreadyTyped.add(word.toLowerCase());
                            }
                        }

                        int wordStart = suggestionsBuilder.getStart() + (lastSpace >= 0 ? lastSpace + 1 : 0);
                        var offsetBuilder = suggestionsBuilder.createOffset(wordStart);

                        String lowerPrefix = prefix.toLowerCase();
                        for (Identifier id : Registries.BLOCK.getIds()) {
                            String full = id.toString();
                            String path = id.getPath();

                            if (alreadyTyped.contains(full.toLowerCase())) continue;

                            if (full.startsWith(lowerPrefix) || path.startsWith(lowerPrefix)) {
                                offsetBuilder.suggest(full);
                            }
                        }
                        return offsetBuilder.buildFuture();
                    })
                    .executes(context -> {
                        Swarm swarm = Modules.get().get(Swarm.class);

                        if (!swarm.isActive()) {
                            ChatUtils.error("The swarm module must be active to use this command.");
                            return 0;
                        }

                        if (swarm.isHost()) {
                            swarm.host.sendMessage(context.getInput());
                        } else if (swarm.isWorker()) {
                            String raw = StringArgumentType.getString(context, "blocks");
                            String[] parts = raw.trim().replace(",", " ").split("\\s+");

                            List<Block> blocks = new ArrayList<>();
                            for (String part : parts) {
                                Identifier id = Identifier.tryParse(part);
                                if (id == null || !Registries.BLOCK.containsId(id)) {
                                    ChatUtils.error("Could not find block: " + part);
                                    continue;
                                }
                                blocks.add(Registries.BLOCK.get(id));
                            }

                            if (blocks.isEmpty()) {
                                ChatUtils.error("No valid blocks provided.");
                                return 0;
                            }

                            net.minecraft.client.Minecraft.getInstance().execute(() -> {
                                PathManagers.get().stop();
                                PathManagers.get().mine(blocks.toArray(new Block[0]));
                            });
                        }

                        return 1;
                    })
            )
        );

        // ── swarm drop ─────────────────────────────────────────────────────
        // Default: drop junk (safe — no ores, tools, armor, weapons, food)
        builder.then(
            LiteralArgumentBuilder.<CommandSource>literal("drop")
                .executes(context -> {
                    Swarm swarm = Modules.get().get(Swarm.class);
                    if (!swarm.isActive()) { ChatUtils.error("The swarm module must be active to use this command."); return 0; }
                    if (swarm.isHost()) { swarm.host.sendMessage("swarm drop-junk"); }
                    else if (swarm.isWorker()) { aeropack$dropByType("junk"); }
                    return 1;
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("all")
                    .executes(context -> {
                        Swarm swarm = Modules.get().get(Swarm.class);
                        if (!swarm.isActive()) { ChatUtils.error("The swarm module must be active to use this command."); return 0; }
                        if (swarm.isHost()) { swarm.host.sendMessage("swarm drop-all"); }
                        else if (swarm.isWorker()) { aeropack$dropByType("all"); }
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("ores")
                    .executes(context -> {
                        Swarm swarm = Modules.get().get(Swarm.class);
                        if (!swarm.isActive()) { ChatUtils.error("The swarm module must be active to use this command."); return 0; }
                        if (swarm.isHost()) { swarm.host.sendMessage("swarm drop-ores"); }
                        else if (swarm.isWorker()) { aeropack$dropByType("ores"); }
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("hotbar")
                    .executes(context -> {
                        Swarm swarm = Modules.get().get(Swarm.class);
                        if (!swarm.isActive()) { ChatUtils.error("The swarm module must be active to use this command."); return 0; }
                        if (swarm.isHost()) { swarm.host.sendMessage("swarm drop-hotbar"); }
                        else if (swarm.isWorker()) { aeropack$dropByType("hotbar"); }
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("junk")
                    .executes(context -> {
                        Swarm swarm = Modules.get().get(Swarm.class);
                        if (!swarm.isActive()) { ChatUtils.error("The swarm module must be active to use this command."); return 0; }
                        if (swarm.isHost()) { swarm.host.sendMessage("swarm drop-junk"); }
                        else if (swarm.isWorker()) { aeropack$dropByType("junk"); }
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("valuables")
                    .executes(context -> {
                        Swarm swarm = Modules.get().get(Swarm.class);
                        if (!swarm.isActive()) { ChatUtils.error("The swarm module must be active to use this command."); return 0; }
                        if (swarm.isHost()) { swarm.host.sendMessage("swarm drop-valuables"); }
                        else if (swarm.isWorker()) { aeropack$dropByType("valuables"); }
                        return 1;
                    })
                )
        );

        // ── swarm slot ─────────────────────────────────────────────────────
        var slotArg = RequiredArgumentBuilder.<CommandSource, Integer>argument("slot", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 9))
            .executes(context -> {
                Swarm swarm = Modules.get().get(Swarm.class);
                if (!swarm.isActive()) { ChatUtils.error("The swarm module must be active to use this command."); return 0; }

                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker()) {
                    int slot = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "slot");
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.execute(() -> mc.player.getInventory().setSelectedSlot(slot - 1));
                    }
                }
                return 1;
            });
        builder.then(LiteralArgumentBuilder.<CommandSource>literal("slot").then(slotArg));

        // ── swarm guard ────────────────────────────────────────────────────
        builder.then(
            LiteralArgumentBuilder.<CommandSource>literal("guard")
                .executes(context -> {
                    Swarm swarm = Modules.get().get(Swarm.class);
                    if (!swarm.isActive()) { ChatUtils.error("The swarm module must be active to use this command."); return 0; }
                    if (swarm.isHost()) { aeropack$sendNames(swarm); ChatUtils.info("Swarm Guard activated on all workers."); }
                    else if (swarm.isWorker()) { aeropack$activateGuardOnWorker(null); }
                    return 1;
                })
        );

        // ── swarm server ───────────────────────────────────────────────────
        builder.then(
            LiteralArgumentBuilder.<CommandSource>literal("server").then(
                RequiredArgumentBuilder.<CommandSource, String>argument("address", StringArgumentType.word())
                    .executes(context -> {
                        Swarm swarm = Modules.get().get(Swarm.class);
                        String address = StringArgumentType.getString(context, "address").trim();
                        if (swarm.isActive() && swarm.isHost()) {
                            meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmConnection[] conns = swarm.host.getConnections();
                            int nextId = 1;
                            for (int i = 0; i < conns.length; i++) {
                                if (conns[i] != null) {
                                    conns[i].messageToSend = "swarm server " + address + "###ID###" + nextId;
                                    nextId++;
                                }
                            }
                            ChatUtils.info("Telling %d workers to join (highlight)%s", nextId - 1, address);
                        } else {
                            aeropack$joinMinecraftServer(address);
                        }
                        return 1;
                    })
                    .then(
                        LiteralArgumentBuilder.<CommandSource>literal("random")
                            .executes(context -> {
                                Swarm swarm = Modules.get().get(Swarm.class);
                                String address = StringArgumentType.getString(context, "address").trim();
                                if (swarm.isActive() && swarm.isHost()) {
                                    meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmConnection[] conns = swarm.host.getConnections();
                                    int nextId = 1;
                                    for (int i = 0; i < conns.length; i++) {
                                        if (conns[i] != null) {
                                            conns[i].messageToSend = "swarm server " + address + "###ID###" + nextId + "###RANDOM###";
                                            nextId++;
                                        }
                                    }
                                    ChatUtils.info("Telling %d workers to join (highlight)%s (random names)", nextId - 1, address);
                                } else {
                                    aeropack$joinMinecraftServer(address);
                                }
                                return 1;
                            })
                    )
            )
        );

        // ── swarm disconnect ───────────────────────────────────────────────
        builder.then(
            LiteralArgumentBuilder.<CommandSource>literal("disconnect")
                .executes(context -> {
                    Swarm swarm = Modules.get().get(Swarm.class);
                    if (!swarm.isActive()) { ChatUtils.error("The swarm module must be active to use this command."); return 0; }
                    if (swarm.isHost()) { swarm.host.sendMessage("swarm disconnect-all"); ChatUtils.info("All workers disconnected from server."); }
                    return 1;
                })
        );

        // ── swarm kick ─────────────────────────────────────────────────────
        builder.then(
            LiteralArgumentBuilder.<CommandSource>literal("kick").then(
                RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                    .suggests((ctx, sb) -> {
                        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                        if (mc.level != null) {
                            for (net.minecraft.entity.player.PlayerEntity p : mc.level.getPlayers()) {
                                if (p == mc.player) continue;
                                sb.suggest(p.getName().getString());
                            }
                        }
                        return sb.buildFuture();
                    })
                    .executes(context -> {
                        Swarm swarm = Modules.get().get(Swarm.class);
                        if (!swarm.isActive()) { ChatUtils.error("The swarm module must be active to use this command."); return 0; }
                        if (!swarm.isHost()) return 0;

                        String targetName = StringArgumentType.getString(context, "player").trim();
                        meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmConnection[] conns = swarm.host.getConnections();
                        for (int i = conns.length - 1; i >= 0; i--) {
                            if (conns[i] != null) {
                                conns[i].messageToSend = "swarm impersonate-kick " + targetName;
                                ChatUtils.info("Telling worker to impersonate-kick (highlight)%s", targetName);
                                return 1;
                            }
                        }
                        ChatUtils.error("No workers connected.");
                        return 0;
                    })
            )
        );
    }

    // ── Drop logic (host-side execution when on worker) ───────────────────

    private static void aeropack$dropByType(String type) {
        net.aero.aeropack.util.SwarmDropHelper.dropByType(type);
    }

    // ── Server join helper ────────────────────────────────────────────────

    private static void aeropack$joinMinecraftServer(String addressInput) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();

        String host = addressInput;
        int port = 25565;

        int colonIndex = addressInput.lastIndexOf(':');
        if (colonIndex > 0) {
            String portPart = addressInput.substring(colonIndex + 1);
            try {
                port = Integer.parseInt(portPart);
                host = addressInput.substring(0, colonIndex);
            } catch (NumberFormatException ignored) {}
        }

        final String finalHost = host;
        final int finalPort = port;

        mc.execute(() -> {
            try {
                net.minecraft.client.network.ServerAddress serverAddress =
                    new net.minecraft.client.network.ServerAddress(finalHost, finalPort);
                net.minecraft.client.network.ServerInfo serverInfo = new net.minecraft.client.network.ServerInfo(
                    finalHost, finalHost + ":" + finalPort,
                    net.minecraft.client.network.ServerInfo.ServerType.OTHER
                );
                net.minecraft.client.gui.screen.Screen returnScreen =
                    mc.currentScreen != null ? mc.currentScreen : new net.minecraft.client.gui.screen.TitleScreen();
                net.minecraft.client.gui.screen.multiplayer.ConnectScreen.connect(
                    returnScreen, mc, serverAddress, serverInfo, false,
                    new net.minecraft.client.network.CookieStorage(java.util.Map.of(), java.util.Map.of(), false)
                );
                ChatUtils.info("Joining server " + finalHost + ":" + finalPort + "...");
            } catch (Exception e) {
                ChatUtils.error("Failed to join server: " + e.getMessage());
            }
        });
    }

    // ── Guard sync ────────────────────────────────────────────────────────

    private static final java.util.Map<String, Integer> aeropack$workerIds = new java.util.HashMap<>();
    private static final java.util.Set<Integer> aeropack$usedIds = new java.util.HashSet<>();

    private static void aeropack$sendNames(Swarm swarm) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        java.util.List<String> currentPlayers = new java.util.ArrayList<>();
        for (net.minecraft.entity.player.PlayerEntity p : mc.level.getPlayers()) {
            if (p == mc.player) continue;
            currentPlayers.add(p.getName().getString());
        }

        aeropack$workerIds.entrySet().removeIf(e -> !currentPlayers.contains(e.getKey()));
        aeropack$usedIds.retainAll(aeropack$workerIds.values());

        for (String name : currentPlayers) {
            if (!aeropack$workerIds.containsKey(name)) {
                int id = 1;
                while (aeropack$usedIds.contains(id)) id++;
                aeropack$workerIds.put(name, id);
                aeropack$usedIds.add(id);
            }
        }

        StringBuilder sb = new StringBuilder("swarm aeropack-sync ");
        boolean first = true;
        for (String name : currentPlayers) {
            if (!first) sb.append(",");
            sb.append(name);
            first = false;
        }
        if (!first) {
            net.aero.aeropack.modules.combat.SwarmGuard guard = net.aero.aeropack.modules.combat.SwarmGuard.get();
            sb.append("###CFG###");
            sb.append(guard.attackMode.get().name()).append(",");
            sb.append(guard.meleeRange.get()).append(",");
            sb.append(guard.meleeVerticalRange.get()).append(",");
            sb.append(guard.rangedRange.get()).append(",");
            sb.append(guard.rangedVerticalRange.get());

            sb.append("###IDS###");
            boolean firstId = true;
            for (java.util.Map.Entry<String, Integer> entry : aeropack$workerIds.entrySet()) {
                if (!firstId) sb.append(";");
                sb.append(entry.getKey()).append(":").append(entry.getValue());
                firstId = false;
            }

            swarm.host.sendMessage(sb.toString());
        }
    }

    private static void aeropack$activateGuardOnWorker(String specifiedName) {
        net.minecraft.client.Minecraft.getInstance().execute(() -> {
            String hostName = specifiedName;
            if (hostName == null) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.player != null && mc.level != null) {
                    double closest = Double.MAX_VALUE;
                    for (net.minecraft.entity.player.PlayerEntity p : mc.level.getPlayers()) {
                        if (p == mc.player) continue;
                        double dist = mc.player.distanceTo(p);
                        if (dist < closest) {
                            closest = dist;
                            hostName = p.getName().getString();
                        }
                    }
                }
            }
            if (hostName != null) {
                net.aero.aeropack.modules.combat.SwarmGuard.get().activate(hostName);
                ChatUtils.info("Swarm Guard activated \u2014 following " + hostName);
            } else {
                ChatUtils.error("Could not find a host player to guard.");
            }
        });
    }
}
