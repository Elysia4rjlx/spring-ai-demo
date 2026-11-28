package com.elyjah.springchatbot.controller;


import com.elyjah.springchatbot.domain.ChatInfo;
import com.elyjah.springchatbot.domain.MessageV0;
import com.elyjah.springchatbot.repository.ChatHistoryRepository;
import com.elyjah.springchatbot.tools.DateTimeTools;
import com.elyjah.springchatbot.tools.WeatherTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.el.util.ReflectionUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {


//    private final ChatClient chatClient;
//    //构造方法注入
//    public ChatController(ChatClient omllamaChatClient) {
//        this.chatClient = omllamaChatClient;
//    }

//    private final ChatClient chatClient;
//    //构造方法注入ChatModel,并设置默认工具
//    public ChatController(ChatModel model) {
//        this.chatClient = ChatClient.builder(model)
//                .defaultTools(new DateTimeTools())
//                .build();
//    }


//
//    private final ChatClient deepseekChatClient;
//        public ChatController(ChatClient deepseekChatClient) {
//        this.deepseekChatClient = deepseekChatClient;
//    }


//    @Autowired
//    private ChatHistoryRepository chatHistoryRepository;
//
//    @Autowired
//    private ChatMemory chatMemory;


    private final ChatClient chatClient;
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatMemory chatMemory;
    private final ToolCallback weatherTool;

    //    向ChatClient添加默认工具
    public ChatController(ChatClient.Builder builder,
                            ChatHistoryRepository chatHistoryRepository,
                            ChatMemory chatMemory,
                            @Qualifier("weatherTool") ToolCallback weatherTool) {
        this.chatClient = builder
                .defaultTools(new DateTimeTools())
                .build();
        this.chatHistoryRepository = chatHistoryRepository;
        this.chatMemory = chatMemory;
        this.weatherTool = weatherTool;
    }


    @RequestMapping(value = "/stream", produces = "text/html;charset=utf-8")
    public Flux<String> stream(String prompt, String conversationId) {

//        Method method = ReflectionUtils.findMethod(WeatherTools.class,
//                "getCurrentWeatherByCityName",
//                String.class);

//        if (method == null) {
//            throw new IllegalStateException("找不到 getCurrentWeatherByCityName 方法");
//        }
//        ToolCallback weatherTools = MethodToolCallback.builder()
//                .toolDefinition(
//                        ToolDefinition.builder()
//                                .name("getCurrentWeatherByCityName")
//                                .description("根据给定的城市名称，获取城市当前的天气")
//                                .inputSchema("""
//                                        {
//                                            "type": "object",
//                                            "properties": {
//                                            "cityName": {"type": "string"}
//                                            },
//                                            "required": ["cityName"]
//                                        }
//                                        """)
//                                .build())
//                .toolMethod(method)
//                .toolObject(new WeatherTools())
//                .build();


//        ToolCallback weatherTools = MethodToolCallback.builder()
//                .toolDefinition(
//                        ToolDefinitions.builder(method)
//                                .description("根据给定的城市名称，获取城市当前的天气")
//                                .build())
//                .toolMethod(method)
//                .toolObject(new WeatherTools())
//                .build();

        log.info("conversationId:{},prompt:{}", conversationId, prompt);
        chatHistoryRepository.save(conversationId, prompt);
        return chatClient.prompt()
                .user(prompt)
//                .tools(new DateTimeTools())
                .toolCallbacks(weatherTool)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    /**
     * 获取会话列表
     */
    @RequestMapping("/getChatList")
    public List<ChatInfo> getChatList() {
        return chatHistoryRepository.getChats();
    }

    /**
     * 获取会话历史
     */
    @RequestMapping("/getChatHistory")
    public List<MessageV0> getChatHistory(String conversationId) {
        log.info("获取会话记录。conversationId:{}", conversationId);
        List<Message> messages = chatMemory.get(conversationId);
        return messages.stream()
                .map(MessageV0::new)//m -> new MessageV0(m)
                .collect(Collectors.toList());
    }

    /**
     * 删除会话
     *
     * @param conversationId
     * @return
     */
    @RequestMapping("/deleteChat")
    public Boolean deleteChat(String conversationId) {
        log.info("删除会话记录，conversationId:{}", conversationId);

        try {
            chatHistoryRepository.clearByConversationId(conversationId);
            chatMemory.clear(conversationId);
        } catch (Exception e) {
            log.error("删除会话失败：conversationId：{}", conversationId);
            return false;
        }
        return true;
    }


}