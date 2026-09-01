package com.superkl.backend.controller.springai;

import com.superkl.backend.common.Result;
import com.superkl.backend.service.springai.AIChatClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIChatClientController {
    private final AIChatClientService aiChatClientService;

    // 通用问答
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> body) {
        return Result.success(aiChatClientService.chat(body.get("question")));
    }
}
