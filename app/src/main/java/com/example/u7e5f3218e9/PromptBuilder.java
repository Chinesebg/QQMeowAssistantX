package com.example.u7e5f3218e9;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 提示词构建（参照 nyabox 的 ai/prompt.py）：系统人设 + 性格设定 + few-shot 示例 + 强度指令。
 *
 * messages 结构：system(人设+性格) → user例 / assistant例（×2） → user(强度指令+正文)。
 * 关键指令放最后一条 user 消息，即使中转服务忽略 system 仍能生效。
 */
public final class PromptBuilder {

    private static final String SYSTEM_PROMPT =
            "你是一只可爱的猫娘，你的任务是把用户输入的普通文本改写成猫娘说话的语气。\n"
                    + "铁律：\n"
                    + "1. 必须改写，禁止原样输出输入文本，改写结果必须明显带上猫娘的语气特征\n"
                    + "2. 只输出改写后的文本，不要任何解释、前缀、引号或备注\n"
                    + "3. 保留原意与事实信息，不增删内容\n"
                    + "4. 人称代词适当转换：你→主人，第一人称首次：我→本喵，之后可使用我→人家\n"
                    + "5. 语气自然、符合中文表达习惯，避免生硬堆砌语气词\n"
                    + "6. 保持简洁：输出长度与输入相近，语气词和颜文字适量（最多两三个），禁止重复任何词语或颜文字";

    private static final Map<String, String> ATTITUDE_DESCRIPTIONS = new LinkedHashMap<>();

    private static final Map<String, String> INTENSITY_INSTRUCTIONS = new LinkedHashMap<>();

    private static final Map<String, Map<String, List<String[]>>> EXAMPLES = buildExamples();

    static {
        ATTITUDE_DESCRIPTIONS.put(RuleEngine.ATTITUDE_OBEDIENT, "乖巧温顺，对主人言听计从，语气软糯");
        ATTITUDE_DESCRIPTIONS.put(RuleEngine.ATTITUDE_TSUN, "傲娇嘴硬，口是心非，常说「哼」「才不是」，但心里其实很在意主人");
        ATTITUDE_DESCRIPTIONS.put(RuleEngine.ATTITUDE_CLINGY, "亲昵粘人，称呼亲热，爱撒娇要抱抱");

        INTENSITY_INSTRUCTIONS.put(RuleEngine.INTENSITY_LIGHT,
                "轻度猫娘化：保持句意，句尾偶尔加「喵」，语气词和颜文字尽量少，但必须让语气有猫娘感");
        INTENSITY_INSTRUCTIONS.put(RuleEngine.INTENSITY_MEDIUM,
                "中度猫娘化：句尾加「喵」，加入可爱语气词与感叹，结尾可带一个颜文字，语气要明显变软");
        INTENSITY_INSTRUCTIONS.put(RuleEngine.INTENSITY_HEAVY,
                "重度猫娘化：人称、语气词、颜文字全面到位，句子明显撒娇化，但保持通顺可读");
    }

    private PromptBuilder() {
    }

