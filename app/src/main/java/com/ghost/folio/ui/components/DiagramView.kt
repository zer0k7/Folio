package com.ghost.folio.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ghost.folio.R
import com.ghost.folio.ui.theme.Spacing

@Composable
fun DiagramView(
    diagramKey: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resourceName = "ic_diagram_$diagramKey"
    val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)

    val actualResourceId = if (resourceId != 0) {
        resourceId
    } else {
        when (diagramKey) {
            "pixel_subpixel" -> R.drawable.ic_diagram_pixel_subpixel
            "lcd_layers" -> R.drawable.ic_diagram_lcd_layers
            "oled_vs_lcd" -> R.drawable.ic_diagram_oled_vs_lcd
            "bootloader_chain" -> R.drawable.ic_diagram_bootloader_chain
            "latency_timeline" -> R.drawable.ic_diagram_latency_timeline
            "dns_resolution" -> R.drawable.ic_diagram_dns_resolution
            else -> 0
        }
    }

    if (actualResourceId != 0) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.sm),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 260.dp)
                    .padding(Spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = actualResourceId),
                    contentDescription = "Diagram: $diagramKey",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
