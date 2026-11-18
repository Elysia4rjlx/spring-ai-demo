package com.elyjah.springaidemo1.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatClientController {

    @Autowired
    private ChatClient chatClient;

//    public ChatClientController(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder
//                .defaultSystem("你叫科比，擅长肘击")
//                .build();
//    }

    @GetMapping("/ai")
    public String generation(String userInput) {
        return this.chatClient.prompt()
                .user(userInput)
                .advisors(new SimpleLoggerAdvisor())
                .call()
                .content();
    }

    record Recipe(String dish, List<String> ingredients){}

    @RequestMapping("/entity")
    public String entity(String userInput){
        Recipe recipe = chatClient.prompt()
                .user(String.format("请帮我生成%s的食谱",userInput))
                .call()
                .entity(Recipe.class);
        return recipe.toString();
    }

    @RequestMapping(value = "/stream",produces = "test/html;charset=utf-8")
    public Flux<String> stream(String userInput) {
        return chatClient.prompt()
                //用户输入
                .user(userInput)
                //调用ai，流式输出
                .stream()
                //返回响应
                .content();
    }
}