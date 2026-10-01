package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Settings : Screen("settings")
    data object AddEdit : Screen("add_edit?habitId={habitId}") {
        fun createRoute(habitId: Long? = null): String {
            return if (habitId != null) "add_edit?habitId=$habitId" else "add_edit"
        }
    }
    data object Detail : Screen("detail/{habitId}") {
        fun createRoute(habitId: Long): String = "detail/$habitId"
    }
}
