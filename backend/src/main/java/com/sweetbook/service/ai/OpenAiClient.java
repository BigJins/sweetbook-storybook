package com.sweetbook.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sweetbook.domain.story.PageLayout;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class OpenAiClient implements AiClient {

    private static final String ANALYSIS_SYSTEM_PROMPT = """
        당신은 아이의 그림을 그림책 일러스트의 시작점으로 분석하는 전문 분석가입니다.
        그림에서 다음 5가지를 추출해 한 줄 JSON으로만 답하세요.

        1. subject: 그림의 주된 대상 한 줄. 동물이면 종/색까지 (예: "갈색 강아지", "주황색 공룡", "노란 새"). 사람이면 "아이"/"어른"/특정 캐릭터. 추상적이면 "별이 가득한 밤하늘" 같은 묘사.
        2. subjectType: 다음 중 하나 — "ANIMAL" / "PERSON" / "CREATURE" / "OBJECT" / "SCENE".
        3. mood: 그림 전체에서 느껴지는 감정 한 단어 (예: "따뜻한", "신비로운", "용감한", "엉뚱한", "포근한").
        4. sceneCues: 그림 안에 보이는 배경/소품/디테일 2-4개의 짧은 한국어 명사구 배열 (예: ["꽃밭","해","집"]).
        5. keywords: 일러스트 스타일을 4-6개의 짧은 한국어 키워드 배열로 (예: ["수채화풍","파스텔톤","굵은 외곽선","천진한 선"]).

        형식 예시:
        {"subject":"갈색 강아지","subjectType":"ANIMAL","mood":"따뜻한","sceneCues":["꽃밭","해","공"],"keywords":["수채화풍","파스텔톤","굵은 외곽선"]}
        """;

    static final String STORY_SYSTEM_PROMPT = """
        당신은 어린이용 한국어 그림책을 쓰는 작가입니다. 5페이지짜리 따뜻한 그림책을 만드세요.

        가장 중요한 원칙:
        - 주인공은 그림 속 대상(subject)입니다. 이 캐릭터의 시점과 행동을 중심으로 이야기를 풀어 주세요.
        - 아이(childName)는 그림을 그린 사람으로, 이야기 속에 자연스럽게 등장시킵니다 — 친구, 동행자, 주인, 안내자, 대화 상대 등 맥락에 맞춰 배치하세요. 모든 페이지에 무리해서 등장시키지 말고, 1~3개 페이지에 자연스럽게 배치하면 충분합니다.
        - 분석된 분위기(mood)와 장면 단서(sceneCues)를 본문과 일러스트 묘사에 녹여 주세요.
        - 5페이지가 하나의 부드러운 흐름을 갖도록: 만남 → 모험 시작 → 절정/발견 → 따뜻한 마무리.
        - 의성어, 짧은 대화 한 줄을 1~2번 정도 자연스럽게 섞어 그림책 톤을 살리세요.
        - 아이의 상상(imaginationPrompt)은 가능한 만큼 그대로 살려서 본문 흐름에 녹입니다.

        글자 수 / 문장 수 규칙 (그림책 호흡의 핵심):
        - 본문(page 2~4): 짧은 한국어 문장 2~4개. bodyText 한 페이지는 한국어 기준 약 70~110자가 적당합니다.
        - 엔딩(page 5): 1~2문장의 따뜻한 마무리. 본문보다 짧아도 좋습니다.
        - 빽빽한 단락, 같은 표현의 반복, 지나친 설명은 피합니다.
        - 그림책 리듬: 단순·짧음·여백 우선. 디테일을 더 넣기보다 한 줄을 짧게 자르는 편이 낫습니다.
        - 본문은 페이지의 텍스트 영역에 오버레이되므로, 한 페이지 안에 너무 많은 문장을 넣지 않습니다.

        페이지 구성:
        - page 1 (표지): bodyText는 null. illustrationPrompt에 표지 한 컷의 시각적 묘사 — subject가 중앙에, mood가 느껴지도록.
        - page 2~4 (본문): bodyText는 위 글자 수/문장 수 규칙을 따릅니다. illustrationPrompt 한 줄 (그 페이지의 핵심 장면, 어떤 캐릭터가 어디서 무엇을 하는지).
        - page 5 (엔딩): 따뜻한 마무리 1~2문장 + illustrationPrompt.

        JSON으로만 답하세요. 형식:
        {"title":"...","pages":[{"pageNumber":1,"bodyText":null,"illustrationPrompt":"..."},{"pageNumber":2,"bodyText":"...","illustrationPrompt":"..."},...]}
        """;

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
                Map.of("role", "system", "content", ANALYSIS_SYSTEM_PROMPT),
                Map.of("role", "user", "content", List.of(
                    Map.of("type", "text", "text", "이 그림을 위 5가지 항목으로 분석해 JSON 한 줄로 답해주세요."),
                    Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
                ))
            ),
            "response_format", Map.of("type", "json_object"),
            "max_tokens", 400
        );
        JsonNode resp = call("/v1/chat/completions", body);
        try {
            String content = resp.get("choices").get(0).get("message").get("content").asText();
            return om.readValue(content, StyleDescriptor.class);
        } catch (Exception e) {
            throw new RuntimeException("STYLE_PARSE_FAILED", e);
        }
    }

    @Override
    public StoryDraft generateStory(String childName, String prompt, StyleDescriptor style) {
        String userBlock = """
            아이 이름(childName): %s
            아이의 상상(imaginationPrompt): %s

            그림 분석 결과:
            - subject: %s
            - subjectType: %s
            - mood: %s
            - sceneCues: %s
            - 스타일 키워드: %s

            위 정보를 바탕으로, subject를 주인공으로 하는 5페이지 그림책을 JSON으로 만들어 주세요.
            """.formatted(
                childName,
                prompt,
                style.subjectOrFallback(),
                nullSafe(style.subjectType()),
                style.moodOrFallback(),
                String.join(", ", style.sceneCues()),
                style.asPromptPrefix()
            );

        Map<String, Object> body = Map.of(
            "model", textModel,
            "messages", List.of(
                Map.of("role", "system", "content", STORY_SYSTEM_PROMPT),
                Map.of("role", "user", "content", userBlock)
            ),
            "response_format", Map.of("type", "json_object"),
            "max_tokens", 1800
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
    public byte[] generateIllustration(String prompt, StyleDescriptor style, PageLayout layout) {
        String fullPrompt = buildIllustrationPrompt(prompt, style, layout);

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

    static String buildIllustrationPrompt(String scene, StyleDescriptor style, PageLayout layout) {
        return """
            스타일 키워드: %s
            주인공: %s (%s)
            전체 분위기: %s
            장면 단서: %s

            장면: %s

            %s

            Important: the main character and any key subject matter must NOT
            sit inside the reserved text-safe zone — keep that zone visually
            quiet and breathable so overlaid Korean text remains clearly
            readable. Important visual elements belong in the opposite portion
            of the frame.

            ABSOLUTELY NO text, NO letters, NO Korean characters (한글 글자 금지),
            NO alphabet, NO numbers, NO words, NO captions, NO speech bubbles,
            NO signs, NO signage, NO billboards, NO storefront names, NO book
            titles inside the picture, NO logos, NO watermarks, NO signatures,
            NO labels, NO writing of any kind anywhere in the image.
            The illustration must be entirely wordless — text will be overlaid
            by the layout system afterwards, so the image itself must contain
            zero typography.

            형식: 4:5 세로 비율, 어린이 그림책 일러스트, 손그림 느낌, 부드러운 색감,
            주인공이 화면 안에 명확히 보이도록 구성.
            """.formatted(
                style.asPromptPrefix(),
                style.subjectOrFallback(),
                nullSafe(style.subjectType()),
                style.moodOrFallback(),
                String.join(", ", style.sceneCues()),
                scene,
                textSafeAreaInstruction(layout == null ? PageLayout.SPLIT : layout)
            );
    }

    private static String textSafeAreaInstruction(PageLayout layout) {
        return switch (layout) {
            case COVER -> """
                Composition: picture-book COVER. Reserve a calm, uncluttered area
                in the upper third OR lower third of the frame (about 25–30%% of
                the canvas) as a title-safe zone — keep it visually quiet so a
                title can be placed there afterwards. The main character stays
                clearly visible in the remaining area, ideally centered.""";
            case SPLIT -> """
                Composition: picture-book INTERIOR spread. Reserve a clean,
                low-detail area on the right side OR the lower third of the frame
                (about 30–35%% of the canvas) as a body-text-safe zone — keep
                that region soft and uncluttered (open sky, water, ground,
                gentle gradient, etc.) so 2–3 lines of body text can be overlaid
                there afterwards. Keep the main character on the opposite side
                or upper portion, clearly readable.""";
            case ENDING -> """
                Composition: picture-book CLOSING scene. Calm, breathable
                composition with generous open negative space toward the lower
                or center-bottom (about 30%% of the canvas) for one final line
                of text. The mood is restful and resolved.""";
        };
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
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
