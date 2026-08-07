package xaero.hud.minimap.radar.category;

import net.minecraft.class_1299;
import xaero.common.settings.ModSettings;
import xaero.hud.minimap.radar.category.rule.EntityRadarCategoryHardRules;
import xaero.hud.minimap.radar.category.rule.EntityRadarListRuleTypes;
import xaero.hud.minimap.radar.category.setting.EntityRadarCategorySettings;

public final class EntityRadarDefaultCategories {
   private final boolean forServer;

   public EntityRadarDefaultCategories(boolean forServer) {
      this.forServer = forServer;
   }

   public EntityRadarCategory setupDefault(ModSettings settings) {
      EntityRadarBackwardsCompatibilityConfig compatibilityConfig = settings.getEntityRadarBackwardsCompatibilityConfig();
      EntityRadarCategory.Builder builder = (EntityRadarCategory.Builder)((EntityRadarCategory.Builder)((EntityRadarCategory.Builder)EntityRadarCategory.Builder.begin().setName("gui.xaero_entity_category_root")).setBaseRule(EntityRadarCategoryHardRules.IS_ANYTHING)).setProtection(true);
      if (!this.forServer && (!settings.foundOldRadarSettings() || !compatibilityConfig.itemFramesOnRadar)) {
         builder.getExcludeListBuilder(EntityRadarListRuleTypes.ENTITY_TYPE).addListElement(class_1299.method_5890(class_1299.field_6043).toString());
         builder.getExcludeListBuilder(EntityRadarListRuleTypes.ENTITY_TYPE).addListElement(class_1299.method_5890(class_1299.field_28401).toString());
      }

      if (!this.forServer && settings.foundOldRadarSettings()) {
         builder.setSettingValue(EntityRadarCategorySettings.ENTITY_NUMBER, (double)compatibilityConfig.entityAmount * (double)100.0F);
         builder.setSettingValue(EntityRadarCategorySettings.DOT_SIZE, (double)compatibilityConfig.dotsSize);
         builder.setSettingValue(EntityRadarCategorySettings.ICON_SCALE, compatibilityConfig.headsScale);
         builder.setSettingValue(EntityRadarCategorySettings.HEIGHT_FADE, compatibilityConfig.showEntityHeight);
         builder.setSettingValue(EntityRadarCategorySettings.HEIGHT_LIMIT, (double)compatibilityConfig.heightLimit);
         builder.setSettingValue(EntityRadarCategorySettings.ALWAYS_NAMETAGS, compatibilityConfig.alwaysEntityNametags);
         builder.setSettingValue(EntityRadarCategorySettings.ICON_NAME_FALLBACK, compatibilityConfig.displayNameWhenIconFails);
      }

      EntityRadarCategory.Builder livingBuilder = (EntityRadarCategory.Builder)((EntityRadarCategory.Builder)((EntityRadarCategory.Builder)EntityRadarCategory.Builder.begin().setName("gui.xaero_entity_category_living")).setBaseRule(EntityRadarCategoryHardRules.IS_LIVING)).setProtection(true);
      if (!this.forServer) {
         livingBuilder.setSettingValue(EntityRadarCategorySettings.RENDER_ORDER, (double)2.0F);
         livingBuilder.setSettingValue(EntityRadarCategorySettings.COLOR, (double)14.0F);
      }

      livingBuilder.getExcludeListBuilder(EntityRadarListRuleTypes.ENTITY_TYPE).addListElement(class_1299.method_5890(class_1299.field_6131).toString());
      EntityRadarCategory.Builder hostileBuilder = (EntityRadarCategory.Builder)((EntityRadarCategory.Builder)((EntityRadarCategory.Builder)EntityRadarCategory.Builder.begin().setName("gui.xaero_entity_category_hostile")).setBaseRule(EntityRadarCategoryHardRules.IS_HOSTILE)).setProtection(true);
      if (!this.forServer) {
         hostileBuilder.setSettingValue(EntityRadarCategorySettings.RENDER_ORDER, (double)3.0F);
      }

      if (!this.forServer && settings.foundOldRadarSettings()) {
         if (!compatibilityConfig.showHostile) {
            hostileBuilder.setSettingValue(EntityRadarCategorySettings.DISPLAYED, false);
         }

         if (compatibilityConfig.hostileColor != 14) {
            hostileBuilder.setSettingValue(EntityRadarCategorySettings.COLOR, (double)compatibilityConfig.hostileColor);
         }

         if (compatibilityConfig.hostileIcons != 1) {
            hostileBuilder.setSettingValue(EntityRadarCategorySettings.ICONS, (double)compatibilityConfig.hostileIcons);
         }

         if (compatibilityConfig.hostileMobNames != 0) {
            hostileBuilder.setSettingValue(EntityRadarCategorySettings.NAMES, (double)compatibilityConfig.hostileMobNames);
         }
      }

      EntityRadarCategory.Builder friendlyBuilder = (EntityRadarCategory.Builder)((EntityRadarCategory.Builder)((EntityRadarCategory.Builder)EntityRadarCategory.Builder.begin().setName("gui.xaero_entity_category_friendly")).setBaseRule(EntityRadarCategoryHardRules.IS_ANYTHING)).setProtection(true);
      if (!this.forServer && settings.foundOldRadarSettings()) {
         if (!compatibilityConfig.showMobs) {
            friendlyBuilder.setSettingValue(EntityRadarCategorySettings.DISPLAYED, false);
         }

         if (compatibilityConfig.mobsColor != 14) {
            friendlyBuilder.setSettingValue(EntityRadarCategorySettings.COLOR, (double)compatibilityConfig.mobsColor);
         }

         if (compatibilityConfig.mobIcons != 1) {
            friendlyBuilder.setSettingValue(EntityRadarCategorySettings.ICONS, (double)compatibilityConfig.mobIcons);
         }

         if (compatibilityConfig.friendlyMobNames != 0) {
            friendlyBuilder.setSettingValue(EntityRadarCategorySettings.NAMES, (double)compatibilityConfig.friendlyMobNames);
         }
      }

      EntityRadarCategory.Builder playersBuilder = (EntityRadarCategory.Builder)((EntityRadarCategory.Builder)((EntityRadarCategory.Builder)EntityRadarCategory.Builder.begin().setName("gui.xaero_entity_category_players")).setBaseRule(EntityRadarCategoryHardRules.IS_PLAYER)).setProtection(true);
      if (!this.forServer) {
         playersBuilder.setSettingValue(EntityRadarCategorySettings.RENDER_ORDER, (double)6.0F);
         playersBuilder.setSettingValue(EntityRadarCategorySettings.COLOR, (double)15.0F);
         int lastHeightLimitIndex = EntityRadarCategorySettings.HEIGHT_LIMIT.getUiLastOption();
         playersBuilder.setSettingValue(EntityRadarCategorySettings.HEIGHT_LIMIT, (Double)EntityRadarCategorySettings.HEIGHT_LIMIT.getIndexReader().apply(lastHeightLimitIndex));
      }

      if (!this.forServer && settings.foundOldRadarSettings()) {
         if (!compatibilityConfig.showPlayers) {
            playersBuilder.setSettingValue(EntityRadarCategorySettings.DISPLAYED, false);
         }

         if (compatibilityConfig.playersColor != 14) {
            playersBuilder.setSettingValue(EntityRadarCategorySettings.COLOR, (double)compatibilityConfig.playersColor);
         }

         if (compatibilityConfig.playerIcons != 1) {
            playersBuilder.setSettingValue(EntityRadarCategorySettings.ICONS, (double)compatibilityConfig.playerIcons);
         }

         if (compatibilityConfig.playerNames != 0) {
            playersBuilder.setSettingValue(EntityRadarCategorySettings.NAMES, (double)compatibilityConfig.playerNames);
         }
      }

      EntityRadarCategory.Builder friendsBuilder = (EntityRadarCategory.Builder)((EntityRadarCategory.Builder)((EntityRadarCategory.Builder)EntityRadarCategory.Builder.begin().setName("gui.xaero_entity_category_friend")).setBaseRule(EntityRadarCategoryHardRules.IS_NOTHING)).setProtection(true);
      EntityRadarCategory.Builder playersTrackedBuilder = (EntityRadarCategory.Builder)((EntityRadarCategory.Builder)((EntityRadarCategory.Builder)EntityRadarCategory.Builder.begin().setName("gui.xaero_entity_category_tracked")).setBaseRule(EntityRadarCategoryHardRules.IS_TRACKED)).setProtection(true);
      if (!this.forServer) {
         playersTrackedBuilder.setSettingValue(EntityRadarCategorySettings.ICONS, (double)2.0F);
      }

      EntityRadarCategory.Builder playersTeamBuilder = (EntityRadarCategory.Builder)((EntityRadarCategory.Builder)((EntityRadarCategory.Builder)EntityRadarCategory.Builder.begin().setName("gui.xaero_entity_category_same_team")).setBaseRule(EntityRadarCategoryHardRules.IS_SAME_TEAM)).setProtection(true);
      EntityRadarCategory.Builder playersOtherTeamsBuilder = (EntityRadarCategory.Builder)((EntityRadarCategory.Builder)((EntityRadarCategory.Builder)EntityRadarCategory.Builder.begin().setName("gui.xaero_entity_category_other_teams")).setBaseRule(EntityRadarCategoryHardRules.IS_ANYTHING)).setProtection(true);
      if (!this.forServer) {
         playersOtherTeamsBuilder.setSettingValue(EntityRadarCategorySettings.RENDER_ORDER, (double)7.0F);
      }

      if (!this.forServer && settings.foundOldRadarSettings()) {
         if (!compatibilityConfig.showOtherTeam) {
            playersOtherTeamsBuilder.setSettingValue(EntityRadarCategorySettings.DISPLAYED, false);
         }

         if (compatibilityConfig.otherTeamColor != -1) {
            playersOtherTeamsBuilder.setSettingValue(EntityRadarCategorySettings.COLOR, (double)compatibilityConfig.otherTeamColor);
         }

         if (compatibilityConfig.otherTeamsNames != 3) {
            playersOtherTeamsBuilder.setSettingValue(EntityRadarCategorySettings.NAMES, (double)compatibilityConfig.otherTeamsNames);
         }
      }

      EntityRadarCategory.Builder tamedHostileBuilder = (EntityRadarCategory.Builder)((EntityRadarCategory.Builder)((EntityRadarCategory.Builder)EntityRadarCategory.Builder.begin().setName("gui.xaero_entity_category_hostile_tamed")).setBaseRule(EntityRadarCategoryHardRules.IS_TAMED)).setProtection(true);
      if (!this.forServer) {
         tamedHostileBuilder.setSettingValue(EntityRadarCategorySettings.RENDER_ORDER, (double)5.0F);
      }

      EntityRadarCategory.Builder tamedFriendlyBuilder = (EntityRadarCategory.Builder)((EntityRadarCategory.Builder)((EntityRadarCategory.Builder)EntityRadarCategory.Builder.begin().setName("gui.xaero_entity_category_friendly_tamed")).setBaseRule(EntityRadarCategoryHardRules.IS_TAMED)).setProtection(true);
      if (!this.forServer) {
         tamedFriendlyBuilder.setSettingValue(EntityRadarCategorySettings.RENDER_ORDER, (double)4.0F);
      }

      if (!this.forServer && settings.foundOldRadarSettings()) {
         if (!compatibilityConfig.showTamed) {
            tamedFriendlyBuilder.setSettingValue(EntityRadarCategorySettings.DISPLAYED, false);
            tamedHostileBuilder.setSettingValue(EntityRadarCategorySettings.DISPLAYED, false);
         }

         if (compatibilityConfig.tamedMobsColor != -1) {
            tamedFriendlyBuilder.setSettingValue(EntityRadarCategorySettings.COLOR, (double)compatibilityConfig.tamedMobsColor);
            tamedHostileBuilder.setSettingValue(EntityRadarCategorySettings.COLOR, (double)compatibilityConfig.tamedMobsColor);
         }

         if (compatibilityConfig.tamedIcons != 3) {
            tamedFriendlyBuilder.setSettingValue(EntityRadarCategorySettings.ICONS, (double)compatibilityConfig.tamedIcons);
            tamedHostileBuilder.setSettingValue(EntityRadarCategorySettings.ICONS, (double)compatibilityConfig.tamedIcons);
         }

         if (compatibilityConfig.tamedMobNames != 3) {
            tamedFriendlyBuilder.setSettingValue(EntityRadarCategorySettings.NAMES, (double)compatibilityConfig.tamedMobNames);
            tamedHostileBuilder.setSettingValue(EntityRadarCategorySettings.NAMES, (double)compatibilityConfig.tamedMobNames);
         }
      }

      EntityRadarCategory.Builder itemsBuilder = (EntityRadarCategory.Builder)((EntityRadarCategory.Builder)((EntityRadarCategory.Builder)EntityRadarCategory.Builder.begin().setName("gui.xaero_entity_category_items")).setBaseRule(EntityRadarCategoryHardRules.IS_ITEM)).setProtection(true);
      if (!this.forServer) {
         itemsBuilder.setSettingValue(EntityRadarCategorySettings.RENDER_ORDER, (double)1.0F);
         itemsBuilder.setSettingValue(EntityRadarCategorySettings.COLOR, (double)12.0F);
      }

      if (!this.forServer && settings.foundOldRadarSettings()) {
         if (!compatibilityConfig.showItems) {
            itemsBuilder.setSettingValue(EntityRadarCategorySettings.DISPLAYED, false);
         }

         itemsBuilder.setSettingValue(EntityRadarCategorySettings.COLOR, (double)compatibilityConfig.itemsColor);
         if (compatibilityConfig.itemNames != 0) {
            itemsBuilder.setSettingValue(EntityRadarCategorySettings.NAMES, (double)compatibilityConfig.itemNames);
         }
      }

      EntityRadarCategory.Builder otherBuilder = (EntityRadarCategory.Builder)((EntityRadarCategory.Builder)((EntityRadarCategory.Builder)EntityRadarCategory.Builder.begin().setName("gui.xaero_entity_category_other_entities")).setBaseRule(EntityRadarCategoryHardRules.IS_ANYTHING)).setProtection(true);
      if (!this.forServer) {
         otherBuilder.setSettingValue(EntityRadarCategorySettings.COLOR, (double)5.0F);
      }

      if (!this.forServer && settings.foundOldRadarSettings()) {
         if (!compatibilityConfig.showOther) {
            otherBuilder.setSettingValue(EntityRadarCategorySettings.DISPLAYED, false);
         }

         otherBuilder.setSettingValue(EntityRadarCategorySettings.COLOR, (double)compatibilityConfig.otherColor);
         if (compatibilityConfig.otherNames != 0) {
            otherBuilder.setSettingValue(EntityRadarCategorySettings.NAMES, (double)compatibilityConfig.otherNames);
         }
      }

      builder.addSubCategoryBuilder(livingBuilder);
      builder.addSubCategoryBuilder(itemsBuilder);
      builder.addSubCategoryBuilder(otherBuilder);
      livingBuilder.addSubCategoryBuilder(playersBuilder);
      livingBuilder.addSubCategoryBuilder(hostileBuilder);
      livingBuilder.addSubCategoryBuilder(friendlyBuilder);
      hostileBuilder.addSubCategoryBuilder(tamedHostileBuilder);
      friendlyBuilder.addSubCategoryBuilder(tamedFriendlyBuilder);
      playersBuilder.addSubCategoryBuilder(friendsBuilder);
      playersBuilder.addSubCategoryBuilder(playersTrackedBuilder);
      playersBuilder.addSubCategoryBuilder(playersTeamBuilder);
      playersBuilder.addSubCategoryBuilder(playersOtherTeamsBuilder);
      EntityRadarCategory root = (EntityRadarCategory)builder.build();
      return root;
   }

   public static final class Builder {
      private boolean forServer;

      private Builder() {
      }

      public Builder setDefault() {
         this.setForServer(false);
         return this;
      }

      public Builder setForServer(boolean forServer) {
         this.forServer = forServer;
         return this;
      }

      public EntityRadarDefaultCategories build() {
         return new EntityRadarDefaultCategories(this.forServer);
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
