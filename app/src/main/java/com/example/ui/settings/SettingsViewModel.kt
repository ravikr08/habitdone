package com.example.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.HabitApplication
import com.example.data.backup.BackupResult
import com.example.widget.HabitGlanceWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isOperating: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HabitApplication
    private val backupManager = app.backupManager

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun exportBackup(targetUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, successMessage = null, errorMessage = null) }
            when (val result = backupManager.exportToJson(getApplication(), targetUri)) {
                is BackupResult.Success -> {
                    _uiState.update {
                        it.copy(isOperating = false, successMessage = result.message)
                    }
                }
                is BackupResult.Error -> {
                    _uiState.update {
                        it.copy(isOperating = false, errorMessage = result.error)
                    }
                }
            }
        }
    }

    fun importBackup(sourceUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, successMessage = null, errorMessage = null) }
            when (val result = backupManager.importFromJson(getApplication(), sourceUri)) {
                is BackupResult.Success -> {
                    // Refresh widgets
                    runCatching {
                        HabitGlanceWidget().updateAll(getApplication())
                    }
                    _uiState.update {
                        it.copy(isOperating = false, successMessage = result.message)
                    }
                }
                is BackupResult.Error -> {
                    _uiState.update {
                        it.copy(isOperating = false, errorMessage = result.error)
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
