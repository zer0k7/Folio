package com.ghost.folio.ui.screens.home

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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ghost.folio.R
import com.ghost.folio.ui.components.ArticleCard
import com.ghost.folio.ui.theme.Spacing

import androidx.compose.ui.platform.LocalContext
import com.ghost.folio.ui.components.UpdateBottomSheet

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onArticleClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.checkForAppUpdates(context)
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listBottomPadding = 84.dp + navBarBottomInset
    val featuredCardWidth = (screenWidth * 0.72f).coerceIn(240.dp, 300.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.settings_title),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = listBottomPadding)
            ) {
                // Featured Section
                if (state.featuredArticles.isNotEmpty() && state.selectedCategorySlug == null) {
                    item {
                        Column(modifier = Modifier.padding(top = Spacing.xs)) {
                            Text(
                                text = stringResource(id = R.string.featured_title),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = Spacing.lg),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                modifier = Modifier.padding(vertical = Spacing.xs)
                            ) {
                                items(state.featuredArticles, key = { it.id }) { article ->
                                    ArticleCard(
                                        article = article,
                                        onClick = { onArticleClick(article.id) },
                                        modifier = Modifier.width(featuredCardWidth)
                                    )
                                }
                            }
                        }
                    }
                }

                // Categories Row
                if (state.categories.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(top = Spacing.md)) {
                            Text(
                                text = stringResource(id = R.string.categories_title),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = Spacing.lg),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                modifier = Modifier.padding(vertical = Spacing.xs)
                            ) {
                                items(state.categories, key = { it.slug }) { category ->
                                    val isSelected = state.selectedCategorySlug == category.slug
                                    val chipBg = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                    val chipTextColor = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }

                                    Box(
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 48.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(chipBg)
                                            .clickable { viewModel.selectCategory(category.slug) }
                                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = category.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = chipTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // All Articles List Header
                item {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text = if (state.selectedCategorySlug != null) {
                            state.categories.find { it.slug == state.selectedCategorySlug }?.label
                                ?: stringResource(id = R.string.all_articles_title)
                        } else {
                            stringResource(id = R.string.all_articles_title)
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                    )
                }

                // Articles Feed
                items(state.allArticles, key = { it.id }) { article ->
                    Box(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)) {
                        ArticleCard(
                            article = article,
                            onClick = { onArticleClick(article.id) }
                        )
                    }
                }
            }
        }

        updateInfo?.let { info ->
            UpdateBottomSheet(
                updateInfo = info,
                isDownloading = isDownloading,
                downloadProgress = downloadProgress,
                onDownloadClick = { viewModel.startDownload(context) },
                onDismiss = { viewModel.dismissUpdate(context) }
            )
        }
    }
}
