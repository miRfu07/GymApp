package com.gym.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gym.app.data.DatabaseProvider
import com.gym.app.ui.screens.*
import com.gym.app.ui.theme.GymAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = DatabaseProvider.getDatabase(applicationContext)

        setContent {
            GymAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GymApp(database)
                }
            }
        }
    }
}

@Composable
fun GymApp(database: com.gym.app.data.GymDatabase) {
    val navController = rememberNavController()
    var editingWorkout by remember { mutableStateOf<com.gym.app.data.Workout?>(null) }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                onNavigateToCreateWorkout = {
                    editingWorkout = null
                    navController.navigate("create_workout")
                },
                onNavigateToStartWorkout = { navController.navigate("start_workout") },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToManageWorkouts = { navController.navigate("manage_workouts") }
            )
        }

        composable("create_workout") {
            CreateWorkoutScreen(
                database = database,
                editingWorkout = editingWorkout,
                onNavigateBack = {
                    editingWorkout = null
                    navController.popBackStack()
                }
            )
        }

        composable("start_workout") {
            StartWorkoutScreen(
                database = database,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("history") {
            HistoryScreen(
                database = database,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("manage_workouts") {
            ManageWorkoutsScreen(
                database = database,
                onNavigateBack = { navController.popBackStack() },
                onEditWorkout = { workout ->
                    editingWorkout = workout
                    navController.navigate("create_workout")
                }
            )
        }
    }
}
