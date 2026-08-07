package xaero.hud.minimap.radar.category;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import net.minecraft.class_310;
import xaero.common.HudMod;
import xaero.common.minimap.MinimapProcessor;
import xaero.hud.category.rule.resolver.ObjectCategoryRuleResolver;
import xaero.hud.category.setting.ObjectCategoryDefaultSettingsSetter;
import xaero.hud.category.setting.ObjectCategorySetting;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.common.radar.category.EntityRadarCategorySerializers;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.radar.RadarSession;
import xaero.hud.minimap.radar.category.serialization.EntityRadarCategorySerializationHandler;
import xaero.hud.minimap.radar.category.setting.EntityRadarCategorySettings;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;
import xaero.lib.common.config.Config;

public final class EntityRadarCategoryManager {
   private final Path mainLegacyFilePath;
   private final Path secondaryLegacyFilePath;
   private final Path defaultClientFilePath;
   private final Path defaultServerFilePath;
   private final Path secondaryDefaultClientFilePath;
   private final Path secondaryDefaultServerFilePath;
   private final EntityRadarCategorySerializers serializers;
   private boolean initialized;
   private EntityRadarCategoryConfigIO mainIO;
   private EntityRadarCategoryFileIO defaultClientIO;
   private EntityRadarCategoryFileIO defaultServerIO;
   private EntityRadarCategoryFileIO secondaryDefaultClientIO;
   private EntityRadarCategoryFileIO secondaryDefaultServerIO;
   private EntityRadarCategory rootCategory;
   private Config rootCategoryConfig;
   private EntityRadarCategory syncedRootCategory;
   private Config editedCategoryConfig;
   private EntityRadarCategory editedCategory;
   private boolean editedCategoryNeedsSaving;
   private EntityRadarDefaultCategories defaultCategoryConfigurator;
   private EntityRadarDefaultCategories defaultServerCategoryConfigurator;
   private ObjectCategoryRuleResolver ruleResolver;
   private ObjectCategoryDefaultSettingsSetter defaultSettings;

   private EntityRadarCategoryManager(@Nonnull Path mainLegacyFilePath, @Nonnull Path secondaryLegacyFilePath, @Nonnull Path defaultClientFilePath, @Nonnull Path defaultServerFilePath, @Nonnull Path secondaryDefaultClientFilePath, @Nonnull Path secondaryDefaultServerFilePath, @Nonnull EntityRadarCategorySerializers serializers) {
      this.mainLegacyFilePath = mainLegacyFilePath;
      this.secondaryLegacyFilePath = secondaryLegacyFilePath;
      this.defaultClientFilePath = defaultClientFilePath;
      this.defaultServerFilePath = defaultServerFilePath;
      this.secondaryDefaultClientFilePath = secondaryDefaultClientFilePath;
      this.secondaryDefaultServerFilePath = secondaryDefaultServerFilePath;
      this.serializers = serializers;
   }

