package xaero.common.gui;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1074;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_4280;
import net.minecraft.class_437;
import net.minecraft.class_6381;
import net.minecraft.class_6382;
import net.minecraft.class_8016;
import net.minecraft.class_8023;
import net.minecraft.class_8028;
import xaero.common.HudMod;
import xaero.common.settings.ModSettings;
import xaero.hud.minimap.common.config.MinimapConfigConstants;
import xaero.hud.minimap.common.config.info.config.InfoDisplayConfigData;
import xaero.hud.minimap.common.config.info.config.InfoDisplayManagerConfigData;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.info.InfoDisplay;
import xaero.hud.minimap.info.InfoDisplayManager;
import xaero.hud.minimap.info.config.InfoDisplayConfigClientUtils;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.config.EditConfigScreen;
import xaero.lib.client.gui.widget.dropdown.DropDownWidget;
import xaero.lib.client.gui.widget.dropdown.DropDownWidget.Builder;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.option.ConfigOption;

public class GuiInfoDisplayEdit extends ScreenBase {
   private static final int FRAME_TOP_SIZE = 30;
   private static final int FRAME_BOTTOM_SIZE = 61;
   private static final int SELECTION_ITEM_HEIGHT = 24;
   private static final class_2561 HELP_COMPONENT = class_2561.method_43471("gui.xaero_minimap_info_display_manager_help");
   private static final class_2561 SERVER_ENFORCED_COMPONENT;
   private SelectionList selectionList;
   private final InfoDisplayManager manager;
   private List<String> currentOrder;
   private int selected;
   private int subSelected;
   private final Map<String, MoveableEntry<?>> moveableEntries;
   private final boolean clientSide;
   private InfoDisplayManagerConfigData inputConfig;
   private final Config config;
   private final Runnable onChange;
   private final boolean viewingEnforced;
   private boolean madeChanges;

   public GuiInfoDisplayEdit(EditConfigScreen parent, class_437 escape, Config config, Runnable onChange, boolean viewingEnforced) {
      super(parent, escape, class_2561.method_43471("gui.xaero_minimap_info_display_manager"));
      this.config = config;
      this.onChange = onChange;
      this.clientSide = parent.getContext().isClientSide();
      this.viewingEnforced = viewingEnforced;
      this.manager = HudMod.INSTANCE.getMinimap().getInfoDisplays().getManager();
      this.inputConfig = (InfoDisplayManagerConfigData)config.get(MinimapProfiledConfigOptions.INFO_DISPLAY_CONFIG);
      if (this.inputConfig == null || this.inputConfig == InfoDisplayManagerConfigData.EMPTY) {
         this.inputConfig = InfoDisplayConfigClientUtils.createDefaultConfig(this.manager, (ModSettings)null, this.clientSide);
      }

      this.currentOrder = this.manager.adaptOrder(this.inputConfig.getOrderStream());
      this.moveableEntries = new HashMap();
      this.selected = -1;
      this.subSelected = -1;
   }

   protected void method_25426() {
      super.method_25426();
      this.selectionList = new SelectionList();
      this.method_25429(this.selectionList);
      this.method_37063(class_4185.method_46430(class_2561.method_43469("gui.done", new Object[0]), (b) -> this.goBack()).method_46434(this.field_22789 / 2 - 100, this.field_22790 - 34, 200, 20).method_46431());
      if (this.moveableEntries.isEmpty()) {
         for(String id : this.currentOrder) {
            InfoDisplay<?> infoDisplay = this.manager.get(id);
            MoveableEntry<?> moveable = this.createEntryFor(infoDisplay);
            this.moveableEntries.put(id, moveable);
         }
      }

      this.moveableEntries.values().forEach(this::refreshEntry);
   }

