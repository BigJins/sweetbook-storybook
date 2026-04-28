package com.sweetbook.config;

import com.sweetbook.service.ai.AiClient;
import com.sweetbook.service.ai.MockAiClient;
import com.sweetbook.service.ai.OpenAiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiConfig {

    @Bean
    @Primary
    public AiClient aiClient(
        @Value("${app.ai.mock-mode}") boolean mockMode,
        @Value("${app.ai.openai-api-key:}") String apiKey,
        @Value("${app.ai.text-model}") String textModel,
        @Value("${app.ai.vision-model}") String visionModel,
        @Value("${app.ai.image-model}") String imageModel,
        MockAiClient mockClient
    ) {
        if (mockMode || apiKey == null || apiKey.isBlank()) {
            return mockClient;
        }
        WebClient http = WebClient.builder()
            .baseUrl("https://api.openai.com")
            .codecs(c -> c.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
            .build();
        return new OpenAiClient(http, apiKey, visionModel, textModel, imageModel);
    }
}
