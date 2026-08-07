package xaero.hud.minimap.radar.category.rule;

import java.util.List;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1944;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import xaero.common.XaeroMinimapSession;
import xaero.common.core.XaeroMinimapCore;
import xaero.hud.category.rule.ObjectCategoryHardRule;
import xaero.hud.minimap.radar.category.EntityRadarCategoryConstants;
import xaero.hud.minimap.radar.util.RadarUtils;

public final class EntityRadarCategoryHardRules {
   public static final List<ObjectCategoryHardRule<class_1297, class_1657>> HARD_RULES_LIST;
   public static final Map<String, ObjectCategoryHardRule<class_1297, class_1657>> HARD_RULES;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_NOTHING;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_ANYTHING;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_LIVING;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_PLAYER;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_SAME_TEAM;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_HOSTILE;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_TAMED;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_ITEM;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_FRIENDLY;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_OTHER_TEAMS;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_BABY;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_VANILLA;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_MODDED;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_ABOVE_GROUND;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_BELOW_GROUND;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_MY_GROUND;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_NOT_MY_GROUND;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_LIT;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_UNLIT;
   public static final ObjectCategoryHardRule<class_1297, class_1657> HAS_CUSTOM_NAME;
   public static final ObjectCategoryHardRule<class_1297, class_1657> NO_CUSTOM_NAME;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_TRACKED;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_IN_TEAM;
   public static final ObjectCategoryHardRule<class_1297, class_1657> IS_TEAMLESS;

   static {
      HARD_RULES_LIST = EntityRadarCategoryConstants.LIST_FACTORY.<ObjectCategoryHardRule<class_1297, class_1657>>get();
      HARD_RULES = EntityRadarCategoryConstants.MAP_FACTORY.<String, ObjectCategoryHardRule<class_1297, class_1657>>get();
      IS_NOTHING = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("nothing").setPredicate((e, p) -> false).build(HARD_RULES, HARD_RULES_LIST);
      IS_ANYTHING = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("anything").setPredicate((e, p) -> true).build(HARD_RULES, HARD_RULES_LIST);
      IS_LIVING = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("living").setPredicate((e, p) -> e instanceof class_1309).build(HARD_RULES, HARD_RULES_LIST);
      IS_PLAYER = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("players").setPredicate((e, p) -> e instanceof class_1657).build(HARD_RULES, HARD_RULES_LIST);
      IS_SAME_TEAM = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("same-team").setPredicate((e, p) -> p.method_5781() == e.method_5781()).build(HARD_RULES, HARD_RULES_LIST);
      IS_HOSTILE = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("hostile").setPredicate((e, p) -> RadarUtils.isHostile(e)).build(HARD_RULES, HARD_RULES_LIST);
      IS_TAMED = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("tamed").setPredicate((e, p) -> RadarUtils.isTamed(e, p)).build(HARD_RULES, HARD_RULES_LIST);
      IS_ITEM = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("items").setPredicate((e, p) -> e instanceof class_1542).build(HARD_RULES, HARD_RULES_LIST);
      IS_FRIENDLY = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("friendly").setPredicate((e, p) -> !IS_HOSTILE.isFollowedBy(e, p)).build(HARD_RULES, HARD_RULES_LIST);
      IS_OTHER_TEAMS = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("other-teams").setPredicate((e, p) -> !IS_SAME_TEAM.isFollowedBy(e, p)).build(HARD_RULES, HARD_RULES_LIST);
      IS_BABY = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("baby").setPredicate((e, p) -> e instanceof class_1309 && ((class_1309)e).method_6109()).build(HARD_RULES, HARD_RULES_LIST);
      IS_VANILLA = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("vanilla").setPredicate((e, p) -> {
         class_1299<?> type = e.method_5864();
         class_2960 resourceLocation = type == null ? null : class_1299.method_5890(type);
         return resourceLocation == null ? false : resourceLocation.method_12836().equals("minecraft");
      }).build(HARD_RULES, HARD_RULES_LIST);
      IS_MODDED = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("modded").setPredicate((e, p) -> !IS_VANILLA.isFollowedBy(e, p)).build(HARD_RULES, HARD_RULES_LIST);
      IS_ABOVE_GROUND = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("above-ground").setPredicate((e, p) -> e.method_73183().method_8314(class_1944.field_9284, e.method_24515()) == 15).build(HARD_RULES, HARD_RULES_LIST);
      IS_BELOW_GROUND = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("below-ground").setPredicate((e, p) -> !IS_ABOVE_GROUND.isFollowedBy(e, p)).build(HARD_RULES, HARD_RULES_LIST);
      IS_MY_GROUND = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("my-ground").setPredicate((e, p) -> {
         if (class_310.method_1551().method_1560() == null) {
            return true;
         } else {
            boolean caveMode = XaeroMinimapSession.getCurrentSession().getMinimapProcessor().isCaveModeDisplayed();
            return IS_ABOVE_GROUND.isFollowedBy(e, p) == !caveMode;
         }
      }).build(HARD_RULES, HARD_RULES_LIST);
      IS_NOT_MY_GROUND = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("not-my-ground").setPredicate((e, p) -> !IS_MY_GROUND.isFollowedBy(e, p)).build(HARD_RULES, HARD_RULES_LIST);
      IS_LIT = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("block-lit").setPredicate((e, p) -> e.method_73183().method_8314(class_1944.field_9282, e.method_24515()) > 0).build(HARD_RULES, HARD_RULES_LIST);
      IS_UNLIT = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("block-unlit").setPredicate((e, p) -> !IS_LIT.isFollowedBy(e, p)).build(HARD_RULES, HARD_RULES_LIST);
      HAS_CUSTOM_NAME = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("has-custom-name").setPredicate((e, p) -> e.method_16914()).build(HARD_RULES, HARD_RULES_LIST);
      NO_CUSTOM_NAME = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("no-custom-name").setPredicate((e, p) -> !HAS_CUSTOM_NAME.isFollowedBy(e, p)).build(HARD_RULES, HARD_RULES_LIST);
      IS_TRACKED = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("tracked").setPredicate((e, p) -> !(e instanceof class_1657) ? false : XaeroMinimapCore.modMain.getTrackedPlayerRenderer().getCollector().playerExists(e.method_5667())).build(HARD_RULES, HARD_RULES_LIST);
      IS_IN_TEAM = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("in-a-team").setPredicate((e, p) -> e.method_5781() != null).build(HARD_RULES, HARD_RULES_LIST);
      IS_TEAMLESS = (new ObjectCategoryHardRule.Builder<class_1297, class_1657>()).setName("teamless").setPredicate((e, p) -> !IS_IN_TEAM.isFollowedBy(e, p)).build(HARD_RULES, HARD_RULES_LIST);
   }
}
