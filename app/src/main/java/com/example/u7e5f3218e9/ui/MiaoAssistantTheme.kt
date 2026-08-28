package com.example.u7e5f3218e9.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.graphics.drawable.toDrawable
import com.example.u7e5f3218e9.config.ThemeMode
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

@Composable
fun MiaoAssistantTheme(
    themeMode: ThemeMode,
    useMonet: Boolean,
    themeColor: String = "red",
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val darkTheme = themeMode == ThemeMode.DARK || (themeMode == ThemeMode.SYSTEM && systemDark)
    val seed = when (themeColor) {
        "blue" -> Color(0xFF0061A4)
        "green" -> Color(0xFF386A20)
        "pink" -> Color(0xFFA6004D)
        else -> Color(0xFFB3261E)
    }
    val controller = remember(themeMode, useMonet, darkTheme, seed) {
        ThemeController(
            colorSchemeMode = when (themeMode) {
                ThemeMode.LIGHT -> ColorSchemeMode.MonetLight
                ThemeMode.DARK -> ColorSchemeMode.MonetDark
                else -> ColorSchemeMode.MonetSystem
            },
            keyColor = seed,
            colorSpec = ThemeColorSpec.Spec2025,
            paletteStyle = ThemePaletteStyle.TonalSpot,
            isDark = darkTheme,
        )
    }

    MiuixTheme(controller = controller) {
        val surfaceColor = MiuixTheme.colorScheme.surface
        LaunchedEffect(darkTheme, surfaceColor) {
            val window = (context as? Activity)?.window ?: return@LaunchedEffect
            // App-selected dark mode can differ from the system resource qualifier. Keep the
            // actual window/transition background in sync so page and task animations never flash light.
            window.setBackgroundDrawable(surfaceColor.toArgb().toDrawable())
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
        content()
    }
}
