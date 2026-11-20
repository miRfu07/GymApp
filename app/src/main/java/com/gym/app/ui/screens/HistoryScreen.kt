package com.gym.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.app.data.GymDatabase
import com.gym.app.data.WorkoutHistory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    database: GymDatabase,
    onNavigateBack: () -> Unit
) {
    val history by database.workoutHistoryDao().getAllHistory().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf<WorkoutHistory?>(null) }
    var selectedPeriod by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История тренировок") },
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(12.dp)) {
            // «Все» показывает записи; остальные вкладки — сумму за скользящий период.
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Все", "30 дней", "Год", "∞").forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedPeriod == index,
                        onClick = { selectedPeriod = index },
                        label = { Text(label, maxLines = 1) }
                    )
                }
            }
            if (selectedPeriod == 0) {
                if (history.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("История пуста. Завершите первую тренировку!", textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(history, key = { it.id }) { workout ->
                            HistoryCard(workout = workout, onDelete = { showDeleteDialog = workout })
                        }
                    }
                }
            } else {
                val now = System.currentTimeMillis()
                val days = when (selectedPeriod) { 1 -> 30L; 2 -> 365L; else -> null }
                val filtered = history.filter { it.completedAt <= now &&
                    (days == null || it.completedAt >= now - days * 24L * 60 * 60 * 1000) }
                Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Тренировок: ${filtered.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Общее время: ${formatTime(filtered.sumOf { it.totalDuration })}", fontSize = 18.sp)
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { workout ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Удалить запись?") },
            text = { Text("Вы уверены, что хотите удалить эту тренировку из истории?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            database.workoutHistoryDao().deleteHistory(workout)
                            showDeleteDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun HistoryCard(
    workout: WorkoutHistory,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.workoutName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateFormat.format(Date(workout.completedAt)),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Тоннаж",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.1f", workout.totalTonnage)} кг",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = "Время",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTime(workout.totalDuration),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Column {
                    Text(
                        text = "Подходов",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${workout.completedSets.size}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (expanded) "Скрыть детали" else "Показать детали")
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                // В истории показываем фактически запущенные подходы, а не план тренировки.
                workout.exercises.forEachIndexed { index, exercise ->
                    val sets = workout.completedSets.filter { it.exerciseIndex == index }
                    val tonnage = sets.sumOf { it.weight * it.reps }
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(exercise.name, modifier = Modifier.weight(1f), fontSize = 14.sp,
                            fontWeight = FontWeight.Medium)
                        Text("${sets.size} подх. • ${String.format("%.1f", tonnage)} кг", fontSize = 14.sp)
                    }
                }
                val expected = workout.completedSets.sumOf { it.duration }
                val minimum = workout.completedSets.size * 120_000L
                Spacer(modifier = Modifier.height(12.dp))
                Text("Подходов выполнено: ${workout.completedSets.size}")
                Text("Ожидаемое время: ${formatTime(expected)}")
                Text("Минимальное возможное время\nпри 2-минутном подходе: ${formatTime(minimum)}")
                Text("Твоё время", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                TimeDifference("ОВ", workout.totalDuration - expected)
                TimeDifference("2 мин", workout.totalDuration - minimum)

            }
        }
    }
}


// Разница выводится со знаком: быстрее ориентира — зелёным, медленнее — красным.
@Composable
private fun TimeDifference(label: String, difference: Long) {
    val sign = if (difference < 0) "−" else if (difference > 0) "+" else ""
    val color = when {
        difference < 0 -> Color(0xFF2E7D32)
        difference > 0 -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onSurface
    }
    Text("$label: $sign${formatTime(kotlin.math.abs(difference))}", color = color)
}
