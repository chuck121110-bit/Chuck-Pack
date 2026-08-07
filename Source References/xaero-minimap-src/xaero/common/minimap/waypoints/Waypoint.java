package xaero.common.minimap.waypoints;

import net.minecraft.class_1074;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import org.joml.Vector3fc;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.WaypointRenderInfo;
import xaero.hud.minimap.waypoint.WaypointVisibilityType;

public class Waypoint extends WaypointRenderInfo implements Comparable<Waypoint> {
   public static final int ONEOFF_DESTINATION_SAFE_FOR = 5000;
   public static final int ONEOFF_DESTINATION_REMOVE_DISTANCE = 4;
   public static class_243 RENDER_SORTING_POS = new class_243((double)0.0F, (double)0.0F, (double)0.0F);
   private int x;
   private int y;
   private int z;
   private String name;
   protected WaypointPurpose purpose;
   private boolean rotation;
   private int yaw;
   private boolean temporary;
   private boolean yIncluded;
   private final long createdAt;
   private class_2960 thirdPartyOrigin;
   private WaypointRenderInfo thirdPartyRenderOverride;

   /** @deprecated */
   @Deprecated
   public Waypoint(int x, int y, int z, String name, String initials, int color) {
      this(x, y, z, name, initials, color, 0, false);
   }

   /** @deprecated */
   @Deprecated
   public Waypoint(int x, int y, int z, String name, String initials, int color, int type) {
      this(x, y, z, name, initials, color, type, false);
   }

   /** @deprecated */
   @Deprecated
   public Waypoint(int x, int y, int z, String name, String initials, int color, int type, boolean temp) {
      this(x, y, z, name, initials, color, type, temp, true);
   }

   /** @deprecated */
   @Deprecated
   public Waypoint(int x, int y, int z, String name, String initials, int color, int type, boolean temp, boolean yIncluded) {
      this(x, y, z, name, initials, WaypointColor.fromIndex(color), WaypointPurpose.values()[type], temp, yIncluded);
      this.actualColor = color;
   }

   public Waypoint(int x, int y, int z, String name, String initials, WaypointColor color) {
      this(x, y, z, name, initials, color, WaypointPurpose.NORMAL, false);
   }

   public Waypoint(int x, int y, int z, String name, String initials, WaypointColor color, WaypointPurpose purpose) {
      this(x, y, z, name, initials, color, purpose, false);
   }

   public Waypoint(int x, int y, int z, String name, String initials, WaypointColor color, WaypointPurpose purpose, boolean temp) {
      this(x, y, z, name, initials, color, purpose, temp, true);
   }

   public Waypoint(int x, int y, int z, String name, String initials, WaypointColor color, WaypointPurpose purpose, boolean temp, boolean yIncluded) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.initials = initials;
      this.color = color;
      this.purpose = purpose;
      this.name = name;
      this.temporary = temp;
      this.visibility = WaypointVisibilityType.LOCAL;
      if (this.purpose.isDeath()) {
         this.visibility = WaypointVisibilityType.GLOBAL;
      }

