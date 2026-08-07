package xaero.hud.minimap.radar.render.element;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_2561;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import xaero.common.HudMod;
import xaero.common.graphics.CustomRenderTypes;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRenderer;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.common.icon.XaeroIcon;
import xaero.common.minimap.render.MinimapRendererHelper;
import xaero.common.misc.Misc;
import xaero.common.settings.ModSettings;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.config.primary.option.MinimapPrimaryClientConfigOptions;
import xaero.hud.minimap.controls.key.MinimapKeyMappings;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderer;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.radar.RadarSession;
import xaero.hud.minimap.radar.category.EntityRadarCategory;
import xaero.hud.minimap.radar.category.EntityRadarCategoryManager;
import xaero.hud.minimap.radar.category.setting.EntityRadarCategorySettings;
import xaero.hud.minimap.radar.color.RadarColor;
import xaero.hud.minimap.radar.icon.RadarIconManager;
import xaero.hud.minimap.radar.state.RadarList;
import xaero.hud.minimap.radar.util.RadarUtils;
import xaero.hud.render.util.MultiTextureRenderUtil;
import xaero.hud.render.util.RenderBufferUtil;
import xaero.lib.XaeroLib;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.graphics.XaeroBufferProvider;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.single.SingleConfigManager;

public final class RadarRenderer extends MinimapElementRenderer<class_1297, RadarRenderContext> {
   private final RadarIconManager radarIconManager;
   private final Minimap minimap;
   private RadarSession radarSession;
   private EntityRadarCategoryManager categoryManager;
   private RadarList previousList;
   private double maxDistanceSquared;
   private double labelScale;
   private boolean smoothDots;
   private boolean debugEntityIcons;
   private boolean debugEntityVariantIds;
   private int dotsStyle;
   private int heightLimit;
   private boolean heightBasedFade;
   private int startFadingAt;
   private boolean displayNameWhenIconFails;
   private boolean alwaysNameTags;
   private RadarColor radarColor;
   private RadarColor fallbackColor;
   private int displayY;
   private int nameSettingForCategory;
   private boolean namesForCategory;
   private boolean name;
   private boolean iconsAllowed;
   private boolean labelsAllowed;
   private class_1921 dotsRenderType;
   private XaeroBufferProvider minimapBufferSource;
   private class_4588 dotsBufferBuilder;
   private class_4588 labelBgBuilder;
   private MultiTextureRenderTypeRenderer iconsRenderer;
   private MinimapRendererHelper helper;
   private final RadarRenderProvider radarRenderProvider;

   private RadarRenderer(RadarIconManager radarIconManager, Minimap minimap, RadarElementReader elementReader, RadarRenderProvider provider, RadarRenderContext context) {
      super(elementReader, provider, context);
      this.radarIconManager = radarIconManager;
      this.minimap = minimap;
      this.radarRenderProvider = provider;
   }

