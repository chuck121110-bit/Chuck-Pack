package xaeroplus.module.impl;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.lenni0451.lambdaevents.EventHandler;
import org.jetbrains.annotations.NotNull;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.module.Module;

public class TickTaskExecutor extends Module implements Executor {
   public static TickTaskExecutor INSTANCE;
   private final Queue<Runnable> tasks = new ConcurrentLinkedQueue();

   public TickTaskExecutor() {
      INSTANCE = this;
      this.enable();
   }

   @EventHandler
   public void onRenderTick(ClientTickEvent.RenderPre event) {
      while(!this.tasks.isEmpty()) {
         try {
            ((Runnable)this.tasks.poll()).run();
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Caught exception in tick task", e);
         }
      }

   }

   public <V> CompletableFuture<V> submit(Supplier<V> task) {
      Supplier<V> wrapped = this.wrap(task);
      if (this.mc.method_18854()) {
         return CompletableFuture.completedFuture(wrapped.get());
      } else {
         CompletableFuture<V> future = new CompletableFuture();
         this.tasks.add((Runnable)() -> {
            try {
               V result = (V)wrapped.get();
               future.complete(result);
            } catch (Throwable e) {
               future.completeExceptionally(e);
            }

         });
         return future;
      }
   }

   public CompletableFuture<Void> submit(Runnable task) {
      Runnable wrapped = this.wrap(task);
      if (this.mc.method_18854()) {
         wrapped.run();
         return CompletableFuture.completedFuture((Object)null);
      } else {
         CompletableFuture<Void> future = new CompletableFuture();
         this.tasks.add((Runnable)() -> {
            wrapped.run();
            future.complete((Object)null);
         });
         return future;
      }
   }

   public void execute(final @NotNull Runnable command) {
      this.submit(command);
   }

   private <T> Supplier<T> wrap(Supplier<T> task) {
      return () -> {
         try {
            return task.get();
         } catch (Throwable e) {
            XaeroPlus.LOGGER.error("Caught exception in tick task", e);
            return null;
         }
      };
   }

   private Runnable wrap(Runnable task) {
      return () -> {
         try {
            task.run();
         } catch (Throwable e) {
            XaeroPlus.LOGGER.error("Caught exception in tick task", e);
         }

      };
   }
}
