package net.aero.aeropack;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.aero.aeropack.modules.combat.MaceDamage;
import net.aero.aeropack.modules.combat.SpearKill;
import net.aero.aeropack.modules.combat.Untouchable;
import net.aero.aeropack.modules.misc.AiChat;
import net.aero.aeropack.modules.misc.AiChatConverse;
import net.aero.aeropack.modules.misc.AntiSocial;
import net.aero.aeropack.modules.misc.AutoInteract;
import net.aero.aeropack.modules.misc.AutoLogin;
import net.aero.aeropack.modules.misc.ChatUtility;
import net.aero.aeropack.modules.misc.DoubleDoorsInteract;
import net.aero.aeropack.modules.misc.NbtFilter;
import net.aero.aeropack.modules.misc.MapIntegration;
import net.aero.aeropack.modules.misc.OppStats;
import net.aero.aeropack.modules.misc.PlayerTriangulate;
import net.aero.aeropack.modules.misc.UiUtilsMod;
import net.aero.aeropack.modules.misc.villagerroller.VillagerRoller;
import net.aero.aeropack.modules.misc.StaffMonitor;
import net.aero.aeropack.modules.movement.AutoFly;
import net.aero.aeropack.modules.movement.BedrockEscape;
import net.aero.aeropack.modules.movement.FlightScrollHandler;
import net.aero.aeropack.modules.render.CoordinateLogout;
import net.aero.aeropack.modules.render.DeepslateESP;
import net.aero.aeropack.modules.render.NewChunks;
import net.aero.aeropack.modules.render.PearlChecker;
import net.aero.aeropack.modules.render.TrueSight;
import net.aero.aeropack.modules.render.HoleTunnelStairsESP;
import net.aero.aeropack.modules.render.MobGearESP;
import net.aero.aeropack.modules.world.AutoFarming;
import net.aero.aeropack.modules.world.BaseFinder;
import net.aero.aeropack.modules.world.OreSim;
import net.aero.aeropack.util.config.AeroPackConfigModifier;
import net.aero.aeropack.util.config.CategoryConfig;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AeroPack extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("AeroPack");

    public static final Category AERO_CATEGORY = new Category("Aero Pack", () -> Items.EMERALD.getDefaultInstance());

    private static final List<Module> aeroModules = new ArrayList<>();
    private static final Map<Module, Category> naturalCategories = new LinkedHashMap<>();

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(AERO_CATEGORY);
    }

    @Override
    public void onInitialize() {
        LOG.info("Aero Pack initialising...");

        addModule(new NewChunks());
        addModule(new MaceDamage());
        addModule(new SpearKill());
        addModule(new AiChat());
        addModule(new AiChatConverse());
        addModule(new AutoLogin());
        addModule(new DoubleDoorsInteract());
        addModule(new AutoInteract());
        addModule(new AntiSocial());
        addModule(new AutoFly());
        addModule(new BedrockEscape());
        addModule(new AutoFarming());
        addModule(new ChatUtility());
        addModule(new PearlChecker());
        addModule(new DeepslateESP());
        addModule(new CoordinateLogout());
        addModule(new HoleTunnelStairsESP());
        addModule(new BaseFinder());
        addModule(new MobGearESP());
        addModule(new PlayerTriangulate());
        addModule(new Untouchable());
        addModule(new TrueSight());
        addModule(new NbtFilter());
        addModule(new StaffMonitor());
        addModule(new OppStats());
        addModule(new MapIntegration());
        addModule(new UiUtilsMod());
        addModule(new OreSim());
        addModule(new VillagerRoller());

        meteordevelopment.meteorclient.commands.Commands.add(
            new net.aero.aeropack.commands.SeedCommand()
        );
        meteordevelopment.meteorclient.commands.Commands.add(
            new net.aero.aeropack.commands.LocateCommand()
        );

        meteordevelopment.meteorclient.systems.hud.Hud.get().register(
            net.aero.aeropack.hud.KeybindsHud.INFO
        );
        meteordevelopment.meteorclient.systems.hud.Hud.get().register(
            net.aero.aeropack.hud.PlayerTriangulateHud.INFO
        );
        meteordevelopment.meteorclient.systems.hud.Hud.get().register(
            net.aero.aeropack.hud.AutoFlyHud.INFO
        );

        meteordevelopment.meteorclient.MeteorClient.EVENT_BUS.subscribe(
            new net.aero.aeropack.swarm.AutoSwarmConnectHandler()
        );
        meteordevelopment.meteorclient.MeteorClient.EVENT_BUS.subscribe(
            net.aero.aeropack.render.AeroShaderHelper.class
        );
        meteordevelopment.meteorclient.MeteorClient.EVENT_BUS.subscribe(
            new FlightScrollHandler()
        );
        meteordevelopment.meteorclient.MeteorClient.EVENT_BUS.subscribe(
            net.aero.aeropack.util.SetbackDetector.get()
        );
        CategoryConfig.load();
        if (CategoryConfig.isSeparateCategory()) {
            setSeparateCategory(true);
        }

        AeroPackConfigModifier.get();

        meteordevelopment.meteorclient.gui.GuiThemes.add(
            new net.aero.aeropack.theme.gui.themes.base.BaseGuiTheme()
        );

        LOG.info("Aero Pack ready.");
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
                List<Module> aeroGroup = modules.getGroup(AERO_CATEGORY);
                if (naturalGroup.contains(module)) {
                    naturalGroup.remove(module);
                    if (!aeroGroup.contains(module)) {
                        aeroGroup.add(module);
                    }
                }
            } else {
                List<Module> aeroGroup = modules.getGroup(AERO_CATEGORY);
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
        return "net.aero.aeropack";
    }
}
