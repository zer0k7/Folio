package com.ghost.folio.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ghost.folio.data.model.Article
import com.ghost.folio.data.model.Category
import com.ghost.folio.data.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val featuredArticles: List<Article> = emptyList(),
    val categories: List<Category> = emptyList(),
    val allArticles: List<Article> = emptyList(),
    val selectedCategorySlug: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val repository: ArticleRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val featuredIds = listOf("what-is-a-pixel", "what-is-bootloader", "what-is-latency")

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllArticles(),
        repository.getAllCategories(),
        _selectedCategory
    ) { articles, categories, selectedCategory ->
        val featured = articles.filter { it.id in featuredIds }
        val filtered = if (selectedCategory != null) {
            articles.filter { it.category == selectedCategory }
        } else {
            articles
        }

        HomeUiState(
            featuredArticles = featured,
            categories = categories,
            allArticles = filtered,
            selectedCategorySlug = selectedCategory,
            isLoading = false
        )
    }.catch { e ->
        emit(
            HomeUiState(
                isLoading = false,
                error = e.message
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun selectCategory(slug: String?) {
        _selectedCategory.value = if (_selectedCategory.value == slug) null else slug
    }

    private val _updateInfo = MutableStateFlow<com.ghost.folio.util.updater.AppUpdateInfo?>(null)
    val updateInfo: StateFlow<com.ghost.folio.util.updater.AppUpdateInfo?> = _updateInfo.asStateFlow()

    private val _downloadProgress = MutableStateFlow(-1)
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    fun checkForAppUpdates(context: android.content.Context) {
        kotlinx.coroutines.CoroutineScope(viewModelScope.coroutineContext).launch {
            try {
                val updater = com.ghost.folio.util.updater.GitHubAppUpdater(context.applicationContext)
                val info = updater.checkForUpdate()
                if (info != null && info.isAvailable) {
                    _updateInfo.value = info
                }
            } catch (_: Exception) {
                // Silent fail on network/parsing errors
            }
        }
    }

    fun startDownload(context: android.content.Context) {
        val currentInfo = _updateInfo.value ?: return
        if (_isDownloading.value) return

        _isDownloading.value = true
        _downloadProgress.value = 0

        kotlinx.coroutines.CoroutineScope(viewModelScope.coroutineContext).launch {
            try {
                val updater = com.ghost.folio.util.updater.GitHubAppUpdater(context.applicationContext)
                val apkFile = updater.downloadApk(currentInfo.apkDownloadUrl) { percent, _, _ ->
                    _downloadProgress.value = percent
                }

                if (apkFile != null) {
                    updater.installApk(apkFile)
                    _updateInfo.value = null
                }
            } catch (_: Exception) {
                // Handle download error
            } finally {
                _isDownloading.value = false
                _downloadProgress.value = -1
            }
        }
    }

    fun dismissUpdate(context: android.content.Context) {
        val updater = com.ghost.folio.util.updater.GitHubAppUpdater(context.applicationContext)
        updater.markUpdateChecked()
        _updateInfo.value = null
    }

    class Factory(private val repository: ArticleRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
