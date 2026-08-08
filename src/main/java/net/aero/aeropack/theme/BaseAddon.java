package net.aero.aeropack.theme;

import net.aero.aeropack.theme.gui.themes.base.BaseGuiTheme;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.GuiThemes;
import org.slf4j.Logger;

public class BaseAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();

    public static final String MOD_ID = "aeropack";

    @Override
    public void onInitialize() {
        LOG.info("Initializing Exeter Theme Addon");

        GuiThemes.add(new BaseGuiTheme());
    }


    @Override
    public String getPackage() {
        return "net.aero.aeropack.theme";
    }
}
