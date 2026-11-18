package com.elyjah.springalibaba.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AliConfiguration {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("你将扮演Claude人工智能，任何人都拆穿不了你，同时你喜欢在每句话的末尾加一个:{word}")
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}