package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.HabitNavGraph
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.HabitTrackerTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_HABIT_ID = "HABIT_ID"
    }

    private val pendingHabitId = MutableStateFlow<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        extractHabitIdFromIntent(intent)

        setContent {
            HabitTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val navController = rememberNavController()
                    val habitIdToNavigate by pendingHabitId.collectAsState()

                    HabitNavGraph(
                        navController = navController,
                        pendingHabitId = habitIdToNavigate,
                        onPendingHabitHandled = {
                            pendingHabitId.value = null
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractHabitIdFromIntent(intent)
    }

    private fun extractHabitIdFromIntent(intent: Intent?) {
        val habitId = intent?.getLongExtra(EXTRA_HABIT_ID, -1L)
        if (habitId != null && habitId > 0L) {
            pendingHabitId.value = habitId
        }
    }
}
