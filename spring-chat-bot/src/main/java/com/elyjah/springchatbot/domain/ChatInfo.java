package com.elyjah.springchatbot.domain;


import lombok.Data;

@Data
public class ChatInfo {
    private String conversationId;
    private String title;

    public ChatInfo(String conversationId, String title) {
        this.conversationId = conversationId;
        this.title = title;
    }
}