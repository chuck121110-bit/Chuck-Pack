package org.rfresh.sqlite.core;

public interface Codes {
   int SQLITE_OK = 0;
   int SQLITE_ERROR = 1;
   int SQLITE_INTERNAL = 2;
   int SQLITE_PERM = 3;
   int SQLITE_ABORT = 4;
   int SQLITE_BUSY = 5;
   int SQLITE_LOCKED = 6;
   int SQLITE_NOMEM = 7;
   int SQLITE_READONLY = 8;
   int SQLITE_INTERRUPT = 9;
   int SQLITE_IOERR = 10;
   int SQLITE_CORRUPT = 11;
   int SQLITE_NOTFOUND = 12;
   int SQLITE_FULL = 13;
   int SQLITE_CANTOPEN = 14;
   int SQLITE_PROTOCOL = 15;
   int SQLITE_EMPTY = 16;
   int SQLITE_SCHEMA = 17;
   int SQLITE_TOOBIG = 18;
   int SQLITE_CONSTRAINT = 19;
   int SQLITE_MISMATCH = 20;
   int SQLITE_MISUSE = 21;
   int SQLITE_NOLFS = 22;
   int SQLITE_AUTH = 23;
   int SQLITE_ROW = 100;
   int SQLITE_DONE = 101;
   int SQLITE_INTEGER = 1;
   int SQLITE_FLOAT = 2;
   int SQLITE_TEXT = 3;
   int SQLITE_BLOB = 4;
   int SQLITE_NULL = 5;
}
