package com.example.u7e5f3218e9.config

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.u7e5f3218e9.CatConfig
import com.example.u7e5f3218e9.UiConfig

class ConfigRepository(context: Context) {
    private val appContext = context.applicationContext
    private val catPrefs = appContext.getSharedPreferences(CAT_PREFS, Context.MODE_PRIVATE)
    private val uiPrefs = appContext.getSharedPreferences(UI_PREFS, Context.MODE_PRIVATE)
    private val themePrefs = appContext.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
    private val bgPrefs = appContext.getSharedPreferences(BG_PREFS, Context.MODE_PRIVATE)

    fun load(): AppConfig = AppConfig(
        enabled = catPrefs.getBoolean(KEY_ENABLED, true),
        processingMode = ProcessingMode.fromStored(catPrefs.getString(CatConfig.KEY_PROCESSING_MODE, null)),
        engineMode = catPrefs.getString(CatConfig.KEY_ENGINE_MODE, CatConfig.MODE_ENGINE_RULE)
            ?: CatConfig.MODE_ENGINE_RULE,
        attitude = catPrefs.getString(CatConfig.KEY_ATTITUDE, CatConfig.DEFAULT_ATTITUDE)
            ?: CatConfig.DEFAULT_ATTITUDE,
        intensity = catPrefs.getString(CatConfig.KEY_INTENSITY, CatConfig.DEFAULT_INTENSITY)
            ?: CatConfig.DEFAULT_INTENSITY,
        enableSentenceSuffix = catPrefs.getBoolean(CatConfig.KEY_TAIL_ENABLED, false),
        sentenceSuffix = catPrefs.getString(CatConfig.KEY_TAIL_TEXT, "喵").orEmpty().ifEmpty { "喵" },
        enableRandomEmoticon = catPrefs.getBoolean(CatConfig.KEY_EMOTICON_ENABLED, false),
        customEmoticons = parseNonBlankLines(catPrefs.getString(CatConfig.KEY_CUSTOM_EMOTICONS, null)),
        rules = parseRules(catPrefs.getString(CatConfig.KEY_RULES, null)).first,
        appScopeMode = AppScopeMode.fromStored(catPrefs.getString(KEY_APP_SCOPE_MODE, null)),
        scopedPackages = catPrefs.getStringSet(KEY_SCOPED_PACKAGES, emptySet()).orEmpty(),
        inputWriteMode = InputWriteMode.ACCESSIBILITY,
        keepAliveEnabled = false,
        rootKeepAliveEnabled = false,
        themeMode = ThemeMode.fromStored(themePrefs.getString(KEY_THEME_MODE, null)),
        useMonet = uiPrefs.getBoolean(KEY_USE_MONET, false),
        blurEnabled = uiPrefs.getBoolean(KEY_BLUR_ENABLED, false),
        floatingBottomBar = uiPrefs.getBoolean(KEY_FLOATING_BOTTOM_BAR, true),
        liquidGlassEnabled = uiPrefs.getBoolean(KEY_LIQUID_GLASS, true),
        themeColor = themePrefs.getString(KEY_THEME_COLOR, "red") ?: "red",
        backgroundUri = bgPrefs.getString(KEY_BG_URI, "") ?: "",
        backgroundBlur = bgPrefs.getInt(KEY_BG_BLUR, 0),
        backgroundBrightness = bgPrefs.getInt(KEY_BG_BRIGHTNESS, 0),
        dialogBlur = uiPrefs.getInt(KEY_DIALOG_BLUR, 24),
        fixedBottomBar = uiPrefs.getBoolean(KEY_FIXED_BOTTOM_BAR, true),
        aiToastEnabled = uiPrefs.getBoolean(KEY_AI_TOAST, true),
        uiStyle = uiPrefs.getString(KEY_UI_STYLE, UiConfig.UI_MATERIAL) ?: UiConfig.UI_MATERIAL,
        cardLiquidGlass = uiPrefs.getBoolean(KEY_CARD_LIQUID_GLASS, false),
    )

