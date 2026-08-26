package com.example.u7e5f3218e9;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 背景个性化配置：自定义背景图片 URI + 模糊半径 + 明暗度（-100..100，负数提亮、正数压暗）。
 */
public class BackgroundConfig {

    public static final int MAX_BLUR = 50; // dp
    public static final int MIN_BRIGHTNESS = -100;
    public static final int MAX_BRIGHTNESS = 100;

    private static final String PREFS = "background_config";
    private static final String KEY_URI = "bg_uri";
    private static final String KEY_BLUR = "bg_blur";
    private static final String KEY_BRIGHTNESS = "bg_brightness";

    /** 空字符串表示无自定义背景图片 */
    public String uri = "";
    /** 模糊半径（dp），0 = 不模糊 */
    public int blur = 0;
    /** 明暗度（-100 = 最暗，0 = 原图，+100 = 最亮） */
    public int brightness = 0;

    public static BackgroundConfig load(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        BackgroundConfig c = new BackgroundConfig();
        c.uri = sp.getString(KEY_URI, "");
        c.blur = clamp(sp.getInt(KEY_BLUR, 0), 0, MAX_BLUR);
        c.brightness = clamp(sp.getInt(KEY_BRIGHTNESS, 0), MIN_BRIGHTNESS, MAX_BRIGHTNESS);
        return c;
    }

    public void save(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_URI, this.uri == null ? "" : this.uri)
                .putInt(KEY_BLUR, clamp(this.blur, 0, MAX_BLUR))
                .putInt(KEY_BRIGHTNESS, clamp(this.brightness, MIN_BRIGHTNESS, MAX_BRIGHTNESS))
                .apply();
    }

    private static int clamp(int v, int min, int max) {
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }
}
