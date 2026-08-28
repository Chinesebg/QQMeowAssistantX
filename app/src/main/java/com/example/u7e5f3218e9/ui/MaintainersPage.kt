package com.example.u7e5f3218e9.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.u7e5f3218e9.R
import com.example.u7e5f3218e9.config.AppConfig
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MaintainersPage(
    config: AppConfig,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var showImage by remember { mutableStateOf(false) }

    MiaoPage(
        title = R.string.maintainers,
        bottomInnerPadding = 0.dp,
        blurEnabled = config.blurEnabled || config.dialogBlur > 0,
        transparentBackground = config.backgroundUri.isNotEmpty(),
        navigationIcon = { BackNavigationButton(onBack) },
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                colors = if (config.cardLiquidGlass) CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
                ) else CardDefaults.defaultColors(),
            ) {
                ArrowPreference(
                    title = "Chinesebg",
                    summary = "GitHub",
                    startAction = { PreferenceIcon(Icons.Rounded.Info) },
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Chinesebg")))
                    },
                )
                ArrowPreference(
                    title = stringResource(R.string.about_contrib_do_not),
                    summary = stringResource(R.string.about_contrib_do_not),
                    startAction = { PreferenceIcon(Icons.Rounded.Person) },
                    onClick = { showImage = true },
                )
            }
        }
    }

    if (showImage) {
        OverlayDialog(
            show = showImage,
            title = stringResource(R.string.about_contrib_do_not),
            onDismissRequest = { showImage = false },
        ) {
            Image(
                painter = painterResource(R.drawable.src1),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )
        }
    }
}