   public void init() throws IOException {
      this.defaultCategoryConfigurator = EntityRadarDefaultCategories.Builder.begin().build();
      this.defaultServerCategoryConfigurator = EntityRadarDefaultCategories.Builder.begin().setForServer(true).build();
      EntityRadarCategorySerializationHandler.Builder serializationHandlerBuilder = EntityRadarCategorySerializationHandler.Builder.begin(this.serializers.getGson());
      this.defaultClientIO = EntityRadarCategoryFileIO.Builder.begin(serializationHandlerBuilder).setSaveLocationPath(this.defaultClientFilePath).build();
      this.defaultServerIO = EntityRadarCategoryFileIO.Builder.begin(serializationHandlerBuilder).setSaveLocationPath(this.defaultServerFilePath).build();
      this.secondaryDefaultClientIO = EntityRadarCategoryFileIO.Builder.begin(serializationHandlerBuilder).setSaveLocationPath(this.secondaryDefaultClientFilePath).build();
      this.secondaryDefaultServerIO = EntityRadarCategoryFileIO.Builder.begin(serializationHandlerBuilder).setSaveLocationPath(this.secondaryDefaultServerFilePath).build();
      EntityRadarCategorySerializationHandler serializationHandler = (EntityRadarCategorySerializationHandler)serializationHandlerBuilder.build();
      this.mainIO = EntityRadarCategoryConfigIO.Builder.begin().setOption(MinimapProfiledConfigOptions.RADAR_CATEGORIES).setSerializationHandler(serializationHandler).build();
      this.ruleResolver = ObjectCategoryRuleResolver.Builder.begin().build();
      this.defaultSettings = ObjectCategoryDefaultSettingsSetter.Builder.begin().setSettings(EntityRadarCategorySettings.SETTINGS).build();
      if (!Files.exists(this.defaultClientFilePath, new LinkOption[0]) && Files.exists(this.mainLegacyFilePath, new LinkOption[0])) {
         Files.move(this.mainLegacyFilePath, this.defaultClientFilePath);
      }

      if (!Files.exists(this.secondaryDefaultClientFilePath, new LinkOption[0]) && Files.exists(this.secondaryLegacyFilePath, new LinkOption[0])) {
         Files.createDirectories(this.secondaryDefaultClientFilePath.getParent());
         Files.move(this.secondaryLegacyFilePath, this.secondaryDefaultClientFilePath);
      }

      ClientConfigManager clientConfigManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      Config currentClientConfig = clientConfigManager.getCurrentProfile();
      EntityRadarCategory root = this.mainIO.loadRootCategory(currentClientConfig);
      EntityRadarCategory fetchedClientDefault = null;
      if (root == null) {
         root = fetchedClientDefault = this.fetchDefaultClientCategory();
      } else {
         this.defaultSettings.setDefaultsFor(root, true);
      }

      this.mainIO.storeRootCategory(root, currentClientConfig);
      if (!Files.exists(this.defaultClientFilePath, new LinkOption[0])) {
         if (fetchedClientDefault == null) {
            fetchedClientDefault = this.fetchDefaultClientCategory();
         }

         this.defaultClientIO.saveRootCategory(fetchedClientDefault);
      }

      if (!Files.exists(this.defaultServerFilePath, new LinkOption[0])) {
         EntityRadarCategory defaultServerCategory = this.fetchDefaultServerCategory();
         this.defaultServerIO.saveRootCategory(defaultServerCategory);
      }

      HudMod.INSTANCE.getSettings().resetEntityRadarBackwardsCompatibilityConfig();
      this.rootCategory = root;
      this.rootCategoryConfig = currentClientConfig;
      this.initialized = true;
   }

   public EntityRadarCategory fetchDefaultClientCategory() {
      return this.fetchDefaultClientCategory(20);
   }

   public EntityRadarCategory fetchDefaultClientCategory(int attempts) {
      EntityRadarCategory root = null;

      try {
         if (Files.exists(this.defaultClientFilePath, new LinkOption[0])) {
            root = this.defaultClientIO.loadRootCategory();
         }

         if (root == null && Files.exists(this.secondaryDefaultClientFilePath, new LinkOption[0])) {
            root = this.secondaryDefaultClientIO.loadRootCategory();
         }
      } catch (IOException ioe) {
         if (attempts <= 1) {
            throw new RuntimeException(ioe);
         }

         MinimapLogs.LOGGER.info("IO exception loading default client entity radar categories! Retrying...");
         --attempts;
         return this.fetchDefaultClientCategory(attempts);
      }

      if (root == null) {
         root = this.defaultCategoryConfigurator.setupDefault(HudMod.INSTANCE.getSettings());
      }

      this.defaultSettings.setDefaultsFor(root, true);
      return root;
   }

   public EntityRadarCategory fetchDefaultServerCategory() {
      return this.fetchDefaultServerCategory(20);
   }

