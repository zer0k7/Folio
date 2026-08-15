package com.ghost.folio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.folio.BuildConfig
import com.ghost.folio.R
import com.ghost.folio.data.changelog.ChangeType
import com.ghost.folio.data.changelog.ChangelogData
import com.ghost.folio.data.changelog.ChangelogItem
import com.ghost.folio.ui.theme.ChampionBlue
import com.ghost.folio.ui.theme.LavenderMist
import com.ghost.folio.ui.theme.MutedDark
import com.ghost.folio.ui.theme.Spacing
import com.ghost.folio.ui.theme.SurfaceDark
import com.ghost.folio.ui.theme.WhiteConvolvulus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                .fillMaxHeight(0.70f)
                .padding(horizontal = Spacing.lg, vertical = Spacing.md)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.changelog_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = WhiteConvolvulus
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(R.string.changelog_app_version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = MaterialTheme.typography.labelSmall.fontFamily),
                        color = MutedDark
                    )
                }

                // Version Chip Pill
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(LavenderMist.copy(alpha = 0.12f))
                        .border(
                            width = 1.dp,
                            color = LavenderMist.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = LavenderMist
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Scrollable Releases
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = Spacing.md)
            ) {
                itemsIndexed(ChangelogData.releases) { index, release ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Release Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "v${release.version}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = MaterialTheme.typography.labelSmall.fontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = LavenderMist
                            )

                            Text(
                                text = release.date,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = MaterialTheme.typography.labelSmall.fontFamily
                                ),
                                color = MutedDark
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        // Changes List
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            release.changes.forEach { item ->
                                ChangelogItemRow(item = item)
                            }
                        }

                        if (index < ChangelogData.releases.lastIndex) {
                            Spacer(modifier = Modifier.height(Spacing.lg))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MutedDark.copy(alpha = 0.15f))
                            )
                            Spacer(modifier = Modifier.height(Spacing.lg))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Sticky Bottom Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .semantics { contentDescription = "Dismiss changelog" },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderMist,
                    contentColor = ChampionBlue
                )
            ) {
                Text(
                    text = stringResource(R.string.changelog_got_it),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun ChangelogItemRow(
    item: ChangelogItem,
    modifier: Modifier = Modifier
) {
    val (chipBg, chipText) = when (item.type) {
        ChangeType.NEW -> LavenderMist.copy(alpha = 0.15f) to LavenderMist
        ChangeType.FIX -> Color(0xFF1A3A2A) to Color(0xFF4CAF8A)
        ChangeType.IMPROVED -> Color(0xFF1A2A3A) to Color(0xFF64B5F6)
        ChangeType.REMOVED -> Color(0xFF3A1A1A) to Color(0xFFEF5350)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(chipBg)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = item.type.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MaterialTheme.typography.labelSmall.fontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = chipText
            )
        }

        Spacer(modifier = Modifier.width(Spacing.sm))

        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp
            ),
            color = WhiteConvolvulus,
            modifier = Modifier.weight(1f)
        )
    }
}
