package com.elyjah.springaidemo1.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatClientController {

    private final ChatClient chatClient;

    public ChatClientController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你叫科比，擅长肘击")
                .build();
    }

    @GetMapping("/ai")
    public String generation(String userInput) {
        return this.chatClient.prompt()
                .user(userInput)
                .call()
                .content();
    }
}