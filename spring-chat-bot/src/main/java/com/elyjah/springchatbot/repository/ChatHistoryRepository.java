package com.elyjah.springchatbot.repository;

import com.elyjah.springchatbot.domain.ChatInfo;

import java.util.List;

public interface ChatHistoryRepository {
    void save(String conversationId,String title);
    void clearByConversationId(String conversationId);
    List<ChatInfo> getChats();
}
