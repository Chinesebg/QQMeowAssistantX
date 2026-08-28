package com.example.u7e5f3218e9;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 态度 × 强度的确定性规则引擎（参照 nyabox 的 engine.py + pipeline/*.py + data/default.yaml）。
 *
 * 五步流水线：句子切分 → 词汇映射(lexicon) → 语气词(tone) → 标点(punct) → 颜文字(emoticon)。
 * 态度（乖巧 / 傲娇 / 亲昵）决定一整套规则；强度（light / medium / heavy）决定每步的档位。
 */
public final class RuleEngine {

    public static final String ATTITUDE_OBEDIENT = "乖巧";
    public static final String ATTITUDE_TSUN = "傲娇";
    public static final String ATTITUDE_CLINGY = "亲昵";

    public static final String INTENSITY_LIGHT = "light";
    public static final String INTENSITY_MEDIUM = "medium";
    public static final String INTENSITY_HEAVY = "heavy";

    /** 与 ATTITUDES 一一对应的展示名 */
    public static final String[] ATTITUDES = {ATTITUDE_OBEDIENT, ATTITUDE_TSUN, ATTITUDE_CLINGY};
    public static final String[] ATTITUDE_NAMES = {"乖巧", "傲娇", "亲昵"};

    /** 与 INTENSITIES 一一对应的展示名 */
    public static final String[] INTENSITIES = {INTENSITY_LIGHT, INTENSITY_MEDIUM, INTENSITY_HEAVY};
    public static final String[] INTENSITY_NAMES = {"轻度", "中度", "重度"};

    /** 三套规则颜文字的并集（供无障碍服务剥离本应用写入的颜文字） */
    public static final String[] ALL_EMOTICONS = {
            "(=^･ω･^=)", "(>ω<)", "(๑•̀ㅂ•́)و✧",
            "(￣^￣)", "(｀へ´)", "(๑•̀ㅁ•́ฅ)",
            "(｡･ω･｡)", "(≧▽≦)"
    };

    /** 规则模式自定义颜文字的内置默认库。 */
    public static final String[] CUSTOM_BUILTIN_EMOTICONS = {
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
            "~o( =∩ω∩= )m", "≡ω≡"
    };

    private static final Pattern SENT_END_RE = Pattern.compile("[。！？!?；;]+");
    private static final Pattern END_PUNCT_RE = Pattern.compile("[。！？!?；;]+$");
    private static final Pattern CAT_TAIL_RE = Pattern.compile("(喵|喵呜|喵喵)(~|～)?$");
    private static final String CLOSING_CHARS = "\"'」』)）";

    // ---- 数据模型 ----

    /** 一条词汇映射规则。dst 三档中 null 表示该强度不替换。 */
    static final class LexRule {
        final String name;
        final String src;
        final String light;
        final String medium;
        final String heavy;

        LexRule(String name, String src, String light, String medium, String heavy) {
            this.name = name;
            this.src = src;
            this.light = light;
            this.medium = medium;
            this.heavy = heavy;
        }
    }

    /** 单套完整规则。 */
    static final class RuleSet {
        final List<LexRule> lexicon;
        final Map<String, String> tone;
        final Map<String, String> punct;
        final String[] emoticons;
        final Map<String, String> placement;

        RuleSet(List<LexRule> lexicon, Map<String, String> tone, Map<String, String> punct,
                String[] emoticons, Map<String, String> placement) {
            this.lexicon = lexicon;
            this.tone = tone;
            this.punct = punct;
            this.emoticons = emoticons;
            this.placement = placement;
        }
    }

    private static final Map<String, RuleSet> DEFAULT_RULES = buildDefaultRules();

    private RuleEngine() {
    }

    // ---- 公开 API ----

    /**
     * 完整规则转换。customRules 为「文本替换规则」前置自定义替换；withEmoticon=false 时跳过颜文字
     * 步骤（实时处理模式用于避免逐字刷颜文字）。
     */
    public static String convert(String text, String intensity, String attitude,
                                 List<CatConfig.Rule> customRules, boolean withEmoticon) {
        return convert(text, intensity, attitude, customRules, withEmoticon, false, null, false, null);
    }