   private <T> MoveableEntry<T> createEntryFor(InfoDisplay<T> infoDisplay) {
      MoveableEntry<T> moveable = new MoveableEntry<T>(infoDisplay);
      InfoDisplayConfigData infoDisplayConfig = this.inputConfig.get(infoDisplay.getId());
      if (infoDisplayConfig == null) {
         infoDisplayConfig = InfoDisplayConfigClientUtils.createDefaultConfig(infoDisplay, (ModSettings)null, this.clientSide);
      }

      String configStateString = infoDisplayConfig.getState();
      moveable.textColor = infoDisplayConfig.getTextColor();
      moveable.backgroundColor = infoDisplayConfig.getBackgroundColor();
      moveable.state = (T)(configStateString == null ? null : infoDisplay.getCodec().decode(configStateString, (Path)null, (ConfigOption)null));
      return moveable;
   }

   private <T> void refreshEntry(MoveableEntry<T> moveable) {
      moveable.clearSubElements();
      this.addSubElements(moveable);
   }

   public void onExit(class_437 screen) {
      super.onExit(screen);
      if (this.madeChanges) {
         this.onChange.run();
      }

   }

   private void saveToConfigProfile() {
      InfoDisplayManagerConfigData.Builder builder = InfoDisplayManagerConfigData.Builder.begin();

      for(String id : this.currentOrder) {
         MoveableEntry<?> editorEntry = (MoveableEntry)this.moveableEntries.get(id);
         String stateString = this.encodeState(editorEntry);
         builder.add(id, new InfoDisplayConfigData(editorEntry.backgroundColor, editorEntry.textColor, stateString));
      }

      InfoDisplayManagerConfigData outputConfig = builder.build();
      this.config.set(MinimapProfiledConfigOptions.INFO_DISPLAY_CONFIG, outputConfig);
      this.madeChanges = true;
   }

   private <T> String encodeState(MoveableEntry<T> editorEntry) {
      return editorEntry.state == null ? null : editorEntry.infoDisplay.getCodec().encode(editorEntry.state, (Path)null, (ConfigOption)null);
   }

   public String[] createColorOptions(String symbol, boolean noneOption, boolean nullOption) {
      int firstColorIndex = (noneOption ? 1 : 0) + (nullOption ? 1 : 0);
      String[] options = new String[MinimapConfigConstants.COLOR_NAMES.length + firstColorIndex];
      if (nullOption) {
         options[0] = "~";
      }

      if (noneOption) {
         options[firstColorIndex - 1] = "□□";
      }

      for(int i = 0; i < MinimapConfigConstants.COLOR_NAMES.length; ++i) {
         int var10001 = i + firstColorIndex;
         String var10002 = MinimapConfigConstants.COLOR_CODES[i];
         options[var10001] = "§" + var10002 + symbol;
      }

      return options;
   }

