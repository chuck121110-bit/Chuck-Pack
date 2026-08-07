package xaero.hud.config.channel.register;

import xaero.hud.minimap.config.listener.handler.MinimapConfigOptionClientHandlers;
import xaero.hud.minimap.config.option.ui.MinimapConfigOptionUIRegister;
import xaero.hud.minimap.config.option.value.redirect.MinimapConfigOptionClientRedirectors;
import xaero.hud.minimap.config.primary.option.MinimapPrimaryClientConfigOptions;
import xaero.lib.client.config.channel.register.handler.IConfigChannelClientRegistryHandler;
import xaero.lib.client.config.listener.ClientConfigChangeListener;
import xaero.lib.client.config.option.ClientConfigOptionManager;
import xaero.lib.client.config.option.ui.ConfigOptionUITypeManager;
import xaero.lib.client.config.option.value.redirect.ClientOptionValueRedirectorManager;

public class HudModConfigClientRegistryHandler implements IConfigChannelClientRegistryHandler {
   public void registerPrimaryClientOptions(ClientConfigOptionManager manager) {
      MinimapPrimaryClientConfigOptions.registerAll(manager);
   }

   public void registerConfigOptionUITypes(ConfigOptionUITypeManager manager) {
      MinimapConfigOptionUIRegister.registerAll(manager);
   }

   public void registerClientOptionChangeHandlers(ClientConfigChangeListener registry) {
      MinimapConfigOptionClientHandlers.registerAll(registry);
   }

   public void registerOptionClientRedirectors(ClientOptionValueRedirectorManager manager) {
      MinimapConfigOptionClientRedirectors.registerAll(manager);
   }
}