   public EntityRadarCategory fetchDefaultServerCategory(int attempts) {
      EntityRadarCategory root = null;

      try {
         if (Files.exists(this.defaultServerFilePath, new LinkOption[0])) {
            root = this.defaultServerIO.loadRootCategory();
         }

         if (root == null && Files.exists(this.secondaryDefaultServerFilePath, new LinkOption[0])) {
            root = this.secondaryDefaultServerIO.loadRootCategory();
         }
      } catch (IOException ioe) {
         if (attempts <= 1) {
            throw new RuntimeException(ioe);
         }

         MinimapLogs.LOGGER.info("IO exception loading default server entity radar categories! Retrying...");
         --attempts;
         return this.fetchDefaultServerCategory(attempts);
      }

      if (root == null) {
         root = this.defaultServerCategoryConfigurator.setupDefault(HudMod.INSTANCE.getSettings());
      }

      return root;
   }

   public void updateFromConfigChange(Config config) {
      if (this.initialized) {
         ClientConfigManager clientConfigManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
         Config syncedConfig = clientConfigManager.getServerSynced().getConfig();
         if (config == syncedConfig) {
            MinimapLogs.LOGGER.info("Updating synced root category!");
            this.syncedRootCategory = this.mainIO.loadRootCategory(config);
         } else if (config == clientConfigManager.getCurrentProfile() && config != this.rootCategoryConfig) {
            this.rootCategory = this.mainIO.loadRootCategory(config);
            if (this.rootCategory == null) {
               this.rootCategory = this.fetchDefaultClientCategory();
            } else {
               this.defaultSettings.setDefaultsFor(this.rootCategory, true);
            }

            this.rootCategoryConfig = config;
         }

         HudMod.INSTANCE.getMinimap().getMinimapFBORenderer().resetEntityIcons();
         MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
         if (minimapSession != null) {
            MinimapProcessor minimapProcessor = minimapSession.getProcessor();
            RadarSession radar = minimapProcessor.getRadarSession();
            radar.update(class_310.method_1551().field_1687, class_310.method_1551().method_1560(), class_310.method_1551().field_1724);
         }
      }
   }

   public ObjectCategoryRuleResolver getRuleResolver() {
      return this.ruleResolver;
   }

   public EntityRadarCategory getRootCategory() {
      return this.rootCategory;
   }

   public EntityRadarCategory getSyncedRootCategory() {
      return this.syncedRootCategory;
   }

   public EntityRadarCategory getEffectiveSyncedRootCategory() {
      ClientConfigManager clientConfigManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      return clientConfigManager.shouldIgnoreServerEnforcement(MinimapProfiledConfigOptions.RADAR_CATEGORIES) ? null : this.getSyncedRootCategory();
   }

   public EntityRadarDefaultCategories getDefaultCategoryConfigurator() {
      return this.defaultCategoryConfigurator;
   }

   public Path getSecondaryLegacyFilePath() {
      return this.secondaryLegacyFilePath;
   }

   public EntityRadarCategory loadEditedCategory(Config config, boolean clientSide) {
      ClientConfigManager clientConfigManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      this.editedCategoryConfig = config;
      this.editedCategory = config == clientConfigManager.getCurrentProfile() ? this.rootCategory : this.mainIO.loadRootCategory(config);
      if (this.editedCategory != null) {
         return this.editedCategory;
      } else {
         return clientSide ? (this.editedCategory = this.fetchDefaultClientCategory()) : (this.editedCategory = this.fetchDefaultServerCategory());
      }
   }

   public Config getEditedCategoryConfig() {
      return this.editedCategoryConfig;
   }

   public EntityRadarCategory getEditedCategory() {
      return this.editedCategory;
   }

   public void forgetEditedCategory() {
      this.editedCategoryConfig = null;
      this.editedCategory = null;
   }

   public void storeEditedCategory(boolean clientSide) {
      this.storeEditedCategory(this.editedCategory, clientSide);
   }

   public void storeEditedCategory(EntityRadarCategory replacement, boolean clientSide) {
      this.editedCategory = replacement;
      if (clientSide) {
         this.rootCategory = this.editedCategory;
      }

      this.editedCategoryNeedsSaving = false;
      this.mainIO.storeRootCategory(this.editedCategory, this.editedCategoryConfig);
   }

   public EntityRadarCategoryConfigIO getMainIO() {
      return this.mainIO;
   }

