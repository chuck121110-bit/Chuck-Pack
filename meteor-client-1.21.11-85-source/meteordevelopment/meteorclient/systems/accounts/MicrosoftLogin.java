package meteordevelopment.meteorclient.systems.accounts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.class_156;
import net.minecraft.class_3545;

public class MicrosoftLogin {
   private static final String CLIENT_ID = "4673b348-3efa-4f6a-bbb6-34e141cdc638";
   private static final int PORT = 9675;
   private static volatile HttpServer server;
   private static volatile Consumer<String> callback;

   private MicrosoftLogin() {
   }

   public static String getRefreshToken(Consumer<String> callback) {
      MicrosoftLogin.callback = callback;
      startServer();
      String url = "https://login.live.com/oauth20_authorize.srf?client_id=4673b348-3efa-4f6a-bbb6-34e141cdc638&response_type=code&redirect_uri=http://127.0.0.1:9675&scope=XboxLive.signin%20offline_access&prompt=select_account";
      class_156.method_668().method_670(url);
      return url;
   }

   public static LoginData login(String refreshToken) {
      AuthTokenResponse res = (AuthTokenResponse)Http.post("https://login.live.com/oauth20_token.srf").bodyForm("client_id=4673b348-3efa-4f6a-bbb6-34e141cdc638&refresh_token=" + refreshToken + "&grant_type=refresh_token&redirect_uri=http://127.0.0.1:9675").sendJson(AuthTokenResponse.class);
      if (res == null) {
         return new LoginData();
      } else {
         String accessToken = res.access_token;
         refreshToken = res.refresh_token;
         XblXstsResponse xblRes = (XblXstsResponse)Http.post("https://user.auth.xboxlive.com/user/authenticate").bodyJson("{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d=" + accessToken + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}").sendJson(XblXstsResponse.class);
         if (xblRes == null) {
            return new LoginData();
         } else {
            XblXstsResponse xstsRes = (XblXstsResponse)Http.post("https://xsts.auth.xboxlive.com/xsts/authorize").bodyJson("{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xblRes.Token + "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}").sendJson(XblXstsResponse.class);
            if (xstsRes == null) {
               return new LoginData();
            } else {
               McResponse mcRes = (McResponse)Http.post("https://api.minecraftservices.com/authentication/login_with_xbox").bodyJson("{\"identityToken\":\"XBL3.0 x=" + xblRes.DisplayClaims.xui[0].uhs + ";" + xstsRes.Token + "\"}").sendJson(McResponse.class);
               if (mcRes == null) {
                  return new LoginData();
               } else {
                  GameOwnershipResponse gameOwnershipRes = (GameOwnershipResponse)Http.get("https://api.minecraftservices.com/entitlements/mcstore").bearer(mcRes.access_token).sendJson(GameOwnershipResponse.class);
                  if (gameOwnershipRes != null && gameOwnershipRes.hasGameOwnership()) {
                     ProfileResponse profileRes = (ProfileResponse)Http.get("https://api.minecraftservices.com/minecraft/profile").bearer(mcRes.access_token).sendJson(ProfileResponse.class);
                     return profileRes == null ? new LoginData() : new LoginData(mcRes.access_token, refreshToken, profileRes.id, profileRes.name);
                  } else {
                     return new LoginData();
                  }
               }
            }
         }
      }
   }

