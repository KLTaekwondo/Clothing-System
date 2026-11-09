package com.superkl.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// 这里给 AI 的 api-key 一个假值，否则 ${DEEPSEEK_API_KEY} 解析不到，整个上下文起不来。
// 只是创建 ChatClient 这个 Bean，不会真的发请求。
@SpringBootTest(properties = "spring.ai.openai.api-key=test-key")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
