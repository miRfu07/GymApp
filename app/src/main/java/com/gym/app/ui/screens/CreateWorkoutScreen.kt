package com.gym.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.app.data.Exercise
import com.gym.app.data.GymDatabase
import com.gym.app.data.Workout
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(
    database: GymDatabase,
    editingWorkout: Workout? = null,
    onNavigateBack: () -> Unit
) {
    var workoutName by remember { mutableStateOf(editingWorkout?.name ?: "") }
    var exercises by remember { mutableStateOf(editingWorkout?.exercises ?: listOf<Exercise>()) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var editingExerciseIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editingWorkout != null) "Редактировать тренировку" else "Создать тренировку") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = workoutName,
                onValueChange = { workoutName = it },
                label = { Text("Название тренировки") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Упражнения",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        editingExerciseIndex = null
                        showAddExerciseDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Добавить упражнение")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(exercises) { index, exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onEdit = {
                            editingExerciseIndex = index
                            showAddExerciseDialog = true
                        },
                        onDelete = {
                            exercises = exercises.toMutableList().apply { removeAt(index) }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (workoutName.isNotBlank() && exercises.isNotEmpty()) {
                        scope.launch {
                            if (editingWorkout != null) {
                                database.workoutDao().updateWorkout(
                                    editingWorkout.copy(
                                        name = workoutName,
                                        exercises = exercises
                                    )
                                )
                            } else {
                                database.workoutDao().insertWorkout(
                                    Workout(
                                        name = workoutName,
                                        exercises = exercises
                                    )
                                )
                            }
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = workoutName.isNotBlank() && exercises.isNotEmpty()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (editingWorkout != null) "Обновить тренировку" else "Сохранить тренировку", fontSize = 18.sp)
            }
        }
    }

    if (showAddExerciseDialog) {
        val editingExercise = editingExerciseIndex?.let { exercises[it] }
        AddExerciseDialog(
            exercise = editingExercise,
            onDismiss = { showAddExerciseDialog = false },
            onSave = { exercise ->
                exercises = if (editingExerciseIndex != null) {
                    exercises.toMutableList().apply {
                        set(editingExerciseIndex!!, exercise)
                    }
                } else {
                    exercises + exercise
                }
                showAddExerciseDialog = false
            }
        )
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Повторы: ${exercise.reps} | Вес: ${exercise.weight} кг",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Тоннаж: ${String.format("%.1f", exercise.tonnage)} кг",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AddExerciseDialog(
    exercise: Exercise?,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit
) {
    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var reps by remember { mutableStateOf(exercise?.reps ?: "") }
    var weight by remember { mutableStateOf(exercise?.weight ?: "") }
    var repsError by remember { mutableStateOf<String?>(null) }

    val isRepsValid = remember(reps) {
        if (reps.isBlank()) {
            repsError = null
            false
        } else if (Exercise.validateReps(reps)) {
            repsError = null
            true
        } else {
            repsError = "Формат: 3x10 или 3*10"
            false
        }
    }

    val tonnage = remember(reps, weight) {
        if (isRepsValid && weight.isNotBlank()) {
            Exercise.calculateTonnage(reps, weight)
        } else {
            0.0
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (exercise != null) "Редактировать упражнение" else "Добавить упражнение") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Повторы") },
                    placeholder = { Text("3x10 или 3*10") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = repsError != null,
                    supportingText = {
                        if (repsError != null) {
                            Text(
                                text = repsError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Формат: (подходы)x(повторы)")
                        }
                    }
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Вес (кг)") },
                    placeholder = { Text("60 или 60-65 или 60-65-70-75") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        Text("Один вес или через дефис для разных подходов")
                    }
                )

                if (isRepsValid && weight.isNotBlank()) {
                    Text(
                        text = "Тоннаж: ${String.format("%.1f", tonnage)} кг",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && isRepsValid && weight.isNotBlank()) {
                        onSave(
                            Exercise(
                                id = exercise?.id ?: 0,
                                name = name,
                                reps = reps,
                                weight = weight,
                                tonnage = tonnage
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && isRepsValid && weight.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