    /**
     * 完整规则转换。自定义句末文字和自定义颜文字作为规则模式的高优先级覆盖项：
     * 自定义句末文字覆盖内置语气词；自定义颜文字覆盖内置颜文字库。
     */
    public static String convert(String text, String intensity, String attitude,
                                 List<CatConfig.Rule> customRules, boolean withEmoticon,
                                 boolean customTailEnabled, String tailText,
                                 boolean customEmoticonEnabled, String customEmoticonsText) {
        if (text == null || text.trim().isEmpty()) {
            return text == null ? "" : text;
        }
        String att = normalizeAttitude(attitude);
        String inten = normalizeIntensity(intensity);
        String t = applyCustomRules(text.trim(), customRules);
        RuleSet rs = DEFAULT_RULES.get(att);

        List<String> sentences = splitSentences(t);
        sentences = applyLexicon(sentences, rs, inten);
        if (customTailEnabled) {
            sentences = applyTail(sentences, tailText);
        } else {
            sentences = applyTone(sentences, toneFor(rs, inten));
        }
        sentences = applyPunct(sentences, punctFor(rs, inten));
        if (withEmoticon) {
            if (customEmoticonEnabled) {
                String[] customEmoticons = parseEmoticons(customEmoticonsText);
                if (customEmoticons.length > 0) {
                    sentences = applyEmoticon(sentences, customEmoticons, placementFor(rs, inten));
                }
            } else {
                sentences = applyEmoticon(sentences, rs.emoticons, placementFor(rs, inten));
            }
        }
        return rstrip(join(sentences));
    }

    /** 混合模式预处理：仅自定义替换 + 词汇映射（人称等硬性替换），不做语气词/标点/颜文字。 */
    public static String preprocess(String text, String intensity, String attitude,
                                    List<CatConfig.Rule> customRules) {
        if (text == null || text.trim().isEmpty()) {
            return text == null ? "" : text;
        }
        String att = normalizeAttitude(attitude);
        String inten = normalizeIntensity(intensity);
        String t = applyCustomRules(text.trim(), customRules);
        RuleSet rs = DEFAULT_RULES.get(att);
        return join(applyLexicon(splitSentences(t), rs, inten));
    }

    /** 混合模式后处理：逐句补默认「喵」收尾（句尾已有喵/喵呜/喵喵，可带波浪线，则不重复）。 */
    public static String finish(String text) {
        return finish(text, CatConfig.DEFAULT_TAIL_TEXT);
    }

