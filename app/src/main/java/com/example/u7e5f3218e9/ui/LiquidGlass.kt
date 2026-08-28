package com.example.u7e5f3218e9.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun Modifier.liquidGlassCard(cornerRadius: Dp = 28.dp): Modifier {
    val backdrop = LocalBackdrop.current ?: return this
    val surfaceColor = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f)
    return this.drawBackdrop(
        backdrop = backdrop,
        shape = { RoundedCornerShape(cornerRadius) },
        effects = {
            vibrancy()
            blur(24f)
            lens(refractionHeight = 16f, refractionAmount = 32f)
        },
        onDrawSurface = {
            drawRect(surfaceColor)
        },
    )
}
