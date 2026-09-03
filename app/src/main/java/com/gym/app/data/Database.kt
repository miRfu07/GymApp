package com.gym.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY createdAt DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): Workout?

    @Insert
    suspend fun insertWorkout(workout: Workout): Long

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)
}

@Dao
interface WorkoutHistoryDao {
    @Query("SELECT * FROM workout_history ORDER BY completedAt DESC")
    fun getAllHistory(): Flow<List<WorkoutHistory>>

    @Insert
    suspend fun insertHistory(history: WorkoutHistory): Long

    @Delete
    suspend fun deleteHistory(history: WorkoutHistory)
}

@Database(
    entities = [Workout::class, WorkoutHistory::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GymDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutHistoryDao(): WorkoutHistoryDao
}