   private <T> void addSubElements(MoveableEntry<T> moveable) {
      boolean includeNull = !this.clientSide || this.viewingEnforced;
      if (includeNull || moveable.state != null && moveable.textColor != null && moveable.backgroundColor != null) {
         class_339 stateWidget = moveable.infoDisplay.createWidget(this.field_22789 / 2 + 150 - 102, 0, 100, 20, moveable, this::saveToConfigProfile, includeNull);
         if (stateWidget != null) {
            moveable.addSubElement(stateWidget);
         }

         int currentSelectedTextColor = moveable.textColor == null ? -1 : moveable.textColor % MinimapConfigConstants.COLOR_NAMES.length;
         if (includeNull) {
            ++currentSelectedTextColor;
         }

         if (currentSelectedTextColor < 0) {
            currentSelectedTextColor = 0;
            moveable.textColor = includeNull ? null : 0;
         }

         DropDownWidget textColorWidget = Builder.begin().setOptions(this.createColorOptions("Aa", false, includeNull)).setX(this.field_22789 / 2 - 147).setW(20).setSelected(currentSelectedTextColor).setContainer(this).setCallback((menu, index) -> {
            if (includeNull && index == 0) {
               moveable.textColor = null;
            } else {
               moveable.textColor = index - (includeNull ? 1 : 0);
            }

            this.saveToConfigProfile();
            return true;
         }).setNarrationTitle(class_2561.method_43471("gui.xaero_dropdown_info_display_text_color")).build();
         moveable.addSubElement(textColorWidget);
         int currentSelectedBackground = moveable.backgroundColor == null ? 0 : (moveable.backgroundColor < 0 ? moveable.backgroundColor : moveable.backgroundColor % MinimapConfigConstants.COLOR_NAMES.length) + 1 + (includeNull ? 1 : 0);
         if (currentSelectedBackground < 0) {
            currentSelectedBackground = 0;
            moveable.backgroundColor = includeNull ? null : -1;
         }

         DropDownWidget backgroundColorWidget = Builder.begin().setOptions(this.createColorOptions("■■", true, includeNull)).setX(this.field_22789 / 2 - 124).setW(20).setSelected(currentSelectedBackground).setContainer(this).setCallback((menu, index) -> {
            if (includeNull && index == 0) {
               moveable.backgroundColor = null;
            } else {
               moveable.backgroundColor = index - 1 - (includeNull ? 1 : 0);
            }

            this.saveToConfigProfile();
            return true;
         }).setNarrationTitle(class_2561.method_43471("gui.xaero_dropdown_info_display_background_color")).build();
         moveable.addSubElement(backgroundColorWidget);
         if (this.viewingEnforced) {
            if (stateWidget != null) {
               stateWidget.field_22763 = false;
            }

            textColorWidget.field_22763 = false;
            backgroundColorWidget.field_22763 = false;
         }

      } else {
         throw new IllegalArgumentException();
      }
   }

   public boolean method_25406(class_11909 event) {
      if (this.selectionList != null) {
         this.selectionList.releaseDrag();
      }

      return super.method_25406(event);
   }

   public void method_25395(class_364 guiEventListener) {
      super.method_25395(guiEventListener);
   }

   public void method_25420(class_332 guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.method_25420(guiGraphics, mouseX, mouseY, partialTicks);
      this.selectionList.method_25394(guiGraphics, mouseX, mouseY, partialTicks);
      guiGraphics.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 5, -1);
      if (this.clientSide) {
         guiGraphics.method_27534(this.field_22793, HELP_COMPONENT, this.field_22789 / 2, this.field_22790 - 52, -1);
      }

