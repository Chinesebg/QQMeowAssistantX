package com.example.u7e5f3218e9.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.u7e5f3218e9.R
import com.example.u7e5f3218e9.config.AppConfig
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.preference.ArrowPreference

@Composable
internal fun ThirdPartyLicensesPage(
    config: AppConfig,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val projects = listOf(
        Pair("LibChecker", "https://github.com/LibChecker/LibChecker"),
        Pair("NyaBox", "https://github.com/yzsyzdy/nyabox"),
        Pair("RikkaHub", "https://github.com/rikkahub/rikkahub"),
    )

    MiaoPage(
        title = R.string.third_party_licenses,
        bottomInnerPadding = 0.dp,
        blurEnabled = config.blurEnabled || config.dialogBlur > 0,
        transparentBackground = config.backgroundUri.isNotEmpty(),
        navigationIcon = { BackNavigationButton(onBack) },
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                colors = if (config.cardLiquidGlass) top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
                ) else top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(),
            ) {
                projects.forEach { (name, url) ->
                    ArrowPreference(
                        title = name,
                        summary = url,
                        startAction = { PreferenceIcon(Icons.Rounded.Info) },
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                colors = if (config.cardLiquidGlass) top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
                ) else top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(),
            ) {
                top.yukonga.miuix.kmp.basic.BasicComponent(
                    title = stringResource(R.string.project_license),
                    summary = stringResource(R.string.project_license_summary),
                )
            }
        }
    }
}
