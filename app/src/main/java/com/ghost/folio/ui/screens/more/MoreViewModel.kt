package com.ghost.folio.ui.screens.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ghost.folio.data.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class AppThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

data class MoreUiState(
    val totalArticles: Int = 0,
    val totalCategories: Int = 0,
    val isLoading: Boolean = true
)

class MoreViewModel(
    private val repository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoreUiState())
    val uiState: StateFlow<MoreUiState> = _uiState.asStateFlow()

    init {
        loadMetrics()
    }

    private fun loadMetrics() {
        viewModelScope.launch {
            combine(
                repository.getAllArticles(),
                repository.getAllCategories()
            ) { articles, categories ->
                MoreUiState(
                    totalArticles = articles.size,
                    totalCategories = categories.size,
                    isLoading = false
                )
            }.catch {
                _uiState.value = MoreUiState(isLoading = false)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    class Factory(
        private val repository: ArticleRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MoreViewModel(repository) as T
        }
    }
}
