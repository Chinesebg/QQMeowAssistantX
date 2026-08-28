package com.example.u7e5f3218e9.config

enum class ProcessingMode(val storedValue: String) {
    PUNCTUATION("punctuation"),
    REALTIME("realtime");

    companion object {
        fun fromStored(value: String?): ProcessingMode =
            entries.firstOrNull { it.storedValue == value } ?: PUNCTUATION
    }
}

enum class ThemeMode(val storedValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStored(value: String?): ThemeMode =
            entries.firstOrNull { it.storedValue == value } ?: SYSTEM
    }
}

enum class AppScopeMode(val storedValue: String) {
    EXCLUDE_SELECTED("exclude_selected"),
    INCLUDE_SELECTED("include_selected");

    companion object {
        fun fromStored(value: String?): AppScopeMode =
            entries.firstOrNull { it.storedValue == value } ?: EXCLUDE_SELECTED
    }
}

enum class InputWriteMode(val storedValue: String) {
    ACCESSIBILITY("accessibility"),
    SHIZUKU("shizuku"),
    ROOT("root");

    companion object {
        fun fromStored(value: String?): InputWriteMode =
            entries.firstOrNull { it.storedValue == value } ?: ACCESSIBILITY
    }
}

data class ReplacementRule(
    val from: String,
    val to: String,
) {
    init {
        require(from.isNotEmpty()) { "Rule source cannot be empty" }
    }

    fun serialize(): String = "$from=$to"

    companion object {
        private val separators = charArrayOf('=', '＝', '→')

        fun parse(line: String?): ReplacementRule? {
            val value = line?.trim().orEmpty()
            if (value.isEmpty()) return null

            val separatorIndex = separators
                .map { value.indexOf(it) }
                .filter { it >= 0 }
                .minOrNull()
                ?: return null

            if (separatorIndex == 0) return null
            val from = value.substring(0, separatorIndex).trim()
            if (from.isEmpty()) return null
            val to = value.substring(separatorIndex + 1).trim()
            return ReplacementRule(from, to)
        }
    }
}

data class AppConfig(
    val enabled: Boolean = true,
    val processingMode: ProcessingMode = ProcessingMode.PUNCTUATION,
    val engineMode: String = "rule",
    val attitude: String = "乖巧",
    val intensity: String = "medium",
    val enableSentenceSuffix: Boolean = true,
    val sentenceSuffix: String = "喵",
    val enableRandomEmoticon: Boolean = false,
    val customEmoticons: List<String> = emptyList(),
    val rules: List<ReplacementRule> = DEFAULT_RULES,
    val appScopeMode: AppScopeMode = AppScopeMode.EXCLUDE_SELECTED,
    val scopedPackages: Set<String> = emptySet(),
    val inputWriteMode: InputWriteMode = InputWriteMode.ACCESSIBILITY,
    val keepAliveEnabled: Boolean = false,
    val rootKeepAliveEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useMonet: Boolean = false,
    val blurEnabled: Boolean = false,
    val floatingBottomBar: Boolean = true,
    val liquidGlassEnabled: Boolean = true,
    val themeColor: String = "red",
    val backgroundUri: String = "",
    val backgroundBlur: Int = 0,
    val backgroundBrightness: Int = 0,
    val dialogBlur: Int = 24,
    val fixedBottomBar: Boolean = true,
    val aiToastEnabled: Boolean = true,
    val uiStyle: String = "material",
    val cardLiquidGlass: Boolean = false,
) {
    fun isPackageInScope(packageName: String): Boolean {
        val isSelected = packageName in scopedPackages
        return when (appScopeMode) {
            AppScopeMode.EXCLUDE_SELECTED -> !isSelected
            AppScopeMode.INCLUDE_SELECTED -> isSelected
        }
    }

    val activeEmoticons: List<String>
        get() = customEmoticons.ifEmpty { BUILTIN_EMOTICONS }

    companion object {
        val DEFAULT_RULES = listOf(
            ReplacementRule("我", "本喵"),
            ReplacementRule("你", "主人"),
        )

        val BUILTIN_EMOTICONS = listOf(
            "^⌯𖥦⌯^ ੭ ^", "⌯'ㅅ'⌯", "=^𖥦^=", "⌯•ㅅ•⌯",
            "ฅ•̀∀•́ฅ", "ฅ ̳͒•ˑ̫• ̳͒ฅ♡", "ฅ(̳•·̫•̳ฅ)♡", "ฅ^••^ฅ",
            "=^•ω•^=", "₍^ >ヮ<^₎", "/ᐠ - ˕ -マ Ⳋ", "ฅ^•ﻌ•^ฅ",
            "ฅ՞•ﻌ•՞ฅ", "(ฅ´ω`ฅ)", "ฅ(*`ω´*)ฅ", "ฅ꒰ ⸝˶• •˶⸝꒱ฅ",
            "₍˄·͈༝·͈˄*₎◞ ̑̑", "!!^⌯𖥦⌯^ ੭!!", "₍^⸝⸝> ·̫ <⸝⸝ ^₎", "ฅ^._.^ฅ",
            "₍🎀˄•͈༝•͈˄₎ฅ˒˒", "^•͈༝•^ฅ", "꒰ఎ(^ . ֑ .^)໒꒱", "ฅ●ω●ฅ",
            "₍⸍⸌·͈༝·͈⸍⸌₎◞", "(>^ω^<)", "ฅ^-﹃-^ฅ", "^ ̳ට ̫ ට ̳^",
            "୧₍˄·͈༝·͈˄₎୨", "^ ̳ᴗ  ̫ ᴗ ̳^", "˓˓ก(⸍⸌̣ʷ̣̫⸍̣⸌₎ค˒˒", "ヽ(ฅ≧へ≦)ฅ",
            "(`･ω･´)ฅ", "(=^･ᴥ･^=)", "(^ω^ฅ)", "ฅ(≧▽≦)ฅ",
            "ฅ(=´▽`=)ฅ", "ヾ((๑˘ㅂ˘๑)ฅ", "(ฅ◑ω◑ฅ)", "(๑•̀ω•́ฅ)",
            "(ฅ>ω<*ฅ)", "(=^.^=)", "(=´ᴥ`)", "(=ↀωↀ=)",
            "(=^-ω-^=)", "ฅ(*°ω°*ฅ)", "ヽ(=^･ω･^=)丿", "(^•ᴥ•^)",
            "( Φ ω Φ )", "(=^x^=)", "ฅ( ̳• ◡ • ̳)ฅ", "o( =•ω•= )m",
            "~o( =∩ω∩= )m", "≡ω≡",
        )
    }
}
