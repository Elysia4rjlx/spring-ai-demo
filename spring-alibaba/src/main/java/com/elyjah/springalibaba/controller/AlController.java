package com.elyjah.springalibaba.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("alclient")
public class AlController {

    private final ChatClient chatClient;

    public AlController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @RequestMapping("/chat")
    public String chat(String message) {
        return chatClient.prompt(message).call().content();
    }

    @RequestMapping("/word")
    public String word(String message,String word) {
        return chatClient.prompt()
                .system(sp->sp.param("word",word))
                .user(message)
                .call()
                .content();
    }
}