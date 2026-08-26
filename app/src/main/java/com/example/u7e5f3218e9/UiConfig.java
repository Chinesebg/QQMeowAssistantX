package com.example.u7e5f3218e9;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * UI 行为偏好：底栏固定、AI 处理 toast 通知等（参照 BackgroundConfig 的写法）。
 */
public final class UiConfig {

    private static final String PREFS = "ui_settings";
    private static final String KEY_FIXED_BOTTOM_BAR = "fixed_bottom_bar";
    private static final String KEY_AI_TOAST = "ai_toast";
    private static final String KEY_DIALOG_BLUR = "dialog_blur";

    /** 弹窗背景模糊半径上限。 */
    public static final int MAX_DIALOG_BLUR = 50;

    private UiConfig() {
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** 底栏是否固定显示。默认 true（固定），关闭后滑动时自动隐藏。 */
    public static boolean fixedBottomBar(Context ctx) {
        return prefs(ctx).getBoolean(KEY_FIXED_BOTTOM_BAR, true);
    }

    public static void setFixedBottomBar(Context ctx, boolean fixed) {
        prefs(ctx).edit().putBoolean(KEY_FIXED_BOTTOM_BAR, fixed).apply();
    }

    /** AI / 混合模式处理开始时是否弹 toast。默认 true。 */
    public static boolean aiToastEnabled(Context ctx) {
        return prefs(ctx).getBoolean(KEY_AI_TOAST, true);
    }

    public static void setAiToastEnabled(Context ctx, boolean enabled) {
        prefs(ctx).edit().putBoolean(KEY_AI_TOAST, enabled).apply();
    }

    /** 弹窗背景模糊半径（dp），0 表示关闭。默认 24。 */
    public static int dialogBlur(Context ctx) {
        return prefs(ctx).getInt(KEY_DIALOG_BLUR, 24);
    }

    public static void setDialogBlur(Context ctx, int radius) {
        prefs(ctx).edit().putInt(KEY_DIALOG_BLUR, radius).apply();
    }
}
