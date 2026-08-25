package com.example.u7e5f3218e9;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * 主题色 + 深浅色模式 管理器。
 * 供 MainActivity / AboutActivity 在 super.onCreate 之前调用 {@link #themeRes(Context)} 应用主题色；
 * 深浅色模式由 {@link App} 在 Application 层通过 {@link #nightMode(Context)} 统一设置。
 */
public final class ThemeManager {

    private static final String PREFS = "ui_config";
    private static final String KEY_THEME = "theme";
    private static final String KEY_NIGHT = "night_mode";

    // ---- 主题色 ----
    public static final String THEME_RED = "red";
    public static final String THEME_BLUE = "blue";
    public static final String THEME_GREEN = "green";
    public static final String THEME_PINK = "pink";

    /** 展示名（用于主题色单选弹窗） */
    public static final String[] COLOR_NAMES = {"新年红", "鲸鱼蓝", "原野绿", "昔涟粉"};
    /** 与 COLOR_NAMES 一一对应的取值 */
    public static final String[] COLOR_VALUES = {THEME_RED, THEME_BLUE, THEME_GREEN, THEME_PINK};

    // ---- 深浅色模式 ----
    public static final String NIGHT_SYSTEM = "system";
    public static final String NIGHT_LIGHT = "light";
    public static final String NIGHT_DARK = "dark";

    /** 展示名（用于深浅色模式单选弹窗） */
    public static final String[] NIGHT_NAMES = {"跟随系统", "浅色", "深色"};
    /** 与 NIGHT_NAMES 一一对应的取值 */
    public static final String[] NIGHT_VALUES = {NIGHT_SYSTEM, NIGHT_LIGHT, NIGHT_DARK};

    private ThemeManager() {
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ---- 主题色 ----

    public static String currentColor(Context ctx) {
        return prefs(ctx).getString(KEY_THEME, THEME_RED);
    }

    /** 返回当前主题色对应的 style 资源（浅/深色由 values / values-night 资源限定自动切换） */
    public static int themeRes(Context ctx) {
        switch (currentColor(ctx)) {
            case THEME_BLUE:
                return R.style.Theme_QQMeow_Blue;
            case THEME_GREEN:
                return R.style.Theme_QQMeow_Green;
            case THEME_PINK:
                return R.style.Theme_QQMeow_Pink;
            default:
                return R.style.Theme_QQMeow_Red;
        }
    }

    public static void saveColor(Context ctx, String color) {
        prefs(ctx).edit().putString(KEY_THEME, color).apply();
    }

    /** 当前主题色在单选列表中的索引（用于弹窗预选中） */
    public static int colorIndexOf(Context ctx) {
        String cur = currentColor(ctx);
        for (int i = 0; i < COLOR_VALUES.length; i++) {
            if (COLOR_VALUES[i].equals(cur)) {
                return i;
            }
        }
        return 0;
    }

    // ---- 深浅色模式 ----

    public static String currentNight(Context ctx) {
        return prefs(ctx).getString(KEY_NIGHT, NIGHT_SYSTEM);
    }

    /** 返回 AppCompatDelegate 的 night mode（参照 LibChecker 的 NightModeResolver） */
    public static int nightMode(Context ctx) {
        switch (currentNight(ctx)) {
            case NIGHT_LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case NIGHT_DARK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

    public static void saveNight(Context ctx, String night) {
        prefs(ctx).edit().putString(KEY_NIGHT, night).apply();
    }

    /** 当前深浅色模式在单选列表中的索引（用于弹窗预选中） */
    public static int nightIndexOf(Context ctx) {
        String cur = currentNight(ctx);
        for (int i = 0; i < NIGHT_VALUES.length; i++) {
            if (NIGHT_VALUES[i].equals(cur)) {
                return i;
            }
        }
        return 0;
    }
}
