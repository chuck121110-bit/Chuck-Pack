package xaero.hud.minimap.info;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.class_2561;
import net.minecraft.class_339;
import xaero.common.gui.GuiInfoDisplayEdit;
import xaero.common.settings.ModSettings;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.common.config.info.config.InfoDisplayConfigData;
import xaero.hud.minimap.common.config.info.config.InfoDisplayManagerConfigData;
import xaero.hud.minimap.info.codec.InfoDisplayStateCodec;
import xaero.hud.minimap.info.render.compile.InfoDisplayOnCompile;
import xaero.hud.minimap.info.widget.InfoDisplayWidgetFactory;
import xaero.lib.common.config.option.ConfigOption;
import xaero.lib.common.config.option.value.io.serialization.ConfigValueIOCodec;

public final class InfoDisplay<T> {
   private final String id;
   private final class_2561 name;
   private final T defaultState;
   private final ConfigValueIOCodec<T> codec;
   private final InfoDisplayWidgetFactory<T> widgetFactory;
   private final InfoDisplayOnCompile<T> compiler;
   private final Function<ModSettings, T> legacyGetter;
   private InfoDisplay<T>.ConfigCache configCache;
   private InfoDisplayManager manager;

   private InfoDisplay(String id, class_2561 name, T defaultState, ConfigValueIOCodec<T> codec, InfoDisplayWidgetFactory<T> widgetFactory, InfoDisplayOnCompile<T> compiler, Function<ModSettings, T> legacyGetter) {
      this.id = id;
      this.name = name;
      this.defaultState = defaultState;
      this.codec = codec;
      this.widgetFactory = widgetFactory;
      this.compiler = compiler;
      this.legacyGetter = legacyGetter;
   }

   public void setManager(InfoDisplayManager manager) {
      if (this.manager != null) {
         throw new IllegalStateException();
      } else {
         this.manager = manager;
      }
   }

   /** @deprecated */
   @Deprecated
   public T getState() {
      return (T)this.getEffectiveState();
   }

   public T getEffectiveState() {
      if (this.manager == null) {
         throw new IllegalStateException("The info display must be added to a manager first!");
      } else {
         this.updateConfigCache();
         return this.configCache.state;
      }
   }

   public int getTextColor() {
      if (this.manager == null) {
         throw new IllegalStateException("The info display must be added to a manager first!");
      } else {
         this.updateConfigCache();
         return this.configCache.textColor;
      }
   }

   public int getBackgroundColor() {
      if (this.manager == null) {
         throw new IllegalStateException("The info display must be added to a manager first!");
      } else {
         this.updateConfigCache();
         return this.configCache.backgroundColor;
      }
   }

   private void updateConfigCache() {
      if (this.configCache == null) {
         this.configCache = new ConfigCache();
      }

   }

   public String getId() {
      return this.id;
   }

   public class_2561 getName() {
      return this.name;
   }

   public T getDefaultState() {
      return this.defaultState;
   }

   public ConfigValueIOCodec<T> getCodec() {
      return this.codec;
   }

   public class_339 createWidget(int x, int y, int w, int h, GuiInfoDisplayEdit.MoveableEntry<T> entry, Runnable onChange, boolean includeNull) {
      return this.widgetFactory.create(x, y, w, h, entry, onChange, includeNull);
   }

   public InfoDisplayOnCompile<T> getCompiler() {
      return this.compiler;
   }

   public void clearStateCache() {
      this.configCache = null;
   }

   public T getLegacyValue(ModSettings legacySettings) {
      return (T)(this.legacyGetter != null && legacySettings != null ? this.legacyGetter.apply(legacySettings) : null);
   }

   private class ConfigCache {
      private static final Function<InfoDisplayConfigData, Integer> BG_COLOR_GETTER = InfoDisplayConfigData::getBackgroundColor;
      private static final Function<InfoDisplayConfigData, Integer> TEXT_COLOR_GETTER = InfoDisplayConfigData::getTextColor;
      private T state = (T)this.fetchStateFromConfig();
      private int backgroundColor = this.fetchBackgroundColorFromConfig();
      private int textColor = this.fetchTextColorFromConfig();

      public ConfigCache() {
      }

