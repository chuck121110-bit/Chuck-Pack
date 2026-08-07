package xaeroplus.event;

public class ClientTickEvent {
   public static class Pre extends ClientTickEvent {
      public static final Pre INSTANCE = new Pre();
   }

   public static class Post extends ClientTickEvent {
      public static final Post INSTANCE = new Post();
   }

   public static class RenderPre extends ClientTickEvent {
      public static final RenderPre INSTANCE = new RenderPre();
   }
}
