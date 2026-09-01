package com.superkl.backend.service.springai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AIChatClientService {
    private final ChatClient chatClient;

    // 通用问答
    public String chat(String question) {
        return chatClient.prompt(question).call().content();
    }
}
