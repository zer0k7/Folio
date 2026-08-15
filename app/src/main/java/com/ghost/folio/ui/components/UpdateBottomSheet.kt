package com.ghost.folio.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.folio.R
import com.ghost.folio.ui.theme.ChampionBlue
import com.ghost.folio.ui.theme.LavenderMist
import com.ghost.folio.ui.theme.MutedDark
import com.ghost.folio.ui.theme.Spacing
import com.ghost.folio.ui.theme.SurfaceDark
import com.ghost.folio.ui.theme.WhiteConvolvulus
import com.ghost.folio.util.updater.AppUpdateInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateBottomSheet(
    updateInfo: AppUpdateInfo,
    isDownloading: Boolean,
    downloadProgress: Int,
    onDownloadClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
) {
    val context = LocalContext.current
    val reduceMotion = remember {
        try {
            val duration = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            duration == 0f
        } catch (_: Exception) {
            false
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (downloadProgress > 0) downloadProgress / 100f else 0f,
        animationSpec = if (reduceMotion) spring() else spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "download_progress_anim"
    )

    ModalBottomSheet(
        onDismissRequest = {
            if (!isDownloading) {
                onDismiss()
            }
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = SurfaceDark,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = Spacing.md, bottom = Spacing.xs)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MutedDark)
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.lg)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Top Row: App Icon + Name/Subtext + Version Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ChampionBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(Spacing.md))

                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = WhiteConvolvulus
                        )
                        Text(
                            text = stringResource(R.string.update_available),
                            style = MaterialTheme.typography.labelMedium,
                            color = MutedDark
                        )
                    }
                }

                // Version Pill Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(LavenderMist.copy(alpha = 0.15f))
                        .padding(horizontal = Spacing.sm, vertical = 4.dp)
                ) {
                    Text(
                        text = "v${updateInfo.latestVersion}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        color = LavenderMist
                    )
                }
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.md),
                thickness = 1.dp,
                color = MutedDark.copy(alpha = 0.20f)
            )

            // Release Notes Section
            Text(
                text = stringResource(R.string.whats_new),
                style = MaterialTheme.typography.labelMedium,
                color = MutedDark
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ChampionBlue.copy(alpha = 0.40f))
                    .padding(Spacing.sm)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = updateInfo.releaseNotes.ifBlank { "Performance improvements and bug fixes." },
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = WhiteConvolvulus
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Download Progress (shown during downloading)
            AnimatedVisibility(
                visible = isDownloading,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = LavenderMist,
                        trackColor = MutedDark.copy(alpha = 0.20f)
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Text(
                        text = stringResource(R.string.downloading_progress, downloadProgress.coerceAtLeast(0)),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = MutedDark,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))
                }
            }

            // Action Buttons
            Button(
                onClick = {
                    if (!isDownloading) {
                        onDownloadClick()
                    }
                },
                enabled = !isDownloading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .semantics { contentDescription = "Download Update" },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderMist,
                    contentColor = ChampionBlue,
                    disabledContainerColor = LavenderMist.copy(alpha = 0.50f),
                    disabledContentColor = ChampionBlue.copy(alpha = 0.70f)
                )
            ) {
                Text(
                    text = if (isDownloading) stringResource(R.string.installing) else stringResource(R.string.download_update),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }

            if (!isDownloading) {
                Spacer(modifier = Modifier.height(Spacing.xs))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .clickable { onDismiss() }
                        .padding(vertical = Spacing.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.remind_later),
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedDark,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