   public void preRender(MinimapElementRenderInfo renderInfo, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers) {
      this.radarIconManager.allowPrerender();
      ModSettings settings = HudMod.INSTANCE.getSettings();
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      SingleConfigManager<Config> primaryConfigManager = configManager.getPrimaryConfigManager();
      this.iconsAllowed = true;
      this.labelsAllowed = true;
      (this.context).reversedOrder = MinimapKeyMappings.REVERSE_ENTITY_RADAR.method_1434();
      MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
      this.radarSession = session.getRadarSession();
      this.categoryManager = this.radarSession.getCategoryManager();
      this.previousList = null;
      this.labelScale = (Double)configManager.getEffective(MinimapProfiledConfigOptions.RADAR_NAME_SCALE) * (class_310.method_1551().method_1573() ? (double)2.0F : (double)1.0F);
      this.smoothDots = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.RADAR_SMOOTH_DOTS);
      this.debugEntityIcons = (Boolean)primaryConfigManager.getEffective(MinimapPrimaryClientConfigOptions.DEBUG_ENTITY_ICONS);
      this.debugEntityVariantIds = (Boolean)primaryConfigManager.getEffective(MinimapPrimaryClientConfigOptions.DEBUG_ENTITY_VARIANT_IDS);
      this.dotsStyle = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.RADAR_DOTS_STYLE);
      this.dotsRenderType = this.smoothDots ? CustomRenderTypes.GUI_BILINEAR : CustomRenderTypes.GUI_NEAREST;
      vanillaBufferSource.method_22993();
      this.minimapBufferSource = XaeroLib.INSTANCE.getClient().getBufferProvider();
      this.dotsBufferBuilder = null;
      this.labelBgBuilder = this.minimapBufferSource.getBuffer(CustomRenderTypes.RADAR_NAME_BGS);
      this.iconsRenderer = multiTextureRenderTypeRenderers.getRenderer(MultiTextureRenderTypeRendererProvider::defaultTextureBind, CustomRenderTypes.GUI_BILINEAR);
      this.helper = HudMod.INSTANCE.getMinimap().getMinimapFBORenderer().getHelper();
      double playerDimDiv = renderInfo.backgroundCoordinateScale / renderInfo.renderEntityDimensionScale;
      int shapeConfig = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.SHAPE);
      this.maxDistanceSquared = RadarUtils.getMaxDistance(session.getProcessor(), shapeConfig == 1) * playerDimDiv * playerDimDiv;
   }

   public boolean renderElement(class_1297 e, boolean highlighted, boolean outOfBounds, double optionalDepth, float optionalScale, double partialX, double partialY, MinimapElementRenderInfo renderInfo, MinimapElementGraphics guiGraphics, class_4597.class_4598 vanillaBufferSource) {
      if (renderInfo.location == MinimapElementRenderLocation.IN_MINIMAP) {
         double offX = e.method_23317() - renderInfo.renderEntityPos.field_1352;
         if (offX * offX > this.maxDistanceSquared) {
            return false;
         }

         double offY = e.method_23321() - renderInfo.renderEntityPos.field_1350;
         if (offY * offY > this.maxDistanceSquared) {
            return false;
         }
      }

      if ((this.context).radarList == null) {
         EntityRadarCategory rootCategory = this.categoryManager.getRootCategory();
         EntityRadarCategory syncedRootCategory = this.categoryManager.getEffectiveSyncedRootCategory();
         (this.context).radarList = RadarList.Builder.getDefault().build();
         (this.context).radarList.setClientCategory((EntityRadarCategory)this.categoryManager.getRuleResolver().resolve(rootCategory, e, renderInfo.player));
         if ((this.context).radarList.getClientCategory() == null) {
            if (!(this.context).isMainDot) {
               return false;
            }

            (this.context).radarList.setClientCategory(rootCategory);
         }

         if (syncedRootCategory != null) {
            (this.context).radarList.setSyncedCategory((EntityRadarCategory)this.categoryManager.getRuleResolver().resolve(syncedRootCategory, e, renderInfo.player));
            if (!(this.context).isMainDot && (this.context).radarList.getSyncedCategory() == null) {
               return false;
            }
         }

         if ((this.context).radarList == null) {
            return false;
         }
      }

      if ((this.context).radarList != this.previousList) {
         this.setupRenderForList((this.context).radarList);
         this.previousList = (this.context).radarList;
      }

      this.setupRenderForEntity(e);
      if (e instanceof class_1657) {
         this.confirmTrackedPlayerRadarRender((class_1657)e);
      }

      class_1297 renderEntity = renderInfo.renderEntity;
      boolean cave = renderInfo.cave;
      float optionalScaleAdjust = renderInfo.location == MinimapElementRenderLocation.OVER_MINIMAP ? 0.5F : 1.0F;
      optionalScale *= optionalScaleAdjust;
      class_4587 matrixStack = guiGraphics.pose();
      matrixStack.method_22903();
      boolean icon = this.iconsAllowed && (this.context).icon;
      boolean name = this.name;
      if (highlighted && this.nameSettingForCategory > 0) {
         name = true;
      }

      XaeroIcon entityIcon = null;
      if (icon) {
         entityIcon = this.radarIconManager.get(e, (float)(this.context).iconScale, this.debugEntityIcons, this.debugEntityVariantIds, guiGraphics, renderInfo.framebuffer);
      }

      if (entityIcon == RadarIconManager.DOT) {
         entityIcon = null;
         icon = false;
      }

      boolean usableIcon = entityIcon != null && entityIcon != RadarIconManager.FAILED;
      float offY = (float)(renderEntity.method_23318() - e.method_23318());
      int labelOffsetX = 0;
      int labelOffsetY = 0;
      matrixStack.method_22904(partialX, partialY, (double)0.0F);
      double figureScale;
      if (usableIcon) {
         figureScale = (this.context).iconScale;
         this.renderIcon(entityIcon, (double)optionalScale, figureScale, offY, cave, matrixStack);
      } else {
         boolean smooth = this.smoothDots;
         if (!smooth) {
            optionalScale = (float)Math.ceil((double)optionalScale);
         }

         double dotActualScale = (double)optionalScale;
         figureScale = (this.context).dotScale;
         if (this.dotsStyle == 1) {
            if (!smooth) {
               figureScale = (double)((int)figureScale);
            }

            dotActualScale *= figureScale;
         }

         float dotOffset = this.renderDot(e, renderInfo.player, smooth, optionalScale, figureScale, offY, cave, matrixStack);
         if (!smooth) {
            double dotRadius = (double)(-dotOffset) * dotActualScale;
            double dotRadiusPartial = dotRadius - (double)((int)dotRadius);
            labelOffsetX = partialX - dotRadiusPartial <= (double)-0.5F ? -1 : 0;
            labelOffsetY = partialY - dotRadiusPartial < (double)-0.5F ? -1 : 0;
         }

         if (icon && this.displayNameWhenIconFails && entityIcon == RadarIconManager.FAILED) {
            name = true;
         }
      }

      matrixStack.method_22909();
      if (!this.labelsAllowed) {
         return true;
      } else if (!name && this.displayY <= 0) {
         return true;
      } else {
         labelOffsetY += (int)Math.round((double)(usableIcon ? 11 : 5) * figureScale * (double)optionalScale);
         matrixStack.method_22904((double)labelOffsetX, (double)labelOffsetY, optionalDepth + (double)0.1F);
         if (optionalScale < 1.0F) {
            optionalScale = 1.0F;
         }

         this.renderLabel(e, renderEntity, name, (double)optionalScale, matrixStack);
         return true;
      }
   }

   public void postRender(MinimapElementRenderInfo renderInfo, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers) {
      if ((this.context).reversedOrder && this.dotsBufferBuilder != null) {
         this.minimapBufferSource.endBatch(this.dotsRenderType);
      }

      multiTextureRenderTypeRenderers.draw(this.iconsRenderer);
      if (!(this.context).reversedOrder && this.dotsBufferBuilder != null) {
         this.minimapBufferSource.endBatch(this.dotsRenderType);
      }

      this.minimapBufferSource.endBatch();
      this.iconsRenderer = null;
      this.previousList = null;
   }

   private void renderIcon(XaeroIcon entityIcon, double optionalScale, double figureScale, float offY, boolean cave, class_4587 matrixStack) {
      double clampedScale = Math.max((double)1.0F, figureScale * optionalScale);
      matrixStack.method_22905((float)clampedScale, (float)clampedScale, 1.0F);
      float brightness = !this.heightBasedFade ? 1.0F : this.radarSession.getColorHelper().getEntityHeightFade(offY, this.heightLimit, this.startFadingAt);
      float opacity = 1.0F;
      if (cave) {
         opacity = brightness;
         brightness = 1.0F;
      }

      MultiTextureRenderUtil.prepareTexturedColoredRect(matrixStack.method_23760().method_23761(), -31.0F, -31.0F, entityIcon.getOffsetX() + 1, entityIcon.getOffsetY() + 1, 62.0F, 62.0F, 62.0F, (float)entityIcon.getTextureAtlas().getWidth(), brightness, brightness, brightness, opacity, entityIcon.getTextureAtlas().getTextureId(), this.iconsRenderer);
   }

   private float renderDot(class_1297 e, class_1657 player, boolean smooth, float optionalScale, double figureScale, float offY, boolean cave, class_4587 matrixStack) {
      matrixStack.method_22905(optionalScale, optionalScale, 1.0F);
      int color = this.radarSession.getColorHelper().getEntityColor(e, offY, cave, this.heightLimit, this.startFadingAt, this.heightBasedFade, this.radarColor, this.fallbackColor);
      float r = (float)(color >> 16 & 255) / 255.0F;
      float g = (float)(color >> 8 & 255) / 255.0F;
      float b = (float)(color & 255) / 255.0F;
      float a = (float)(color >> 24 & 255) / 255.0F;
      int dotTextureX = 0;
      int dotTextureY = 0;
      int dotTextureW = 0;
      int dotTextureH = 0;
      float dotOffset = 0.0F;
      if (this.dotsStyle == 1) {
         if (smooth) {
            dotTextureX = 1;
            dotTextureY = 88;
         } else {
            dotTextureX = 9;
            dotTextureY = 77;
         }

         dotOffset = -3.5F;
         dotTextureH = 8;
         dotTextureW = 8;
         matrixStack.method_22905((float)figureScale, (float)figureScale, 1.0F);
      } else {
         switch ((this.context).dotSize) {
            case 1:
               dotOffset = -4.5F;
               dotTextureY = 108;
               dotTextureH = 9;
               dotTextureW = 9;
               break;
            case 2:
            default:
               dotOffset = -5.5F;
               dotTextureY = 117;
               dotTextureH = 11;
               dotTextureW = 11;
               break;
            case 3:
               dotOffset = -7.5F;
               dotTextureY = 128;
               dotTextureH = 15;
               dotTextureW = 15;
               break;
            case 4:
               dotOffset = -10.5F;
               dotTextureY = 160;
               dotTextureH = 21;
               dotTextureW = 21;
         }
      }

      if (this.dotsBufferBuilder == null) {
         this.dotsBufferBuilder = this.minimapBufferSource.getBuffer(this.dotsRenderType);
      }

      RenderBufferUtil.addTexturedColoredRect(matrixStack.method_23760().method_23761(), this.dotsBufferBuilder, dotOffset, dotOffset, dotTextureX, dotTextureY, dotTextureW, dotTextureH, r, g, b, a, 256.0F);
      return dotOffset;
   }

   private void renderLabel(class_1297 e, class_1297 renderEntity, boolean name, double optionalScale, class_4587 matrixStack) {
      double dotNameScale = this.labelScale * optionalScale;
      matrixStack.method_22905((float)dotNameScale, (float)dotNameScale, 1.0F);
      String yValueString = null;
      if (this.displayY > 0) {
         int yInt = (int)Math.floor(e.method_23318());
         int pYInt = (int)Math.floor(renderEntity.method_23318());
         if (this.displayY == 1) {
            yValueString = "" + yInt;
         } else if (this.displayY == 2) {
            yValueString = "" + (yInt - pYInt);
         } else {
            yValueString = "";
         }

         yValueString = yValueString + (yInt > pYInt ? "↑" : (yInt != pYInt ? "↓" : ""));
         if (yValueString.length() == 0) {
            yValueString = "-";
         }
      }

      class_327 font = class_310.method_1551().field_1772;
      String label = null;
      if (name) {
         class_2561 component = Misc.getFixedDisplayName(e);
         if (component == null) {
            return;
         }

         label = component.getString();
         if (this.displayY > 0) {
            label = label + "(" + yValueString + ")";
         }
      } else if (this.displayY > 0) {
         label = yValueString;
      }

      if (label != null) {
         int labelW = font.method_1727(label);
         RenderBufferUtil.addColoredRect(matrixStack.method_23760().method_23761(), this.labelBgBuilder, (float)(-labelW / 2 - 2), -1.0F, labelW + 3, 10, 0.0F, 0.0F, 0.0F, 0.3529412F);
         Misc.drawNormalText(matrixStack, (String)label, (float)(-labelW / 2), 0.0F, -1, false, this.minimapBufferSource);
      }
   }

   private void setupRenderForList(RadarList radarList) {
      if (!this.radarRenderProvider.isUsed()) {
         this.radarRenderProvider.setupContextForList(radarList, this.context);
      }

      this.heightLimit = ((Double)radarList.getEffective(EntityRadarCategorySettings.HEIGHT_LIMIT)).intValue();
      this.heightBasedFade = (Boolean)radarList.getEffective(EntityRadarCategorySettings.HEIGHT_FADE);
      this.startFadingAt = ((Double)radarList.getEffective(EntityRadarCategorySettings.START_FADING_AT)).intValue();
      this.displayNameWhenIconFails = (Boolean)radarList.getEffective(EntityRadarCategorySettings.ICON_NAME_FALLBACK);
      this.alwaysNameTags = (Boolean)radarList.getEffective(EntityRadarCategorySettings.ALWAYS_NAMETAGS);
      this.radarColor = RadarColor.fromIndex(((Double)radarList.getEffective(EntityRadarCategorySettings.COLOR)).intValue());
      this.fallbackColor = this.radarSession.getColorHelper().getFallbackColor(radarList);
      this.displayY = ((Double)radarList.getEffective(EntityRadarCategorySettings.DISPLAY_Y)).intValue();
      this.nameSettingForCategory = ((Double)radarList.getEffective(EntityRadarCategorySettings.NAMES)).intValue();
      this.namesForCategory = this.nameSettingForCategory == 1 && (this.context).playerListDown || this.nameSettingForCategory == 2;
   }

   private void setupRenderForEntity(class_1297 entity) {
      if (!this.radarRenderProvider.isUsed()) {
         this.radarRenderProvider.setupContextForEntity(entity, this.context);
      }

      boolean name = this.namesForCategory;
      if (!name && !(entity instanceof class_1657)) {
         name = this.alwaysNameTags && entity.method_16914();
      }

      this.name = name;
   }

   private void confirmTrackedPlayerRadarRender(class_1657 e) {
      if (HudMod.INSTANCE.getTrackedPlayerRenderer().getCollector().playerExists(e.method_5667())) {
         HudMod.INSTANCE.getTrackedPlayerRenderer().getCollector().confirmPlayerRadarRender(e);
      }

      if (HudMod.INSTANCE.getSupportMods().worldmap()) {
         HudMod.INSTANCE.getSupportMods().worldmapSupport.confirmPlayerRadarRender(e);
      }
   }

   public void renderSingleEntity(class_1297 entity, boolean cave, boolean highlighted, float optionalScale, boolean allowIcon, boolean allowLabel, MinimapElementRenderLocation location, class_276 defaultFramebuffer, MinimapElementGraphics guiGraphics, boolean depth) {
      (this.context).radarList = null;
      MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
      class_1657 player = class_310.method_1551().field_1724;
      MinimapElementRenderInfo renderInfo = new MinimapElementRenderInfo(location, entity, player, entity.method_73189(), cave, 1.0F, defaultFramebuffer, (double)1.0F, entity.method_73183().method_27983());
      MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers = session.getMultiTextureRenderTypeRenderers();
      class_4597.class_4598 vanillaBufferSource = class_310.method_1551().method_22940().method_23000();
      (this.context).isMainDot = entity == class_310.method_1551().method_1560();
      this.preRender(renderInfo, vanillaBufferSource, multiTextureRenderTypeRenderers);
      if (!depth) {
         this.dotsRenderType = CustomRenderTypes.GUI_BILINEAR_NO_DEPTH;
      }

      this.iconsAllowed = allowIcon;
      this.labelsAllowed = allowLabel;
      this.renderElement(entity, highlighted, false, (double)0.0F, optionalScale, (double)0.0F, (double)0.0F, renderInfo, guiGraphics, vanillaBufferSource);
      this.postRender(renderInfo, vanillaBufferSource, multiTextureRenderTypeRenderers);
      (this.context).isMainDot = false;
   }

   public boolean shouldRender(MinimapElementRenderLocation location) {
      if (!this.minimap.usingFBO()) {
         return false;
      } else if (location == MinimapElementRenderLocation.WORLD_MAP) {
         return true;
      } else if (location == MinimapElementRenderLocation.WORLD_MAP_MENU) {
         return true;
      } else {
         ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
         return (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.DISPLAY_RADAR);
      }
   }

   public static final class Builder {
      private RadarIconManager radarIconManager;
      private Minimap minimap;

      private Builder() {
      }

      public Builder setDefault() {
         this.setRadarIconManager((RadarIconManager)null);
         return this;
      }

      public Builder setRadarIconManager(RadarIconManager radarIconManager) {
         this.radarIconManager = radarIconManager;
         return this;
      }

      public Builder setMinimap(Minimap minimap) {
         this.minimap = minimap;
         return this;
      }

      public RadarRenderer build() {
         if (this.radarIconManager != null && this.minimap != null) {
            RadarElementReader elementReader = new RadarElementReader();
            RadarRenderProvider provider = new RadarRenderProvider();
            RadarRenderContext context = new RadarRenderContext();
            return new RadarRenderer(this.radarIconManager, this.minimap, elementReader, provider, context);
         } else {
            throw new IllegalStateException();
         }
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
