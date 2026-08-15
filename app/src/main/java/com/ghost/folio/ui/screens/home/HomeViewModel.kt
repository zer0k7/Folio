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

    class Factory(private val repository: ArticleRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
