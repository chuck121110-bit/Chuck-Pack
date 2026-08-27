package net.chuck.chuckpack.modules.misc;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.chuck.chuckpack.util.config.ChuckConfig;
import net.chuck.chuckpack.util.config.DebugLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static meteordevelopment.meteorclient.gui.renderer.GuiRenderer.COPY;

public class AutoLogin extends Module {
    private static AutoLogin instance;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> autoSave = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-save")
            .description("Automatically saves passwords when you login or register.")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> ignoreSelf = sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-self")
            .description("Ignore self commands. Recommended to leave enabled, otherwise Auto Save will trigger on Auto Login commands.")
            .defaultValue(true)
            .visible(autoSave::get)
            .build()
    );
    private final Setting<String> loginCommand = sgGeneral.add(new StringSetting.Builder()
            .name("login-command")
            .description("Command to login.")
            .defaultValue("/login")
            .visible(autoSave::get)
            .build()
    );
    private final Setting<List<String>> commandsToHandle = sgGeneral.add(new StringListSetting.Builder()
            .name("commands-to-handle")
            .description("Commands to handle.")
            .defaultValue("login", "log", "l", "register", "reg", "signin", "sign", "auth", "authenticate", "premium", "account", "join")
            .visible(autoSave::get)
            .build()
    );
    private final Setting<Boolean> saveServerIp = sgGeneral.add(new BoolSetting.Builder()
            .name("save-server-ip")
            .description("Save server IP in filter.")
            .defaultValue(true)
            .visible(autoSave::get)
            .build()
    );
    private final Setting<Boolean> saveUsername = sgGeneral.add(new BoolSetting.Builder()
            .name("save-username")
            .description("Save username in filter.")
            .defaultValue(true)
            .visible(autoSave::get)
            .build()
    );
    private final Setting<Boolean> checkPasswordCommand = sgGeneral.add(new BoolSetting.Builder()
            .name("check-password-command")
            .description("Whether to check password command when adding new auto login.")
            .defaultValue(true)
            .visible(autoSave::get)
            .build()
    );
    private final Setting<Boolean> checkExecutionMode = sgGeneral.add(new BoolSetting.Builder()
            .name("check-execution-mode")
            .description("Whether to check execution mode when adding new auto login.")
            .defaultValue(false)
            .visible(autoSave::get)
            .build()
    );
    private final Setting<Boolean> checkDelay = sgGeneral.add(new BoolSetting.Builder()
            .name("check-delay")
            .description("Whether to check delay when adding new auto login.")
            .defaultValue(false)
            .visible(autoSave::get)
            .build()
    );
    private final Setting<Boolean> checkUsername = sgGeneral.add(new BoolSetting.Builder()
            .name("check-username")
            .description("Whether to check username when adding new auto login.")
            .defaultValue(false)
            .visible(autoSave::get)
            .build()
    );
    private final Setting<Boolean> checkServerIp = sgGeneral.add(new BoolSetting.Builder()
            .name("check-server-ip")
            .description("Whether to check server IP when adding new auto login.")
            .defaultValue(false)
            .visible(autoSave::get)
            .build()
    );
    private final Setting<Boolean> checkLastLogin = sgGeneral.add(new BoolSetting.Builder()
            .name("check-last-login")
            .description("Whether to check last login when adding new auto login.")
            .defaultValue(false)
            .visible(autoSave::get)
            .build()
    );

    public static List<BaseAutoLogin> autoLogins = new ArrayList<>();
    private long loginStartTime = -1;
    private boolean shouldContinueProcessing = true;
    private boolean initialized = true;

    private boolean isSendingChatMessage = false;

    private final DebugLogger debugLogger;

    public AutoLogin() {
        super(Categories.Misc, "auto-login", "Automatically logs in your account using /login. (Ported from Meteorist)");
        instance = this;
        debugLogger = new DebugLogger(this, settings);
    }

    public static void onChatSent(String message) {
        if (instance == null || !instance.isActive() || !instance.autoSave.get()) return;
        if (instance.ignoreSelf.get() && instance.isSendingChatMessage) return;

        if (!message.startsWith("/")) return;

        String command = message.substring(1);
        String[] args = command.split(" ");
        if (args.length < 2) return;

        String cmdName = args[0];
        boolean matched = false;
        for (String c : instance.commandsToHandle.get()) {
            if (c.equalsIgnoreCase(cmdName)) {
                matched = true;
                break;
            }
        }
        if (!matched) return;

        BaseAutoLogin entry = new BaseAutoLogin();
        entry.loginCommand.set(instance.loginCommand.get() + " " + args[1]);

        if (instance.saveUsername.get()) {
            entry.usernameFilter.set(instance.mc.getUser().getName());
        }

        if (instance.saveServerIp.get()) {
            String worldName = Utils.getWorldName();
            if (worldName != null && !worldName.isEmpty()) {
                entry.serverIpFilter.set(worldName);
            }
        }

        if (!instance.exists(entry)) {
            autoLogins.add(entry);
            instance.info("Auto-saved login for " + Utils.getWorldName());
            instance.saveToDisk();
        }
    }

    private void saveToDisk() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (BaseAutoLogin autoLogin : autoLogins) {
            CompoundTag mTag = new CompoundTag();
            mTag.put("autoLogin", autoLogin.toTag());
            list.add(mTag);
        }
        tag.put("autoLogins", list);
        ChuckConfig.save(this.name, "default", tag);
    }

    public CompoundTag toTag() {
        CompoundTag superTag = super.toTag();
        saveToDisk();
        return superTag;
    }

    public Module fromTag(CompoundTag superTag) {
        CompoundTag tag = ChuckConfig.load(this.name, "default");

        autoLogins.clear();
        ListTag list = tag.getListOrEmpty("autoLogins");

        for (Tag tagII : list) {
            CompoundTag tagI = (CompoundTag) tagII;

            BaseAutoLogin autoLogin = new BaseAutoLogin();
            CompoundTag autoLoginTag = (CompoundTag) tagI.get("autoLogin");

            if (autoLoginTag != null) autoLogin.fromTag(autoLoginTag);

            autoLogins.add(autoLogin);
        }

        return super.fromTag(superTag);
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();
        fillWidget(theme, list);
        return list;
    }

    private void fillWidget(GuiTheme theme, WVerticalList list) {
        list.clear();

        for (BaseAutoLogin autoLogin : autoLogins) {

            list.add(theme.settings(autoLogin.settings)).expandX();

            WContainer controls = list.add(theme.horizontalList()).widget();

            if (autoLogins.size() > 1) {
                WContainer moveContainer = controls.add(theme.horizontalList()).expandX().widget();
                int index = autoLogins.indexOf(autoLogin);

                if (index > 0) {
                    WButton moveUp = moveContainer.add(theme.button("\u25B2")).expandX().widget();
                    moveUp.tooltip = "Move auto login up.";
                    moveUp.action = () -> {
                        autoLogins.remove(index);
                        autoLogins.add(index - 1, autoLogin);
                        fillWidget(theme, list);
                    };
                }

                if (index < autoLogins.size() - 1) {
                    WButton moveDown = moveContainer.add(theme.button("\u25BC")).expandX().widget();
                    moveDown.tooltip = "Move auto login down.";
                    moveDown.action = () -> {
                        autoLogins.remove(index);
                        autoLogins.add(index + 1, autoLogin);
                        fillWidget(theme, list);
                    };
                }
            }

            WButton copy = controls.add(theme.button(COPY)).widget();
            copy.tooltip = "Duplicate auto login.";
            copy.action = () -> {
                autoLogins.add(autoLogins.indexOf(autoLogin), autoLogin.copy());
                fillWidget(theme, list);
            };

            WMinus remove = controls.add(theme.minus()).widget();
            remove.tooltip = "Remove auto login.";
            remove.action = () -> {
                autoLogins.remove(autoLogin);
                fillWidget(theme, list);
            };
        }

        if (!autoLogins.isEmpty()) list.add(theme.horizontalSeparator()).expandX();

        WContainer controls = list.add(theme.horizontalList()).expandX().widget();

        WButton add = controls.add(theme.button("New Auto Login")).expandX().widget();
        add.action = () -> {
            BaseAutoLogin autoLogin = new BaseAutoLogin();
            autoLogins.add(autoLogin);
            fillWidget(theme, list);
        };

        WButton removeAll = controls.add(theme.button("Remove All Auto Logins")).expandX().widget();
        removeAll.action = () -> {
            autoLogins.clear();
            fillWidget(theme, list);
        };
    }

    @Override
    public void onActivate() {
        loginStartTime = -1;
        shouldContinueProcessing = true;
        initialized = true;
    }

    @Override
    public void onDeactivate() {
        saveToDisk();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.level == null) return;

        if (initialized) {
            loginStartTime = -1;
            shouldContinueProcessing = true;
            initialized = false;
        }

        if (shouldContinueProcessing) {
            if (loginStartTime == -1) loginStartTime = mc.level.getDefaultClockTime();
            boolean hasRemainingAutoLogins = false;
            for (BaseAutoLogin autoLogin : List.copyOf(autoLogins)) {
                if (mc.level.getDefaultClockTime() < loginStartTime + autoLogin.delay.get()) {
                    hasRemainingAutoLogins = true;
                    continue;
                }
                debugLogger.info("Delay check passed");

                BaseAutoLogin.ExecutionMode executionMode = autoLogin.executionMode.get();
                if (executionMode == BaseAutoLogin.ExecutionMode.Multiplayer && mc.isSingleplayer()) continue;
                if (executionMode == BaseAutoLogin.ExecutionMode.Singleplayer && !mc.isSingleplayer()) continue;
                debugLogger.info("Execution mode check passed");

                String serverIpFilter = autoLogin.serverIpFilter.get();
                if (!serverIpFilter.isEmpty()) {
                    if (!Utils.getWorldName().equals(serverIpFilter)) continue;
                    debugLogger.info("Server ip check passed");
                }

                String usernameFilter = autoLogin.usernameFilter.get();
                if (!usernameFilter.isEmpty()) {
                    if (!mc.getUser().getName().equals(usernameFilter)) continue;
                    debugLogger.info("Username check passed");
                }

                isSendingChatMessage = true;
                ChatUtils.sendPlayerMsg(autoLogin.loginCommand.get());
                isSendingChatMessage = false;

                if (autoLogin.lastLogin.get()) {
                    shouldContinueProcessing = false;
                    break;
                }

                if (!hasRemainingAutoLogins) shouldContinueProcessing = false;
            }
        }
    }

    public boolean exists(BaseAutoLogin toCheck) {
        for (BaseAutoLogin autoLogin : List.copyOf(autoLogins)) {
            boolean allChecksPassed = true;

            if (checkPasswordCommand.get()) {
                allChecksPassed &= autoLogin.loginCommand.get().equals(toCheck.loginCommand.get());
            }

            if (checkExecutionMode.get()) {
                allChecksPassed &= autoLogin.executionMode.get() == toCheck.executionMode.get();
            }

            if (checkDelay.get()) {
                allChecksPassed &= Objects.equals(autoLogin.delay.get(), toCheck.delay.get());
            }

            if (checkUsername.get()) {
                allChecksPassed &= autoLogin.usernameFilter.get().equals(toCheck.usernameFilter.get());
            }

            if (checkServerIp.get()) {
                allChecksPassed &= autoLogin.serverIpFilter.get().equals(toCheck.serverIpFilter.get());
            }

            if (checkLastLogin.get()) {
                allChecksPassed &= autoLogin.lastLogin.get() == toCheck.lastLogin.get();
            }

            if (allChecksPassed) return true;
        }
        return false;
    }
}