      private T fetchStateFromConfig() {
         InfoDisplayManagerConfigData enforcedManagerConfig = InfoDisplay.this.manager.getEnforcedConfig();
         InfoDisplayConfigData enforcedDisplayConfig = enforcedManagerConfig == null ? null : enforcedManagerConfig.get(InfoDisplay.this.id);
         String enforcedStateString = enforcedDisplayConfig == null ? null : enforcedDisplayConfig.getState();
         if (enforcedStateString != null) {
            try {
               return (T)InfoDisplay.this.codec.decode(enforcedStateString, (Path)null, (ConfigOption)null);
            } catch (Throwable t) {
               MinimapLogs.LOGGER.error("Error trying to decode info display state from enforced config: {}", enforcedStateString, t);
            }
         }

         InfoDisplayManagerConfigData localManagerConfig = InfoDisplay.this.manager.getLocalConfig();
         InfoDisplayConfigData localDisplayConfig = localManagerConfig.get(InfoDisplay.this.id);
         String localStateString = localDisplayConfig == null ? null : localDisplayConfig.getState();
         if (localStateString == null) {
            return (T)InfoDisplay.this.getDefaultState();
         } else {
            try {
               return (T)InfoDisplay.this.codec.decode(localStateString, (Path)null, (ConfigOption)null);
            } catch (Throwable t) {
               MinimapLogs.LOGGER.error("Error trying to decode info display state from current config profile: {}", localStateString, t);
               return (T)InfoDisplay.this.getDefaultState();
            }
         }
      }

      private int fetchBackgroundColorFromConfig() {
         return this.fetchColorFromConfig(BG_COLOR_GETTER, -1);
      }

      private int fetchTextColorFromConfig() {
         return this.fetchColorFromConfig(TEXT_COLOR_GETTER, 15);
      }

      private int fetchColorFromConfig(Function<InfoDisplayConfigData, Integer> colorGetter, int defaultColor) {
         InfoDisplayManagerConfigData enforcedManagerConfig = InfoDisplay.this.manager.getEnforcedConfig();
         InfoDisplayConfigData enforcedDisplayConfig = enforcedManagerConfig == null ? null : enforcedManagerConfig.get(InfoDisplay.this.id);
         Integer enforcedValue = enforcedDisplayConfig == null ? null : (Integer)colorGetter.apply(enforcedDisplayConfig);
         if (enforcedValue != null) {
            return enforcedValue;
         } else {
            InfoDisplayManagerConfigData localManagerConfig = InfoDisplay.this.manager.getLocalConfig();
            InfoDisplayConfigData localDisplayConfig = localManagerConfig.get(InfoDisplay.this.id);
            Integer localValue = localDisplayConfig == null ? null : (Integer)colorGetter.apply(localDisplayConfig);
            return localValue == null ? defaultColor : localValue;
         }
      }
   }

   public static final class Builder<T> {
      private String id;
      private class_2561 name;
      private T defaultState;
      private ConfigValueIOCodec<T> codec;
      private InfoDisplayWidgetFactory<T> widgetFactory;
      private InfoDisplayOnCompile<T> compiler;
      private Consumer<InfoDisplay<?>> destination;
      private Function<ModSettings, T> legacyGetter;

      private Builder() {
      }

      public Builder<T> setDefault() {
         this.setId((String)null);
         this.setName((class_2561)null);
         this.setDefaultState((Object)null);
         this.setCodec((InfoDisplayStateCodec)null);
         this.setWidgetFactory((InfoDisplayWidgetFactory)null);
         this.setCompiler((InfoDisplayOnCompile)null);
         this.setDestination((Consumer)null);
         this.setLegacyGetter((Function)null);
         return this;
      }

      public Builder<T> setId(String id) {
         this.id = id;
         return this;
      }

      public Builder<T> setName(class_2561 name) {
         this.name = name;
         return this;
      }

      public Builder<T> setDefaultState(T defaultState) {
         this.defaultState = defaultState;
         return this;
      }

      /** @deprecated */
      @Deprecated
      public Builder<T> setCodec(InfoDisplayStateCodec<T> codec) {
         return this.setCodec(codec);
      }

      public Builder<T> setCodec(ConfigValueIOCodec<T> codec) {
         this.codec = codec;
         return this;
      }

      public Builder<T> setWidgetFactory(InfoDisplayWidgetFactory<T> widgetFactory) {
         this.widgetFactory = widgetFactory;
         return this;
      }

      public Builder<T> setCompiler(InfoDisplayOnCompile<T> compiler) {
         this.compiler = compiler;
         return this;
      }

      public Builder<T> setDestination(Consumer<InfoDisplay<?>> destination) {
         this.destination = destination;
         return this;
      }

      public Builder<T> setLegacyGetter(Function<ModSettings, T> legacyGetter) {
         this.legacyGetter = legacyGetter;
         return this;
      }

      public InfoDisplay<T> build() {
         if (this.id != null && this.name != null && this.defaultState != null && this.codec != null && this.widgetFactory != null && this.compiler != null) {
            InfoDisplay<T> result = new InfoDisplay<T>(this.id, this.name, this.defaultState, this.codec, this.widgetFactory, this.compiler, this.legacyGetter);
            if (this.destination != null) {
               this.destination.accept(result);
            }

            return result;
         } else {
            throw new IllegalStateException();
         }
      }

      public static <T> Builder<T> begin() {
         return (new Builder<T>()).setDefault();
      }
   }
}
