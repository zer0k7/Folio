package com.ghost.folio.ui.screens.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ghost.folio.data.model.Article
import com.ghost.folio.data.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ArticleUiState(
    val article: Article? = null,
    val relatedArticles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ArticleViewModel(
    private val repository: ArticleRepository,
    private val articleId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleUiState(isLoading = true))
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    init {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val article = repository.getArticleById(articleId)
                if (article != null) {
                    val related = mutableListOf<Article>()
                    for (relatedId in article.relatedIds) {
                        try {
                            val relatedArticle = repository.getArticleById(relatedId)
                            if (relatedArticle != null) {
                                related.add(relatedArticle)
                            }
                        } catch (_: Exception) {
                            // Skip individual related article failures
                        }
                    }
                    _uiState.value = ArticleUiState(
                        article = article,
                        relatedArticles = related,
                        isLoading = false
                    )
                } else {
                    _uiState.value = ArticleUiState(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = ArticleUiState(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun toggleSaved() {
        val currentArticle = _uiState.value.article ?: return
        val newSavedState = !currentArticle.isSaved
        viewModelScope.launch {
            try {
                repository.setArticleSaved(currentArticle.id, newSavedState)
                _uiState.value = _uiState.value.copy(
                    article = currentArticle.copy(isSaved = newSavedState)
                )
            } catch (_: Exception) {
                // Silently handle save toggle failure
            }
        }
    }

    class Factory(
        private val repository: ArticleRepository,
        private val articleId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ArticleViewModel(repository, articleId) as T
        }
    }
}