   public void resetRootCategorySettings(IEditConfigScreenContext context) {
      if (this.editedCategoryConfig == null) {
         MinimapLogs.LOGGER.error("Tried to reset entity radar root category settings for null config");
      } else {
         EntityRadarCategory defaultCategory;
         if (context.isClientSide()) {
            defaultCategory = this.fetchDefaultClientCategory();
         } else {
            defaultCategory = this.fetchDefaultServerCategory();
         }

         if (this.editedCategory != null) {
            for(ObjectCategorySetting<?> setting : EntityRadarCategorySettings.SETTINGS.values()) {
               this.copySettingValue(setting, defaultCategory, this.editedCategory);
            }
         } else {
            this.editedCategory = defaultCategory;
         }

         this.mainIO.storeRootCategory(this.editedCategory, this.editedCategoryConfig);
      }
   }

   private <T> void copySettingValue(ObjectCategorySetting<T> setting, EntityRadarCategory from, EntityRadarCategory to) {
      to.setSettingValue(setting, from.getSettingValue(setting));
   }

   public boolean editedCategoryNeedsSaving() {
      return this.editedCategoryNeedsSaving;
   }

   public void setEditedCategoryNeedsSaving(boolean editedCategoryNeedsSaving) {
      this.editedCategoryNeedsSaving = editedCategoryNeedsSaving;
   }

   public static final class Builder {
      private Path mainLegacyFilePath;
      private Path secondaryLegacyFilePath;
      private Path defaultClientFilePath;
      private Path defaultServerFilePath;
      private Path secondaryDefaultFolderPath;
      private EntityRadarCategorySerializers serializers;

      private Builder() {
      }

      public Builder setMainLegacyFilePath(Path mainLegacyFilePath) {
         this.mainLegacyFilePath = mainLegacyFilePath;
         return this;
      }

      public Builder setSecondaryLegacyFilePath(Path secondaryLegacyFilePath) {
         this.secondaryLegacyFilePath = secondaryLegacyFilePath;
         return this;
      }

      public Builder setDefaultClientFilePath(Path defaultClientFilePath) {
         this.defaultClientFilePath = defaultClientFilePath;
         return this;
      }

      public Builder setDefaultServerFilePath(Path defaultServerFilePath) {
         this.defaultServerFilePath = defaultServerFilePath;
         return this;
      }

      public Builder setSecondaryDefaultFolderPath(Path secondaryDefaultFolderPath) {
         this.secondaryDefaultFolderPath = secondaryDefaultFolderPath;
         return this;
      }

      public Builder setSerializers(EntityRadarCategorySerializers serializers) {
         this.serializers = serializers;
         return this;
      }

      public Builder setDefault() {
         this.setMainLegacyFilePath(EntityRadarCategoryConstants.LEGACY_CONFIG_PATH);
         this.setSecondaryLegacyFilePath(EntityRadarCategoryConstants.LEGACY_DEFAULT_CONFIG_PATH);
         this.setDefaultClientFilePath((Path)null);
         this.setDefaultServerFilePath((Path)null);
         this.setSecondaryDefaultFolderPath((Path)null);
         this.setSerializers((EntityRadarCategorySerializers)null);
         return this;
      }

      public EntityRadarCategoryManager build() {
         if (this.mainLegacyFilePath != null && this.secondaryLegacyFilePath != null && this.defaultClientFilePath != null && this.defaultServerFilePath != null && this.secondaryDefaultFolderPath != null && this.serializers != null) {
            Path secondaryDefaultClientFilePath = this.secondaryDefaultFolderPath.resolve(this.defaultClientFilePath.getFileName());
            Path secondaryDefaultServerFilePath = this.secondaryDefaultFolderPath.resolve(this.defaultServerFilePath.getFileName());
            return new EntityRadarCategoryManager(this.mainLegacyFilePath, this.secondaryLegacyFilePath, this.defaultClientFilePath, this.defaultServerFilePath, secondaryDefaultClientFilePath, secondaryDefaultServerFilePath, this.serializers);
         } else {
            throw new IllegalStateException("required fields not set!");
         }
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
