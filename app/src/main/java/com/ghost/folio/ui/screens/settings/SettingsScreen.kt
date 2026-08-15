package com.ghost.folio.ui.screens.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.FormatSize
import com.ghost.folio.ui.components.ChangelogBottomSheet
import com.ghost.folio.ui.components.FontSizeBottomSheet
import com.ghost.folio.ui.components.getFontScaleLabel
import com.ghost.folio.BuildConfig
import com.ghost.folio.R
import com.ghost.folio.data.local.preferences.ThemeSetting
import com.ghost.folio.data.local.preferences.UpdateFrequency
import com.ghost.folio.ui.theme.LavenderMist
import com.ghost.folio.ui.theme.MutedDark
import com.ghost.folio.ui.theme.Spacing
import com.ghost.folio.ui.theme.SurfaceDark
import com.ghost.folio.ui.theme.WhiteConvolvulus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val APACHE_LICENSE_TEXT = """
                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

   1. Definitions.
      "License" shall mean the terms and conditions for use, reproduction,
      and distribution as defined by Sections 1 through 9 of this document.

      "Licensor" shall mean the copyright owner or entity authorized by
      the copyright owner that is granting the License.

   2. Grant of Copyright License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      copyright license to reproduce, prepare Derivative Works of,
      publicly display, publicly perform, sublicense, and distribute the
      Work and such Derivative Works in Source or Object form.

   3. Grant of Patent License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      patent license to make, have made, use, offer to sell, sell, import,
      and otherwise transfer the Work.

   4. Redistribution. You may reproduce and distribute copies of the
      Work or Derivative Works thereof in any medium, with or without
      modifications, and in Source or Object form, provided that You
      meet the required notice conditions.

   5. Disclaimer of Warranty. Unless required by applicable law or
      agreed to in writing, Licensor provides the Work (and each
      Contributor provides its Contributions) on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
      implied, including, without limitation, any warranties or conditions
      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A
      PARTICULAR PURPOSE.
"""

