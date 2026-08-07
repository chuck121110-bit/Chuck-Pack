package org.rfresh.sqlite;

public interface SQLiteCommitListener {
   void onCommit();

   void onRollback();
}
