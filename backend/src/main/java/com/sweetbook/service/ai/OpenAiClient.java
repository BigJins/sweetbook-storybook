package com.sweetbook.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class OpenAiClient implements AiClient {

    private final WebClient http;
    private final String apiKey;
    private final String visionModel;
    private final String textModel;
    private final String imageModel;
    private final ObjectMapper om = new ObjectMapper();

    public OpenAiClient(WebClient http, String apiKey, String visionModel, String textModel, String imageModel) {
        this.http = http;
        this.apiKey = apiKey;
        this.visionModel = visionModel;
        this.textModel = textModel;
        this.imageModel = imageModel;
    }

    @Override
    public StyleDescriptor analyzeDrawing(byte[] bytes, String contentType) {
        String b64 = Base64.getEncoder().encodeToString(bytes);
        String dataUrl = "data:" + contentType + ";base64," + b64;
        Map<String, Object> body = Map.of(
            "model", visionModel,
            "messages", List.of(
                Map.of("role", "system", "content",
                    "당신은 아이의 그림을 보고 일러스트 스타일을 4-6개 짧은 한국어 키워드로 추출하는 분석가입니다. JSON 한 줄로만 답하세요. 형태: {\"keywords\":[\"...\",\"...\"]}"),
                Map.of("role", "user", "content", List.of(
                    Map.of("type", "text", "text", "이 그림의 스타일을 분석해주세요"),
                    Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
                ))
            ),
            "response_format", Map.of("type", "json_object"),
            "max_tokens", 200
        );
        JsonNode resp = call("/v1/chat/completions", body);
        try {
            String content = resp.get("choices").get(0).get("message").get("content").asText();
            JsonNode parsed = om.readTree(content);
            List<String> kws = new ArrayList<>();
            parsed.get("keywords").forEach(k -> kws.add(k.asText()));
            return new StyleDescriptor(kws);
        } catch (Exception e) {
            throw new RuntimeException("STYLE_PARSE_FAILED", e);
        }
    }

    @Override
    public StoryDraft generateStory(String childName, String prompt, StyleDescriptor style) {
        String sys = """
            당신은 어린이용 한국어 동화 작가입니다. 5페이지 동화를 만드세요.
            - page 1은 표지: bodyText는 null, illustrationPrompt에 표지 장면 묘사
            - page 2~4는 본문: bodyText 2-3문장, illustrationPrompt 한 줄
            - page 5는 엔딩: 마무리 한 문장 + illustrationPrompt
            JSON으로만 답하세요. 형태:
            {"title":"...","pages":[{"pageNumber":1,"bodyText":null,"illustrationPrompt":"..."},...]}
            """;
        String user = "아이 이름: " + childName + "\n상상: " + prompt;
        Map<String, Object> body = Map.of(
            "model", textModel,
            "messages", List.of(
                Map.of("role", "system", "content", sys),
                Map.of("role", "user", "content", user)
            ),
            "response_format", Map.of("type", "json_object"),
            "max_tokens", 1500
        );
        JsonNode resp = call("/v1/chat/completions", body);
        try {
            String content = resp.get("choices").get(0).get("message").get("content").asText();
            return om.readValue(content, StoryDraft.class);
        } catch (Exception e) {
            throw new RuntimeException("STORY_PARSE_FAILED", e);
        }
    }

    @Override
    public byte[] generateIllustration(String prompt, StyleDescriptor style) {
        String fullPrompt = "스타일: " + style.asPromptPrefix() + "\n장면: " + prompt
            + "\n어린이 동화책 일러스트, 4:5 세로 비율, 텍스트 없음";
        Map<String, Object> body = Map.of(
            "model", imageModel,
            "prompt", fullPrompt,
            "size", "1024x1024",
            "n", 1
        );
        JsonNode resp = call("/v1/images/generations", body);
        try {
            String b64 = resp.get("data").get(0).get("b64_json").asText();
            return Base64.getDecoder().decode(b64);
        } catch (Exception e) {
            throw new RuntimeException("IMAGE_PARSE_FAILED", e);
        }
    }

    private JsonNode call(String path, Map<String, Object> body) {
        return http.post().uri(path)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();
    }
}
