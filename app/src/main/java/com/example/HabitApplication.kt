package com.example

import android.app.Application
import com.example.data.backup.HabitBackupManager
import com.example.data.local.HabitDatabase
import com.example.data.local.InitialDataSeeder
import com.example.data.repository.HabitRepository
import com.example.domain.HabitCompletionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HabitApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { HabitDatabase.getInstance(this) }
    val repository by lazy { HabitRepository(database.habitDao()) }
    val completionUseCase by lazy { HabitCompletionUseCase(repository) }
    val backupManager by lazy { HabitBackupManager(repository) }

    override fun onCreate() {
        super.onCreate()
        instance = this

        applicationScope.launch {
            InitialDataSeeder.seedIfNecessary(this@HabitApplication, database.habitDao())
        }
    }

    companion object {
        lateinit var instance: HabitApplication
            private set
    }
}
