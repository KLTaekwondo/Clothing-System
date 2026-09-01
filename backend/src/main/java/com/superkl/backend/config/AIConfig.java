package com.superkl.backend.config;

import com.superkl.backend.service.springai.AIToolService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    // 手动注册默认 ChatClient（2.0 不再自动提供）
    @Bean
    @ConditionalOnProperty(name = "spring.ai.openai.api-key")
    ChatClient chatClient(ChatClient.Builder builder, AIToolService aiToolService) {
        return builder.defaultTools(aiToolService).build();
    }
}
