package com.example.u7e5f3218e9;

import java.util.List;

/**
 * 三种处理引擎的统一入口（参照 nyabox 的 rule / hybrid / ai）：
 * - rule：态度 × 强度 规则引擎（离线确定性改写，见 {@link RuleEngine}）
 * - hybrid：规则预处理（词汇映射）→ AI 润色 → 补喵收尾，失败自动降级规则
 * - ai：大模型直接改写（few-shot 提示词，见 {@link PromptBuilder}）
 *
 * 三种引擎均遵循 CatConfig 中的「态度」与「强度」。AI / 混合会发起阻塞网络请求，请在后台线程调用。
 */
public final class Engine {

    private Engine() {
    }

    public static String process(String text, CatConfig cfg, AiConfig aiCfg) {
        String engine = cfg != null && cfg.engineMode != null ? cfg.engineMode : CatConfig.MODE_ENGINE_RULE;
        String intensity = cfg != null && cfg.intensity != null ? cfg.intensity : CatConfig.DEFAULT_INTENSITY;
        String attitude = cfg != null && cfg.attitude != null ? cfg.attitude : CatConfig.DEFAULT_ATTITUDE;
        List<CatConfig.Rule> rules = cfg != null ? cfg.rules : null;

        if (CatConfig.MODE_ENGINE_AI.equals(engine)) {
            List<ChatMessage> messages = PromptBuilder.buildMessages(text, intensity, attitude);
            String r = AiClient.complete(aiCfg, messages);
            return isBlank(r) ? text : r;
        }
        if (CatConfig.MODE_ENGINE_HYBRID.equals(engine)) {
            String pre = RuleEngine.preprocess(text, intensity, attitude, rules);
            List<ChatMessage> messages = PromptBuilder.buildMessages(pre, intensity, attitude);
            String r = AiClient.complete(aiCfg, messages);
            if (isBlank(r)) {
                return RuleEngine.convert(text, intensity, attitude, rules, true);
            }
            return RuleEngine.finish(r);
        }
        return RuleEngine.convert(text, intensity, attitude, rules, true);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
