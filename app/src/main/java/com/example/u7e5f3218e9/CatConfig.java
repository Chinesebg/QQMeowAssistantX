package com.example.u7e5f3218e9;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

public class CatConfig {
    public static final String KEY_RULES = "rules";
    public static final String KEY_PROCESSING_MODE = "processing_mode";
    public static final String KEY_ENGINE_MODE = "engine_mode";
    public static final String KEY_ATTITUDE = "attitude";
    public static final String KEY_INTENSITY = "intensity";

    public static final String MODE_PUNCTUATION = "punctuation";
    public static final String MODE_REALTIME = "realtime";
    public static final String MODE_ENGINE_RULE = "rule";
    public static final String MODE_ENGINE_HYBRID = "hybrid";
    public static final String MODE_ENGINE_AI = "ai";

    /** 默认态度 / 强度（与 RuleEngine 常量保持一致） */
    public static final String DEFAULT_ATTITUDE = RuleEngine.ATTITUDE_OBEDIENT;
    public static final String DEFAULT_INTENSITY = RuleEngine.INTENSITY_MEDIUM;

    private static final String PREFS_NAME = "cat_config";

    public static class Rule {
        public final String from;
        public final String to;

        public Rule(String from, String to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public String toString() {
            return from + "=" + to;
        }
    }

    public String processingMode = MODE_PUNCTUATION;
    public String engineMode = MODE_ENGINE_RULE;
    public String attitude = DEFAULT_ATTITUDE;
    public String intensity = DEFAULT_INTENSITY;
    public List<Rule> rules = new ArrayList<>();

    public static Rule parseRule(String line) {
        if (line == null) {
            return null;
        }
        String s = line.trim();
        if (s.isEmpty()) {
            return null;
        }
        String separators = "=＝→";
        int idx = -1;
        for (int i = 0; i < separators.length(); i++) {
            int p = s.indexOf(separators.charAt(i));
            if (p >= 0 && (idx < 0 || p < idx)) {
                idx = p;
            }
        }
        if (idx <= 0) {
            return null;
        }
        String from = s.substring(0, idx).trim();
        String to = s.substring(idx + 1).trim();
        if (from.isEmpty()) {
            return null;
        }
        return new Rule(from, to);
    }

    public static String rulesToString(List<Rule> rules) {
        StringBuilder sb = new StringBuilder();
        if (rules != null) {
            for (Rule r : rules) {
                if (r == null || r.from.isEmpty()) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(r.from).append('=').append(r.to);
            }
        }
        return sb.toString();
    }

    public static CatConfig load(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, 0);
        CatConfig cfg = new CatConfig();
        cfg.processingMode = sp.getString(KEY_PROCESSING_MODE, MODE_PUNCTUATION);
        cfg.engineMode = sp.getString(KEY_ENGINE_MODE, MODE_ENGINE_RULE);
        cfg.attitude = sp.getString(KEY_ATTITUDE, DEFAULT_ATTITUDE);
        cfg.intensity = sp.getString(KEY_INTENSITY, DEFAULT_INTENSITY);

        String rulesStr = sp.getString(KEY_RULES, "");
        if (rulesStr != null && !rulesStr.trim().isEmpty()) {
            List<Rule> list = new ArrayList<>();
            for (String line : rulesStr.split("\n")) {
                Rule r = parseRule(line);
                if (r != null) {
                    list.add(r);
                }
            }
            cfg.rules = list;
        }
        return cfg;
    }

    public void save(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(KEY_PROCESSING_MODE, this.processingMode == null ? MODE_PUNCTUATION : this.processingMode);
        ed.putString(KEY_ENGINE_MODE, this.engineMode == null ? MODE_ENGINE_RULE : this.engineMode);
        ed.putString(KEY_ATTITUDE, this.attitude == null ? DEFAULT_ATTITUDE : this.attitude);
        ed.putString(KEY_INTENSITY, this.intensity == null ? DEFAULT_INTENSITY : this.intensity);
        ed.putString(KEY_RULES, rulesToString(this.rules));
        ed.apply();
    }
}
