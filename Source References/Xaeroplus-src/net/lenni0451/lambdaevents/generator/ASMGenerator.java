package net.lenni0451.lambdaevents.generator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.lenni0451.lambdaevents.AHandler;
import net.lenni0451.lambdaevents.EventHandler;
import net.lenni0451.lambdaevents.IGenerator;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.wrapper.ASMWrapper;

public class ASMGenerator implements IGenerator {
   @Nonnull
   public AHandler generate(Class<?> owner, @Nullable Object instance, EventHandler annotation, Method method, Class<?> arg) {
      return this.define(owner, instance, annotation, method, arg);
   }

   @Nonnull
   public AHandler generateVirtual(Class<?> owner, @Nullable Object instance, EventHandler annotation, Method method) {
      return this.define(owner, instance, annotation, method, (Class)null);
   }

   private AHandler define(Class<?> owner, @Nullable Object instance, EventHandler annotation, Method method, @Nullable Class<?> arg) {
      String handlerName = ASMWrapper.slash(owner.getPackage().getName()) + "/LambdaEvents$ASMHandler";
      ASMWrapper w = ASMWrapper.create(ASMWrapper.opcode("ACC_PUBLIC"), handlerName, (String)null, ASMWrapper.slash(AHandler.class), (String[])null);
      this.makeConstructor(handlerName, w, instance);
      this.makeCaller(handlerName, w, owner, instance, method, arg);
      Class<?> handlerClazz = w.defineMetafactory(owner);
      return (AHandler)RStream.of(handlerClazz).constructors().by(0).newInstance(new Object[]{owner, instance, annotation});
   }

   private void makeConstructor(String handlerName, ASMWrapper w, @Nullable Object instance) {
      String desc = ASMWrapper.desc(new Class[]{Class.class, Object.class, EventHandler.class}, Void.TYPE);
      boolean isStatic = instance == null;
      if (!isStatic) {
         w.visitField(ASMWrapper.opcode("ACC_PRIVATE"), "instance", ASMWrapper.desc(instance.getClass()), (String)null, (Object)null);
      }

      ASMWrapper.MethodVisitorAccess mv = w.visitMethod(ASMWrapper.opcode("ACC_PUBLIC"), "<init>", desc, (String)null, (String[])null);
      mv.visitVarInsn(ASMWrapper.opcode("ALOAD"), 0);
      mv.visitVarInsn(ASMWrapper.opcode("ALOAD"), 1);
      mv.visitVarInsn(ASMWrapper.opcode("ALOAD"), 2);
      mv.visitVarInsn(ASMWrapper.opcode("ALOAD"), 3);
      mv.visitMethodInsn(ASMWrapper.opcode("INVOKESPECIAL"), ASMWrapper.slash(AHandler.class), "<init>", desc, false);
      if (!isStatic) {
         mv.visitVarInsn(ASMWrapper.opcode("ALOAD"), 0);
         mv.visitVarInsn(ASMWrapper.opcode("ALOAD"), 2);
         mv.visitTypeInsn(ASMWrapper.opcode("CHECKCAST"), ASMWrapper.slash(instance.getClass()));
         mv.visitFieldInsn(ASMWrapper.opcode("PUTFIELD"), handlerName, "instance", ASMWrapper.desc(instance.getClass()));
      }

      mv.visitInsn(ASMWrapper.opcode("RETURN"));
      if (isStatic) {
         mv.visitMaxs(4, 4);
      } else {
         mv.visitMaxs(5, 5);
      }

      mv.visitEnd();
   }

   private void makeCaller(String handlerName, ASMWrapper w, Class<?> owner, @Nullable Object instance, Method method, @Nullable Class<?> arg) {
      boolean isStatic = instance == null;
      boolean isInterface = Modifier.isInterface(owner.getModifiers());
      ASMWrapper.MethodVisitorAccess mv = w.visitMethod(ASMWrapper.opcode("ACC_PUBLIC"), "call", ASMWrapper.desc(new Class[]{Object.class}, Void.TYPE), (String)null, (String[])null);
      if (!isStatic) {
         mv.visitVarInsn(ASMWrapper.opcode("ALOAD"), 0);
         mv.visitFieldInsn(ASMWrapper.opcode("GETFIELD"), handlerName, "instance", ASMWrapper.desc(instance.getClass()));
      }

      if (arg != null) {
         mv.visitVarInsn(ASMWrapper.opcode("ALOAD"), 1);
         mv.visitTypeInsn(ASMWrapper.opcode("CHECKCAST"), ASMWrapper.slash(arg));
      }

      if (isStatic) {
         mv.visitMethodInsn(ASMWrapper.opcode("INVOKESTATIC"), ASMWrapper.slash(owner), method.getName(), ASMWrapper.desc(method), isInterface);
      } else {
         mv.visitMethodInsn(ASMWrapper.opcode(isInterface ? "INVOKEINTERFACE" : "INVOKEVIRTUAL"), ASMWrapper.slash(owner), method.getName(), ASMWrapper.desc(method), isInterface);
      }

      mv.visitInsn(ASMWrapper.opcode("RETURN"));
      mv.visitMaxs(3, 3);
      mv.visitEnd();
   }
}