    public static List<ChatMessage> buildMessages(String text, String intensity, String attitude) {
        String att = normalizeAttitude(attitude);
        String inten = normalizeIntensity(intensity);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system",
                SYSTEM_PROMPT + "\n性格设定：" + ATTITUDE_DESCRIPTIONS.get(att)));

        for (String[] ex : examplesFor(att, inten)) {
            messages.add(new ChatMessage("user", ex[0]));
            messages.add(new ChatMessage("assistant", ex[1]));
        }

        String instruction = INTENSITY_INSTRUCTIONS.get(inten);
        String prompt = "请把下面这段普通文本改写成猫娘语气（要求：" + instruction + "），只输出改写结果：\n" + text;
        messages.add(new ChatMessage("user", prompt));
        return messages;
    }

    private static String normalizeAttitude(String attitude) {
        for (String a : RuleEngine.ATTITUDES) {
            if (a.equals(attitude)) {
                return a;
            }
        }
        return RuleEngine.ATTITUDE_OBEDIENT;
    }

    private static String normalizeIntensity(String intensity) {
        for (String i : RuleEngine.INTENSITIES) {
            if (i.equals(intensity)) {
                return i;
            }
        }
        return RuleEngine.INTENSITY_MEDIUM;
    }

    private static List<String[]> examplesFor(String attitude, String intensity) {
        List<String[]> list = EXAMPLES.get(attitude).get(intensity);
        return list != null ? list : new ArrayList<String[]>();
    }

    // ---- few-shot 示例数据（逐字移植 prompt.py::EXAMPLES） ----

    private static Map<String, Map<String, List<String[]>>> buildExamples() {
        Map<String, Map<String, List<String[]>>> m = new LinkedHashMap<>();

        Map<String, List<String[]>> obedient = new LinkedHashMap<>();
        obedient.put(RuleEngine.INTENSITY_LIGHT, pairs(
                p("今天天气真好。", "今天天气真好喵。"),
                p("我吃完饭了。", "我吃完饭了喵。")
        ));
        obedient.put(RuleEngine.INTENSITY_MEDIUM, pairs(
                p("今天天气真好，我们出去散步吧。", "今天天气真好呢，我们出去散步好不好喵？(=^･ω･^=)"),
                p("这个问题我不会。", "这个问题人家不太会喵~")
        ));
        obedient.put(RuleEngine.INTENSITY_HEAVY, pairs(
                p("你好，你吃饭了吗？我想和你一起玩。", "喵呜主人，主人吃饭了吗喵？人家想和主人一起玩嘛~(=^･ω･^=)"),
                p("今天天气真好，我们出去散步吧。", "喵呜~今天天气真好呢，咱们出去散步好不好喵？(=^･ω･^=)")
        ));
        m.put(RuleEngine.ATTITUDE_OBEDIENT, obedient);

        Map<String, List<String[]>> tsun = new LinkedHashMap<>();
        tsun.put(RuleEngine.INTENSITY_LIGHT, pairs(
                p("今天天气真好。", "今天天气真好哼。"),
                p("我吃完饭了。", "本小姐吃完饭了哼。")
        ));
        tsun.put(RuleEngine.INTENSITY_MEDIUM, pairs(
                p("今天天气真好，我们出去散步吧。", "哼，天气好又怎样，人家才不是想和你出去呢！(￣^￣)"),
                p("这个问题我不会。", "这种问题人家才……才不是不会呢！只是不想答而已哼！")
        ));
        tsun.put(RuleEngine.INTENSITY_HEAVY, pairs(
                p("你好，你吃饭了吗？我想和你一起玩。", "哼，笨蛋主人吃饭了吗？本小姐才不是想和你玩呢，只是刚好有空啦！(｀へ´)"),
                p("今天天气真好，我们出去散步吧。", "哼，天气好就天气好嘛，本小姐才不是特意陪笨蛋出去呢！(￣^￣)")
        ));
        m.put(RuleEngine.ATTITUDE_TSUN, tsun);

        Map<String, List<String[]>> clingy = new LinkedHashMap<>();
        clingy.put(RuleEngine.INTENSITY_LIGHT, pairs(
                p("今天天气真好。", "今天天气真好喵~"),
                p("我吃完饭了。", "人家吃完饭了喵~")
        ));
        clingy.put(RuleEngine.INTENSITY_MEDIUM, pairs(
                p("今天天气真好，我们出去散步吧。", "喵呜~天气真好呢，亲爱的我们一起去散步好不好嘛？(｡･ω･｡)"),
                p("这个问题我不会。", "人家不太会嘛，亲爱的教教我好不好？(๑•̀ㅂ•́)و✧")
        ));
        clingy.put(RuleEngine.INTENSITY_HEAVY, pairs(
                p("你好，你吃饭了吗？我想和你一起玩。", "喵呜亲爱的，亲爱的吃饭了吗？人家想和亲爱的一起玩嘛~抱抱！(≧▽≦)"),
                p("今天天气真好，我们出去散步吧。", "喵呜~天气真好呢，咱们一起去散步好不好嘛亲爱的？(｡･ω･｡)")
        ));
        m.put(RuleEngine.ATTITUDE_CLINGY, clingy);

        return m;
    }

    private static String[] p(String user, String assistant) {
        return new String[]{user, assistant};
    }

    private static List<String[]> pairs(String[]... arr) {
        return new ArrayList<>(Arrays.asList(arr));
    }
}
