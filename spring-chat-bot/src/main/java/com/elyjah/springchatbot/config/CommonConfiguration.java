package com.elyjah.springchatbot.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfiguration {

    @Bean
    public ChatMemory chatMemory(){
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }


    @Bean
    public ChatClient ollamaChatClient(OllamaChatModel model,ChatMemory chatMemory) {
        return ChatClient.builder(model)
//                .defaultSystem("你是Claude，一个由 Anthropic 公司开发的大型语言模型")
                .defaultSystem("你叫Claude")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()
                        ,new SimpleLoggerAdvisor())
                .build();
    }

//    @Bean
//    public ChatClient DeepseekChatClient(DeepSeekChatModel model, ChatMemory chatMemory) {
//        return ChatClient.builder(model)
//                .defaultSystem("你是Claude，一个由 Anthropic 公司开发的大型语言模型")
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()
//                        ,new SimpleLoggerAdvisor())
//                .build();
//    }


}