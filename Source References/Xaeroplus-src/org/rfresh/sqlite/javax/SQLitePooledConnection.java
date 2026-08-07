package org.rfresh.sqlite.javax;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import org.rfresh.sqlite.SQLiteConnection;
import org.rfresh.sqlite.jdbc4.JDBC4PooledConnection;

public class SQLitePooledConnection extends JDBC4PooledConnection {
   protected SQLiteConnection physicalConn;
   protected volatile Connection handleConn;
   protected List<ConnectionEventListener> listeners = new ArrayList();

   protected SQLitePooledConnection(SQLiteConnection physicalConn) {
      this.physicalConn = physicalConn;
   }

   public SQLiteConnection getPhysicalConn() {
      return this.physicalConn;
   }

   public void close() throws SQLException {
      if (this.handleConn != null) {
         this.listeners.clear();
         this.handleConn.close();
      }

      if (this.physicalConn != null) {
         try {
            this.physicalConn.close();
         } finally {
            this.physicalConn = null;
         }
      }

   }

   public Connection getConnection() throws SQLException {
      if (this.handleConn != null) {
         this.handleConn.close();
      }

      this.handleConn = (Connection)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Connection.class}, new InvocationHandler(this) {
         volatile boolean isClosed;

         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
               String name = method.getName();
               if (!"close".equals(name)) {
                  if ("isClosed".equals(name)) {
                     if (!this.isClosed) {
                        this.isClosed = (Boolean)method.invoke(SQLitePooledConnection.this.physicalConn, args);
                     }

                     return this.isClosed;
                  } else if (this.isClosed) {
                     throw new SQLException("Connection is closed");
                  } else {
                     return method.invoke(SQLitePooledConnection.this.physicalConn, args);
                  }
               } else if (this.isClosed) {
                  return null;
               } else {
                  if (!SQLitePooledConnection.this.physicalConn.getAutoCommit()) {
                     SQLitePooledConnection.this.physicalConn.rollback();
                  }

                  SQLitePooledConnection.this.physicalConn.setAutoCommit(true);
                  this.isClosed = true;
                  ConnectionEvent event = new ConnectionEvent(SQLitePooledConnection.this);

                  for(int i = SQLitePooledConnection.this.listeners.size() - 1; i >= 0; --i) {
                     ((ConnectionEventListener)SQLitePooledConnection.this.listeners.get(i)).connectionClosed(event);
                  }

                  return null;
               }
            } catch (SQLException var7) {
               if ("database connection closed".equals(var7.getMessage())) {
                  ConnectionEvent event = new ConnectionEvent(SQLitePooledConnection.this, var7);

                  for(int i = SQLitePooledConnection.this.listeners.size() - 1; i >= 0; --i) {
                     ((ConnectionEventListener)SQLitePooledConnection.this.listeners.get(i)).connectionErrorOccurred(event);
                  }
               }

               throw var7;
            } catch (InvocationTargetException ex) {
               throw ex.getCause();
            }
         }
      });
      return this.handleConn;
   }

   public void addConnectionEventListener(ConnectionEventListener listener) {
      this.listeners.add(listener);
   }

   public void removeConnectionEventListener(ConnectionEventListener listener) {
      this.listeners.remove(listener);
   }

   public List<ConnectionEventListener> getListeners() {
      return this.listeners;
   }
}
