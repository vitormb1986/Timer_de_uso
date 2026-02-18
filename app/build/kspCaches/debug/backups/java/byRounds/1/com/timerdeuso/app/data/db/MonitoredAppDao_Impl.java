package com.timerdeuso.app.data.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.timerdeuso.app.data.model.MonitoredApp;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MonitoredAppDao_Impl implements MonitoredAppDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MonitoredApp> __insertionAdapterOfMonitoredApp;

  private final EntityDeletionOrUpdateAdapter<MonitoredApp> __deletionAdapterOfMonitoredApp;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByPackage;

  private final SharedSQLiteStatement __preparedStmtOfSilenceUntilMidnight;

  private final SharedSQLiteStatement __preparedStmtOfResetAllSilenced;

  private final SharedSQLiteStatement __preparedStmtOfSetEnabled;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTimeLimit;

  public MonitoredAppDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMonitoredApp = new EntityInsertionAdapter<MonitoredApp>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `monitored_apps` (`packageName`,`appName`,`timeLimitMinutes`,`isEnabled`,`isSilencedUntilMidnight`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MonitoredApp entity) {
        statement.bindString(1, entity.getPackageName());
        statement.bindString(2, entity.getAppName());
        statement.bindLong(3, entity.getTimeLimitMinutes());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(4, _tmp);
        final int _tmp_1 = entity.isSilencedUntilMidnight() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
      }
    };
    this.__deletionAdapterOfMonitoredApp = new EntityDeletionOrUpdateAdapter<MonitoredApp>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `monitored_apps` WHERE `packageName` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MonitoredApp entity) {
        statement.bindString(1, entity.getPackageName());
      }
    };
    this.__preparedStmtOfDeleteByPackage = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM monitored_apps WHERE packageName = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSilenceUntilMidnight = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE monitored_apps SET isSilencedUntilMidnight = 1 WHERE packageName = ?";
        return _query;
      }
    };
    this.__preparedStmtOfResetAllSilenced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE monitored_apps SET isSilencedUntilMidnight = 0";
        return _query;
      }
    };
    this.__preparedStmtOfSetEnabled = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE monitored_apps SET isEnabled = ? WHERE packageName = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTimeLimit = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE monitored_apps SET timeLimitMinutes = ? WHERE packageName = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final MonitoredApp app) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfMonitoredApp.insert(app);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final MonitoredApp app) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfMonitoredApp.handle(app);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteByPackage(final String packageName) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByPackage.acquire();
    int _argIndex = 1;
    _stmt.bindString(_argIndex, packageName);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteByPackage.release(_stmt);
    }
  }

  @Override
  public void silenceUntilMidnight(final String packageName) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfSilenceUntilMidnight.acquire();
    int _argIndex = 1;
    _stmt.bindString(_argIndex, packageName);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfSilenceUntilMidnight.release(_stmt);
    }
  }

  @Override
  public void resetAllSilenced() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfResetAllSilenced.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfResetAllSilenced.release(_stmt);
    }
  }

  @Override
  public void setEnabled(final String packageName, final boolean enabled) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfSetEnabled.acquire();
    int _argIndex = 1;
    final int _tmp = enabled ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp);
    _argIndex = 2;
    _stmt.bindString(_argIndex, packageName);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfSetEnabled.release(_stmt);
    }
  }

  @Override
  public void updateTimeLimit(final String packageName, final int minutes) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTimeLimit.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, minutes);
    _argIndex = 2;
    _stmt.bindString(_argIndex, packageName);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdateTimeLimit.release(_stmt);
    }
  }

  @Override
  public LiveData<List<MonitoredApp>> getAll() {
    final String _sql = "SELECT * FROM monitored_apps";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"monitored_apps"}, false, new Callable<List<MonitoredApp>>() {
      @Override
      @Nullable
      public List<MonitoredApp> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfTimeLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeLimitMinutes");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfIsSilencedUntilMidnight = CursorUtil.getColumnIndexOrThrow(_cursor, "isSilencedUntilMidnight");
          final List<MonitoredApp> _result = new ArrayList<MonitoredApp>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MonitoredApp _item;
            final String _tmpPackageName;
            _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final int _tmpTimeLimitMinutes;
            _tmpTimeLimitMinutes = _cursor.getInt(_cursorIndexOfTimeLimitMinutes);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final boolean _tmpIsSilencedUntilMidnight;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSilencedUntilMidnight);
            _tmpIsSilencedUntilMidnight = _tmp_1 != 0;
            _item = new MonitoredApp(_tmpPackageName,_tmpAppName,_tmpTimeLimitMinutes,_tmpIsEnabled,_tmpIsSilencedUntilMidnight);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public List<MonitoredApp> getAllSync() {
    final String _sql = "SELECT * FROM monitored_apps";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
      final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
      final int _cursorIndexOfTimeLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeLimitMinutes");
      final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
      final int _cursorIndexOfIsSilencedUntilMidnight = CursorUtil.getColumnIndexOrThrow(_cursor, "isSilencedUntilMidnight");
      final List<MonitoredApp> _result = new ArrayList<MonitoredApp>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final MonitoredApp _item;
        final String _tmpPackageName;
        _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
        final String _tmpAppName;
        _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
        final int _tmpTimeLimitMinutes;
        _tmpTimeLimitMinutes = _cursor.getInt(_cursorIndexOfTimeLimitMinutes);
        final boolean _tmpIsEnabled;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
        _tmpIsEnabled = _tmp != 0;
        final boolean _tmpIsSilencedUntilMidnight;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsSilencedUntilMidnight);
        _tmpIsSilencedUntilMidnight = _tmp_1 != 0;
        _item = new MonitoredApp(_tmpPackageName,_tmpAppName,_tmpTimeLimitMinutes,_tmpIsEnabled,_tmpIsSilencedUntilMidnight);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<MonitoredApp> getActiveSync() {
    final String _sql = "SELECT * FROM monitored_apps WHERE isEnabled = 1 AND isSilencedUntilMidnight = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
      final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
      final int _cursorIndexOfTimeLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeLimitMinutes");
      final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
      final int _cursorIndexOfIsSilencedUntilMidnight = CursorUtil.getColumnIndexOrThrow(_cursor, "isSilencedUntilMidnight");
      final List<MonitoredApp> _result = new ArrayList<MonitoredApp>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final MonitoredApp _item;
        final String _tmpPackageName;
        _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
        final String _tmpAppName;
        _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
        final int _tmpTimeLimitMinutes;
        _tmpTimeLimitMinutes = _cursor.getInt(_cursorIndexOfTimeLimitMinutes);
        final boolean _tmpIsEnabled;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
        _tmpIsEnabled = _tmp != 0;
        final boolean _tmpIsSilencedUntilMidnight;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsSilencedUntilMidnight);
        _tmpIsSilencedUntilMidnight = _tmp_1 != 0;
        _item = new MonitoredApp(_tmpPackageName,_tmpAppName,_tmpTimeLimitMinutes,_tmpIsEnabled,_tmpIsSilencedUntilMidnight);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public MonitoredApp getByPackage(final String packageName) {
    final String _sql = "SELECT * FROM monitored_apps WHERE packageName = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, packageName);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
      final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
      final int _cursorIndexOfTimeLimitMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "timeLimitMinutes");
      final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
      final int _cursorIndexOfIsSilencedUntilMidnight = CursorUtil.getColumnIndexOrThrow(_cursor, "isSilencedUntilMidnight");
      final MonitoredApp _result;
      if (_cursor.moveToFirst()) {
        final String _tmpPackageName;
        _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
        final String _tmpAppName;
        _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
        final int _tmpTimeLimitMinutes;
        _tmpTimeLimitMinutes = _cursor.getInt(_cursorIndexOfTimeLimitMinutes);
        final boolean _tmpIsEnabled;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
        _tmpIsEnabled = _tmp != 0;
        final boolean _tmpIsSilencedUntilMidnight;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsSilencedUntilMidnight);
        _tmpIsSilencedUntilMidnight = _tmp_1 != 0;
        _result = new MonitoredApp(_tmpPackageName,_tmpAppName,_tmpTimeLimitMinutes,_tmpIsEnabled,_tmpIsSilencedUntilMidnight);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