private const val OSS_LICENSES_TEXT = """
Android Jetpack Compose
Copyright 2024 The Android Open Source Project
Licensed under the Apache License, Version 2.0

Kotlin & KotlinX Coroutines & Serialization
Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors
Licensed under the Apache License, Version 2.0

AndroidX Room Database
Copyright 2024 The Android Open Source Project
Licensed under the Apache License, Version 2.0

AndroidX DataStore
Copyright 2024 The Android Open Source Project
Licensed under the Apache License, Version 2.0
"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var showThemePicker by remember { mutableStateOf(false) }
    var showFontSizeSheet by remember { mutableStateOf(false) }
    var showFrequencyPicker by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearExportsDialog by remember { mutableStateOf(false) }
    var showLicenseSheet by remember { mutableStateOf(false) }
    var showOssLicensesSheet by remember { mutableStateOf(false) }
    var showChangelogSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshCacheSize(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TopAppBar
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
                    contentDescription = stringResource(R.string.back_desc),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = Spacing.xs)
            )
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.lg,
                end = Spacing.lg,
                top = Spacing.xs,
                bottom = Spacing.xl + navBarBottomInset
            )
        ) {
            // SECTION 1: APPEARANCE
            item {
                SectionHeader(title = stringResource(R.string.settings_section_appearance))
            }

            item {
                val themeLabel = when (state.themeSetting) {
                    ThemeSetting.SYSTEM -> stringResource(R.string.settings_theme_system)
                    ThemeSetting.LIGHT -> stringResource(R.string.settings_theme_light)
                    ThemeSetting.DARK -> stringResource(R.string.settings_theme_dark)
                }

                SettingsRow(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(R.string.settings_theme_title),
                    subtitle = themeLabel,
                    onClick = { showThemePicker = true }
                ) {
                    Text(
                        text = themeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                val fontScaleLabel = stringResource(getFontScaleLabel(state.fontScale))

                SettingsRow(
                    icon = Icons.Outlined.FormatSize,
                    title = stringResource(R.string.font_scale_title),
                    subtitle = fontScaleLabel,
                    onClick = { showFontSizeSheet = true }
                ) {
                    Text(
                        text = fontScaleLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // SECTION 2: CONTENT
            item {
                Spacer(modifier = Modifier.height(Spacing.md))
                SectionHeader(title = stringResource(R.string.settings_section_content))
            }

            item {
                val lastCheckedFormatted = if (state.lastUpdateCheckTime > 0L) {
                    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    sdf.format(Date(state.lastUpdateCheckTime))
                } else {
                    stringResource(R.string.settings_never_checked)
                }

                SettingsRow(
                    icon = Icons.Outlined.SystemUpdate,
                    title = stringResource(R.string.settings_check_updates),
                    subtitle = stringResource(R.string.settings_last_checked, lastCheckedFormatted),
                    onClick = { viewModel.checkForUpdates(context) }
                ) {
                    when (state.updateCheckStatus) {
                        UpdateCheckStatus.CHECKING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        UpdateCheckStatus.UPDATE_AVAILABLE -> {
                            Text(
                                text = stringResource(R.string.settings_update_available),
                                style = MaterialTheme.typography.labelSmall,
                                color = LavenderMist
                            )
                        }
                        UpdateCheckStatus.UP_TO_DATE -> {
                            Text(
                                text = stringResource(R.string.settings_up_to_date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedDark
                            )
                        }
                        UpdateCheckStatus.IDLE -> {
                            Text(
                                text = stringResource(R.string.settings_check_updates),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            item {
                val freqLabel = stringResource(state.updateFrequency.labelResId)

                SettingsRow(
                    icon = Icons.Outlined.Schedule,
                    title = stringResource(R.string.settings_auto_check_frequency),
                    subtitle = null,
                    onClick = { showFrequencyPicker = true }
                ) {
                    Text(
                        text = freqLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                SettingsRow(
                    icon = Icons.Outlined.NewReleases,
                    title = stringResource(R.string.settings_whats_new),
                    subtitle = stringResource(R.string.changelog_app_version, BuildConfig.VERSION_NAME),
                    onClick = { showChangelogSheet = true }
                )
            }

            // SECTION 3: DATA
            item {
                Spacer(modifier = Modifier.height(Spacing.md))
                SectionHeader(title = stringResource(R.string.settings_section_data))
            }

            item {
                SettingsRow(
                    icon = Icons.Outlined.History,
                    title = stringResource(R.string.settings_reading_history),
                    subtitle = stringResource(R.string.settings_articles_read, state.readArticlesCount),
                    onClick = null
                ) {
                    TextButton(
                        onClick = { showClearHistoryDialog = true },
                        contentPadding = PaddingValues(horizontal = Spacing.xs, vertical = 0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_clear_action),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                val sizeText = if (state.cachedExportsSizeBytes >= 1024 * 1024) {
                    stringResource(R.string.settings_storage_used_mb, state.cachedExportsSizeBytes / (1024f * 1024f))
                } else {
                    stringResource(R.string.settings_storage_used_kb, state.cachedExportsSizeBytes / 1024)
                }

                SettingsRow(
                    icon = Icons.Outlined.FolderOpen,
                    title = stringResource(R.string.settings_cached_exports),
                    subtitle = sizeText,
                    onClick = null
                ) {
                    TextButton(
                        onClick = { showClearExportsDialog = true },
                        contentPadding = PaddingValues(horizontal = Spacing.xs, vertical = 0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_clear_action),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // SECTION 4: ABOUT
            item {
                Spacer(modifier = Modifier.height(Spacing.md))
                SectionHeader(title = stringResource(R.string.settings_section_about))
            }

            item {
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.settings_version_title),
                    subtitle = stringResource(
                        R.string.settings_version_subtitle,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE
                    ),
                    subtitleFontFamily = MaterialTheme.typography.labelSmall.fontFamily,
                    onClick = null
                )
            }

            item {
                SettingsRow(
                    icon = Icons.Outlined.Code,
                    title = stringResource(R.string.settings_source_code_title),
                    subtitle = stringResource(R.string.settings_source_code_url),
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zer0k7/Folio"))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                        }
                    }
                )
            }

            item {
                SettingsRow(
                    icon = Icons.Outlined.Gavel,
                    title = stringResource(R.string.settings_license_title),
                    subtitle = stringResource(R.string.settings_license_type),
                    onClick = { showLicenseSheet = true }
                )
            }

            item {
                SettingsRow(
                    icon = Icons.Outlined.Code,
                    title = stringResource(R.string.settings_oss_licenses_title),
                    subtitle = null,
                    onClick = { showOssLicensesSheet = true }
                )
            }
        }
    }

    // Theme Picker Bottom Sheet
    if (showThemePicker) {
        ModalBottomSheet(
            onDismissRequest = { showThemePicker = false },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = Spacing.md, bottom = Spacing.xs)
                        .size(width = 32.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(MutedDark)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .padding(bottom = Spacing.lg)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Text(
                    text = stringResource(R.string.settings_theme_picker_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = WhiteConvolvulus
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                val options = listOf(
                    ThemeSetting.SYSTEM to stringResource(R.string.settings_theme_system),
                    ThemeSetting.LIGHT to stringResource(R.string.settings_theme_light),
                    ThemeSetting.DARK to stringResource(R.string.settings_theme_dark)
                )

                options.forEach { (theme, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setThemeSetting(theme)
                                showThemePicker = false
                            }
                            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (state.themeSetting == theme) LavenderMist else WhiteConvolvulus
                        )

                        if (state.themeSetting == theme) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = LavenderMist,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Update Frequency Picker Bottom Sheet
    if (showFrequencyPicker) {
        ModalBottomSheet(
            onDismissRequest = { showFrequencyPicker = false },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = Spacing.md, bottom = Spacing.xs)
                        .size(width = 32.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(MutedDark)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .padding(bottom = Spacing.lg)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Text(
                    text = stringResource(R.string.settings_frequency_picker_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = WhiteConvolvulus
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                UpdateFrequency.entries.forEach { freq ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setUpdateFrequency(freq)
                                showFrequencyPicker = false
                            }
                            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(freq.labelResId),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (state.updateFrequency == freq) LavenderMist else WhiteConvolvulus
                        )

                        if (state.updateFrequency == freq) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = LavenderMist,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // License Bottom Sheet
    if (showLicenseSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLicenseSheet = false },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = Spacing.md, bottom = Spacing.xs)
                        .size(width = 32.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(MutedDark)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .padding(bottom = Spacing.lg)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Text(
                    text = "Apache License 2.0",
                    style = MaterialTheme.typography.headlineSmall,
                    color = WhiteConvolvulus
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                        .padding(Spacing.sm)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = APACHE_LICENSE_TEXT.trimIndent(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        ),
                        color = WhiteConvolvulus
                    )
                }
            }
        }
    }

    // OSS Licenses Bottom Sheet
    if (showOssLicensesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOssLicensesSheet = false },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = Spacing.md, bottom = Spacing.xs)
                        .size(width = 32.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(MutedDark)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
                    .padding(bottom = Spacing.lg)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Text(
                    text = "Open Source Licenses",
                    style = MaterialTheme.typography.headlineSmall,
                    color = WhiteConvolvulus
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                        .padding(Spacing.sm)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = OSS_LICENSES_TEXT.trimIndent(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        ),
                        color = WhiteConvolvulus
                    )
                }
            }
        }
    }

    // Clear History Confirmation Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = stringResource(R.string.settings_clear_history_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = WhiteConvolvulus
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_clear_history_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedDark
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearReadingHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.settings_clear_action),
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearHistoryDialog = false }
                ) {
                    Text(
                        text = stringResource(R.string.cancel_button),
                        color = MutedDark
                    )
                }
            }
        )
    }

    // Clear Cached Exports Confirmation Dialog
    if (showClearExportsDialog) {
        AlertDialog(
            onDismissRequest = { showClearExportsDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = stringResource(R.string.settings_clear_exports_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = WhiteConvolvulus
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_clear_exports_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedDark
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCachedExports(context)
                        showClearExportsDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.settings_clear_action),
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearExportsDialog = false }
                ) {
                    Text(
                        text = stringResource(R.string.cancel_button),
                        color = MutedDark
                    )
                }
            }
        )
    }

    // Changelog Bottom Sheet
    if (showChangelogSheet) {
        ChangelogBottomSheet(
            onDismiss = { showChangelogSheet = false }
        )
    }

    // Font Size Bottom Sheet
    if (showFontSizeSheet) {
        FontSizeBottomSheet(
            currentScale = state.fontScale,
            onApplyScale = { newScale ->
                viewModel.setFontScale(newScale)
            },
            onDismiss = { showFontSizeSheet = false }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 0.15.sp,
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.sm)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    subtitleFontFamily: androidx.compose.ui.text.font.FontFamily? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(Spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = if (subtitleFontFamily != null) {
                        MaterialTheme.typography.bodyMedium.copy(fontFamily = subtitleFontFamily)
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(Spacing.sm))
            trailingContent()
        }
    }
}
