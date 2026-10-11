package com.zyproo.devlink.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DevLinkLightColorScheme = lightColorScheme(
    primary = DevLinkBlue,
    onPrimary = DevLinkSurface,
    primaryContainer = DevLinkBlueLight,
    background = DevLinkBackground,
    surface = DevLinkSurface,
    error = DevLinkError,
    onSurfaceVariant = DevLinkOnSurfaceVariant
)

private val DevLinkDarkColorScheme = darkColorScheme(
    primary = DevLinkBlueLight,
    onPrimary = DevLinkBackgroundDark,
    primaryContainer = DevLinkBlueDark,
    background = DevLinkBackgroundDark,
    surface = DevLinkSurfaceDark,
    error = DevLinkError
)

@Composable
fun DevLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DevLinkDarkColorScheme else DevLinkLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = DevLinkTypography,
        content = content
    )
}
