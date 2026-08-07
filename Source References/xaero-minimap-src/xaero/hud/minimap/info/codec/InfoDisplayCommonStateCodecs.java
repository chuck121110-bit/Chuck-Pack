package xaero.hud.minimap.info.codec;

/** @deprecated */
@Deprecated
public class InfoDisplayCommonStateCodecs {
   /** @deprecated */
   @Deprecated
   public static final InfoDisplayStateCodec<Boolean> BOOLEAN = new InfoDisplayStateCodec<Boolean>((s) -> s.equals("true"), Object::toString, 5);
   /** @deprecated */
   @Deprecated
   public static final InfoDisplayStateCodec<Integer> INTEGER = new InfoDisplayStateCodec<Integer>((s) -> Integer.parseInt(s), Object::toString, "-2147483648".length());
}
