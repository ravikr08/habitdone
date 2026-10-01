package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.detail.HabitDetailScreen
import com.example.ui.edit.AddEditHabitScreen
import com.example.ui.home.HomeScreen
import com.example.ui.settings.SettingsScreen

@Composable
fun HabitNavGraph(
    navController: NavHostController,
    pendingHabitId: Long?,
    onPendingHabitHandled: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle cold start or warm start deep-link navigation
    LaunchedEffect(pendingHabitId) {
        if (pendingHabitId != null && pendingHabitId > 0) {
            navController.navigate(Screen.Detail.createRoute(pendingHabitId)) {
                popUpTo(Screen.Home.route) { inclusive = false }
            }
            onPendingHabitHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDetail = { habitId ->
                    navController.navigate(Screen.Detail.createRoute(habitId))
                },
                onNavigateToAdd = {
                    navController.navigate(Screen.AddEdit.createRoute(null))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: -1L
            HabitDetailScreen(
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.AddEdit.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.AddEdit.route,
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val habitIdArg = backStackEntry.arguments?.getLong("habitId") ?: -1L
            val habitId = if (habitIdArg > 0) habitIdArg else null
            AddEditHabitScreen(
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() },
                onSaved = { savedId ->
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
