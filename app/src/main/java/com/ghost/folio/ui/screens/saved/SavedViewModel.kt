package com.ghost.folio.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ghost.folio.data.model.Article
import com.ghost.folio.data.repository.ArticleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SavedUiState(
    val savedArticles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class SavedViewModel(
    private val repository: ArticleRepository
) : ViewModel() {

    val uiState: StateFlow<SavedUiState> = repository.getSavedArticles()
        .map { SavedUiState(savedArticles = it, isLoading = false) }
        .catch { e ->
            emit(
                SavedUiState(
                    savedArticles = emptyList(),
                    isLoading = false,
                    error = e.message
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SavedUiState(isLoading = true)
        )

    class Factory(private val repository: ArticleRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SavedViewModel(repository) as T
        }
    }
}
