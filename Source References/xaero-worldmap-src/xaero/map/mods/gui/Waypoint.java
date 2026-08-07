package xaero.map.mods.gui;

import net.minecraft.class_2960;
import net.minecraft.class_310;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.map.animation.SlowingAnimation;

public class Waypoint implements Comparable<Waypoint> {
   private Object original;
   public static final class_2960 minimapTextures = class_2960.method_60655("xaerobetterpvp", "gui/guis.png");
   public static final int white = -1;
   private float destAlpha = 0.0F;
   private float alpha = 0.0F;
   private SlowingAnimation alphaAnim = null;
   private boolean editable;
   private String setName;
   private double dimDiv;
   private int cachedNameLength;

   public Waypoint(Object original, boolean editable, String setName, double dimDiv) {
      this.original = original;
      this.editable = editable;
      this.setName = setName;
      this.dimDiv = dimDiv;
      this.cachedNameLength = class_310.method_1551().field_1772.method_1727(this.getName());
   }

   public String getName() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).getLocalizedName();
   }

   public int compareTo(Waypoint arg0) {
      int z = this.getZ();
      int otherZ = arg0.getZ();
      return z > otherZ ? 1 : (z != otherZ ? -1 : 0);
   }

   public String toString() {
      return this.getName();
   }

   public int getX() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).getX();
   }

   public int getY() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).getY();
   }

   public int getZ() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).getZ();
   }

   public boolean isDisabled() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).isDisabled();
   }

   /** @deprecated */
   @Deprecated
   public void setDisabled(boolean disabled) {
   }

   /** @deprecated */
   @Deprecated
   public int getType() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).getWaypointType();
   }

   public WaypointPurpose getPurpose() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).getPurpose();
   }

   public int getYaw() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).getYaw();
   }

   /** @deprecated */
   @Deprecated
   public void setYaw(int yaw) {
   }

   public boolean isRotation() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).isRotation();
   }

   /** @deprecated */
   @Deprecated
   public void setRotation(boolean rotation) {
   }

   public boolean isEditable() {
      return this.editable;
   }

   public Object getOriginal() {
      return this.original;
   }

   public String getSymbol() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).getInitials();
   }

   /** @deprecated */
   @Deprecated
   public void setTemporary(boolean temporary) {
   }

   /** @deprecated */
   @Deprecated
   public void setGlobal(boolean global) {
   }

   public String getSetName() {
      return this.setName;
   }

   public String getComparisonName() {
      String comparisonName = this.getName().toLowerCase().trim();
      if (comparisonName.startsWith("the ")) {
         comparisonName = comparisonName.substring(4);
      }

      if (comparisonName.startsWith("a ")) {
         comparisonName = comparisonName.substring(2);
      }

      return comparisonName;
   }

   public int getColor() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).getWaypointColor().getHex();
   }

   public boolean isGlobal() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).isGlobal();
   }

   public double getRenderX() {
      int x = this.getX();
      return this.dimDiv == (double)1.0F ? (double)x + (double)0.5F : Math.floor((double)x / this.dimDiv) + (double)0.5F;
   }

   public double getRenderZ() {
      int z = this.getZ();
      return this.dimDiv == (double)1.0F ? (double)z + (double)0.5F : Math.floor((double)z / this.dimDiv) + (double)0.5F;
   }

   public boolean isTemporary() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).isTemporary();
   }

   public float getDestAlpha() {
      return this.destAlpha;
   }

   public void setDestAlpha(float destAlpha) {
      this.destAlpha = destAlpha;
   }

   public SlowingAnimation getAlphaAnim() {
      return this.alphaAnim;
   }

   public void setAlphaAnim(SlowingAnimation alphaAnim) {
      this.alphaAnim = alphaAnim;
   }

   public float getAlpha() {
      return this.alpha;
   }

   public void setAlpha(float alpha) {
      this.alpha = alpha;
   }

   public boolean isyIncluded() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).isYIncluded();
   }

   public int getCachedNameLength() {
      return this.cachedNameLength;
   }

   public class_2960 getThirdPartyOrigin() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).getThirdPartyOrigin();
   }

   public boolean isThirdParty() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).isThirdParty();
   }

   public boolean isThirdPartyDeleted() {
      return ((xaero.common.minimap.waypoints.Waypoint)this.original).isThirdPartyDeleted();
   }
}
