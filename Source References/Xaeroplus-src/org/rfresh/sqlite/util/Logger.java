package org.rfresh.sqlite.util;

import java.util.function.Supplier;

public interface Logger {
   void trace(Supplier<String> var1);

   void info(Supplier<String> var1);

   void warn(Supplier<String> var1);

   void error(Supplier<String> var1, Throwable var2);
}
