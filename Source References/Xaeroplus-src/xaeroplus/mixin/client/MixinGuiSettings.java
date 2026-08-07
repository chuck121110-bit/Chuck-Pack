package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.ArrayList;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_339;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.lib.client.gui.GuiSettings;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.MyTinyButton;
import xaero.lib.common.util.KeySortableByOther;
import xaeroplus.feature.extensions.IXaeroPlusSettingEntry;
import xaeroplus.settings.Settings;

@Mixin(
   value = {GuiSettings.class},
   remap = false
)
public abstract class MixinGuiSettings extends ScreenBase {
   @Shadow
   protected int entriesPerPage;
   @Unique
   private int xaeroPlus$settingEntryWidth = 200;
   @Shadow
   private MyTinyButton nextButton;
   @Shadow
   private MyTinyButton prevButton;

   protected MixinGuiSettings(final class_437 parent, final class_437 escape, final class_2561 titleIn) {
      super(parent, escape, titleIn);
   }

   @Inject(
      method = {"method_25426"},
      at = {@At("HEAD")}
   )
   public void adjustEntriesPerPage(final CallbackInfo ci) {
      this.xaeroPlus$settingEntryWidth = 200;
      this.entriesPerPage = 12;
      if (Settings.REGISTRY.expandSettingEntries.get()) {
         if (this.field_22790 > 350) {
            int extraRows = Math.min((this.field_22790 - 240) / 50, 8);
            this.entriesPerPage = 12 + 2 * extraRows;
         }

         if (this.field_22789 > 800) {
            this.xaeroPlus$settingEntryWidth = 300;
         }
      }

   }

   @Inject(
      method = {"method_25426"},
      at = {@At("RETURN")}
   )
   public void adjustForwardBackButtonPositionsForExtraRows(final CallbackInfo ci) {
      if (Settings.REGISTRY.expandSettingEntries.get()) {
         int extraRows = (this.entriesPerPage - 12) / 2;
         int yAdjust = extraRows * 24;
         this.nextButton.method_46419(this.nextButton.method_46427() + yAdjust);
         this.prevButton.method_46419(this.prevButton.method_46427() + yAdjust);
         this.method_25396().stream().filter((child) -> child instanceof class_4185).map((child) -> (class_4185)child).filter((button) -> button.method_25369().method_10851() instanceof class_2588).filter((button) -> ((class_2588)button.method_25369().method_10851()).method_11022().equals("gui.xaero_back")).findFirst().ifPresent((button) -> button.method_46419(button.method_46427() + yAdjust));
      }
   }

   @Redirect(
      method = {"method_25426"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z"
),
      remap = true
   )
   public boolean settingListToRenderRedirect(final ArrayList instance, final Object entryObject) {
      KeySortableByOther<ISettingEntry> entry = (KeySortableByOther)entryObject;
      ISettingEntry settingEntry = (ISettingEntry)entry.getKey();
      if (settingEntry instanceof IXaeroPlusSettingEntry xaeroPlusSettingEntry) {
         if (!xaeroPlusSettingEntry.getXaeroPlusSetting().isVisible()) {
            return false;
         }
      }

      instance.add(entryObject);
      return false;
   }

   @WrapOperation(
      method = {"method_25426"},
      slice = {@Slice(
   from = @At(
   value = "FIELD",
   opcode = 181,
   target = "Lxaero/lib/client/gui/GuiSettings;foundSomething:Z"
)
)},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/lib/client/gui/ISettingEntry;createWidget(III)Lnet/minecraft/class_339;",
   ordinal = 0
)},
      remap = true
   )
   public class_339 adjustSettingEntryWidth(final ISettingEntry instance, final int x, final int y, final int w, final Operation<class_339> original, @Local(name = {"i"}) int i) {
      if (!Settings.REGISTRY.expandSettingEntries.get()) {
         return (class_339)original.call(new Object[]{instance, x, y, w});
      } else {
         int halfW = this.field_22789 / 2;
         int halfMargin = 5;
         int adjustedX = i % 2 == 0 ? halfW - this.xaeroPlus$settingEntryWidth - halfMargin : halfW + halfMargin;
         return (class_339)original.call(new Object[]{instance, adjustedX, y, this.xaeroPlus$settingEntryWidth});
      }
   }
}
