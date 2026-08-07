package xaeroplus.settings;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_304;
import xaeroplus.feature.extensions.IXaeroPlusSettingEntry;

public abstract class SettingRegistry {
   private final Map<SettingLocation, ArrayList<XaeroPlusSetting>> settingLocationMap = new EnumMap(SettingLocation.class);
   private final Map<String, XaeroPlusSetting> settingNameMap = new ConcurrentHashMap();
   private final Map<class_304, BooleanSetting> keybindingMap = new ConcurrentHashMap();

   public BooleanSetting register(BooleanSetting setting, SettingLocation settingLocation) {
      this.register0(settingLocation, setting);
      return setting;
   }

   public DoubleSetting register(DoubleSetting setting, SettingLocation settingLocation) {
      this.register0(settingLocation, setting);
      return setting;
   }

   public <E extends Enum<E>> EnumSetting<E> register(EnumSetting<E> setting, SettingLocation settingLocation) {
      this.register0(settingLocation, setting);
      return setting;
   }

   public StringSetting register(StringSetting setting, SettingLocation settingLocation) {
      this.register0(settingLocation, setting);
      return setting;
   }

   private synchronized void register0(SettingLocation settingLocation, XaeroPlusSetting setting) {
      if (this.settingNameMap.containsKey(setting.getSettingName())) {
         throw new RuntimeException("Setting with name '" + setting.getSettingName() + "' already exists");
      } else {
         ArrayList<XaeroPlusSetting> settingList = (ArrayList)this.settingLocationMap.getOrDefault(settingLocation, new ArrayList());
         settingList.add(setting);
         this.settingLocationMap.put(settingLocation, settingList);
         this.settingNameMap.put(setting.getSettingName(), setting);
         if (setting instanceof BooleanSetting) {
            BooleanSetting booleanSetting = (BooleanSetting)setting;
            class_304 kb = booleanSetting.getKeyBinding();
            if (kb != null) {
               this.keybindingMap.put(kb, booleanSetting);
            }
         }

      }
   }

   public XaeroPlusSetting getSettingByName(String name) {
      return (XaeroPlusSetting)this.settingNameMap.get(name);
   }

   public Set<class_304> getKeybindings() {
      return this.keybindingMap.keySet();
   }

   public BooleanSetting getKeybindingSetting(class_304 keyMapping) {
      return (BooleanSetting)this.keybindingMap.get(keyMapping);
   }

   public List<XaeroPlusSetting> getAllSettings() {
      return new ArrayList(this.settingNameMap.values());
   }

   public synchronized IXaeroPlusSettingEntry[] getXaeroSettingEntries(SettingLocation settingLocation) {
      ArrayList<XaeroPlusSetting> settingList = (ArrayList)this.settingLocationMap.get(settingLocation);
      if (settingList != null) {
         List<IXaeroPlusSettingEntry> entries = new ArrayList(settingList.size());

         for(int i = 0; i < settingList.size(); ++i) {
            XaeroPlusSetting xaeroPlusSetting = (XaeroPlusSetting)settingList.get(i);
            IXaeroPlusSettingEntry entry = xaeroPlusSetting.toXaeroSettingEntry();
            if (entry != null) {
               entries.add(entry);
            }
         }

         return (IXaeroPlusSettingEntry[])entries.toArray(new IXaeroPlusSettingEntry[0]);
      } else {
         return new IXaeroPlusSettingEntry[0];
      }
   }
}
