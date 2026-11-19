package com.elyjah.springchatbot.domain;


import lombok.Data;
import org.springframework.ai.chat.messages.Message;

@Data
public class MessageV0 {
    String role;
    String content;

    public MessageV0(Message message) {
        switch (message.getMessageType()) {
            case USER -> {
                role = "user";
                break;
            }
            case ASSISTANT -> {
                role = "assistant";
                break;
            }
            case SYSTEM -> {
                role = "system";
                break;
            }
            case TOOL -> {
                role = "tool";
                break;
            }
        }
        this.content = message.getText();
    }
}