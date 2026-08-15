package com.ghost.folio.ui.screens.explore

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SettingsEthernet
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.ghost.folio.R
import com.ghost.folio.data.model.Category
import com.ghost.folio.ui.components.ArticleCard
import com.ghost.folio.ui.components.CategoryExportBottomSheet
import com.ghost.folio.ui.theme.Spacing
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private fun getCategoryIcon(slug: String): ImageVector {
    return when (slug) {
        "display" -> Icons.Outlined.Tv
        "android" -> Icons.Outlined.Android
        "networking" -> Icons.Outlined.Router
        "hardware" -> Icons.Outlined.Memory
        "storage" -> Icons.Outlined.Storage
        "security" -> Icons.Outlined.Security
        "os" -> Icons.Outlined.Terminal
        "web" -> Icons.Outlined.Language
        "programming" -> Icons.Outlined.Code
        "electronics" -> Icons.Outlined.ElectricBolt
        "audio" -> Icons.Outlined.Headphones
        "camera" -> Icons.Outlined.CameraAlt
        "gaming" -> Icons.Outlined.SportsEsports
        "formats" -> Icons.Outlined.Description
        "protocols" -> Icons.Outlined.SettingsEthernet
        else -> Icons.Outlined.Memory
    }
}

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onArticleClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val exportingCategorySlug by viewModel.exportingCategorySlug.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var categoryToExport by remember { mutableStateOf<Category?>(null) }
    var activeSnackbarJob by remember { mutableStateOf<Job?>(null) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val columns = if (screenWidth >= 600) 3 else 2
    val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listBottomPadding = 84.dp + navBarBottomInset

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.statusBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.selectedCategory != null) {
                    IconButton(
                        onClick = { viewModel.selectCategory(null) },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .padding(end = Spacing.xs)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_desc),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Text(
                    text = state.selectedCategory?.label ?: stringResource(id = R.string.nav_explore),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.selectedCategory != null) {
                // Filtered Category Article List
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(bottom = listBottomPadding)
                ) {
                    items(state.categoryArticles, key = { it.id }) { article ->
                        Box(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)) {
                            ArticleCard(
                                article = article,
                                onClick = { onArticleClick(article.id) }
                            )
                        }
                    }
                }
            } else {
                // Adaptive Columns Categories Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(
                        start = Spacing.lg,
                        end = Spacing.lg,
                        top = Spacing.xs,
                        bottom = listBottomPadding
                    ),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxSize()
                ) {
                    items(state.categories, key = { it.slug }) { category ->
                        val isExporting = exportingCategorySlug == category.slug

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(136.dp)
                                .clickable { viewModel.selectCategory(category.slug) },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(Spacing.md),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(category.slug),
                                        contentDescription = category.label,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )

                                    if (isExporting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        IconButton(
                                            onClick = { categoryToExport = category },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.FileDownload,
                                                contentDescription = stringResource(R.string.export_category_desc),
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = category.label,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = stringResource(id = R.string.article_count, category.articleCount),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Confirmation Bottom Sheet
        if (categoryToExport != null) {
            val targetCategory = categoryToExport
            if (targetCategory != null) {
                CategoryExportBottomSheet(
                    category = targetCategory,
                    onConfirmExport = {
                        activeSnackbarJob?.cancel()
                        activeSnackbarJob = scope.launch {
                            val exportingJob = launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.exporting_category, targetCategory.label),
                                    duration = SnackbarDuration.Indefinite
                                )
                            }

                            viewModel.exportCategoryPdf(
                                context = context,
                                category = targetCategory,
                                onSuccess = { pdfFile ->
                                    exportingJob.cancel()
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.export_complete),
                                            actionLabel = context.getString(R.string.export_open_action),
                                            duration = SnackbarDuration.Long
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            try {
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.provider",
                                                    pdfFile
                                                )
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "application/pdf"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                val chooser = Intent.createChooser(
                                                    shareIntent,
                                                    context.getString(R.string.share_pdf_title)
                                                )
                                                context.startActivity(chooser)
                                            } catch (_: Exception) {
                                            }
                                        }
                                    }
                                },
                                onError = {
                                    exportingJob.cancel()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.export_failed)
                                        )
                                    }
                                }
                            )
                        }
                    },
                    onDismiss = { categoryToExport = null }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp + navBarBottomInset)
        )
    }
}
