package com.gym.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gym.app.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartWorkoutScreen(
    database: GymDatabase,
    onNavigateBack: () -> Unit
) {
    val workouts by database.workoutDao().getAllWorkouts().collectAsState(initial = emptyList())
    var selectedWorkout by remember { mutableStateOf<Workout?>(null) }
    var isWorkoutActive by remember { mutableStateOf(false) }
    var workoutStartTime by remember { mutableStateOf(0L) }
    var workoutElapsedTime by remember { mutableStateOf(0L) }
    var exerciseTimers by remember { mutableStateOf(listOf<ExerciseTimer>()) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    var showMotivationText by remember { mutableStateOf(false) }
    var showCountdown by remember { mutableStateOf(false) }
    var countdownNumber by remember { mutableStateOf(3) }
    var showExitDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isWorkoutActive) {
        if (isWorkoutActive) {
            while (isWorkoutActive) {
                delay(1000)
                workoutElapsedTime = System.currentTimeMillis() - workoutStartTime
            }
        }
    }

    LaunchedEffect(showMotivationText) {
        if (showMotivationText) {
            delay(2000)
            showMotivationText = false
            showCountdown = true
            countdownNumber = 3
        }
    }

    LaunchedEffect(showCountdown, countdownNumber) {
        if (showCountdown && countdownNumber > 0) {
            delay(1000)
            countdownNumber--
        } else if (showCountdown && countdownNumber == 0) {
            delay(500)
            showCountdown = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Начать тренировку") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isWorkoutActive) {
                            showExitDialog = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
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
        if (!isWorkoutActive) {
            // Выбор тренировки
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Выберите тренировку",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (workouts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет доступных тренировок.\nСоздайте новую тренировку!",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(workouts) { workout ->
                            WorkoutSelectionCard(
                                workout = workout,
                                onSelect = {
                                    selectedWorkout = workout
                                    showMotivationText = true
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // Активная тренировка
            selectedWorkout?.let { workout ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // Общий таймер тренировки
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = workout.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Время тренировки: ${formatTime(workoutElapsedTime)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Список упражнений
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(workout.exercises) { exerciseIndex, exercise ->
                            ActiveExerciseCard(
                                exercise = exercise,
                                exerciseIndex = exerciseIndex,
                                exerciseTimers = exerciseTimers,
                                onStartExercise = { setNumber ->
                                    // Остановить все активные таймеры (включая другие упражнения)
                                    exerciseTimers = exerciseTimers.map {
                                        if (it.isRunning) {
                                            it.copy(
                                                endTime = System.currentTimeMillis(),
                                                isRunning = false
                                            )
                                        } else it
                                    }

                                    // Создать новый таймер только если его еще нет
                                    val timerExists = exerciseTimers.any {
                                        it.exerciseId == exerciseIndex.toLong() && it.setNumber == setNumber
                                    }

                                    if (!timerExists) {
                                        val newTimer = ExerciseTimer(
                                            exerciseId = exerciseIndex.toLong(),
                                            exerciseName = exercise.name,
                                            setNumber = setNumber,
                                            startTime = System.currentTimeMillis()
                                        )
                                        exerciseTimers = exerciseTimers + newTimer
                                    }
                                },
                                onStopExercise = { timer ->
                                    exerciseTimers = exerciseTimers.map {
                                        if (it.exerciseId == timer.exerciseId && it.setNumber == timer.setNumber) {
                                            it.copy(
                                                endTime = System.currentTimeMillis(),
                                                isRunning = false
                                            )
                                        } else it
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка завершения тренировки
                    Button(
                        onClick = {
                            isWorkoutActive = false

                            // Собираем только выполненные подходы
                            val completedSets = exerciseTimers
                                .filter { !it.isRunning && it.endTime != null }
                                .map { timer ->
                                    val exercise = workout.exercises[timer.exerciseId.toInt()]
                                    val setsCount = Exercise.getSetsCount(exercise.reps)
                                    val repsPerSet = exercise.reps.replace("х", "x").replace("*", "x")
                                        .split("x").getOrNull(1)?.toIntOrNull() ?: 0
                                    val weight = Exercise.getWeightForSet(exercise.weight, timer.setNumber, setsCount)

                                    CompletedSet(
                                        exerciseIndex = timer.exerciseId.toInt(),
                                        exerciseName = timer.exerciseName,
                                        setNumber = timer.setNumber,
                                        weight = weight,
                                        reps = repsPerSet,
                                        duration = (timer.endTime!! - timer.startTime)
                                    )
                                }

                            // Считаем реальный тоннаж только по выполненным подходам
                            val totalTonnage = completedSets.sumOf { it.weight * it.reps }

                            scope.launch {
                                database.workoutHistoryDao().insertHistory(
                                    WorkoutHistory(
                                        workoutName = workout.name,
                                        exercises = workout.exercises,
                                        completedSets = completedSets,
                                        totalTonnage = totalTonnage,
                                        totalDuration = workoutElapsedTime
                                    )
                                )
                                showCompletionDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Завершить тренировку", fontSize = 18.sp)
                    }
                }
            }
        }
    }

    if (showCompletionDialog) {
        CompletionDialog(
            onDismiss = {
                showCompletionDialog = false
                onNavigateBack()
            }
        )
    }

    if (showMotivationText) {
        MotivationDialog()
    }

    if (showCountdown && countdownNumber > 0) {
        CountdownOverlay(number = countdownNumber)
    } else if (showCountdown && countdownNumber == 0 && !isWorkoutActive) {
        LaunchedEffect(Unit) {
            isWorkoutActive = true
            workoutStartTime = System.currentTimeMillis()
            workoutElapsedTime = 0L
            exerciseTimers = emptyList()
        }
    }

    if (showExitDialog) {
        ExitWorkoutDialog(
            onConfirm = {
                showExitDialog = false
                isWorkoutActive = false

                // Собираем только выполненные подходы
                val completedSets = exerciseTimers
                    .filter { !it.isRunning && it.endTime != null }
                    .map { timer ->
                        val exercise = selectedWorkout!!.exercises[timer.exerciseId.toInt()]
                        val setsCount = Exercise.getSetsCount(exercise.reps)
                        val repsPerSet = exercise.reps.replace("х", "x").replace("*", "x")
                            .split("x").getOrNull(1)?.toIntOrNull() ?: 0
                        val weight = Exercise.getWeightForSet(exercise.weight, timer.setNumber, setsCount)

                        CompletedSet(
                            exerciseIndex = timer.exerciseId.toInt(),
                            exerciseName = timer.exerciseName,
                            setNumber = timer.setNumber,
                            weight = weight,
                            reps = repsPerSet,
                            duration = (timer.endTime!! - timer.startTime)
                        )
                    }

                // Считаем реальный тоннаж только по выполненным подходам
                val totalTonnage = completedSets.sumOf { it.weight * it.reps }

                scope.launch {
                    if (completedSets.isNotEmpty()) {
                        database.workoutHistoryDao().insertHistory(
                            WorkoutHistory(
                                workoutName = selectedWorkout!!.name,
                                exercises = selectedWorkout!!.exercises,
                                completedSets = completedSets,
                                totalTonnage = totalTonnage,
                                totalDuration = workoutElapsedTime
                            )
                        )
                    }
                    onNavigateBack()
                }
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }
}

@Composable
fun ExitWorkoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Прервать тренировку?") },
        text = { Text("Вы уверены, что хотите прервать тренировку? Прогресс выполненных подходов будет сохранен.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Да, прервать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Продолжить")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSelectionCard(
    workout: Workout,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = workout.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${workout.exercises.size} упражнений",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Упражнения:",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            workout.exercises.forEach { exercise ->
                Text(
                    text = "• ${exercise.name}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ActiveExerciseCard(
    exercise: Exercise,
    exerciseIndex: Int,
    exerciseTimers: List<ExerciseTimer>,
    onStartExercise: (Int) -> Unit,
    onStopExercise: (ExerciseTimer) -> Unit
) {
    val setsCount = Exercise.getSetsCount(exercise.reps)
    val activeTimers = exerciseTimers.filter { it.exerciseId == exerciseIndex.toLong() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = exercise.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Повторы: ${exercise.reps}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Подходы
            for (setNumber in 1..setsCount) {
                val setWeight = Exercise.getWeightForSet(exercise.weight, setNumber, setsCount)
                val timer = activeTimers.find { it.setNumber == setNumber }
                SetTimerRow(
                    setNumber = setNumber,
                    weight = setWeight,
                    timer = timer,
                    onStart = { onStartExercise(setNumber) },
                    onStop = { timer?.let { onStopExercise(it) } }
                )
                if (setNumber < setsCount) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SetTimerRow(
    setNumber: Int,
    weight: Double,
    timer: ExerciseTimer?,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    var elapsedTime by remember { mutableStateOf(0L) }

    LaunchedEffect(timer?.isRunning, timer?.startTime) {
        if (timer?.isRunning == true) {
            while (true) {
                elapsedTime = System.currentTimeMillis() - (timer?.startTime ?: System.currentTimeMillis())
                delay(100)
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Подход $setNumber",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${String.format("%.1f", weight)} кг",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (timer != null) {
                val displayTime = if (timer.isRunning) {
                    elapsedTime
                } else {
                    (timer.endTime ?: timer.startTime) - timer.startTime
                }
                Text(
                    text = formatTime(displayTime),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (timer.isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
            }

            if (timer == null) {
                Button(
                    onClick = onStart,
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Начать")
                }
            } else if (timer.isRunning) {
                Button(
                    onClick = onStop,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Стоп")
                }
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Завершено",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun CompletionDialog(onDismiss: () -> Unit) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale.value),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉",
                    fontSize = 80.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ты красавчик!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Хорошо поработал!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Отлично!", fontSize = 18.sp)
                }
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

@Composable
fun MotivationDialog() {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .scale(scale.value),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💪",
                    fontSize = 80.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ну что, давай поработаем?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun CountdownOverlay(number: Int) {
    val scale = remember { Animatable(0.8f) }
    val backgroundColor = when (number) {
        3 -> Color(0xFFE57373).copy(alpha = 0.3f) // Матовый красный
        2 -> Color(0xFFBA68C8).copy(alpha = 0.3f) // Матовый фиолетовый
        1 -> Color(0xFF81C784).copy(alpha = 0.3f) // Матовый зелёный
        else -> Color.Transparent
    }

    LaunchedEffect(number) {
        scale.snapTo(0.8f)
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 200.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.scale(scale.value)
        )
    }
}
