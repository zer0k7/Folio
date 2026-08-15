package com.ghost.folio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.ghost.folio.data.model.Category
import com.ghost.folio.ui.theme.ChampionBlue
import com.ghost.folio.ui.theme.LavenderMist
import com.ghost.folio.ui.theme.MutedDark
import com.ghost.folio.ui.theme.Spacing
import com.ghost.folio.ui.theme.SurfaceDark
import com.ghost.folio.ui.theme.WhiteConvolvulus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryExportBottomSheet(
    category: Category,
    onConfirmExport: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val estimatedSizeKb = maxOf(45, category.articleCount * 14)

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
                text = stringResource(R.string.export_category_title, category.label),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = WhiteConvolvulus
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = stringResource(
                    R.string.export_category_subtitle,
                    category.articleCount,
                    estimatedSizeKb
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MutedDark
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Button(
                onClick = {
                    onDismiss()
                    onConfirmExport()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .semantics { contentDescription = "Export PDF" },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderMist,
                    contentColor = ChampionBlue
                )
            ) {
                Text(
                    text = stringResource(R.string.export_pdf_button),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }

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
                    text = stringResource(R.string.cancel_button),
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedDark,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
