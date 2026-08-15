package com.ghost.folio.ui.screens.explore

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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExploreUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val categoryArticles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ExploreViewModel(
    private val repository: ArticleRepository
) : ViewModel() {

    private val _selectedCategorySlug = MutableStateFlow<String?>(null)
    val selectedCategorySlug: StateFlow<String?> = _selectedCategorySlug.asStateFlow()

    val uiState: StateFlow<ExploreUiState> = combine(
        repository.getAllCategories(),
        repository.getAllArticles(),
        _selectedCategorySlug
    ) { categories, articles, selectedSlug ->
        val selectedCat = categories.find { it.slug == selectedSlug }
        val categoryArticles = if (selectedSlug != null) {
            articles.filter { it.category == selectedSlug }
        } else {
            emptyList()
        }

        ExploreUiState(
            categories = categories,
            selectedCategory = selectedCat,
            categoryArticles = categoryArticles,
            isLoading = false
        )
    }.catch { e ->
        emit(
            ExploreUiState(
                isLoading = false,
                error = e.message
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExploreUiState(isLoading = true)
    )

    private val _exportingCategorySlug = MutableStateFlow<String?>(null)
    val exportingCategorySlug: StateFlow<String?> = _exportingCategorySlug.asStateFlow()

    fun selectCategory(slug: String?) {
        _selectedCategorySlug.value = slug
    }

    fun exportCategoryPdf(
        context: android.content.Context,
        category: Category,
        onSuccess: (java.io.File) -> Unit,
        onError: () -> Unit
    ) {
        if (_exportingCategorySlug.value != null) return
        _exportingCategorySlug.value = category.slug

        viewModelScope.launch {
            try {
                val articles = repository.getArticlesByCategory(category.slug).firstOrNull() ?: emptyList()
                val pdfFile = com.ghost.folio.util.CategoryPdfGenerator.generateCategoryPdf(
                    context = context,
                    category = category,
                    articles = articles
                )
                onSuccess(pdfFile)
            } catch (_: Exception) {
                onError()
            } finally {
                _exportingCategorySlug.value = null
            }
        }
    }

    class Factory(private val repository: ArticleRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExploreViewModel(repository) as T
        }
    }
}
