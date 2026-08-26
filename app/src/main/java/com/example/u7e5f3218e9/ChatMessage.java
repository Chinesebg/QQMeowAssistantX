package com.example.u7e5f3218e9;

/** 一条 OpenAI 兼容聊天消息（role / content）。 */
public final class ChatMessage {
    public final String role;
    public final String content;

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
