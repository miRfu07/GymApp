package com.gym.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val reps: String, // Формат: "3x10" или "3*10"
    val weight: String, // Формат: "60" или "60-65" или "60-65-70-75"
    val tonnage: Double // Автоматически рассчитывается
) {
    companion object {
        fun validateReps(reps: String): Boolean {
            val pattern = Regex("^\\d+[x*х]\\d+$")
            return pattern.matches(reps)
        }

        fun parseWeights(weightStr: String, setsCount: Int): List<Double> {
            val weights = weightStr.split("-").mapNotNull { it.toDoubleOrNull() }
            return when {
                weights.isEmpty() -> List(setsCount) { 0.0 }
                weights.size == 1 -> List(setsCount) { weights[0] }
                weights.size == 2 -> {
                    val half = setsCount / 2
                    List(half) { weights[0] } + List(setsCount - half) { weights[1] }
                }
                else -> {
                    if (weights.size >= setsCount) {
                        weights.take(setsCount)
                    } else {
                        weights + List(setsCount - weights.size) { weights.last() }
                    }
                }
            }
        }

        fun calculateTonnage(reps: String, weightStr: String): Double {
            val parts = reps.replace("х", "x").replace("*", "x").split("x")
            if (parts.size != 2) return 0.0

            val sets = parts[0].toIntOrNull() ?: 0
            val repsPerSet = parts[1].toIntOrNull() ?: 0
            val weights = parseWeights(weightStr, sets)

            return weights.sumOf { it * repsPerSet }
        }

        fun getSetsCount(reps: String): Int {
            val parts = reps.replace("х", "x").replace("*", "x").split("x")
            return if (parts.size == 2) {
                parts[0].toIntOrNull() ?: 1
            } else {
                1
            }
        }

        fun getWeightForSet(weightStr: String, setNumber: Int, totalSets: Int): Double {
            val weights = parseWeights(weightStr, totalSets)
            return weights.getOrNull(setNumber - 1) ?: weights.lastOrNull() ?: 0.0
        }
    }
}

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val exercises: List<Exercise>,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_history")
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutName: String,
    val exercises: List<Exercise>,
    val completedSets: List<CompletedSet>, // Реально выполненные подходы
    val totalTonnage: Double,
    val totalDuration: Long, // в миллисекундах
    val completedAt: Long = System.currentTimeMillis()
)

data class CompletedSet(
    val exerciseIndex: Int,
    val exerciseName: String,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val duration: Long
)

data class ExerciseTimer(
    val exerciseId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val startTime: Long,
    var endTime: Long? = null,
    var isRunning: Boolean = true
)

class Converters {
    @TypeConverter
    fun fromExerciseList(value: List<Exercise>): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toExerciseList(value: String): List<Exercise> {
        val gson = Gson()
        val type = object : TypeToken<List<Exercise>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromCompletedSetList(value: List<CompletedSet>): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCompletedSetList(value: String): List<CompletedSet> {
        val gson = Gson()
        val type = object : TypeToken<List<CompletedSet>>() {}.type
        return gson.fromJson(value, type)
    }
}
