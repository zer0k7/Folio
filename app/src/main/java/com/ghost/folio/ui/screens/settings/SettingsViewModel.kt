package com.ghost.folio.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ghost.folio.data.local.preferences.SettingsPreferences
import com.ghost.folio.data.local.preferences.ThemeSetting
import com.ghost.folio.data.local.preferences.UpdateFrequency
import com.ghost.folio.data.repository.ArticleRepository
import com.ghost.folio.util.updater.GitHubAppUpdater
import com.ghost.folio.util.updater.UpdateState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class UpdateCheckStatus {
    IDLE,
    CHECKING,
    UP_TO_DATE,
    UPDATE_AVAILABLE
}

data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

data class SettingsUiState(
    val themeSetting: ThemeSetting = ThemeSetting.SYSTEM,
    val fontScale: Float = 1.0f,
    val updateFrequency: UpdateFrequency = UpdateFrequency.DAILY,
    val lastUpdateCheckTime: Long = 0L,
    val updateCheckStatus: UpdateCheckStatus = UpdateCheckStatus.IDLE,
    val readArticlesCount: Int = 0,
    val cachedExportsSizeBytes: Long = 0L,
    val availableUpdateVersion: String? = null
)

class SettingsViewModel(
    private val repository: ArticleRepository,
    private val preferences: SettingsPreferences
) : ViewModel() {

    private val _updateCheckStatus = MutableStateFlow(UpdateCheckStatus.IDLE)
    private val _availableUpdateVersion = MutableStateFlow<String?>(null)
    private val _cachedExportsSizeBytes = MutableStateFlow(0L)

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            preferences.themeSetting,
            preferences.fontScale,
            preferences.updateFrequency,
            preferences.lastUpdateCheckTime
        ) { theme, fontScale, frequency, lastCheck ->
            Tuple4(theme, fontScale, frequency, lastCheck)
        },
        repository.getReadingHistoryCount(),
        _updateCheckStatus,
        _cachedExportsSizeBytes,
        _availableUpdateVersion
    ) { (theme, fontScale, frequency, lastCheck), readCount, status, cacheSize, updateVer ->
        SettingsUiState(
            themeSetting = theme,
            fontScale = fontScale,
            updateFrequency = frequency,
            lastUpdateCheckTime = lastCheck,
            updateCheckStatus = status,
            readArticlesCount = readCount,
            cachedExportsSizeBytes = cacheSize,
            availableUpdateVersion = updateVer
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setThemeSetting(theme: ThemeSetting) {
        viewModelScope.launch {
            preferences.setThemeSetting(theme)
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            preferences.setFontScale(scale)
        }
    }

    fun setUpdateFrequency(frequency: UpdateFrequency) {
        viewModelScope.launch {
            preferences.setUpdateFrequency(frequency)
        }
    }

    fun checkForUpdates(context: Context) {
        if (_updateCheckStatus.value == UpdateCheckStatus.CHECKING) return

        _updateCheckStatus.value = UpdateCheckStatus.CHECKING
        viewModelScope.launch {
            try {
                val result = GitHubAppUpdater.checkForUpdate(context)
                val now = System.currentTimeMillis()
                preferences.setLastUpdateCheckTime(now)

                if (result != null) {
                    _updateCheckStatus.value = UpdateCheckStatus.UPDATE_AVAILABLE
                    _availableUpdateVersion = MutableStateFlow(result.versionName)
                } else {
                    _updateCheckStatus.value = UpdateCheckStatus.UP_TO_DATE
                }
            } catch (_: Exception) {
                _updateCheckStatus.value = UpdateCheckStatus.UP_TO_DATE
            }
        }
    }

    fun clearReadingHistory() {
        viewModelScope.launch {
            repository.clearReadingHistory()
        }
    }

    fun refreshCacheSize(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val totalSize = calculateDirectorySize(File(context.cacheDir, "exports")) +
                    calculateDirectorySize(File(context.cacheDir, "shared_articles"))
            _cachedExportsSizeBytes.value = totalSize
        }
    }

    fun clearCachedExports(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val exportsDir = File(context.cacheDir, "exports")
            val sharedDir = File(context.cacheDir, "shared_articles")
            deleteDirectoryContents(exportsDir)
            deleteDirectoryContents(sharedDir)
            refreshCacheSize(context)
        }
    }

    private fun calculateDirectorySize(dir: File): Long {
        if (!dir.exists()) return 0L
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) calculateDirectorySize(file) else file.length()
        }
        return size
    }

    private fun deleteDirectoryContents(dir: File) {
        if (!dir.exists()) return
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) deleteDirectoryContents(file)
            file.delete()
        }
    }

    class Factory(
        private val repository: ArticleRepository,
        private val preferences: SettingsPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository, preferences) as T
        }
    }
}
