package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.class_1074;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_124;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_276;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_3532;
import net.minecraft.class_364;
import net.minecraft.class_3675;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5321;
import net.minecraft.class_6379;
import net.minecraft.class_3675.class_307;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.controls.util.KeyMappingUtils;
import xaero.lib.client.graphics.XaeroBufferProvider;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.lib.common.config.option.ConfigOption;
import xaero.map.MapProcessor;
import xaero.map.WorldMap;
import xaero.map.animation.SlowingAnimation;
import xaero.map.common.config.option.WorldMapProfiledConfigOptions;
import xaero.map.config.util.WorldMapClientConfigUtils;
import xaero.map.graphics.MapRenderHelper;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRenderer;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.map.gui.GuiMap;
import xaero.map.gui.GuiTexturedButton;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.misc.Misc;
import xaero.map.mods.SupportMods;
import xaero.map.world.MapDimension;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.drawing.ColorPickerWidget;
import xaeroplus.feature.drawing.DrawingColorPickerButton;
import xaeroplus.feature.render.ellipse.Ellipse;
import xaeroplus.feature.render.line.Line;
import xaeroplus.feature.render.shaders.XaeroPlusShaders;
import xaeroplus.feature.render.text.Text;
import xaeroplus.module.ModuleManager;
import xaeroplus.module.impl.Breadcrumbs;
import xaeroplus.module.impl.Drawing;
import xaeroplus.module.impl.LavaColumns;
import xaeroplus.module.impl.LiquidNewChunks;
import xaeroplus.module.impl.OldBiomes;
import xaeroplus.module.impl.OldChunks;
import xaeroplus.module.impl.PaletteNewChunks;
import xaeroplus.module.impl.Portals;
import xaeroplus.settings.Settings;
import xaeroplus.util.BaritoneExecutor;
import xaeroplus.util.BaritoneHelper;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.Color;
import xaeroplus.util.ColorHelper;
import xaeroplus.util.DrawingMode;

@Mixin(
   value = {GuiMap.class},
   remap = false
)
public abstract class MixinGuiMap extends ScreenBase implements IRightClickableElement {
   @Unique
   boolean pan;
   @Unique
   double panMouseStartX;
   @Unique
   double panMouseStartY;
   @Unique
   class_4185 switchToNetherButton;
   @Unique
   class_4185 switchToOverworldButton;
   @Unique
   class_4185 switchToEndButton;
   @Unique
   class_4185 startDrawingButton;
   @Unique
   class_4185 drawLineSegmentButton;
   @Unique
   class_4185 drawInfiniteLineButton;
   @Unique
   class_4185 drawHighlightsButton;
   @Unique
   class_4185 drawEllipseButton;
   @Unique
   class_4185 drawTextButton;
   @Unique
   DrawingColorPickerButton drawColorPickerButton;
   @Unique
   ColorPickerWidget drawColorPicker;
   @Unique
   class_4185 drawMeasurementToolButton;
   @Unique
   class_4185 exitButton;
   @Unique
   boolean drawing = false;
   @Unique
   class_2338 drawInProgressPos = null;
   @Unique
   boolean drawingLeftClickDown = false;
   @Unique
   boolean drawingRightClickDown = false;
   @Unique
   boolean drawTextEntryActive = false;
   @Unique
   DrawingMode drawingMode;
   @Unique
   boolean colorPickerActive;
   @Unique
   class_342 drawTextEntryField;
   @Shadow
   private double cameraX;
   @Shadow
   private double cameraZ;
   @Shadow
   private int[] cameraDestination;
   @Shadow
   private SlowingAnimation cameraDestinationAnimX;
   @Shadow
   private SlowingAnimation cameraDestinationAnimZ;
   @Shadow
   private double prevPlayerDimDiv;
   @Shadow
   private MapProcessor mapProcessor;
   @Shadow
   private class_4185 exportButton;
   @Shadow
   private class_4185 claimsButton;
   @Shadow
   private class_4185 zoomInButton;
   @Shadow
   private class_4185 zoomOutButton;
   @Shadow
   private class_4185 keybindingsButton;
   @Shadow
   private class_4185 dimensionToggleButton;
   @Shadow
   private class_4185 attachedCameraButton;
   @Shadow
   private int rightClickX;
   @Shadow
   private int rightClickY;
   @Shadow
   private int rightClickZ;
   @Shadow
   private int mouseBlockPosX;
   @Shadow
   private int mouseBlockPosZ;
   @Shadow
   private static double destScale;
   @Shadow
   private MapTileSelection mapTileSelection;
   @Shadow
   private double scale;
   @Shadow
   public static boolean hiddenUI;

   protected MixinGuiMap(final class_437 parent, final class_437 escape, final class_2561 titleIn) {
      super(parent, escape, titleIn);
      this.drawingMode = DrawingMode.LINE_SEGMENT;
      this.colorPickerActive = false;
      this.cameraX = (double)0.0F;
      this.cameraZ = (double)0.0F;
      this.cameraDestination = null;
      this.cameraDestinationAnimX = null;
      this.cameraDestinationAnimZ = null;
   }

   @Shadow
   public abstract <T extends class_364 & class_4068 & class_6379> T addButton(final T guiEventListener);

   @Shadow
   public abstract <T extends class_364 & class_6379> T method_25429(final T guiEventListener);

   @Unique
   private class_2561 xaeroPlus$prefix(class_2561 component) {
      return class_2561.method_43470("[XP] ").method_10852(component);
   }

   @Unique
   private class_2561 xaeroPlus$keybindPrefix(class_2561 component, class_304 bind) {
      return class_2561.method_43473().method_10852(class_2561.method_43470(KeyMappingUtils.getKeyName(bind) + " ").method_27692(class_124.field_1077)).method_10852(component);
   }

