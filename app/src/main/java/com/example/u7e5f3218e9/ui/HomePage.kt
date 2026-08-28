package com.example.u7e5f3218e9.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TagFaces
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.u7e5f3218e9.R
import com.example.u7e5f3218e9.config.AppConfig
import com.example.u7e5f3218e9.config.ConfigRepository
import com.example.u7e5f3218e9.config.ProcessingMode
import com.example.u7e5f3218e9.config.ReplacementRule
import com.example.u7e5f3218e9.text.TextProcessor
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun HomePage(
    config: AppConfig,
    serviceEnabled: Boolean,
    bottomInnerPadding: Dp,
    onConfigChange: (AppConfig) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onScrollDown: (Boolean) -> Unit = {},
    onInvalidRules: (Int) -> Unit,
) {
    var editor by remember { mutableStateOf<HomeEditor?>(null) }
    val previewSample = "今天我很开心，你准备好了吗？"
    var previewInput by rememberSaveable(previewSample) { mutableStateOf(previewSample) }
    val customOn = config.enableSentenceSuffix || config.enableRandomEmoticon

    MiaoPage(
        title = R.string.home_title,
        bottomInnerPadding = bottomInnerPadding,
        blurEnabled = config.blurEnabled || config.dialogBlur > 0,
        transparentBackground = config.backgroundUri.isNotEmpty(),
        onScrollDown = onScrollDown,
    ) {
        item {
            ServiceCard(
                serviceEnabled = serviceEnabled,
                onClick = onOpenAccessibilitySettings,
            )

            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                colors = if (config.cardLiquidGlass) CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
                ) else CardDefaults.defaultColors(),
            ) {
                SwitchPreference(
                    checked = config.processingMode == ProcessingMode.PUNCTUATION,
                    onCheckedChange = { checked ->
                        onConfigChange(config.copy(processingMode = if (checked) ProcessingMode.PUNCTUATION else ProcessingMode.REALTIME))
                    },
                    title = stringResource(R.string.mode_punctuation),
                    startAction = { PreferenceIcon(Icons.Rounded.AutoFixHigh) },
                )
                SwitchPreference(
                    checked = config.processingMode == ProcessingMode.REALTIME,
                    onCheckedChange = { checked ->
                        onConfigChange(config.copy(processingMode = if (checked) ProcessingMode.REALTIME else ProcessingMode.PUNCTUATION))
                    },
                    title = stringResource(R.string.mode_realtime),
                    startAction = { PreferenceIcon(Icons.Rounded.AutoFixHigh) },
                )
            }

            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                colors = if (config.cardLiquidGlass) CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
                ) else CardDefaults.defaultColors(),
            ) {
                SwitchPreference(
                    checked = config.engineMode == "rule",
                    onCheckedChange = { checked -> if (checked) onConfigChange(config.copy(engineMode = "rule")) },
                    title = stringResource(R.string.engine_rule),
                    enabled = !customOn,
                    startAction = { PreferenceIcon(Icons.Rounded.PowerSettingsNew) },
                )
                SwitchPreference(
                    checked = config.engineMode == "hybrid",
                    onCheckedChange = { checked -> if (checked) onConfigChange(config.copy(engineMode = "hybrid")) },
                    title = stringResource(R.string.engine_hybrid),
                    enabled = true,
                    startAction = { PreferenceIcon(Icons.Rounded.PowerSettingsNew) },
                )
                SwitchPreference(
                    checked = config.engineMode == "ai",
                    onCheckedChange = { checked -> if (checked) onConfigChange(config.copy(engineMode = "ai")) },
                    title = stringResource(R.string.engine_ai),
                    enabled = true,
                    startAction = { PreferenceIcon(Icons.Rounded.PowerSettingsNew) },
                )
            }

            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                colors = if (config.cardLiquidGlass) CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
                ) else CardDefaults.defaultColors(),
            ) {
                SwitchPreference(
                    checked = config.enableSentenceSuffix,
                    onCheckedChange = { onConfigChange(config.copy(enableSentenceSuffix = it)) },
                    title = stringResource(R.string.switch_append_title),
                    summary = stringResource(R.string.switch_append_desc),
                    startAction = { PreferenceIcon(Icons.Rounded.Edit) },
                )
                ArrowPreference(
                    title = stringResource(R.string.hint_append_text),
                    summary = config.sentenceSuffix,
                    startAction = { PreferenceIcon(Icons.Rounded.Edit) },
                    enabled = config.enableSentenceSuffix,
                    onClick = { editor = HomeEditor.SUFFIX },
                )
                SwitchPreference(
                    checked = config.enableRandomEmoticon,
                    onCheckedChange = { onConfigChange(config.copy(enableRandomEmoticon = it)) },
                    title = stringResource(R.string.switch_emoticon_title),
                    summary = stringResource(R.string.switch_emoticon_desc),
                    startAction = { PreferenceIcon(Icons.Rounded.TagFaces) },
                )
                ArrowPreference(
                    title = stringResource(R.string.hint_custom_emoticons),
                    summary = if (config.customEmoticons.isEmpty()) {
                        stringResource(R.string.custom_emoticons_builtin)
                    } else {
                        stringResource(R.string.custom_emoticons_count, config.customEmoticons.size)
                    },
                    startAction = { PreferenceIcon(Icons.Rounded.TagFaces) },
                    enabled = config.enableRandomEmoticon,
                    onClick = { editor = HomeEditor.EMOTICONS },
                )
            }

            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                colors = if (config.cardLiquidGlass) CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
                ) else CardDefaults.defaultColors(),
            ) {
                OverlayDropdownPreference(
                    items = listOf("乖巧", "傲娇", "亲昵"),
                    selectedIndex = listOf("乖巧", "傲娇", "亲昵").indexOf(config.attitude).coerceAtLeast(0),
                    title = stringResource(R.string.label_attitude),
                    startAction = { PreferenceIcon(Icons.Rounded.TagFaces) },
                    onSelectedIndexChange = { index ->
                        onConfigChange(config.copy(attitude = listOf("乖巧", "傲娇", "亲昵")[index]))
                    },
                )
                OverlayDropdownPreference(
                    items = listOf("轻度", "中度", "重度"),
                    selectedIndex = when (config.intensity) {
                        "light" -> 0
                        "heavy" -> 2
                        else -> 1
                    },
                    title = stringResource(R.string.label_intensity),
                    startAction = { PreferenceIcon(Icons.Rounded.TagFaces) },
                    onSelectedIndexChange = { index ->
                        onConfigChange(config.copy(intensity = listOf("light", "medium", "heavy")[index]))
                    },
                )
            }

            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
                colors = if (config.cardLiquidGlass) CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f),
                ) else CardDefaults.defaultColors(),
            ) {
                ArrowPreference(
                    title = stringResource(R.string.section_rules),
                    summary = if (config.rules.isEmpty()) {
                        stringResource(R.string.replacement_rules_empty)
                    } else {
                        stringResource(R.string.replacement_rules_count, config.rules.size)
                    },
                    startAction = { PreferenceIcon(Icons.Rounded.SwapHoriz) },
                    onClick = { editor = HomeEditor.RULES },
                )
                ArrowPreference(
                    title = stringResource(R.string.preset_rules),
                    startAction = { PreferenceIcon(Icons.Rounded.SwapHoriz) },
                    onClick = {
                        val existing = config.rules.map { it.from }.toMutableSet()
                        val merged = listOf(
                            ReplacementRule("我", "本喵"),
                            ReplacementRule("你", "主人"),
                        ).filterNot { it.from in existing } + config.rules
                        onConfigChange(config.copy(rules = merged))
                    },
                )
            }

            Spacer(Modifier.height(12.dp))
            PreviewCard(
                input = previewInput,
                onInputChange = { previewInput = it },
                config = config,
            )
        }
    }

    EditorDialog(
        show = editor == HomeEditor.SUFFIX,
        title = stringResource(R.string.edit_suffix_title),
        summary = stringResource(R.string.edit_suffix_hint),
        initialValue = config.sentenceSuffix,
        minLines = 1,
        onDismiss = { editor = null },
        onSave = { value ->
            onConfigChange(config.copy(sentenceSuffix = value.trim().ifEmpty { "喵" }))
            editor = null
        },
    )
    EditorDialog(
        show = editor == HomeEditor.RULES,
        title = stringResource(R.string.edit_rules_title),
        summary = stringResource(R.string.rule_editor_hint),
        initialValue = config.rules.joinToString("\n") { it.serialize() },
        minLines = 7,
        onDismiss = { editor = null },
        onSave = { value ->
            val (rules, invalidCount) = ConfigRepository.parseRules(value)
            onConfigChange(config.copy(rules = rules))
            if (invalidCount > 0) onInvalidRules(invalidCount)
            editor = null
        },
    )
    EditorDialog(
        show = editor == HomeEditor.EMOTICONS,
        title = stringResource(R.string.edit_emoticons_title),
        summary = stringResource(R.string.edit_emoticons_hint),
        initialValue = if (config.customEmoticons.isEmpty()) {
            AppConfig.BUILTIN_EMOTICONS.joinToString("\n")
        } else {
            config.customEmoticons.joinToString("\n")
        },
        minLines = 6,
        onDismiss = { editor = null },
        onSave = { value ->
            onConfigChange(config.copy(customEmoticons = ConfigRepository.parseNonBlankLines(value)))
            editor = null
        },
    )
}

