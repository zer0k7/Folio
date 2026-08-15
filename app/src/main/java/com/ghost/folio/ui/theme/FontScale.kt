package com.ghost.folio.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

val LocalFontScale = compositionLocalOf { 1.0f }

val TextUnit.scaled: TextUnit
    @Composable
    get() = (this.value * LocalFontScale.current).sp