   @Inject(
      method = {"method_25426"},
      at = {@At("RETURN")},
      remap = true
   )
   public void customInitGui(CallbackInfo ci) {
      this.startDrawingButton = new GuiTexturedButton(0, this.attachedCameraButton.method_46427() - 20, 20, 20, 47, 0, 16, 16, Globals.guiTextures, (button) -> this.onToggleDrawingButton(), () -> new Tooltip(this.xaeroPlus$keybindPrefix(this.xaeroPlus$prefix(class_2561.method_43471("xaeroplus.gui.world_map.start_drawing")), Settings.REGISTRY.worldMapToggleDrawingKeybindSetting.getKeyBinding())), 256, 256);
      this.drawLineSegmentButton = new GuiTexturedButton(this.startDrawingButton.method_46426() + 16, this.startDrawingButton.method_46427(), 20, 20, 65, 0, 16, 16, Globals.guiTextures, (button) -> this.setDrawingMode(DrawingMode.LINE_SEGMENT), () -> new Tooltip(this.xaeroPlus$prefix(class_2561.method_43471("xaeroplus.gui.world_map.draw_line_segment"))), 256, 256);
      this.drawLineSegmentButton.field_22764 = false;
      this.drawInfiniteLineButton = new GuiTexturedButton(this.startDrawingButton.method_46426() + 16, this.drawLineSegmentButton.method_46427() + 20, 20, 20, 101, 0, 16, 16, Globals.guiTextures, (button) -> this.setDrawingMode(DrawingMode.INFINITE_LINE), () -> new Tooltip(this.xaeroPlus$prefix(class_2561.method_43471("xaeroplus.gui.world_map.draw_infinite_line"))), 256, 256);
      this.drawInfiniteLineButton.field_22764 = false;
      this.drawHighlightsButton = new GuiTexturedButton(this.startDrawingButton.method_46426() + 16, this.drawInfiniteLineButton.method_46427() + 20, 20, 20, 82, 0, 16, 16, Globals.guiTextures, (button) -> this.setDrawingMode(DrawingMode.HIGHLIGHT), () -> new Tooltip(this.xaeroPlus$prefix(class_2561.method_43471("xaeroplus.gui.world_map.draw_highlights"))), 256, 256);
      this.drawHighlightsButton.field_22764 = false;
      this.drawEllipseButton = new GuiTexturedButton(this.startDrawingButton.method_46426() + 16, this.drawHighlightsButton.method_46427() + 20, 20, 20, 137, 19, 16, 16, Globals.guiTextures, (button) -> this.setDrawingMode(DrawingMode.ELLIPSE), () -> new Tooltip(this.xaeroPlus$prefix(class_2561.method_43471("xaeroplus.gui.world_map.draw_ellipse"))), 256, 256);
      this.drawEllipseButton.field_22764 = false;
      this.drawTextButton = new GuiTexturedButton(this.startDrawingButton.method_46426() + 16, this.drawEllipseButton.method_46427() + 20, 20, 20, 118, 0, 16, 16, Globals.guiTextures, (button) -> this.setDrawingMode(DrawingMode.TEXT), () -> new Tooltip(this.xaeroPlus$prefix(class_2561.method_43471("xaeroplus.gui.world_map.draw_text"))), 256, 256);
      this.drawTextButton.field_22764 = false;
      Drawing drawingModule = (Drawing)ModuleManager.getModule(Drawing.class);
      int var10003 = this.startDrawingButton.method_46426() + 16;
      int var10004 = this.drawTextButton.method_46427() + 20;
      Supplier var10005 = () -> new Tooltip(this.xaeroPlus$prefix(class_2561.method_43471("xaeroplus.gui.world_map.draw_color")));
      Objects.requireNonNull(drawingModule);
      this.drawColorPickerButton = new DrawingColorPickerButton(var10003, var10004, var10005, drawingModule::getDrawingColor, (button) -> this.onColorPickerButton());
      this.drawColorPickerButton.field_22764 = false;
      int colorPickerSize = 90;
      int colorPickerX = this.drawColorPickerButton.method_46426() + this.drawColorPickerButton.method_25368() + 4;
      int colorPickerY = this.startDrawingButton.method_46427() + 20;
      Color var10006 = drawingModule.getDrawingColor();
      Objects.requireNonNull(drawingModule);
      this.drawColorPicker = new ColorPickerWidget(colorPickerX, colorPickerY, colorPickerSize, var10006, drawingModule::setDrawingColor);
      this.drawColorPicker.field_22764 = false;
      this.drawMeasurementToolButton = new GuiTexturedButton(this.startDrawingButton.method_46426() + 16, this.drawColorPickerButton.method_46427() + 20, 20, 20, 135, 0, 16, 16, Globals.guiTextures, (button) -> this.setDrawingMode(DrawingMode.MEASUREMENT), () -> new Tooltip(this.xaeroPlus$prefix(class_2561.method_43471("xaeroplus.gui.world_map.draw_measurement_tool"))), 256, 256);
      this.drawMeasurementToolButton.field_22764 = false;
      this.drawTextEntryField = new class_342(class_310.method_1551().field_1772, 0, 0, 150, 20, class_2561.method_30163("Text:"));
      this.drawTextEntryField.method_1862(false);
      this.drawTextEntryField.method_1875(0);
      this.drawTextEntryField.method_47404(class_2561.method_43470("Text:").method_27692(class_124.field_1063));
      this.switchToEndButton = new GuiTexturedButton(this.field_22789 - 20, this.zoomInButton.method_46427() - 20, 20, 20, 117, 19, 16, 16, Globals.guiTextures, (button) -> this.onSwitchDimensionButton(class_1937.field_25181), () -> new Tooltip(this.xaeroPlus$keybindPrefix(this.xaeroPlus$prefix(class_2561.method_43471("xaeroplus.keybind.switch_to_end")), Settings.REGISTRY.switchToEndSetting.getKeyBinding())), 256, 256);
      this.switchToOverworldButton = new GuiTexturedButton(this.field_22789 - 20, this.switchToEndButton.method_46427() - 20, 20, 20, 98, 18, 16, 16, Globals.guiTextures, (button) -> this.onSwitchDimensionButton(class_1937.field_25179), () -> new Tooltip(this.xaeroPlus$keybindPrefix(this.xaeroPlus$prefix(class_2561.method_43471("xaeroplus.keybind.switch_to_overworld")), Settings.REGISTRY.switchToOverworldSetting.getKeyBinding())), 256, 256);
      this.switchToNetherButton = new GuiTexturedButton(this.field_22789 - 20, this.switchToOverworldButton.method_46427() - 20, 20, 20, 79, 19, 16, 16, Globals.guiTextures, (button) -> this.onSwitchDimensionButton(class_1937.field_25180), () -> new Tooltip(this.xaeroPlus$keybindPrefix(this.xaeroPlus$prefix(class_2561.method_43471("xaeroplus.keybind.switch_to_nether")), Settings.REGISTRY.switchToNetherSetting.getKeyBinding())), 256, 256);
      this.exitButton = new GuiTexturedButton(this.field_22789 - 34, 2, 32, 32, 0, 0, 0, 0, Globals.guiTextures, (button) -> this.method_25419(), () -> new Tooltip(this.xaeroPlus$prefix(class_2561.method_43471("xaeroplus.gui.world_map.exit"))), 256, 256);
      this.pan = false;
      this.drawing = false;
      if (Settings.REGISTRY.worldMapUIAdditions.get()) {
         if (!SupportMods.pac()) {
            this.method_37066(this.claimsButton);
            this.exportButton.method_46419(this.claimsButton.method_46427());
            this.keybindingsButton.method_46419(this.claimsButton.method_46427() - 20);
            this.zoomOutButton.method_46419(this.keybindingsButton.method_46427() - 20);
            this.zoomInButton.method_46419(this.zoomOutButton.method_46427() - 20);
            this.switchToEndButton.method_46419(this.zoomInButton.method_46427() - 20);
            this.switchToOverworldButton.method_46419(this.switchToEndButton.method_46427() - 20);
            this.switchToNetherButton.method_46419(this.switchToOverworldButton.method_46427() - 20);
         }

         this.addButton(this.startDrawingButton);
         this.addButton(this.switchToEndButton);
         this.addButton(this.switchToOverworldButton);
         this.addButton(this.switchToNetherButton);
         this.addButton(this.exitButton);
      }
   }

