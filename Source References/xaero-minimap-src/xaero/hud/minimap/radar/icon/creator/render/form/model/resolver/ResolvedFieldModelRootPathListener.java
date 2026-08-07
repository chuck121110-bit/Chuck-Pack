package xaero.hud.minimap.radar.icon.creator.render.form.model.resolver;

import java.lang.reflect.Field;
import xaero.hud.minimap.MinimapLogs;

public class ResolvedFieldModelRootPathListener implements RadarIconModelFieldResolver.Listener {
   private Object resolvedObject;
   private boolean stop;
   private boolean failed;

   public void prepare() {
      this.resolvedObject = null;
      this.stop = false;
      this.failed = false;
   }

   public boolean isFieldAllowed(Field f) {
      return true;
   }

   public boolean shouldStop() {
      return this.stop;
   }

   public void onFieldResolved(Object[] resolved, String matchedFilterElement) {
      this.stop = true;
      if (resolved.length != 1) {
         MinimapLogs.LOGGER.warn("Only exactly 1 object can be referenced with a model root path step but {} were referenced with {}", resolved.length, matchedFilterElement);
         this.failed = true;
      } else {
         this.resolvedObject = resolved[0];
      }
   }

   public Object getCurrentNode() {
      return this.resolvedObject;
   }

   public boolean failed() {
      return this.failed;
   }
}