@Composable
private fun ServiceCard(serviceEnabled: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
        insideMargin = PaddingValues(18.dp),
        colors = CardDefaults.defaultColors(
            color = if (serviceEnabled) MiuixTheme.colorScheme.primaryContainer
            else MiuixTheme.colorScheme.errorContainer,
        ),
        onClick = onClick,
    ) {
        Text(
            text = stringResource(
                if (serviceEnabled) R.string.service_status_enabled else R.string.service_status_disabled,
            ),
            fontSize = 18.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.tap_to_manage_service),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun PreviewCard(
    input: String,
    onInputChange: (String) -> Unit,
    config: AppConfig,
) {
    val result = remember(input, config) {
        TextProcessor.process(input, config.copy(enabled = true)).text
    }
    Card(
        modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
        insideMargin = PaddingValues(16.dp),
    ) {
        Text(
            text = stringResource(R.string.section_preview),
            fontSize = 17.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        )
        Spacer(Modifier.height(12.dp))
        TextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.fillMaxWidth().liquidGlassCard(28.dp),
            label = stringResource(R.string.preview_input),
            minLines = 3,
            maxLines = 6,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.preview_result),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = result.ifEmpty { "—" },
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )
    }
}

private enum class HomeEditor {
    SUFFIX,
    RULES,
    EMOTICONS,
}