   @Unique
   private void setDrawingMode(DrawingMode drawingMode) {
      this.drawInProgressPos = null;
      ((Drawing)ModuleManager.getModule(Drawing.class)).removeInProgressLine();
      ((Drawing)ModuleManager.getModule(Drawing.class)).removeInProgressEllipse();
      this.drawingLeftClickDown = false;
      this.drawingRightClickDown = false;
      this.drawTextEntryActive = false;
      this.drawingMode = drawingMode;
   }

   @Unique
   private void onToggleDrawingButton() {
      boolean prevDrawing = this.drawing;
      this.method_25423(this.field_22789, this.field_22790);
      this.drawing = !prevDrawing;
      if (this.drawing) {
         this.addButton(this.drawLineSegmentButton);
         this.addButton(this.drawInfiniteLineButton);
         this.addButton(this.drawHighlightsButton);
         this.addButton(this.drawEllipseButton);
         this.addButton(this.drawTextButton);
         this.addButton(this.drawColorPickerButton);
         this.addButton(this.drawColorPicker);
         this.addButton(this.drawMeasurementToolButton);
         this.drawLineSegmentButton.field_22764 = true;
         this.drawInfiniteLineButton.field_22764 = true;
         this.drawHighlightsButton.field_22764 = true;
         this.drawEllipseButton.field_22764 = true;
         this.drawTextButton.field_22764 = true;
         this.drawColorPickerButton.field_22764 = true;
         this.drawColorPicker.field_22764 = this.colorPickerActive;
         this.drawMeasurementToolButton.field_22764 = true;
      } else {
         this.xaeroPlus$stopDrawing();
      }

   }

   @Unique
   private void onColorPickerButton() {
      if (this.drawing) {
         this.colorPickerActive = !this.colorPickerActive;
         this.drawColorPicker.field_22764 = this.colorPickerActive;
      } else {
         this.colorPickerActive = false;
         this.drawColorPicker.field_22764 = false;
      }

   }

   public void onExit(class_437 screen) {
      if (!Settings.REGISTRY.persistMapDimensionSwitchSetting.get()) {
         try {
            class_5321<class_1937> actualDimension = ChunkUtils.getActualDimension();
            if (Globals.getCurrentDimensionId() != actualDimension) {
               Globals.switchToDimension(actualDimension);
               if (!Settings.REGISTRY.radarWhileDimensionSwitchedSetting.get()) {
                  trySettingCurrentProfileOption(WorldMapProfiledConfigOptions.MINIMAP_RADAR, true);
               }
            }
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Failed to switch back to original dimension", e);
         }
      }

      super.onExit(screen);
   }

