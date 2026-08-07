package org.rfresh.sqlite.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProcessRunner {
   String runAndWaitFor(String command) throws IOException, InterruptedException {
      Process p = Runtime.getRuntime().exec(command);
      p.waitFor();
      return getProcessOutput(p);
   }

   static String getProcessOutput(Process process) throws IOException {
      InputStream in = process.getInputStream();

      String var5;
      try {
         ByteArrayOutputStream b = new ByteArrayOutputStream();
         byte[] buf = new byte[32];

         int readLen;
         while((readLen = in.read(buf, 0, buf.length)) >= 0) {
            b.write(buf, 0, readLen);
         }

         var5 = b.toString();
      } catch (Throwable var7) {
         if (in != null) {
            try {
               in.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (in != null) {
         in.close();
      }

      return var5;
   }
}
