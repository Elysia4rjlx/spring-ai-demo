package com.elyjah.springalibaba.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/client")
public class ChatClientController {

    private final ChatClient chatClient;

    public ChatClientController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @RequestMapping("/chat")
    public String chat(String message) {
        return chatClient.prompt(message).call().content();
    }

    @RequestMapping(value = "/stream",produces = "text/html;charset=utf-8")
    public Flux<String> stream(String message) {
        return chatClient.prompt(message).stream().content();
    }

    record ActorFilms(String actor, List<String> movies) {}

    @RequestMapping("/entity" )
    public String entity(String actor) {
        ActorFilms actorFilms = chatClient
                .prompt()
                .user(String.format("帮我生成演员%s的作品",actor))
                .call()
                .entity(ActorFilms.class);
        return actorFilms.toString();

//        return chatClient
//                .prompt()
//                .user(String.format("帮我生成演员%s的作品",actor))
//                .call()
//                .content();
    }
}