   @Inject(
      method = {"method_25394"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/gui/GuiMap;method_25423(II)V",
   ordinal = 1,
   shift = Shift.AFTER
)},
      remap = true
   )
   public void toggleRadarWhileDimensionSwitched(final CallbackInfo ci, @Local(name = {"futureDimension"}) MapDimension futureDimension) {
      if (!Settings.REGISTRY.radarWhileDimensionSwitchedSetting.get() && futureDimension != null) {
         trySettingCurrentProfileOption(WorldMapProfiledConfigOptions.MINIMAP_RADAR, futureDimension.getDimId() == ChunkUtils.getActualDimension());
      }

   }

   private static void trySettingCurrentProfileOption(ConfigOption<Boolean> option, boolean value) {
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      Boolean currentValue = (Boolean)configManager.getEffective(option);
      if (currentValue != value) {
         WorldMapClientConfigUtils.tryTogglingCurrentProfileOption(option);
      }

   }

   @Redirect(
      method = {"method_25394"},
      at = @At(
   value = "FIELD",
   target = "Lxaero/map/gui/GuiMap;cameraX:D",
   opcode = 181,
   ordinal = 1
),
      remap = true
   )
   public void fixDimensionSwitchCameraCoordsX(GuiMap owner, double value, @Local(name = {"playerDimDiv"}) double playerDimDiv) {
      this.cameraX *= this.prevPlayerDimDiv / playerDimDiv;
   }

   @Redirect(
      method = {"method_25394"},
      at = @At(
   value = "FIELD",
   target = "Lxaero/map/gui/GuiMap;cameraZ:D",
   opcode = 181,
   ordinal = 1
),
      remap = true
   )
   public void fixDimensionSwitchCameraCoordsZ(GuiMap owner, double value, @Local(name = {"playerDimDiv"}) double playerDimDiv) {
      this.cameraZ *= this.prevPlayerDimDiv / playerDimDiv;
   }

   @WrapOperation(
      method = {"method_25394"},
      slice = {@Slice(
   from = @At(
   value = "FIELD",
   target = "Lxaero/map/gui/GuiMap;prevLoadingLeaves:Z",
   opcode = 181
),
   to = @At(
   value = "INVOKE",
   target = "Lxaero/map/graphics/ImprovedFramebuffer;bindDefaultFramebuffer(Lnet/minecraft/class_310;)V"
)
)},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/lib/client/graphics/XaeroBufferProvider;endBatch()V",
   ordinal = 0
)},
      remap = true
   )
   public void drawWorldMapFeatures(final XaeroBufferProvider instance, final Operation<Void> original, @Local(name = {"flooredCameraX"}) int flooredCameraX, @Local(name = {"flooredCameraZ"}) int flooredCameraZ, @Local(name = {"matrixStack"}) class_4587 matrixStack, @Local(name = {"renderTypeBuffers"}) XaeroBufferProvider renderTypeBuffers, @Local(name = {"fboScale"}) double fboScale) {
      original.call(new Object[]{instance});
      if (!hiddenUI) {
         Globals.drawManager.drawWorldMapFeatures(flooredCameraX, flooredCameraZ, matrixStack, fboScale, renderTypeBuffers);
      }
   }

   @ModifyArg(
      method = {"method_25394"},
      slice = @Slice(
   from = @At(
   value = "FIELD",
   opcode = 178,
   target = "Lxaero/map/common/config/option/WorldMapProfiledConfigOptions;COORDINATES:Lxaero/lib/common/config/option/BooleanConfigOption;"
)
),
      at = @At(
   value = "INVOKE",
   target = "Lxaero/map/graphics/MapRenderHelper;drawCenteredStringWithBackground(Lnet/minecraft/class_332;Lnet/minecraft/class_327;Ljava/lang/String;IIIFFFF)V",
   ordinal = 0
)
   )
   public String renderCrossDimensionCursorCoordinates(final String original) {
      if (!Settings.REGISTRY.crossDimensionCursorCoordinates.get()) {
         return original;
      } else {
         class_5321<class_1937> dim = Globals.getCurrentDimensionId();
         if (dim != class_1937.field_25179 && dim != class_1937.field_25180) {
            return original;
         } else {
            double dimDiv = dim == class_1937.field_25180 ? (double)0.125F : (double)8.0F;
            int x = (int)((double)this.mouseBlockPosX / dimDiv);
            int z = (int)((double)this.mouseBlockPosZ / dimDiv);
            return original + " [" + x + ", " + z + "]";
         }
      }
   }

   @Inject(
      method = {"method_25394"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/graphics/MapRenderHelper;restoreDefaultShaderBlendState()V"
)},
      remap = true
   )
   public void renderCoordinatesGotoTextEntryFields(final class_332 guiGraphics, final int scaledMouseX, final int scaledMouseY, final float partialTicks, final CallbackInfo ci) {
      if (Settings.REGISTRY.worldMapUIAdditions.get()) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1755 != null && mc.field_1755.getClass().equals(GuiMap.class) && this.drawing && this.drawTextEntryActive && this.drawingMode == DrawingMode.TEXT && this.drawTextEntryField.field_22764) {
            this.drawTextEntryField.method_25394(guiGraphics, scaledMouseX, scaledMouseY, partialTicks);
         }

      }
   }

   @Inject(
      method = {"onDimensionToggleButton"},
      at = {@At("RETURN")}
   )
   public void onDimensionToggleAfter(final class_4185 b, final CallbackInfo ci) {
      if (!Settings.REGISTRY.radarWhileDimensionSwitchedSetting.get()) {
         trySettingCurrentProfileOption(WorldMapProfiledConfigOptions.MINIMAP_RADAR, this.mapProcessor.getMapWorld().getFutureDimensionId() == ChunkUtils.getActualDimension());
      }

   }

   @Inject(
      method = {"method_25393"},
      at = {@At("RETURN")},
      remap = true
   )
   public void onTick(final CallbackInfo ci) {
      if (Settings.REGISTRY.worldMapUIAdditions.get()) {
         if (this.drawing) {
            this.startDrawingButton.method_25365(false);
            this.drawLineSegmentButton.method_25365(this.drawingMode == DrawingMode.LINE_SEGMENT);
            this.drawInfiniteLineButton.method_25365(this.drawingMode == DrawingMode.INFINITE_LINE);
            this.drawHighlightsButton.method_25365(this.drawingMode == DrawingMode.HIGHLIGHT);
            this.drawEllipseButton.method_25365(this.drawingMode == DrawingMode.ELLIPSE);
            this.drawTextButton.method_25365(this.drawingMode == DrawingMode.TEXT);
            this.drawColorPickerButton.method_25365(false);
            this.drawMeasurementToolButton.method_25365(this.drawingMode == DrawingMode.MEASUREMENT);
            if (this.drawingMode == DrawingMode.TEXT && this.drawTextEntryActive) {
               this.drawTextEntryField.method_1888(true);
               this.drawTextEntryField.method_25365(true);
               this.method_25395(this.drawTextEntryField);
            }

         }
      }
   }

   @Inject(
      method = {"method_25394"},
      at = {@At("RETURN")}
   )
   public void updateInProgressLine(CallbackInfo ci) {
      if (this.drawing) {
         Drawing drawingModule = (Drawing)ModuleManager.getModule(Drawing.class);
         if (this.drawingMode != DrawingMode.ELLIPSE) {
            drawingModule.removeInProgressEllipse();
         }

         switch (this.drawingMode) {
            case LINE_SEGMENT:
            case INFINITE_LINE:
               if (this.drawInProgressPos == null) {
                  drawingModule.removeInProgressLine();
               } else {
                  Line inProgress = drawingModule.snap(this.drawInProgressPos.method_10263(), this.drawInProgressPos.method_10260(), this.mouseBlockPosX, this.mouseBlockPosZ, destScale);
                  drawingModule.setInProgressLine(inProgress, this.drawingMode);
               }
               break;
            case HIGHLIGHT:
               drawingModule.removeInProgressLine();
               if (this.drawingLeftClickDown) {
                  drawingModule.addHighlight(ChunkUtils.posToChunkPos(this.mouseBlockPosX), ChunkUtils.posToChunkPos(this.mouseBlockPosZ));
               }
               break;
            case ELLIPSE:
               drawingModule.removeInProgressLine();
               if (this.drawInProgressPos == null) {
                  drawingModule.removeInProgressEllipse();
               } else {
                  Ellipse ellipse = drawingModule.snapEllipse(this.drawInProgressPos.method_10263(), this.drawInProgressPos.method_10260(), this.mouseBlockPosX, this.mouseBlockPosZ, destScale);
                  if (ellipse == null) {
                     drawingModule.removeInProgressEllipse();
                  } else {
                     drawingModule.setInProgressEllipse(ellipse);
                  }
               }
               break;
            case MEASUREMENT:
               if (this.drawInProgressPos == null) {
                  drawingModule.removeInProgressLine();
               } else {
                  drawingModule.setInProgressLine(new Line(this.drawInProgressPos.method_10263(), this.drawInProgressPos.method_10260(), this.mouseBlockPosX, this.mouseBlockPosZ), this.drawingMode);
               }
         }

         if (this.drawingRightClickDown) {
            drawingModule.removeHighlight(ChunkUtils.posToChunkPos(this.mouseBlockPosX), ChunkUtils.posToChunkPos(this.mouseBlockPosZ));
            drawingModule.removeLine(this.mouseBlockPosX, this.mouseBlockPosZ);
            drawingModule.removeEllipse(this.mouseBlockPosX, this.mouseBlockPosZ);
            drawingModule.removeText(this.mouseBlockPosX, this.mouseBlockPosZ, this.getFboScale());
         }
      }

   }

   @WrapOperation(
      method = {"method_25394"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/lib/client/graphics/util/TextureUtils;clearRenderTarget(Lnet/minecraft/class_276;IF)V"
)}
   )
   public void transparentBgSetTransparentClearColor(final class_276 renderTarget, final int color, final float depth, final Operation<Void> original) {
      if (Settings.REGISTRY.transparentWorldmapBackgroundSetting.get()) {
         original.call(new Object[]{renderTarget, ColorHelper.getColor(0, 0, 0, 0), depth});
      } else {
         original.call(new Object[]{renderTarget, color, depth});
      }

   }

   @WrapOperation(
      method = {"method_25394"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;draw(Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRenderer;)V",
   ordinal = 0
)}
   )
   public void transparentBgConfigMapRenderWithLight(final MultiTextureRenderTypeRendererProvider instance, final MultiTextureRenderTypeRenderer renderer, final Operation<Void> original) {
      if (Settings.REGISTRY.transparentWorldmapBackgroundSetting.get()) {
         XaeroPlusShaders.setTransparentWMBackground(true);
         Globals.transparentWmBgApplyMapBlend = true;

         try {
            original.call(new Object[]{instance, renderer});
         } finally {
            XaeroPlusShaders.setTransparentWMBackground(false);
            Globals.transparentWmBgApplyMapBlend = false;
         }
      } else {
         XaeroPlusShaders.setTransparentWMBackground(false);
         original.call(new Object[]{instance, renderer});
      }

   }

   @WrapOperation(
      method = {"method_25394"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;draw(Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRenderer;)V",
   ordinal = 1
)}
   )
   public void transparentBgConfigMapRenderNoLight(final MultiTextureRenderTypeRendererProvider instance, final MultiTextureRenderTypeRenderer renderer, final Operation<Void> original) {
      if (Settings.REGISTRY.transparentWorldmapBackgroundSetting.get()) {
         XaeroPlusShaders.setTransparentWMBackground(true);
         Globals.transparentWmBgApplyMapBlend = true;

         try {
            original.call(new Object[]{instance, renderer});
         } finally {
            XaeroPlusShaders.setTransparentWMBackground(false);
            Globals.transparentWmBgApplyMapBlend = false;
         }
      } else {
         XaeroPlusShaders.setTransparentWMBackground(false);
         original.call(new Object[]{instance, renderer});
      }

   }

   @WrapOperation(
      method = {"method_25394"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;draw(Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRenderer;)V",
   ordinal = 2
)}
   )
   public void transparentBgConfigMainFBORender(final MultiTextureRenderTypeRendererProvider instance, final MultiTextureRenderTypeRenderer renderer, final Operation<Void> original) {
      if (Settings.REGISTRY.transparentWorldmapBackgroundSetting.get()) {
         Globals.transparentWmBgApplyMapFrameBlend = true;

         try {
            original.call(new Object[]{instance, renderer});
         } finally {
            Globals.transparentWmBgApplyMapFrameBlend = false;
         }
      } else {
         original.call(new Object[]{instance, renderer});
      }

   }

   @Inject(
      method = {"shouldSkipWorldRender"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void transparentBgDisableWorldRenderSkip(final CallbackInfoReturnable<Boolean> cir) {
      if (Settings.REGISTRY.transparentWorldmapBackgroundSetting.get()) {
         cir.setReturnValue(false);
      }

   }

   @WrapWithCondition(
      method = {"method_25394"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/graphics/MapRenderHelper;fillIntoExistingBuffer(Lorg/joml/Matrix4f;Lnet/minecraft/class_4588;IIIIFFFF)V",
   ordinal = 0
)},
      slice = {@Slice(
   from = @At(
   value = "FIELD",
   target = "Lxaero/map/graphics/CustomRenderTypes;MAP_COLOR_FILLER:Lnet/minecraft/class_1921;",
   opcode = 178
)
)}
   )
   public boolean transparentBgCancelMapColorFiller0(final Matrix4f matrix, final class_4588 bufferBuilder, final int x1, final int y1, final int x2, final int y2, final float r, final float g, final float b, final float a) {
      return !Settings.REGISTRY.transparentWorldmapBackgroundSetting.get();
   }

   @WrapWithCondition(
      method = {"method_25394"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/graphics/MapRenderHelper;fillIntoExistingBuffer(Lorg/joml/Matrix4f;Lnet/minecraft/class_4588;IIIIFFFF)V",
   ordinal = 1
)},
      slice = {@Slice(
   from = @At(
   value = "FIELD",
   target = "Lxaero/map/graphics/CustomRenderTypes;MAP_COLOR_FILLER:Lnet/minecraft/class_1921;",
   opcode = 178
)
)}
   )
   public boolean transparentBgCancelMapColorFiller1(final Matrix4f matrix, final class_4588 bufferBuilder, final int x1, final int y1, final int x2, final int y2, final float r, final float g, final float b, final float a) {
      return !Settings.REGISTRY.transparentWorldmapBackgroundSetting.get();
   }

   @Inject(
      method = {"method_25394"},
      at = {@At(
   value = "FIELD",
   target = "Lxaero/map/common/config/option/WorldMapProfiledConfigOptions;COORDINATES:Lxaero/lib/common/config/option/BooleanConfigOption;",
   opcode = 178,
   ordinal = 0
)},
      remap = true
   )
   public void renderMeasurementToolText(final class_332 guiGraphics, final int scaledMouseX, final int scaledMouseY, final float partialTicks, final CallbackInfo ci) {
      if (this.drawing) {
         if (this.drawingMode == DrawingMode.MEASUREMENT) {
            if (this.drawInProgressPos != null) {
               Line line = ((Drawing)ModuleManager.getModule(Drawing.class)).getInProgressLine();
               if (line != null) {
                  int len = class_3532.method_15357(line.length());
                  int dx = line.x2() - line.x1();
                  int dz = line.z2() - line.z1();
                  class_327 var10001 = this.field_22793;
                  String var10002 = len + " blocks [" + dx + " x " + dz + "]";
                  Objects.requireNonNull(this.field_22793);
                  MapRenderHelper.drawCenteredStringWithBackground(guiGraphics, var10001, var10002, scaledMouseX, scaledMouseY - 9, -1, 0.0F, 0.0F, 0.0F, 0.4F);
                  String degreeStr = String.format("%.2f", line.angle());
                  var10001 = this.field_22793;
                  var10002 = degreeStr + "°";
                  Objects.requireNonNull(this.field_22793);
                  MapRenderHelper.drawCenteredStringWithBackground(guiGraphics, var10001, var10002, scaledMouseX, scaledMouseY + 9, -1, 0.0F, 0.0F, 0.0F, 0.4F);
               }
            }
         }
      }
   }

   @Unique
   private float getFboScale() {
      float fboScale;
      if (this.scale >= (double)1.0F) {
         fboScale = (float)Math.max((double)1.0F, Math.floor(this.scale));
      } else {
         fboScale = (float)this.scale;
      }

      return fboScale;
   }

   @Inject(
      method = {"method_25394"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/mods/SupportXaeroMinimap;getSubWorldNameToRender()Ljava/lang/String;"
)}
   )
   public void renderDrawingStatusText(final CallbackInfo ci, @Local(argsOnly = true) final class_332 guiGraphics) {
      if (this.drawing) {
         MapRenderHelper.drawCenteredStringWithBackground(guiGraphics, class_310.method_1551().field_1772, "[XP] " + class_1074.method_4662("xaeroplus.gui.world_map.drawing_mode", new Object[0]), this.field_22789 / 2, 24, -1, 0.0F, 0.0F, 0.0F, 0.4F);
         String[] lines = class_1074.method_4662("xaeroplus.gui.world_map.drawing_mode_controls", new Object[0]).split("\n");

         for(int i = 0; i < lines.length; ++i) {
            class_327 var10001 = class_310.method_1551().field_1772;
            String var10002 = lines[i].trim();
            int var10004 = this.field_22790 - 2;
            int var10005 = lines.length;
            Objects.requireNonNull(class_310.method_1551().field_1772);
            var10004 -= var10005 * (9 + 1);
            Objects.requireNonNull(class_310.method_1551().field_1772);
            MapRenderHelper.drawStringWithBackground(guiGraphics, var10001, var10002, 40, var10004 + i * (9 + 1), -1, 0.0F, 0.0F, 0.0F, 0.4F);
         }

      }
   }

   @Inject(
      method = {"method_25402"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = true
   )
   public void cancelClicksWhileDrawing(final class_11909 event, final boolean doubleClick, final CallbackInfoReturnable<Boolean> cir) {
      if (this.drawing) {
         boolean toReturn = super.method_25402(event, doubleClick);
         if (toReturn) {
            cir.setReturnValue(true);
         } else if (this.colorPickerActive && (event.method_74245() == 0 || event.method_74245() == 1) && this.drawColorPicker.method_25405(event.comp_4798(), event.comp_4799())) {
            cir.setReturnValue(true);
         } else {
            if (event.method_74245() == 0) {
               this.drawingLeftClickDown = true;
               switch (this.drawingMode) {
                  case LINE_SEGMENT:
                  case INFINITE_LINE:
                  case ELLIPSE:
                  case MEASUREMENT:
                  case TEXT:
                     this.drawInProgressPos = new class_2338(this.mouseBlockPosX, 0, this.mouseBlockPosZ);
                  case HIGHLIGHT:
                  default:
                     if (this.drawingMode == DrawingMode.TEXT) {
                        if (this.drawTextEntryActive) {
                           if (this.drawTextEntryField.method_25405(event.comp_4798(), event.comp_4799())) {
                              return;
                           }

                           this.method_37066(this.drawTextEntryField);
                        }

                        this.drawTextEntryActive = true;
                        this.drawTextEntryField.method_46421(class_3532.method_15340((int)event.comp_4798() - this.drawTextEntryField.method_25368() / 2, 5, this.field_22789 - this.drawTextEntryField.method_25368() - 5));
                        this.drawTextEntryField.method_46419(class_3532.method_15340((int)event.comp_4799() - this.drawTextEntryField.method_25364() / 2, 5, this.field_22790 - this.drawTextEntryField.method_25364() - 5));
                        this.method_25429(this.drawTextEntryField);
                        this.drawTextEntryField.method_1862(true);
                        this.drawTextEntryField.method_1875(0);
                        this.drawTextEntryField.method_47404(class_2561.method_43470("Text:").method_27692(class_124.field_1063));
                        this.method_25395(this.drawTextEntryField);
                     }

                     ((Drawing)ModuleManager.getModule(Drawing.class)).startOperation(Globals.getCurrentDimensionId(), false);
                     cir.setReturnValue(true);
               }
            } else if (event.method_74245() == 1) {
               this.drawingRightClickDown = true;
               ((Drawing)ModuleManager.getModule(Drawing.class)).startOperation(Globals.getCurrentDimensionId(), true);
               cir.setReturnValue(true);
            }

         }
      }
   }

   @Inject(
      method = {"method_25406"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = true
   )
   public void drawingClickReleasedHandler(final class_11909 event, final CallbackInfoReturnable<Boolean> cir) {
      if (this.drawing) {
         if (this.colorPickerActive && (event.method_74245() == 0 || event.method_74245() == 1) && this.drawColorPicker.method_25405(event.comp_4798(), event.comp_4799())) {
            this.drawingLeftClickDown = false;
            this.drawingRightClickDown = false;
            this.drawInProgressPos = null;
         }

         boolean toReturn = super.method_25406(event);
         if (toReturn) {
            cir.setReturnValue(true);
         } else {
            if (event.method_74245() == 0) {
               switch (this.drawingMode) {
                  case LINE_SEGMENT:
                  case INFINITE_LINE:
                     if (this.drawInProgressPos != null) {
                        Line line = ((Drawing)ModuleManager.getModule(Drawing.class)).snap(this.drawInProgressPos.method_10263(), this.drawInProgressPos.method_10260(), this.mouseBlockPosX, this.mouseBlockPosZ, destScale);
                        switch (this.drawingMode) {
                           case LINE_SEGMENT -> ((Drawing)ModuleManager.getModule(Drawing.class)).addLine(line);
                           case INFINITE_LINE -> ((Drawing)ModuleManager.getModule(Drawing.class)).addInfiniteLine(line);
                        }

                        this.drawInProgressPos = null;
                        ((Drawing)ModuleManager.getModule(Drawing.class)).endOperation();
                     }
                     break;
                  case HIGHLIGHT:
                     ((Drawing)ModuleManager.getModule(Drawing.class)).endOperation();
                     break;
                  case ELLIPSE:
                     if (this.drawInProgressPos != null) {
                        Drawing drawingModule = (Drawing)ModuleManager.getModule(Drawing.class);
                        Ellipse ellipse = drawingModule.snapEllipse(this.drawInProgressPos.method_10263(), this.drawInProgressPos.method_10260(), this.mouseBlockPosX, this.mouseBlockPosZ, destScale);
                        if (ellipse != null) {
                           drawingModule.addEllipse(ellipse);
                        }

                        this.drawInProgressPos = null;
                        drawingModule.removeInProgressEllipse();
                        drawingModule.endOperation();
                     }
                     break;
                  case MEASUREMENT:
                     this.drawInProgressPos = null;
               }

               this.drawingLeftClickDown = false;
               cir.setReturnValue(true);
            } else if (event.method_74245() == 1) {
               this.drawingRightClickDown = false;
               if (this.drawInProgressPos != null) {
                  return;
               }

               ((Drawing)ModuleManager.getModule(Drawing.class)).removeLine(this.mouseBlockPosX, this.mouseBlockPosZ);
               ((Drawing)ModuleManager.getModule(Drawing.class)).removeEllipse(this.mouseBlockPosX, this.mouseBlockPosZ);
               ((Drawing)ModuleManager.getModule(Drawing.class)).removeText(this.mouseBlockPosX, this.mouseBlockPosZ, this.getFboScale());
               ((Drawing)ModuleManager.getModule(Drawing.class)).endOperation();
               cir.setReturnValue(true);
            }

         }
      }
   }

   @Unique
   private void xaeroPlus$stopDrawing() {
      this.drawing = false;
      this.drawInProgressPos = null;
      ((Drawing)ModuleManager.getModule(Drawing.class)).endOperation();
      ((Drawing)ModuleManager.getModule(Drawing.class)).removeInProgressLine();
      ((Drawing)ModuleManager.getModule(Drawing.class)).removeInProgressEllipse();
      this.drawingLeftClickDown = false;
      this.drawingRightClickDown = false;
      this.drawTextEntryActive = false;
      this.colorPickerActive = false;
      this.method_37066(this.drawLineSegmentButton);
      this.method_37066(this.drawInfiniteLineButton);
      this.method_37066(this.drawHighlightsButton);
      this.method_37066(this.drawEllipseButton);
      this.method_37066(this.drawTextButton);
      this.method_37066(this.drawColorPickerButton);
      this.method_37066(this.drawColorPicker);
      this.method_37066(this.drawTextEntryField);
      this.method_37066(this.drawMeasurementToolButton);
      this.drawLineSegmentButton.field_22764 = false;
      this.drawInfiniteLineButton.field_22764 = false;
      this.drawHighlightsButton.field_22764 = false;
      this.drawEllipseButton.field_22764 = false;
      this.drawTextButton.field_22764 = false;
      this.drawColorPickerButton.field_22764 = false;
      this.drawColorPicker.field_22764 = false;
      this.drawTextEntryField.field_22764 = false;
      this.drawMeasurementToolButton.field_22764 = false;
      this.method_25423(this.field_22789, this.field_22790);
   }

   @Inject(
      method = {"method_25404"},
      at = {@At("RETURN")},
      remap = true
   )
   public void xaeroplus$drawingModeUndo(final class_11908 event, final CallbackInfoReturnable<Boolean> cir) {
      if (event.method_74240() && event.comp_4795() == 90) {
         ((Drawing)ModuleManager.getModule(Drawing.class)).undoLastOperation();
      }

   }

   @Inject(
      method = {"onInputPress"},
      at = {@At("HEAD")}
   )
   public void panMouseButtonClick(final class_3675.class_307 type, final int code, final CallbackInfoReturnable<Boolean> cir) {
      if (type == class_307.field_1672) {
         if (code == 2) {
            if (Settings.REGISTRY.worldMapUIAdditions.get()) {
               this.pan = true;
               class_310 mc = class_310.method_1551();
               this.panMouseStartX = Misc.getMouseX(mc, true);
               this.panMouseStartY = Misc.getMouseY(mc, true);
            }
         }
      }
   }

   @Inject(
      method = {"onInputRelease"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void panMouseButtonRelease(final class_3675.class_307 type, final int code, final CallbackInfoReturnable<Boolean> cir) {
      if (this.drawing) {
         if (type == class_307.field_1668 && code == 256) {
            this.xaeroPlus$stopDrawing();
            cir.setReturnValue(true);
            return;
         }

         if (this.drawTextEntryActive && type == class_307.field_1668 && code == 257) {
            String value = this.drawTextEntryField.method_1882();
            if (!value.isEmpty()) {
               Text text = new Text(value, this.drawInProgressPos.method_10263(), this.drawInProgressPos.method_10260(), ColorHelper.getColor(255, 255, 255, 255), 1.0F);
               ((Drawing)ModuleManager.getModule(Drawing.class)).addText(text);
               this.xaeroPlus$stopDrawing();
               this.onToggleDrawingButton();
               cir.setReturnValue(true);
               return;
            }
         }
      }

      if (type == class_307.field_1672) {
         if (code == 2) {
            this.pan = false;
         }
      }
   }

   public boolean method_25422() {
      return !this.drawing;
   }

   @Inject(
      method = {"method_25394"},
      at = {@At("HEAD")}
   )
   public void panMapOnRender(final CallbackInfo ci, @Local(argsOnly = true) final float partialTicks) {
      if (this.pan) {
         class_310 mc = class_310.method_1551();
         double mouseX = Misc.getMouseX(mc, true);
         double mouseY = Misc.getMouseY(mc, true);
         double mouseDeltaX = mouseX - this.panMouseStartX;
         double mouseDeltaY = mouseY - this.panMouseStartY;
         double panDeltaX = (double)partialTicks * mouseDeltaX / destScale;
         double panDeltaZ = (double)partialTicks * mouseDeltaY / destScale;
         this.cameraX += panDeltaX;
         this.cameraZ += panDeltaZ;
      }
   }

   @Inject(
      method = {"method_25394"},
      at = {@At(
   value = "FIELD",
   target = "Lxaero/map/common/config/option/WorldMapProfiledConfigOptions;COORDINATES:Lxaero/lib/common/config/option/BooleanConfigOption;",
   opcode = 178,
   ordinal = 0
)},
      remap = true
   )
   public void renderTileSelectionSize(final class_332 guiGraphics, final int scaledMouseX, final int scaledMouseY, final float partialTicks, final CallbackInfo ci) {
      if (Settings.REGISTRY.worldMapUIAdditions.get()) {
         MapTileSelection selection = this.mapTileSelection;
         if (selection != null) {
            int sideLen = Math.abs(selection.getRight() - selection.getLeft()) + 1;
            int heightLen = Math.abs(selection.getBottom() - selection.getTop()) + 1;
            if (sideLen > 1 || heightLen > 1) {
               class_327 var10001 = this.field_22793;
               String var10002 = sideLen + " x " + heightLen;
               Objects.requireNonNull(this.field_22793);
               MapRenderHelper.drawCenteredStringWithBackground(guiGraphics, var10001, var10002, scaledMouseX, scaledMouseY - 9, -1, 0.0F, 0.0F, 0.0F, 0.4F);
            }
         }
      }
   }

   @Inject(
      method = {"method_25404"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = true
   )
   public void onInputPress(final class_11908 event, final CallbackInfoReturnable<Boolean> cir) {
      if (BaritoneHelper.isBaritonePresent()) {
         if (Settings.REGISTRY.worldMapBaritoneGoalHereKeybindSetting.getKeyBinding().method_1417(event)) {
            BaritoneExecutor.goal(this.mouseBlockPosX, this.mouseBlockPosZ);
            cir.setReturnValue(true);
         } else if (Settings.REGISTRY.worldMapBaritonePathHereKeybindSetting.getKeyBinding().method_1417(event)) {
            BaritoneExecutor.path(this.mouseBlockPosX, this.mouseBlockPosZ);
            cir.setReturnValue(true);
         } else if (BaritoneHelper.isBaritoneElytraPresent() && Settings.REGISTRY.worldMapBaritoneElytraHereKeybindSetting.getKeyBinding().method_1417(event)) {
            BaritoneExecutor.elytra(this.mouseBlockPosX, this.mouseBlockPosZ);
            cir.setReturnValue(true);
         }
      }

      if (Settings.REGISTRY.worldMapToggleDrawingKeybindSetting.getKeyBinding().method_1417(event)) {
         this.onToggleDrawingButton();
         cir.setReturnValue(true);
      }
   }

   @Inject(
      method = {"getRightClickOptions"},
      at = {@At("RETURN")},
      remap = false
   )
   public void getRightClickOptionsInject(final CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
      if (Settings.REGISTRY.worldMapUIAdditions.get()) {
         ArrayList<RightClickOption> options = (ArrayList)cir.getReturnValue();
         int index = 3;
         options.add(index++, new RightClickOption("xaeroplus.gui.world_map.copy_coordinates", options.size(), this) {
            public void onAction(final class_437 screen) {
               class_310.method_1551().field_1774.method_1455(MixinGuiMap.this.rightClickX + " " + MixinGuiMap.this.rightClickY + " " + MixinGuiMap.this.rightClickZ);
            }
         });
         if (BaritoneHelper.isBaritonePresent()) {
            final int goalX = this.rightClickX;
            final int goalZ = this.rightClickZ;
            options.add(index++, (new RightClickOption("xaeroplus.gui.world_map.baritone_goal_here", options.size(), this) {
               public void onAction(class_437 screen) {
                  BaritoneExecutor.goal(goalX, goalZ);
               }
            }).setNameFormatArgs(new Object[]{KeyMappingUtils.getKeyName(Settings.REGISTRY.worldMapBaritoneGoalHereKeybindSetting.getKeyBinding())}));
            options.add(index++, (new RightClickOption("xaeroplus.gui.world_map.baritone_path_here", options.size(), this) {
               public void onAction(class_437 screen) {
                  BaritoneExecutor.path(goalX, goalZ);
               }
            }).setNameFormatArgs(new Object[]{KeyMappingUtils.getKeyName(Settings.REGISTRY.worldMapBaritonePathHereKeybindSetting.getKeyBinding())}));
            if (BaritoneHelper.isBaritoneElytraPresent()) {
               options.add(index++, (new RightClickOption("xaeroplus.gui.world_map.baritone_elytra_here", options.size(), this) {
                  public void onAction(class_437 screen) {
                     BaritoneExecutor.elytra(goalX, goalZ);
                  }
               }).setNameFormatArgs(new Object[]{KeyMappingUtils.getKeyName(Settings.REGISTRY.worldMapBaritoneElytraHereKeybindSetting.getKeyBinding())}));
            }
         }

         boolean tileSelPresent = this.mapTileSelection != null;
         final int delHighlightMinX = tileSelPresent ? this.mapTileSelection.getLeft() : this.rightClickX;
         final int delHighlightMaxX = tileSelPresent ? this.mapTileSelection.getRight() : this.rightClickX;
         final int delHighlightMinZ = tileSelPresent ? this.mapTileSelection.getTop() : this.rightClickZ;
         final int delHighlightMaxZ = tileSelPresent ? this.mapTileSelection.getBottom() : this.rightClickZ;
         options.add(index++, new RightClickOption("xaeroplus.gui.world_map.delete_highlights", options.size(), this) {
            public void onAction(final class_437 screen) {
               class_5321<class_1937> dim = Globals.getCurrentDimensionId();
               LongList toRemove = new LongArrayList((delHighlightMaxX - delHighlightMinX + 1) * (delHighlightMaxZ - delHighlightMinZ + 1));

               for(int x = delHighlightMinX; x <= delHighlightMaxX; ++x) {
                  for(int z = delHighlightMinZ; z <= delHighlightMaxZ; ++z) {
                     toRemove.add(ChunkUtils.chunkPosToLong(x, z));
                     ((Drawing)ModuleManager.getModule(Drawing.class)).removeLine(ChunkUtils.chunkCoordToCoord(x), ChunkUtils.chunkCoordToCoord(z));
                     ((Drawing)ModuleManager.getModule(Drawing.class)).removeEllipse(ChunkUtils.chunkCoordToCoord(x), ChunkUtils.chunkCoordToCoord(z));
                     ((Drawing)ModuleManager.getModule(Drawing.class)).removeText(ChunkUtils.chunkCoordToCoord(x), ChunkUtils.chunkCoordToCoord(z), 1.0F);
                  }
               }

               ((Drawing)ModuleManager.getModule(Drawing.class)).removeHighlights(toRemove);
               Breadcrumbs breadcrumbs = (Breadcrumbs)ModuleManager.getModule(Breadcrumbs.class);
               if (breadcrumbs.isEnabled()) {
                  breadcrumbs.breadcrumbsCache.get().removeHighlights(toRemove, dim);
               }

               LiquidNewChunks liquidNewChunks = (LiquidNewChunks)ModuleManager.getModule(LiquidNewChunks.class);
               if (liquidNewChunks.isEnabled()) {
                  liquidNewChunks.newChunksCache.get().removeHighlights(toRemove, dim);
                  liquidNewChunks.inverseNewChunksCache.get().removeHighlights(toRemove, dim);
               }

               OldBiomes oldbiomes = (OldBiomes)ModuleManager.getModule(OldBiomes.class);
               if (oldbiomes.isEnabled()) {
                  oldbiomes.oldBiomesCache.get().removeHighlights(toRemove, dim);
               }

               OldChunks oldChunks = (OldChunks)ModuleManager.getModule(OldChunks.class);
               if (oldChunks.isEnabled()) {
                  oldChunks.oldChunksCache.get().removeHighlights(toRemove, dim);
                  oldChunks.modernChunksCache.get().removeHighlights(toRemove, dim);
               }

               PaletteNewChunks paletteNewChunks = (PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class);
               if (paletteNewChunks.isEnabled()) {
                  paletteNewChunks.newChunksCache.get().removeHighlights(toRemove, dim);
                  paletteNewChunks.newChunksInverseCache.get().removeHighlights(toRemove, dim);
               }

               Portals portals = (Portals)ModuleManager.getModule(Portals.class);
               if (portals.isEnabled()) {
                  portals.portalsCache.get().removeHighlights(toRemove, dim);
               }

               LavaColumns lavaColumns = (LavaColumns)ModuleManager.getModule(LavaColumns.class);
               if (lavaColumns.isEnabled()) {
                  lavaColumns.lavaColumnsCache.get().removeHighlights(toRemove, dim);
               }

            }
         });
         if (Settings.REGISTRY.disableWaypointSharing.get()) {
            options.removeIf((option) -> ((AccessorRightClickOption)option).invokeGetName().equals("gui.xaero_right_click_map_share_location"));
         }

         if (Settings.REGISTRY.disableTeleportation.get()) {
            options.removeIf((option) -> ((AccessorRightClickOption)option).invokeGetName().equals("gui.xaero_wm_right_click_map_teleport_not_allowed"));
         }

      }
   }

   @Unique
   private void onSwitchDimensionButton(final class_5321<class_1937> newDimId) {
      Globals.switchToDimension(newDimId);
   }
}
