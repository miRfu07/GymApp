package com.gym.app.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WorkoutHistoryDao_Impl implements WorkoutHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WorkoutHistory> __insertionAdapterOfWorkoutHistory;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<WorkoutHistory> __deletionAdapterOfWorkoutHistory;

  public WorkoutHistoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWorkoutHistory = new EntityInsertionAdapter<WorkoutHistory>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `workout_history` (`id`,`workoutName`,`exercises`,`completedSets`,`totalTonnage`,`totalDuration`,`completedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WorkoutHistory entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getWorkoutName());
        final String _tmp = __converters.fromExerciseList(entity.getExercises());
        statement.bindString(3, _tmp);
        final String _tmp_1 = __converters.fromCompletedSetList(entity.getCompletedSets());
        statement.bindString(4, _tmp_1);
        statement.bindDouble(5, entity.getTotalTonnage());
        statement.bindLong(6, entity.getTotalDuration());
        statement.bindLong(7, entity.getCompletedAt());
      }
    };
    this.__deletionAdapterOfWorkoutHistory = new EntityDeletionOrUpdateAdapter<WorkoutHistory>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `workout_history` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WorkoutHistory entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object insertHistory(final WorkoutHistory history,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfWorkoutHistory.insertAndReturnId(history);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteHistory(final WorkoutHistory history,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfWorkoutHistory.handle(history);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<WorkoutHistory>> getAllHistory() {
    final String _sql = "SELECT * FROM workout_history ORDER BY completedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"workout_history"}, new Callable<List<WorkoutHistory>>() {
      @Override
      @NonNull
      public List<WorkoutHistory> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWorkoutName = CursorUtil.getColumnIndexOrThrow(_cursor, "workoutName");
          final int _cursorIndexOfExercises = CursorUtil.getColumnIndexOrThrow(_cursor, "exercises");
          final int _cursorIndexOfCompletedSets = CursorUtil.getColumnIndexOrThrow(_cursor, "completedSets");
          final int _cursorIndexOfTotalTonnage = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTonnage");
          final int _cursorIndexOfTotalDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDuration");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final List<WorkoutHistory> _result = new ArrayList<WorkoutHistory>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WorkoutHistory _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWorkoutName;
            _tmpWorkoutName = _cursor.getString(_cursorIndexOfWorkoutName);
            final List<Exercise> _tmpExercises;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfExercises);
            _tmpExercises = __converters.toExerciseList(_tmp);
            final List<CompletedSet> _tmpCompletedSets;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfCompletedSets);
            _tmpCompletedSets = __converters.toCompletedSetList(_tmp_1);
            final double _tmpTotalTonnage;
            _tmpTotalTonnage = _cursor.getDouble(_cursorIndexOfTotalTonnage);
            final long _tmpTotalDuration;
            _tmpTotalDuration = _cursor.getLong(_cursorIndexOfTotalDuration);
            final long _tmpCompletedAt;
            _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            _item = new WorkoutHistory(_tmpId,_tmpWorkoutName,_tmpExercises,_tmpCompletedSets,_tmpTotalTonnage,_tmpTotalDuration,_tmpCompletedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
