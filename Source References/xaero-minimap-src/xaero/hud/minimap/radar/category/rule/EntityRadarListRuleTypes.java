package xaero.hud.minimap.radar.category.rule;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_7922;
import net.minecraft.class_7923;
import xaero.hud.category.rule.ObjectCategoryListRuleType;
import xaero.hud.minimap.radar.category.EntityRadarCategoryConstants;
import xaero.hud.minimap.radar.util.RadarUtils;

public class EntityRadarListRuleTypes {
   public static final List<ObjectCategoryListRuleType<class_1297, class_1657, ?>> TYPE_LIST;
   public static final Map<String, ObjectCategoryListRuleType<class_1297, class_1657, ?>> TYPE_MAP;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, class_1299<?>> ENTITY_TYPE;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, class_1792> ITEM_TYPE;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, String> PLAYER_NAME;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, String> CUSTOM_NAME;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, Boolean> LIVING;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, Boolean> HOSTILE;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, Boolean> TAMED;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, Boolean> SAME_TEAM;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, Boolean> BABY;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, Boolean> VANILLA;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, Boolean> ABOVE_GROUND;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, Boolean> MY_GROUND;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, Boolean> LIT;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, Boolean> HAS_CUSTOM_NAME;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, Boolean> IN_TEAM;
   public static final ObjectCategoryListRuleType<class_1297, class_1657, Boolean> TRACKED;