   private static void startServer() {
      if (server == null) {
         try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 9675), 0);
            server.createContext("/", MicrosoftLogin::handleRequest);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            server.start();
         } catch (IOException e) {
            MeteorClient.LOG.error("Error starting Microsoft login server", e);
            stopServer();
         }

      }
   }

   public static void stopServer() {
      if (server != null) {
         server.stop(0);
         server = null;
         callback = null;
      }
   }

   private static void handleRequest(HttpExchange req) throws IOException {
      if (req.getRequestMethod().equals("GET")) {
         List<class_3545<String, String>> query = parseURL(req.getRequestURI().getRawQuery());
         boolean ok = false;

         for(class_3545<String, String> pair : query) {
            if (((String)pair.method_15442()).equals("code")) {
               handleCode((String)pair.method_15441());
               ok = true;
               break;
            }
         }

         if (!ok) {
            writeText(req, "Cannot authenticate.");
            callback.accept((Object)null);
         } else {
            writeText(req, "You may now close this page.");
         }
      }

      stopServer();
   }

   private static void handleCode(String code) {
      AuthTokenResponse res = (AuthTokenResponse)Http.post("https://login.live.com/oauth20_token.srf").bodyForm("client_id=4673b348-3efa-4f6a-bbb6-34e141cdc638&code=" + code + "&grant_type=authorization_code&redirect_uri=http://127.0.0.1:9675").sendJson(AuthTokenResponse.class);
      if (res == null) {
         callback.accept((Object)null);
      } else {
         callback.accept(res.refresh_token);
      }

   }

   private static void writeText(HttpExchange req, String text) throws IOException {
      byte[] responseBody = text.getBytes(StandardCharsets.UTF_8);
      req.sendResponseHeaders(200, (long)responseBody.length);
      OutputStream out = req.getResponseBody();

      try {
         out.write(responseBody);
      } catch (Throwable var7) {
         if (out != null) {
            try {
               out.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (out != null) {
         out.close();
      }

   }

   private static List<class_3545<String, String>> parseURL(String string) {
      List<class_3545<String, String>> query = new ArrayList();
      char[] buf = string.toCharArray();
      int i = 0;

      while(i < buf.length) {
         StringBuilder name = new StringBuilder();

         StringBuilder value;
         for(value = new StringBuilder(); i < buf.length && buf[i] != '&' && buf[i] != ';' && buf[i] != '='; ++i) {
            name.append(buf[i]);
         }

         if (i < buf.length) {
            char ch = buf[i];
            ++i;
            if (ch == '=') {
               while(i < buf.length) {
                  if (buf[i] == '&' || buf[i] == ';') {
                     ++i;
                     break;
                  }

                  value.append(buf[i]);
                  ++i;
               }
            }
         }

         if (!name.isEmpty()) {
            query.add(new class_3545(urlDecode(name.toString()), urlDecode(value.toString())));
         }
      }

      return query;
   }

   private static String urlDecode(String s) {
      if (s == null) {
         return null;
      } else {
         ByteBuffer bb = ByteBuffer.allocate(s.length());
         CharBuffer cb = CharBuffer.wrap(s);

         while(cb.hasRemaining()) {
            char c = cb.get();
            if (c == '%' && cb.remaining() >= 2) {
               char uc = cb.get();
               char lc = cb.get();
               int u = Character.digit(uc, 16);
               int l = Character.digit(lc, 16);
               if (u != -1 && l != -1) {
                  bb.put((byte)((u << 4) + l));
               } else {
                  bb.put((byte)37);
                  bb.put((byte)uc);
                  bb.put((byte)lc);
               }
            } else if (c == '+') {
               bb.put((byte)32);
            } else {
               bb.put((byte)c);
            }
         }

         bb.flip();
         return StandardCharsets.UTF_8.decode(bb).toString();
      }
   }

   public static class LoginData {
      public String mcToken;
      public String newRefreshToken;
      public String uuid;
      public String username;

      public LoginData() {
      }

      public LoginData(String mcToken, String newRefreshToken, String uuid, String username) {
         this.mcToken = mcToken;
         this.newRefreshToken = newRefreshToken;
         this.uuid = uuid;
         this.username = username;
      }

      public boolean isGood() {
         return this.mcToken != null;
      }
   }

   private static class AuthTokenResponse {
      public String access_token;
      public String refresh_token;
   }

   private static class XblXstsResponse {
      public String Token;
      public DisplayClaims DisplayClaims;

      private static class DisplayClaims {
         private Claim[] xui;

         private static class Claim {
            private String uhs;
         }
      }
   }

   private static class McResponse {
      public String access_token;
   }

   private static class GameOwnershipResponse {
      private Item[] items;

      private boolean hasGameOwnership() {
         boolean hasProduct = false;
         boolean hasGame = false;

         for(Item item : this.items) {
            if (item.name.equals("product_minecraft")) {
               hasProduct = true;
            } else if (item.name.equals("game_minecraft")) {
               hasGame = true;
            }
         }

         return hasProduct && hasGame;
      }

      private static class Item {
         private String name;
      }
   }

   private static class ProfileResponse {
      public String id;
      public String name;
   }
}
