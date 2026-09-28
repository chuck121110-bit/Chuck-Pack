package net.chuck.chuckpack;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.chuck.chuckpack.modules.combat.KnockbackDisplacer;
import net.chuck.chuckpack.modules.combat.MaceDamage;
import net.chuck.chuckpack.modules.combat.SpearKill;
import net.chuck.chuckpack.modules.combat.Untouchable;
import net.chuck.chuckpack.modules.misc.AiChat;
import net.chuck.chuckpack.modules.misc.AiChatConverse;
import net.chuck.chuckpack.modules.misc.AntiSocial;
import net.chuck.chuckpack.modules.misc.AutoInteract;
import net.chuck.chuckpack.modules.misc.BaritoneNotifier;
import net.chuck.chuckpack.modules.misc.AutoLogin;
import net.chuck.chuckpack.modules.misc.ChatUtility;
import net.chuck.chuckpack.modules.misc.DoubleDoorsInteract;
import net.chuck.chuckpack.modules.misc.NbtFilter;
import net.chuck.chuckpack.modules.misc.MapIntegration;
import net.chuck.chuckpack.modules.misc.OppStats;
import net.chuck.chuckpack.modules.misc.PlayerTriangulate;
import net.chuck.chuckpack.modules.misc.villagerroller.VillagerRoller;
import net.chuck.chuckpack.modules.misc.StaffMonitor;
import net.chuck.chuckpack.modules.movement.AutoFly;
import net.chuck.chuckpack.modules.movement.BaritoneSwim;
import net.chuck.chuckpack.modules.movement.BedrockEscape;
import net.chuck.chuckpack.modules.movement.Tunnel;
import net.chuck.chuckpack.modules.movement.FlightScrollHandler;
import net.chuck.chuckpack.modules.render.CoordinateLogout;
import net.chuck.chuckpack.modules.render.DeepslateESP;
import net.chuck.chuckpack.modules.render.NewChunks;
import net.chuck.chuckpack.modules.render.PearlChecker;
import net.chuck.chuckpack.modules.render.TrueSight;
import net.chuck.chuckpack.modules.render.HoleTunnelStairsESP;
import net.chuck.chuckpack.modules.render.MobGearESP;
import net.chuck.chuckpack.modules.world.AutoFarming;
import net.chuck.chuckpack.modules.world.BaseFinder;
import net.chuck.chuckpack.modules.world.OreSim;
import net.chuck.chuckpack.modules.world.printer.Printer;
import net.chuck.chuckpack.modules.world.printer.Shredder;
import net.chuck.chuckpack.util.config.ChuckPackConfigModifier;
import net.chuck.chuckpack.util.config.CategoryConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChuckPack extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("ChuckPack");

    public static final Category CHUCK_CATEGORY = new Category("Chuck Pack", () -> new ItemStack(Items.EMERALD));

    private static final List<Module> aeroModules = new ArrayList<>();
    private static final Map<Module, Category> naturalCategories = new LinkedHashMap<>();

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CHUCK_CATEGORY);
    }

    @Override
    public void onInitialize() {
        LOG.info("Chuck Pack initialising...");

        addModule(new NewChunks());
        addModule(new MaceDamage());
        addModule(new KnockbackDisplacer());
        addModule(new SpearKill());
        addModule(new AiChat());
        addModule(new AiChatConverse());
        addModule(new AutoLogin());
        addModule(new DoubleDoorsInteract());
        addModule(new AutoInteract());
        addModule(new AntiSocial());
        addModule(new AutoFly());
        addModule(new BaritoneSwim());
        addModule(new BedrockEscape());
        addModule(new Tunnel());
        addModule(new AutoFarming());
        addModule(new ChatUtility());
        addModule(new BaritoneNotifier());
        addModule(new PearlChecker());
        addModule(new DeepslateESP());
        addModule(new CoordinateLogout());
        addModule(new HoleTunnelStairsESP());
        addModule(new BaseFinder());
        addModule(new Printer());
        addModule(new Shredder());
        addModule(new MobGearESP());
        addModule(new PlayerTriangulate());
        addModule(new Untouchable());
        addModule(new TrueSight());
        addModule(new NbtFilter());
        addModule(new StaffMonitor());
        addModule(new OppStats());
        addModule(new MapIntegration());
        addModule(new OreSim());
        addModule(new VillagerRoller());

        meteordevelopment.meteorclient.commands.Commands.add(
            new net.chuck.chuckpack.commands.SeedCommand()
        );
        meteordevelopment.meteorclient.commands.Commands.add(
            new net.chuck.chuckpack.commands.LocateCommand()
        );
        meteordevelopment.meteorclient.commands.Commands.add(
            new net.chuck.chuckpack.commands.AutoFlyCommand()
        );

        meteordevelopment.meteorclient.systems.hud.Hud.get().register(
            net.chuck.chuckpack.hud.KeybindsHud.INFO
        );
        meteordevelopment.meteorclient.systems.hud.Hud.get().register(
            net.chuck.chuckpack.hud.PlayerTriangulateHud.INFO
        );
        meteordevelopment.meteorclient.systems.hud.Hud.get().register(
            net.chuck.chuckpack.hud.AutoFlyHud.INFO
        );

        meteordevelopment.meteorclient.MeteorClient.EVENT_BUS.subscribe(
            new net.chuck.chuckpack.swarm.AutoSwarmConnectHandler()
        );
        meteordevelopment.meteorclient.MeteorClient.EVENT_BUS.subscribe(
            net.chuck.chuckpack.render.AeroShaderHelper.class
        );
        meteordevelopment.meteorclient.MeteorClient.EVENT_BUS.subscribe(
            new FlightScrollHandler()
        );
        meteordevelopment.meteorclient.MeteorClient.EVENT_BUS.subscribe(
            net.chuck.chuckpack.util.SetbackDetector.get()
        );
        CategoryConfig.load();
        if (CategoryConfig.isSeparateCategory()) {
            setSeparateCategory(true);
        }

        ChuckPackConfigModifier.get();
        // Auto-update check on MC startup (like separate-category, not a module)
        net.chuck.chuckpack.util.AutoUpdateChecker.checkOnStartup();

        meteordevelopment.meteorclient.gui.GuiThemes.add(
            new net.chuck.chuckpack.theme.gui.themes.base.BaseGuiTheme()
        );

        LOG.info("Chuck Pack ready.");
    }

    private void addModule(Module module) {
        aeroModules.add(module);
        naturalCategories.put(module, module.category);
        Modules.get().add(module);
    }

    public static void setSeparateCategory(boolean separate) {
        Modules modules = Modules.get();
        if (modules == null) return;

        for (Module module : aeroModules) {
            Category natural = naturalCategories.get(module);
            if (natural == null) continue;

            if (separate) {
                List<Module> naturalGroup = modules.getGroup(natural);
                List<Module> aeroGroup = modules.getGroup(CHUCK_CATEGORY);
                if (naturalGroup.contains(module)) {
                    naturalGroup.remove(module);
                    if (!aeroGroup.contains(module)) {
                        aeroGroup.add(module);
                    }
                }
            } else {
                List<Module> aeroGroup = modules.getGroup(CHUCK_CATEGORY);
                List<Module> naturalGroup = modules.getGroup(natural);
                if (aeroGroup.contains(module)) {
                    aeroGroup.remove(module);
                    if (!naturalGroup.contains(module)) {
                        naturalGroup.add(module);
                    }
                }
            }
        }

        modules.sortModules();
    }

    @Override
    public String getPackage() {
        return "net.chuck.chuckpack";
    }
}
