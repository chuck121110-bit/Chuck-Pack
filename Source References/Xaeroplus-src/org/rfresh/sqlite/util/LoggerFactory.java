package org.rfresh.sqlite.util;

import java.util.function.Supplier;
import java.util.logging.Level;

public class LoggerFactory {
   static final boolean USE_SLF4J;

   public static Logger getLogger(Class<?> hostClass) {
      return (Logger)(USE_SLF4J ? new SLF4JLogger(hostClass) : new JDKLogger(hostClass));
   }

   static {
      boolean useSLF4J;
      try {
         Class.forName("org.slf4j.Logger");
         useSLF4J = true;
      } catch (Exception var2) {
         useSLF4J = false;
      }

      USE_SLF4J = useSLF4J;
   }

   private static class JDKLogger implements Logger {
      final java.util.logging.Logger logger;

      public JDKLogger(Class<?> hostClass) {
         this.logger = java.util.logging.Logger.getLogger(hostClass.getCanonicalName());
      }

      public void trace(Supplier<String> message) {
         if (this.logger.isLoggable(Level.FINEST)) {
            this.logger.log(Level.FINEST, (String)message.get());
         }

      }

      public void info(Supplier<String> message) {
         if (this.logger.isLoggable(Level.INFO)) {
            this.logger.log(Level.INFO, (String)message.get());
         }

      }

      public void warn(Supplier<String> message) {
         if (this.logger.isLoggable(Level.WARNING)) {
            this.logger.log(Level.WARNING, (String)message.get());
         }

      }

      public void error(Supplier<String> message, Throwable t) {
         if (this.logger.isLoggable(Level.SEVERE)) {
            this.logger.log(Level.SEVERE, (String)message.get(), t);
         }

      }
   }

   private static class SLF4JLogger implements Logger {
      final org.slf4j.Logger logger;

      SLF4JLogger(Class<?> hostClass) {
         this.logger = org.slf4j.LoggerFactory.getLogger(hostClass);
      }

      public void trace(Supplier<String> message) {
         if (this.logger.isTraceEnabled()) {
            this.logger.trace((String)message.get());
         }

      }

      public void info(Supplier<String> message) {
         if (this.logger.isInfoEnabled()) {
            this.logger.info((String)message.get());
         }

      }

      public void warn(Supplier<String> message) {
         if (this.logger.isWarnEnabled()) {
            this.logger.warn((String)message.get());
         }

      }

      public void error(Supplier<String> message, Throwable t) {
         if (this.logger.isErrorEnabled()) {
            this.logger.error((String)message.get(), t);
         }

      }
   }
}