    fun save(config: AppConfig, synchronous: Boolean = false) {
        catPrefs.edit(commit = synchronous) {
            putBoolean(KEY_ENABLED, config.enabled)
            putString(CatConfig.KEY_PROCESSING_MODE, config.processingMode.storedValue)
            putString(CatConfig.KEY_ENGINE_MODE, config.engineMode)
            putString(CatConfig.KEY_ATTITUDE, config.attitude)
            putString(CatConfig.KEY_INTENSITY, config.intensity)
            putBoolean(CatConfig.KEY_TAIL_ENABLED, config.enableSentenceSuffix)
            putString(CatConfig.KEY_TAIL_TEXT, config.sentenceSuffix.ifEmpty { "喵" })
            putBoolean(CatConfig.KEY_EMOTICON_ENABLED, config.enableRandomEmoticon)
            putString(CatConfig.KEY_CUSTOM_EMOTICONS, config.customEmoticons.joinToString("\n"))
            putString(CatConfig.KEY_RULES, config.rules.joinToString("\n") { it.serialize() })
            putString(KEY_APP_SCOPE_MODE, config.appScopeMode.storedValue)
            putStringSet(KEY_SCOPED_PACKAGES, config.scopedPackages)
        }
        themePrefs.edit(commit = synchronous) {
            putString(KEY_THEME_MODE, config.themeMode.storedValue)
            putString(KEY_THEME_COLOR, config.themeColor)
        }
        bgPrefs.edit(commit = synchronous) {
            putString(KEY_BG_URI, config.backgroundUri)
            putInt(KEY_BG_BLUR, config.backgroundBlur.coerceIn(0, 50))
            putInt(KEY_BG_BRIGHTNESS, config.backgroundBrightness.coerceIn(-100, 100))
        }
        uiPrefs.edit(commit = synchronous) {
            putBoolean(KEY_USE_MONET, config.useMonet)
            putBoolean(KEY_BLUR_ENABLED, config.blurEnabled)
            putBoolean(KEY_FLOATING_BOTTOM_BAR, config.floatingBottomBar)
            putBoolean(KEY_LIQUID_GLASS, config.liquidGlassEnabled)
            putInt(KEY_DIALOG_BLUR, config.dialogBlur.coerceIn(0, 50))
            putBoolean(KEY_FIXED_BOTTOM_BAR, config.fixedBottomBar)
            putBoolean(KEY_AI_TOAST, config.aiToastEnabled)
            putString(KEY_UI_STYLE, config.uiStyle)
            putBoolean(KEY_CARD_LIQUID_GLASS, config.cardLiquidGlass)
        }
    }

    fun registerListener(onConfigChanged: (AppConfig) -> Unit): SharedPreferences.OnSharedPreferenceChangeListener {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            onConfigChanged(load())
        }
        catPrefs.registerOnSharedPreferenceChangeListener(listener)
        uiPrefs.registerOnSharedPreferenceChangeListener(listener)
        themePrefs.registerOnSharedPreferenceChangeListener(listener)
        bgPrefs.registerOnSharedPreferenceChangeListener(listener)
        return listener
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        catPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        uiPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        themePrefs.unregisterOnSharedPreferenceChangeListener(listener)
        bgPrefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        private const val CAT_PREFS = "cat_config"
        private const val UI_PREFS = "ui_settings"
        private const val THEME_PREFS = "ui_config"
        private const val BG_PREFS = "background_config"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_APP_SCOPE_MODE = "app_scope_mode"
        private const val KEY_SCOPED_PACKAGES = "scoped_packages"
        private const val KEY_THEME_MODE = "night_mode"
        private const val KEY_THEME_COLOR = "theme"
        private const val KEY_USE_MONET = "use_monet"
        private const val KEY_BLUR_ENABLED = "blur_enabled"
        private const val KEY_FLOATING_BOTTOM_BAR = "floating_bottom_bar"
        private const val KEY_LIQUID_GLASS = "liquid_glass"
        private const val KEY_DIALOG_BLUR = "dialog_blur"
        private const val KEY_FIXED_BOTTOM_BAR = "fixed_bottom_bar"
        private const val KEY_AI_TOAST = "ai_toast"
        private const val KEY_UI_STYLE = "ui_style"
        private const val KEY_CARD_LIQUID_GLASS = "card_liquid_glass"
        private const val KEY_BG_URI = "bg_uri"
        private const val KEY_BG_BLUR = "bg_blur"
        private const val KEY_BG_BRIGHTNESS = "bg_brightness"

        fun parseRules(value: String?): Pair<List<ReplacementRule>, Int> {
            var invalidCount = 0
            val rules = value.orEmpty().lineSequence().mapNotNull { line ->
                if (line.isBlank()) return@mapNotNull null
                ReplacementRule.parse(line) ?: run {
                    invalidCount += 1
                    null
                }
            }.toList()
            return rules to invalidCount
        }

        fun seedDefaultRules(existingRules: List<ReplacementRule>): List<ReplacementRule> {
            val existingSources = existingRules.mapTo(mutableSetOf(), ReplacementRule::from)
            return AppConfig.DEFAULT_RULES.filterNot { it.from in existingSources } + existingRules
        }

        fun parseNonBlankLines(value: String?): List<String> = value.orEmpty()
            .lineSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
            .toList()
    }
}
