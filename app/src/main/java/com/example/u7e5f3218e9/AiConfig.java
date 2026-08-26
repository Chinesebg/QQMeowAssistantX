package com.example.u7e5f3218e9;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * AI 引擎配置（OpenAI 兼容接口）。参照 nyabox 的 AI 配置项。
 */
public class AiConfig {

    public static final String DEFAULT_BASE_URL = "https://api.deepseek.com/v1";
    public static final String DEFAULT_MODEL = "deepseek-chat";

    private static final String PREFS = "ai_config";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_MODEL = "model";
    private static final String KEY_TEMPERATURE = "temperature";

    public String baseUrl = DEFAULT_BASE_URL;
    public String apiKey = "";
    public String model = DEFAULT_MODEL;
    public float temperature = 0.8f;

    public static AiConfig load(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, 0);
        AiConfig c = new AiConfig();
        c.baseUrl = sp.getString(KEY_BASE_URL, DEFAULT_BASE_URL);
        c.apiKey = sp.getString(KEY_API_KEY, "");
        c.model = sp.getString(KEY_MODEL, DEFAULT_MODEL);
        c.temperature = sp.getFloat(KEY_TEMPERATURE, 0.8f);
        return c;
    }

    public void save(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, 0);
        sp.edit()
                .putString(KEY_BASE_URL, this.baseUrl == null ? DEFAULT_BASE_URL : this.baseUrl)
                .putString(KEY_API_KEY, this.apiKey == null ? "" : this.apiKey)
                .putString(KEY_MODEL, this.model == null ? DEFAULT_MODEL : this.model)
                .putFloat(KEY_TEMPERATURE, this.temperature)
                .apply();
    }
}
