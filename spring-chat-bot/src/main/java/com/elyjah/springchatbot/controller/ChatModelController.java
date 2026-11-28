package com.elyjah.springchatbot.controller;

import com.elyjah.springchatbot.tools.DateTimeTools;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/chat1")
public class ChatModelController {

//    @Autowired
//    private ChatModel ollamaModel;
//
//    @RequestMapping(value = "/stream", produces = "text/html;charset=utf-8")
//    //向ChatModel添加工具
//    public Flux<String> stream(String message) {
//
//        ToolCallback[] dateTimeTools = ToolCallbacks.from(new DateTimeTools());
//        ChatOptions chatOptions = ToolCallingChatOptions.builder()
//                .toolCallbacks(dateTimeTools)
//                .build();
//        Prompt prompt = new Prompt(message, chatOptions);
//        return ollamaModel.stream(prompt)
//                .map(s ->s.getResult().getOutput().getText());
//    }

    private final OllamaChatModel ollamaChatModel;


    //向ChatModel添加默认工具
    public ChatModelController(OllamaChatModel ollamaChatModel) {
        ToolCallback[] dateTimeTools = ToolCallbacks.from(new DateTimeTools());
        // 用 OllamaChatOptions 自己的 builder，别用 ToolCallingChatOptions
        OllamaChatOptions options = OllamaChatOptions.builder()
                .model("qwen2.5:3b")
                .toolCallbacks(dateTimeTools)      // 这个方法它有（继承自 ToolCallingChatOptions.Builder）
                .build();

        this.ollamaChatModel = OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder().build())
                .defaultOptions(options)   // 类型匹配了
                .build();
    }

    @RequestMapping(value = "/stream", produces = "text/html;charset=utf-8")
    public Flux<String> stream(String message) {

        Prompt prompt = new Prompt(message);
        return ollamaChatModel.stream(prompt)
                .map(s -> s.getResult().getOutput().getText());
    }
}