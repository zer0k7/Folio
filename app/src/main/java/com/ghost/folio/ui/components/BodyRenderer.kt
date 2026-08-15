package com.ghost.folio.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.folio.R
import com.ghost.folio.data.model.BodyBlock
import com.ghost.folio.ui.theme.LavenderMist
import com.ghost.folio.ui.theme.MutedDark
import com.ghost.folio.ui.theme.Spacing
import com.ghost.folio.ui.theme.scaled
import kotlinx.coroutines.launch

fun copyTextToClipboard(
    context: Context,
    label: String,
    text: String
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard?.setPrimaryClip(clip)
}

fun isReduceMotionEnabled(context: Context): Boolean {
    return try {
        val animScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
        animScale == 0.0f
    } catch (_: Exception) {
        false
    }
}

@Composable
fun BodyRenderer(
    bodyBlocks: List<BodyBlock>,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        bodyBlocks.forEach { block ->
            when (block) {
                is BodyBlock.Paragraph -> {
                    ParagraphBlockItem(
                        text = block.text,
                        onLongClick = {
                            copyTextToClipboard(context, "Folio Paragraph", block.text)
                            snackbarHostState?.let { host ->
                                scope.launch {
                                    host.showSnackbar(
                                        message = context.getString(R.string.copied_to_clipboard),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    )
                }

                is BodyBlock.Heading -> {
                    Text(
                        text = block.text,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 22.sp.scaled
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is BodyBlock.Definition -> {
                    DefinitionBlockItem(
                        term = block.term,
                        definition = block.definition,
                        onLongClick = {
                            val copyContent = "${block.term}: ${block.definition}"
                            copyTextToClipboard(context, "Folio Definition", copyContent)
                            snackbarHostState?.let { host ->
                                scope.launch {
                                    host.showSnackbar(
                                        message = context.getString(R.string.definition_copied_to_clipboard, block.term),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    )
                }

                is BodyBlock.BulletList -> {
                    BulletListBlockItem(
                        items = block.items,
                        onLongClick = {
                            val copyContent = block.items.joinToString("\n") { "• $it" }
                            copyTextToClipboard(context, "Folio List", copyContent)
                            snackbarHostState?.let { host ->
                                scope.launch {
                                    host.showSnackbar(
                                        message = context.getString(R.string.copied_to_clipboard),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    )
                }

                is BodyBlock.Diagram -> {
                    DiagramView(
                        diagramKey = block.key
                    )
                }

                is BodyBlock.Note -> {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                            )
                            .drawBehind {
                                line(
                                    color = primaryColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }
                            .padding(horizontal = Spacing.md, vertical = Spacing.md)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            Text(
                                text = stringResource(id = R.string.note_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = block.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp.scaled,
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 24.sp.scaled
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                is BodyBlock.Comparison -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(Spacing.md)
                        ) {
                            // Headers
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = Spacing.xs)
                            ) {
                                block.headers.forEach { header ->
                                    Text(
                                        text = header,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 13.sp.scaled
                                        ),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .widthIn(min = 120.dp, max = 220.dp)
                                            .padding(end = Spacing.sm)
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = Spacing.xs)
                            )

                            // Rows
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                block.rows.forEach { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = row.label,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp.scaled
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier
                                                .widthIn(min = 120.dp, max = 220.dp)
                                                .padding(end = Spacing.sm)
                                        )
                                        row.values.forEach { value ->
                                            Text(
                                                text = value,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 14.sp.scaled
                                                ),
                                                color = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier
                                                    .widthIn(min = 120.dp, max = 220.dp)
                                                    .padding(end = Spacing.sm)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DefinitionBlockItem(
    term: String,
    definition: String,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reduceMotion = isReduceMotionEnabled(context)

    val scaleAnim = remember { Animatable(1.0f) }
    val borderAlphaAnim = remember { Animatable(0.0f) }

    val handleLongClick = {
        onLongClick()
        if (!reduceMotion) {
            scope.launch {
                scaleAnim.animateTo(0.97f, animationSpec = tween(durationMillis = 50))
                scaleAnim.animateTo(
                    1.0f,
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            scope.launch {
                borderAlphaAnim.snapTo(1.0f)
                borderAlphaAnim.animateTo(0.0f, animationSpec = tween(durationMillis = 300))
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scaleAnim.value)
            .border(
                width = 1.dp,
                color = LavenderMist.copy(alpha = borderAlphaAnim.value),
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = {},
                onLongClick = handleLongClick
            )
            .semantics {
                contentDescription = "Definition: $term. Long press to copy."
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    text = term,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp.scaled,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = definition,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp.scaled,
                        lineHeight = 26.sp.scaled,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Subtle Copy Hint Icon
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = null,
                tint = MutedDark.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = Spacing.sm, bottom = Spacing.sm)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ParagraphBlockItem(
    text: String,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reduceMotion = isReduceMotionEnabled(context)
    val scaleAnim = remember { Animatable(1.0f) }

    val handleLongClick = {
        onLongClick()
        if (!reduceMotion) {
            scope.launch {
                scaleAnim.animateTo(0.97f, animationSpec = tween(durationMillis = 50))
                scaleAnim.animateTo(
                    1.0f,
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scaleAnim.value)
            .clip(RoundedCornerShape(4.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = handleLongClick
            )
            .semantics {
                contentDescription = "Paragraph: $text. Long press to copy."
            }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp.scaled,
                lineHeight = 26.sp.scaled
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BulletListBlockItem(
    items: List<String>,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reduceMotion = isReduceMotionEnabled(context)
    val scaleAnim = remember { Animatable(1.0f) }

    val handleLongClick = {
        onLongClick()
        if (!reduceMotion) {
            scope.launch {
                scaleAnim.animateTo(0.97f, animationSpec = tween(durationMillis = 50))
                scaleAnim.animateTo(
                    1.0f,
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(scaleAnim.value)
            .clip(RoundedCornerShape(4.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = handleLongClick
            )
            .semantics {
                contentDescription = "List with ${items.size} items. Long press to copy."
            },
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp.scaled,
                        lineHeight = 26.sp.scaled
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = Spacing.sm)
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp.scaled,
                        lineHeight = 26.sp.scaled
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
