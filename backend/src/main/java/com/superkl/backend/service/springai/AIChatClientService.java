package com.superkl.backend.service.springai;

import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AIChatClientService {
    private final ObjectProvider<ChatClient> chatClientProvider;

    public String chat(String question) {
        ChatClient chatClient = chatClientProvider.getIfAvailable();
        if (chatClient == null) {
            throw new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "AI 服务未配置，请设置 API_KEY 后重试");
        }
        return chatClient.prompt(question).call().content();
    }
}
