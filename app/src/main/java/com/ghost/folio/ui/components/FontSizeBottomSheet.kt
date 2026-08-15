package com.ghost.folio.ui.components

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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ghost.folio.R
import com.ghost.folio.ui.theme.ChampionBlue
import com.ghost.folio.ui.theme.DMSerifDisplayFamily
import com.ghost.folio.ui.theme.InterFamily
import com.ghost.folio.ui.theme.JetBrainsMonoFamily
import com.ghost.folio.ui.theme.LavenderMist
import com.ghost.folio.ui.theme.MutedDark
import com.ghost.folio.ui.theme.Spacing
import com.ghost.folio.ui.theme.SurfaceDark
import com.ghost.folio.ui.theme.WhiteConvolvulus
import kotlin.math.abs

private val FONT_SCALE_STOPS = listOf(0.85f, 1.0f, 1.15f, 1.3f, 1.4f)

fun snapToNearestStop(value: Float): Float {
    return FONT_SCALE_STOPS.minByOrNull { abs(it - value) } ?: 1.0f
}

fun getFontScaleLabel(scale: Float): Int {
    val snapped = snapToNearestStop(scale)
    return when {
        snapped <= 0.86f -> R.string.font_scale_small
        snapped <= 1.01f -> R.string.font_scale_default
        snapped <= 1.16f -> R.string.font_scale_large
        snapped <= 1.31f -> R.string.font_scale_xlarge
        else -> R.string.font_scale_max
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSizeBottomSheet(
    currentScale: Float,
    onApplyScale: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var tempScale by remember { mutableFloatStateOf(currentScale) }

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
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.lg)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Text(
                text = stringResource(R.string.font_scale_title),
                style = MaterialTheme.typography.headlineMedium,
                color = WhiteConvolvulus
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Preview Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChampionBlue)
                    .padding(Spacing.md)
            ) {
                Text(
                    text = "PREVIEW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.sp,
                        letterSpacing = 0.15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MutedDark
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Text(
                    text = "What is a Pixel?",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = DMSerifDisplayFamily,
                        fontSize = (22f * tempScale).sp,
                        lineHeight = (28f * tempScale).sp
                    ),
                    color = WhiteConvolvulus
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Text(
                    text = "A pixel is the smallest addressable unit of a digital display.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = InterFamily,
                        fontSize = (15f * tempScale).sp,
                        lineHeight = (22f * tempScale).sp
                    ),
                    color = WhiteConvolvulus.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Slider
            Slider(
                value = tempScale,
                onValueChange = { tempScale = snapToNearestStop(it) },
                valueRange = 0.85f..1.4f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = LavenderMist,
                    activeTrackColor = LavenderMist,
                    inactiveTrackColor = MutedDark.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // 5 Discrete Scale Labels
            val labels = listOf("S", "Default", "L", "XL", "Max")
            val stopValues = FONT_SCALE_STOPS

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEachIndexed { index, label ->
                    val isSelected = abs(tempScale - stopValues[index]) < 0.05f
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) LavenderMist else MutedDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Apply Button
            Button(
                onClick = {
                    onApplyScale(tempScale)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .semantics { contentDescription = "Apply text size" },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderMist,
                    contentColor = ChampionBlue
                )
            ) {
                Text(
                    text = stringResource(R.string.font_scale_apply),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Reset to Default Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 44.dp)
                    .clickable {
                        tempScale = 1.0f
                        onApplyScale(1.0f)
                        onDismiss()
                    }
                    .padding(vertical = Spacing.xs),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.font_scale_reset),
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedDark,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
