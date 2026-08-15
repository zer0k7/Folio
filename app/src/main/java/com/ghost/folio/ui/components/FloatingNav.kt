package com.ghost.folio.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.ghost.folio.R
import com.ghost.folio.ui.theme.Spacing

data class NavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

@Composable
fun FloatingNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
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

    val isDark = isSystemInDarkTheme()
    val navShape = RoundedCornerShape(20.dp)

    val backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = 0.92f)

    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.Black.copy(alpha = 0.06f)
    }

    val items = listOf(
        NavDestination("home", stringResource(R.string.nav_home), Icons.Outlined.Home),
        NavDestination("explore", stringResource(R.string.nav_explore), Icons.Outlined.Explore),
        NavDestination("search", stringResource(R.string.nav_search), Icons.Outlined.Search),
        NavDestination("more", stringResource(R.string.nav_more), Icons.Outlined.Tune)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .height(64.dp)
                .shadow(elevation = 24.dp, shape = navShape, clip = false)
                .clip(navShape)
                .background(backgroundColor)
                .border(width = 1.dp, color = borderColor, shape = navShape),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { destination ->
                    val isSelected = currentRoute == destination.route

                    val iconScale = if (reduceMotion) {
                        if (isSelected) 1.15f else 1.0f
                    } else {
                        animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "nav_scale_${destination.route}"
                        ).value
                    }

                    val labelAlpha = if (reduceMotion) {
                        if (isSelected) 1.0f else 0.6f
                    } else {
                        animateFloatAsState(
                            targetValue = if (isSelected) 1.0f else 0.6f,
                            label = "nav_alpha_${destination.route}"
                        ).value
                    }

                    val activeColor = MaterialTheme.colorScheme.primary
                    val inactiveColor = MaterialTheme.colorScheme.secondary

                    val itemColor = if (isSelected) activeColor else inactiveColor
                    val currentIcon = if (isSelected) destination.selectedIcon else destination.icon

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics { contentDescription = destination.label }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isSelected) {
                                    onNavigate(destination.route)
                                }
                            }
                            .padding(vertical = Spacing.xs),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = currentIcon,
                            contentDescription = destination.label,
                            tint = itemColor,
                            modifier = Modifier
                                .size(22.dp)
                                .scale(iconScale)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = destination.label,
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                            color = itemColor.copy(alpha = labelAlpha),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(width = 3.dp, height = 3.dp)
                                    .background(activeColor, CircleShape)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(width = 3.dp, height = 3.dp))
                        }
                    }
                }
            }
        }
    }
}
