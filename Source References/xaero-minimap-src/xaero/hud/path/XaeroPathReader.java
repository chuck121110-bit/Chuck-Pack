package xaero.hud.path;

public class XaeroPathReader {
   public XaeroPath read(String pathString) {
      return this.read(pathString, false);
   }

   public XaeroPath read(String pathString, boolean caseSensitive) {
      String[] pathStringNodes = pathString.split("/");
      if (pathStringNodes.length == 0) {
         return XaeroPath.root("", caseSensitive);
      } else {
         XaeroPath result = XaeroPath.root(pathStringNodes[0], caseSensitive);

         for(int i = 1; i < pathStringNodes.length; ++i) {
            result = result.resolve(pathStringNodes[i]);
         }

         return result;
      }
   }
}
