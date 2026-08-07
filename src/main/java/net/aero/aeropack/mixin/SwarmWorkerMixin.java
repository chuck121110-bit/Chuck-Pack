package net.aero.aeropack.mixin;

import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.AccountType;
import meteordevelopment.meteorclient.systems.accounts.Accounts;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmWorker;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.aero.aeropack.modules.combat.SwarmGuard;
import net.aero.aeropack.swarm.IAutoSwarmConnect;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(SwarmWorker.class)
public class SwarmWorkerMixin {

    private static final String SYNC_PREFIX = "swarm aeropack-sync ";
    private static final String RETALIATE_PREFIX = "swarm aeropack-retaliate ";
    private static final String SERVER_PREFIX = "swarm server ";
    private static final String DISCONNECT_ALL_CMD = "swarm disconnect-all";
    private static final String IMPERSONATE_KICK_PREFIX = "swarm impersonate-kick ";
    private static final String DROP_SLOT_CMD = "swarm drop-slot ";

    @Unique
    private static final AtomicInteger aeropack$accountCounter = new AtomicInteger(0);

    @Inject(method = "tick", at = @At("HEAD"))
    private void aeropack$onTick(CallbackInfo ci) {
        SwarmGuard.get().tick(Minecraft.getInstance());
    }

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/commands/Commands;dispatch(Ljava/lang/String;)V"))
    private void aeropack$onDispatch(String command) {
        Minecraft mc = Minecraft.getInstance();
        String lower = command.toLowerCase().trim();

        // ── Aeropack-specific commands (handled here, never forwarded to Meteor) ──

        if (command.startsWith(RETALIATE_PREFIX)) {
            String attackerName = command.substring(RETALIATE_PREFIX.length()).trim();
            if (!attackerName.isEmpty()) {
                mc.execute(() -> SwarmGuard.get().applyRetaliation(attackerName));
            }
            return;
        }

        if (command.startsWith(SYNC_PREFIX)) {
            aeropack$handleSync(mc, command.substring(SYNC_PREFIX.length()));
            return;
        }

        if (lower.equals(DISCONNECT_ALL_CMD)) {
            mc.execute(() -> {
                SwarmGuard.get().deactivate();
                mc.disconnectFromWorld(net.minecraft.network.chat.Component.literal("Disconnected by host"));
                ChatUtils.infoPrefix("Swarm", "Disconnected from server by host.");
            });
            return;
        }

        // ── Drop commands ──────────────────────────────────────────────────

        if (lower.equals("swarm drop-all")) {
            mc.execute(() -> aeropack$dropByType("all"));
            return;
        }
        if (lower.equals("swarm drop-junk")) {
            mc.execute(() -> aeropack$dropByType("junk"));
            return;
        }
        if (lower.equals("swarm drop-ores")) {
            mc.execute(() -> aeropack$dropByType("ores"));
            return;
        }
        if (lower.equals("swarm drop-hotbar")) {
            mc.execute(() -> aeropack$dropByType("hotbar"));
            return;
        }
        if (lower.equals("swarm drop-valuables")) {
            mc.execute(() -> aeropack$dropByType("valuables"));
            return;
        }

        // ── Inventory check (host polls workers) ───────────────────────────

        if (lower.equals("swarm inventory-check")) {
            mc.execute(() -> {
                if (mc.player == null) return;
                aeropack$writeInventoryToFile(mc);
            });
            return;
        }

        // ── Drop slot ──────────────────────────────────────────────────────

        if (lower.startsWith(DROP_SLOT_CMD)) {
            String args = command.substring(DROP_SLOT_CMD.length()).trim();
            String[] parts = args.split("\\s+");
            if (parts.length >= 3) {
                String workerName = parts[0];
                int slotIndex;
                boolean dropAll;
                try {
                    slotIndex = Integer.parseInt(parts[1]);
                    dropAll = Boolean.parseBoolean(parts[2]);
                } catch (Exception e) { return; }

                final int fSlot = slotIndex;
                final boolean fDropAll = dropAll;
                mc.execute(() -> {
                    if (mc.player == null || mc.gameMode == null) return;
                    if (!workerName.equalsIgnoreCase(mc.player.getName().getString())) return;
                    if (fSlot < 0 || fSlot >= 41) return;
                    if (mc.player.getInventory().getItem(fSlot).isEmpty()) return;

                    int screenSlot;
                    if (fSlot < 9) screenSlot = fSlot + 36;
                    else if (fSlot < 36) screenSlot = fSlot;
                    else if (fSlot < 40) screenSlot = 44 - fSlot;
                    else if (fSlot == 40) screenSlot = 45;
                    else return;

                    mc.gameMode.handleContainerInput(
                        mc.player.containerMenu.containerId,
                        fDropAll ? 0 : 1,
                        screenSlot,
                        net.minecraft.world.inventory.ContainerInput.THROW,
                        mc.player
                    );
                });
            }
            return;
        }

        // ── Impersonate kick ───────────────────────────────────────────────

        if (lower.startsWith(IMPERSONATE_KICK_PREFIX)) {
            String targetName = command.substring(IMPERSONATE_KICK_PREFIX.length()).trim();
            if (!targetName.isEmpty()) {
                mc.execute(() -> aeropack$impersonateKick(mc, targetName));
            }
            return;
        }

        // ── Server join ────────────────────────────────────────────────────

        if (lower.startsWith(SERVER_PREFIX)) {
            aeropack$handleServerJoin(mc, command.substring(SERVER_PREFIX.length()).trim());
            return;
        }

        // ── Everything else goes to Meteor (guard, mine, execute, etc.) ────

        try {
            Commands.dispatch(command);
        } catch (Exception ignored) {
        }
    }