    /** 混合模式后处理：逐句补自定义句末文字（句尾已有相同文字则不重复）。 */
    public static String finish(String text, String tailText) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String tail = tailText == null || tailText.trim().isEmpty() ? CatConfig.DEFAULT_TAIL_TEXT : tailText.trim();
        List<String> sentences = splitSentences(text);
        StringBuilder out = new StringBuilder();
        for (String sentence : sentences) {
            Matcher m = END_PUNCT_RE.matcher(sentence);
            if (m.find()) {
                String body = sentence.substring(0, m.start());
                if (!hasTail(body, tail)) {
                    sentence = body + tail + sentence.substring(m.start());
                }
            } else if (!hasTail(sentence, tail)) {
                sentence = sentence + tail;
            }
            out.append(sentence);
        }
        return rstrip(out.toString());
    }

    private static boolean hasTail(String text, String tail) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        if (CatConfig.DEFAULT_TAIL_TEXT.equals(tail)) {
            return CAT_TAIL_RE.matcher(text).find();
        }
        return tail.equals(text.substring(Math.max(0, text.length() - tail.length())));
    }

    private static List<String> applyTail(List<String> sentences, String tailText) {
        String tail = tailText == null || tailText.trim().isEmpty() ? CatConfig.DEFAULT_TAIL_TEXT : tailText.trim();
        List<String> out = new ArrayList<>();
        for (String sentence : sentences) {
            Matcher m = END_PUNCT_RE.matcher(sentence);
            if (m.find()) {
                String body = sentence.substring(0, m.start());
                if (!hasTail(body, tail)) {
                    sentence = body + tail + sentence.substring(m.start());
                }
            } else if (!hasTail(sentence, tail)) {
                sentence = sentence + tail;
            }
            out.add(sentence);
        }
        return out;
    }

    private static String[] parseEmoticons(String text) {
        if (text == null) {
            return new String[0];
        }
        List<String> list = new ArrayList<>();
        for (String line : text.split("\n")) {
            String s = line.trim();
            if (!s.isEmpty()) {
                list.add(s);
            }
        }
        return list.toArray(new String[0]);
    }

    /** 仅应用文本替换规则，然后在每个句末追加自定义文字（规则模式禁用时使用）。 */
    public static String appendTail(String text, String tailText) {
        if (text == null || text.isEmpty()) {
            return text == null ? "" : text;
        }
        return finish(text, tailText);
    }

    // ---- 辅助查询（供 UI 弹窗预选） ----

    public static int attitudeIndexOf(String attitude) {
        String cur = normalizeAttitude(attitude);
        for (int i = 0; i < ATTITUDES.length; i++) {
            if (ATTITUDES[i].equals(cur)) {
                return i;
            }
        }
        return 0;
    }

    public static int intensityIndexOf(String intensity) {
        String cur = normalizeIntensity(intensity);
        for (int i = 0; i < INTENSITIES.length; i++) {
            if (INTENSITIES[i].equals(cur)) {
                return i;
            }
        }
        return 1;
    }

    private static String normalizeAttitude(String attitude) {
        if (attitude != null) {
            for (String a : ATTITUDES) {
                if (a.equals(attitude)) {
                    return a;
                }
            }
        }
        return ATTITUDE_OBEDIENT;
    }

    private static String normalizeIntensity(String intensity) {
        if (intensity != null) {
            for (String i : INTENSITIES) {
                if (i.equals(intensity)) {
                    return i;
                }
            }
        }
        return INTENSITY_MEDIUM;
    }

    private static String applyCustomRules(String text, List<CatConfig.Rule> customRules) {
        if (customRules == null || customRules.isEmpty()) {
            return text;
        }
        String t = text;
        for (CatConfig.Rule rule : customRules) {
            if (rule == null || rule.from == null || rule.from.isEmpty() || rule.to == null) {
                continue;
            }
            t = t.replace(rule.from, rule.to);
        }
        return t;
    }

    // ---- 流水线 ----

    private static List<String> splitSentences(String text) {
        String[] parts = SENT_END_RE.split(text, -1);
        List<String> ends = new ArrayList<>();
        Matcher m = SENT_END_RE.matcher(text);
        while (m.find()) {
            ends.add(m.group());
        }
        List<String> sentences = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            String segment = parts[i] + (i < ends.size() ? ends.get(i) : "");
            if (!segment.isEmpty()) {
                sentences.add(segment);
            }
        }
        return mergeClosingQuotes(sentences);
    }

    private static List<String> mergeClosingQuotes(List<String> sentences) {
        List<String> merged = new ArrayList<>();
        for (String segment : sentences) {
            if (!merged.isEmpty() && !segment.isEmpty() && isAllClosing(segment)
                    && isSentEndChar(lastChar(merged.get(merged.size() - 1)))) {
                int last = merged.size() - 1;
                merged.set(last, merged.get(last) + segment);
            } else {
                merged.add(segment);
            }
        }
        return merged;
    }

    private static boolean isAllClosing(String s) {
        String t = s.trim();
        if (t.isEmpty()) {
            return false;
        }
        for (int i = 0; i < t.length(); i++) {
            if (CLOSING_CHARS.indexOf(t.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSentEndChar(char c) {
        return SENT_END_RE.matcher(String.valueOf(c)).matches();
    }

    private static char lastChar(String s) {
        return s.charAt(s.length() - 1);
    }

    private static List<String> applyLexicon(List<String> sentences, RuleSet rules, String intensity) {
        List<LexRule> enabled = new ArrayList<>();
        for (LexRule r : rules.lexicon) {
            if (dstFor(r, intensity) != null) {
                enabled.add(r);
            }
        }
        if (enabled.isEmpty()) {
            return new ArrayList<>(sentences);
        }
        Collections.sort(enabled, new Comparator<LexRule>() {
            @Override
            public int compare(LexRule a, LexRule b) {
                return b.src.length() - a.src.length();
            }
        });
        List<String> out = new ArrayList<>();
        for (String sentence : sentences) {
            String work = sentence;
            Map<String, String> marks = new LinkedHashMap<>();
            for (int idx = 0; idx < enabled.size(); idx++) {
                LexRule r = enabled.get(idx);
                if (work.contains(r.src)) {
                    String mark = "#MB" + idx + "#";
                    marks.put(mark, dstFor(r, intensity));
                    work = work.replace(r.src, mark);
                }
            }
            for (Map.Entry<String, String> e : marks.entrySet()) {
                work = work.replace(e.getKey(), e.getValue());
            }
            out.add(work);
        }
        return out;
    }

    private static List<String> applyTone(List<String> sentences, String word) {
        List<String> out = new ArrayList<>();
        for (String s : sentences) {
            Matcher m = END_PUNCT_RE.matcher(s);
            if (m.find()) {
                out.add(s.substring(0, m.start()) + word + s.substring(m.start()));
            } else {
                out.add(s + word);
            }
        }
        return out;
    }

    private static List<String> applyPunct(List<String> sentences, String mode) {
        if ("none".equals(mode)) {
            return new ArrayList<>(sentences);
        }
        List<String> out = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            String s = sentences.get(i);
            if (("all".equals(mode) || ("odd".equals(mode) && i % 2 == 0)) && s.endsWith("。")) {
                s = s.substring(0, s.length() - 1) + "~ ";
            }
            out.add(s);
        }
        return out;
    }

    private static List<String> applyEmoticon(List<String> sentences, String[] emoticons, String placement) {
        if (sentences.isEmpty() || "none".equals(placement) || emoticons == null || emoticons.length == 0) {
            return new ArrayList<>(sentences);
        }
        List<String> out = new ArrayList<>();
        if ("once".equals(placement)) {
            out = new ArrayList<>(sentences);
            int last = out.size() - 1;
            out.set(last, rstrip(out.get(last)) + " " + emoticons[0] + " ");
        } else {
            for (int i = 0; i < sentences.size(); i++) {
                out.add(rstrip(sentences.get(i)) + " " + emoticons[i % emoticons.length] + " ");
            }
        }
        return out;
    }

    // ---- 规则数据（逐字移植 nyabox/nyabox/data/default.yaml） ----

    private static String dstFor(LexRule r, String intensity) {
        if (INTENSITY_LIGHT.equals(intensity)) {
            return r.light;
        }
        if (INTENSITY_HEAVY.equals(intensity)) {
            return r.heavy;
        }
        return r.medium;
    }

    private static String toneFor(RuleSet rs, String intensity) {
        String v = rs.tone.get(intensity);
        return v != null ? v : rs.tone.get(INTENSITY_MEDIUM);
    }

    private static String punctFor(RuleSet rs, String intensity) {
        String v = rs.punct.get(intensity);
        return v != null ? v : rs.punct.get(INTENSITY_MEDIUM);
    }

    private static String placementFor(RuleSet rs, String intensity) {
        String v = rs.placement.get(intensity);
        return v != null ? v : rs.placement.get(INTENSITY_MEDIUM);
    }

    private static Map<String, RuleSet> buildDefaultRules() {
        Map<String, RuleSet> m = new LinkedHashMap<>();

        m.put(ATTITUDE_OBEDIENT, new RuleSet(
                Arrays.asList(
                        lex("you", "你", null, "主人", "主人"),
                        lex("you_guys", "你们", null, "主人和主人", "主人和主人"),
                        lex("i", "我", null, null, "人家"),
                        lex("we", "我们", null, null, "咱们"),
                        lex("okay", "好的", null, "好哒", "好喵"),
                        lex("hello", "你好", null, "喵呜", "喵呜"),
                        lex("bye", "再见", null, "拜拜喵", "拜拜喵")
                ),
                map("喵", "喵", "喵"),
                map("none", "odd", "all"),
                new String[]{"(=^･ω･^=)", "(>ω<)", "(๑•̀ㅂ•́)و✧"},
                map("none", "once", "every")
        ));

        m.put(ATTITUDE_TSUN, new RuleSet(
                Arrays.asList(
                        lex("hello", "你好", null, "喂", "喂"),
                        lex("you", "你", null, "笨蛋", "笨蛋"),
                        lex("you_guys", "你们", null, "笨蛋们", "笨蛋们"),
                        lex("i", "我", null, null, "本小姐"),
                        lex("we", "我们", null, null, "本小姐和笨蛋们"),
                        lex("okay", "好的", null, "哼，知道了", "哼，知道了"),
                        lex("bye", "再见", null, "哼，再见啦", "哼，再见啦")
                ),
                map("哼", "哼", "哼"),
                map("none", "odd", "all"),
                new String[]{"(￣^￣)", "(｀へ´)", "(๑•̀ㅁ•́ฅ)"},
                map("none", "once", "every")
        ));

        m.put(ATTITUDE_CLINGY, new RuleSet(
                Arrays.asList(
                        lex("you", "你", null, "亲爱的", "亲爱的"),
                        lex("you_guys", "你们", null, "亲爱的们", "亲爱的们"),
                        lex("i", "我", null, null, "人家"),
                        lex("we", "我们", null, null, "咱们"),
                        lex("okay", "好的", null, "好嘛", "好嘛"),
                        lex("hello", "你好", null, "亲爱的", "亲爱的"),
                        lex("bye", "再见", null, "抱抱，再见", "抱抱，再见")
                ),
                map("喵~", "喵~", "喵呜~"),
                map("none", "odd", "all"),
                new String[]{"(｡･ω･｡)", "(๑•̀ㅂ•́)و✧", "(≧▽≦)"},
                map("none", "once", "every")
        ));

        return m;
    }

    private static LexRule lex(String name, String src, String light, String medium, String heavy) {
        return new LexRule(name, src, light, medium, heavy);
    }

    private static Map<String, String> map(String light, String medium, String heavy) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(INTENSITY_LIGHT, light);
        m.put(INTENSITY_MEDIUM, medium);
        m.put(INTENSITY_HEAVY, heavy);
        return m;
    }

    // ---- 工具 ----

    private static String join(List<String> parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            sb.append(p);
        }
        return sb.toString();
    }

    private static String rstrip(String s) {
        int end = s.length();
        while (end > 0 && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }
        return s.substring(0, end);
    }
}
