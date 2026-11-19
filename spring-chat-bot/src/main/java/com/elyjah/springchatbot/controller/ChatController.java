package com.elyjah.springchatbot.controller;


import com.elyjah.springchatbot.domain.ChatInfo;
import com.elyjah.springchatbot.domain.MessageV0;
import com.elyjah.springchatbot.repository.ChatHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClient chatClient;

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Autowired
    private ChatMemory chatMemory;

    public ChatController(ChatClient omllamaChatClient) {
        this.chatClient = omllamaChatClient;
    }

    @RequestMapping(value = "/stream", produces = "text/html;charset=utf-8")
    public Flux<String> stream(String prompt, String conversationId) {

        log.info("conversationId:{},prompt:{}", conversationId, prompt);
        chatHistoryRepository.save(conversationId, prompt);
        return chatClient.prompt()
                .user(prompt)
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
     * @param conversationId
     * @return
     */
    @RequestMapping("/deleteChat")
    public Boolean deleteChat(String conversationId) {
        log.info("删除会话记录，conversationId:{}",conversationId);

        try {
            chatHistoryRepository.clearByConversationId(conversationId);
            chatMemory.clear(conversationId);
        } catch (Exception e) {
            log.error("删除会话失败：conversationId：{}",conversationId);
            return false;
        }
        return true;
    }














}