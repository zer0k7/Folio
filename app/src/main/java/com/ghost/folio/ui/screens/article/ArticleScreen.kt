package com.ghost.folio.ui.screens.article

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.ghost.folio.R
import com.ghost.folio.data.model.Difficulty
import com.ghost.folio.ui.components.ArticleCard
import com.ghost.folio.ui.components.BodyRenderer
import com.ghost.folio.ui.theme.Spacing
import com.ghost.folio.ui.theme.scaled
import com.ghost.folio.util.ArticleImageGenerator
import kotlinx.coroutines.launch

@Composable
fun ArticleScreen(
    viewModel: ArticleViewModel,
    onBack: () -> Unit,
    onRelatedArticleClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSharingImage by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listBottomPadding = Spacing.xl + navBarBottomInset
    val relatedCardWidth = (screenWidth * 0.72f).coerceIn(240.dp, 300.dp)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.statusBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val article = state.article

            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_desc),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = article?.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = Spacing.xs)
                )

                if (article != null) {
                    // Share as Image Button
                    IconButton(
                        onClick = {
                            if (!isSharingImage) {
                                isSharingImage = true
                                scope.launch {
                                    try {
                                        val imageFile = ArticleImageGenerator.generateAndSaveImage(context, article)
                                        val contentUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            imageFile
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "image/png"
                                            putExtra(Intent.EXTRA_STREAM, contentUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        val chooser = Intent.createChooser(
                                            shareIntent,
                                            context.getString(R.string.share_article_image)
                                        )
                                        context.startActivity(chooser)
                                    } catch (_: Exception) {
                                        snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.share_image_error)
                                        )
                                    } finally {
                                        isSharingImage = false
                                    }
                                }
                            }
                        },
                        enabled = !isSharingImage,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        if (isSharingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.IosShare,
                                contentDescription = stringResource(id = R.string.share_article_desc),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Save Bookmark Button
                    IconButton(
                        onClick = { viewModel.toggleSaved() },
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = if (article.isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(id = R.string.save_article_desc),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (article != null) {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.lg,
                        end = Spacing.lg,
                        top = Spacing.xs,
                        bottom = listBottomPadding
                    )
                ) {
                    // Header Tags
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = Spacing.sm, vertical = 2.dp)
                            ) {
                                Text(
                                    text = article.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(Spacing.sm))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                                    .padding(horizontal = Spacing.sm, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (article.difficulty == Difficulty.BASIC) stringResource(R.string.difficulty_basic) else stringResource(R.string.difficulty_intermediate),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        // Article Main Title
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = MaterialTheme.typography.displayLarge.fontSize.scaled
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        // Article Summary
                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize.scaled
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))
                    }

                    // Body Blocks Rendering
                    item {
                        BodyRenderer(
                            bodyBlocks = article.body,
                            snackbarHostState = snackbarHostState
                        )
                    }

                    // Related Articles Section
                    if (state.relatedArticles.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(Spacing.xl))

                            Text(
                                text = stringResource(id = R.string.related_articles_title),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = Spacing.sm)
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(state.relatedArticles, key = { it.id }) { rel ->
                                    ArticleCard(
                                        article = rel,
                                        onClick = { onRelatedArticleClick(rel.id) },
                                        modifier = Modifier.width(relatedCardWidth)
                                    )
                                }
                            }
                        }
                    }

                    // External References Section
                    if (article.relatedLinks.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(Spacing.xl))

                            Text(
                                text = stringResource(id = R.string.external_references_title),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(bottom = Spacing.sm)
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                            ) {
                                article.relatedLinks.forEach { link ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {
                                                }
                                            }
                                            .padding(vertical = Spacing.sm, horizontal = Spacing.xs),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = link.label,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(Spacing.sm))
                                        Icon(
                                            imageVector = Icons.Outlined.OpenInNew,
                                            contentDescription = stringResource(id = R.string.external_references_title),
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Spacing.xl)
        )
    }
}
