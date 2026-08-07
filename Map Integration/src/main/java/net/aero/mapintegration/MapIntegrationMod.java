package net.aero.mapintegration;

import net.aero.mapintegration.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapIntegrationMod implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("MapIntegration");

    @Override
    public void onInitializeClient() {
        ModConfig.load();
        LOG.info("Map Integration loaded.");
    }
}