      this.yIncluded = yIncluded;
      this.createdAt = System.currentTimeMillis();
      this.actualColor = color.ordinal();
      this.disabled = false;
   }

   public int getX() {
      return this.x;
   }

   public void setX(int x) {
      this.x = x;
   }

   public int getX(double dimDiv) {
      return dimDiv == (double)1.0F ? this.x : (int)Math.floor((double)this.x / dimDiv);
   }

   public int getY() {
      return this.y;
   }

   public void setY(int y) {
      this.y = y;
   }

   public int getZ() {
      return this.z;
   }

   public void setZ(int z) {
      this.z = z;
   }

   public int getZ(double dimDiv) {
      return dimDiv == (double)1.0F ? this.z : (int)Math.floor((double)this.z / dimDiv);
   }

   public String getName() {
      return this.name;
   }

   public String getLocalizedName() {
      return class_1074.method_4662(this.name, new Object[0]);
   }

   public String getInitials() {
      return this.thirdPartyRenderOverride != null && this.thirdPartyRenderOverride.getInitials() != null ? this.thirdPartyRenderOverride.getInitials() : super.getInitials();
   }

   public String getNameSafe(String replacement) {
      return this.getName().replace(":", replacement);
   }

   public void setName(String name) {
      this.name = name;
   }

   /** @deprecated */
   @Deprecated
   public String getSymbol() {
      return this.getInitials();
   }

   /** @deprecated */
   @Deprecated
   public void setSymbol(String symbol) {
      this.setInitials(symbol);
   }

   /** @deprecated */
   @Deprecated
   public String getSymbolSafe(String replacement) {
      return this.getInitialsSafe(replacement);
   }

   /** @deprecated */
   @Deprecated
   public int getColor() {
      return this.getWaypointColor().ordinal();
   }

   /** @deprecated */
   @Deprecated
   public int getActualColor() {
      return this.actualColor;
   }

   /** @deprecated */
   @Deprecated
   public void setColor(int c) {
      this.setWaypointColor(WaypointColor.fromIndex(c));
      this.actualColor = c;
   }

   public WaypointColor getWaypointColor() {
      return this.thirdPartyRenderOverride != null && this.thirdPartyRenderOverride.getWaypointColor() != null ? this.thirdPartyRenderOverride.getWaypointColor() : super.getWaypointColor();
   }

   public boolean isGlobal() {
      return this.getVisibility().isGlobal();
   }

   /** @deprecated */
   @Deprecated
   public int getVisibilityType() {
      return this.getVisibility().ordinal();
   }

   public WaypointVisibilityType getVisibility() {
      return this.thirdPartyRenderOverride != null && this.thirdPartyRenderOverride.getVisibility() != null ? this.thirdPartyRenderOverride.getVisibility() : super.getVisibility();
   }

   /** @deprecated */
   @Deprecated
   public void setVisibilityType(int visibility) {
      this.setVisibility(WaypointVisibilityType.values()[visibility]);
   }

   public void setVisibility(WaypointVisibilityType visibility) {
      if (this.purpose != WaypointPurpose.DEATH) {
         super.setVisibility(visibility);
      }

   }

   public boolean isDisabled() {
      return this.getDisabled();
   }

   public Boolean getDisabled() {
      return this.thirdPartyRenderOverride != null && this.thirdPartyRenderOverride.getDisabled() != null ? this.thirdPartyRenderOverride.getDisabled() : super.getDisabled();
   }

   public void setDisabled(boolean b) {
      this.setDisabled(b);
   }

   public void setDisabled(Boolean disabled) {
      this.temporary = false;
      super.setDisabled(disabled);
   }

   /** @deprecated */
   @Deprecated
   public int getWaypointType() {
      return this.purpose.ordinal();
   }

   /** @deprecated */
   @Deprecated
   public void setType(int type) {
      this.setPurpose(WaypointPurpose.values()[type]);
   }

   public WaypointPurpose getPurpose() {
      return this.purpose;
   }

   public void setPurpose(WaypointPurpose purpose) {
      this.purpose = purpose;
      if (this.purpose.isDeath()) {
         this.visibility = WaypointVisibilityType.GLOBAL;
      }

   }

   public boolean isRotation() {
      return this.rotation;
   }

   public void setRotation(boolean rotation) {
      this.rotation = rotation;
   }

   public int getYaw() {
      return this.yaw;
   }

   public void setYaw(int yaw) {
      this.yaw = yaw;
   }

   public boolean isTemporary() {
      return this.temporary;
   }

   public void setTemporary(boolean temporary) {
      this.temporary = temporary;
      this.disabled = false;
   }

   public boolean isYIncluded() {
      return this.yIncluded;
   }

   public void setYIncluded(boolean yIncluded) {
      this.yIncluded = yIncluded;
   }

   public long getCreatedAt() {
      return this.createdAt;
   }

   /** @deprecated */
   @Deprecated
   public boolean isOneoffDestination() {
      return this.getPurpose().isDestination();
   }

   /** @deprecated */
   @Deprecated
   public void setOneoffDestination(boolean oneoffDestination) {
      if (oneoffDestination) {
         if (this.purpose == WaypointPurpose.NORMAL) {
            this.purpose = WaypointPurpose.DESTINATION;
         }

      } else if (this.purpose == WaypointPurpose.DESTINATION) {
         this.purpose = WaypointPurpose.NORMAL;
      }
   }

   public boolean isDestination() {
      return this.getPurpose().isDestination();
   }

   public double getDistanceSq(double x, double y, double z) {
      double offX = (double)this.x - x;
      double offY = this.yIncluded ? (double)this.y - y : (double)0.0F;
      double offZ = (double)this.z - z;
      return offX * offX + offY * offY + offZ * offZ;
   }

   public static String getStringFromStringSafe(String stringSafe, String replacement) {
      return stringSafe.replace(replacement, ":");
   }

   /** @deprecated */
   @Deprecated
   public boolean isServerWaypoint() {
      return this.isThirdParty();
   }

   public String getComparisonName() {
      String comparisonName = this.getLocalizedName().toLowerCase().trim();
      if (comparisonName.startsWith("the ")) {
         return comparisonName.substring(4);
      } else {
         return comparisonName.startsWith("a ") ? comparisonName.substring(2) : comparisonName;
      }
   }

   public double getComparisonDistance(class_4184 camera, double dimDiv) {
      class_243 cameraPos = camera.method_71156();
      double offX = (double)this.getX(dimDiv) - cameraPos.field_1352;
      double offY = !this.isYIncluded() ? (double)0.0F : (double)this.getY() - cameraPos.field_1351;
      double offZ = (double)this.getZ(dimDiv) - cameraPos.field_1350;
      return offX * offX + offY * offY + offZ * offZ;
   }

   public double getComparisonAngleCos(class_4184 camera, double dimDiv) {
      Vector3fc lookVector = camera.method_19335();
      class_243 cameraPos = camera.method_71156();
      double offX = (double)this.getX(dimDiv) - cameraPos.field_1352;
      double offY = !this.isYIncluded() ? (double)0.0F : (double)this.getY() - cameraPos.field_1351;
      double offZ = (double)this.getZ(dimDiv) - cameraPos.field_1350;
      double distance = Math.sqrt(offX * offX + offY * offY + offZ * offZ);
      return (offX * (double)lookVector.x() + offY * (double)lookVector.y() + offZ * (double)lookVector.z()) / distance;
   }

   private double getRenderSortingDistanceSquared() {
      double fromCameraX = (double)this.x - RENDER_SORTING_POS.field_1352;
      double fromCameraY = this.yIncluded ? (double)this.y - RENDER_SORTING_POS.field_1351 : (double)0.0F;
      double fromCameraZ = (double)this.z - RENDER_SORTING_POS.field_1350;
      return fromCameraX * fromCameraX + fromCameraY * fromCameraY + fromCameraZ * fromCameraZ;
   }

   public int compareTo(Waypoint other) {
      boolean isDeath = this.purpose.isDeath();
      if (isDeath != other.purpose.isDeath()) {
         return isDeath ? 1 : -1;
      } else {
         double rsds = this.getRenderSortingDistanceSquared();
         double otherRsds = other.getRenderSortingDistanceSquared();
         return rsds > otherRsds ? -1 : (rsds == otherRsds ? 0 : 1);
      }
   }

   public void setThirdPartyOrigin(class_2960 thirdPartyOrigin) {
      this.thirdPartyOrigin = thirdPartyOrigin;
      this.thirdPartyRenderOverride = new WaypointRenderInfo();
   }

   public class_2960 getThirdPartyOrigin() {
      return this.thirdPartyOrigin;
   }

   public boolean isThirdParty() {
      return this.thirdPartyOrigin != null;
   }

   public boolean isEffectivelyDeleted() {
      return this.isTemporary() || this.isThirdPartyDeleted();
   }

   public WaypointRenderInfo getThirdPartyRenderOverride() {
      return this.thirdPartyRenderOverride;
   }

   public void setThirdPartyRenderOverride(WaypointRenderInfo thirdPartyRenderOverride) {
      this.thirdPartyRenderOverride = thirdPartyRenderOverride;
   }

   public boolean isThirdPartyDeleted() {
      return this.thirdPartyRenderOverride != null && this.thirdPartyRenderOverride.isThirdPartyDeleted();
   }

   public void setThirdPartyDeleted(boolean thirdPartyDeleted) {
      if (this.thirdPartyRenderOverride == null) {
         throw new IllegalStateException();
      } else {
         this.thirdPartyRenderOverride.setThirdPartyDeleted(thirdPartyDeleted);
      }
   }

   public WaypointRenderInfo getRenderInfoEditDest() {
      return (WaypointRenderInfo)(this.isThirdParty() ? this.getThirdPartyRenderOverride() : this);
   }
}
