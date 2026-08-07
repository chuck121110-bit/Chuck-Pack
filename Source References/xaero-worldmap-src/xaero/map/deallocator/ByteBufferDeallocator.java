package xaero.map.deallocator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import xaero.map.WorldMap;

public class ByteBufferDeallocator {
   private boolean usingInvokeCleanerMethod;
   private final String directBufferClassName = "java.nio.DirectByteBuffer";
   private Object theUnsafe;
   private Method invokeCleanerMethod;
   private Method directBufferCleanerMethod;
   private Method cleanerCleanMethod;

   public ByteBufferDeallocator() throws ClassNotFoundException, NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
      try {
         Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
         Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
         theUnsafeField.setAccessible(true);
         this.theUnsafe = theUnsafeField.get((Object)null);
         theUnsafeField.setAccessible(false);
         this.invokeCleanerMethod = unsafeClass.getDeclaredMethod("invokeCleaner", ByteBuffer.class);
         this.usingInvokeCleanerMethod = true;
      } catch (NoSuchFieldException | NoSuchMethodException var4) {
         Class<?> directByteBufferClass = Class.forName("java.nio.DirectByteBuffer");
         this.directBufferCleanerMethod = directByteBufferClass.getDeclaredMethod("cleaner");
         Class<?> cleanerClass = this.directBufferCleanerMethod.getReturnType();
         if (Runnable.class.isAssignableFrom(cleanerClass)) {
            this.cleanerCleanMethod = Runnable.class.getDeclaredMethod("run");
         } else {
            this.cleanerCleanMethod = cleanerClass.getDeclaredMethod("clean");
         }
      }

   }

   public synchronized void deallocate(ByteBuffer buffer, boolean debug) {
      if (buffer != null && buffer.isDirect()) {
         if (this.usingInvokeCleanerMethod) {
            try {
               this.invokeCleanerMethod.invoke(this.theUnsafe, buffer);
            } catch (IllegalAccessException e) {
               this.reportException(e);
            } catch (IllegalArgumentException e) {
               this.reportException(e);
            } catch (InvocationTargetException e) {
               this.reportException(e);
            }
         } else {
            boolean cleanerAccessibleBU = this.directBufferCleanerMethod.isAccessible();
            boolean cleanAccessibleBU = this.cleanerCleanMethod.isAccessible();

            try {
               this.directBufferCleanerMethod.setAccessible(true);
               Object cleaner = this.directBufferCleanerMethod.invoke(buffer);
               if (cleaner != null) {
                  this.cleanerCleanMethod.setAccessible(true);
                  this.cleanerCleanMethod.invoke(cleaner);
               } else if (debug) {
                  WorldMap.LOGGER.info("No cleaner to deallocate a buffer!");
               }
            } catch (IllegalAccessException e) {
               this.reportException(e);
            } catch (IllegalArgumentException e) {
               this.reportException(e);
            } catch (InvocationTargetException e) {
               this.reportException(e);
            }

            this.directBufferCleanerMethod.setAccessible(cleanerAccessibleBU);
            this.cleanerCleanMethod.setAccessible(cleanAccessibleBU);
         }

      }
   }

   private void reportException(Exception e) {
      WorldMap.LOGGER.error("Failed to deallocate a direct byte buffer: ", e);
   }
}
