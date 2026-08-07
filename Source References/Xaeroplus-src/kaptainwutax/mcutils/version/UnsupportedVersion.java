package kaptainwutax.mcutils.version;

public class UnsupportedVersion extends RuntimeException {
   public UnsupportedVersion(MCVersion version, String type) {
      String var10001 = String.valueOf(version);
      super("Minecraft " + var10001 + " does not support " + type);
   }
}