    // ── Sync handler ──────────────────────────────────────────────────────

    @Unique
    private static void aeropack$handleSync(Minecraft mc, String payload) {
        String nameList = payload;
        String configPart = null;
        String idsPart = null;
        int cfgIndex = payload.indexOf("###CFG###");
        if (cfgIndex >= 0) {
            nameList = payload.substring(0, cfgIndex);
            String rest = payload.substring(cfgIndex + "###CFG###".length());
            int idsIndex = rest.indexOf("###IDS###");
            if (idsIndex >= 0) {
                configPart = rest.substring(0, idsIndex);
                idsPart = rest.substring(idsIndex + "###IDS###".length());
            } else {
                configPart = rest;
            }
        }

        final String finalConfigPart = configPart;
        final String finalIdsPart = idsPart;

        mc.execute(() -> {
            SwarmGuard guard = SwarmGuard.get();

            if (finalConfigPart != null) {
                try {
                    String[] parts = finalConfigPart.split(",");
                    if (parts.length == 5) {
                        guard.attackMode.set(SwarmGuard.AttackMode.valueOf(parts[0]));
                        guard.meleeRange.set(Double.parseDouble(parts[1]));
                        guard.meleeVerticalRange.set(Double.parseDouble(parts[2]));
                        guard.rangedRange.set(Double.parseDouble(parts[3]));
                        guard.rangedVerticalRange.set(Double.parseDouble(parts[4]));
                    }
                } catch (Exception e) {
                    ChatUtils.error("Swarm Guard: failed to apply host config \u2014 " + e.getMessage());
                }
            }

            if (finalIdsPart != null && mc.player != null) {
                String myName = mc.player.getName().getString();
                String[] pairs = finalIdsPart.split(";");
                for (String pair : pairs) {
                    String[] kv = pair.split(":");
                    if (kv.length == 2 && kv[0].trim().equalsIgnoreCase(myName)) {
                        try {
                            int id = Integer.parseInt(kv[1].trim());
                            aeropack$saveWorkerId(id);
                            guard.workerId = id;
                        } catch (NumberFormatException ignored) {}
                        break;
                    }
                }
            }

            String hostName = null;
            if (mc.player != null && mc.level != null) {
                double closest = Double.MAX_VALUE;
                for (net.minecraft.world.entity.player.Player p : mc.level.players()) {
                    if (p == mc.player) continue;
                    double dist = mc.player.distanceTo(p);
                    if (dist < closest) {
                        closest = dist;
                        hostName = p.getName().getString();
                    }
                }
            }
            if (hostName != null) {
                guard.activate(hostName);
                ChatUtils.infoPrefix("Swarm Guard", "Following (highlight)%s", hostName);
            }
        });
    }

