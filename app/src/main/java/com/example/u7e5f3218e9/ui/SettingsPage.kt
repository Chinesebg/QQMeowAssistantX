package com.example.u7e5f3218e9.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.u7e5f3218e9.AiConfig
import com.example.u7e5f3218e9.BuildConfig
import com.example.u7e5f3218e9.CatGirlActivity
import com.example.u7e5f3218e9.MainActivity
import com.example.u7e5f3218e9.PrivacyActivity
import com.example.u7e5f3218e9.R
import com.example.u7e5f3218e9.ThemeManager
import com.example.u7e5f3218e9.UiConfig
import com.example.u7e5f3218e9.config.AppConfig
import com.example.u7e5f3218e9.config.ThemeMode
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SliderPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun SettingsPage(
    config: AppConfig,
    bottomInnerPadding: Dp,
    onOpenAppearance: () -> Unit,
    onOpenAbout: () -> Unit,
    onConfigChange: (AppConfig) -> Unit,
    onScrollDown: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    var showAiDialog by remember { mutableStateOf(false) }
    var showDisclaimer by remember { mutableStateOf(false) }
    var languageTapCount by remember { mutableIntStateOf(0) }
    var lastLanguageTap by remember { mutableLongStateOf(0L) }
    val bgLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: Exception) {
            }
            onConfigChange(config.copy(backgroundUri = uri.toString()))
        }
    }

    val themeNames = ThemeManager.COLOR_NAMES
    val themeValues = ThemeManager.COLOR_VALUES
    val themeIndex = themeValues.indexOf(config.themeColor).coerceAtLeast(0)
    val nightNames = ThemeManager.NIGHT_NAMES
    val nightValues = ThemeManager.NIGHT_VALUES
    val nightIndex = nightValues.indexOf(config.themeMode.storedValue).coerceAtLeast(0)

    MiaoPage(
        title = R.string.nav_settings,
        bottomInnerPadding = bottomInnerPadding,
        blurEnabled = config.blurEnabled || config.dialogBlur > 0,
        transparentBackground = config.backgroundUri.isNotEmpty(),
        onScrollDown = onScrollDown,
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp), colors = if (config.cardLiquidGlass) CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f)) else CardDefaults.defaultColors()) {
                OverlayDropdownPreference(
                    items = themeNames.toList(),
                    selectedIndex = themeIndex,
                    title = stringResource(R.string.settings_theme_color),
                    startAction = { PreferenceIcon(Icons.Rounded.Palette) },
                    onSelectedIndexChange = { index ->
                        onConfigChange(config.copy(themeColor = themeValues[index]))
                    },
                )
                OverlayDropdownPreference(
                    items = nightNames.toList(),
                    selectedIndex = nightIndex,
                    title = stringResource(R.string.settings_dark_mode),
                    startAction = { PreferenceIcon(Icons.Rounded.Palette) },
                    onSelectedIndexChange = { index ->
                        val mode = when (nightValues[index]) {
                            ThemeManager.NIGHT_LIGHT -> ThemeMode.LIGHT
                            ThemeManager.NIGHT_DARK -> ThemeMode.DARK
                            else -> ThemeMode.SYSTEM
                        }
                        onConfigChange(config.copy(themeMode = mode))
                    },
                )
                OverlayDropdownPreference(
                    items = listOf("Material Design", "Miuix"),
                    selectedIndex = if (config.uiStyle == UiConfig.UI_MIUI) 1 else 0,
                    title = stringResource(R.string.settings_ui_style),
                    startAction = { PreferenceIcon(Icons.Rounded.Palette) },
                    onSelectedIndexChange = { index ->
                        if (index == 0) {
                            UiConfig.setUiStyle(context, UiConfig.UI_MATERIAL)
                            context.startActivity(Intent(context, MainActivity::class.java))
                            (context as? Activity)?.finish()
                        } else {
                            onConfigChange(config.copy(uiStyle = UiConfig.UI_MIUI))
                        }
                    },
                )
            }

            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp), colors = if (config.cardLiquidGlass) CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f)) else CardDefaults.defaultColors()) {
                ArrowPreference(
                    title = stringResource(R.string.settings_bg_image),
                    startAction = { PreferenceIcon(Icons.Rounded.Wallpaper) },
                    onClick = { bgLauncher.launch(arrayOf("image/*")) },
                )
                ArrowPreference(
                    title = stringResource(R.string.settings_bg_clear),
                    startAction = { PreferenceIcon(Icons.Rounded.Wallpaper) },
                    onClick = { onConfigChange(config.copy(backgroundUri = "")) },
                )
                SliderPreference(
                    value = config.backgroundBrightness.toFloat(),
                    onValueChange = { onConfigChange(config.copy(backgroundBrightness = it.toInt())) },
                    title = stringResource(R.string.settings_bg_dim),
                    valueText = config.backgroundBrightness.toString(),
                    valueRange = -100f..100f,
                )
                SliderPreference(
                    value = config.dialogBlur.toFloat(),
                    onValueChange = { onConfigChange(config.copy(dialogBlur = it.toInt())) },
                    title = stringResource(R.string.settings_dialog_blur),
                    valueText = config.dialogBlur.toString(),
                    valueRange = 0f..50f,
                )
                SwitchPreference(
                    checked = config.fixedBottomBar,
                    onCheckedChange = { onConfigChange(config.copy(fixedBottomBar = it)) },
                    title = stringResource(R.string.settings_fixed_bottom_bar),
                    summary = stringResource(R.string.settings_fixed_bottom_bar_desc),
                    startAction = { PreferenceIcon(Icons.Rounded.Settings) },
                )
                SwitchPreference(
                    checked = config.cardLiquidGlass,
                    onCheckedChange = { onConfigChange(config.copy(cardLiquidGlass = it)) },
                    title = "背景卡片液态玻璃",
                    summary = "让 Miuix 背景卡片使用半透明液态玻璃效果",
                    startAction = { PreferenceIcon(Icons.Rounded.Wallpaper) },
                )
            }

            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp), colors = if (config.cardLiquidGlass) CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f)) else CardDefaults.defaultColors()) {
                SwitchPreference(
                    checked = config.aiToastEnabled,
                    onCheckedChange = { onConfigChange(config.copy(aiToastEnabled = it)) },
                    title = stringResource(R.string.settings_ai_toast),
                    summary = stringResource(R.string.settings_ai_toast_desc),
                    startAction = { PreferenceIcon(Icons.Rounded.Info) },
                )
            }

            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp), colors = if (config.cardLiquidGlass) CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f)) else CardDefaults.defaultColors()) {
                ArrowPreference(
                    title = stringResource(R.string.settings_ai),
                    startAction = { PreferenceIcon(Icons.Rounded.Settings) },
                    onClick = { showAiDialog = true },
                )
            }

            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp), colors = if (config.cardLiquidGlass) CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f)) else CardDefaults.defaultColors()) {
                ArrowPreference(
                    title = stringResource(R.string.settings_disclaimer),
                    startAction = { PreferenceIcon(Icons.Rounded.Info) },
                    onClick = { showDisclaimer = true },
                )
                ArrowPreference(
                    title = stringResource(R.string.settings_privacy),
                    startAction = { PreferenceIcon(Icons.Rounded.Info) },
                    onClick = { context.startActivity(Intent(context, PrivacyActivity::class.java)) },
                )
            }

            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp), colors = if (config.cardLiquidGlass) CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f)) else CardDefaults.defaultColors()) {
                ArrowPreference(
                    title = stringResource(R.string.settings_about),
                    summary = stringResource(R.string.version_summary, BuildConfig.VERSION_NAME),
                    startAction = { PreferenceIcon(Icons.Rounded.Info) },
                    onClick = onOpenAbout,
                )
                ArrowPreference(
                    title = stringResource(R.string.settings_get_updates),
                    startAction = { PreferenceIcon(Icons.Rounded.Info) },
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Chinesebg/QQMeowAssistantX"))
                        context.startActivity(intent)
                    },
                )
                ArrowPreference(
                    title = stringResource(R.string.settings_language),
                    startAction = { PreferenceIcon(Icons.Rounded.Info) },
                    onClick = {
                        val now = System.currentTimeMillis()
                        if (now - lastLanguageTap > 1500) languageTapCount = 0
                        lastLanguageTap = now
                        languageTapCount++
                        if (languageTapCount >= 7) {
                            languageTapCount = 0
                            context.startActivity(Intent(context, CatGirlActivity::class.java))
                        } else {
                            android.widget.Toast.makeText(context, R.string.toast_language_wip, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                )
            }
        }
    }

    AiSettingsDialog(show = showAiDialog, onDismiss = { showAiDialog = false })
    if (showDisclaimer) {
        OverlayDialog(
            show = showDisclaimer,
            title = stringResource(R.string.disclaimer_title),
            onDismissRequest = { showDisclaimer = false },
        ) {
            Text(stringResource(R.string.disclaimer_message))
        }
    }
}

@Composable
private fun AiSettingsDialog(show: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val ai = remember { AiConfig.load(context) }
    var baseUrl by remember { mutableStateOf(ai.baseUrl) }
    var apiKey by remember { mutableStateOf(ai.apiKey) }
    var model by remember { mutableStateOf(ai.model) }

    OverlayDialog(
        show = show,
        title = "AI 设置",
        onDismissRequest = onDismiss,
    ) {
        Column(Modifier.padding(horizontal = 4.dp)) {
            TextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                label = stringResource(R.string.ai_base_url),
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                label = stringResource(R.string.ai_api_key),
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = model,
                onValueChange = { model = it },
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                label = stringResource(R.string.ai_model),
            )
            Spacer(Modifier.height(16.dp))
            top.yukonga.miuix.kmp.basic.TextButton(
                text = "保存",
                onClick = {
                    val c = AiConfig()
                    c.baseUrl = baseUrl.trim().ifEmpty { AiConfig.DEFAULT_BASE_URL }
                    c.apiKey = apiKey.trim()
                    c.model = model.trim().ifEmpty { AiConfig.DEFAULT_MODEL }
                    c.save(context)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
            )
        }
    }
}
