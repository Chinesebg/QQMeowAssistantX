package com.example.u7e5f3218e9.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Policy
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
import androidx.compose.ui.unit.sp
import com.example.u7e5f3218e9.BuildConfig
import com.example.u7e5f3218e9.R
import com.example.u7e5f3218e9.config.AppConfig
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun AboutPage(
    config: AppConfig,
    onBack: () -> Unit,
    onOpenLicenses: () -> Unit,
    onOpenMaintainers: () -> Unit,
) {
    val context = LocalContext.current
    MiaoPage(
        title = R.string.about,
        bottomInnerPadding = 0.dp,
        blurEnabled = config.blurEnabled || config.dialogBlur > 0,
        transparentBackground = config.backgroundUri.isNotEmpty(),
        navigationIcon = { BackNavigationButton(onBack) },
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                insideMargin = PaddingValues(18.dp),
                colors = if (config.cardLiquidGlass) top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
                ) else top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(),
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 20.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.version_summary, BuildConfig.VERSION_NAME),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.about_description),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                )
            }

            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                colors = if (config.cardLiquidGlass) top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
                ) else top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(),
            ) {
                ArrowPreference(
                    title = "维护者",
                    summary = "Chinesebg / 己所不欲 勿施于人",
                    startAction = { PreferenceIcon(Icons.Rounded.Person) },
                    onClick = onOpenMaintainers,
                )
            }

            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                colors = if (config.cardLiquidGlass) top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
                ) else top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(),
            ) {
                ArrowPreference(
                    title = stringResource(R.string.third_party_licenses),
                    summary = stringResource(R.string.third_party_licenses_summary),
                    startAction = { PreferenceIcon(Icons.Rounded.Policy) },
                    onClick = onOpenLicenses,
                )
            }
        }
    }

}