    // ── Server join handler ───────────────────────────────────────────────

    @Unique
    private static void aeropack$handleServerJoin(Minecraft mc, String raw) {
        if (raw.isEmpty()) return;

        String address = raw;
        int embeddedId = 0;
        boolean useRandom = false;

        int randomMarker = raw.indexOf("###RANDOM###");
        if (randomMarker >= 0) {
            useRandom = true;
            raw = raw.substring(0, randomMarker);
        }

        int idMarker = raw.indexOf("###ID###");
        if (idMarker >= 0) {
            address = raw.substring(0, idMarker);
            try { embeddedId = Integer.parseInt(raw.substring(idMarker + 8)); } catch (NumberFormatException ignored) {}
        }

        final String fAddress = address;
        final int fEmbeddedId = embeddedId;
        final boolean fUseRandom = useRandom;

        mc.execute(() -> {
            try {
                if (fUseRandom) {
                    String randomName = net.aero.aeropack.util.SwarmUsernameManager.getOrCreateUsername(fEmbeddedId, fAddress);
                    meteordevelopment.meteorclient.systems.accounts.Account.setSession(
                        new net.minecraft.client.User(
                            randomName,
                            net.minecraft.core.UUIDUtil.createOfflinePlayerUUID(randomName),
                            "",
                            java.util.Optional.empty(),
                            java.util.Optional.empty()
                        )
                    );
                    ChatUtils.infoPrefix("Swarm", "Set random username: (highlight)%s", randomName);
                } else {
                    meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm swarm = Modules.get().get(meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm.class);
                    if (swarm != null) {
                        IAutoSwarmConnect settings = (IAutoSwarmConnect) swarm;
                        if (settings.aeropack$autoAccountSetting().get()) {
                            int savedId = aeropack$loadWorkerId();
                            int useId = savedId > 0 ? savedId : fEmbeddedId;
                            if (useId > 0) {
                                aeropack$saveWorkerId(useId);
                                aeropack$switchToAccount(useId);
                            } else {
                                aeropack$switchToNextCrackedAccount();
                            }
                        }
                    }
                }

                String host = fAddress;
                int port = 25565;
                int colonIndex = fAddress.lastIndexOf(':');
                if (colonIndex > 0) {
                    try { port = Integer.parseInt(fAddress.substring(colonIndex + 1)); host = fAddress.substring(0, colonIndex); } catch (NumberFormatException ignored) {}
                }
                final String fHost = host;
                final int fPort = port;
                net.minecraft.client.multiplayer.resolver.ServerAddress sa = new net.minecraft.client.multiplayer.resolver.ServerAddress(fHost, fPort);
                net.minecraft.client.multiplayer.ServerData si = new net.minecraft.client.multiplayer.ServerData(fHost, fHost + ":" + fPort, net.minecraft.client.multiplayer.ServerData.Type.OTHER);
                net.minecraft.client.gui.screens.Screen rs = mc.screen != null ? mc.screen : new net.minecraft.client.gui.screens.TitleScreen();
                net.minecraft.client.gui.screens.ConnectScreen.startConnecting(rs, mc, sa, si, false, new net.minecraft.client.multiplayer.TransferState(java.util.Map.of(), java.util.Map.of(), false));
                ChatUtils.infoPrefix("Swarm", "Joining (highlight)%s:%d", fHost, fPort);
            } catch (Exception e) {
                ChatUtils.error("Failed to join server: " + e.getMessage());
            }
        });
    }

