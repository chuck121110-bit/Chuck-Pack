package xaero.hud.minimap.radar;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_638;
import xaero.hud.minimap.radar.category.EntityRadarCategoryManager;
import xaero.hud.minimap.radar.color.RadarColorHelper;
import xaero.hud.minimap.radar.state.RadarState;
import xaero.hud.minimap.radar.state.RadarStateUpdater;

public final class RadarSession {
   private final EntityRadarCategoryManager categoryManager;
   private final RadarState state;
   private final RadarStateUpdater stateUpdater;
   private final RadarColorHelper colorHelper;

   private RadarSession(EntityRadarCategoryManager categoryManager, RadarState state, RadarStateUpdater stateUpdater, RadarColorHelper colorHelper) {
      this.categoryManager = categoryManager;
      this.state = state;
      this.stateUpdater = stateUpdater;
      this.colorHelper = colorHelper;
   }

   public void update(class_638 world, class_1297 renderEntity, class_1657 player) {
      this.stateUpdater.update(world, renderEntity, player);
   }

   public EntityRadarCategoryManager getCategoryManager() {
      return this.categoryManager;
   }

   public RadarState getState() {
      return this.state;
   }

   public RadarStateUpdater getStateUpdater() {
      return this.stateUpdater;
   }

   public RadarColorHelper getColorHelper() {
      return this.colorHelper;
   }

   public static class Builder {
      private EntityRadarCategoryManager categoryManager;

      private Builder() {
      }

      public Builder setDefault() {
         this.setCategoryManager((EntityRadarCategoryManager)null);
         return this;
      }

      public Builder setCategoryManager(EntityRadarCategoryManager categoryManager) {
         this.categoryManager = categoryManager;
         return this;
      }

      public RadarSession build() {
         if (this.categoryManager == null) {
            throw new IllegalStateException();
         } else {
            RadarState state = new RadarState();
            RadarStateUpdater stateUpdater = new RadarStateUpdater(this.categoryManager, state);
            RadarColorHelper colorHelper = new RadarColorHelper();
            return new RadarSession(this.categoryManager, state, stateUpdater, colorHelper);
         }
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