      if (this.viewingEnforced) {
         guiGraphics.method_27534(this.field_22793, SERVER_ENFORCED_COMPONENT, this.field_22789 / 2, 15, -1);
      }

   }

   static {
      SERVER_ENFORCED_COMPONENT = class_2561.method_43471("gui.xaero_info_display_editor_server_enforced").method_27692(class_124.field_1054);
   }

   public class MoveableEntry<T> {
      private final List<class_339> subElements;
      private final InfoDisplay<T> infoDisplay;
      private T state;
      private Integer textColor;
      private Integer backgroundColor;

      public MoveableEntry(InfoDisplay<T> infoDisplay) {
         this.infoDisplay = infoDisplay;
         this.subElements = new ArrayList();
      }

      public void addSubElement(class_339 widget) {
         this.subElements.add(widget);
      }

      public T getState() {
         return this.state;
      }

      public void setState(T state) {
         this.state = state;
      }

      private void clearSubElements() {
         this.subElements.clear();
      }
   }

   class SelectionList extends class_4280<Entry> {
      private static final class_2561 USAGE_NARRATION = class_2561.method_43471("narration.selection.usage");
      private static final class_2561 LEFT_RIGHT_USAGE = class_2561.method_43471("narration.xaero_ui_list_left_right_usage");
      private boolean dragging;
      private int dragStartX;
      private int dragStartY;
      private int dragged;
      private int draggedOffsetX;
      private int draggedOffsetY;

      public SelectionList() {
         super(GuiInfoDisplayEdit.this.field_22787, GuiInfoDisplayEdit.this.field_22789, GuiInfoDisplayEdit.this.field_22790 - 61 - 30, 30, 24);
         this.createEntries();
         if (GuiInfoDisplayEdit.this.selected != -1) {
            this.method_25395((class_364)this.method_25396().get(GuiInfoDisplayEdit.this.selected));
         }

         this.dragged = -1;
      }

      public boolean method_25370() {
         return GuiInfoDisplayEdit.this.method_25399() == this;
      }

      public void method_25395(class_364 guiEventListener) {
         if (guiEventListener instanceof Entry entry || guiEventListener == null) {
            if (GuiInfoDisplayEdit.this.subSelected != -1) {
               Entry oldSelected = (Entry)this.method_25334();
               if (oldSelected != null) {
                  MoveableEntry<?> moveable = oldSelected.getMoveable();
                  ((class_339)moveable.subElements.get(GuiInfoDisplayEdit.this.subSelected)).method_25365(false);
               }
            }

            GuiInfoDisplayEdit.this.selected = entry == null ? -1 : entry.index;
            GuiInfoDisplayEdit.this.subSelected = -1;
         }

         super.method_25395(guiEventListener);
         if (this.method_25336() == null) {
            this.setSelected((Entry)null);
         }

      }

      public void setSelected(Entry entry) {
         super.method_25313(entry);
      }

      public void method_47399(class_6382 narrationElementOutput) {
         super.method_47399(narrationElementOutput);
         if (this.method_25370()) {
            narrationElementOutput.method_37035(class_6381.field_33791, new class_2561[]{USAGE_NARRATION, LEFT_RIGHT_USAGE});
         }

      }

      private void createEntries() {
         for(int i = 0; i < GuiInfoDisplayEdit.this.currentOrder.size(); ++i) {
            Entry entry = new Entry(i);
            this.method_25321(entry);
         }

      }

      private void releaseDrag() {
         this.dragging = false;
         this.dragged = -1;
      }

      protected int method_65507() {
         return this.field_22758 / 2 + 164;
      }

      public int method_25322() {
         return 300;
      }

      public void method_48579(class_332 guiGraphics, int mouseX, int mouseY, float partialTicks) {
         super.method_48579(guiGraphics, mouseX, mouseY, partialTicks);
         if (this.dragging) {
            Entry draggedEntry = (Entry)this.method_25396().get(this.dragged);
            draggedEntry.renderNonInteractable(guiGraphics, mouseX + this.draggedOffsetX, mouseY + this.draggedOffsetY);
            Entry hoveredEntry = (Entry)this.method_25308((double)mouseX, (double)mouseY);
            int hoveredIndex = hoveredEntry == null ? -1 : hoveredEntry.index;
            if (hoveredIndex != -1 && hoveredIndex != this.dragged) {
               String draggedId = (String)GuiInfoDisplayEdit.this.currentOrder.get(this.dragged);
               int slideDirection = hoveredIndex < this.dragged ? 1 : -1;

               for(int i = this.dragged; i != hoveredIndex; i -= slideDirection) {
                  GuiInfoDisplayEdit.this.currentOrder.set(i, (String)GuiInfoDisplayEdit.this.currentOrder.get(i - slideDirection));
               }

               GuiInfoDisplayEdit.this.currentOrder.set(hoveredIndex, draggedId);
               this.dragged = hoveredIndex;
               GuiInfoDisplayEdit.this.saveToConfigProfile();
            }
         } else if (this.dragged != -1 && (Math.abs(mouseX - this.dragStartX) > 5 || Math.abs(mouseY - this.dragStartY) > 5)) {
            this.dragging = true;
            this.method_25395((class_364)null);
         }

      }

      public class_8016 method_48205(class_8023 focusNavigationEvent) {
         if (focusNavigationEvent instanceof class_8023.class_8024 arrowNavigation) {
            if (arrowNavigation.comp_1191() == class_8028.field_41828 || arrowNavigation.comp_1191() == class_8028.field_41829) {
               return null;
            }
         }

         return super.method_48205(focusNavigationEvent);
      }

      public class Entry extends class_4280.class_4281<Entry> {
         private final int index;
         private int lastRenderX;
         private int lastRenderY;
         private int lastMouseX;
         private int lastMouseY;

         public Entry(int index) {
            this.index = index;
         }

         private void renderNonInteractable(class_332 guiGraphics, int x, int y) {
            String infoDisplayId = (String)GuiInfoDisplayEdit.this.currentOrder.get(this.index);
            InfoDisplay<?> infoDisplay = GuiInfoDisplayEdit.this.manager.get(infoDisplayId);
            guiGraphics.method_27535(GuiInfoDisplayEdit.this.field_22793, infoDisplay.getName(), x + 48, y + 6, -1);
         }

         private MoveableEntry<?> getMoveable() {
            String infoDisplayId = (String)GuiInfoDisplayEdit.this.currentOrder.get(this.index);
            return (MoveableEntry)GuiInfoDisplayEdit.this.moveableEntries.get(infoDisplayId);
         }

         public void method_25343(class_332 guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            int x = this.method_46426();
            int y = this.method_46427() + 2;
            this.lastRenderX = x;
            this.lastRenderY = y;
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            if (!SelectionList.this.dragging || SelectionList.this.dragged != this.index) {
               this.renderNonInteractable(guiGraphics, x, y);
               MoveableEntry<?> moveableEntry = this.getMoveable();

               for(class_339 subElement : moveableEntry.subElements) {
                  subElement.method_46419(y - 2 + 12 - subElement.method_25364() / 2);
                  if (subElement instanceof DropDownWidget) {
                     subElement.method_46419(subElement.method_46427() - 1);
                  }

                  subElement.method_25394(guiGraphics, mouseX, mouseY, partialTicks);
               }

            }
         }

         public boolean method_25402(class_11909 event, boolean doubleClick) {
            double d = event.comp_4798();
            double e = event.comp_4799();
            MoveableEntry<?> moveableEntry = this.getMoveable();

            for(class_339 subElement : moveableEntry.subElements) {
               if (subElement.method_25405(d, e) && subElement.method_25402(event, doubleClick)) {
                  return true;
               }
            }

            if (event.method_74245() == 0) {
               SelectionList.this.dragging = false;
               if (!GuiInfoDisplayEdit.this.clientSide || GuiInfoDisplayEdit.this.viewingEnforced) {
                  return true;
               }

               SelectionList.this.dragged = this.index;
               SelectionList.this.draggedOffsetX = (int)((double)this.lastRenderX - d);
               SelectionList.this.draggedOffsetY = (int)((double)this.lastRenderY - e);
               SelectionList.this.dragStartX = (int)d;
               SelectionList.this.dragStartY = (int)e;
               if (SelectionList.this.method_25334() != this) {
                  return true;
               }

               SelectionList.this.method_25395((class_364)null);
            } else {
               SelectionList.this.method_25395((class_364)null);
            }

            return super.method_25402(event, doubleClick);
         }

         public boolean method_25406(class_11909 event) {
            MoveableEntry<?> moveableEntry = this.getMoveable();

            for(class_339 subElement : moveableEntry.subElements) {
               subElement.method_25406(event);
            }

            return super.method_25406(event);
         }

         public void method_16014(double d, double e) {
            this.lastMouseX = (int)d;
            this.lastMouseY = (int)e;
            MoveableEntry<?> moveableEntry = this.getMoveable();

            for(class_339 subElement : moveableEntry.subElements) {
               if (subElement.method_25405(d, e)) {
                  subElement.method_16014(d, e);
               }
            }

            super.method_16014(d, e);
         }

         public boolean method_25403(class_11909 event, double moveX, double moveY) {
            double d = event.comp_4798();
            double e = event.comp_4799();
            this.lastMouseX = (int)d;
            this.lastMouseY = (int)e;
            MoveableEntry<?> moveableEntry = this.getMoveable();

            for(class_339 subElement : moveableEntry.subElements) {
               if (subElement.method_25405(d, e) && subElement.method_25403(event, moveX, moveY)) {
                  return true;
               }
            }

            return super.method_25403(event, moveX, moveY);
         }

         public boolean method_25401(double d, double e, double f, double g) {
            MoveableEntry<?> moveableEntry = this.getMoveable();

            for(class_339 subElement : moveableEntry.subElements) {
               if (subElement.method_25405(d, e) && subElement.method_25401(d, e, f, g)) {
                  return true;
               }
            }

            return super.method_25401(d, e, f, g);
         }

         public boolean method_25404(class_11908 event) {
            MoveableEntry<?> moveableEntry = this.getMoveable();
            int i = event.comp_4795();
            if (i != 262 && i != 263) {
               if (GuiInfoDisplayEdit.this.subSelected != -1 && ((class_339)moveableEntry.subElements.get(GuiInfoDisplayEdit.this.subSelected)).method_25404(event)) {
                  return true;
               }
            } else {
               if (GuiInfoDisplayEdit.this.subSelected != -1) {
                  ((class_339)moveableEntry.subElements.get(GuiInfoDisplayEdit.this.subSelected)).method_25365(false);
               }

               if (i == 262) {
                  ++GuiInfoDisplayEdit.this.subSelected;
                  if (GuiInfoDisplayEdit.this.subSelected == moveableEntry.subElements.size()) {
                     GuiInfoDisplayEdit.this.subSelected = -1;
                  }
               } else {
                  --GuiInfoDisplayEdit.this.subSelected;
                  if (GuiInfoDisplayEdit.this.subSelected < -1) {
                     GuiInfoDisplayEdit.this.subSelected = moveableEntry.subElements.size() - 1;
                  }
               }

               if (GuiInfoDisplayEdit.this.subSelected != -1) {
                  ((class_339)moveableEntry.subElements.get(GuiInfoDisplayEdit.this.subSelected)).method_25365(true);
               }
            }

            return super.method_25404(event);
         }

         public boolean method_16803(class_11908 event) {
            MoveableEntry<?> moveableEntry = this.getMoveable();
            return GuiInfoDisplayEdit.this.subSelected != -1 && ((class_339)moveableEntry.subElements.get(GuiInfoDisplayEdit.this.subSelected)).method_16803(event) ? true : super.method_16803(event);
         }

         public boolean method_25400(class_11905 event) {
            MoveableEntry<?> moveableEntry = this.getMoveable();
            return GuiInfoDisplayEdit.this.subSelected != -1 && ((class_339)moveableEntry.subElements.get(GuiInfoDisplayEdit.this.subSelected)).method_25400(event) ? true : super.method_25400(event);
         }

         public void method_37020(class_6382 narrationElementOutput) {
            MoveableEntry<?> moveableEntry = this.getMoveable();
            int sub = -1;
            if (GuiInfoDisplayEdit.this.selected == this.index && GuiInfoDisplayEdit.this.subSelected >= 0) {
               sub = GuiInfoDisplayEdit.this.subSelected;
            } else {
               for(int i = 0; i < moveableEntry.subElements.size(); ++i) {
                  if (((class_339)moveableEntry.subElements.get(i)).method_25405((double)this.lastMouseX, (double)this.lastMouseY)) {
                     sub = i;
                  }
               }
            }

            if (sub >= 0) {
               ((class_339)moveableEntry.subElements.get(sub)).method_37020(narrationElementOutput);
            } else {
               super.method_37020(narrationElementOutput);
            }

         }

         public class_2561 method_37006() {
            String infoDisplayId = (String)GuiInfoDisplayEdit.this.currentOrder.get(this.index);
            InfoDisplay<?> infoDisplay = GuiInfoDisplayEdit.this.manager.get(infoDisplayId);
            String narration = infoDisplay.getName().getString();
            return class_2561.method_43470(class_1074.method_4662("narrator.select", new Object[]{narration}));
         }
      }
   }
}
