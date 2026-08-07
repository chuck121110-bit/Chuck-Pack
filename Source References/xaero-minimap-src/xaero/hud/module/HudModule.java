package xaero.hud.module;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_634;
import org.apache.commons.lang3.function.TriFunction;
import xaero.common.HudMod;
import xaero.hud.HudSession;
import xaero.hud.pushbox.PushboxHandler;
import xaero.hud.render.module.IModuleRenderer;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.option.BooleanConfigOption;

public final class HudModule<MS extends ModuleSession<MS>> {
   private final class_2960 id;
   private final class_2561 displayName;
   private final TriFunction<HudMod, HudModule<MS>, class_634, MS> sessionFactory;
   private final Supplier<IModuleRenderer<MS>> rendererFactory;
   private final Function<class_437, class_437> configScreenFactory;
   private IModuleRenderer<MS> renderer;
   private ModuleTransform transform;
   private ModuleTransform unconfirmedTransform;
   private PushboxHandler.State pushState;
   private BooleanConfigOption activeOption;

   public HudModule(class_2960 id, class_2561 displayName, TriFunction<HudMod, HudModule<MS>, class_634, MS> sessionFactory, Supplier<IModuleRenderer<MS>> rendererFactory, Function<class_437, class_437> configScreenFactory, BooleanConfigOption activeOption) {
      this.displayName = displayName;
      this.activeOption = activeOption;
      this.id = id;
      this.sessionFactory = sessionFactory;
      this.rendererFactory = rendererFactory;
      this.configScreenFactory = configScreenFactory;
      this.transform = new ModuleTransform();
      this.pushState = new PushboxHandler.State();
   }

   public class_2960 getId() {
      return this.id;
   }

   public boolean isActive(ClientConfigManager configManager) {
      return (Boolean)configManager.getEffective(this.activeOption);
   }

   public void setActive(ClientConfigManager configManager, boolean active) {
      configManager.getCurrentProfile().set(this.activeOption, active);
   }

   public MS getCurrentSession() {
      HudSession hudSession = HudSession.getCurrentSession();
      return (MS)(hudSession == null ? null : hudSession.getSession(this));
   }

   public IModuleRenderer<MS> getRenderer() {
      if (this.renderer == null) {
         this.renderer = (IModuleRenderer)this.rendererFactory.get();
      }

      return this.renderer;
   }

   public ModuleTransform getUsedTransform() {
      if (class_310.method_1551().field_1755 != null) {
         return this.getUnconfirmedTransform();
      } else {
         if (this.unconfirmedTransform != null) {
            this.cancelTransform();
         }

         return this.transform;
      }
   }

   public ModuleTransform getUnconfirmedTransform() {
      if (this.unconfirmedTransform == null) {
         this.unconfirmedTransform = this.transform.copy();
      }

      return this.unconfirmedTransform;
   }

   public void confirmTransform() {
      if (this.unconfirmedTransform != null) {
         this.transform = this.unconfirmedTransform;
         this.unconfirmedTransform = null;
      }
   }

   public ModuleTransform getConfirmedTransform() {
      return this.transform;
   }

   public void setTransform(ModuleTransform transform) {
      this.transform = transform;
      this.unconfirmedTransform = null;
   }

   public void cancelTransform() {
      this.unconfirmedTransform = null;
   }

   public PushboxHandler.State getPushState() {
      return this.pushState;
   }

   public class_2561 getDisplayName() {
      return this.displayName;
   }

   public Function<class_437, class_437> getConfigScreenFactory() {
      return this.configScreenFactory;
   }

   TriFunction<HudMod, HudModule<MS>, class_634, MS> getSessionFactory() {
      return this.sessionFactory;
   }

   void setRenderer(IModuleRenderer<MS> renderer) {
      this.renderer = renderer;
   }
}
