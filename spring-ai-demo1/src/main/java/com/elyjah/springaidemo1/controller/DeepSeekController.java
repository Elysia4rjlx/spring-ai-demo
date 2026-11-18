package com.elyjah.springaidemo1.controller;

import lombok.val;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ds")
public class DeepSeekController {

    @Autowired
    private DeepSeekChatModel deepSeekChatModel;

    @RequestMapping("/chat")
    public String chat(String message) {
        return deepSeekChatModel.call(message);
    }

    @RequestMapping("/chatByPrompt")
    public String chatByPrompt(String message) {
        Prompt prompt = new Prompt(message);
        ChatResponse response = deepSeekChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    @RequestMapping("/role")
    public String role(String message) {
        SystemMessage systemMessage = new SystemMessage("你是科比，喜欢肘击和喊man！");
        UserMessage userMessage = new UserMessage(message);

        Prompt prompt = new Prompt(systemMessage,userMessage);

        ChatResponse response = deepSeekChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    @RequestMapping(value = "/stream",produces = "text/html;charset=utf-8")
    public Flux<String> stream(String message) {
        SystemMessage systemMessage = new SystemMessage("你是科比，喜欢肘击和喊man！");
        UserMessage userMessage = new UserMessage(message);

        Prompt prompt = new Prompt(systemMessage,userMessage);

        Flux<ChatResponse> response = deepSeekChatModel.stream(prompt);

        return response.map(S -> S.getResult().getOutput().getText());

    }
}