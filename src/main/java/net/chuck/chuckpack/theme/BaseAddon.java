package net.chuck.chuckpack.theme;

import net.chuck.chuckpack.theme.gui.themes.base.BaseGuiTheme;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.GuiThemes;
import org.slf4j.Logger;

public class BaseAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();

    public static final String MOD_ID = "ChuckPack";

    @Override
    public void onInitialize() {
        LOG.info("Initializing Exeter Theme Addon");

        GuiThemes.add(new BaseGuiTheme());
    }


    @Override
    public String getPackage() {
        return "net.chuck.chuckpack.theme";
    }
}