   static {
      TYPE_LIST = EntityRadarCategoryConstants.LIST_FACTORY.<ObjectCategoryListRuleType<class_1297, class_1657, ?>>get();
      TYPE_MAP = EntityRadarCategoryConstants.MAP_FACTORY.<String, ObjectCategoryListRuleType<class_1297, class_1657, ?>>get();
      ENTITY_TYPE = new ObjectCategoryListRuleType<class_1297, class_1657, class_1299<?>>("entity", (e, p) -> e.method_5864(), () -> class_7923.field_41177, EntityRadarCategoryConstants.getDefaultElementResolver(class_7923.field_41177, (s) -> (class_1299)class_1299.method_5898(s).orElse((Object)null), class_1299::method_5890), EntityRadarCategoryConstants.DEFAULT_LIST_SERIALIZER, EntityRadarCategoryConstants.DEFAULT_LIST_STRING_VALIDATOR_FIXER, EntityRadarCategoryConstants.DEFAULT_LIST_STRING_VALIDATOR, TYPE_LIST, TYPE_MAP);
      BiFunction var10003 = (e, p) -> e instanceof class_1542 ? ((class_1542)e).method_6983().method_7909() : null;
      Supplier var10004 = () -> class_7923.field_41178;
      class_7922 var10005 = class_7923.field_41178;
      Function var10006 = (s) -> (class_1792)class_7923.field_41178.method_17966(class_2960.method_60654(s)).orElse((Object)null);
      class_7922 var10007 = class_7923.field_41178;
      Objects.requireNonNull(var10007);
      ITEM_TYPE = new ObjectCategoryListRuleType<class_1297, class_1657, class_1792>("item", var10003, var10004, EntityRadarCategoryConstants.getDefaultElementResolver(var10005, var10006, var10007::method_10221), (item) -> class_7923.field_41178.method_10221(item).toString(), EntityRadarCategoryConstants.DEFAULT_LIST_STRING_VALIDATOR_FIXER, EntityRadarCategoryConstants.DEFAULT_LIST_STRING_VALIDATOR, TYPE_LIST, TYPE_MAP);
      PLAYER_NAME = new ObjectCategoryListRuleType<class_1297, class_1657, String>("player", (e, p) -> e instanceof class_1657 ? ((class_1657)e).method_7334().name() : null, () -> {
         Object var10000;
         if (class_310.method_1551().method_1562() == null) {
            var10000 = new ArrayList();
         } else {
            Stream var0 = class_310.method_1551().method_1562().method_2880().stream().map((pi) -> pi.method_2966().name());
            Objects.requireNonNull(var0);
            var10000 = var0::iterator;
         }

         return (Iterable)var10000;
      }, (xva$0) -> Lists.newArrayList(new String[]{xva$0}), Function.identity(), EntityRadarCategoryConstants.PLAYER_NAME_VALIDATOR_FIXER, EntityRadarCategoryConstants.PLAYER_NAME_VALIDATOR, TYPE_LIST, TYPE_MAP);
      CUSTOM_NAME = new ObjectCategoryListRuleType<class_1297, class_1657, String>("custom-name", (e, p) -> RadarUtils.getCustomName(e, false), () -> {
         if (class_310.method_1551().field_1687 == null) {
            return Lists.newArrayList(new String[]{"example"});
         } else {
            Iterable<class_1297> entities = class_310.method_1551().field_1687.method_18112();
            if (entities == null) {
               return Lists.newArrayList(new String[]{"example"});
            } else {
               Stream<String> nameStream = StreamSupport.stream(entities.spliterator(), false).map((e) -> RadarUtils.getCustomName(e, true)).filter(Objects::nonNull);
               Iterator<String> iterator = nameStream.iterator();
               return (Iterable)(!iterator.hasNext() ? Lists.newArrayList(new String[]{"example"}) : () -> iterator);
            }
         }
      }, (xva$0) -> Lists.newArrayList(new String[]{xva$0}), Function.identity(), (s) -> s, (s) -> true, TYPE_LIST, TYPE_MAP);
      LIVING = EntityRadarCategoryConstants.createHardRuleBasedPredicateListRuleType(EntityRadarCategoryHardRules.IS_LIVING, TYPE_LIST, TYPE_MAP);
      HOSTILE = EntityRadarCategoryConstants.createHardRuleBasedPredicateListRuleType(EntityRadarCategoryHardRules.IS_HOSTILE, TYPE_LIST, TYPE_MAP);
      TAMED = EntityRadarCategoryConstants.createHardRuleBasedPredicateListRuleType(EntityRadarCategoryHardRules.IS_TAMED, TYPE_LIST, TYPE_MAP);
      SAME_TEAM = EntityRadarCategoryConstants.createHardRuleBasedPredicateListRuleType(EntityRadarCategoryHardRules.IS_SAME_TEAM, TYPE_LIST, TYPE_MAP);
      BABY = EntityRadarCategoryConstants.createHardRuleBasedPredicateListRuleType(EntityRadarCategoryHardRules.IS_BABY, TYPE_LIST, TYPE_MAP);
      VANILLA = EntityRadarCategoryConstants.createHardRuleBasedPredicateListRuleType(EntityRadarCategoryHardRules.IS_VANILLA, TYPE_LIST, TYPE_MAP);
      ABOVE_GROUND = EntityRadarCategoryConstants.createHardRuleBasedPredicateListRuleType(EntityRadarCategoryHardRules.IS_ABOVE_GROUND, TYPE_LIST, TYPE_MAP);
      MY_GROUND = EntityRadarCategoryConstants.createHardRuleBasedPredicateListRuleType(EntityRadarCategoryHardRules.IS_MY_GROUND, TYPE_LIST, TYPE_MAP);
      LIT = EntityRadarCategoryConstants.createHardRuleBasedPredicateListRuleType(EntityRadarCategoryHardRules.IS_LIT, TYPE_LIST, TYPE_MAP);
      HAS_CUSTOM_NAME = EntityRadarCategoryConstants.createHardRuleBasedPredicateListRuleType(EntityRadarCategoryHardRules.HAS_CUSTOM_NAME, TYPE_LIST, TYPE_MAP);
      IN_TEAM = EntityRadarCategoryConstants.createHardRuleBasedPredicateListRuleType(EntityRadarCategoryHardRules.IS_IN_TEAM, TYPE_LIST, TYPE_MAP);
      TRACKED = EntityRadarCategoryConstants.createHardRuleBasedPredicateListRuleType(EntityRadarCategoryHardRules.IS_TRACKED, TYPE_LIST, TYPE_MAP);
   }
}
