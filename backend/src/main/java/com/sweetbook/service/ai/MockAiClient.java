package com.sweetbook.service.ai;

import com.sweetbook.domain.story.PageLayout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class MockAiClient implements AiClient {

    private static final ThreadLocal<MockPreset> ACTIVE_PRESET = new ThreadLocal<>();

    @Override
    public StyleDescriptor analyzeDrawing(byte[] bytes, String contentType) {
        sleep(600);
        return new StyleDescriptor(
            List.of("아이 그림", "다채로운 색감", "그림책 스타일", "따뜻한 분위기"),
            "그림 속 주인공",
            "PERSON",
            "포근한",
            List.of("하늘", "꽃", "친구")
        );
    }

    @Override
    public StoryDraft generateStory(String childName, String imaginationPrompt, StyleDescriptor style) {
        sleep(600);
        MockPreset preset = MockPreset.pick(imaginationPrompt, style);
        ACTIVE_PRESET.set(preset);
        return preset.toDraft(childName);
    }

    @Override
    public byte[] generateIllustration(String prompt, StyleDescriptor style, PageLayout layout, int pageNumber) {
        sleep(600);
        MockPreset preset = ACTIVE_PRESET.get();
        if (preset == null) {
            preset = MockPreset.pick(prompt, style);
        }
        try (InputStream in = new ClassPathResource(preset.resourceFor(pageNumber)).getInputStream()) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    enum MockPreset {
        PRINCESS(
            "princess",
            new StyleDescriptor(
                List.of("어린이 그림", "다채로운 색상", "굵은 외곽선", "활기찬"),
                "왕관을 쓴 공주",
                "PERSON",
                "행복한",
                List.of("왕관", "드레스", "꽃", "궁전")
            ),
            "서아의 행복한 왕국",
            Arrays.asList(
                null,
                "어느 날, {{childName}}는 자신이 공주인 꿈을 꾸었어요. 왕자님이 나타나서 \"{{childName}} 공주님, 함께 놀아요!\"라고 말했어요.",
                "두 친구는 성의 정원에서 숨바꼭질을 했어요. \"딱! 왕자님이 저기 숨어 있네!\" {{childName}}가 환하게 웃었어요.",
                "{{childName}}와 왕자님은 정원 한가운데서 둥실둥실 춤을 췄어요. 꽃들도 함께 흔들리며 두 친구를 축하해 주는 것 같았어요.",
                "{{childName}}는 눈을 감고 미소 지었어요. 공주님과 왕자님은 앞으로도 오래오래 행복할 거라고 믿었답니다."
            ),
            List.of(
                "왕관을 쓴 아이 공주가 밝게 웃고 있는 표지 장면.",
                "공주와 왕자님이 정원에서 처음 만나는 장면.",
                "공주와 왕자님이 성의 정원에서 숨바꼭질하는 장면.",
                "공주와 왕자님이 꽃밭에서 함께 춤추는 장면.",
                "공주가 행복한 꿈의 여운 속에서 미소 짓는 엔딩 장면."
            )
        ),
        BEAR(
            "bear",
            new StyleDescriptor(
                List.of("생동감 있는", "자연주의", "부드러운 느낌"),
                "갈색 곰",
                "ANIMAL",
                "따뜻한",
                List.of("풀밭", "들꽃", "숲", "별")
            ),
            "곰돌이의 별 따러 가는 여행",
            Arrays.asList(
                null,
                "어느 날, 곰돌이는 밤하늘의 별을 보고 말했어요. \"저 별들을 따러 가고 싶어!\" 생각하며 숲 속으로 나섰어요.",
                "숲 속에서 다양한 친구들을 만났어요. 토끼가 말했어요. \"같이 가줄게!\" 곰돌이는 기뻐하며 답했어요. \"고마워! 함께 가자!\"",
                "드디어 높은 언덕에 도착했어요. 곰돌이는 손을 쭉 뻗어 별을 잡으려 했어요. \"와! 너무 아름다워!\" 친구들도 신기하게 바라봤어요.",
                "그날 밤, 곰돌이는 별빛 속에서 행복한 꿈을 꾸었어요. 별은 멀리 있었지만, 친구들과 함께라서 좋았어요."
            ),
            List.of(
                "곰돌이가 별을 바라보는 표지 장면.",
                "곰돌이가 숲길을 따라 별을 향해 떠나는 장면.",
                "곰돌이가 친구들과 함께 숲길을 걷는 장면.",
                "곰돌이가 높은 언덕에서 별을 향해 손을 뻗는 장면.",
                "곰돌이가 밤하늘 아래 별빛을 바라보는 엔딩 장면."
            )
        ),
        TANI(
            "tani",
            new StyleDescriptor(
                List.of("사진풍", "리얼리즘", "실내", "깔끔한"),
                "검은 포메라니안",
                "ANIMAL",
                "포근한",
                List.of("책상", "모니터", "사진", "침대")
            ),
            "탄이의 낮잠 이야기",
            Arrays.asList(
                null,
                "{{childName}}는 탄이가 낮잠을 많이 자는 것이 조금 섭섭했어요. \"왜 이렇게 자는 걸까?\" {{childName}}는 조용히 생각해 보았어요.",
                "그때 탄이는 꿈속에서 {{childName}}를 지키고 있었어요. 작은 발걸음으로 밤새 곁을 지킨 마음이 반짝였답니다.",
                "{{childName}}는 이제 알 것 같았어요. 탄이가 늦은 밤까지 곁을 지키느라 피곤했구나, 하고요.",
                "탄이와 {{childName}}는 서로를 바라보며 미소 지었어요. 이제 두 친구는 서로의 마음을 더 잘 알게 되었답니다."
            ),
            List.of(
                "검은 포메라니안 탄이가 포근하게 쉬는 표지 장면.",
                "아이가 탄이를 바라보며 궁금해하는 실내 장면.",
                "탄이가 꿈속에서 아이를 지키는 따뜻한 장면.",
                "아이가 탄이를 꼭 안아 주는 따뜻한 장면.",
                "아이와 탄이가 평화롭게 미소 짓는 엔딩 장면."
            )
        );

        private final String resourceDir;
        private final StyleDescriptor style;
        private final String title;
        private final List<String> bodyTexts;
        private final List<String> illustrationPrompts;

        MockPreset(String resourceDir,
                   StyleDescriptor style,
                   String title,
                   List<String> bodyTexts,
                   List<String> illustrationPrompts) {
            this.resourceDir = resourceDir;
            this.style = style;
            this.title = title;
            this.bodyTexts = bodyTexts;
            this.illustrationPrompts = illustrationPrompts;
        }

        StoryDraft toDraft(String childName) {
            return new StoryDraft(replaceChildName(title, childName), List.of(
                page(1, null),
                page(2, childName),
                page(3, childName),
                page(4, childName),
                page(5, childName)
            ));
        }

        private StoryDraft.PageDraft page(int pageNumber, String childName) {
            String body = bodyTexts.get(pageNumber - 1);
            String prompt = illustrationPrompts.get(pageNumber - 1);
            return new StoryDraft.PageDraft(
                pageNumber,
                body == null ? null : replaceChildName(body, childName),
                replaceChildName(prompt, childName)
            );
        }

        String resourceFor(int pageNumber) {
            if (pageNumber <= 1) {
                return resourceDirPrefix() + "/cover.png";
            }
            return resourceDirPrefix() + "/page-" + pageNumber + ".png";
        }

        private String resourceDirPrefix() {
            return switch (this) {
                case PRINCESS -> "mock/presets/princess";
                case BEAR -> "seed/story-1";
                case TANI -> "seed/story-4";
            };
        }

        static MockPreset pick(String text, StyleDescriptor style) {
            String normalized = normalize(text)
                + " "
                + normalize(style.subject())
                + " "
                + normalize(style.subjectType())
                + " "
                + normalize(style.mood())
                + " "
                + normalize(String.join(" ", style.keywords()))
                + " "
                + normalize(String.join(" ", style.sceneCues()));

            if (containsAny(normalized, "공주", "왕자", "왕국", "드레스", "왕관")) {
                return PRINCESS;
            }
            if (containsAny(normalized, "곰", "별", "숲", "토끼")) {
                return BEAR;
            }
            if (containsAny(normalized, "강아지", "탄이", "낮잠", "포메")) {
                return TANI;
            }
            if ("PERSON".equals(style.subjectType())) {
                return PRINCESS;
            }
            return TANI;
        }

        private static boolean containsAny(String text, String... tokens) {
            for (String token : tokens) {
                if (text.contains(token)) {
                    return true;
                }
            }
            return false;
        }

        private static String normalize(String s) {
            return s == null ? "" : s.toLowerCase(Locale.ROOT);
        }

        private static String replaceChildName(String text, String childName) {
            if (text == null) {
                return null;
            }
            if (childName == null) {
                return text;
            }
            return text.replace("{{childName}}", childName);
        }
    }
}
