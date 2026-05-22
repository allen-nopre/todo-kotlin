package com.fullstackcert.todo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary              = Blue,
    onPrimary            = White,
    primaryContainer     = LightBlue,
    onPrimaryContainer   = CharcoalDark,
    secondary            = GraySecondary,
    onSecondary          = White,
    secondaryContainer   = GrayLight,
    onSecondaryContainer = CharcoalDark,
    tertiary             = Teal,
    onTertiary           = White,
    surface              = White,
    onSurface            = CharcoalDark,
    surfaceVariant       = OffWhite,
    onSurfaceVariant     = GraySecondary,
    outline              = GrayLight,
    error                = Pink,
    onError              = White,
)

@Composable
fun TodoAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