    // ── Drop logic ────────────────────────────────────────────────────────

    @Unique
    private static void aeropack$dropByType(String type) {
        net.aero.aeropack.util.SwarmDropHelper.dropByType(type);
    }

    // ── Impersonate kick ──────────────────────────────────────────────────

    @Unique
    private static void aeropack$impersonateKick(Minecraft mc, String targetName) {
        if (mc.player == null || mc.level == null || mc.getCurrentServer() == null) {
            ChatUtils.error("Must be on a server to impersonate-kick.");
            return;
        }

        net.minecraft.client.multiplayer.ServerData serverInfo = mc.getCurrentServer();
        String serverAddress = serverInfo.ip;

        ChatUtils.infoPrefix("Swarm", "Impersonating (highlight)%s to get them kicked...", targetName);

        mc.disconnectFromWorld(net.minecraft.network.chat.Component.literal("Impersonating target"));

        java.util.concurrent.ScheduledExecutorService executor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            mc.execute(() -> {
                try {
                    meteordevelopment.meteorclient.systems.accounts.Account.setSession(
                        new net.minecraft.client.User(
                            targetName,
                            net.minecraft.core.UUIDUtil.createOfflinePlayerUUID(targetName),
                            "",
                            java.util.Optional.empty(),
                            java.util.Optional.empty()
                        )
                    );

                    ChatUtils.infoPrefix("Swarm", "Joined as (highlight)%s \u2014 waiting for original to get kicked...", targetName);

                    net.minecraft.client.multiplayer.resolver.ServerAddress sa = net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(serverAddress);
                    net.minecraft.client.gui.screens.Screen rs = new net.minecraft.client.gui.screens.TitleScreen();
                    net.minecraft.client.gui.screens.ConnectScreen.startConnecting(
                        rs, mc, sa, serverInfo, false,
                        new net.minecraft.client.multiplayer.TransferState(java.util.Map.of(), java.util.Map.of(), false)
                    );

                    executor.schedule(() -> {
                        mc.execute(() -> {
                            ChatUtils.infoPrefix("Swarm", "Disconnecting impersonation session...");
                            mc.disconnectFromWorld(net.minecraft.network.chat.Component.literal("Restoring session"));

                            executor.schedule(() -> {
                                mc.execute(() -> {
                                    try {
                                        int savedId = aeropack$loadWorkerId();
                                        if (savedId > 0) {
                                            aeropack$switchToAccount(savedId);
                                        } else {
                                            aeropack$switchToNextCrackedAccount();
                                        }

                                        net.minecraft.client.multiplayer.resolver.ServerAddress sa2 = net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(serverAddress);
                                        net.minecraft.client.gui.screens.Screen rs2 = new net.minecraft.client.gui.screens.TitleScreen();
                                        net.minecraft.client.gui.screens.ConnectScreen.startConnecting(
                                            rs2, mc, sa2, serverInfo, false,
                                            new net.minecraft.client.multiplayer.TransferState(java.util.Map.of(), java.util.Map.of(), false)
                                        );
                                        ChatUtils.infoPrefix("Swarm", "Restored original account and reconnected.");
                                    } catch (Exception e) {
                                        ChatUtils.error("Failed to restore session: " + e.getMessage());
                                    }
                                });
                            }, 2, java.util.concurrent.TimeUnit.SECONDS);
                        });
                    }, 5, java.util.concurrent.TimeUnit.SECONDS);

                } catch (Exception e) {
                    ChatUtils.error("Failed to impersonate: " + e.getMessage());
                }
            });
        }, 1, java.util.concurrent.TimeUnit.SECONDS);
    }

    // ── Account management ────────────────────────────────────────────────

    @Unique
    private static void aeropack$switchToAccount(int workerId) {
        List<Account<?>> crackedAccounts = new ArrayList<>();
        for (Account<?> account : Accounts.get()) {
            if (account.getType() == AccountType.Cracked) crackedAccounts.add(account);
        }

        if (crackedAccounts.isEmpty()) { ChatUtils.warning("No cracked accounts found."); return; }

        if (workerId < 1 || workerId > crackedAccounts.size()) {
            ChatUtils.warning("Worker ID %d exceeds account count (%d). Using fallback.", workerId, crackedAccounts.size());
            crackedAccounts.get(Math.abs(workerId % crackedAccounts.size())).login();
            return;
        }

        Account<?> account = crackedAccounts.get(workerId - 1);
        if (account.login()) {
            ChatUtils.infoPrefix("Swarm", "Switched to account: (highlight)%s (worker ID: %d)", account.getUsername(), workerId);
        } else {
            ChatUtils.error("Failed to login with account: " + account.getUsername());
        }
    }

    @Unique
    private static void aeropack$switchToNextCrackedAccount() {
        List<Account<?>> crackedAccounts = new ArrayList<>();
        for (Account<?> account : Accounts.get()) {
            if (account.getType() == AccountType.Cracked) crackedAccounts.add(account);
        }

        if (crackedAccounts.isEmpty()) {
            ChatUtils.warning("No cracked accounts found. Add cracked accounts in Meteor's account manager.");
            return;
        }

        int workerId = aeropack$loadWorkerId();
        if (workerId > 0 && workerId <= crackedAccounts.size()) {
            Account<?> account = crackedAccounts.get(workerId - 1);
            if (account.login()) {
                ChatUtils.infoPrefix("Swarm", "Switched to account: (highlight)%s (worker ID: %d)", account.getUsername(), workerId);
            }
        } else {
            int index = Math.abs(aeropack$accountCounter.getAndIncrement() % crackedAccounts.size());
            Account<?> account = crackedAccounts.get(index);
            if (account.login()) {
                ChatUtils.infoPrefix("Swarm", "Switched to account: (highlight)%s (no ID, round-robin)", account.getUsername());
            }
        }
    }

    @Unique
    private static int aeropack$savedWorkerId = 0;

    @Unique
    private static void aeropack$saveWorkerId(int id) {
        aeropack$savedWorkerId = id;
        ChatUtils.infoPrefix("Swarm", "Assigned worker ID: (highlight)%d", id);
    }

    @Unique
    private static int aeropack$loadWorkerId() {
        return aeropack$savedWorkerId;
    }

    // ── Inventory file writing ────────────────────────────────────────────

    @Unique
    private static void aeropack$writeInventoryToFile(Minecraft mc) {
        if (mc.player == null) return;

        SwarmGuard.initInventoryDir();
        String workerName = mc.player.getName().getString();
        int workerId = aeropack$savedWorkerId;
        String fileName = "inv-" + (workerId > 0 ? workerId : Math.abs(workerName.hashCode() % 10000)) + ".txt";

        try {
            net.minecraft.world.entity.player.Inventory inv = mc.player.getInventory();
            int emptyCount = 0;

            for (int i = 0; i < 36; i++) {
                if (inv.getItem(i).isEmpty()) emptyCount++;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("WORKER:").append(workerName).append("\n");
            sb.append("EMPTY:").append(emptyCount).append("\n");

            net.minecraft.client.multiplayer.ServerData serverEntry = mc.getCurrentServer();
            if (serverEntry != null) {
                sb.append("SERVER:").append(serverEntry.ip).append("\n");
            }

            java.nio.file.Files.writeString(SwarmGuard.INVENTORY_DIR.resolve(fileName), sb.toString());
        } catch (Exception ignored) {}
    }
}
