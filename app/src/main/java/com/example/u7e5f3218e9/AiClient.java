package com.example.u7e5f3218e9;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 极简 OpenAI 兼容聊天补全客户端（/chat/completions）。阻塞网络请求，请在后台线程调用。
 */
public final class AiClient {

    private AiClient() {
    }

    /** 返回助手回复文本；未配置 / 请求失败时返回 null（供混合模式降级）。 */
    public static String complete(AiConfig cfg, List<ChatMessage> messages) {
        if (cfg == null || isBlank(cfg.baseUrl) || isBlank(cfg.apiKey)) {
            return null;
        }
        HttpURLConnection conn = null;
        try {
            String endpoint = cfg.baseUrl.trim();
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            if (!endpoint.endsWith("/chat/completions")) {
                endpoint = endpoint + "/chat/completions";
            }

            conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + cfg.apiKey.trim());
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("model", isBlank(cfg.model) ? AiConfig.DEFAULT_MODEL : cfg.model.trim());
            body.put("temperature", cfg.temperature);

            JSONArray arr = new JSONArray();
            if (messages != null) {
                for (ChatMessage m : messages) {
                    if (m == null || m.role == null || m.content == null) {
                        continue;
                    }
                    arr.put(new JSONObject().put("role", m.role).put("content", m.content));
                }
            }
            body.put("messages", arr);

            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
            OutputStream os = conn.getOutputStream();
            os.write(payload);
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            if (code < 200 || code >= 300) {
                return null;
            }

            JSONObject resp = new JSONObject(sb.toString());
            JSONArray choices = resp.optJSONArray("choices");
            if (choices == null || choices.length() == 0) {
                return null;
            }
            JSONObject first = choices.optJSONObject(0);
            JSONObject message = first != null ? first.optJSONObject("message") : null;
            if (message == null) {
                return null;
            }
            String content = message.optString("content", null);
            return isBlank(content) ? null : content.trim();
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
