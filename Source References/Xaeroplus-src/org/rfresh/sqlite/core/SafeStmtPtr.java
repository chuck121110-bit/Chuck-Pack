package org.rfresh.sqlite.core;

import java.sql.SQLException;

public class SafeStmtPtr {
   private final DB db;
   private final long ptr;
   private volatile boolean closed = false;
   private int closedRC;
   private SQLException closeException;

   public SafeStmtPtr(DB db, long ptr) {
      this.db = db;
      this.ptr = ptr;
   }

   public boolean isClosed() {
      return this.closed;
   }

   public int close() throws SQLException {
      synchronized(this.db) {
         return this.internalClose();
      }
   }

   private int internalClose() throws SQLException {
      int var1;
      try {
         if (!this.closed) {
            this.closedRC = this.db.finalize(this, this.ptr);
            var1 = this.closedRC;
            return var1;
         }

         if (this.closeException != null) {
            throw this.closeException;
         }

         var1 = this.closedRC;
      } catch (SQLException ex) {
         this.closeException = ex;
         throw ex;
      } finally {
         this.closed = true;
      }

      return var1;
   }

   public <E extends Throwable> int safeRunInt(SafePtrIntFunction<E> run) throws SQLException, E {
      synchronized(this.db) {
         this.ensureOpen();
         return run.run(this.db, this.ptr);
      }
   }

   public <E extends Throwable> long safeRunLong(SafePtrLongFunction<E> run) throws SQLException, E {
      synchronized(this.db) {
         this.ensureOpen();
         return run.run(this.db, this.ptr);
      }
   }

   public <E extends Throwable> double safeRunDouble(SafePtrDoubleFunction<E> run) throws SQLException, E {
      synchronized(this.db) {
         this.ensureOpen();
         return run.run(this.db, this.ptr);
      }
   }

   public <T, E extends Throwable> T safeRun(SafePtrFunction<T, E> run) throws SQLException, E {
      synchronized(this.db) {
         this.ensureOpen();
         return run.run(this.db, this.ptr);
      }
   }

   public <E extends Throwable> void safeRunConsume(SafePtrConsumer<E> run) throws SQLException, E {
      synchronized(this.db) {
         this.ensureOpen();
         run.run(this.db, this.ptr);
      }
   }

   private void ensureOpen() throws SQLException {
      if (this.closed) {
         throw new SQLException("stmt pointer is closed");
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SafeStmtPtr that = (SafeStmtPtr)o;
         return this.ptr == that.ptr;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Long.hashCode(this.ptr);
   }

   @FunctionalInterface
   public interface SafePtrConsumer<E extends Throwable> {
      void run(DB var1, long var2) throws E;
   }

   @FunctionalInterface
   public interface SafePtrDoubleFunction<E extends Throwable> {
      double run(DB var1, long var2) throws E;
   }

   @FunctionalInterface
   public interface SafePtrFunction<T, E extends Throwable> {
      T run(DB var1, long var2) throws E;
   }

   @FunctionalInterface
   public interface SafePtrIntFunction<E extends Throwable> {
      int run(DB var1, long var2) throws E;
   }

   @FunctionalInterface
   public interface SafePtrLongFunction<E extends Throwable> {
      long run(DB var1, long var2) throws E;
   }
}
