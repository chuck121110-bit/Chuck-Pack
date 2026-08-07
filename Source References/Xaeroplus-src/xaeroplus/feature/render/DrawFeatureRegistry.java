package xaeroplus.feature.render;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.minecraft.class_310;
import xaeroplus.Globals;
import xaeroplus.settings.Settings;
import xaeroplus.util.DrawOrderHelper;

public class DrawFeatureRegistry {
   private final Int2ObjectRBTreeMap<DrawFeature> features = new Int2ObjectRBTreeMap(Comparator.naturalOrder());
   private final List<String> drawOrder = new ArrayList();

   private boolean validFeatureId(String id) {
      return Pattern.compile("[a-zA-Z0-9_-]+").matcher(id).find();
   }

   public synchronized void register(DrawFeature feature) {
      this.assertOnMainThread();
      String id = feature.id();
      if (!this.validFeatureId(id)) {
         throw new IllegalStateException("Invalid feature id " + id + "\nMust be only letters, numbers, or '_' or '-' characters");
      } else {
         this.unregister(id);

         for(int i = 0; i < this.drawOrder.size(); ++i) {
            String entryId = (String)this.drawOrder.get(i);
            if (entryId.equals(id)) {
               this.features.put(i, feature);
               return;
            }
         }

         int insertIndex = this.drawOrder.size();

         for(int i = 0; i < this.drawOrder.size(); ++i) {
            String entryId = (String)this.drawOrder.get(i);
            if (entryId.compareTo(id) > 0) {
               insertIndex = i;
               break;
            }
         }

         this.drawOrder.add(insertIndex, id);
         this.features.put(this.features.isEmpty() ? 0 : this.features.lastIntKey() + 1, feature);
         String serialized = DrawOrderHelper.serialize(this.drawOrder);
         Settings.REGISTRY.drawOrderSetting.setValue(serialized);
      }
   }

   public synchronized void unregister(String id) {
      this.assertOnMainThread();
      ObjectIterator<Int2ObjectMap.Entry<DrawFeature>> it = Int2ObjectMaps.fastIterator(this.features);

      while(it.hasNext()) {
         Int2ObjectMap.Entry<DrawFeature> entry = (Int2ObjectMap.Entry)it.next();
         if (((DrawFeature)entry.getValue()).id().equals(id)) {
            it.remove();
         }
      }

   }

   public synchronized void forEach(Consumer<DrawFeature> action) {
      this.assertOnMainThread();
      ObjectIterator var2 = Int2ObjectMaps.fastIterable(this.features).iterator();

      while(var2.hasNext()) {
         Int2ObjectMap.Entry<DrawFeature> entry = (Int2ObjectMap.Entry)var2.next();
         DrawFeature feature = (DrawFeature)entry.getValue();
         action.accept(feature);
      }

   }

   public synchronized void loadOrder(final String setting) {
      this.assertOnMainThread();
      List<String> featureIds = DrawOrderHelper.load(setting);
      this.drawOrder.clear();
      this.drawOrder.addAll(featureIds);
      ArrayList<DrawFeature> featuresCopy = new ArrayList(featureIds.size());
      featuresCopy.addAll(this.features.values());
      this.features.clear();

      for(int i = 0; i < this.drawOrder.size(); ++i) {
         String entryId = (String)this.drawOrder.get(i);

         for(DrawFeature feature : featuresCopy) {
            if (entryId.equals(feature.id())) {
               this.features.put(i, feature);
               featuresCopy.remove(feature);
               break;
            }
         }
      }

      for(DrawFeature feature : featuresCopy) {
         this.register(feature);
      }

   }

   private void assertOnMainThread() {
      if (Globals.minimapSettingsInitialized && !class_310.method_1551().method_18854()) {
         throw new RuntimeException("Not on main thread!");
      }
   }
}
