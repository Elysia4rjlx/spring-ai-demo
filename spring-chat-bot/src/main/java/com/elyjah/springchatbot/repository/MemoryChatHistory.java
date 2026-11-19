package com.elyjah.springchatbot.repository;


import com.elyjah.springchatbot.domain.ChatInfo;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Repository
public class MemoryChatHistory implements ChatHistoryRepository {

    private final Map<String,String> chatInfos = new LinkedHashMap<>();
    /**
     * 保持会话记录
     * 如果是新会话，则新增
     * 如果已经存在，就更新
     */
    @Override
    public void save(String conversationId,String title) {
        chatInfos.put(conversationId,title);
    }

    @Override
    public void clearByConversationId(String conversationId) {
        chatInfos.remove(conversationId);
    }

    @Override
    public List<ChatInfo> getChats() {
        return chatInfos.entrySet()
                .stream()
                .map(entry->new ChatInfo(entry.getKey(),entry.getValue()))
                .collect(Collectors.toList());
    }
}