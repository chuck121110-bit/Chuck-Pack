package xaero.common.exception;

import org.lwjgl.opengl.GL11;

public class OpenGLException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   public OpenGLException(int error) {
      super("OpenGL error: " + error);
   }

   public static void checkGLError() throws OpenGLException {
      int error = GL11.glGetError();
      if (error != 0) {
         throw new OpenGLException(error);
      }
   }
}
