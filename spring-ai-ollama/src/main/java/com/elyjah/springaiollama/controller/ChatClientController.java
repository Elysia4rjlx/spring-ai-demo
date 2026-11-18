package com.elyjah.springaiollama.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/client")
public class ChatClientController {


    private final ChatClient chatClient;

    public ChatClientController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @RequestMapping("/chat")
    public String chat(String message) {
        return chatClient.prompt(message).call().content();
    }

    @RequestMapping(value = "/stream",produces = "text/html;charset=utf-8")
    public Flux<String> stream(String message) {
        return chatClient.prompt(message).stream().content();
    }


}